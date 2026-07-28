package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.graphics.Bitmap
import com.kuzulabz.waifutaggercn.R
import com.kuzulabz.waifutaggercn.filterPromptNoiseTags

/**
 * 联合推理编排：精准模式下把 YOLO 检测 → 分割 → 裁剪 → WD Tagger 串起来。
 *
 * 两阶段精准模式流程：
 *   原图 → DetEngine(YOLO11n) 检测目标 → 发现人物/物体
 *        → SegEngine(YOLO11n-seg) 实例分割 → 生成 mask
 *        → 按 mask 裁剪多个主体区域
 *        → WD Tagger 分别识别每个裁剪区域 → 主体标签
 *        → WD Tagger(原图) → 背景/整体标签
 *        → 标签融合（主体加权、背景降权、去重）
 *        → 输出最终提示词
 *
 * 优雅降级：
 *   - 若 SegEngine 未加载 → 回退到 DetEngine bbox 裁剪模式
 *   - 若 DetEngine 未加载 → 回退到普通模式（仅原图打标）
 *   - 若未检测到目标 → 回退到普通模式
 */
class JointInference(
    val tagger: TaggerEngine,
    val det: DetEngine,
    val seg: SegEngine? = null,
    private val context: Context? = null
) {

    /** 解析字符串资源，无 Context 时返回空串 */
    private fun s(resId: Int, vararg args: Any): String =
        if (context != null) context.getString(resId, *args) else ""

    data class JointResult(
        val tags: List<TaggerEngine.Tag>,
        val objectsDetected: Boolean,
        val detectionCount: Int,
        val detectedClasses: List<String>,
        val cropBitmap: Bitmap?,          // 第一个裁剪主体（可用于 UI 预览）
        val segmentCount: Int,           // 分割实例数（0 = 未使用分割）
        val pipeline: String             // 实际使用的流水线名称
    )

    /**
     * @param precisionMode true=精准模式，false=普通模式（直接原图打标）
     */
    suspend fun run(
        bitmap: Bitmap,
        precisionMode: Boolean,
        threshold: Float,
        generalWeight: Float,
        characterWeight: Float,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): JointResult {
        if (!precisionMode || !det.isReady) {
            onProgress(0.5f, s(R.string.workflow_progress_tagger))
            val tags = tagger.tag(bitmap, threshold, generalWeight, characterWeight)
            return JointResult(
                tags, objectsDetected = false, detectionCount = 0,
                detectedClasses = emptyList(), cropBitmap = null,
                segmentCount = 0, pipeline = "Normal"
            )
        }

        // ===== Stage 1: YOLO11n detection =====
        onProgress(0.05f, s(R.string.workflow_progress_detect))
        val detections = det.detectObjects(bitmap)
        if (detections.isEmpty()) {
            onProgress(0.5f, s(R.string.workflow_progress_no_target))
            val tags = tagger.tag(bitmap, threshold, generalWeight, characterWeight)
            return JointResult(
                tags, objectsDetected = false, detectionCount = 0,
                detectedClasses = emptyList(), cropBitmap = null,
                segmentCount = 0, pipeline = "Normal (no target)"
            )
        }

        val detectedClasses = detections.map { it.className }.distinct()
        val detectedClassIds = detections.map { it.classId }.toSet()

        // ===== Stage 2: YOLO11n-seg segmentation =====
        val segReady = seg?.isReady == true
        if (segReady) {
            return runTwoStagePipeline(
                bitmap, threshold, generalWeight, characterWeight,
                detectedClasses, detectedClassIds, onProgress
            )
        }

        // Fallback: DetEngine bbox crop only
        onProgress(0.15f, s(R.string.workflow_progress_seg_not_loaded))
        return runDetectOnlyPipeline(
            bitmap, threshold, generalWeight, characterWeight,
            detections, detectedClasses, onProgress
        )
    }

    /**
     * 两阶段流水线：DetEngine 检测 → SegEngine 分割 → 多区域裁剪 → 分别打标 → 融合
     */
    private suspend fun runTwoStagePipeline(
        bitmap: Bitmap,
        threshold: Float,
        generalWeight: Float,
        characterWeight: Float,
        detectedClasses: List<String>,
        detectedClassIds: Set<Int>,
        onProgress: (Float, String) -> Unit
    ): JointResult {
        val segEngine = seg!!

        // Stage 2: instance segmentation
        onProgress(0.15f, s(R.string.workflow_progress_segment))
        val instances = segEngine.segmentAll(
            bitmap = bitmap,
            conf = 0.25f,
            iou = 0.45f,
            maxInstances = 10,
            classFilter = detectedClassIds
        )

        if (instances.isEmpty()) {
            onProgress(0.30f, s(R.string.workflow_progress_seg_not_found))
            val detections = det.detectObjects(bitmap)
            return runDetectOnlyPipeline(
                bitmap, threshold, generalWeight, characterWeight,
                detections, detectedClasses, onProgress
            )
        }

        // Stage 3: crop multiple regions by mask
        onProgress(0.35f, s(R.string.workflow_progress_crop, instances.size))
        val crops = segEngine.cropInstances(bitmap, instances)
        if (crops.isEmpty()) {
            onProgress(0.40f, s(R.string.workflow_progress_crop_failed))
            val detections = det.detectObjects(bitmap)
            return runDetectOnlyPipeline(
                bitmap, threshold, generalWeight, characterWeight,
                detections, detectedClasses, onProgress
            )
        }

        // Stage 4: WD Tagger for each cropped region
        val subjectTagsList = mutableListOf<List<TaggerEngine.Tag>>()
        crops.forEachIndexed { index, crop ->
            val pct = 0.40f + (0.40f * (index + 1) / crops.size)
            onProgress(pct, s(R.string.workflow_progress_tag_subject, index + 1, crops.size, crop.className))
            val tags = tagger.tag(crop.bitmap, threshold * 0.80f, generalWeight, characterWeight)
                .filterPromptNoiseTags()
            subjectTagsList.add(tags)
        }

        // Stage 5: background/overall tags (original image)
        onProgress(0.85f, s(R.string.workflow_progress_tag_background))
        val backgroundTags = tagger.tag(bitmap, threshold, generalWeight, characterWeight)
            .filterPromptNoiseTags()

        // Stage 6: tag fusion
        onProgress(0.97f, s(R.string.workflow_progress_fuse))
        val merged = fuseMultiRegionTags(
            subjectTagsList = subjectTagsList,
            backgroundTags = backgroundTags,
            subjectWeight = 1.15f,
            backgroundWeight = 0.85f
        )

        return JointResult(
            tags = merged,
            objectsDetected = true,
            detectionCount = instances.size,
            detectedClasses = detectedClasses,
            cropBitmap = crops.firstOrNull()?.bitmap,
            segmentCount = instances.size,
            pipeline = "Detect→Segment→Crop→Tag→Fuse"
        )
    }

    /**
     * 单阶段回退：DetEngine bbox 裁剪 → 打标
     */
    private suspend fun runDetectOnlyPipeline(
        bitmap: Bitmap,
        threshold: Float,
        generalWeight: Float,
        characterWeight: Float,
        detections: List<DetEngine.Detection>,
        detectedClasses: List<String>,
        onProgress: (Float, String) -> Unit
    ): JointResult {
        val crop = det.cropDetectedRegion(bitmap, detections)
            ?: run {
                onProgress(0.50f, s(R.string.workflow_progress_crop_failed))
                val tags = tagger.tag(bitmap, threshold, generalWeight, characterWeight)
                return JointResult(
                    tags, objectsDetected = false, detectionCount = detections.size,
                    detectedClasses = detectedClasses, cropBitmap = null,
                    segmentCount = 0, pipeline = "Detect (crop failed)"
                )
            }

        onProgress(0.45f, s(R.string.workflow_progress_tag_background))
        val subjectTags = tagger.tag(crop, threshold * 0.85f, generalWeight, characterWeight)
            .filterPromptNoiseTags()

        onProgress(0.85f, s(R.string.workflow_progress_tag_background))
        val backgroundTags = tagger.tag(bitmap, threshold, generalWeight, characterWeight)
            .filterPromptNoiseTags()

        onProgress(0.97f, s(R.string.workflow_progress_merge))
        val merged = mergeTags(
            subjectTags = subjectTags,
            backgroundTags = backgroundTags,
            subjectWeight = 1.15f,
            backgroundWeight = 0.85f
        )

        return JointResult(
            tags = merged,
            objectsDetected = true,
            detectionCount = detections.size,
            detectedClasses = detectedClasses,
            cropBitmap = crop,
            segmentCount = 0,
            pipeline = "检测→裁剪→打标"
        )
    }

    /**
     * 多区域标签融合：合并多个主体区域的标签与背景标签。
     * - 同名标签取最高分（已加权）
     * - 主体标签加权，背景标签降权
     * - 按 score 降序排列
     */
    private fun fuseMultiRegionTags(
        subjectTagsList: List<List<TaggerEngine.Tag>>,
        backgroundTags: List<TaggerEngine.Tag>,
        subjectWeight: Float,
        backgroundWeight: Float
    ): List<TaggerEngine.Tag> {
        val merged = linkedMapOf<String, TaggerEngine.Tag>()

        // 合并所有主体区域标签
        for (subjectTags in subjectTagsList) {
            for (tag in subjectTags) {
                val key = normalizeKey(tag.name)
                val weighted = tag.copy(score = (tag.score * subjectWeight).coerceAtMost(1.5f))
                val existing = merged[key]
                if (existing == null || weighted.score > existing.score) {
                    merged[key] = weighted
                }
            }
        }

        // 合并背景标签
        for (tag in backgroundTags) {
            val key = normalizeKey(tag.name)
            val weighted = tag.copy(score = tag.score * backgroundWeight)
            val existing = merged[key]
            if (existing == null) {
                merged[key] = weighted
            } else {
                if (weighted.score > existing.score) merged[key] = weighted
            }
        }

        return merged.values
            .sortedWith(compareByDescending<TaggerEngine.Tag> { it.score }.thenBy { it.name.lowercase() })
    }

    /**
     * 合并主体与背景标签：同义标签取高分，并按来源加权。
     */
    private fun mergeTags(
        subjectTags: List<TaggerEngine.Tag>,
        backgroundTags: List<TaggerEngine.Tag>,
        subjectWeight: Float,
        backgroundWeight: Float
    ): List<TaggerEngine.Tag> {
        val merged = linkedMapOf<String, TaggerEngine.Tag>()
        for (tag in subjectTags) {
            val key = normalizeKey(tag.name)
            val weighted = tag.copy(score = (tag.score * subjectWeight).coerceAtMost(1.5f))
            merged[key] = weighted
        }
        for (tag in backgroundTags) {
            val key = normalizeKey(tag.name)
            val weighted = tag.copy(score = tag.score * backgroundWeight)
            val existing = merged[key]
            if (existing == null) {
                merged[key] = weighted
            } else {
                if (weighted.score > existing.score) merged[key] = weighted
            }
        }
        return merged.values
            .sortedWith(compareByDescending<TaggerEngine.Tag> { it.score }.thenBy { it.name.lowercase() })
    }

    private fun normalizeKey(name: String): String =
        name.trim().lowercase().replace(' ', '_').replace(Regex("_+"), "_")
}
