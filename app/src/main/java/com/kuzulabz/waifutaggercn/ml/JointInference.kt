package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.graphics.Bitmap
import com.kuzulabz.waifutaggercn.R
import com.kuzulabz.waifutaggercn.filterPromptNoiseTags

/**
 * 联合推理编排：精准模式下把 YOLO 检测 → 分割 → 裁剪 → WD Tagger 串起来。
 *
 * 第二阶段升级：
 *   - 角色实例评分（CharacterScorer）
 *   - 角色标签隔离（每角色独立过滤）
 *   - 调试信息输出
 *   - 提示词长度控制
 *
 * 流程：
 *   原图 → DetEngine(YOLO11n) 检测 → SegEngine(YOLO11n-seg) 分割
 *        → 按实例裁剪 → WD Tagger 分别识别
 *        → CharacterScorer 评分 → 角色标签独立过滤
 *        → 融合 + 长度控制 → 输出
 */
class JointInference(
    val tagger: TaggerEngine,
    val det: DetEngine,
    val seg: SegEngine? = null,
    private val context: Context? = null
) {

    /** 标签过滤器：分类、冲突解决、数量限制 */
    private val tagFilter = TagFilter()

    /** 角色评分系统 */
    private val characterScorer = CharacterScorer()

    /** 提示词长度控制 */
    private val lengthController = PromptLengthController()

    /** 输出格式化 */
    private val outputFormatter = TagOutputFormatter()

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
        val pipeline: String,            // 实际使用的流水线名称
        val characters: List<CharacterInstance> = emptyList(),  // 角色实例列表
        val debugInfo: String = ""       // 调试信息
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
            // 普通模式：应用 TagFilter + 长度控制
            val filtered = tagFilter.process(tags)
            val limited = lengthController.limitByScore(
                filtered,
                PromptLengthController.OutputMode.NORMAL
            )
            return JointResult(
                limited, objectsDetected = false, detectionCount = 0,
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
            val filtered = tagFilter.process(tags)
            val limited = lengthController.limitByScore(
                filtered,
                PromptLengthController.OutputMode.NORMAL
            )
            return JointResult(
                limited, objectsDetected = false, detectionCount = 0,
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
     * 两阶段流水线：检测 → 分割 → 多区域裁剪 → 分别打标 → 角色评分 → 标签隔离 → 融合
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

        // Stage 4: WD Tagger for each cropped region + 构建角色实例
        val characterInstances = mutableListOf<CharacterInstance>()
        val totalTagsBeforeFilter = mutableListOf<Int>()
        crops.forEachIndexed { index, crop ->
            val pct = 0.35f + (0.35f * (index + 1) / crops.size)
            onProgress(pct, s(R.string.workflow_progress_tag_subject, index + 1, crops.size, crop.className))
            val tags = tagger.tag(crop.bitmap, threshold * 0.80f, generalWeight, characterWeight)
                .filterPromptNoiseTags()
            totalTagsBeforeFilter.add(tags.size)

            // 每个角色实例独立过滤标签（角色隔离）
            val filteredTags = tagFilter.processSingleCharacter(tags)
            val instance = CharacterInstance(
                id = index,
                bbox = instances[index].bbox,
                mask = instances[index].mask,
                cropImage = crop.bitmap,
                tags = tags,
                filteredTags = filteredTags,
                yoloConfidence = crop.score,
                area = characterScorer.calculateAreaRatio(
                    instances[index].mask, bitmap.width, bitmap.height
                ),
                className = crop.className,
                classId = crop.classId
            )
            characterInstances.add(instance)
        }

        // Stage 5: 角色评分 + 排序
        onProgress(0.75f, s(R.string.workflow_progress_tag_background))
        val scoredCharacters = characterScorer.scoreAndRank(
            characterInstances, bitmap.width, bitmap.height
        )

        // Stage 6: background/overall tags (original image)
        onProgress(0.85f, s(R.string.workflow_progress_tag_background))
        val backgroundTags = tagger.tag(bitmap, threshold, generalWeight, characterWeight)
            .filterPromptNoiseTags()

        // Stage 7: 融合 — 主体标签加权 + 背景标签降权 + 冲突过滤
        onProgress(0.95f, s(R.string.workflow_progress_fuse))
        val subjectTagsList = scoredCharacters.map { it.filteredTags }
        val limits = TagFilter.TagLimits(instanceCount = scoredCharacters.size)
        val merged = tagFilter.fuseAndFilter(
            subjectTagsList = subjectTagsList,
            backgroundTags = backgroundTags,
            subjectWeight = 1.15f,
            backgroundWeight = 0.85f,
            limits = limits
        )

        // 长度控制：精准模式最多 50 个标签
        val limited = lengthController.limitByScore(
            merged,
            PromptLengthController.OutputMode.PRECISION
        )

        // 调试信息
        val totalBefore = totalTagsBeforeFilter.sum()
        val totalAfter = limited.size
        val debugInfo = outputFormatter.formatDebug(
            characters = scoredCharacters,
            totalTagsBeforeFilter = totalBefore,
            totalTagsAfterFilter = totalAfter,
            pipeline = "Detect→Segment→Crop→Score→Tag→Isolate→Fuse"
        )

        return JointResult(
            tags = limited,
            objectsDetected = true,
            detectionCount = instances.size,
            detectedClasses = detectedClasses,
            cropBitmap = crops.firstOrNull()?.bitmap,
            segmentCount = instances.size,
            pipeline = "Detect→Segment→Crop→Score→Tag→Isolate→Fuse",
            characters = scoredCharacters,
            debugInfo = debugInfo
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
                val filtered = tagFilter.process(tags)
                val limited = lengthController.limitByScore(
                    filtered, PromptLengthController.OutputMode.NORMAL
                )
                return JointResult(
                    limited, objectsDetected = false, detectionCount = detections.size,
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
        val merged = tagFilter.fuseAndFilter(
            subjectTagsList = listOf(subjectTags),
            backgroundTags = backgroundTags,
            subjectWeight = 1.15f,
            backgroundWeight = 0.85f
        )

        // 长度控制
        val limited = lengthController.limitByScore(
            merged, PromptLengthController.OutputMode.NORMAL
        )

        return JointResult(
            tags = limited,
            objectsDetected = true,
            detectionCount = detections.size,
            detectedClasses = detectedClasses,
            cropBitmap = crop,
            segmentCount = 0,
            pipeline = "Detect→Crop→Tag→Filter"
        )
    }
}
