package Shirakawa.LocalCueWord.ml

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import Shirakawa.LocalCueWord.R
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
        val tagFile: String = "",        // HF 仓库中的标签文件名（如 "camie-tagger-v2-metadata.json"），空则用 "selected_tags.csv"
        // 适用图片类型：anime(二次元) / real(现实) / general(通用) / anime_real(二次元/现实)
        val supportedImageTypes: String = "anime",
        /**
         * 除主 ONNX 之外的额外必需文件（HF 仓库中的文件名）。
         *
         * 主要服务于 ONNX external data 格式：模型图在 model.onnx，权重在 model.onnx.data。
         * ⚠️ 新字段一律追加在末尾并给默认值，避免破坏既有「具名 + 位置」混用的构造调用。
         */
        val extraFiles: List<String> = emptyList(),
        /**
         * 与 [extraFiles] 一一对应的**下载源文件名**。
         *
         * 本地落盘名与 HF 上的名字可能不同（例如本地为 model.onnx.data，
         * 而某些仓库叫 pixai-tagger-v1.0.onnx.data）。为空时按同下标取 [extraFiles]
         * 中的名字作为下载名。
         */
        val extraFileSources: List<String> = emptyList()
    )

    /** 默认内置目录——当 models.json 不存在或解析失败时使用 */
    val defaultCatalog: List<ModelEntry> = buildList {
        // ---- Tagger（按精度 accuracyRank 从高到低排列）----
        add(ModelEntry("wd-eva02-large-tagger-v3", "WD EVA02 Large Tagger v3", ModelCategory.TAGGER, "v3", "~ 1.4GB", 1_500_000_000L, Backend.ONNX, "", null, "wd-eva02-large-tagger-v3", "骁龙 8 Gen3+", 35, 100, "Strongest Danbooru tagger model, large input, requires high performance", "ai_model_desc_wd_eva02_large_v3", 448, true))
        add(ModelEntry("camie-tagger-v2", "Camie Tagger v2", ModelCategory.TAGGER, "v2", "~ 789MB", 789_000_000L, Backend.ONNX, "", null, "camie-tagger-v2", "骁龙 8 Gen2+", 65, 95, "Camie v2 tagger, excels at character and detail recognition, broad tag coverage", "ai_model_desc_camie_v2", 512, true, hfRepo = "Camais03/camie-tagger-v2", onnxFile = "camie-tagger-v2.onnx", tagFile = "camie-tagger-v2-metadata.json", supportedImageTypes = "anime"))
        add(ModelEntry("pixai-tagger-v0.9", "PixAI Tagger v0.9", ModelCategory.TAGGER, "v0.9", "~ 1.27GB", 1_271_000_000L, Backend.ONNX, "", null, "pixai-tagger-v0.9", "骁龙 8 Gen2+", 70, 92, "PixAI v0.9 tagger, optimized for anime illustration tagging, high accuracy on style tags", "ai_model_desc_pixai_v09", 448, true, hfRepo = "deepghs/pixai-tagger-v0.9-onnx", onnxFile = "model.onnx", tagFile = "selected_tags.csv", supportedImageTypes = "anime"))
        // PixAI Tagger v1.0：官方 ONNX 导出采用 external data 格式——
        // model.onnx 只是图定义（~1KB），真正的权重在同目录的 model.onnx.data 分片中。
        // ONNX Runtime 按「<onnx文件路径>.data」约定查找分片，因此必须保证：
        //   1) .onnx 与 .data 同名同目录；
        //   2) .onnx 文件名保持 HF 原始名 model.onnx（不能重命名为 {repoName}.onnx）。
        // 为此该模型的三个文件统一落到 ai_models/pixai-tagger-v1.0/ 子目录里。
        add(ModelEntry("pixai-tagger-v1.0", "PixAI Tagger v1.0", ModelCategory.TAGGER, "v1.0", "~ 3.2GB", 3_200_000_000L, Backend.ONNX, "", null, "pixai-tagger-v1.0", "骁龙 8 Gen3+", 58, 96, "PixAI v1.0 tagger with official ONNX backend, 30877 tags, best-in-class style and character recognition", "ai_model_desc_pixai_v10", 1008, true, hfRepo = "noaione/pixai-tagger-v1.0-onnx", onnxFile = "model.onnx", tagFile = "tags.json", supportedImageTypes = "anime", extraFiles = listOf("model.onnx.data"), extraFileSources = listOf("model.onnx.data")))
        add(ModelEntry("animetimm-resnet34-dbv4", "AnimeTimm DBv4 (ResNet34)", ModelCategory.TAGGER, "DBv4", "~ 111MB", 111_000_000L, Backend.ONNX, "", null, "animetimm-resnet34-dbv4", "骁龙 8 Gen2+", 82, 90, "AnimeTimm Danbooru DBv4 ResNet34 tagger", "ai_model_desc_animetimm_dbv4", 384, true, hfRepo = "Makki2104/animetimm", onnxFile = "resnet34.dbv4-full/model.onnx", tagFile = "resnet34.dbv4-full/selected_tags.csv", supportedImageTypes = "anime"))
        add(ModelEntry("wd-convnext-tagger-v3", "WD ConvNeXt Tagger v3", ModelCategory.TAGGER, "v3", "~ 377MB", 395_000_000L, Backend.ONNX, "", null, "wd-convnext-tagger-v3", "骁龙 8 Gen2+", 78, 88, "Balanced tagger model, good speed and accuracy", "ai_model_desc_wd_convnext_v3", 448, true))
        add(ModelEntry("wd-swinv2-tagger-v3", "WD SwinV2 Tagger v3", ModelCategory.TAGGER, "v3", "~ 342MB", 359_000_000L, Backend.ONNX, "", null, "wd-swinv2-tagger-v3", "骁龙 8 Gen2+", 72, 84, "Swin Transformer, excels at detailed tags", "ai_model_desc_wd_swinv2_v3", 448, true))
        add(ModelEntry("wd-vit-tagger-v3", "WD ViT Tagger v3", ModelCategory.TAGGER, "v3", "~ 327MB", 343_000_000L, Backend.ONNX, "", null, "wd-vit-tagger-v3", "骁龙 8+ Gen1+", 88, 80, "ViT architecture, fastest speed", "ai_model_desc_wd_vit_v3", 448, true))
        add(ModelEntry("wd-v1-4-moat-tagger-v2", "WD v1.4 MOAT Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-moat-tagger-v2", "骁龙 8+ Gen1+", 58, 76, "MOAT architecture, proven and stable", "ai_model_desc_wd_v14_moat_v2", 448, true))
        add(ModelEntry("wd-v1-4-convnextv2-tagger-v2", "WD v1.4 ConvNeXtV2 Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-convnextv2-tagger-v2", "骁龙 8+ Gen1+", 68, 72, "ConvNeXtV2 architecture", "ai_model_desc_wd_v14_convnextv2_v2", 448, true))
        add(ModelEntry("wd-v1-4-convnext-tagger-v2", "WD v1.4 ConvNeXt Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-convnext-tagger-v2", "骁龙 8 Gen1+", 72, 68, "Classic ConvNeXt architecture", "ai_model_desc_wd_v14_convnext_v2", 448, true))
        add(ModelEntry("wd-v1-4-swinv2-tagger-v2", "WD v1.4 SwinV2 Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-swinv2-tagger-v2", "骁龙 8 Gen1+", 62, 66, "SwinV2 architecture", "ai_model_desc_wd_v14_swinv2_v2", 448, true))
        add(ModelEntry("wd-v1-4-vit-tagger-v2", "WD v1.4 ViT Tagger v2", ModelCategory.TAGGER, "v2", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-vit-tagger-v2", "骁龙 7+ Gen2+", 82, 62, "Lightweight ViT architecture", "ai_model_desc_wd_v14_vit_v2", 448, true))
        add(ModelEntry("wd-v1-4-vit-tagger", "WD v1.4 ViT Tagger", ModelCategory.TAGGER, "v1.4", "~ 300MB", 314_000_000L, Backend.ONNX, "", null, "wd-v1-4-vit-tagger", "骁龙 7+ Gen2+", 86, 58, "Earliest version, best compatibility", "ai_model_desc_wd_v14_vit", 448, true))

        // ---- Detection ----
        add(ModelEntry("yolo11n", "YOLO11n", ModelCategory.DETECTION, "11", "~ 3MB", 3_000_000L, Backend.ONNX, "", null, "yolo11n", "所有设备", 98, 62, "Nano detection model, ultra-fast inference", "ai_model_desc_yolo_det", 640, true, supportedImageTypes = "general"))
        add(ModelEntry("yolo11s", "YOLO11s", ModelCategory.DETECTION, "11", "~ 9MB", 9_000_000L, Backend.ONNX, "", null, "yolo11s", "骁龙 7+ Gen2+", 92, 75, "Small detection model, balanced speed and accuracy", "ai_model_desc_yolo_det", 640, true, supportedImageTypes = "general"))
        add(ModelEntry("yolo11m", "YOLO11m", ModelCategory.DETECTION, "11", "~ 20MB", 20_000_000L, Backend.ONNX, "", null, "yolo11m", "骁龙 8 Gen2+", 80, 84, "Medium detection model, higher accuracy", "ai_model_desc_yolo_det", 640, true, supportedImageTypes = "general"))
        add(ModelEntry("yolov8n", "YOLOv8n", ModelCategory.DETECTION, "8", "~ 6MB", 6_000_000L, Backend.ONNX, "", null, "yolov8n", "所有设备", 96, 60, "Classic v8 nano detection", "ai_model_desc_yolo_det", 640, true, supportedImageTypes = "general"))

        // ---- Segmentation ----
        // yolo11 系列从 MikeLud/ObjectDetectionYOLO11-ONNX 下载；yolov8n-seg 从 mobilint/YOLOv8n-seg 下载
        // 大小为实际 ONNX 文件大小（FP32，opset 19）
        add(ModelEntry("yolo11n-seg", "YOLO11n-seg", ModelCategory.SEGMENTATION, "11", "~ 12MB", 11_740_440L, Backend.ONNX, "", null, "yolo11n-seg", "所有设备", 97, 60, "Nano instance segmentation, person mask", "ai_model_desc_yolo_seg", 640, true, supportedImageTypes = "general", hfRepo = "MikeLud/ObjectDetectionYOLO11-ONNX", onnxFile = "yolo11n-seg.onnx"))
        add(ModelEntry("yolo11s-seg", "YOLO11s-seg", ModelCategory.SEGMENTATION, "11", "~ 39MB", 40_657_138L, Backend.ONNX, "", null, "yolo11s-seg", "骁龙 7+ Gen2+", 90, 74, "Small instance segmentation, more precise mask", "ai_model_desc_yolo_seg", 640, true, supportedImageTypes = "general", hfRepo = "MikeLud/ObjectDetectionYOLO11-ONNX", onnxFile = "yolo11s-seg.onnx"))
        add(ModelEntry("yolov8n-seg", "YOLOv8n-seg", ModelCategory.SEGMENTATION, "8", "~ 14MB", 13_834_790L, Backend.ONNX, "", null, "yolov8n-seg", "所有设备", 95, 58, "Classic v8 nano instance segmentation", "ai_model_desc_yolo_seg", 640, true, supportedImageTypes = "general", hfRepo = "mobilint/YOLOv8n-seg", onnxFile = "yolov8n-seg.onnx"))
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
                    tagFile = obj.optString("tagFile", ""),
                    supportedImageTypes = obj.optString("supportedImageTypes", "anime"),
                    extraFiles = obj.optJSONArray("extraFiles").toStringList(),
                    extraFileSources = obj.optJSONArray("extraFileSources").toStringList()
                )
            )
        }
        return if (list.isEmpty()) defaultCatalog else list
    }

    /** 把 JSON 数组安全地转成 List&lt;String&gt;；缺失或类型不符时返回空列表。 */
    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val v = optString(i, "")
                if (v.isNotEmpty()) add(v)
            }
        }
    }

    /**
     * 检查某个模型是否已安装。
     *
     * 普通模型：`ai_models/{repoName}.onnx`
     * external data 模型（存在 [ModelEntry.extraFiles]）：整个模型位于
     * `ai_models/{repoName}/` 子目录，主文件固定为 `model.onnx`，分片与主文件同目录。
     */
    fun isInstalled(context: Context, entry: ModelEntry): Boolean {
        val dir = TaggerEngine.modelDirectory(context)
        if (entry.extraFiles.isEmpty()) {
            val onnxFile = File(dir, "${entry.repoName}.onnx")
            return onnxFile.exists() && onnxFile.length() > 0L
        }
        val modelDir = File(dir, entry.repoName)
        val onnxFile = File(modelDir, "model.onnx")
        if (!onnxFile.exists() || onnxFile.length() == 0L) return false
        // 分片缺失时 ORT 会在加载阶段抛异常，这里提前判掉，避免以「已安装」状态暴露给用户
        return entry.extraFiles.all { name ->
            val f = File(modelDir, name)
            f.exists() && f.length() > 0L
        }
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

    /**
     * 根据模型 id/name 生成简洁的徽章标签（用于模型卡片右上角）。
     * 例如："WD EVA02 Large Tagger v3" → "WD v3"，"Camie Tagger v2" → "Camie"，
     *       "PixAI Tagger v0.9" → "PixAI"，"YOLO11n-seg" → "11n-seg"。
     */
    fun shortBadgeFor(entry: ModelEntry): String {
        val id = entry.id.lowercase()
        return when {
            // Tagger 系列：按家族归类
            id.contains("camie") -> "Camie"
            // v1.0 必须先判，否则会被下面的 pixai 分支截胡
            id.contains("pixai") && (id.contains("v1.0") || id.contains("v1-0") || id.contains("v10")) -> "PixAI v1.0"
            id.contains("pixai") -> "PixAI"
            id.contains("animetimm") -> "AnimeTimm"
            // WD v3 系列（EVA02/ConvNeXt/SwinV2/ViT 都统一显示 WD v3）
            id.contains("wd-eva02") ||
            (id.contains("wd") && id.endsWith("-tagger-v3")) -> "WD v3"
            // WD v2 系列（v1.4 架构的 v2 版本）
            id.contains("wd-v1-4") && id.endsWith("-v2") -> "WD v2"
            // WD v1.4 原始版本
            id.contains("wd-v1-4-vit-tagger") && !id.contains("-v2") -> "WD v1.4"
            // YOLO 检测/分割：只显示型号后缀
            id == "yolo11n" -> "11n"
            id == "yolo11s" -> "11s"
            id == "yolo11m" -> "11m"
            id == "yolov8n" -> "v8n"
            id == "yolo11n-seg" -> "11n-seg"
            id == "yolo11s-seg" -> "11s-seg"
            id == "yolov8n-seg" -> "v8n-seg"
            // 回退：使用原 version 字段
            else -> entry.version
        }
    }

    /** 获取已安装的所有模型（跨分类） */
    fun installedEntries(context: Context, catalog: List<ModelEntry>): List<ModelEntry> =
        catalog.filter { isInstalled(context, it) }
}
