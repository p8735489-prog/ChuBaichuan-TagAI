package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.StringRes
import com.kuzulabz.waifutaggercn.R
import com.kuzulabz.waifutaggercn.filterPromptNoiseTags
import org.json.JSONArray
import org.json.JSONObject

/**
 * AI 工作流引擎：让用户自由组合"检测 → 裁剪 → 打标 → 翻译"等步骤。
 *
 * 工作流是一个有序步骤列表 [WorkflowStep]，每步消耗上一步输出并产生新输出。
 * 执行器按顺序跑，支持进度回调。
 */
class WorkflowEngine(
    jointInference: JointInference,
    private val context: Context? = null
) {
    private val tagger: TaggerEngine = jointInference.tagger
    private val detEngine: DetEngine = jointInference.det
    private val segEngine: SegEngine? = jointInference.seg

    /** 解析字符串资源，无 Context 时返回空串 */
    private fun s(@StringRes resId: Int, vararg args: Any): String =
        if (context != null) context.getString(resId, *args) else ""

    /** 工作流步骤类型 */
    enum class StepType(val key: String, val displayNameZh: String, val displayNameEn: String) {
        INPUT("input", "输入图片", "Input Image"),
        DETECT("detect", "目标检测", "YOLO Detection"),
        SEGMENT("segment", "实例分割", "YOLO Segmentation"),
        CROP_PERSON("crop_person", "裁剪主体", "Crop Subject"),
        TAG("tag", "标签识别", "WD Tagger"),
        TRANSLATE("translate", "中文翻译", "Translate"),
        OUTPUT("output", "输出提示词", "Output Prompt");

        companion object {
            fun fromKey(key: String): StepType? = values().firstOrNull { it.key == key }
        }
    }

    /** 单个工作流步骤 */
    data class WorkflowStep(
        val type: StepType,
        val modelId: String? = null,   // 该步骤使用的模型 ID（如 tag 步骤选哪个 tagger）
        val params: Map<String, String> = emptyMap()
    )

    /** 完整工作流 */
    data class Workflow(
        val id: String,
        val name: String,
        val steps: List<WorkflowStep>,
        val isBuiltIn: Boolean = false
    )

    /** 执行结果 */
    data class WorkflowResult(
        val tags: List<TaggerEngine.Tag>,
        val intermediateBitmap: Bitmap?,  // 最后一步处理的图片（可能是裁剪后的）
        val log: List<String>,            // 每步的日志
        val success: Boolean,
        val errorMessage: String?
    )

    /** 内置预设工作流 */
    val builtInWorkflows: List<Workflow> = listOf(
        Workflow(
            id = "preset_normal",
            name = "Normal Mode",
            steps = listOf(
                WorkflowStep(StepType.INPUT),
                WorkflowStep(StepType.TAG),
                WorkflowStep(StepType.OUTPUT)
            ),
            isBuiltIn = true
        ),
        Workflow(
            id = "preset_precision",
            name = "Precision Mode (Detect + Segment + Tag)",
            steps = listOf(
                WorkflowStep(StepType.INPUT),
                WorkflowStep(StepType.DETECT),
                WorkflowStep(StepType.SEGMENT),
                WorkflowStep(StepType.CROP_PERSON),
                WorkflowStep(StepType.TAG),
                WorkflowStep(StepType.OUTPUT)
            ),
            isBuiltIn = true
        )
    )

    /**
     * 执行工作流。
     * @param bitmap 输入图片
     * @param workflow 工作流定义
     * @param threshold 标签阈值
     * @param generalWeight 通用标签权重
     * @param characterWeight 角色标签权重
     * @param onProgress (0..1, message) 进度回调
     */
    suspend fun execute(
        bitmap: Bitmap,
        workflow: Workflow,
        threshold: Float,
        generalWeight: Float,
        characterWeight: Float,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): WorkflowResult {
        val log = mutableListOf<String>()
        var currentBitmap: Bitmap = bitmap
        var tags: List<TaggerEngine.Tag> = emptyList()
        // 步骤间上下文：存储检测结果供裁剪步骤使用
        var detections: List<DetEngine.Detection> = emptyList()
        // 步骤间上下文：存储分割实例供裁剪步骤使用
        var segInstances: List<SegEngine.Instance> = emptyList()
        var segCrops: List<SegEngine.CropResult> = emptyList()
        val totalSteps = workflow.steps.size

        try {
            for ((index, step) in workflow.steps.withIndex()) {
                val progress = (index.toFloat() / totalSteps)
                val stepName = step.type.displayNameEn
                onProgress(progress, s(R.string.workflow_progress_executing, stepName))

                when (step.type) {
                    StepType.INPUT -> {
                        log.add("Input: ${bitmap.width}x${bitmap.height}")
                    }
                    StepType.DETECT -> {
                        if (!detEngine.isReady) {
                            val err = detEngine.load()
                            if (err != null) {
                                log.add(s(R.string.workflow_progress_det_not_loaded, err))
                                continue
                            }
                        }
                        onProgress(progress + 0.05f, s(R.string.workflow_progress_detect))
                        detections = detEngine.detectObjects(currentBitmap)
                        val classSummary = detections.map { it.className }.distinct()
                        log.add("Detect: ${detections.size} objects (${classSummary.joinToString(", ")})")
                    }
                    StepType.SEGMENT -> {
                        val currentSeg = segEngine
                        if (currentSeg == null || !currentSeg.isReady) {
                            log.add(s(R.string.workflow_progress_seg_not_loaded))
                            continue
                        }
                        onProgress(progress + 0.05f, s(R.string.workflow_progress_segment))
                        val classIds = if (detections.isNotEmpty()) {
                            detections.map { it.classId }.toSet()
                        } else null
                        segInstances = currentSeg.segmentAll(
                            bitmap = currentBitmap,
                            conf = 0.25f,
                            iou = 0.45f,
                            maxInstances = 10,
                            classFilter = classIds
                        )
                        log.add("Segment: ${segInstances.size} instance masks")
                    }
                    StepType.CROP_PERSON -> {
                        if (segInstances.isNotEmpty() && segEngine != null) {
                            segCrops = segEngine.cropInstances(currentBitmap, segInstances)
                            onProgress(progress + 0.05f, s(R.string.workflow_progress_crop, segCrops.size))
                            log.add("Crop: ${segCrops.size} regions (mask)")
                        } else if (detections.isNotEmpty()) {
                            val cropped = detEngine.cropDetectedRegion(currentBitmap, detections)
                            if (cropped != null) {
                                currentBitmap = cropped
                                log.add("Crop: ${cropped.width}x${cropped.height} (bbox)")
                            } else {
                                log.add(s(R.string.workflow_progress_crop_failed))
                            }
                        } else {
                            log.add("No targets to crop, using original")
                        }
                    }
                    StepType.TAG -> {
                        if (segCrops.isNotEmpty()) {
                            val allTags = mutableListOf<TaggerEngine.Tag>()
                            segCrops.forEachIndexed { i, crop ->
                                onProgress(progress + 0.05f * (i + 1) / segCrops.size,
                                    s(R.string.workflow_progress_tag_subject, i + 1, segCrops.size, crop.className))
                                val cropTags = tagger.tag(crop.bitmap, threshold * 0.80f, generalWeight, characterWeight)
                                    .filterPromptNoiseTags()
                                allTags.addAll(cropTags)
                                log.add("  Subject ${i + 1} (${crop.className}): ${cropTags.size} tags")
                            }
                            onProgress(progress + 0.08f, s(R.string.workflow_progress_tag_background))
                            val bgTags = tagger.tag(bitmap, threshold, generalWeight, characterWeight)
                                .filterPromptNoiseTags()
                            val merged = linkedMapOf<String, TaggerEngine.Tag>()
                            for (tag in allTags) {
                                val key = tag.name.trim().lowercase().replace(' ', '_')
                                val weighted = tag.copy(score = (tag.score * 1.15f).coerceAtMost(1.5f))
                                val existing = merged[key]
                                if (existing == null || weighted.score > existing.score) merged[key] = weighted
                            }
                            for (tag in bgTags) {
                                val key = tag.name.trim().lowercase().replace(' ', '_')
                                val weighted = tag.copy(score = tag.score * 0.85f)
                                val existing = merged[key]
                                if (existing == null || weighted.score > existing.score) merged[key] = weighted
                            }
                            tags = merged.values
                                .sortedWith(compareByDescending<TaggerEngine.Tag> { it.score }.thenBy { it.name.lowercase() })
                            log.add("Tags: ${tags.size} (${segCrops.size + 1} regions fused)")
                        } else {
                            onProgress(progress + 0.05f, s(R.string.workflow_progress_tagger))
                            tags = tagger.tag(currentBitmap, threshold, generalWeight, characterWeight)
                                .filterPromptNoiseTags()
                            log.add("Tags: ${tags.size}")
                        }
                    }
                    StepType.TRANSLATE -> {
                        log.add("Translate marked")
                    }
                    StepType.OUTPUT -> {
                        onProgress(0.98f, s(R.string.workflow_progress_output))
                        log.add("Output: ${tags.size} tags")
                    }
                }
            }
            onProgress(1f, s(R.string.workflow_progress_done))
            return WorkflowResult(tags, currentBitmap, log, success = true, errorMessage = null)
        } catch (e: Exception) {
            Log.e("WorkflowEngine", "执行失败", e)
            return WorkflowResult(tags, currentBitmap, log, success = false, errorMessage = e.message)
        }
    }

    // ---- 序列化 / 持久化 ----

    fun workflowToJson(workflow: Workflow): String {
        val arr = JSONArray()
        workflow.steps.forEach { step ->
            val obj = JSONObject()
            obj.put("type", step.type.key)
            step.modelId?.let { obj.put("modelId", it) }
            if (step.params.isNotEmpty()) {
                val params = JSONObject()
                step.params.forEach { (k, v) -> params.put(k, v) }
                obj.put("params", params)
            }
            arr.put(obj)
        }
        val root = JSONObject()
        root.put("id", workflow.id)
        root.put("name", workflow.name)
        root.put("steps", arr)
        return root.toString()
    }

    fun jsonToWorkflow(json: String): Workflow? {
        return try {
            val obj = JSONObject(json)
            val steps = mutableListOf<WorkflowStep>()
            val arr = obj.getJSONArray("steps")
            for (i in 0 until arr.length()) {
                val stepObj = arr.getJSONObject(i)
                val type = StepType.fromKey(stepObj.getString("type")) ?: continue
                val modelId = stepObj.optString("modelId", "").takeIf { it.isNotEmpty() }
                steps.add(WorkflowStep(type, modelId))
            }
            Workflow(
                id = obj.optString("id", "custom_${System.currentTimeMillis()}"),
                name = obj.optString("name", "Custom Workflow"),
                steps = steps
            )
        } catch (e: Exception) {
            Log.w("WorkflowEngine", "JSON parse failed", e)
            null
        }
    }

    /** 加载用户保存的自定义工作流列表 */
    fun loadCustomWorkflows(context: Context): List<Workflow> {
        val prefs = context.getSharedPreferences("workflows", Context.MODE_PRIVATE)
        val count = prefs.getInt("count", 0)
        val list = mutableListOf<Workflow>()
        for (i in 0 until count) {
            val json = prefs.getString("workflow_$i", null) ?: continue
            jsonToWorkflow(json)?.let { list.add(it) }
        }
        return list
    }

    fun saveCustomWorkflow(context: Context, workflow: Workflow) {
        val prefs = context.getSharedPreferences("workflows", Context.MODE_PRIVATE)
        val count = prefs.getInt("count", 0)
        prefs.edit().putString("workflow_$count", workflowToJson(workflow)).putInt("count", count + 1).apply()
    }

    fun deleteCustomWorkflow(context: Context, workflowId: String) {
        val prefs = context.getSharedPreferences("workflows", Context.MODE_PRIVATE)
        val count = prefs.getInt("count", 0)
        val remaining = mutableListOf<String>()
        for (i in 0 until count) {
            val json = prefs.getString("workflow_$i", null) ?: continue
            val wf = jsonToWorkflow(json)
            if (wf != null && wf.id != workflowId) remaining.add(json)
        }
        val edit = prefs.edit().clear()
        remaining.forEachIndexed { idx, json -> edit.putString("workflow_$idx", json) }
        edit.putInt("count", remaining.size).apply()
    }
}
