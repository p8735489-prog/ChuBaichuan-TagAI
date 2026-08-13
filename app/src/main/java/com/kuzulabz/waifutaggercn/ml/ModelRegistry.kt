package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.kuzulabz.waifutaggercn.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 统一模型注册表：管理所有类型的 AI 模型（标签识别 / 目标检测 / 实例分割 / 图像增强）。
 *
 * 数据来源优先级：
 * 1. assets/models.json（随 APK 打包的本地仓库）
 * 2. 内置默认列表 [defaultCatalog]
 *
 * 每个模型条目包含名称、类型、下载地址、大小、推荐设备、速度/精度评级等元信息。
 */
object ModelRegistry {

    /** 模型大类 */
    enum class ModelCategory(
        val key: String,
        val displayNameZh: String,
        val displayNameEn: String,
        @StringRes val displayNameResId: Int
    ) {
        TAGGER("tagger", "标签识别", "Tagger", R.string.model_cat_tagger),
        DETECTION("detection", "目标检测", "Detection", R.string.model_cat_detection),
        SEGMENTATION("segmentation", "实例分割", "Segmentation", R.string.model_cat_segmentation);

        companion object {
            fun fromKey(key: String): ModelCategory =
                values().firstOrNull { it.key == key } ?: TAGGER
        }
    }

    /** 后端运行时 */
    enum class Backend(val key: String) {
        ONNX("onnx"),
        TFLITE("tflite");

        companion object {
            fun fromKey(key: String): Backend = values().firstOrNull { it.key == key } ?: ONNX
        }
    }

    /** 单个模型条目 */
    data class ModelEntry(
        val id: String,
        val name: String,
        val category: ModelCategory,
        val version: String,
        val sizeLabel: String,
        val sizeBytes: Long,
        val backend: Backend,
        val downloadUrl: String,
        val tagFileUrl: String?,         // 标签文件地址（仅 Tagger 需要）
        val repoName: String,            // 本地文件名前缀（如 "wd-vit-tagger-v3"）
        val recommendedDevice: String,
        val speedRank: Int,              // 0-100，越高越快
        val accuracyRank: Int,           // 0-100，越高越准
        val description: String,
        val descKey: String = "",
        val inputSize: Int,              // 模型输入尺寸
        val isOfficial: Boolean,
        val hfRepo: String = "",         // HuggingFace 仓库全路径（如 "Camais03/camie-tagger-v2"），空则用默认 SmilingWolf 仓库
        val onnxFile: String = "",       // HF 仓库中的 ONNX 文件名（如 "camie-tagger-v2.onnx"），空则用 "model.onnx"
        val tagFile: String = ""         // HF 仓库中的标签文件名（如 "camie-tagger-v2-metadata.json"），空则用 "selected_tags.csv"
    )

