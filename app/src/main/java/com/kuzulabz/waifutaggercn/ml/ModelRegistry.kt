package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.util.Log
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
    enum class ModelCategory(val key: String, val displayNameZh: String, val displayNameEn: String) {
        TAGGER("tagger", "标签识别", "Tagger"),
        DETECTION("detection", "目标检测", "Detection"),
        SEGMENTATION("segmentation", "实例分割", "Segmentation");

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
        val repoName: String,            // HF 仓库名（用于镜像切换）
        val recommendedDevice: String,
        val speedRank: Int,              // 0-100，越高越快
        val accuracyRank: Int,           // 0-100，越高越准
        val description: String,
        val inputSize: Int,              // 模型输入尺寸
        val isOfficial: Boolean
    )

    /** 默认内置目录——当 models.json 不存在或解析失败时使用 */
    val defaultCatalog: List<ModelEntry> = buildList {
        // ---- Tagger ----
        add(ModelEntry("wd-eva02-large-tagger-v3", "WD EVA02 Large Tagger v3", ModelCategory.TAGGER, "v3", "~ 1.4GB", 1_500_000_000L, Backend.ONNX, "", null, "wd-eva02-large-tagger-v3", "骁龙 8 Gen3+", 35, 100, "最强 Danbooru 标签模型，大尺寸输入，需要较高性能", 448, true))
        add(ModelEntry("wd-convnext-tagger-v3", "WD ConvNeXt Tagger v3", ModelCategory.TAGGER, "v3", "~ 377MB", 395_000_000L, Backend.ONNX, "", null, "wd-convnext-tagger-v3", "骁龙 8 Gen2+", 78, 88, "均衡的标签模型，速度与精度兼顾", 448, true))
        add(ModelEntry("wd-swinv2-tagger-v3", "WD SwinV2 Tagger v3", ModelCategory.TAGGER, "v3", "~ 342MB", 359_000_000L, Backend.ONNX, "", null, "wd-swinv2-tagger-v3", "骁龙 8 Gen2+", 72, 84, "Swin Transformer 架构，擅长细节标签", 448, true))
        add(ModelEntry("wd-vit-tagger-v3", "WD ViT Tagger v3", ModelCategory.TAGGER, "v3", "~ 327MB", 343_000_000L, Backend.ONNX, "", null, "wd-vit-tagger-v3", "骁龙 8+ Gen1+", 88, 80, "ViT 架构，速度最快", 448, true))
        add(ModelEntry("wd-v1-4-moat-tagger-v2", "WD v1.4 MOAT Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-moat-tagger-v2", "骁龙 8+ Gen1+", 58, 76, "MOAT 架构，老牌稳定", 448, true))
        add(ModelEntry("wd-v1-4-convnextv2-tagger-v2", "WD v1.4 ConvNeXtV2 Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-convnextv2-tagger-v2", "骁龙 8+ Gen1+", 68, 72, "ConvNeXtV2 架构", 448, true))
        add(ModelEntry("wd-v1-4-convnext-tagger-v2", "WD v1.4 ConvNeXt Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-convnext-tagger-v2", "骁龙 8 Gen1+", 72, 68, "经典 ConvNeXt 架构", 448, true))
        add(ModelEntry("wd-v1-4-swinv2-tagger-v2", "WD v1.4 SwinV2 Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-swinv2-tagger-v2", "骁龙 8 Gen1+", 62, 66, "SwinV2 架构", 448, true))
        add(ModelEntry("wd-v1-4-vit-tagger-v2", "WD v1.4 ViT Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-vit-tagger-v2", "骁龙 7+ Gen2+", 82, 62, "轻量 ViT 架构", 448, true))
        add(ModelEntry("wd-v1-4-vit-tagger", "WD v1.4 ViT Tagger", ModelCategory.TAGGER, "v1.4", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-vit-tagger", "骁龙 7+ Gen2+", 86, 58, "最早版本，兼容性最好", 448, true))

        // ---- Detection ----
        add(ModelEntry("yolo11n", "YOLO11n", ModelCategory.DETECTION, "11", "~ 3MB", 3_000_000L, Backend.ONNX, "", null, "yolo11n", "所有设备", 98, 62, "nano 检测模型，极速推理", 640, true))
        add(ModelEntry("yolo11s", "YOLO11s", ModelCategory.DETECTION, "11", "~ 9MB", 9_000_000L, Backend.ONNX, "", null, "yolo11s", "骁龙 7+ Gen2+", 92, 75, "small 检测模型，速度与精度均衡", 640, true))
        add(ModelEntry("yolo11m", "YOLO11m", ModelCategory.DETECTION, "11", "~ 20MB", 20_000_000L, Backend.ONNX, "", null, "yolo11m", "骁龙 8 Gen2+", 80, 84, "medium 检测模型，更高精度", 640, true))
        add(ModelEntry("yolov8n", "YOLOv8n", ModelCategory.DETECTION, "8", "~ 6MB", 6_000_000L, Backend.ONNX, "", null, "yolov8n", "所有设备", 96, 60, "经典 v8 nano 检测", 640, true))

        // ---- Segmentation ----
        add(ModelEntry("yolo11n-seg", "YOLO11n-seg", ModelCategory.SEGMENTATION, "11", "~ 4MB", 4_000_000L, Backend.ONNX, "", null, "yolo11n-seg", "所有设备", 97, 60, "nano 实例分割，人物轮廓 mask", 640, true))
        add(ModelEntry("yolo11s-seg", "YOLO11s-seg", ModelCategory.SEGMENTATION, "11", "~ 11MB", 11_000_000L, Backend.ONNX, "", null, "yolo11s-seg", "骁龙 7+ Gen2+", 90, 74, "small 实例分割，更精确轮廓", 640, true))
        add(ModelEntry("yolov8n-seg", "YOLOv8n-seg", ModelCategory.SEGMENTATION, "8", "~ 6MB", 6_000_000L, Backend.ONNX, "", null, "yolov8n-seg", "所有设备", 95, 58, "经典 v8 nano 实例分割", 640, true))
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
                    inputSize = obj.optInt("inputSize", 640),
                    isOfficial = obj.optBoolean("official", true)
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