    /** 默认内置目录——当 models.json 不存在或解析失败时使用 */
    val defaultCatalog: List<ModelEntry> = buildList {
        // ---- Tagger ----
        add(ModelEntry("wd-eva02-large-tagger-v3", "WD EVA02 Large Tagger v3", ModelCategory.TAGGER, "v3", "~ 1.4GB", 1_500_000_000L, Backend.ONNX, "", null, "wd-eva02-large-tagger-v3", "骁龙 8 Gen3+", 35, 100, "Strongest Danbooru tagger model, large input, requires high performance", "ai_model_desc_wd_eva02_large_v3", 448, true))
        add(ModelEntry("wd-convnext-tagger-v3", "WD ConvNeXt Tagger v3", ModelCategory.TAGGER, "v3", "~ 377MB", 395_000_000L, Backend.ONNX, "", null, "wd-convnext-tagger-v3", "骁龙 8 Gen2+", 78, 88, "Balanced tagger model, good speed and accuracy", "ai_model_desc_wd_convnext_v3", 448, true))
        add(ModelEntry("wd-swinv2-tagger-v3", "WD SwinV2 Tagger v3", ModelCategory.TAGGER, "v3", "~ 342MB", 359_000_000L, Backend.ONNX, "", null, "wd-swinv2-tagger-v3", "骁龙 8 Gen2+", 72, 84, "Swin Transformer, excels at detailed tags", "ai_model_desc_wd_swinv2_v3", 448, true))
        add(ModelEntry("wd-vit-tagger-v3", "WD ViT Tagger v3", ModelCategory.TAGGER, "v3", "~ 327MB", 343_000_000L, Backend.ONNX, "", null, "wd-vit-tagger-v3", "骁龙 8+ Gen1+", 88, 80, "ViT architecture, fastest speed", "ai_model_desc_wd_vit_v3", 448, true))
        add(ModelEntry("wd-v1-4-moat-tagger-v2", "WD v1.4 MOAT Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-moat-tagger-v2", "骁龙 8+ Gen1+", 58, 76, "MOAT architecture, proven and stable", "ai_model_desc_wd_v14_moat_v2", 448, true))
        add(ModelEntry("wd-v1-4-convnextv2-tagger-v2", "WD v1.4 ConvNeXtV2 Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-convnextv2-tagger-v2", "骁龙 8+ Gen1+", 68, 72, "ConvNeXtV2 architecture", "ai_model_desc_wd_v14_convnextv2_v2", 448, true))
        add(ModelEntry("wd-v1-4-convnext-tagger-v2", "WD v1.4 ConvNeXt Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-convnext-tagger-v2", "骁龙 8 Gen1+", 72, 68, "Classic ConvNeXt architecture", "ai_model_desc_wd_v14_convnext_v2", 448, true))
        add(ModelEntry("wd-v1-4-swinv2-tagger-v2", "WD v1.4 SwinV2 Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-swinv2-tagger-v2", "骁龙 8 Gen1+", 62, 66, "SwinV2 architecture", "ai_model_desc_wd_v14_swinv2_v2", 448, true))
        add(ModelEntry("wd-v1-4-vit-tagger-v2", "WD v1.4 ViT Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-vit-tagger-v2", "骁龙 7+ Gen2+", 82, 62, "Lightweight ViT architecture", "ai_model_desc_wd_v14_vit_v2", 448, true))
        add(ModelEntry("wd-v1-4-vit-tagger", "WD v1.4 ViT Tagger", ModelCategory.TAGGER, "v1.4", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-vit-tagger", "骁龙 7+ Gen2+", 86, 58, "Earliest version, best compatibility", "ai_model_desc_wd_v14_vit", 448, true))

        // ---- 新增强力 Tagger 模型 ----
        add(ModelEntry("camie-tagger-v2", "Camie Tagger v2", ModelCategory.TAGGER, "v2", "~ 430MB", 451_000_000L, Backend.ONNX, "", null, "camie-tagger-v2", "骁龙 8 Gen2+", 65, 95, "Camie v2 tagger, excels at character and detail recognition, broad tag coverage", "ai_model_desc_camie_v2", 448, true, hfRepo = "Camais03/camie-tagger-v2", onnxFile = "camie-tagger-v2.onnx", tagFile = "camie-tagger-v2-metadata.json"))
        add(ModelEntry("pixai-tagger-v0.9", "PixAI Tagger v0.9", ModelCategory.TAGGER, "v0.9", "~ 380MB", 399_000_000L, Backend.ONNX, "", null, "pixai-tagger-v0.9", "骁龙 8 Gen2+", 70, 92, "PixAI v0.9 tagger, optimized for anime illustration tagging, high accuracy on style tags", "ai_model_desc_pixai_v09", 448, true, hfRepo = "deepghs/pixai-tagger-v0.9-onnx", onnxFile = "model.onnx", tagFile = "selected_tags.csv"))
        add(ModelEntry("animetimm-danbooru-dbv4", "AnimeTimm ResNet34 DBv4-full", ModelCategory.TAGGER, "DBv4", "~ 350MB", 367_000_000L, Backend.ONNX, "", null, "animetimm-danbooru-dbv4", "骁龙 8+ Gen1+", 75, 90, "AnimeTimm ResNet34 with Danbooru DBv4 tag set, strong generalization across anime styles", "ai_model_desc_animetimm_dbv4", 448, true, hfRepo = "animetimm/resnet34.dbv4-full", onnxFile = "model.onnx", tagFile = "selected_tags.csv"))

        // ---- Detection ----
        add(ModelEntry("yolo11n", "YOLO11n", ModelCategory.DETECTION, "11", "~ 3MB", 3_000_000L, Backend.ONNX, "", null, "yolo11n", "所有设备", 98, 62, "Nano detection model, ultra-fast inference", "ai_model_desc_yolo_det", 640, true))
        add(ModelEntry("yolo11s", "YOLO11s", ModelCategory.DETECTION, "11", "~ 9MB", 9_000_000L, Backend.ONNX, "", null, "yolo11s", "骁龙 7+ Gen2+", 92, 75, "Small detection model, balanced speed and accuracy", "ai_model_desc_yolo_det", 640, true))
        add(ModelEntry("yolo11m", "YOLO11m", ModelCategory.DETECTION, "11", "~ 20MB", 20_000_000L, Backend.ONNX, "", null, "yolo11m", "骁龙 8 Gen2+", 80, 84, "Medium detection model, higher accuracy", "ai_model_desc_yolo_det", 640, true))
        add(ModelEntry("yolov8n", "YOLOv8n", ModelCategory.DETECTION, "8", "~ 6MB", 6_000_000L, Backend.ONNX, "", null, "yolov8n", "所有设备", 96, 60, "Classic v8 nano detection", "ai_model_desc_yolo_det", 640, true))

        // ---- Segmentation ----
        add(ModelEntry("yolo11n-seg", "YOLO11n-seg", ModelCategory.SEGMENTATION, "11", "~ 4MB", 4_000_000L, Backend.ONNX, "", null, "yolo11n-seg", "所有设备", 97, 60, "Nano instance segmentation, person mask", "ai_model_desc_yolo_seg", 640, true))
        add(ModelEntry("yolo11s-seg", "YOLO11s-seg", ModelCategory.SEGMENTATION, "11", "~ 11MB", 11_000_000L, Backend.ONNX, "", null, "yolo11s-seg", "骁龙 7+ Gen2+", 90, 74, "Small instance segmentation, more precise mask", "ai_model_desc_yolo_seg", 640, true))
        add(ModelEntry("yolov8n-seg", "YOLOv8n-seg", ModelCategory.SEGMENTATION, "8", "~ 6MB", 6_000_000L, Backend.ONNX, "", null, "yolov8n-seg", "所有设备", 95, 58, "Classic v8 nano instance segmentation", "ai_model_desc_yolo_seg", 640, true))
    }

    /**
     * 从 assets/models.json 加载模型目录；解析失败时回退到 [defaultCatalog]。
     */
    fun loadCatalog(context: Context): List<ModelEntry> {
        return try {
            val json = context.assets.open("models.json").bufferedReader().use { it.readText() }
            parseCatalog(json)
        } catch (e: Exception) {
            Log.w("ModelRegistry", "models.json not found or invalid, using default catalog", e)
            defaultCatalog
        }
    }

    private fun parseCatalog(json: String): List<ModelEntry> {
        val arr = JSONArray(json)
        val list = mutableListOf<ModelEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                ModelEntry(
                    id = obj.optString("id", obj.optString("name").lowercase().replace(' ', '-')),
                    name = obj.optString("name", ""),
                    category = ModelCategory.fromKey(obj.optString("type", "tagger")),
                    version = obj.optString("version", ""),
                    sizeLabel = obj.optString("size", ""),
                    sizeBytes = obj.optLong("sizeBytes", 0L),
                    backend = Backend.fromKey(obj.optString("backend", "onnx")),
                    downloadUrl = obj.optString("url", ""),
                    tagFileUrl = obj.optString("tagFileUrl", "").takeIf { it.isNotEmpty() },
                    repoName = obj.optString("repoName", obj.optString("name").lowercase().replace(' ', '-')),
                    recommendedDevice = obj.optString("recommended", ""),
                    speedRank = obj.optInt("speedRank", 50),
                    accuracyRank = obj.optInt("accuracyRank", 50),
                    description = obj.optString("description", ""),
                    descKey = obj.optString("descKey", ""),
                    inputSize = obj.optInt("inputSize", 640),
                    isOfficial = obj.optBoolean("official", true),
                    hfRepo = obj.optString("hfRepo", ""),
                    onnxFile = obj.optString("onnxFile", ""),
                    tagFile = obj.optString("tagFile", "")
                )
            )
        }
        return if (list.isEmpty()) defaultCatalog else list
    }

    /**
     * 检查某个模型是否已安装（在 filesDir/ai_models 目录下有对应 .onnx 文件）。
     */
    fun isInstalled(context: Context, entry: ModelEntry): Boolean {
        val dir = TaggerEngine.modelDirectory(context)
        val onnxFile = File(dir, "${entry.repoName}.onnx")
        if (onnxFile.exists() && onnxFile.length() > 0L) return true
        return false
    }

    /** 检查模型是否作为内置资源打包在 assets 中（如 yolo11n-seg.onnx） */
    fun isBuiltinInAssets(context: Context, entry: ModelEntry): Boolean {
        return try {
            context.assets.open("${entry.repoName}.onnx").use { stream ->
                stream.read() >= 0
            }
        } catch (e: Exception) {
            false
        }
    }

    /** 获取某个分类下所有模型 */
    fun byCategory(catalog: List<ModelEntry>, cat: ModelCategory): List<ModelEntry> =
        catalog.filter { it.category == cat }

    /** 获取已安装的所有模型（跨分类） */
    fun installedEntries(context: Context, catalog: List<ModelEntry>): List<ModelEntry> =
        catalog.filter { isInstalled(context, it) }
}
