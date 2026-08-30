package com.kuzulabz.waifutaggercn

import com.kuzulabz.waifutaggercn.auth.HFTokenManager
import com.kuzulabz.waifutaggercn.auth.HFTokenManagerHolder

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.os.SystemClock
import android.util.Log
import android.provider.OpenableColumns
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.kuzulabz.waifutaggercn.ml.TaggerEngine
import com.kuzulabz.waifutaggercn.ml.DetEngine
import com.kuzulabz.waifutaggercn.ml.JointInference
import com.kuzulabz.waifutaggercn.ml.SegEngine
import com.kuzulabz.waifutaggercn.ml.ModelRegistry
import com.kuzulabz.waifutaggercn.ml.DeviceCapability
import com.kuzulabz.waifutaggercn.ml.WorkflowEngine
import com.kuzulabz.waifutaggercn.ml.WorkflowEngine.StepType
import com.kuzulabz.waifutaggercn.ml.PromptWeightMapper
import androidx.compose.material.icons.filled.AccountTree
import com.kuzulabz.waifutaggercn.ui.components.MorphingBlobLoader

import com.kuzulabz.waifutaggercn.ui.theme.WaifuTaggerCNTheme
import com.kuzulabz.waifutaggercn.ui.components.DynamicPromptBox
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.produceState
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipInputStream
import kotlin.math.max
import kotlin.math.min

private const val PREFS_NAME = "settings"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"
private const val KEY_THEME_STYLE = "theme_style" // "monet" | "custom_background"
private const val KEY_MONET_PALETTE = "monet_palette" // "device" | "green" | "blue" | "pink" | "yellow" | "purple" | "rainbow" | "brown" | "black"
private const val KEY_DARK_MODE = "dark_mode" // "system" | "light" | "dark"
private const val KEY_CUSTOM_BACKGROUND_IMAGE_PATH = "custom_background_image_path"
private const val KEY_CUSTOM_BACKGROUND_OPACITY = "custom_background_opacity"
private const val KEY_CUSTOM_BACKGROUND_DIM_AMOUNT = "custom_background_dim_amount"
private const val KEY_CUSTOM_BACKGROUND_TAB_BAR_OPACITY = "custom_background_tab_bar_opacity"
private const val KEY_HERO_SUBTITLE_MODE = "hero_subtitle_mode"
private const val KEY_HERO_CUSTOM_SUBTITLE = "hero_custom_subtitle"
private const val KEY_HERO_SUBTITLE_FONT_SIZE = "hero_subtitle_font_size"
private const val KEY_HERO_POETRY_SUBTITLE = "hero_poetry_subtitle"
private const val KEY_HERO_POETRY_DATE = "hero_poetry_date"
private const val KEY_HERO_POETRY_NOTICE_SHOWN = "hero_poetry_notice_shown"
private const val KEY_LANGUAGE = "language"   // "system" | "zh" | "en" | "ru" | "ja" | "ko"
private const val KEY_INTRO_SHOWN = "intro_shown"
private const val KEY_LANGUAGE_SELECTED = "language_selected"
private const val KEY_PRIVACY_AGREED = "privacy_agreed"
private const val KEY_GENERAL_TAG_WEIGHT = "general_tag_weight"
private const val KEY_CHARACTER_TAG_WEIGHT = "character_tag_weight"
private const val KEY_PROMPT_TAG_LIMIT = "prompt_tag_limit"
private const val KEY_PROMPT_WEIGHT_ENABLED = "prompt_weight_enabled"
private const val KEY_PROMPT_WEIGHT_MODE = "prompt_weight_mode"           // "off" | "enhance" | "full"
private const val KEY_PROMPT_WEIGHT_STRENGTH = "prompt_weight_strength"    // 0.0 ~ 1.0
private const val KEY_PROMPT_WEIGHT_MIN_CONFIDENCE = "prompt_weight_min_confidence"  // 0.0 ~ 1.0
private const val KEY_PROMPT_WEIGHT_MAX = "prompt_weight_max"             // 1.0 ~ 1.5
private const val KEY_FAVORITE_TAG_RECORDS = "favorite_tag_records"
private const val KEY_HISTORY_TAG_RECORDS = "history_tag_records"
private const val KEY_ANALYTICS_DATE = "analytics_date"
private const val KEY_ANALYTICS_TODAY_COUNT = "analytics_today_count"
private const val KEY_ANALYTICS_TOTAL_COUNT = "analytics_total_count"
private const val KEY_ANALYTICS_TOTAL_TIME_MS = "analytics_total_time_ms"
private const val KEY_EXPERIENCE_ENABLED = "experience_enabled"
private const val KEY_EXPERIENCE_INTRO_SHOWN = "experience_intro_shown"
private const val KEY_TRANSLATE_NOTICE_SHOWN = "translate_notice_shown"
private const val KEY_TRANSLATE_REJECTED = "translate_rejected"
private const val KEY_HERO_POETRY_REJECTED = "hero_poetry_rejected"
private const val KEY_CONFIRM_SAVE_DELETE = "confirm_save_delete"
private const val KEY_HIGH_PERFORMANCE_MODE = "inference_performance_mode" // "power_saving" | "performance" | "auto"
private const val KEY_PRECISION_MODE = "precision_mode"        // false=普通模式, true=精准模式
private const val KEY_PRECISION_MODE_NOTICE_SHOWN = "precision_mode_notice_shown"  // 精准模式首次开启提醒是否已确认
private const val KEY_DET_NNAPI = "det_nnapi_enabled"          // YOLO 检测是否启用 NNAPI
private const val KEY_DET_CONFIDENCE = "det_confidence"        // 检测置信度阈值
private const val KEY_DET_MODEL = "det_model_name"           // 用户选择的检测模型文件名
private const val KEY_DETECTION_MODEL = "detection_model_id"  // 用户选择的目标检测模型ID
private const val KEY_SEG_MODEL = "seg_model_name"           // 用户选择的分割模型文件名
private const val DEFAULT_DET_CONFIDENCE = 0.25f
private const val DEFAULT_PRECISION_MODE = false
private const val DEFAULT_DET_NNAPI = false
private const val PERF_MODE_POWER_SAVING = "power_saving"
private const val PERF_MODE_PERFORMANCE = "performance"
private const val PERF_MODE_AUTO = "auto"
private const val DEFAULT_PERF_MODE = PERF_MODE_PERFORMANCE
private const val KEY_TOTAL_EXP = "total_exp"
private const val KEY_SELECTED_AI_MODEL_ID = "selected_ai_model_id"
private const val KEY_AI_MODEL_DOWNLOAD_SOURCE = "ai_model_download_source"
private const val AI_MODEL_SOURCE_HUGGING_FACE = "huggingface"
private const val AI_MODEL_SOURCE_HF_MIRROR = "hf_mirror"
private const val HERO_SUBTITLE_MODE_DEFAULT = "default"
private const val HERO_SUBTITLE_MODE_POETRY = "poetry"
private const val HERO_SUBTITLE_MODE_CUSTOM = "custom"
private const val HERO_SUBTITLE_MAX_LENGTH = 20
private const val DEFAULT_HERO_SUBTITLE_FONT_SIZE = 20
private const val MIN_HERO_SUBTITLE_FONT_SIZE = 14
private const val MAX_HERO_SUBTITLE_FONT_SIZE = 28

// 全局副标题字体大小 CompositionLocal，让所有副标题都可以响应字体大小设置
val LocalSubtitleFontSize = compositionLocalOf { 16 }

// 全局卡片透明度 CompositionLocal，让所有 Card 在自定义背景下可统一调节透明度
val LocalCardOpacity = compositionLocalOf { 1f }

// 获取当前是否处于自定义背景模式（由 LocalCardOpacity < 1f 间接判断）
private const val CHINESE_POETRY_API_URL = "https://v2.jinrishici.com/one.json"
private const val MAX_TAG_RECORDS = 50
private const val MIN_PROMPT_TAG_LIMIT = 5
private const val DEFAULT_PROMPT_TAG_LIMIT = 100
private const val MAX_PROMPT_TAG_LIMIT = 150
private const val DEFAULT_PROMPT_WEIGHT_ENABLED = false
private const val DEFAULT_PROMPT_WEIGHT_MODE = "enhance"
private const val DEFAULT_PROMPT_WEIGHT_STRENGTH = 0.7f
private const val DEFAULT_PROMPT_WEIGHT_MIN_CONFIDENCE = 0.5f
private const val DEFAULT_PROMPT_WEIGHT_MAX = 1.2f
private const val PROMPT_WEIGHT_MODE_OFF = "off"
private const val PROMPT_WEIGHT_MODE_ENHANCE = "enhance"
private const val PROMPT_WEIGHT_MODE_FULL = "full"
private const val MAX_EXPERIENCE_LEVEL = 10
private const val EXP_PER_LEVEL = 1000
private const val MAX_TOTAL_EXP = (MAX_EXPERIENCE_LEVEL - 1) * EXP_PER_LEVEL
private const val BASE_EXP_GAIN = 100
private const val MIN_RELIABLE_COLOR_BODY_PART_SCORE = 0.75f
private const val AI_MODEL_NAME = "WD14 v3"
private const val PROJECT_URL = "https://github.com/p8735489-prog/ChuBaichuan-TagAI"
private const val WEBSITE_URL = "https://haobai.us.ci/"
private const val QQ_GROUP_URL = "https://qm.qq.com/q/6jViPcR9le"
private const val TELEGRAM_URL = "https://t.me/Local_Cue_Word"
private const val SPONSOR_URL = "https://www.ifdian.net/a/cubaicuan"

private val LOW_CONFIDENCE_COLOR_BODY_PART_TAGS = setOf(
    "blue_skin",
    "colored_skin",
    "blue_tongue",
    "colored_tongue",
    "purple_tongue",
    "green_tongue",
    "red_tongue",
    "black_tongue",
    "blue_teeth",
    "colored_teeth",
    "purple_teeth",
    "green_teeth",
    "red_teeth",
    "black_teeth",
    "colored_mouth",
    "blue_mouth",
    "purple_mouth",
    "green_mouth",
    "red_mouth",
    "black_mouth"
)

private val LOW_CONFIDENCE_COLOR_BODY_PART_SUFFIXES = setOf(
    "skin",
    "tongue",
    "teeth",
    "mouth",
    "lips",
    "saliva"
)

private val LOW_CONFIDENCE_COLOR_BODY_PART_PREFIXES = setOf(
    "blue",
    "purple",
    "green",
    "red",
    "black",
    "colored"
)

private data class ConfirmActionRequest(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit
)

data class TagRecord(
    val id: Long,
    val text: String,
    val createdAt: Long,
    val imagePath: String? = null
)

data class ImageScore(
    val composition: Int,
    val quality: Int,
    val art: Int,
    val overall: Int
)

data class AutoPromptDraft(
    val quality: List<String>,
    val subject: List<String>,
    val appearance: List<String>,
    val scene: List<String>,
    val action: List<String>,
    val fullPrompt: String
)

data class AnalysisStats(
    val todayCount: Int,
    val totalCount: Int,
    val averageTimeMs: Long
)

data class ExperienceState(
    val totalExp: Int,
    val level: Int,
    val currentLevelExp: Int,
    val nextLevelExp: Int,
    val nextGain: Int
)

data class DownloadableAiModel(
    val id: String,
    val displayName: String,
    @StringRes val descriptionResId: Int,
    val repoName: String,
    val sizeLabel: String,
    val family: String,
    val strengthRank: Int,
    val speedRank: Int,
    val category: String = "tagger",  // tagger / detection / segmentation
    val hfRepo: String = "",          // HuggingFace 仓库全路径，空则用默认 SmilingWolf 仓库
    val onnxFile: String = "",        // HF 仓库中的 ONNX 文件名，空则用 "model.onnx"
    val tagFile: String = "",         // HF 仓库中的标签文件名，空则用 "selected_tags.csv"
    // 适用图片类型：anime(二次元) / real(现实) / general(通用) / anime_real(二次元/现实)
    val supportedImageTypes: String = "anime"
)

data class AiModelDownloadResult(
    val success: Boolean,
    val message: String,
    val modelId: String? = null
)

data class DownloadProgress(
    val modelId: String,
    val phase: String,
    val percent: Int,
    val receivedBytes: Long,
    val totalBytes: Long,
    val isVerifying: Boolean = false
)

data class BatchResultItem(
    val uri: Uri,
    val fileName: String,
    val tags: List<TaggerEngine.Tag>,
    val success: Boolean,
    val errorMessage: String? = null
)

private val BUILT_IN_DOWNLOADABLE_AI_MODELS = listOf(
    DownloadableAiModel(
        id = "wd-eva02-large-tagger-v3",
        displayName = "WD EVA02 Large Tagger v3",
        descriptionResId = R.string.ai_model_desc_wd_eva02_large_v3,
        repoName = "wd-eva02-large-tagger-v3",
        sizeLabel = "~ 1.4GB",
        family = "WD v3",
        strengthRank = 100,
        speedRank = 35
    ),
    DownloadableAiModel(
        id = "wd-convnext-tagger-v3",
        displayName = "WD ConvNeXt Tagger v3",
        descriptionResId = R.string.ai_model_desc_wd_convnext_v3,
        repoName = "wd-convnext-tagger-v3",
        sizeLabel = "~ 377MB",
        family = "WD v3",
        strengthRank = 88,
        speedRank = 78
    ),
    DownloadableAiModel(
        id = "wd-swinv2-tagger-v3",
        displayName = "WD SwinV2 Tagger v3",
        descriptionResId = R.string.ai_model_desc_wd_swinv2_v3,
        repoName = "wd-swinv2-tagger-v3",
        sizeLabel = "~ 342MB",
        family = "WD v3",
        strengthRank = 84,
        speedRank = 72
    ),
    DownloadableAiModel(
        id = "wd-vit-tagger-v3",
        displayName = "WD ViT Tagger v3",
        descriptionResId = R.string.ai_model_desc_wd_vit_v3,
        repoName = "wd-vit-tagger-v3",
        sizeLabel = "~ 327MB",
        family = "WD v3",
        strengthRank = 80,
        speedRank = 88
    ),
    DownloadableAiModel(
        id = "wd-v1-4-moat-tagger-v2",
        displayName = "WD v1.4 MOAT Tagger v2",
        descriptionResId = R.string.ai_model_desc_wd_v14_moat_v2,
        repoName = "wd-v1-4-moat-tagger-v2",
        sizeLabel = "~ 300MB+",
        family = "WD v1.4",
        strengthRank = 76,
        speedRank = 58
    ),
    DownloadableAiModel(
        id = "wd-v1-4-convnextv2-tagger-v2",
        displayName = "WD v1.4 ConvNeXtV2 Tagger v2",
        descriptionResId = R.string.ai_model_desc_wd_v14_convnextv2_v2,
        repoName = "wd-v1-4-convnextv2-tagger-v2",
        sizeLabel = "~ 300MB+",
        family = "WD v1.4",
        strengthRank = 72,
        speedRank = 68
    ),
    DownloadableAiModel(
        id = "wd-v1-4-convnext-tagger-v2",
        displayName = "WD v1.4 ConvNeXt Tagger v2",
        descriptionResId = R.string.ai_model_desc_wd_v14_convnext_v2,
        repoName = "wd-v1-4-convnext-tagger-v2",
        sizeLabel = "~ 300MB+",
        family = "WD v1.4",
        strengthRank = 68,
        speedRank = 72
    ),
    DownloadableAiModel(
        id = "wd-v1-4-swinv2-tagger-v2",
        displayName = "WD v1.4 SwinV2 Tagger v2",
        descriptionResId = R.string.ai_model_desc_wd_v14_swinv2_v2,
        repoName = "wd-v1-4-swinv2-tagger-v2",
        sizeLabel = "~ 300MB+",
        family = "WD v1.4",
        strengthRank = 66,
        speedRank = 62
    ),
    DownloadableAiModel(
        id = "wd-v1-4-vit-tagger-v2",
        displayName = "WD v1.4 ViT Tagger v2",
        descriptionResId = R.string.ai_model_desc_wd_v14_vit_v2,
        repoName = "wd-v1-4-vit-tagger-v2",
        sizeLabel = "~ 300MB",
        family = "WD v1.4",
        strengthRank = 62,
        speedRank = 82
    ),
    DownloadableAiModel(
        id = "wd-v1-4-vit-tagger",
        displayName = "WD v1.4 ViT Tagger",
        descriptionResId = R.string.ai_model_desc_wd_v14_vit,
        repoName = "wd-v1-4-vit-tagger",
        sizeLabel = "~ 300MB",
        family = "WD v1.4",
        strengthRank = 58,
        speedRank = 86
    )
)

/**
 * 将 ModelRegistry.ModelEntry 转换为 DownloadableAiModel，
 * 复用现有的 AiModelDownloadService 下载机制。
 * TAGGER 模型从 SmilingWolf 仓库下载（含 model.onnx + selected_tags.csv）。
 * DETECTION 模型从 deepghs/yolos 仓库下载（仅 model.onnx）。
 * SEGMENTATION 模型无公开下载源，仅支持内置或用户手动导入，返回 null。
 */
fun ModelRegistry.ModelEntry.toDownloadable(): DownloadableAiModel? = when (category) {
    ModelRegistry.ModelCategory.TAGGER -> DownloadableAiModel(
        id = id,
        displayName = name,
        descriptionResId = R.string.ai_model_switch_summary,
        repoName = repoName,
        sizeLabel = sizeLabel,
        family = version,
        strengthRank = accuracyRank,
        speedRank = speedRank,
        category = "tagger",
        hfRepo = hfRepo,
        onnxFile = onnxFile,
        tagFile = tagFile,
        supportedImageTypes = supportedImageTypes
    )
    // 检测模型：从 deepghs/yolos 下载单个 model.onnx（无 CSV 标签文件）
    ModelRegistry.ModelCategory.DETECTION -> DownloadableAiModel(
        id = id,
        displayName = name,
        descriptionResId = R.string.ai_model_switch_summary,
        repoName = repoName,
        sizeLabel = sizeLabel,
        family = version,
        strengthRank = accuracyRank,
        speedRank = speedRank,
        category = "detection",
        supportedImageTypes = supportedImageTypes
    )
    // 分割模型：优先使用 hfRepo 指定的仓库（如 yolo11 系列从 MikeLud/ObjectDetectionYOLO11-ONNX，
    // yolov8n-seg 从 mobilint/YOLOv8n-seg），否则回退到 MikeLud 仓库。
    // 内置 yolo11n-seg.onnx（assets 中已包含）不在此列，其余分割模型均可下载。
    ModelRegistry.ModelCategory.SEGMENTATION -> DownloadableAiModel(
        id = id,
        displayName = name,
        descriptionResId = R.string.ai_model_switch_summary,
        repoName = repoName,
        sizeLabel = sizeLabel,
        family = version,
        strengthRank = accuracyRank,
        speedRank = speedRank,
        category = "segmentation",
        hfRepo = hfRepo,
        onnxFile = onnxFile,
        supportedImageTypes = supportedImageTypes
    )
}

class MainActivity : ComponentActivity() {

    private lateinit var engine: TaggerEngine
    private lateinit var prefs: SharedPreferences
    private lateinit var detEngine: DetEngine
    private lateinit var segEngine: SegEngine
    private lateinit var jointInference: JointInference
    private var detLoadError by mutableStateOf<String?>(null)
    private var loadError by mutableStateOf<String?>(null)
    private var isLoadingModel by mutableStateOf(true)
    private var incomingSpecialLinkRecord by mutableStateOf<TagRecord?>(null)
    private var availableAiModels by mutableStateOf<List<TaggerEngine.ModelConfig>>(emptyList())
    private var selectedAiModelId by mutableStateOf(TaggerEngine.DEFAULT_MODEL_ID)

    override fun attachBaseContext(newBase: Context) {
        val option = newBase
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "system") ?: "system"
        super.attachBaseContext(createLocalizedContext(newBase, normalizeLanguageOption(option)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installCrashLogger(applicationContext)
        super.onCreate(savedInstanceState)
        // Initialize HuggingFace token manager before model downloads.
        // Fixes gated HF model downloads that previously had no Bearer header.
        HFTokenManagerHolder.instance = HFTokenManager(applicationContext)
        runCatching { enableEdgeToEdge() }
        engine = TaggerEngine(applicationContext)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        engine.highPerformanceMode = prefs.getString(KEY_HIGH_PERFORMANCE_MODE, DEFAULT_PERF_MODE) ?: DEFAULT_PERF_MODE
        // 精准模式：YOLO11n 检测引擎（按用户设置选择 NNAPI/CPU）
        detEngine = DetEngine(applicationContext)
        // 精准模式第二阶段：YOLO11n-seg 分割引擎
        segEngine = SegEngine(applicationContext)
        // 恢复用户选择的检测模型；未选择时默认使用 yolo11n.onnx
        val savedDetModel = prefs.getString(KEY_DET_MODEL, null) ?: "yolo11n.onnx"
        detEngine.setPreferredModel(savedDetModel)
        // 恢复用户选择的分割模型；未选择时默认使用 yolo11n-seg.onnx
        val savedSegModel = prefs.getString(KEY_SEG_MODEL, null) ?: "yolo11n-seg.onnx"
        segEngine.setPreferredModel(savedSegModel)
        jointInference = JointInference(engine, detEngine, segEngine, applicationContext)
        val savedLanguage = normalizeLanguageOption(prefs.getString(KEY_LANGUAGE, "system") ?: "system")
        applyAppLocale(this, savedLanguage)
        incomingSpecialLinkRecord = parseSpecialTagLink(intent)
        // scanModelConfigs 涉及文件遍历，放到 IO 线程执行（loadAiModel 内部会扫描）
        availableAiModels = emptyList()
        // 如果选中的模型已不存在（比如没有内置模型时默认选 built_in_model 会失效），
        // 自动回退到第一个可用模型；没有可用模型时才用默认占位。
        val savedModelId = prefs.getString(KEY_SELECTED_AI_MODEL_ID, TaggerEngine.DEFAULT_MODEL_ID)
            ?: TaggerEngine.DEFAULT_MODEL_ID
        selectedAiModelId = savedModelId

        loadAiModel(savedModelId)

        setContent {
            val scope = rememberCoroutineScope()
            var useDynamicColor by remember { mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_COLOR, true)) }
            var themeStyle by remember { mutableStateOf(prefs.getString(KEY_THEME_STYLE, THEME_STYLE_MONET) ?: THEME_STYLE_MONET) }
            var monetPalette by remember { mutableStateOf(prefs.getString(KEY_MONET_PALETTE, MONET_PALETTE_DEVICE) ?: MONET_PALETTE_DEVICE) }
            var darkModeOption by remember { mutableStateOf(prefs.getString(KEY_DARK_MODE, "system") ?: "system") }
            var customBackgroundImagePath by remember { mutableStateOf(prefs.getString(KEY_CUSTOM_BACKGROUND_IMAGE_PATH, "") ?: "") }
            var customBackgroundOpacity by remember { mutableStateOf(prefs.getFloat(KEY_CUSTOM_BACKGROUND_OPACITY, 0.38f).coerceIn(0f, 1f)) }
            var customBackgroundDimAmount by remember { mutableStateOf(prefs.getFloat(KEY_CUSTOM_BACKGROUND_DIM_AMOUNT, 0.12f).coerceIn(0f, 1f)) }
            var customBackgroundTabBarOpacity by remember { mutableStateOf(prefs.getFloat(KEY_CUSTOM_BACKGROUND_TAB_BAR_OPACITY, 0.58f).coerceIn(0f, 1f)) }
            var heroSubtitleMode by remember { mutableStateOf(prefs.getString(KEY_HERO_SUBTITLE_MODE, HERO_SUBTITLE_MODE_DEFAULT) ?: HERO_SUBTITLE_MODE_DEFAULT) }
            var heroCustomSubtitle by remember {
                mutableStateOf(
                    (prefs.getString(KEY_HERO_CUSTOM_SUBTITLE, "") ?: "")
                        .take(HERO_SUBTITLE_MAX_LENGTH)
                )
            }
            var heroSubtitleFontSize by remember {
                mutableStateOf(
                    prefs.getInt(KEY_HERO_SUBTITLE_FONT_SIZE, DEFAULT_HERO_SUBTITLE_FONT_SIZE)
                        .coerceIn(MIN_HERO_SUBTITLE_FONT_SIZE, MAX_HERO_SUBTITLE_FONT_SIZE)
                )
            }
            var heroPoetrySubtitle by remember { mutableStateOf(prefs.getString(KEY_HERO_POETRY_SUBTITLE, "") ?: "") }
            var heroPoetryDate by remember { mutableStateOf(prefs.getString(KEY_HERO_POETRY_DATE, "") ?: "") }
            var heroPoetryNoticeShown by remember { mutableStateOf(prefs.getBoolean(KEY_HERO_POETRY_NOTICE_SHOWN, false)) }
            var heroPoetryRejected by remember { mutableStateOf(prefs.getBoolean(KEY_HERO_POETRY_REJECTED, false)) }
            var languageOption by remember { mutableStateOf(normalizeLanguageOption(prefs.getString(KEY_LANGUAGE, "system") ?: "system")) }
            var showFirstLaunchFlowDialog by remember {
                mutableStateOf(
                    !prefs.getBoolean(KEY_INTRO_SHOWN, false) ||
                        !prefs.getBoolean(KEY_LANGUAGE_SELECTED, false) ||
                        !prefs.getBoolean(KEY_PRIVACY_AGREED, false)
                )
            }
            // Legacy dialogs remain available from Settings, but never stack on the new first-launch flow.
            var showIntroDialog by remember { mutableStateOf(false) }
            var showLanguageSelectDialog by remember { mutableStateOf(false) }
            var showPrivacyDialog by remember { mutableStateOf(false) }
            var generalTagWeight by remember { mutableStateOf(prefs.getFloat(KEY_GENERAL_TAG_WEIGHT, 1f)) }
            var characterTagWeight by remember { mutableStateOf(prefs.getFloat(KEY_CHARACTER_TAG_WEIGHT, 1f)) }
            var promptTagLimit by remember {
                mutableStateOf(prefs.getInt(KEY_PROMPT_TAG_LIMIT, DEFAULT_PROMPT_TAG_LIMIT).coerceIn(MIN_PROMPT_TAG_LIMIT, MAX_PROMPT_TAG_LIMIT))
            }
            // Prompt 权重设置
            var promptWeightEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_PROMPT_WEIGHT_ENABLED, DEFAULT_PROMPT_WEIGHT_ENABLED)) }
            var promptWeightMode by remember { mutableStateOf(prefs.getString(KEY_PROMPT_WEIGHT_MODE, DEFAULT_PROMPT_WEIGHT_MODE) ?: DEFAULT_PROMPT_WEIGHT_MODE) }
            var promptWeightStrength by remember { mutableStateOf(prefs.getFloat(KEY_PROMPT_WEIGHT_STRENGTH, DEFAULT_PROMPT_WEIGHT_STRENGTH).coerceIn(0f, 1f)) }
            var promptWeightMinConfidence by remember { mutableStateOf(prefs.getFloat(KEY_PROMPT_WEIGHT_MIN_CONFIDENCE, DEFAULT_PROMPT_WEIGHT_MIN_CONFIDENCE).coerceIn(0f, 1f)) }
            var promptWeightMax by remember { mutableStateOf(prefs.getFloat(KEY_PROMPT_WEIGHT_MAX, DEFAULT_PROMPT_WEIGHT_MAX).coerceIn(1f, 1.5f)) }
            var experienceEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_EXPERIENCE_ENABLED, false)) }
            var confirmSaveDelete by remember { mutableStateOf(prefs.getBoolean(KEY_CONFIRM_SAVE_DELETE, true)) }
            var inferencePerfMode by remember { mutableStateOf(prefs.getString(KEY_HIGH_PERFORMANCE_MODE, DEFAULT_PERF_MODE) ?: DEFAULT_PERF_MODE) }
            // 精准模式：开启后单图/批量推理会先做 YOLO11n 人物检测再打标
            var precisionMode by remember { mutableStateOf(prefs.getBoolean(KEY_PRECISION_MODE, DEFAULT_PRECISION_MODE)) }
            var detNnapiEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_DET_NNAPI, DEFAULT_DET_NNAPI)) }
            var detConfidence by remember { mutableStateOf(prefs.getFloat(KEY_DET_CONFIDENCE, DEFAULT_DET_CONFIDENCE)) }
            // 检测模型：未选择时默认使用 yolo11n.onnx，使 UI 正确显示"当前使用"
            var detModelName by remember {
                mutableStateOf(prefs.getString(KEY_DET_MODEL, null) ?: "yolo11n.onnx")
            }
            var selectedDetectionModelId by remember { mutableStateOf(prefs.getString(KEY_DETECTION_MODEL, null)) }
            var detReady by remember { mutableStateOf(detEngine.isReady) }
            // 分割模型：未选择时默认使用 yolo11n-seg.onnx
            var segModelName by remember {
                mutableStateOf(prefs.getString(KEY_SEG_MODEL, null) ?: "yolo11n-seg.onnx")
            }
            var segReady by remember { mutableStateOf(segEngine.isReady) }
            // 精准模式开启时，首次启用自动加载检测模型和分割模型（懒加载，避免无谓启动耗时）
            LaunchedEffect(precisionMode, detNnapiEnabled, detModelName) {
                if (precisionMode && !detEngine.isReady) {
                    detLoadError = withContext(Dispatchers.IO) {
                        detEngine.load(if (detNnapiEnabled) DetEngine.ExecProvider.NNAPI else DetEngine.ExecProvider.CPU)
                    }
                    detReady = detEngine.isReady
                }
            }
            LaunchedEffect(precisionMode, detNnapiEnabled, segModelName) {
                if (precisionMode && !segEngine.isReady) {
                    withContext(Dispatchers.IO) {
                        segEngine.load(if (detNnapiEnabled) SegEngine.ExecProvider.NNAPI else SegEngine.ExecProvider.CPU)
                    }
                    segReady = segEngine.isReady
                }
            }
            var showExperienceIntroDialog by remember { mutableStateOf(false) }

            val darkThemeOverride = when (darkModeOption) {
                "light" -> false
                "dark" -> true
                else -> null // follow system
            }

            val useCustomBackground = themeStyle == THEME_STYLE_CUSTOM_BACKGROUND
            val effectiveDynamicColor = useDynamicColor && themeStyle == THEME_STYLE_MONET

            WaifuTaggerCNTheme(
                    useDynamicColor = effectiveDynamicColor,
                    useCustomBackgroundStyle = useCustomBackground,
                    monetPalette = monetPalette,
                    darkTheme = darkThemeOverride
                ) {
                    val systemDarkTheme =
                        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    val effectiveDarkTheme = darkThemeOverride ?: systemDarkTheme
                    val systemBarColor = if (useCustomBackground) {
                        if (effectiveDarkTheme) Color.Black else Color.White
                    } else {
                        MaterialTheme.colorScheme.background
                    }
                    SideEffect {
                        runCatching {
                            window.statusBarColor = if (useCustomBackground) Color.Transparent.toArgb() else systemBarColor.toArgb()
                            window.navigationBarColor = systemBarColor.toArgb()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                window.isStatusBarContrastEnforced = false
                                window.isNavigationBarContrastEnforced = false
                            }
                            WindowCompat.getInsetsController(window, window.decorView).apply {
                                isAppearanceLightStatusBars = !effectiveDarkTheme
                                isAppearanceLightNavigationBars = !effectiveDarkTheme
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (useCustomBackground) Color.Transparent else MaterialTheme.colorScheme.background
                    ) {
                        TaggerScreen(
                            engine = engine,
                            loadError = loadError,
                            isLoadingModel = isLoadingModel,
                            aiModels = availableAiModels,
                            selectedAiModelId = selectedAiModelId,
                            useDynamicColor = useDynamicColor,
                            themeStyle = themeStyle,
                            monetPalette = monetPalette,
                            generalTagWeight = generalTagWeight,
                            characterTagWeight = characterTagWeight,
                            promptTagLimit = promptTagLimit,
                            customBackgroundImagePath = customBackgroundImagePath,
                            customBackgroundOpacity = customBackgroundOpacity,
                            customBackgroundDimAmount = customBackgroundDimAmount,
                            customBackgroundTabBarOpacity = customBackgroundTabBarOpacity,
                            heroSubtitleMode = heroSubtitleMode,
                            heroCustomSubtitle = heroCustomSubtitle,
                            heroSubtitleFontSize = heroSubtitleFontSize,
                            heroPoetrySubtitle = heroPoetrySubtitle,
                            heroPoetryDate = heroPoetryDate,
                            heroPoetryNoticeShown = heroPoetryNoticeShown,
                            experienceEnabled = experienceEnabled,
                            confirmSaveDelete = confirmSaveDelete,
                            inferencePerfMode = inferencePerfMode,
                            precisionMode = precisionMode,
                            detReady = detReady,
                            detLoadError = detLoadError,
                            detNnapiEnabled = detNnapiEnabled,
                            detConfidence = detConfidence,
                            detModelName = detModelName,
                            jointInference = jointInference,
                            selectedDetectionModelId = selectedDetectionModelId,
                            segModelName = segModelName,
                            segReady = segReady,
                            onSelectSegModel = { modelFileName ->
                                segModelName = modelFileName
                                prefs.edit().putString(KEY_SEG_MODEL, modelFileName).apply()
                                segEngine.setPreferredModel(modelFileName)
                                scope.launch {
                                    segReady = false
                                    withContext(Dispatchers.IO) {
                                        segEngine.close()
                                        segEngine.load(if (detNnapiEnabled) SegEngine.ExecProvider.NNAPI else SegEngine.ExecProvider.CPU)
                                    }
                                    segReady = segEngine.isReady
                                }
                            },
                            onDynamicColorChange = {
                                useDynamicColor = it
                                if (it) {
                                    // 开启动态颜色时，自动切换到"跟随系统"配色
                                    monetPalette = MONET_PALETTE_DEVICE
                                    prefs.edit()
                                        .putBoolean(KEY_DYNAMIC_COLOR, true)
                                        .putString(KEY_MONET_PALETTE, MONET_PALETTE_DEVICE)
                                        .apply()
                                } else {
                                    prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, false).apply()
                                }
                            },
                            onThemeStyleChange = {
                                themeStyle = it
                                val nextPalette = if (it == THEME_STYLE_CUSTOM_BACKGROUND) {
                                    MONET_PALETTE_BLACK
                                } else {
                                    if (monetPalette in androidPaletteOptions()) monetPalette else MONET_PALETTE_DEVICE
                                }
                                monetPalette = nextPalette
                                if (it == THEME_STYLE_CUSTOM_BACKGROUND) useDynamicColor = false
                                if (it != THEME_STYLE_CUSTOM_BACKGROUND && nextPalette == MONET_PALETTE_DEVICE) useDynamicColor = true
                                prefs.edit()
                                    .putString(KEY_THEME_STYLE, it)
                                    .putString(KEY_MONET_PALETTE, nextPalette)
                                    .putBoolean(KEY_DYNAMIC_COLOR, useDynamicColor)
                                    .apply()
                            },
                            onMonetPaletteChange = {
                                monetPalette = it
                                if (it == MONET_PALETTE_DEVICE) {
                                    useDynamicColor = true
                                    prefs.edit()
                                        .putString(KEY_MONET_PALETTE, it)
                                        .putBoolean(KEY_DYNAMIC_COLOR, true)
                                        .apply()
                                } else {
                                    prefs.edit().putString(KEY_MONET_PALETTE, it).apply()
                                }
                            },
                            onGeneralTagWeightChange = {
                                generalTagWeight = it
                                prefs.edit().putFloat(KEY_GENERAL_TAG_WEIGHT, it).apply()
                            },
                            onCharacterTagWeightChange = {
                                characterTagWeight = it
                                prefs.edit().putFloat(KEY_CHARACTER_TAG_WEIGHT, it).apply()
                            },
                            onPromptTagLimitChange = {
                                promptTagLimit = it.coerceIn(MIN_PROMPT_TAG_LIMIT, MAX_PROMPT_TAG_LIMIT)
                                prefs.edit().putInt(KEY_PROMPT_TAG_LIMIT, promptTagLimit).apply()
                            },
                            promptWeightEnabled = promptWeightEnabled,
                            promptWeightMode = promptWeightMode,
                            promptWeightStrength = promptWeightStrength,
                            promptWeightMinConfidence = promptWeightMinConfidence,
                            promptWeightMax = promptWeightMax,
                            onPromptWeightEnabledChange = {
                                promptWeightEnabled = it
                                prefs.edit().putBoolean(KEY_PROMPT_WEIGHT_ENABLED, it).apply()
                            },
                            onPromptWeightModeChange = {
                                promptWeightMode = it
                                prefs.edit().putString(KEY_PROMPT_WEIGHT_MODE, it).apply()
                            },
                            onPromptWeightStrengthChange = {
                                promptWeightStrength = it.coerceIn(0f, 1f)
                                prefs.edit().putFloat(KEY_PROMPT_WEIGHT_STRENGTH, promptWeightStrength).apply()
                            },
                            onPromptWeightMinConfidenceChange = {
                                promptWeightMinConfidence = it.coerceIn(0f, 1f)
                                prefs.edit().putFloat(KEY_PROMPT_WEIGHT_MIN_CONFIDENCE, promptWeightMinConfidence).apply()
                            },
                            onPromptWeightMaxChange = {
                                promptWeightMax = it.coerceIn(1f, 1.5f)
                                prefs.edit().putFloat(KEY_PROMPT_WEIGHT_MAX, promptWeightMax).apply()
                            },
                            onCustomBackgroundImagePathChange = {
                                customBackgroundImagePath = it
                                prefs.edit().putString(KEY_CUSTOM_BACKGROUND_IMAGE_PATH, it).apply()
                            },
                            onCustomBackgroundOpacityChange = {
                                customBackgroundOpacity = it.coerceIn(0f, 1f)
                                prefs.edit().putFloat(KEY_CUSTOM_BACKGROUND_OPACITY, customBackgroundOpacity).apply()
                            },
                            onCustomBackgroundDimAmountChange = {
                                customBackgroundDimAmount = it.coerceIn(0f, 1f)
                                prefs.edit().putFloat(KEY_CUSTOM_BACKGROUND_DIM_AMOUNT, customBackgroundDimAmount).apply()
                            },
                            onCustomBackgroundTabBarOpacityChange = {
                                customBackgroundTabBarOpacity = it.coerceIn(0f, 1f)
                                prefs.edit().putFloat(KEY_CUSTOM_BACKGROUND_TAB_BAR_OPACITY, customBackgroundTabBarOpacity).apply()
                            },
                            onHeroSubtitleModeChange = {
                                heroSubtitleMode = it
                                prefs.edit().putString(KEY_HERO_SUBTITLE_MODE, it).apply()
                            },
                            onHeroCustomSubtitleChange = {
                                val nextSubtitle = sanitizeHeroSubtitle(it)
                                heroCustomSubtitle = nextSubtitle
                                prefs.edit().putString(KEY_HERO_CUSTOM_SUBTITLE, nextSubtitle).apply()
                            },
                            onHeroSubtitleFontSizeChange = {
                                heroSubtitleFontSize = it.coerceIn(MIN_HERO_SUBTITLE_FONT_SIZE, MAX_HERO_SUBTITLE_FONT_SIZE)
                                prefs.edit().putInt(KEY_HERO_SUBTITLE_FONT_SIZE, heroSubtitleFontSize).apply()
                            },
                            onHeroPoetrySubtitleChange = { text, date ->
                                val nextSubtitle = sanitizeHeroSubtitle(text)
                                heroPoetrySubtitle = nextSubtitle
                                heroPoetryDate = date
                                prefs.edit()
                                    .putString(KEY_HERO_POETRY_SUBTITLE, nextSubtitle)
                                    .putString(KEY_HERO_POETRY_DATE, date)
                                    .apply()
                            },
                            onHeroPoetryNoticeShownChange = { shown ->
                                heroPoetryNoticeShown = shown
                                prefs.edit().putBoolean(KEY_HERO_POETRY_NOTICE_SHOWN, shown).apply()
                            },
                            onExperienceEnabledChange = {
                                experienceEnabled = it
                                prefs.edit().putBoolean(KEY_EXPERIENCE_ENABLED, it).apply()
                                if (it && !prefs.getBoolean(KEY_EXPERIENCE_INTRO_SHOWN, false)) {
                                    showExperienceIntroDialog = true
                                    prefs.edit().putBoolean(KEY_EXPERIENCE_INTRO_SHOWN, true).apply()
                                }
                            },
                            onConfirmSaveDeleteChange = {
                                confirmSaveDelete = it
                                prefs.edit().putBoolean(KEY_CONFIRM_SAVE_DELETE, it).apply()
                            },
                            onInferencePerfModeChange = {
                                inferencePerfMode = it
                                prefs.edit().putString(KEY_HIGH_PERFORMANCE_MODE, it).apply()
                                engine.highPerformanceMode = it
                            },
                            onPrecisionModeChange = {
                                precisionMode = it
                                prefs.edit().putBoolean(KEY_PRECISION_MODE, it).apply()
                            },
                            onDetNnapiChange = {
                                detNnapiEnabled = it
                                prefs.edit().putBoolean(KEY_DET_NNAPI, it).apply()
                                // 切换 EP 后重新加载检测模型和分割模型
                                scope.launch {
                                    detReady = false
                                    detLoadError = withContext(Dispatchers.IO) {
                                        detEngine.close()
                                        detEngine.load(if (it) DetEngine.ExecProvider.NNAPI else DetEngine.ExecProvider.CPU)
                                    }
                                    detReady = detEngine.isReady
                                    // 同时重新加载分割模型
                                    if (precisionMode) {
                                        segReady = false
                                        withContext(Dispatchers.IO) {
                                            segEngine.close()
                                            segEngine.load(if (it) SegEngine.ExecProvider.NNAPI else SegEngine.ExecProvider.CPU)
                                        }
                                        segReady = segEngine.isReady
                                    }
                                }
                            },
                            onDetConfidenceChange = {
                                detConfidence = it
                                prefs.edit().putFloat(KEY_DET_CONFIDENCE, it).apply()
                            },
                            onSelectDetModel = { modelFileName ->
                                detModelName = modelFileName
                                prefs.edit().putString(KEY_DET_MODEL, modelFileName).apply()
                                detEngine.setPreferredModel(modelFileName)
                                // 重新加载检测模型
                                scope.launch {
                                    detReady = false
                                    detLoadError = withContext(Dispatchers.IO) {
                                        detEngine.close()
                                        detEngine.load(if (detNnapiEnabled) DetEngine.ExecProvider.NNAPI else DetEngine.ExecProvider.CPU)
                                    }
                                    detReady = detEngine.isReady
                                }
                            },
                            onSelectDetectionModel = { modelId ->
                                selectedDetectionModelId = modelId
                                prefs.edit().putString(KEY_DETECTION_MODEL, modelId).apply()
                            },
                            onReloadAiModels = {
                                scope.launch {
                                    val scanned = withContext(Dispatchers.IO) {
                                        runCatching { TaggerEngine.scanModelConfigs(applicationContext) }.getOrDefault(emptyList())
                                    }
                                    availableAiModels = scanned
                                    android.util.Log.d("MainActivity", "onReloadAiModels: ${scanned.size} models")
                                }
                            },
                            onSelectAiModel = { modelId ->
                                android.util.Log.d("MainActivity", "onSelectAiModel: $modelId")
                                selectedAiModelId = modelId
                                loadAiModel(modelId)
                            },
                            darkModeOption = darkModeOption,
                            onDarkModeChange = {
                                darkModeOption = it
                                prefs.edit().putString(KEY_DARK_MODE, it).apply()
                            },
                            languageOption = languageOption,
                            incomingSpecialLinkRecord = incomingSpecialLinkRecord,
                            onLanguageChange = { option ->
                                val normalizedOption = normalizeLanguageOption(option)
                                if (normalizedOption != languageOption) {
                                    prefs.edit().putString(KEY_LANGUAGE, normalizedOption).apply()
                                    applyAppLocale(this@MainActivity, normalizedOption)
                                    languageOption = normalizedOption
                                    recreate()
                                }
                            },
                            showFirstLaunchFlowDialog = showFirstLaunchFlowDialog,
                            onFirstLaunchFlowDismiss = {
                                showFirstLaunchFlowDialog = false
                                prefs.edit()
                                    .putBoolean(KEY_INTRO_SHOWN, true)
                                    .putBoolean(KEY_LANGUAGE_SELECTED, true)
                                    .putBoolean(KEY_PRIVACY_AGREED, true)
                                    .apply()
                                recreate()
                            },
                            onFirstLaunchLanguageChange = { option ->
                                val normalized = normalizeLanguageOption(option)
                                prefs.edit().putString(KEY_LANGUAGE, normalized).putBoolean(KEY_LANGUAGE_SELECTED, true).apply()
                                applyAppLocale(this@MainActivity, normalized)
                                languageOption = normalized
                            },
                            showIntroDialog = showIntroDialog,
                            onIntroDismiss = {
                                showIntroDialog = false
                                prefs.edit().putBoolean(KEY_INTRO_SHOWN, true).apply()
                            },
                            showExperienceIntroDialog = showExperienceIntroDialog,
                            onExperienceIntroDismiss = { showExperienceIntroDialog = false },
                            onShowIntroDialog = {
                                showIntroDialog = true
                            },
                            heroPoetryRejected = heroPoetryRejected,
                            onHeroPoetryRejectedChange = { rejected ->
                                heroPoetryRejected = rejected
                                prefs.edit().putBoolean(KEY_HERO_POETRY_REJECTED, rejected).apply()
                            },
                            showLanguageSelectDialog = showLanguageSelectDialog,
                            onShowLanguageSelectDialogChange = { show ->
                                showLanguageSelectDialog = show
                                if (!show) {
                                    prefs.edit().putBoolean(KEY_LANGUAGE_SELECTED, true).apply()
                                }
                            },
                            showPrivacyDialog = showPrivacyDialog,
                            onShowPrivacyDialog = {
                                showPrivacyDialog = true
                            },
                            onPrivacyAgree = {
                                showPrivacyDialog = false
                                prefs.edit().putBoolean(KEY_PRIVACY_AGREED, true).apply()
                                // 隐私声明同意后，显示欢迎弹窗（如果未看过）
                                if (!prefs.getBoolean(KEY_INTRO_SHOWN, false)) {
                                    showIntroDialog = true
                                }
                            },
                            onPrivacyDisagree = {
                                showPrivacyDialog = false
                                // 退出 app
                                finishAffinity()
                            }
                        )
                    }
                }
        }
    }

    private fun loadAiModel(modelId: String) {
        isLoadingModel = true
        loadError = null
        lifecycleScope.launch {
            // scanModelConfigs 涉及文件系统遍历，放到 IO 线程避免主线程卡顿/ANR
            val scanned = withContext(Dispatchers.IO) {
                runCatching { TaggerEngine.scanModelConfigs(applicationContext) }.getOrDefault(emptyList())
            }
            availableAiModels = scanned
            // 优先精确匹配用户选择的模型；向后兼容旧版本存储的绝对路径（按文件名匹配）
            val matchedConfig = scanned.firstOrNull { it.id == modelId }
                ?: scanned.firstOrNull {
                    File(modelId).nameWithoutExtension == it.id
                }
            // 若用户选择的模型未找到（已被删除或尚未扫描到），回退到首个可用或内置模型，
            // 保证引擎仍有模型可用；但回退时不覆盖用户已保存的选择。
            val modelConfig = matchedConfig
                ?: scanned.firstOrNull()
                ?: TaggerEngine.builtInModelConfig()
            val error = withContext(Dispatchers.IO) { engine.load(modelConfig) }
            if (error == null) {
                // UI 始终显示当前实际加载的模型；但仅当真正匹配到用户选择的模型时才持久化，
                // 避免回退模型（id 不同）覆盖用户原本的选择，导致“当前使用”指向错误模型。
                selectedAiModelId = modelConfig.id
                if (matchedConfig != null) {
                    prefs.edit().putString(KEY_SELECTED_AI_MODEL_ID, modelConfig.id).apply()
                }
            }
            loadError = error
            isLoadingModel = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingSpecialLinkRecord = parseSpecialTagLink(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        engine.close()
    }
}

private fun installCrashLogger(context: Context) {
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        runCatching {
            val dir = File(context.noBackupFilesDir, ".lcw/.crash")
            if (!dir.exists()) dir.mkdirs()
            val stackTrace = StringWriter().also { writer ->
                throwable.printStackTrace(PrintWriter(writer))
            }.toString()
            File(dir, "latest_crash.txt").writeText(
                buildString {
                    appendLine("thread=${thread.name}")
                    appendLine("time=${System.currentTimeMillis()}")
                    appendLine(stackTrace)
                }
            )
        }
        previousHandler?.uncaughtException(thread, throwable)
    }
}

private const val THEME_STYLE_MONET = "monet"
private const val THEME_STYLE_CUSTOM_BACKGROUND = "custom_background"
private const val MONET_PALETTE_DEVICE = "device"
private const val MONET_PALETTE_WHITE = "white"
private const val MONET_PALETTE_GREEN = "green"
private const val MONET_PALETTE_BLUE = "blue"
private const val MONET_PALETTE_PINK = "pink"
private const val MONET_PALETTE_YELLOW = "yellow"
private const val MONET_PALETTE_PURPLE = "purple"
private const val MONET_PALETTE_ORANGE = "orange"
private const val MONET_PALETTE_RAINBOW = "rainbow"
private const val MONET_PALETTE_BROWN = "brown"
private const val MONET_PALETTE_BLACK = "black"
private const val MONET_PALETTE_DEEP_BLUE = "deep_blue"
private const val MONET_PALETTE_LAVA_ORANGE = "lava_orange"
private const val MONET_PALETTE_SWEET_PINK = "sweet_pink"

private fun androidPaletteOptions() = setOf(
    MONET_PALETTE_DEVICE,
    MONET_PALETTE_GREEN,
    MONET_PALETTE_BLUE,
    MONET_PALETTE_PINK,
    MONET_PALETTE_YELLOW,
    MONET_PALETTE_PURPLE,
    MONET_PALETTE_RAINBOW,
    MONET_PALETTE_BROWN,
    MONET_PALETTE_BLACK
)

private fun minimalPaletteOptions() = setOf(
    MONET_PALETTE_DEVICE,
    MONET_PALETTE_DEEP_BLUE,
    MONET_PALETTE_LAVA_ORANGE,
    MONET_PALETTE_SWEET_PINK
)

private fun customBackgroundMainColorOptions() = setOf(
    MONET_PALETTE_WHITE,
    MONET_PALETTE_BLACK,
    MONET_PALETTE_GREEN,
    MONET_PALETTE_PURPLE,
    MONET_PALETTE_YELLOW,
    MONET_PALETTE_PINK,
    MONET_PALETTE_ORANGE,
    MONET_PALETTE_BROWN,
    MONET_PALETTE_BLUE
)

private fun normalizeLanguageOption(option: String): String {
    return when (option) {
        "system", "zh", "zh-rTW", "en", "ja", "ko", "ru" -> option
        else -> "system"
    }
}

private fun createLocalizedContext(context: Context, option: String): Context {
    return runCatching {
        val locale = when (normalizeLanguageOption(option)) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "zh-rTW" -> Locale.TRADITIONAL_CHINESE
            "en" -> Locale.ENGLISH
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "ru" -> Locale("ru")
            "system" -> resolveSystemLocale()
            else -> return context
        }
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        }
        context.createConfigurationContext(configuration)
    }.getOrDefault(context)
}

private fun languageTagForOption(option: String): String {
    return when (normalizeLanguageOption(option)) {
        "zh" -> "zh-Hans-CN"
        "zh-rTW" -> "zh-Hant-TW"
        "en" -> "en"
        "ja" -> "ja"
        "ko" -> "ko"
        "ru" -> "ru"
        else -> ""
    }
}

private fun applyAppLocale(context: Context, option: String) {
    val normalizedOption = normalizeLanguageOption(option)
    val locale = when (normalizedOption) {
        "zh" -> Locale.SIMPLIFIED_CHINESE
        "zh-rTW" -> Locale.TRADITIONAL_CHINESE
        "en" -> Locale.ENGLISH
        "ru" -> Locale("ru")
        "ja" -> Locale.JAPANESE
        "ko" -> Locale.KOREAN
        else -> resolveSystemLocale()
    }
    Locale.setDefault(locale)
    val config = Configuration()
    config.setLocale(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocales(LocaleList(locale))
    }
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    // 同时更新 Application 级别的配置
    @Suppress("DEPRECATION")
    context.applicationContext.resources.updateConfiguration(config, context.resources.displayMetrics)
}

private fun resolveSystemLocale(): Locale {
    val systemConfiguration = Resources.getSystem().configuration
    val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        systemConfiguration.locales.get(0)
    } else {
        @Suppress("DEPRECATION")
        systemConfiguration.locale
    }
    return when (systemLocale.language.lowercase(Locale.ROOT)) {
        "zh" -> {
            // 检查是否为繁体中文区域
            val country = systemLocale.country.uppercase(Locale.ROOT)
            if (country == "TW" || country == "HK" || country == "MO") {
                Locale.TRADITIONAL_CHINESE
            } else {
                Locale.SIMPLIFIED_CHINESE
            }
        }
        "en", "ru", "ja", "ko" -> systemLocale
        else -> Locale.ENGLISH
    }
}

@Composable
private fun localizedLoadErrorText(loadError: String?): String {
    if (loadError.isNullOrBlank()) return ""
    return when {
        loadError.contains("还没有下载 AI 模型") ||
            loadError.contains("No AI model downloaded") ||
            loadError.contains("Модель AI ещё не загружена") -> {
            stringResource(R.string.model_onnx_missing)
        }

        loadError.startsWith("模型加载失败：") ||
            loadError.startsWith("Failed to load model:") ||
            loadError.startsWith("Не удалось загрузить модель:") -> {
            val detail = loadError.substringAfter("：", loadError)
                .substringAfter(":", "")
                .trim()
            stringResource(R.string.model_load_failed, detail)
        }

        loadError.contains("找不到标签表 selected_tags.csv") ||
            loadError.contains("selected_tags.csv not found") ||
            loadError.contains("selected_tags.csv не найден") -> {
            stringResource(R.string.selected_tags_missing)
        }

        else -> loadError
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaggerScreen(
    engine: TaggerEngine,
    loadError: String?,
    isLoadingModel: Boolean,
    aiModels: List<TaggerEngine.ModelConfig>,
    selectedAiModelId: String,
    useDynamicColor: Boolean,
    themeStyle: String,
    monetPalette: String,
    generalTagWeight: Float,
    characterTagWeight: Float,
    promptTagLimit: Int,
    customBackgroundImagePath: String,
    customBackgroundOpacity: Float,
    customBackgroundDimAmount: Float,
    customBackgroundTabBarOpacity: Float,
    heroSubtitleMode: String,
    heroCustomSubtitle: String,
    heroSubtitleFontSize: Int,
    heroPoetrySubtitle: String,
    heroPoetryDate: String,
    heroPoetryNoticeShown: Boolean,
    experienceEnabled: Boolean,
    confirmSaveDelete: Boolean,
    inferencePerfMode: String,
    precisionMode: Boolean,
    detReady: Boolean,
    detLoadError: String?,
    detNnapiEnabled: Boolean,
    detConfidence: Float,
    detModelName: String?,
    jointInference: JointInference,
    onDynamicColorChange: (Boolean) -> Unit,
    onThemeStyleChange: (String) -> Unit,
    onMonetPaletteChange: (String) -> Unit,
    onGeneralTagWeightChange: (Float) -> Unit,
    onCharacterTagWeightChange: (Float) -> Unit,
    onPromptTagLimitChange: (Int) -> Unit,
    promptWeightEnabled: Boolean,
    promptWeightMode: String,
    promptWeightStrength: Float,
    promptWeightMinConfidence: Float,
    promptWeightMax: Float,
    onPromptWeightEnabledChange: (Boolean) -> Unit,
    onPromptWeightModeChange: (String) -> Unit,
    onPromptWeightStrengthChange: (Float) -> Unit,
    onPromptWeightMinConfidenceChange: (Float) -> Unit,
    onPromptWeightMaxChange: (Float) -> Unit,
    onCustomBackgroundImagePathChange: (String) -> Unit,
    onCustomBackgroundOpacityChange: (Float) -> Unit,
    onCustomBackgroundDimAmountChange: (Float) -> Unit,
    onCustomBackgroundTabBarOpacityChange: (Float) -> Unit,
    onHeroSubtitleModeChange: (String) -> Unit,
    onHeroCustomSubtitleChange: (String) -> Unit,
    onHeroSubtitleFontSizeChange: (Int) -> Unit,
    onHeroPoetrySubtitleChange: (String, String) -> Unit,
    onHeroPoetryNoticeShownChange: (Boolean) -> Unit,
    onExperienceEnabledChange: (Boolean) -> Unit,
    onConfirmSaveDeleteChange: (Boolean) -> Unit,
    onInferencePerfModeChange: (String) -> Unit,
    onPrecisionModeChange: (Boolean) -> Unit,
    onDetNnapiChange: (Boolean) -> Unit,
    onDetConfidenceChange: (Float) -> Unit,
    onSelectDetModel: (String) -> Unit,
    onSelectDetectionModel: (String) -> Unit,
    selectedDetectionModelId: String?,
    segModelName: String,
    segReady: Boolean,
    onSelectSegModel: (String) -> Unit,
    onReloadAiModels: () -> Unit,
    onSelectAiModel: (String) -> Unit,
    darkModeOption: String,
    onDarkModeChange: (String) -> Unit,
    languageOption: String,
    showFirstLaunchFlowDialog: Boolean,
    onFirstLaunchFlowDismiss: () -> Unit,
    onFirstLaunchLanguageChange: (String) -> Unit,
    incomingSpecialLinkRecord: TagRecord?,
    onLanguageChange: (String) -> Unit,
    showIntroDialog: Boolean,
    onIntroDismiss: () -> Unit,
    onShowIntroDialog: () -> Unit,
    showExperienceIntroDialog: Boolean,
    onExperienceIntroDismiss: () -> Unit,
    heroPoetryRejected: Boolean,
    onHeroPoetryRejectedChange: (Boolean) -> Unit,
    showLanguageSelectDialog: Boolean,
    onShowLanguageSelectDialogChange: (Boolean) -> Unit,
    showPrivacyDialog: Boolean,
    onShowPrivacyDialog: () -> Unit,
    onPrivacyAgree: () -> Unit,
    onPrivacyDisagree: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tags by remember { mutableStateOf<List<TaggerEngine.Tag>>(emptyList()) }
    var imageScore by remember { mutableStateOf<ImageScore?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var inferenceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var inferenceProgressTargetPercent by remember { mutableStateOf(0f) }
    var inferenceProgressText by remember { mutableStateOf("") }
    // 真实进度值（来自 onProgress 回调，0..1），用于驱动 UI
    var realProgress by remember { mutableStateOf(0f) }
    // 记录上次真实进度更新时间，用于判断是否处于"长操作等待"状态
    var lastRealProgressUpdateMs by remember { mutableStateOf(0L) }
    val animatedInferenceProgressPercent by animateFloatAsState(
        targetValue = inferenceProgressTargetPercent.coerceIn(0f, 100f),
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 350f
        ),
        label = "inferenceProgressPercent"
    )
    var lastInferenceTimeMs by remember { mutableStateOf<Long?>(null) }
    var threshold by remember { mutableStateOf(0.35f) }
    var showFileManager by remember { mutableStateOf(false) }
    // 批量选图与批量识别
    var selectedBatchUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingBatchUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showBatchConfirmDialog by remember { mutableStateOf(false) }
    var batchProgressIndex by remember { mutableStateOf(0) }
    var batchResults by remember { mutableStateOf<List<BatchResultItem>>(emptyList()) }
    var isBatchRunning by remember { mutableStateOf(false) }
    var showBatchDialog by remember { mutableStateOf(false) }
    var showCommunityDialog by remember { mutableStateOf(false) }
    var showSponsorDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    // 多模型对比
    var showCompareDialog by remember { mutableStateOf(false) }
    var showCompareNeedModelsDialog by remember { mutableStateOf(false) }
    var compareModel1Id by remember { mutableStateOf<String?>(null) }
    var compareModel2Id by remember { mutableStateOf<String?>(null) }
    var compareResult1 by remember { mutableStateOf<List<TaggerEngine.Tag>>(emptyList()) }
    var compareResult2 by remember { mutableStateOf<List<TaggerEngine.Tag>>(emptyList()) }
    var isComparing by remember { mutableStateOf(false) }
    var compareOptimized by remember { mutableStateOf<List<TaggerEngine.Tag>>(emptyList()) }
    // 标签翻译
    var showTranslateDialog by remember { mutableStateOf(false) }
    var showTranslateNetworkNotice by remember { mutableStateOf(false) }
    var showTranslateRejectedDialog by remember { mutableStateOf(false) }
    var showPoetryApiNotice by remember { mutableStateOf(false) }
    var showPoetryRejectedDialog by remember { mutableStateOf(false) }
    var translateNoticeShown by remember {
        mutableStateOf(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TRANSLATE_NOTICE_SHOWN, false))
    }
    var translateRejected by remember {
        mutableStateOf(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TRANSLATE_REJECTED, false))
    }
    var isTranslating by remember { mutableStateOf(false) }
    var translateTargetLang by remember { mutableStateOf("zh") }
    var translatedTags by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val mainScrollState = rememberScrollState()
    val safePromptTagLimit = promptTagLimit.coerceIn(MIN_PROMPT_TAG_LIMIT, MAX_PROMPT_TAG_LIMIT)
    var favoriteRecords by remember { mutableStateOf(runCatching { loadTagRecords(context, KEY_FAVORITE_TAG_RECORDS) }.getOrDefault(emptyList())) }
    var historyRecords by remember { mutableStateOf(runCatching { loadTagRecords(context, KEY_HISTORY_TAG_RECORDS) }.getOrDefault(emptyList())) }
    var confirmActionRequest by remember { mutableStateOf<ConfirmActionRequest?>(null) }
    val saveConfirmTitle = stringResource(R.string.confirm_save_title)
    val saveConfirmMessage = stringResource(R.string.confirm_save_message)
    val deleteConfirmTitle = stringResource(R.string.confirm_delete_title)
    val deleteConfirmMessage = stringResource(R.string.confirm_delete_message)
    val confirmOrRun: (Boolean, () -> Unit) -> Unit = { isDelete, action ->
        if (confirmSaveDelete) {
            confirmActionRequest = ConfirmActionRequest(
                title = if (isDelete) deleteConfirmTitle else saveConfirmTitle,
                message = if (isDelete) deleteConfirmMessage else saveConfirmMessage,
                onConfirm = action
            )
        } else {
            action()
        }
    }
    val openTranslateWithNotice = {
        if (translateRejected) {
            showTranslateRejectedDialog = true
        } else if (translateNoticeShown) {
            showTranslateDialog = true
        } else {
            showTranslateNetworkNotice = true
        }
    }

    LaunchedEffect(translatedTags.size) {
        if (translatedTags.isNotEmpty()) {
            delay(280L)
            try {
                mainScrollState.animateScrollTo(
                    value = mainScrollState.maxValue,
                    animationSpec = tween(durationMillis = 720)
                )
            } catch (_: Exception) {
                // scroll state 可能短暂无效
            }
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        inferenceProgressTargetPercent = 0f
        realProgress = 0f
        lastRealProgressUpdateMs = System.currentTimeMillis()
        // 基于真实进度的平滑动画：缓慢逼近真实进度，但不超过真实进度 + 缓冲
        while (isRunning) {
            delay(60L)
            // 真实进度映射到百分比（0..1 → 0..99%，留1%给完成跳转）
            val realPercent = (realProgress * 99f).coerceIn(0f, 99f)
            val now = System.currentTimeMillis()
            val timeSinceLastRealUpdate = now - lastRealProgressUpdateMs
            // 平滑追赶：距离真实进度越远，追赶越快；越近则越慢（避免跳变）
            val gap = realPercent - inferenceProgressTargetPercent
            inferenceProgressTargetPercent = when {
                // 尚未收到任何真实进度（realProgress=0），缓慢爬升到 15%
                realProgress <= 0f -> (inferenceProgressTargetPercent + 1.2f).coerceAtMost(15f)
                // 真实进度低于当前显示值（阶段切换时进度回退），不回退，缓慢爬升
                gap < 0f -> (inferenceProgressTargetPercent + 0.3f).coerceAtMost(
                    realPercent + 10f + timeSinceLastRealUpdate * 0.003f
                )
                // 真实进度已到达，用缓动函数追赶
                gap > 30f -> inferenceProgressTargetPercent + 4f
                gap > 15f -> inferenceProgressTargetPercent + 2.5f
                gap > 5f -> inferenceProgressTargetPercent + 1.5f
                gap > 1f -> inferenceProgressTargetPercent + 0.6f
                gap > 0f -> inferenceProgressTargetPercent + 0.3f
                // 已追上真实进度，如果长时间无更新则缓慢爬升
                else -> {
                    val creepBonus = if (timeSinceLastRealUpdate > 1500L) {
                        minOf(timeSinceLastRealUpdate * 0.0015f, 10f)
                    } else 0f
                    (inferenceProgressTargetPercent + 0.05f + creepBonus).coerceAtMost(
                        realPercent + 10f + creepBonus
                    )
                }
            }.coerceIn(0f, 99.5f)
        }
    }

    // 监听通知栏停止按钮广播
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == InferenceForegroundService.ACTION_STOP_BROADCAST) {
                    inferenceJob?.cancel()
                }
            }
        }
        val filter = android.content.IntentFilter(InferenceForegroundService.ACTION_STOP_BROADCAST)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    // 推理中按返回键停止推理
    BackHandler(enabled = isRunning) {
        inferenceJob?.cancel()
    }

    // 批量识别：逐张处理选中的图片
    LaunchedEffect(isBatchRunning, batchProgressIndex) {
        if (!isBatchRunning || selectedBatchUris.isEmpty()) return@LaunchedEffect
        if (batchProgressIndex >= selectedBatchUris.size) {
            isBatchRunning = false
            return@LaunchedEffect
        }
        val uri = selectedBatchUris[batchProgressIndex]
        val bmp = withContext(Dispatchers.IO) { loadBitmap(context, uri) }
        if (bmp == null) {
            batchResults = batchResults + BatchResultItem(
                uri = uri,
                fileName = uriFileName(context, uri),
                tags = emptyList(),
                success = false,
                errorMessage = context.getString(R.string.batch_load_failed)
            )
            batchProgressIndex = batchProgressIndex + 1
            return@LaunchedEffect
        }
        val result = withContext(Dispatchers.IO) {
            runCatching {
                jointInference.run(
                    bitmap = bmp,
                    precisionMode = precisionMode && detReady,
                    threshold = threshold,
                    generalWeight = generalTagWeight,
                    characterWeight = characterTagWeight
                ).tags
            }
        }
        val tags = result.getOrElse {
            emptyList()
        }.filterPromptNoiseTags()
        val err = result.exceptionOrNull()
        batchResults = batchResults + BatchResultItem(
            uri = uri,
            fileName = uriFileName(context, uri),
            tags = tags.take(safePromptTagLimit),
            success = err == null,
            errorMessage = err?.message
        )
        // 存历史（IO 操作在 IO 线程执行）
        if (err == null && tags.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val savedImagePath = saveHistoryImage(context, bmp)
                historyRecords = saveTagRecord(context, KEY_HISTORY_TAG_RECORDS, tags.take(safePromptTagLimit).toTagText(), savedImagePath)
            }
        }
        batchProgressIndex = batchProgressIndex + 1
    }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showParseLinkDialog by remember { mutableStateOf(false) }
    var showAiModelDialog by remember { mutableStateOf(false) }
    var showImportGuideDialog by remember { mutableStateOf(false) }
    // 精准模式首次开启提醒弹窗
    var showPrecisionModeNotice by remember { mutableStateOf(false) }
    val precisionModeNoticeShown = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PRECISION_MODE_NOTICE_SHOWN, false)
    }
    var pendingCustomBackgroundBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // ---- 工作流状态（从 tab 2 提升到顶层，供 tab 0 使用）----
    val wfEngine = remember { WorkflowEngine(jointInference, context.applicationContext) }
    var workflowExpanded by remember { mutableStateOf(false) }
    var selectedWorkflowId by remember { mutableStateOf(wfEngine.builtInWorkflows.first().id) }

    var downloadingAiModelId by remember { mutableStateOf<String?>(null) }
    var isDownloadCancelled by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf<DownloadProgress?>(null) }
    // 下载完成/失败后自增，用于触发 installedIds / installed*Entries 重新计算，
    // 否则下载完成后模型卡片仍显示"下载"按钮，导致用户重复下载。
    var modelCatalogRefreshKey by remember { mutableStateOf(0) }
    var aiDownloadSource by remember {
        mutableStateOf(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_AI_MODEL_DOWNLOAD_SOURCE, AI_MODEL_SOURCE_HUGGING_FACE)
                ?: AI_MODEL_SOURCE_HUGGING_FACE
        )
    }
    var selectedMainTab by remember { mutableStateOf(0) }
    // 切换主标签页时自动回到顶部。用 selectedMainTab 作为 LaunchedEffect 的 key，
    // 切换时会自动取消上一次尚未完成的滚动动画，避免快速连点底部导航栏时
    // 多个 animateScrollTo 叠加导致的卡顿与画面抖动。
    //
    // 说明：所有标签页（含模型页 tab 2）共用同一个外层 mainScrollState，模型页
    // 不再使用独立的内部滚动。这样切出模型页时不会再出现“外层 verticalScroll 与
    // 退出动画中的模型页形成同方向嵌套滚动”的测量异常（即切换闪退）。
    LaunchedEffect(selectedMainTab) {
        if (mainScrollState.value > 0) {
            try {
                mainScrollState.animateScrollTo(0)
            } catch (_: Exception) {
                // 页面切换动画期间 scroll state 可能短暂无效，忽略
            }
        }
    }
    var analysisStats by remember { mutableStateOf(loadAnalysisStats(context)) }
    var experienceState by remember { mutableStateOf(loadExperienceState(context)) }
    val useCustomBackgroundStyle = themeStyle == THEME_STYLE_CUSTOM_BACKGROUND
    val systemDarkTheme = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val effectiveDarkTheme = when (darkModeOption) {
        "light" -> false
        "dark" -> true
        else -> systemDarkTheme
    }
    // 用 remember 缓存派生计算，避免切换 tab 时 AnimatedContent 重组触发重复计算
    val limitedTags = remember(tags, safePromptTagLimit) {
        tags.filterPromptNoiseTags().take(safePromptTagLimit)
    }
    val currentTagText = remember(limitedTags) { limitedTags.toTagText() }
    val currentLimitedTagNames = remember(limitedTags) { limitedTags.map { it.name }.toSet() }
    val isCurrentFavorite = remember(currentTagText, favoriteRecords) {
        currentTagText.isNotEmpty() && favoriteRecords.any { it.text == currentTagText }
    }
    val detectionResult = remember(limitedTags) { detectResult(limitedTags) }
    val recommendedModels = remember(detectionResult) { recommendModels(detectionResult) }
    val autoPromptDraft = remember(limitedTags, safePromptTagLimit, promptWeightEnabled, promptWeightMode, promptWeightStrength, promptWeightMinConfidence, promptWeightMax) {
        val draft = generateAutoPromptDraft(limitedTags, safePromptTagLimit)
        if (promptWeightEnabled && promptWeightMode != PROMPT_WEIGHT_MODE_OFF) {
            val mode = when (promptWeightMode) {
                PROMPT_WEIGHT_MODE_ENHANCE -> PromptWeightMapper.PromptWeightMode.ENHANCE_ONLY
                PROMPT_WEIGHT_MODE_FULL -> PromptWeightMapper.PromptWeightMode.FULL
                else -> PromptWeightMapper.PromptWeightMode.OFF
            }
            val config = PromptWeightMapper.PromptWeightConfig.sanitized(
                enabled = promptWeightEnabled,
                mode = mode,
                strength = promptWeightStrength,
                minConfidence = promptWeightMinConfidence,
                maxWeight = promptWeightMax
            )
            val mapper = PromptWeightMapper()
            // 构建 pretty name -> score 映射
            val scoreMap = limitedTags.associate { it.name.trim() to it.score }
            val weightedPrompt = draft.fullPrompt.split(", ")
                .filter { it.isNotBlank() }
                .joinToString(", ") { prettyName ->
                    val rawName = prettyName.replace(" ", "_").lowercase()
                    val score = scoreMap[rawName] ?: 1f
                    val weight = runCatching {
                        // 使用反射调用 calculateWeight 不太优雅，直接内联逻辑
                        val effectiveThreshold = if (mode == PromptWeightMapper.PromptWeightMode.ENHANCE_ONLY) {
                            maxOf(promptWeightMinConfidence, 0.7f)
                        } else {
                            promptWeightMinConfidence
                        }
                        if (score < effectiveThreshold) 1.0f
                        else {
                            val range = (1.0f - effectiveThreshold).coerceAtLeast(0.001f)
                            val normalized = ((score - effectiveThreshold) / range).coerceIn(0f, 1f)
                            (1.0f + normalized * (promptWeightMax - 1.0f) * promptWeightStrength).coerceIn(1.0f, promptWeightMax)
                        }
                    }.getOrDefault(1.0f)
                    if (weight > 1.001f) "($prettyName:${String.format("%.2f", weight)})" else prettyName
                }
            draft.copy(fullPrompt = weightedPrompt)
        } else {
            draft
        }
    }
    val negativePrompt = remember(limitedTags) { generateNegativePrompt(limitedTags) }
    // 模型目录与设备能力检测提升到顶层，供 tab 0 / tab 2 共享，避免各 tab 各自重复加载
    val baseCatalog = remember { ModelRegistry.loadCatalog(context) }
    // 动态合并导入的检测/分割模型（不在内置目录中的 .onnx 文件）
    val modelCatalog = remember(baseCatalog, aiModels, modelCatalogRefreshKey) {
        val catalog = baseCatalog.toMutableList()
        val knownNames = catalog.map { it.repoName }.toSet()
        val dir = TaggerEngine.modelDirectory(context)
        dir.listFiles()?.filter { it.extension.equals("onnx", ignoreCase = true) && it.length() > 0L }?.forEach { file ->
            val baseName = file.nameWithoutExtension
            if (baseName !in knownNames) {
                val lower = baseName.lowercase()
                val isSeg = lower.contains("seg")
                val isDet = (lower.contains("yolo") || lower.contains("detect")) && !isSeg
                if (isSeg || isDet) {
                    catalog.add(ModelRegistry.ModelEntry(
                        id = baseName,
                        name = baseName,
                        category = if (isSeg) ModelRegistry.ModelCategory.SEGMENTATION else ModelRegistry.ModelCategory.DETECTION,
                        version = "imported",
                        sizeLabel = "${file.length() / 1024 / 1024}MB",
                        sizeBytes = file.length(),
                        backend = ModelRegistry.Backend.ONNX,
                        downloadUrl = "",
                        tagFileUrl = null,
                        repoName = baseName,
                        recommendedDevice = "",
                        speedRank = 50,
                        accuracyRank = 50,
                        description = "",
                        inputSize = 640,
                        isOfficial = false
                    ))
                }
            }
        }
        catalog
    }
    val deviceReport = remember { DeviceCapability.detect(context) }
    val installedTaggerEntries = remember(modelCatalog, aiModels, modelCatalogRefreshKey) {
        modelCatalog.filter {
            it.category == ModelRegistry.ModelCategory.TAGGER && ModelRegistry.isInstalled(context, it)
        }
    }
    val installedDetectionEntries = remember(modelCatalog, aiModels, modelCatalogRefreshKey) {
        modelCatalog.filter {
            it.category == ModelRegistry.ModelCategory.DETECTION && ModelRegistry.isInstalled(context, it)
        }
    }
    val installedSegEntries = remember(modelCatalog, aiModels, modelCatalogRefreshKey) {
        modelCatalog.filter {
            it.category == ModelRegistry.ModelCategory.SEGMENTATION && ModelRegistry.isInstalled(context, it)
        }
    }
    val selectedAiModelName = aiModels.firstOrNull { it.id == selectedAiModelId }?.displayName
        ?: engine.currentModelName
    val todayDateKey = todayKey()
    val defaultHeroSubtitle = stringResource(R.string.app_hero_subtitle)
    val effectiveHeroSubtitle = remember(
        heroSubtitleMode,
        heroCustomSubtitle,
        heroPoetrySubtitle,
        defaultHeroSubtitle
    ) {
        resolveHeroSubtitle(
            mode = heroSubtitleMode,
            customSubtitle = heroCustomSubtitle,
            poetrySubtitle = heroPoetrySubtitle,
            defaultSubtitle = defaultHeroSubtitle
        )
    }
    LaunchedEffect(heroSubtitleMode, heroPoetryDate, heroPoetrySubtitle) {
        if (heroSubtitleMode == HERO_SUBTITLE_MODE_POETRY && heroPoetryDate != todayDateKey) {
            val fetched = withContext(Dispatchers.IO) {
                withTimeoutOrNull(8_000L) {
                    fetchDailyPoetrySubtitle().ifBlank { fallbackPoetrySubtitle() }
                } ?: fallbackPoetrySubtitle()
            }
            onHeroPoetrySubtitleChange(fetched.take(HERO_SUBTITLE_MAX_LENGTH), todayDateKey)
        }
    }
    LaunchedEffect(currentTagText, translateTargetLang) {
        translatedTags = emptyList()
    }
    val changeAiDownloadSource: (String) -> Unit = { source ->
        aiDownloadSource = source
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AI_MODEL_DOWNLOAD_SOURCE, source)
            .apply()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    fun ensureDownloadNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val startAiModelDownload: (DownloadableAiModel) -> Unit = { model ->
        if (downloadingAiModelId == null) {
            ensureDownloadNotificationPermission()
            downloadingAiModelId = model.id
            downloadProgress = DownloadProgress(model.id, context.getString(R.string.ai_model_preparing_download), 0, 0L, -1L)
            isDownloadCancelled = false
            AiModelDownloadService.start(context, model, aiDownloadSource)
        }
    }
    val cancelAiModelDownload = {
        isDownloadCancelled = true
        AiModelDownloadService.cancel(context)
        downloadProgress = downloadProgress?.copy(
            phase = context.getString(R.string.ai_model_download_cancelling)
        )
        Toast.makeText(context, context.getString(R.string.ai_model_download_cancelling), Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(Unit) {
        if (AiModelDownloadService.isRunning) {
            downloadingAiModelId = AiModelDownloadService.currentModelId
            downloadProgress = AiModelDownloadService.currentProgress
        }
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                val modelId = intent.getStringExtra(AiModelDownloadService.EXTRA_MODEL_ID)
                val phase = intent.getStringExtra(AiModelDownloadService.EXTRA_PHASE)
                val percent = intent.getIntExtra(AiModelDownloadService.EXTRA_PERCENT, 0)
                val received = intent.getLongExtra(AiModelDownloadService.EXTRA_RECEIVED_BYTES, 0L)
                val total = intent.getLongExtra(AiModelDownloadService.EXTRA_TOTAL_BYTES, -1L)
                val verifying = intent.getBooleanExtra(AiModelDownloadService.EXTRA_VERIFYING, false)
                if (!modelId.isNullOrBlank() && !phase.isNullOrBlank()) {
                    downloadingAiModelId = modelId
                    downloadProgress = DownloadProgress(modelId, phase, percent, received, total, verifying)
                }
                when (action) {
                    AiModelDownloadService.ACTION_FINISHED -> {
                        val message = intent.getStringExtra(AiModelDownloadService.EXTRA_MESSAGE)
                            ?: context.getString(R.string.ai_model_download_success, "")
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        tags = emptyList()
                        imageScore = null
                        lastInferenceTimeMs = null
                        onReloadAiModels()
                        // 下载完成后刷新模型安装状态，避免卡片仍显示"下载"按钮
                        modelCatalogRefreshKey++
                        // 仅标签模型下载完成后自动选中并加载
                        // 检测/分割模型不需要加载为 tagger，避免报错
                        downloadingAiModelId?.let { modelId ->
                            val entry = modelCatalog.firstOrNull { it.id == modelId }
                            // 仅当 catalog 中找到该模型且为 TAGGER 时才自动选中
                            if (entry != null && entry.category == ModelRegistry.ModelCategory.TAGGER) {
                                onSelectAiModel(modelId)
                            }
                        }
                        downloadingAiModelId = null
                        downloadProgress = null
                        isDownloadCancelled = false
                    }
                    AiModelDownloadService.ACTION_FAILED,
                    AiModelDownloadService.ACTION_CANCELLED -> {
                        val message = intent.getStringExtra(AiModelDownloadService.EXTRA_MESSAGE)
                            ?: context.getString(R.string.ai_model_download_failed)
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        downloadingAiModelId = null
                        downloadProgress = null
                        isDownloadCancelled = false
                        onReloadAiModels()
                        // 失败/取消后也刷新，清理可能残留的临时状态
                        modelCatalogRefreshKey++
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(AiModelDownloadService.ACTION_PROGRESS)
            addAction(AiModelDownloadService.ACTION_FINISHED)
            addAction(AiModelDownloadService.ACTION_FAILED)
            addAction(AiModelDownloadService.ACTION_CANCELLED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
    val selectAiModelFromUi: (String) -> Unit = {
        tags = emptyList()
        imageScore = null
        lastInferenceTimeMs = null
        onSelectAiModel(it)
    }

    // 进入淡化动画 — fade the whole screen in on first composition,
    // instead of it just appearing abruptly.
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }
    LaunchedEffect(incomingSpecialLinkRecord?.id) {
        val record = incomingSpecialLinkRecord ?: return@LaunchedEffect
        val restoredTags = record.text.toTags()
        tags = restoredTags
        imageScore = scoreImage(null, restoredTags)
        // 大图解码放到 IO 线程，避免主线程 OOM 闪退
        bitmap = withContext(Dispatchers.IO) { loadBitmapFromRecord(record) }
        imageUri = null
        historyRecords = saveTagRecord(context, KEY_HISTORY_TAG_RECORDS, record.text)
        Toast.makeText(context, context.getString(R.string.special_link_loaded), Toast.LENGTH_SHORT).show()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            // 大图解码放到 IO 线程，避免主线程 OOM 闪退
            scope.launch {
                try {
                    val loaded = withContext(Dispatchers.IO) { loadBitmap(context, uri) }
                    if (loaded == null) {
                        Toast.makeText(context, context.getString(R.string.image_load_failed), Toast.LENGTH_LONG).show()
                        imageUri = null
                    } else {
                        bitmap = loaded
                        tags = emptyList()
                        imageScore = null
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("pickImage", "Failed to load image", e)
                    Toast.makeText(context, context.getString(R.string.image_load_failed), Toast.LENGTH_LONG).show()
                    imageUri = null
                }
            }
        }
    }

    // 批量选图：让用户从相册勾选多张图片，然后逐张识别
    val batchPickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            // 限制最多 10 张，超过则只取前 10 张并提示
            val limited = uris.take(10)
            if (uris.size > 10) {
                Toast.makeText(
                    context,
                    context.getString(R.string.batch_limit_toast, 10),
                    Toast.LENGTH_LONG
                ).show()
            }
            selectedBatchUris = limited
            pendingBatchUris = limited.toMutableList()
            showBatchConfirmDialog = true
        }
    }

    val importAiFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // 文件导入（复制/解压）放到 IO 线程，避免主线程卡顿/ANR
            scope.launch {
                val message = withContext(Dispatchers.IO) { importAiModelFile(context, uri) }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                onReloadAiModels()
                // 导入后刷新模型安装状态
                modelCatalogRefreshKey++
            }
        }
    }

    val pickCustomBackgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // 大图解码放到 IO 线程，避免主线程 OOM 闪退
            scope.launch {
                try {
                    val selectedBitmap = withContext(Dispatchers.IO) { loadBitmap(context, uri) }
                    if (selectedBitmap == null) {
                        Toast.makeText(context, context.getString(R.string.settings_custom_background_import_failed), Toast.LENGTH_LONG).show()
                    } else {
                        pendingCustomBackgroundBitmap = selectedBitmap
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("pickBg", "Failed to load background image", e)
                    Toast.makeText(context, context.getString(R.string.settings_custom_background_import_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    pendingCustomBackgroundBitmap?.let { sourceBitmap ->
        CustomBackgroundCropDialog(
            bitmap = sourceBitmap,
            onDismiss = {
                sourceBitmap.recycle()
                pendingCustomBackgroundBitmap = null
            },
            onConfirm = { croppedBitmap ->
                val path = saveCustomBackgroundBitmap(context, croppedBitmap)
                if (path.isNullOrBlank()) {
                    Toast.makeText(context, context.getString(R.string.settings_custom_background_import_failed), Toast.LENGTH_LONG).show()
                } else {
                    onCustomBackgroundImagePathChange(path)
                    Toast.makeText(context, context.getString(R.string.settings_custom_background_import_success), Toast.LENGTH_SHORT).show()
                }
                sourceBitmap.recycle()
                pendingCustomBackgroundBitmap = null
            }
        )
    }

    if (downloadProgress?.isVerifying == true) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(R.string.ai_model_verifying_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.ai_model_verifying_dialog_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        "100% · ${stringResource(R.string.ai_model_verifying)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {}
        )
    }

    if (showPoetryApiNotice) {
        AlertDialog(
            onDismissRequest = { showPoetryApiNotice = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(R.string.settings_subtitle_poetry_notice_title)) },
            text = {
                Text(
                    stringResource(R.string.settings_subtitle_poetry_notice_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPoetryApiNotice = false
                        onHeroPoetryNoticeShownChange(true)
                        onHeroSubtitleModeChange(HERO_SUBTITLE_MODE_POETRY)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.settings_subtitle_poetry_notice_confirm))
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showPoetryApiNotice = false
                            onHeroPoetryRejectedChange(true)
                            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                                .putBoolean(KEY_HERO_POETRY_REJECTED, true)
                                .putBoolean(KEY_HERO_POETRY_NOTICE_SHOWN, true)
                                .apply()
                        }
                    ) {
                        Text(stringResource(R.string.settings_subtitle_poetry_notice_reject))
                    }
                    TextButton(onClick = { showPoetryApiNotice = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }

    if (showImportGuideDialog) {
        ImportAiModelGuideDialog(
            onDismiss = { showImportGuideDialog = false },
            onContinue = {
                showImportGuideDialog = false
                importAiFileLauncher.launch("*/*")
            }
        )
    }

    // 精准模式首次开启提醒弹窗
    if (showPrecisionModeNotice) {
        AlertDialog(
            onDismissRequest = {
                // 点外部区域 = 放弃，不开启精准模式
                showPrecisionModeNotice = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.precision_mode_notice_title))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.precision_mode_notice_body),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.precision_mode_notice_bullets),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.precision_mode_notice_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        // 放弃：不开启，下次仍会提醒
                        showPrecisionModeNotice = false
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.ai_model_download_abandon))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 确认：开启精准模式，标记已提醒
                        showPrecisionModeNotice = false
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_PRECISION_MODE_NOTICE_SHOWN, true)
                            .apply()
                        onPrecisionModeChange(true)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.confirm_action_yes))
                }
            }
        )
    }

    confirmActionRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { confirmActionRequest = null },
            title = { Text(request.title) },
            text = { Text(request.message) },
            confirmButton = {
                Button(
                    onClick = {
                        val action = request.onConfirm
                        confirmActionRequest = null
                        action()
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.confirm_action_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmActionRequest = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showFirstLaunchFlowDialog) {
        FirstLaunchFlowDialog(
            currentLanguage = languageOption,
            onLanguageChange = onFirstLaunchLanguageChange,
            onPrivacyAgree = {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_PRIVACY_AGREED, true).apply()
            },
            onComplete = onFirstLaunchFlowDismiss,
            onDisagree = onPrivacyDisagree
        )
    }

    // Settings-triggered legacy dialogs are still supported, but are no longer used for first launch.
    if (showLanguageSelectDialog) {
        LanguageSelectDialog(
            currentLanguage = languageOption,
            onLanguageSelected = { selectedLang ->
                val normalized = normalizeLanguageOption(selectedLang)
                if (normalized != languageOption) {
                    onLanguageChange(normalized)
                } else {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putBoolean(KEY_LANGUAGE_SELECTED, true).apply()
                    onShowLanguageSelectDialogChange(false)
                    if (!context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .getBoolean(KEY_PRIVACY_AGREED, false)) onShowPrivacyDialog()
                    else if (!context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .getBoolean(KEY_INTRO_SHOWN, false)) onShowIntroDialog()
                }
            }
        )
    }

    if (showIntroDialog) IntroDialog(onDismiss = onIntroDismiss)

    if (showPrivacyDialog) {
        PrivacyDialog(onAgree = onPrivacyAgree, onDisagree = onPrivacyDisagree)
    }

    if (showExperienceIntroDialog) {
        ExperienceIntroDialog(onDismiss = onExperienceIntroDismiss)
    }

    if (showFileManager) {
        FileManagerDialog(
            onReloadAiModels = onReloadAiModels,
            onSelectAiModel = onSelectAiModel,
            onConfirmDelete = { action -> confirmOrRun(true, action) },
            onDismiss = { showFileManager = false }
        )
    }

    if (showBatchConfirmDialog) {
        BatchConfirmDialog(
            uris = pendingBatchUris,
            onCancel = {
                showBatchConfirmDialog = false
                pendingBatchUris = emptyList()
            },
            onToggle = { uri ->
                pendingBatchUris = if (pendingBatchUris.contains(uri)) {
                    pendingBatchUris.filter { it != uri }
                } else {
                    pendingBatchUris + uri
                }
            },
            onStart = {
                showBatchConfirmDialog = false
                if (pendingBatchUris.isNotEmpty()) {
                    selectedBatchUris = pendingBatchUris
                    pendingBatchUris = emptyList()
                    batchProgressIndex = 0
                    batchResults = emptyList()
                    isBatchRunning = true
                    showBatchDialog = true
                }
            }
        )
    }

    if (showBatchDialog) {
        BatchProgressDialog(
            total = selectedBatchUris.size,
            currentIndex = batchProgressIndex,
            isRunning = isBatchRunning,
            results = batchResults,
            onDismiss = {
                showBatchDialog = false
                selectedBatchUris = emptyList()
                batchResults = emptyList()
                batchProgressIndex = 0
            }
        )
    }

    if (showTranslateNetworkNotice) {
        AlertDialog(
            onDismissRequest = { showTranslateNetworkNotice = false },
            icon = {
                Icon(
                    Icons.Filled.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.translate_network_notice_title)) },
            text = {
                Text(
                    stringResource(R.string.translate_network_notice_message),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_TRANSLATE_NOTICE_SHOWN, true)
                            .apply()
                        translateNoticeShown = true
                        showTranslateNetworkNotice = false
                        showTranslateDialog = true
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.translate_network_notice_continue))
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showTranslateNetworkNotice = false
                            translateRejected = true
                            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(KEY_TRANSLATE_REJECTED, true)
                                .putBoolean(KEY_TRANSLATE_NOTICE_SHOWN, true)
                                .apply()
                        }
                    ) {
                        Text(stringResource(R.string.translate_network_notice_reject))
                    }
                    TextButton(onClick = { showTranslateNetworkNotice = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }

    // 翻译拒绝后重新开启弹窗
    if (showTranslateRejectedDialog) {
        AlertDialog(
            onDismissRequest = { showTranslateRejectedDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(R.string.translate_rejected_title)) },
            text = {
                Text(
                    stringResource(R.string.translate_rejected_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTranslateRejectedDialog = false
                        translateRejected = false
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_TRANSLATE_REJECTED, false)
                            .putBoolean(KEY_TRANSLATE_NOTICE_SHOWN, false)
                            .apply()
                        translateNoticeShown = false
                        showTranslateDialog = true
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.translate_rejected_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTranslateRejectedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // 古诗词拒绝后重新开启弹窗
    if (showPoetryRejectedDialog) {
        AlertDialog(
            onDismissRequest = { showPoetryRejectedDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(R.string.poetry_rejected_title)) },
            text = {
                Text(
                    stringResource(R.string.poetry_rejected_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPoetryRejectedDialog = false
                        onHeroPoetryRejectedChange(false)
                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(KEY_HERO_POETRY_REJECTED, false)
                            .putBoolean(KEY_HERO_POETRY_NOTICE_SHOWN, false)
                            .apply()
                        onHeroPoetryNoticeShownChange(false)
                        showPoetryApiNotice = true
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.poetry_rejected_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPoetryRejectedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showTranslateDialog) {
        TranslateLanguageDialog(
            currentLang = translateTargetLang,
            isTranslating = isTranslating,
            onSelect = { lang ->
                showTranslateDialog = false
                translateTargetLang = lang
                translatedTags = emptyList()
                isTranslating = true
                scope.launch {
                    val tagNames = limitedTags
                        .filterPromptNoiseTags()
                        .map { it.name }
                        .distinct()
                    val result = withContext(Dispatchers.IO) {
                        translateTagsOnline(tagNames, lang)
                    }
                    translatedTags = result
                    isTranslating = false
                }
            },
            onDismiss = { showTranslateDialog = false }
        )
    }

    if (showCompareNeedModelsDialog) {
        AlertDialog(
            onDismissRequest = { showCompareNeedModelsDialog = false },
            icon = {
                Icon(
                    Icons.Filled.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.compare_need_models_title)) },
            text = {
                Text(
                    stringResource(R.string.compare_need_models_message),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompareNeedModelsDialog = false
                        selectedMainTab = 2
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.model_go_download))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompareNeedModelsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showCompareDialog) {
        CompareModelsDialog(
            availableModels = aiModels,
            model1Id = compareModel1Id,
            model2Id = compareModel2Id,
            result1 = compareResult1,
            result2 = compareResult2,
            isComparing = isComparing,
            optimized = compareOptimized,
            canRun = bitmap != null && compareModel1Id != null && compareModel2Id != null && compareModel1Id != compareModel2Id,
            onModel1Change = { compareModel1Id = it; compareResult1 = emptyList(); compareResult2 = emptyList(); compareOptimized = emptyList() },
            onModel2Change = { compareModel2Id = it; compareResult1 = emptyList(); compareResult2 = emptyList(); compareOptimized = emptyList() },
            onRun = {
                val bmp = bitmap
                val m1Id = compareModel1Id
                val m2Id = compareModel2Id
                if (bmp == null || m1Id == null || m2Id == null) return@CompareModelsDialog
                isComparing = true
                compareResult1 = emptyList()
                compareResult2 = emptyList()
                compareOptimized = emptyList()
                scope.launch {
                    val (r1, r2) = try {
                        withContext(Dispatchers.IO) {
                            val m1 = aiModels.firstOrNull { it.id == m1Id }
                            val m2 = aiModels.firstOrNull { it.id == m2Id }
                            val originalId = selectedAiModelId
                            var t1 = emptyList<TaggerEngine.Tag>()
                            var t2 = emptyList<TaggerEngine.Tag>()
                            if (m1 != null) {
                                engine.load(m1)
                                t1 = runCatching { engine.tag(bmp, threshold, generalTagWeight, characterTagWeight) }
                                    .getOrDefault(emptyList())
                                    .filterPromptNoiseTags()
                            }
                            if (m2 != null) {
                                engine.load(m2)
                                t2 = runCatching { engine.tag(bmp, threshold, generalTagWeight, characterTagWeight) }
                                    .getOrDefault(emptyList())
                                    .filterPromptNoiseTags()
                            }
                            // 恢复原模型
                            val orig = aiModels.firstOrNull { it.id == originalId } ?: aiModels.firstOrNull()
                            if (orig != null) engine.load(orig)
                            t1 to t2
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("CompareModels", "模型对比失败: ${e.message}", e)
                        emptyList<TaggerEngine.Tag>() to emptyList<TaggerEngine.Tag>()
                    }
                    compareResult1 = r1.take(safePromptTagLimit)
                    compareResult2 = r2.take(safePromptTagLimit)
                    isComparing = false
                }
            },
            onOptimize = {
                // 合并去重，按平均分排序
                val merged = (compareResult1 + compareResult2)
                    .groupBy { it.name }
                    .map { (name, tags) ->
                        TaggerEngine.Tag(name = name, score = tags.map { it.score }.average().toFloat(), category = tags.first().category)
                    }
                    .sortedByDescending { it.score }
                    .filterPromptNoiseTags()
                    .take(safePromptTagLimit)
                compareOptimized = merged
            },
            onCopyOptimized = {
                val text = compareOptimized.toTagText()
                copyTextToClipboard(context, text, "compare_optimized")
                Toast.makeText(context, context.getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
            },
            onTranslateOptimized = {
                showCompareDialog = false
                // 把优化结果作为当前标签，触发翻译流程
                tags = compareOptimized.filterPromptNoiseTags()
                openTranslateWithNotice()
            },
            onDismiss = { showCompareDialog = false }
        )
    }

    if (showCommunityDialog) {
        CommunityDialog(onDismiss = { showCommunityDialog = false })
    }

    if (showSponsorDialog) {
        SponsorDialog(onDismiss = { showSponsorDialog = false })
    }

    if (showParseLinkDialog) {
        ParseSpecialLinkDialog(
            onDismiss = { showParseLinkDialog = false },
            onParse = { linkText ->
                val record = parseSpecialTagLinkText(linkText)
                if (record == null) {
                    Toast.makeText(context, context.getString(R.string.special_link_invalid), Toast.LENGTH_SHORT).show()
                } else {
                    val restoredTags = record.text.toTags()
                    tags = restoredTags
                    imageScore = scoreImage(null, restoredTags)
                    // 大图解码放到 IO 线程，避免主线程 OOM 闪退
                    scope.launch {
                        bitmap = withContext(Dispatchers.IO) { loadBitmapFromRecord(record) }
                    }
                    imageUri = null
                    historyRecords = saveTagRecord(context, KEY_HISTORY_TAG_RECORDS, record.text, record.imagePath)
                    showParseLinkDialog = false
                    Toast.makeText(context, context.getString(R.string.special_link_loaded), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showAiModelDialog) {
        AiModelDialog(
            models = aiModels,
            downloadableModels = BUILT_IN_DOWNLOADABLE_AI_MODELS,
            selectedModelId = selectedAiModelId,
            isLoadingModel = isLoadingModel,
            downloadingModelId = downloadingAiModelId,
            downloadProgress = downloadProgress,
            downloadSource = aiDownloadSource,
            onDownloadSourceChange = { source ->
                aiDownloadSource = source
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_AI_MODEL_DOWNLOAD_SOURCE, source)
                    .apply()
            },
            onImportFile = { showImportGuideDialog = true },
            onDownloadModel = startAiModelDownload,
            onCancelDownload = {
                cancelAiModelDownload()
            },
            onSelectModel = {
                tags = emptyList()
                imageScore = null
                lastInferenceTimeMs = null
                onSelectAiModel(it)
                showAiModelDialog = false
            },
            onDismiss = { showAiModelDialog = false }
        )
    }

    if (showFavoritesDialog) {
        TagRecordDialog(
            title = stringResource(R.string.favorites_title),
            emptyText = stringResource(R.string.favorites_empty),
            records = favoriteRecords,
            onDismiss = { showFavoritesDialog = false },
            onCopy = { copyTextToClipboard(context, it.text) },
            onShare = { shareSpecialTagLink(context, it.text) },
            onFavorite = {
                confirmOrRun(false) {
                    favoriteRecords = saveTagRecord(context, KEY_FAVORITE_TAG_RECORDS, it.text, it.imagePath)
                }
            },
            onBatchFavorite = {
                confirmOrRun(false) {
                    favoriteRecords = saveTagRecords(
                        context,
                        KEY_FAVORITE_TAG_RECORDS,
                        recordsToSaveUnique(it + favoriteRecords).take(MAX_TAG_RECORDS)
                    )
                }
            },
            onBatchShare = { shareSpecialTagLinks(context, it) },
            onUse = {
                val restoredTags = it.text.toTags()
                tags = restoredTags
                imageUri = null
                showFavoritesDialog = false
                // 大图解码放到 IO 线程，避免主线程 OOM 闪退
                scope.launch {
                    val restoredBitmap = withContext(Dispatchers.IO) { loadBitmapFromRecord(it) }
                    bitmap = restoredBitmap
                    imageScore = scoreImage(restoredBitmap, restoredTags)
                }
            },
            onDelete = {
                confirmOrRun(true) {
                    favoriteRecords = deleteTagRecord(context, KEY_FAVORITE_TAG_RECORDS, it.id)
                }
            },
            onBatchDelete = { selected ->
                confirmOrRun(true) {
                    val selectedIds = selected.map { it.id }.toSet()
                    favoriteRecords = saveTagRecords(
                        context,
                        KEY_FAVORITE_TAG_RECORDS,
                        favoriteRecords.filterNot { it.id in selectedIds }
                    )
                }
            }
        )
    }

    if (showHistoryDialog) {
        TagRecordDialog(
            title = stringResource(R.string.history_title),
            emptyText = stringResource(R.string.history_empty),
            records = historyRecords,
            onDismiss = { showHistoryDialog = false },
            onCopy = { copyTextToClipboard(context, it.text) },
            onShare = { shareSpecialTagLink(context, it.text) },
            onFavorite = {
                confirmOrRun(false) {
                    favoriteRecords = saveTagRecord(context, KEY_FAVORITE_TAG_RECORDS, it.text, it.imagePath)
                }
            },
            onBatchFavorite = {
                confirmOrRun(false) {
                    favoriteRecords = saveTagRecords(
                        context,
                        KEY_FAVORITE_TAG_RECORDS,
                        recordsToSaveUnique(it + favoriteRecords).take(MAX_TAG_RECORDS)
                    )
                }
            },
            onBatchShare = { shareSpecialTagLinks(context, it) },
            onUse = {
                val restoredTags = it.text.toTags()
                tags = restoredTags
                imageUri = null
                showHistoryDialog = false
                // 大图解码放到 IO 线程，避免主线程 OOM 闪退
                scope.launch {
                    val restoredBitmap = withContext(Dispatchers.IO) { loadBitmapFromRecord(it) }
                    bitmap = restoredBitmap
                    imageScore = scoreImage(restoredBitmap, restoredTags)
                }
            },
            onDelete = {
                confirmOrRun(true) {
                    historyRecords = deleteTagRecord(context, KEY_HISTORY_TAG_RECORDS, it.id)
                }
            },
            onBatchDelete = { selected ->
                confirmOrRun(true) {
                    val selectedIds = selected.map { it.id }.toSet()
                    historyRecords = saveTagRecords(
                        context,
                        KEY_HISTORY_TAG_RECORDS,
                        historyRecords.filterNot { it.id in selectedIds }
                    )
                }
            }
        )
    }

    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(tween(400, easing = FastOutSlowInEasing))
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景层
        if (useCustomBackgroundStyle && customBackgroundImagePath.isNotBlank()) {
            // 异步加载背景图，避免在主线程解码大 Bitmap 导致 OOM 闪退
            val bitmap by produceState<Bitmap?>(null, customBackgroundImagePath) {
                val path = customBackgroundImagePath.takeIf { it.isNotBlank() }
                if (path != null) {
                    value = withContext(Dispatchers.IO) { loadBitmapFromPath(path, 2048) }
                } else {
                    value = null
                }
            }
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val currentBitmap = bitmap
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(customBackgroundOpacity.coerceIn(0f, 1f))
                    )
                }
                if (effectiveDarkTheme) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.34f)))
                }
                if (customBackgroundDimAmount > 0f) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = customBackgroundDimAmount.coerceIn(0f, 0.85f))))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .appThemedBackground()
            )
        }
        CompositionLocalProvider(
            LocalSubtitleFontSize provides heroSubtitleFontSize,
            LocalCardOpacity provides (if (useCustomBackgroundStyle) customBackgroundTabBarOpacity else 1f)
        ) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            IosMorphingSegmentedControl(
                options = listOf(
                    "0" to stringResource(R.string.main_tab_recognition),
                    "1" to stringResource(R.string.main_tab_records),
                    "2" to stringResource(R.string.main_tab_models),
                    "3" to stringResource(R.string.settings_title)
                ),
                current = selectedMainTab.toString(),
                onSelect = { tabValue ->
                    // 防止异常状态值导致切页时崩溃
                    // 回到顶部已由 LaunchedEffect(selectedMainTab) 统一处理，
                    // 这里只更新目标 tab，不在点击回调里启动协程，避免快速连点时协程堆积。
                    selectedMainTab = tabValue.toIntOrNull()?.coerceIn(0, 3) ?: 0
                },
                tabBarOpacity = if (useCustomBackgroundStyle) customBackgroundTabBarOpacity else 0.58f,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 2.dp)
                    .fillMaxWidth()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 6.dp, bottom = 16.dp)
                .padding(bottom = 12.dp)
                .verticalScroll(mainScrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnimatedVisibility(visible = selectedMainTab != 2 && loadError != null) {
                val localizedLoadError = localizedLoadErrorText(loadError)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .softEnter(0)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = localizedLoadError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        FilledTonalButton(
                            onClick = { selectedMainTab = 2 },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                stringResource(R.string.model_go_download),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (selectedMainTab != 3) {
                AppHeroHeader(
                    subtitle = effectiveHeroSubtitle,
                    subtitleFontSize = heroSubtitleFontSize,
                    modifier = Modifier.softEnter(0)
                )

                TopActionRow(
                    onHistoryClick = { showHistoryDialog = true },
                    onFavoritesClick = { showFavoritesDialog = true },
                    onCompareClick = {
                        if (aiModels.size < 2) {
                            showCompareNeedModelsDialog = true
                        } else {
                            compareModel1Id = aiModels.firstOrNull()?.id
                            compareModel2Id = aiModels.elementAtOrNull(1)?.id
                            compareResult1 = emptyList()
                            compareResult2 = emptyList()
                            compareOptimized = emptyList()
                            showCompareDialog = true
                        }
                    },
                    modifier = Modifier.softEnter(1),
                    barOpacity = if (useCustomBackgroundStyle) customBackgroundTabBarOpacity else 0.92f
                )
            }

            AnimatedContent(
                targetState = selectedMainTab,
                transitionSpec = {
                    // 仅做淡入淡出，不做纵向位移。外层是 verticalScroll（无限高度约束），
                    // slideIn/slideOutVertically 在过渡期需基于内容高度计算位移，
                    // 与无限高度 + 双内容并存的测量叠加时可能触发崩溃。纯 fade 最稳。
                    fadeIn(animationSpec = tween(220)) togetherWith
                        fadeOut(animationSpec = tween(180))
                },
                label = "tabContent"
            ) { tab ->
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (tab == 0) {
            // Image preview — big, rounded, Pixel-style surface
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .softEnter(3),
                shape = RoundedCornerShape(28.dp),
                colors = themedCardColors()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = bitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp))
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_image_selected),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (useCustomBackgroundStyle) customBackgroundTabBarOpacity else 1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.pick_image),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                FilledTonalButton(
                    onClick = {
                        batchPickLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (useCustomBackgroundStyle) customBackgroundTabBarOpacity else 1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.batch_pick_image),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            AnimatedVisibility(visible = experienceEnabled) {
                ExperienceCard(
                    state = experienceState
                )
            }

            // ---- 工作流 · 模型搭配（与识别合并）----
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = themedCardColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .softEnter(2)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // 可折叠头部
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { workflowExpanded = !workflowExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AccountTree,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.workflow_model_pairing_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (precisionMode)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    if (precisionMode) stringResource(R.string.settings_precision_mode_precise) else stringResource(R.string.settings_precision_mode_normal),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (precisionMode)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = if (workflowExpanded) 180f else 0f
                                }
                            )
                        }
                    }

                    // 展开内容
                    AnimatedVisibility(
                        visible = workflowExpanded,
                        enter = fadeIn(tween(180)) + expandVertically(tween(200)),
                        exit = fadeOut(tween(120)) + shrinkVertically(tween(160))
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 标签模型选择
                            Text(
                                stringResource(R.string.workflow_tag_model_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (installedTaggerEntries.isEmpty()) {
                                Text(
                                    stringResource(R.string.workflow_tag_model_empty),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    installedTaggerEntries.forEach { entry ->
                                        FilterChip(
                                            selected = entry.id == selectedAiModelId,
                                            onClick = { selectAiModelFromUi(entry.id) },
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp),
                                            label = {
                                                Text(
                                                    entry.name,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                            // 检测模型选择（仅精准模式下显示）
                            if (precisionMode) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                Text(
                                    stringResource(R.string.workflow_detection_model_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (installedDetectionEntries.isEmpty()) {
                                    Text(
                                        stringResource(R.string.workflow_detection_model_empty),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        installedDetectionEntries.forEach { entry ->
                                            FilterChip(
                                                selected = detModelName == "${entry.repoName}.onnx",
                                                onClick = {
                                                    onSelectDetModel("${entry.repoName}.onnx")
                                                    onSelectDetectionModel(entry.id)
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp),
                                                label = {
                                                    Text(
                                                        entry.name,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // 分割模型选择（仅精准模式下显示）
                            if (precisionMode) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                Text(
                                    stringResource(R.string.workflow_seg_model_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (installedSegEntries.isEmpty()) {
                                    Text(
                                        stringResource(R.string.workflow_seg_model_empty),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        installedSegEntries.forEach { entry ->
                                            FilterChip(
                                                selected = segModelName == "${entry.repoName}.onnx",
                                                onClick = {
                                                    onSelectSegModel("${entry.repoName}.onnx")
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp),
                                                label = {
                                                    Text(
                                                        entry.name,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // 检测模型状态（仅在精准模式下显示）
                            if (precisionMode) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                Text(
                                    stringResource(R.string.workflow_detection_model_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (installedDetectionEntries.isEmpty()) {
                                    Text(
                                        stringResource(R.string.workflow_detection_model_empty),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    // 只显示当前选中的检测模型，不显示全部列表
                                    val currentDetModel = installedDetectionEntries.find { it.id == selectedDetectionModelId }
                                    Text(
                                        stringResource(
                                            R.string.workflow_det_model_status_format,
                                            currentDetModel?.name ?: installedDetectionEntries.first().name,
                                            currentDetModel?.version ?: installedDetectionEntries.first().version,
                                            currentDetModel?.sizeLabel ?: installedDetectionEntries.first().sizeLabel,
                                            installedDetectionEntries.size
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 分割模型状态（仅在精准模式下显示）
                            if (precisionMode) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                Text(
                                    stringResource(R.string.workflow_seg_model_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (installedSegEntries.isEmpty()) {
                                    Text(
                                        stringResource(R.string.workflow_seg_model_empty),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    val currentSegModel = installedSegEntries.find {
                                        segModelName == "${it.repoName}.onnx"
                                    }
                                    val segLoadedText = stringResource(if (segReady) R.string.model_status_loaded else R.string.model_status_not_loaded)
                                    Text(
                                        stringResource(
                                            R.string.workflow_seg_model_status_format,
                                            currentSegModel?.name ?: installedSegEntries.first().name,
                                            segLoadedText,
                                            installedSegEntries.size
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (segReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            val currentWorkflow = wfEngine.builtInWorkflows.find {
                                it.id == selectedWorkflowId
                            } ?: wfEngine.builtInWorkflows.first()
                            Text(
                                stringResource(R.string.workflow_steps_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // 工作流预设选择
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                wfEngine.builtInWorkflows.forEach { wf ->
                                    FilterChip(
                                        selected = wf.id == selectedWorkflowId,
                                        onClick = {
                                            selectedWorkflowId = wf.id
                                            // 同步精准模式状态：选择精准模式流程时开启精准模式
                                            val isPrecisionWf = wf.id == "preset_precision"
                                            if (isPrecisionWf && !precisionMode && !precisionModeNoticeShown) {
                                                showPrecisionModeNotice = true
                                            } else if (isPrecisionWf != precisionMode) {
                                                onPrecisionModeChange(isPrecisionWf)
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp),
                                        label = {
                                            Text(
                                                when (wf.id) {
                                                    "preset_normal" -> stringResource(R.string.workflow_preset_normal)
                                                    "preset_precision" -> stringResource(R.string.workflow_name_precision_seg)
                                                    else -> wf.name
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    )
                                }
                            }
                            // 步骤展示（使用 FlowRow 自动换行，避免文字截断）
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                currentWorkflow.steps.forEachIndexed { idx, step ->
                                    if (idx > 0) {
                                        Text(
                                            "→",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        when (step.type) {
                                            StepType.INPUT -> stringResource(R.string.workflow_step_input)
                                            StepType.CROP_PERSON -> stringResource(R.string.workflow_step_crop_person)
                                            StepType.DETECT -> stringResource(R.string.workflow_step_detect)
                                            StepType.SEGMENT -> stringResource(R.string.workflow_step_segment)
                                            StepType.TAG -> stringResource(R.string.workflow_step_tag)
                                            StepType.TRANSLATE -> stringResource(R.string.workflow_step_translate)
                                            StepType.OUTPUT -> stringResource(R.string.workflow_step_output)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(),
                colors = themedCardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedVisibility(visible = isLoadingModel) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MorphingBlobLoader(size = 20.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.model_loading),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        stringResource(R.string.threshold_value, "%.2f".format(threshold)),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = threshold,
                        onValueChange = { threshold = it },
                        valueRange = 0.05f..0.95f
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    Text(
                        stringResource(R.string.tag_smart_processing_title),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        stringResource(R.string.tag_smart_processing_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.general_tag_weight_value, "%.2f".format(generalTagWeight)),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = generalTagWeight,
                        onValueChange = onGeneralTagWeightChange,
                        valueRange = 0.3f..2.0f
                    )
                    Text(
                        stringResource(R.string.character_tag_weight_value, "%.2f".format(characterTagWeight)),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = characterTagWeight,
                        onValueChange = onCharacterTagWeightChange,
                        valueRange = 0.3f..2.0f
                    )

                    // 提示词数量上限滑块（移至开始识别按钮上方）
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.settings_prompt_tag_limit_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.settings_prompt_tag_limit_summary, promptTagLimit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Slider(
                        value = promptTagLimit.toFloat(),
                        onValueChange = { onPromptTagLimitChange(it.toInt()) },
                        valueRange = MIN_PROMPT_TAG_LIMIT.toFloat()..MAX_PROMPT_TAG_LIMIT.toFloat(),
                        steps = MAX_PROMPT_TAG_LIMIT - MIN_PROMPT_TAG_LIMIT - 1,
                        colors = themedSliderColors()
                    )

                    // ===== Prompt 权重设置 =====
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.prompt_weight_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.prompt_weight_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Switch(
                            checked = promptWeightEnabled,
                            onCheckedChange = onPromptWeightEnabledChange
                        )
                    }
                    AnimatedVisibility(
                        visible = promptWeightEnabled,
                        enter = fadeIn(spring(stiffness = 380f, dampingRatio = 0.7f)) +
                                expandVertically(spring(stiffness = 350f, dampingRatio = 0.7f)),
                        exit = fadeOut(spring(stiffness = 500f, dampingRatio = 0.9f)) +
                               shrinkVertically(spring(stiffness = 400f, dampingRatio = 0.8f))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // 权重模式选择
                        Text(
                            stringResource(R.string.prompt_weight_mode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = promptWeightMode == PROMPT_WEIGHT_MODE_OFF,
                                onClick = { onPromptWeightModeChange(PROMPT_WEIGHT_MODE_OFF) },
                                label = { Text(stringResource(R.string.prompt_weight_mode_off), style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = promptWeightMode == PROMPT_WEIGHT_MODE_ENHANCE,
                                onClick = { onPromptWeightModeChange(PROMPT_WEIGHT_MODE_ENHANCE) },
                                label = { Text(stringResource(R.string.prompt_weight_mode_enhance), style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                                modifier = Modifier.weight(1.2f)
                            )
                            FilterChip(
                                selected = promptWeightMode == PROMPT_WEIGHT_MODE_FULL,
                                onClick = { onPromptWeightModeChange(PROMPT_WEIGHT_MODE_FULL) },
                                label = { Text(stringResource(R.string.prompt_weight_mode_full), style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        AnimatedVisibility(
                            visible = promptWeightMode != PROMPT_WEIGHT_MODE_OFF,
                            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                            exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "${stringResource(R.string.prompt_weight_strength)}: ${"%.2f".format(promptWeightStrength)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Slider(
                                    value = promptWeightStrength,
                                    onValueChange = onPromptWeightStrengthChange,
                                    valueRange = 0f..1f,
                                    colors = themedSliderColors()
                                )
                                Text(
                                    "${stringResource(R.string.prompt_weight_min_confidence)}: ${"%.2f".format(promptWeightMinConfidence)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Slider(
                                    value = promptWeightMinConfidence,
                                    onValueChange = onPromptWeightMinConfidenceChange,
                                    valueRange = 0f..1f,
                                    colors = themedSliderColors()
                                )
                                Text(
                                    "${stringResource(R.string.prompt_weight_max)}: ${"%.2f".format(promptWeightMax)}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Slider(
                                    value = promptWeightMax,
                                    onValueChange = onPromptWeightMaxChange,
                                    valueRange = 1f..1.5f,
                                    colors = themedSliderColors()
                                )
                            }
                        }
                        }
                    }

                    ElevatedButton(
                        enabled = bitmap != null && engine.isReady && !isRunning && !isLoadingModel,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            val bmp = bitmap ?: return@ElevatedButton
                            inferenceProgressTargetPercent = 0f
                            realProgress = 0f
                            inferenceProgressText = context.getString(R.string.workflow_progress_prepare)
                            isRunning = true
                            // 启动前台通知服务，防止后台被杀死
                            InferenceForegroundService.start(context)
                            var lastNotifyUpdate = 0L
                            inferenceJob = scope.launch {
                                try {
                                    val startedAt = SystemClock.elapsedRealtime()
                                    // 精准模式：先 YOLO11n 检测人物并裁剪，再 WD Tagger 打标，最后合并
                                    val result = withContext(Dispatchers.IO) {
                                        // 如果精准模式开启但模型未加载，显示加载提示
                                        if (precisionMode && !detReady) {
                                            inferenceProgressText = context.getString(R.string.workflow_progress_loading_det)
                                        }
                                        jointInference.run(
                                            bitmap = bmp,
                                            precisionMode = precisionMode && detReady,
                                            threshold = threshold,
                                            generalWeight = generalTagWeight,
                                            characterWeight = characterTagWeight,
                                            onProgress = { progress, text ->
                                                realProgress = progress.coerceIn(0f, 1f)
                                                lastRealProgressUpdateMs = System.currentTimeMillis()
                                                inferenceProgressText = text
                                                // 节流更新通知（每200ms一次）
                                                val now = System.currentTimeMillis()
                                                if (now - lastNotifyUpdate > 200L) {
                                                    lastNotifyUpdate = now
                                                    val pct = (progress * 100).toInt().coerceIn(0, 100)
                                                    InferenceForegroundService.update(context, pct, text)
                                                }
                                            }
                                        ).tags
                                    }
                                    if (!isActive) return@launch
                                    // 后处理阶段：添加进度更新，避免卡在99%
                                    realProgress = 0.98f
                                    lastRealProgressUpdateMs = System.currentTimeMillis()
                                    inferenceProgressText = context.getString(R.string.workflow_progress_filtering)
                                    val elapsedMs = SystemClock.elapsedRealtime() - startedAt
                                    lastInferenceTimeMs = elapsedMs
                                    val cleanResult = result.filterPromptNoiseTags()
                                    tags = cleanResult
                                    imageScore = scoreImage(bmp, cleanResult)
                                    realProgress = 0.99f
                                    lastRealProgressUpdateMs = System.currentTimeMillis()
                                    val savedImagePath = withContext(Dispatchers.IO) {
                                        saveHistoryImage(context, bmp)
                                    }
                                    analysisStats = recordAnalysis(context, elapsedMs)
                                    if (experienceEnabled) {
                                        experienceState = recordExperience(context)
                                    }
                                    historyRecords = saveTagRecord(context, KEY_HISTORY_TAG_RECORDS, cleanResult.take(safePromptTagLimit).toTagText(), savedImagePath)
                                } catch (e: CancellationException) {
                                    android.util.Log.i("TaggerScreen", "识别已取消")
                                    throw e
                                } catch (e: Exception) {
                                    android.util.Log.e("TaggerScreen", "识别失败", e)
                                    android.widget.Toast.makeText(context, context.getString(R.string.recognition_failed, e.message ?: ""), android.widget.Toast.LENGTH_SHORT).show()
                                } finally {
                                    inferenceProgressTargetPercent = 100f
                                    InferenceForegroundService.stop(context)
                                    delay(360L)
                                    isRunning = false
                                    inferenceJob = null
                                }
                            }
                        }
                    ) {
                        Text(
                            if (isRunning) stringResource(R.string.running) else stringResource(R.string.run_tagging),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isRunning,
                enter = fadeIn(spring(stiffness = 400f, dampingRatio = 0.7f)) +
                        expandVertically(spring(stiffness = 380f, dampingRatio = 0.65f)),
                exit = fadeOut(spring(stiffness = 500f, dampingRatio = 0.9f)) +
                        shrinkVertically(spring(stiffness = 400f, dampingRatio = 0.8f))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${(animatedInferenceProgressPercent + 0.5f).toInt().coerceIn(0, 100)}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = context.getString(R.string.model_inference_progress),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        PixelSegmentedProgressBar(
                            progress = { animatedInferenceProgressPercent.coerceIn(0f, 100f) / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        // 阶段提示文本（小字，进度条左下角）
                        Text(
                            text = inferenceProgressText.ifEmpty { context.getString(R.string.model_inference_progress) },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = imageScore != null,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(100))
            ) {
                imageScore?.let {
                    ImageScoreCard(
                        score = it
                    )
                }
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(100))
            ) {
                AutoPromptWriterCard(
                    promptDraft = autoPromptDraft,
                    isTranslating = isTranslating,
                    onTranslate = openTranslateWithNotice
                )
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(100))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    limitedTags.forEach { tag ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🏷 ${tag.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    if (isReliableColorBodyPartTag(tag.name)) {
                                        "%.2f / ≥%.2f".format(tag.score, MIN_RELIABLE_COLOR_BODY_PART_SCORE)
                                    } else {
                                        "%.2f".format(tag.score)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
            }

            // 主结果操作：紧跟标签列表，避免按钮与反向词/模型推荐卡片之间产生过大的视觉断层。
            AnimatedVisibility(visible = tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        onClick = { copyTagsToClipboard(context, limitedTags) }
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.copy_tags),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        onClick = { shareTags(context, limitedTags) }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.share_tags),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(100))
            ) {
                NegativePromptCard(
                    negativePrompt = negativePrompt
                )
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(100))
            ) {
                ModelRecommendationCard(
                    detectionResult = detectionResult,
                    recommendedModels = recommendedModels
                )
            }

            AnimatedVisibility(visible = tags.isEmpty() && !isRunning && bitmap != null) {
                Text(
                    stringResource(R.string.no_tags),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val visibleTranslatedTags = translatedTags.filter { (original, _) ->
                original in currentLimitedTagNames
            }
            AnimatedVisibility(visible = tab == 0 && visibleTranslatedTags.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = themedCardColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.translation_result, translateTargetLang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            visibleTranslatedTags.forEach { (original, translated) ->
                                Text(
                                    "$original → $translated",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (tab == 1) {
            // 判断模型是否实际安装：有可用 tagger 模型时显示名称，否则显示"未下载模型"
            val hasTaggerModel = aiModels.isNotEmpty() && engine.isReady
            val displayModelName = if (hasTaggerModel) selectedAiModelName else stringResource(R.string.ai_status_no_model)
            // 检测/分割模型：仅在实际安装时显示
            val installedDetEntry = installedDetectionEntries.firstOrNull { detModelName == "${it.repoName}.onnx" }
            val installedSegEntry = installedSegEntries.firstOrNull { segModelName == "${it.repoName}.onnx" }
            DeviceStatusCard(
                modelName = displayModelName,
                speedText = lastInferenceTimeMs?.let { formatInferenceSpeed(it) } ?: stringResource(R.string.ai_status_speed_empty),
                deviceName = getDeviceName(),
                detModelName = installedDetEntry?.name,
                segModelName = installedSegEntry?.name
            )

            TodayAnalysisCard(
                stats = analysisStats
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = themedCardColors(),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        stringResource(R.string.records_hub_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            enabled = tags.isNotEmpty(),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                confirmOrRun(isCurrentFavorite) {
                                    favoriteRecords = if (isCurrentFavorite) {
                                        deleteTagRecordByText(context, KEY_FAVORITE_TAG_RECORDS, currentTagText)
                                    } else {
                                        saveTagRecord(context, KEY_FAVORITE_TAG_RECORDS, currentTagText, bitmap?.let { saveHistoryImage(context, it) })
                                    }
                                }
                            }
                        ) {
                            Text(
                                if (isCurrentFavorite) stringResource(R.string.unfavorite_tags) else stringResource(R.string.favorite_tags),
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        OutlinedButton(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f),
                            onClick = { showParseLinkDialog = true }
                        ) {
                            Text(stringResource(R.string.parse_special_link))
                        }
                    }
                }
            }
            }

            if (tab == 2) {
                // ===== 融合模型页面：设备能力 + 下载源/导入 + 分类模型列表 =====
                // modelCatalog / deviceReport 已提升到 TaggerScreen 顶层共享
                val installedIds = remember(modelCatalog, aiModels, modelCatalogRefreshKey) {
                    val set = mutableSetOf<String>()
                    modelCatalog.forEach { entry ->
                        if (ModelRegistry.isInstalled(context, entry)) set.add(entry.id)
                    }
                    set
                }
                val categories = ModelRegistry.ModelCategory.values()
                var selectedCatIndex by remember { mutableStateOf(0) }

                // ---- 设备能力概览 ----
                DeviceCapabilityCard(deviceReport)

                // ---- 下载源 + 导入（从 AiModelPage 精简提取，非重复功能）----
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = themedCardColors(),
                    modifier = Modifier.fillMaxWidth().softEnter(1)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.ai_model_source_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SettingsSegmentButton(
                                value = AI_MODEL_SOURCE_HUGGING_FACE,
                                current = aiDownloadSource,
                                label = stringResource(R.string.ai_model_source_hugging_face),
                                onSelect = changeAiDownloadSource,
                                modifier = Modifier.weight(1f)
                            )
                            SettingsSegmentButton(
                                value = AI_MODEL_SOURCE_HF_MIRROR,
                                current = aiDownloadSource,
                                label = stringResource(R.string.ai_model_source_hf_mirror),
                                onSelect = changeAiDownloadSource,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        FilledTonalButton(
                            onClick = { showImportGuideDialog = true },
                            enabled = !isLoadingModel && downloadingAiModelId == null,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.ai_model_import_file), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // ---- 分类标签栏（3个分类等分，不需要滚动）----
                TabRow(
                    selectedTabIndex = selectedCatIndex,
                    modifier = Modifier.softEnter(2),
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        val pos = tabPositions[selectedCatIndex]
                        Box(
                            Modifier
                                .wrapContentSize(Alignment.BottomStart)
                                .offset(x = pos.left, y = 0.dp)
                                .width(pos.width)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        )
                    }
                ) {
                    categories.forEachIndexed { index, cat ->
                        Tab(
                            selected = selectedCatIndex == index,
                            onClick = { selectedCatIndex = index },
                            text = {
                                Text(
                                    stringResource(cat.displayNameResId),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedCatIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // ---- 统一模型卡片列表 ----
                val currentCat = categories[selectedCatIndex]
                val catModels = remember(modelCatalog, selectedCatIndex) {
                    modelCatalog.filter { it.category == currentCat }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().softEnter(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    catModels.forEach { entry ->
                        UnifiedModelCard(
                            entry = entry,
                            deviceReport = deviceReport,
                            isInstalled = entry.id in installedIds,
                            isSelected = when (entry.category) {
                                ModelRegistry.ModelCategory.TAGGER -> entry.id == selectedAiModelId || entry.repoName == selectedAiModelId
                                ModelRegistry.ModelCategory.DETECTION -> detModelName == "${entry.repoName}.onnx"
                                ModelRegistry.ModelCategory.SEGMENTATION -> segModelName == "${entry.repoName}.onnx"
                            },
                            isDownloading = entry.id == downloadingAiModelId,
                            isBuiltinInAssets = ModelRegistry.isBuiltinInAssets(context, entry),
                            downloadProgress = downloadProgress,
                            isLoadingModel = isLoadingModel,
                            onDownload = {
                                entry.toDownloadable()?.let { startAiModelDownload(it) }
                            },
                            onCancelDownload = cancelAiModelDownload,
                            onSelect = {
                                when (entry.category) {
                                    ModelRegistry.ModelCategory.TAGGER -> selectAiModelFromUi(entry.id)
                                    ModelRegistry.ModelCategory.DETECTION -> {
                                        onSelectDetModel("${entry.repoName}.onnx")
                                        onSelectDetectionModel(entry.id)
                                    }
                                    ModelRegistry.ModelCategory.SEGMENTATION -> {
                                        onSelectSegModel("${entry.repoName}.onnx")
                                    }
                                }
                            },
                            onDelete = {
                                // 删除模型文件
                                val dir = TaggerEngine.modelDirectory(context)
                                listOf(
                                    File(dir, "${entry.repoName}.onnx"),
                                    File(dir, "${entry.repoName}.csv")
                                ).forEach { it.delete() }
                                // 如果删除的是当前选中的检测模型，清除选择
                                if (entry.category == ModelRegistry.ModelCategory.DETECTION && detModelName == "${entry.repoName}.onnx") {
                                    onSelectDetModel("")
                                    onSelectDetectionModel("")
                                }
                                // 如果删除的是当前选中的分割模型，清除选择
                                if (entry.category == ModelRegistry.ModelCategory.SEGMENTATION && segModelName == "${entry.repoName}.onnx") {
                                    onSelectSegModel("")
                                }
                                onReloadAiModels()
                                // 删除后刷新模型安装状态
                                modelCatalogRefreshKey++
                            }
                        )
                    }
                }
            }

            if (tab == 3) {
                // 计算当前选中的检测/分割模型显示名称
                val selectedSegName = modelCatalog.firstOrNull {
                    it.category == ModelRegistry.ModelCategory.DETECTION && detModelName == "${it.repoName}.onnx"
                }?.name
                SettingsPage(
                    useDynamicColor = useDynamicColor,
                    themeStyle = themeStyle,
                    monetPalette = monetPalette,
                    customBackgroundImagePath = customBackgroundImagePath,
                    customBackgroundOpacity = customBackgroundOpacity,
                    customBackgroundDimAmount = customBackgroundDimAmount,
                    customBackgroundTabBarOpacity = customBackgroundTabBarOpacity,
                    heroSubtitleMode = heroSubtitleMode,
                    heroCustomSubtitle = heroCustomSubtitle,
                    heroSubtitleFontSize = heroSubtitleFontSize,
                    experienceEnabled = experienceEnabled,
                    confirmSaveDelete = confirmSaveDelete,
                    promptTagLimit = safePromptTagLimit,
                    onDynamicColorChange = onDynamicColorChange,
                    onThemeStyleChange = onThemeStyleChange,
                    onMonetPaletteChange = onMonetPaletteChange,
                    onPickCustomBackground = { pickCustomBackgroundLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onClearCustomBackground = { onCustomBackgroundImagePathChange("") },
                    onCustomBackgroundOpacityChange = onCustomBackgroundOpacityChange,
                    onCustomBackgroundDimAmountChange = onCustomBackgroundDimAmountChange,
                    onCustomBackgroundTabBarOpacityChange = onCustomBackgroundTabBarOpacityChange,
                    onHeroSubtitleModeChange = { mode ->
                        if (mode == HERO_SUBTITLE_MODE_POETRY && !heroPoetryNoticeShown && !heroPoetryRejected) {
                            showPoetryApiNotice = true
                        } else if (mode == HERO_SUBTITLE_MODE_POETRY && heroPoetryRejected) {
                            showPoetryRejectedDialog = true
                        } else {
                            onHeroSubtitleModeChange(mode)
                        }
                    },
                    onHeroCustomSubtitleChange = onHeroCustomSubtitleChange,
                    onHeroSubtitleFontSizeChange = onHeroSubtitleFontSizeChange,
                    onExperienceEnabledChange = onExperienceEnabledChange,
                    onConfirmSaveDeleteChange = onConfirmSaveDeleteChange,
                    onPromptTagLimitChange = onPromptTagLimitChange,
                    inferencePerfMode = inferencePerfMode,
                    onInferencePerfModeChange = onInferencePerfModeChange,
                    precisionMode = precisionMode,
                    onPrecisionModeChange = { newValue ->
                        if (newValue && !precisionModeNoticeShown) {
                            showPrecisionModeNotice = true
                        } else {
                            onPrecisionModeChange(newValue)
                        }
                    },
                    detReady = detReady,
                    detLoadError = detLoadError,
                    detNnapiEnabled = detNnapiEnabled,
                    onDetNnapiChange = onDetNnapiChange,
                    detConfidence = detConfidence,
                    onDetConfidenceChange = onDetConfidenceChange,
                    selectedSegModelName = selectedSegName,
                    darkModeOption = darkModeOption,
                    onDarkModeChange = onDarkModeChange,
                    languageOption = languageOption,
                    onLanguageChange = onLanguageChange,
                    onOpenFileManager = { showFileManager = true },
                    modifier = Modifier.softEnter(3)
                )
            }

                } // Column
            } // AnimatedContent

            // 模型页、设置页不显示底部图标（交流群 / 源代码 / 赞助）
            if (selectedMainTab !in setOf(2, 3)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FooterLinkButton(
                        icon = Icons.Filled.Groups,
                        label = stringResource(R.string.community_entry_short),
                        modifier = Modifier.weight(1f),
                        onClick = { showCommunityDialog = true }
                    )
                    FooterLinkButton(
                        icon = Icons.Filled.Code,
                        label = stringResource(R.string.source_code_entry),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_URL)))
                        }
                    )
                    FooterLinkButton(
                        icon = Icons.Filled.Public,
                        label = stringResource(R.string.footer_website),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL)))
                        }
                    )
                    FooterLinkButton(
                        icon = Icons.Filled.Favorite,
                        label = stringResource(R.string.footer_sponsor),
                        modifier = Modifier.weight(1f),
                        onClick = { showSponsorDialog = true }
                    )
                }
            }

        }
        }
        // Play 商店风格的弯曲变形加载动画，作为轻量浮层展示，避免重新撑开顶部空白。
        AnimatedVisibility(
            visible = isRunning,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MorphingBlobLoader(size = 28.dp)
            }
        }
    }
    }
    }
    }



@Composable
fun AppHeroHeader(
    subtitle: String,
    subtitleFontSize: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.app_hero_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = "💫",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(
                onClick = { sharePlainText(context, PROJECT_URL) },
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.welcome_dialog_share),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = subtitleFontSize.coerceIn(MIN_HERO_SUBTITLE_FONT_SIZE, MAX_HERO_SUBTITLE_FONT_SIZE).sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TopActionRow(
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier,
    barOpacity: Float = 0.92f
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TopActionButton(
            icon = Icons.Filled.History,
            label = stringResource(R.string.history_title),
            iconTint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            barOpacity = barOpacity,
            onClick = onHistoryClick
        )
        TopActionButton(
            icon = Icons.Filled.Favorite,
            label = stringResource(R.string.favorites_title),
            iconTint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            barOpacity = barOpacity,
            onClick = onFavoritesClick
        )
        TopActionButton(
            icon = Icons.Filled.Compare,
            label = stringResource(R.string.compare_title),
            iconTint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            barOpacity = barOpacity,
            onClick = onCompareClick
        )
    }
}

@Composable
private fun IosMorphingSegmentedControl(
    options: List<Pair<String, String>>,
    current: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    tabBarOpacity: Float = 0.58f
) {
    if (options.isEmpty()) return
    val selectedIndex = options.indexOfFirst { it.first == current }.coerceAtLeast(0)
    val hapticFeedback = LocalHapticFeedback.current

    // 长按放大动画
    var isLongPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isLongPressed) 1.06f else 1f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "tabBarPressScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = tabBarOpacity.coerceIn(0f, 1f)))
            .padding(5.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        isLongPressed = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = { isLongPressed = false }
                )
            }
    ) {
        val segmentWidth = maxWidth / options.size.toFloat()
        val sliderOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.toFloat(),
            animationSpec = spring(stiffness = 420f, dampingRatio = 0.78f),
            label = "segmentSliderOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = sliderOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
                    label = "segmentTextColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            isLongPressed = false
                            onSelect(option.first)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.second,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TopActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    barOpacity: Float = 0.92f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 900f, dampingRatio = 0.5f),
        label = "topActionPressScale"
    )
    Row(
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(17.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = barOpacity.coerceIn(0f, 1f)))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                shape = RoundedCornerShape(17.dp)
            )
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = iconTint.copy(alpha = if (enabled) 1f else 0.45f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.88f else 0.42f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun FooterLinkButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.55f),
        label = "footerLinkPressScale"
    )
    Column(
        modifier = modifier
            .height(64.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(21.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            softWrap = true,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TodayAnalysisCard(
    stats: AnalysisStats
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.today_analysis_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnalysisRow(stringResource(R.string.today_analysis_today), stringResource(R.string.today_analysis_count_value, stats.todayCount))
            AnalysisRow(stringResource(R.string.today_analysis_total), stringResource(R.string.today_analysis_count_value, stats.totalCount))
            AnalysisRow(
                stringResource(R.string.today_analysis_average_speed),
                stringResource(R.string.today_analysis_speed_value, stats.averageTimeMs / 1000f)
            )
        }
    }
}

@Composable
private fun AnalysisRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ExperienceCard(
    state: ExperienceState
) {
    val levelEmoji = experienceLevelEmoji(state.level)
    val progress = if (state.level >= MAX_EXPERIENCE_LEVEL) {
        1f
    } else {
        state.currentLevelExp.toFloat() / state.nextLevelExp.toFloat()
    }
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.experience_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.experience_rule),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "$levelEmoji ${stringResource(R.string.experience_level_value, state.level, MAX_EXPERIENCE_LEVEL)}",
                    modifier = Modifier.widthIn(min = 96.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (state.level >= MAX_EXPERIENCE_LEVEL) {
                        "👑 ${stringResource(R.string.experience_max_level)}"
                    } else {
                        stringResource(R.string.experience_progress_value, state.currentLevelExp, state.nextLevelExp)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.experience_next_gain, state.nextGain),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private fun experienceLevelEmoji(level: Int): String {
    return when (level.coerceIn(1, MAX_EXPERIENCE_LEVEL)) {
        1 -> "🌱"
        2 -> "🌿"
        3 -> "🍀"
        4 -> "⭐"
        5 -> "✨"
        6 -> "💎"
        7 -> "🔥"
        8 -> "⚡"
        9 -> "🏆"
        else -> "👑"
    }
}

@Composable
fun DeviceStatusCard(
    modelName: String,
    speedText: String,
    deviceName: String,
    detModelName: String? = null,
    segModelName: String? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.device_status_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AIStatusRow(stringResource(R.string.ai_status_model), modelName)
            AIStatusRow(stringResource(R.string.ai_status_speed), speedText)
            AIStatusRow(stringResource(R.string.ai_status_device), deviceName)
            if (detModelName != null) {
                AIStatusRow(stringResource(R.string.settings_det_model_status), detModelName)
            }
            if (segModelName != null) {
                AIStatusRow(stringResource(R.string.settings_seg_model_status), segModelName)
            }
        }
    }
}

@Composable
private fun AIStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

/** 设备 AI 加速能力概览卡片（从 ModelManagerScreen 移植融合） */
@Composable
private fun DeviceCapabilityCard(report: DeviceCapability.CapabilityReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.model_device_capability), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
            Text(report.socName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.device_mem_value, report.totalMemMB), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.device_cpu_cores_value, report.cpuCores, report.bigCoreCount), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { i ->
                    Icon(
                        if (i < report.performanceTier) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (i < report.performanceTier) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CPU" to report.cpuSupported, "GPU" to report.gpuSupported, "NNAPI" to report.nnapiSupported, "QNN" to report.qnnSupported).forEach { (name, supported) ->
                    val color = if (supported) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(
                            if (supported) Icons.Filled.CheckCircle else Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = color
                        )
                        Text(name, style = MaterialTheme.typography.labelSmall, color = color)
                    }
                }
            }
        }
    }
}

@Composable
fun ImageScoreCard(
    score: ImageScore
) {
    val cardAlpha = LocalCardOpacity.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = (cardAlpha + 0.25f).coerceAtMost(1f))
        ),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.image_score_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ImageScoreRow(stringResource(R.string.image_score_composition), "${score.composition}/10")
            ImageScoreRow(stringResource(R.string.image_score_quality), "${score.quality}/10")
            ImageScoreRow(stringResource(R.string.image_score_art), "${score.art}/10")
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.image_score_overall),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.image_score_overall_value, score.overall),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ImageScoreRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * 像素OS风格分段进度条。
 * 使用 Canvas 绘制圆角分段式进度，模拟 Pixel 系统的进度指示器风格，
 * 带有微妙的末端发光效果，进度值通过 spring 动画驱动呈现物理弹性感。
 */
@Composable
private fun PixelSegmentedProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    segmentCount: Int = 24,
    gapFraction: Float = 0.28f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress().coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 320f
        ),
        label = "segmentedProgress"
    )

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val totalHeight = size.height
        val cornerRadius = totalHeight / 2f
        val segmentWidth = totalWidth / segmentCount
        val gapWidth = segmentWidth * gapFraction
        val filledWidth = segmentWidth - gapWidth

        // 绘制轨道背景
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            size = Size(totalWidth, totalHeight)
        )

        // 绘制分段进度
        val filledSegments = (animatedProgress * segmentCount).toInt()
        val partialProgress = (animatedProgress * segmentCount) - filledSegments

        for (i in 0 until filledSegments) {
            val segmentStart = i * segmentWidth + gapWidth / 2f
            val alpha = 0.7f + (i.toFloat() / segmentCount) * 0.3f
            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(segmentStart, 0f),
                size = Size(filledWidth, totalHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }

        // 绘制部分填充的当前段
        if (filledSegments < segmentCount && partialProgress > 0f) {
            val segmentStart = filledSegments * segmentWidth + gapWidth / 2f
            val partialWidth = filledWidth * partialProgress
            drawRoundRect(
                color = color,
                topLeft = Offset(segmentStart, 0f),
                size = Size(partialWidth.coerceAtLeast(cornerRadius * 2f), totalHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}

@Composable
fun AutoPromptWriterCard(
    promptDraft: AutoPromptDraft,
    isTranslating: Boolean,
    onTranslate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.auto_prompt_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.auto_prompt_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    enabled = promptDraft.fullPrompt.isNotBlank() && !isTranslating,
                    shape = RoundedCornerShape(16.dp),
                    onClick = onTranslate
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.translate_tags))
                }
            }

            DynamicPromptBox(
                text = promptDraft.fullPrompt.ifBlank { stringResource(R.string.auto_prompt_empty) }
            )

            AutoPromptCategoryRow(stringResource(R.string.auto_prompt_quality), promptDraft.quality)
            AutoPromptCategoryRow(stringResource(R.string.auto_prompt_subject), promptDraft.subject)
            AutoPromptCategoryRow(stringResource(R.string.auto_prompt_appearance), promptDraft.appearance)
            AutoPromptCategoryRow(stringResource(R.string.auto_prompt_scene), promptDraft.scene)
            AutoPromptCategoryRow(stringResource(R.string.auto_prompt_action), promptDraft.action)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AutoPromptCategoryRow(
    label: String,
    values: List<String>
) {
    if (values.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            values.take(8).forEach { value ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        value,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NegativePromptCard(
    negativePrompt: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.negative_prompt_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                negativePrompt,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ModelRecommendationCard(
    detectionResult: String,
    recommendedModels: List<String>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.model_recommendation_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.detection_result_value, detectionResult),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.recommended_models_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            recommendedModels.forEach { model ->
                Text(
                    "• $model",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ParseSpecialLinkDialog(
    onDismiss: () -> Unit,
    onParse: (String) -> Unit
) {
    var linkText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = { Text(stringResource(R.string.parse_link_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.parse_link_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text(stringResource(R.string.parse_link_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onParse(linkText) }) {
                Text(stringResource(R.string.parse_link_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_close))
            }
        }
    )
}

@Composable
fun TagRecordDialog(
    title: String,
    emptyText: String,
    records: List<TagRecord>,
    onDismiss: () -> Unit,
    onCopy: (TagRecord) -> Unit,
    onShare: (TagRecord) -> Unit,
    onFavorite: (TagRecord) -> Unit,
    onBatchFavorite: (List<TagRecord>) -> Unit,
    onBatchShare: (List<TagRecord>) -> Unit,
    onUse: (TagRecord) -> Unit,
    onDelete: (TagRecord) -> Unit,
    onBatchDelete: (List<TagRecord>) -> Unit
) {
    var selectedRecord by remember { mutableStateOf<TagRecord?>(null) }
    var promptRecord by remember { mutableStateOf<TagRecord?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedRecordIds by remember { mutableStateOf<Set<Long>>(emptySet<Long>()) }
    val selectedRecords = remember(records, selectedRecordIds) {
        records.filter { it.id in selectedRecordIds }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogView = LocalView.current
        val dialogBarColor = MaterialTheme.colorScheme.background
        val dialogBarDark = dialogBarColor.isVisuallyDark()
        SideEffect {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window.statusBarColor = dialogBarColor.toArgb()
                window.navigationBarColor = dialogBarColor.toArgb()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isStatusBarContrastEnforced = false
                    window.isNavigationBarContrastEnforced = false
                }
                WindowCompat.getInsetsController(window, dialogView).apply {
                    isAppearanceLightStatusBars = !dialogBarDark
                    isAppearanceLightNavigationBars = !dialogBarDark
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .appThemedBackground()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.settings_close),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            if (selectionMode) stringResource(R.string.record_selected_count, selectedRecordIds.size) else title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            enabled = records.isNotEmpty(),
                            onClick = {
                                selectionMode = !selectionMode
                                if (!selectionMode) selectedRecordIds = emptySet<Long>()
                            },
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                if (selectionMode) Icons.Filled.Close else Icons.Filled.FilterList,
                                contentDescription = stringResource(R.string.record_batch_actions),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (records.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = if (selectionMode) 92.dp else 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            gridItems(records, key = { it.id }) { record ->
                                HistoryImageTile(
                                    record = record,
                                    selectionMode = selectionMode,
                                    selected = record.id in selectedRecordIds,
                                    onOpen = {
                                        if (selectionMode) {
                                            selectedRecordIds = if (record.id in selectedRecordIds) {
                                                selectedRecordIds - record.id
                                            } else {
                                                selectedRecordIds + record.id
                                            }
                                        } else {
                                            selectedRecord = record
                                        }
                                    },
                                    onLongPress = {
                                        selectionMode = true
                                        selectedRecordIds = selectedRecordIds + record.id
                                    }
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = selectionMode,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    RecordSelectionBar(
                        selectedCount = selectedRecordIds.size,
                        allSelected = selectedRecordIds.size == records.size && records.isNotEmpty(),
                        hasSelection = selectedRecordIds.isNotEmpty(),
                        onCancel = {
                            selectionMode = false
                            selectedRecordIds = emptySet<Long>()
                        },
                        onSelectAll = {
                            selectedRecordIds = if (selectedRecordIds.size == records.size) {
                                emptySet<Long>()
                            } else {
                                records.map { it.id }.toSet()
                            }
                        },
                        onSave = {
                            if (selectedRecords.isNotEmpty()) {
                                onBatchFavorite(selectedRecords)
                                selectionMode = false
                                selectedRecordIds = emptySet<Long>()
                            }
                        },
                        onShare = {
                            if (selectedRecords.isNotEmpty()) onBatchShare(selectedRecords)
                        },
                        onDelete = {
                            if (selectedRecords.isNotEmpty()) {
                                onBatchDelete(selectedRecords)
                                selectionMode = false
                                selectedRecordIds = emptySet<Long>()
                            }
                        }
                    )
                }
            }
        }
    }
    selectedRecord?.let { record ->
        HistoryImageDetailDialog(
            record = record,
            onDismiss = { selectedRecord = null },
            onFavorite = { onFavorite(record) },
            onShare = { onShare(record) },
            onDelete = {
                onDelete(record)
                selectedRecord = null
            },
            onPrompt = { promptRecord = record }
        )
    }
    promptRecord?.let { record ->
        PromptRecordDialog(
            record = record,
            onDismiss = { promptRecord = null }
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HistoryImageTile(
    record: TagRecord,
    selectionMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit
) {
    // 异步加载缩略图，避免在主线程解码多个 Bitmap 导致 OOM 闪退
    val previewBitmap by produceState<Bitmap?>(null, record.imagePath) {
        val path = record.imagePath
        if (path != null) {
            value = withContext(Dispatchers.IO) { loadBitmapFromPath(path, 384) }
        } else {
            value = null
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val tileScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.965f
            selected -> 0.985f
            else -> 1f
        },
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f),
        label = "historyTileScale"
    )
    val tileAlpha by animateFloatAsState(
        targetValue = if (selected) 0.88f else 1f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.7f),
        label = "historyTileAlpha"
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = tileScale
                scaleY = tileScale
                alpha = tileAlpha
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onOpen,
                onLongClick = onLongPress
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val currentPreviewBitmap = previewBitmap
            if (currentPreviewBitmap != null) {
                Image(
                    bitmap = currentPreviewBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            if (selectionMode) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .padding(0.dp)
                        .offset(x = (-10).dp, y = 10.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.90f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.80f)
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    formatRecordShortTime(record.createdAt),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }

        }
    }
}

@Composable
private fun RecordSelectionBar(
    selectedCount: Int,
    allSelected: Boolean,
    hasSelection: Boolean,
    onCancel: () -> Unit,
    onSelectAll: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onCancel, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
            }
            Text(
                stringResource(R.string.record_selected_count, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onSelectAll, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                Icon(
                    if (allSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = stringResource(R.string.record_select_all),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(enabled = hasSelection, onClick = onSave, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(R.string.record_batch_save),
                    tint = if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
            IconButton(enabled = hasSelection, onClick = onShare, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = stringResource(R.string.record_batch_share),
                    tint = if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
            IconButton(enabled = hasSelection, onClick = onDelete, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.record_batch_delete),
                    tint = if (hasSelection) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Composable
private fun HistoryImageDetailDialog(
    record: TagRecord,
    onDismiss: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onPrompt: () -> Unit
) {
    // 异步加载大图，避免在主线程解码大 Bitmap 导致 OOM 闪退
    val previewBitmap by produceState<Bitmap?>(null, record.imagePath) {
        val path = record.imagePath
        if (path != null) {
            value = withContext(Dispatchers.IO) { loadBitmapFromPath(path, 2048) }
        } else {
            value = null
        }
    }
    var imageScale by remember(record.id) { mutableStateOf(1f) }
    var imageOffset by remember(record.id) { mutableStateOf(Offset.Zero) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogView = LocalView.current
        SideEffect {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
            if (window != null) {
                window.statusBarColor = Color.Black.toArgb()
                window.navigationBarColor = Color.Black.toArgb()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isStatusBarContrastEnforced = false
                    window.isNavigationBarContrastEnforced = false
                }
                WindowCompat.getInsetsController(window, dialogView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 18.dp, top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HistoryDetailAction(stringResource(R.string.favorite_tags), onFavorite)
                HistoryDetailAction(stringResource(R.string.record_share), onShare)
                HistoryDetailAction(stringResource(R.string.record_delete), onDelete)
                HistoryDetailAction(stringResource(R.string.record_prompt), onPrompt)
            }

            if (previewBitmap != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .padding(vertical = 110.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                        .pointerInput(record.id) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val nextScale = (imageScale * zoom).coerceIn(1f, 5f)
                                imageOffset = if (nextScale <= 1.02f) {
                                    Offset.Zero
                                } else {
                                    imageOffset + pan
                                }
                                imageScale = nextScale
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .graphicsLayer {
                                scaleX = imageScale
                                scaleY = imageScale
                                translationX = imageOffset.x
                                translationY = imageOffset.y
                            }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(180.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailAction(
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.82f))
            .height(48.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
    ) {
        Text(label, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PromptRecordDialog(
    record: TagRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val positivePrompt = record.text
    val negativePrompt = generateNegativePrompt(record.text.toTags())
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = { Text(stringResource(R.string.record_prompt_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.record_positive_prompt),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    positivePrompt,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider()
                Text(
                    stringResource(R.string.record_negative_prompt),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    negativePrompt,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(stringResource(R.string.settings_close))
            }
        }
    )
}

@Composable
fun CommunityDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = { Text(stringResource(R.string.community_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(QQ_GROUP_URL)))
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.community_qq), modifier = Modifier.fillMaxWidth())
                }
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL)))
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.community_telegram), modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) }
        }
    )
}

@Composable
fun CustomBackgroundCropDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    var imageScale by remember(bitmap) { mutableStateOf(1f) }
    var imageOffset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    var cropWidthPx by remember { mutableStateOf(1f) }
    var cropHeightPx by remember { mutableStateOf(1f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                stringResource(R.string.settings_custom_background_crop_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.settings_custom_background_crop_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    cropWidthPx = with(density) { maxWidth.toPx() }
                    cropHeightPx = with(density) { maxHeight.toPx() }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(bitmap) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val nextScale = (imageScale * zoom).coerceIn(1f, 5f)
                                    imageScale = nextScale
                                    imageOffset += pan
                                }
                            }
                            .graphicsLayer {
                                scaleX = imageScale
                                scaleY = imageScale
                                translationX = imageOffset.x
                                translationY = imageOffset.y
                            }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cropped = cropBackgroundBitmap(
                        source = bitmap,
                        cropWidthPx = cropWidthPx,
                        cropHeightPx = cropHeightPx,
                        scale = imageScale,
                        offset = imageOffset
                    )
                    onConfirm(cropped)
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.settings_custom_background_crop_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ImportAiModelGuideDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.ai_model_import_guide_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.ai_model_import_guide_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.ai_model_import_guide_onnx),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.ai_model_import_guide_zip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.ai_model_import_guide_tags),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.ai_model_import_guide_no_tags),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.ai_model_import_guide_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AiModelPage(
    models: List<TaggerEngine.ModelConfig>,
    downloadableModels: List<DownloadableAiModel>,
    selectedModelId: String,
    isLoadingModel: Boolean,
    downloadingModelId: String?,
    downloadProgress: DownloadProgress?,
    downloadSource: String,
    onDownloadSourceChange: (String) -> Unit,
    onImportFile: () -> Unit,
    onDownloadModel: (DownloadableAiModel) -> Unit,
    onCancelDownload: () -> Unit,
    onSelectModel: (String) -> Unit,
    themeStyle: String,
    modifier: Modifier = Modifier
) {
    var familyFilter by remember { mutableStateOf("all") }
    var sortMode by remember { mutableStateOf("strength") }
    val filteredModels = remember(downloadableModels, familyFilter, sortMode) {
        downloadableModels
            .filter { familyFilter == "all" || it.family == familyFilter }
            .let { list ->
                if (sortMode == "speed") {
                    list.sortedByDescending { it.speedRank }
                } else {
                    list.sortedByDescending { it.strengthRank }
                }
            }
    }
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = themedCardColors(),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.ai_model_switch_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.ai_model_switch_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.ai_model_supported_models),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.42f)
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.ai_model_import_rules_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    stringResource(R.string.ai_model_import_rules_zip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.ai_model_import_rules_pairing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.ai_model_import_rules_guard),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = themedCardColors(),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.ai_model_source_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    stringResource(R.string.ai_model_source_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSegmentButton(
                        value = AI_MODEL_SOURCE_HUGGING_FACE,
                        current = downloadSource,
                        label = stringResource(R.string.ai_model_source_hugging_face),
                        onSelect = onDownloadSourceChange,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsSegmentButton(
                        value = AI_MODEL_SOURCE_HF_MIRROR,
                        current = downloadSource,
                        label = stringResource(R.string.ai_model_source_hf_mirror),
                        onSelect = onDownloadSourceChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                FilledTonalButton(
                    onClick = onImportFile,
                    enabled = !isLoadingModel && downloadingModelId == null,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.ai_model_import_file), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = themedCardColors(),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(stringResource(R.string.ai_model_filter_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSegmentButton("all", familyFilter, stringResource(R.string.ai_model_filter_all), { familyFilter = it }, Modifier.weight(1f))
                    SettingsSegmentButton("WD v3", familyFilter, stringResource(R.string.ai_model_filter_v3), { familyFilter = it }, Modifier.weight(1f))
                    SettingsSegmentButton("WD v1.4", familyFilter, stringResource(R.string.ai_model_filter_v14), { familyFilter = it }, Modifier.weight(1f))
                }
                Text(stringResource(R.string.ai_model_sort_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSegmentButton("strength", sortMode, stringResource(R.string.ai_model_sort_strength), { sortMode = it }, Modifier.weight(1f))
                    SettingsSegmentButton("speed", sortMode, stringResource(R.string.ai_model_sort_speed), { sortMode = it }, Modifier.weight(1f))
                }
            }
        }

        Text(
            stringResource(R.string.ai_model_downloadable_list),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        filteredModels.forEach { model ->
            AiModelDownloadCard(
                model = model,
                downloaded = models.any { it.id.endsWith("${model.repoName}.onnx") },
                isLoadingModel = isLoadingModel,
                isThisDownloading = downloadingModelId == model.id,
                downloadProgress = downloadProgress,
                onDownloadModel = onDownloadModel,
                onCancelDownload = onCancelDownload
            )
        }

        Text(
            stringResource(R.string.ai_model_current_list),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (models.isEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = themedCardColors(),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.ai_model_empty_hint),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        models.sortedBy { if (it.id == selectedModelId) 0 else 1 }.forEach { model ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (model.id == selectedModelId) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        }
                    )
                    .clickable(enabled = !isLoadingModel) { onSelectModel(model.id) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = model.id == selectedModelId,
                    enabled = !isLoadingModel,
                    onClick = { onSelectModel(model.id) }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            model.displayName,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        ModelTypeChip(model.supportedImageTypes)
                    }
                    Text(
                        if (model.isBuiltIn) stringResource(R.string.ai_model_builtin_badge) else model.modelFile?.name ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AiModelDownloadCard(
    model: DownloadableAiModel,
    downloaded: Boolean,
    isLoadingModel: Boolean,
    isThisDownloading: Boolean,
    downloadProgress: DownloadProgress?,
    onDownloadModel: (DownloadableAiModel) -> Unit,
    onCancelDownload: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(model.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        ModelTypeChip(model.supportedImageTypes)
                    }
                    Text(stringResource(model.descriptionResId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(model.family, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(model.sizeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(stringResource(R.string.ai_model_strength_label, model.strengthRank), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(stringResource(R.string.ai_model_speed_label, model.speedRank), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                TextButton(
                    enabled = !isLoadingModel && !isThisDownloading && !downloaded,
                    onClick = { onDownloadModel(model) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isThisDownloading) {
                        MorphingBlobLoader(size = 18.dp)
                    } else {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            isThisDownloading -> stringResource(R.string.ai_model_downloading)
                            downloaded -> stringResource(R.string.ai_model_downloaded)
                            else -> stringResource(R.string.ai_model_download)
                        },
                        maxLines = 1
                    )
                }
            }
            if (isThisDownloading && downloadProgress != null && downloadProgress.modelId == model.id) {
                LinearProgressIndicator(
                    progress = { downloadProgress.percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${downloadProgress.percent}%  ·  ${downloadProgress.phase}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancelDownload, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.ai_model_download_abandon), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

/**
 * 统一模型卡片 —— 融合原 AiModelDownloadCard 与 ModelManagerScreen.ModelCard。
 * 展示模型名称、版本、大小、推荐设备、速度/精度评级、设备兼容性、描述，
 * 并根据模型类型提供下载/选择/删除操作。
 */
@Composable
private fun UnifiedModelCard(
    entry: ModelRegistry.ModelEntry,
    deviceReport: DeviceCapability.CapabilityReport,
    isInstalled: Boolean,
    isSelected: Boolean,
    isDownloading: Boolean,
    isBuiltinInAssets: Boolean,
    downloadProgress: DownloadProgress?,
    isLoadingModel: Boolean,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val (starRating, estSpeedMs, recommendation) = remember(entry, deviceReport) {
        DeviceCapability.evaluateModel(entry, deviceReport)
    }
    val cardColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            isInstalled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200), label = "unifiedCardBg"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // 标题行：图标 + 名称 + 官方标识 + 版本
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.isOfficial) {
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        ModelRegistry.shortBadgeFor(entry),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 元信息：大小 + 推荐设备
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Text(entry.sizeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    RecommendedDeviceText(entry.recommendedDevice)
                }
            }

            // 评级：速度 + 精度
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RatingLabel(stringResource(R.string.model_speed), entry.speedRank)
                RatingLabel(stringResource(R.string.model_accuracy), entry.accuracyRank)
            }

            // 设备兼容性
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { i ->
                    Icon(
                        if (i < starRating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (i < starRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(recommendation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Text(stringResource(R.string.estimated_speed_value, estSpeedMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // 描述：优先使用本地化字符串资源，回退到 JSON description
            val localizedDesc = remember(entry.descKey) {
                val resId = context.resources.getIdentifier(entry.descKey, "string", context.packageName)
                if (resId != 0) context.getString(resId) else entry.description
            }
            Text(localizedDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // 操作按钮行
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    isDownloading -> {
                        TextButton(onClick = onCancelDownload, shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.ai_model_download_abandon), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    // ===== 分割模型 =====
                    entry.category == ModelRegistry.ModelCategory.SEGMENTATION && isInstalled -> {
                        if (isSelected) {
                            Text(
                                stringResource(R.string.ai_model_selected),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            OutlinedButton(onClick = onSelect, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                                Text(stringResource(R.string.ai_model_select), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                        if (!isBuiltinInAssets) {
                            TextButton(
                                onClick = onDelete,
                                enabled = !isLoadingModel && !isSelected,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.ai_model_delete), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                    entry.category == ModelRegistry.ModelCategory.SEGMENTATION && isBuiltinInAssets -> {
                        // 内置分割模型（yolo11n-seg.onnx 在 assets 中）
                        if (isSelected) {
                            Text(
                                stringResource(R.string.ai_model_selected),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            OutlinedButton(onClick = onSelect, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                                Text(stringResource(R.string.ai_model_select), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                stringResource(R.string.ai_model_builtin),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    entry.category == ModelRegistry.ModelCategory.SEGMENTATION -> {
                        // 未安装且非内置的分割模型：支持下载（MikeLud/ObjectDetectionYOLO11-ONNX）
                        Button(
                            onClick = onDownload,
                            enabled = !isLoadingModel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.ai_model_download), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    // ===== 检测模型 =====
                    entry.category == ModelRegistry.ModelCategory.DETECTION && isInstalled -> {
                        if (isSelected) {
                            Text(
                                stringResource(R.string.ai_model_selected),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            OutlinedButton(onClick = onSelect, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                                Text(stringResource(R.string.ai_model_select), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                        TextButton(
                            onClick = onDelete,
                            enabled = !isLoadingModel && !isSelected,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.ai_model_delete), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    entry.category == ModelRegistry.ModelCategory.DETECTION -> {
                        // 未安装的检测模型：支持下载（deepghs/yolos）
                        Button(
                            onClick = onDownload,
                            enabled = !isLoadingModel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.ai_model_download), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    // ===== 标签模型 =====
                    entry.category == ModelRegistry.ModelCategory.TAGGER && isInstalled -> {
                        if (isSelected) {
                            Text(
                                stringResource(R.string.ai_model_selected),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            OutlinedButton(onClick = onSelect, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                                Text(stringResource(R.string.ai_model_select), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                        TextButton(
                            onClick = onDelete,
                            enabled = !isLoadingModel && !isSelected,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.ai_model_delete), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    entry.category == ModelRegistry.ModelCategory.TAGGER -> {
                        // 未安装的标签模型：支持下载（SmilingWolf）
                        Button(
                            onClick = onDownload,
                            enabled = !isLoadingModel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.ai_model_download), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    // ===== 兜底 =====
                    isInstalled -> {
                        Text(
                            stringResource(R.string.ai_model_installed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onDelete,
                            enabled = !isLoadingModel,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.ai_model_delete), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onDownload,
                            enabled = !isLoadingModel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.ai_model_download), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }

            // 下载进度条
            if (isDownloading && downloadProgress != null && downloadProgress.modelId == entry.id) {
                LinearProgressIndicator(
                    progress = { downloadProgress.percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                )
                Text(
                    "${downloadProgress.percent}%  ·  ${downloadProgress.phase}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 紧凑的评级标签：名称 + 数值条 */
@Composable
private fun RatingLabel(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

/**
 * 模型适用类型标签 Chip —— 紧凑、低视觉干扰的圆角 Badge。
 * 根据模型能力显示：二次元 / 现实 / 通用 / 二次元·现实
 * 不遮挡模型名称、下载按钮或其他控件。
 */
@Composable
fun ModelTypeChip(
    supportedImageTypes: String,
    modifier: Modifier = Modifier
) {
    val (labelRes, chipColor, textColor) = when (supportedImageTypes) {
        "real" -> Triple(R.string.model_type_real, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        "general" -> Triple(R.string.model_type_general, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        "anime_real" -> Triple(R.string.model_type_anime_real, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        else -> Triple(R.string.model_type_anime, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(chipColor.copy(alpha = 0.7f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 设备推荐文本 —— 将中文 "骁龙 X GenY+" 映射为当前语言的本地化文本。
 * 支持所有语言，不硬编码品牌名（骁龙→Snapdragon）。
 */
@Composable
fun RecommendedDeviceText(
    recommendedDevice: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    val text = when (recommendedDevice) {
        "所有设备" -> stringResource(R.string.device_rec_all)
        "骁龙 7+ Gen2+" -> stringResource(R.string.device_rec_sd7_plus_gen2)
        "骁龙 8+ Gen1+" -> stringResource(R.string.device_rec_sd8_plus_gen1)
        "骁龙 8 Gen1+" -> stringResource(R.string.device_rec_sd8_gen1)
        "骁龙 8 Gen2+" -> stringResource(R.string.device_rec_sd8_gen2)
        "骁龙 8 Gen3+" -> stringResource(R.string.device_rec_sd8_gen3)
        else -> recommendedDevice
    }
    Text(text, style = style, color = color, maxLines = maxLines, overflow = overflow, modifier = modifier)
}

@Composable
fun AiModelDialog(
    models: List<TaggerEngine.ModelConfig>,
    downloadableModels: List<DownloadableAiModel>,
    selectedModelId: String,
    isLoadingModel: Boolean,
    downloadingModelId: String?,
    downloadProgress: DownloadProgress?,
    downloadSource: String,
    onDownloadSourceChange: (String) -> Unit,
    onImportFile: () -> Unit,
    onDownloadModel: (DownloadableAiModel) -> Unit,
    onCancelDownload: () -> Unit,
    onSelectModel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = { Text(stringResource(R.string.ai_model_switch_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.ai_model_switch_summary),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.ai_model_supported_models),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSegmentButton(
                        value = AI_MODEL_SOURCE_HUGGING_FACE,
                        current = downloadSource,
                        label = stringResource(R.string.ai_model_source_hugging_face),
                        onSelect = onDownloadSourceChange,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsSegmentButton(
                        value = AI_MODEL_SOURCE_HF_MIRROR,
                        current = downloadSource,
                        label = stringResource(R.string.ai_model_source_hf_mirror),
                        onSelect = onDownloadSourceChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                FilledTonalButton(
                    onClick = onImportFile,
                    enabled = !isLoadingModel && downloadingModelId == null,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.ai_model_import_file))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                Text(
                    stringResource(R.string.ai_model_downloadable_list),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                downloadableModels.forEach { model ->
                    val downloaded = models.any { it.id.endsWith("${model.repoName}.onnx") }
                    val isThisDownloading = downloadingModelId == model.id
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        model.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    ModelTypeChip(model.supportedImageTypes)
                                }
                                Text(
                                    stringResource(model.descriptionResId),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    model.sizeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(
                                enabled = !isLoadingModel && downloadingModelId == null && !downloaded,
                                onClick = { onDownloadModel(model) }
                            ) {
                                if (isThisDownloading) {
                                    MorphingBlobLoader(size = 18.dp)
                                } else {
                                    Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    when {
                                        isThisDownloading -> stringResource(R.string.ai_model_downloading)
                                        downloaded -> stringResource(R.string.ai_model_downloaded)
                                        else -> stringResource(R.string.ai_model_download)
                                    }
                                )
                            }
                        }
                        if (isThisDownloading && downloadProgress != null && downloadProgress.modelId == model.id) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { downloadProgress.percent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${downloadProgress.percent}%  ·  ${downloadProgress.phase}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = onCancelDownload,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            stringResource(R.string.ai_model_download_abandon),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                Text(
                    stringResource(R.string.ai_model_current_list),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (models.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.ai_model_empty_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (model.id == selectedModelId) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable(enabled = !isLoadingModel) { onSelectModel(model.id) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = model.id == selectedModelId,
                            enabled = !isLoadingModel,
                            onClick = { onSelectModel(model.id) }
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    model.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                ModelTypeChip(model.supportedImageTypes)
                            }
                            Text(
                                if (model.isBuiltIn) {
                                    stringResource(R.string.ai_model_builtin_badge)
                                } else {
                                    model.modelFile?.name ?: ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) }
        }
    )
}

@Composable
fun FirstLaunchFlowDialog(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onPrivacyAgree: () -> Unit,
    onComplete: () -> Unit,
    onDisagree: () -> Unit
) {
    val context = LocalContext.current
    var page by remember {
        mutableIntStateOf(
            when {
                !context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_LANGUAGE_SELECTED, false) -> 0
                !context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_PRIVACY_AGREED, false) -> 1
                else -> 2
            }
        )
    }
    // Supported app languages: follow system + Simplified Chinese + Traditional Chinese + English + Japanese + Korean + Russian.
    val languages = listOf(
        "system" to stringResource(R.string.settings_language_system),
        "zh" to stringResource(R.string.settings_language_zh),
        "zh-rTW" to stringResource(R.string.settings_language_zh_rTW),
        "en" to stringResource(R.string.settings_language_en),
        "ja" to stringResource(R.string.settings_language_ja),
        "ko" to stringResource(R.string.settings_language_ko),
        "ru" to stringResource(R.string.settings_language_ru)
    )
    val pageTitles = listOf(
        stringResource(R.string.language_select_title),
        stringResource(R.string.privacy_dialog_title),
        stringResource(R.string.welcome_dialog_title)
    )
    val pageSummaries = listOf(
        stringResource(R.string.language_select_message),
        stringResource(R.string.first_launch_privacy_summary),
        stringResource(R.string.first_launch_welcome_summary)
    )
    val accent = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.34f))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.90f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(36.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 18.dp
            ) {
                Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
                    // A bespoke large-radius header: inspired by modern cards, not copied from the reference.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Brush.linearGradient(listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )))
                    ) {
                        Box(Modifier.size(116.dp).offset((-20).dp, (-34).dp).clip(RoundedCornerShape(48.dp)).background(accent.copy(alpha = .14f)))
                        Box(Modifier.size(78.dp).align(Alignment.BottomEnd).offset(18.dp, 20.dp).clip(RoundedCornerShape(32.dp)).background(MaterialTheme.colorScheme.tertiary.copy(alpha = .14f)))
                        AnimatedContent(
                            targetState = page,
                            transitionSpec = {
                                (fadeIn(tween(240)) + slideInVertically(tween(300)) { it / 5 }) togetherWith
                                    (fadeOut(tween(160)) + slideOutVertically(tween(220)) { -it / 6 })
                            }, label = "firstLaunchHeader"
                        ) { target ->
                            Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (target) { 0 -> Icons.Filled.Language; 1 -> Icons.Filled.Lock; else -> Icons.Filled.AutoAwesome },
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(pageTitles[target], style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        repeat(3) { index ->
                            val selected = index == page
                            val width by animateDpAsState(
                                if (selected) 28.dp else 7.dp,
                                spring(stiffness = 650f, dampingRatio = 0.82f),
                                label = "launchDot"
                            )
                            Box(Modifier.padding(horizontal = 4.dp).width(width).height(7.dp).clip(RoundedCornerShape(99.dp)).background(if (selected) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = .18f)))
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = page,
                        transitionSpec = {
                            (fadeIn(tween(250)) + slideInVertically(tween(320)) { it / 8 }) togetherWith
                                (fadeOut(tween(150)) + slideOutVertically(tween(220)) { -it / 10 })
                        }, label = "firstLaunchContent"
                    ) { target ->
                        Column(
                            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(pageSummaries[target], style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            when (target) {
                                0 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                        languages.chunked(2).forEach { row ->
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                                row.forEach { (code, label) ->
                                                    val selected = code == currentLanguage
                                                    Surface(
                                                        onClick = { onLanguageChange(code) },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f),
                                                        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .14f))
                                                    ) {
                                                        Row(Modifier.padding(horizontal = 13.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                                                            Text(label, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                                            if (selected) Icon(Icons.Filled.Check, null, tint = accent, modifier = Modifier.size(19.dp))
                                                        }
                                                    }
                                                }
                                                if (row.size == 1) Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)) {
                                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                                            Text(stringResource(R.string.privacy_dialog_message), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                                            Text(stringResource(R.string.first_launch_privacy_local_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                                        }
                                    }
                                }
                                else -> {
                                    FirstLaunchFeatureCard(Icons.Filled.AutoAwesome, stringResource(R.string.first_launch_feature_local_title), stringResource(R.string.first_launch_feature_local_body))
                                    FirstLaunchFeatureCard(Icons.Filled.Verified, stringResource(R.string.first_launch_feature_open_title), stringResource(R.string.first_launch_feature_open_body))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (page > 0) {
                            OutlinedButton(onClick = { page-- }, shape = RoundedCornerShape(22.dp), modifier = Modifier.height(50.dp)) {
                                Icon(Icons.Filled.ArrowBack, null)
                            }
                        }
                        Button(
                            onClick = {
                                when (page) {
                                    0 -> {
                                        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_LANGUAGE_SELECTED, true).apply()
                                        page = 1
                                    }
                                    1 -> { onPrivacyAgree(); page = 2 }
                                    else -> onComplete()
                                }
                            },
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text(
                                when (page) {
                                    0 -> stringResource(R.string.first_launch_next)
                                    1 -> stringResource(R.string.first_launch_privacy_agree_continue)
                                    else -> stringResource(R.string.welcome_dialog_enter)
                                },
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Filled.KeyboardArrowRight, null)
                        }
                    }
                    if (page == 1) {
                        TextButton(onClick = onDisagree, modifier = Modifier.align(Alignment.End)) {
                            Text(stringResource(R.string.privacy_dialog_disagree), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FirstLaunchFeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun IntroDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = {
            Text(
                stringResource(R.string.welcome_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Text(
                stringResource(R.string.welcome_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp,
                maxLines = 12,
                overflow = TextOverflow.Ellipsis
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.welcome_dialog_enter))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                sharePlainText(context, PROJECT_URL)
                onDismiss()
            }) {
                Text(stringResource(R.string.welcome_dialog_share))
            }
        }
    )
}

@Composable
fun PrivacyDialog(onAgree: () -> Unit, onDisagree: () -> Unit) {
    AlertDialog(
        onDismissRequest = {}, // 不可点击外部关闭
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = {
            Text(
                stringResource(R.string.privacy_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Text(
                stringResource(R.string.privacy_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp
            )
        },
        confirmButton = {
            Button(onClick = onAgree, shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.privacy_dialog_agree))
            }
        },
        dismissButton = {
            TextButton(onClick = onDisagree) {
                Text(stringResource(R.string.privacy_dialog_disagree))
            }
        }
    )
}

@Composable
fun LanguageSelectDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.language_select_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.language_select_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                val languages = listOf(
                    "system" to stringResource(R.string.settings_language_system),
                    "zh" to stringResource(R.string.settings_language_zh),
                    "zh-rTW" to stringResource(R.string.settings_language_zh_rTW),
                    "en" to stringResource(R.string.settings_language_en),
                    "ja" to stringResource(R.string.settings_language_ja),
                    "ko" to stringResource(R.string.settings_language_ko),
                    "ru" to stringResource(R.string.settings_language_ru)
                )
                // 使用两列网格布局
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    languages.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (code, label) ->
                                val isSelected = code == currentLanguage
                                Surface(
                                    onClick = { onLanguageSelected(code) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.width(18.dp))
                                        }
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            // 如果是奇数项，补一个空的占位
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLanguageSelected(currentLanguage) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.language_select_confirm),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    )
}

@Composable
fun SponsorDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = { Text(stringResource(R.string.sponsor_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.sponsor_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SPONSOR_URL)))
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.sponsor_go))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) }
        }
    )
}

@Composable
fun ExperienceIntroDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        title = { Text(stringResource(R.string.experience_intro_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.experience_intro_rule),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                Text(
                    stringResource(R.string.experience_intro_emoji_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    (1..MAX_EXPERIENCE_LEVEL).joinToString("\n") { level ->
                        "${experienceLevelEmoji(level)} Lv.$level"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.experience_intro_confirm))
            }
        }
    )
}

@Composable
fun SettingsPage(
    useDynamicColor: Boolean,
    themeStyle: String,
    monetPalette: String,
    customBackgroundImagePath: String,
    customBackgroundOpacity: Float,
    customBackgroundDimAmount: Float,
    customBackgroundTabBarOpacity: Float,
    heroSubtitleMode: String,
    heroCustomSubtitle: String,
    heroSubtitleFontSize: Int,
    experienceEnabled: Boolean,
    confirmSaveDelete: Boolean,
    promptTagLimit: Int,
    onDynamicColorChange: (Boolean) -> Unit,
    onThemeStyleChange: (String) -> Unit,
    onMonetPaletteChange: (String) -> Unit,
    onPickCustomBackground: () -> Unit,
    onClearCustomBackground: () -> Unit,
    onCustomBackgroundOpacityChange: (Float) -> Unit,
    onCustomBackgroundDimAmountChange: (Float) -> Unit,
    onCustomBackgroundTabBarOpacityChange: (Float) -> Unit,
    onHeroSubtitleModeChange: (String) -> Unit,
    onHeroCustomSubtitleChange: (String) -> Unit,
    onHeroSubtitleFontSizeChange: (Int) -> Unit,
    onExperienceEnabledChange: (Boolean) -> Unit,
    onConfirmSaveDeleteChange: (Boolean) -> Unit,
    onPromptTagLimitChange: (Int) -> Unit,
    inferencePerfMode: String,
    onInferencePerfModeChange: (String) -> Unit,
    precisionMode: Boolean,
    onPrecisionModeChange: (Boolean) -> Unit,
    detReady: Boolean,
    detLoadError: String?,
    detNnapiEnabled: Boolean,
    onDetNnapiChange: (Boolean) -> Unit,
    detConfidence: Float,
    onDetConfidenceChange: (Float) -> Unit,
    selectedSegModelName: String?,
    darkModeOption: String,
    onDarkModeChange: (String) -> Unit,
    languageOption: String,
    onLanguageChange: (String) -> Unit,
    onOpenFileManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.settings_page_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AppearanceSettingsCard(
            useDynamicColor = useDynamicColor,
            themeStyle = themeStyle,
            monetPalette = monetPalette,
            customBackgroundImagePath = customBackgroundImagePath,
            customBackgroundOpacity = customBackgroundOpacity,
            customBackgroundDimAmount = customBackgroundDimAmount,
            customBackgroundTabBarOpacity = customBackgroundTabBarOpacity,
            heroSubtitleMode = heroSubtitleMode,
            heroCustomSubtitle = heroCustomSubtitle,
            heroSubtitleFontSize = heroSubtitleFontSize,
            darkModeOption = darkModeOption,
            onDynamicColorChange = onDynamicColorChange,
            onThemeStyleChange = onThemeStyleChange,
            onMonetPaletteChange = onMonetPaletteChange,
            onPickCustomBackground = onPickCustomBackground,
            onClearCustomBackground = onClearCustomBackground,
            onCustomBackgroundOpacityChange = onCustomBackgroundOpacityChange,
            onCustomBackgroundDimAmountChange = onCustomBackgroundDimAmountChange,
            onCustomBackgroundTabBarOpacityChange = onCustomBackgroundTabBarOpacityChange,
            onHeroSubtitleModeChange = onHeroSubtitleModeChange,
            onHeroCustomSubtitleChange = onHeroCustomSubtitleChange,
            onHeroSubtitleFontSizeChange = onHeroSubtitleFontSizeChange,
            onDarkModeChange = onDarkModeChange
        )
        LanguageSettingsCard(
            languageOption = languageOption,
            onLanguageChange = onLanguageChange
        )
        FeatureSettingsCard(
            experienceEnabled = experienceEnabled,
            confirmSaveDelete = confirmSaveDelete,
            promptTagLimit = promptTagLimit,
            themeStyle = themeStyle,
            onExperienceEnabledChange = onExperienceEnabledChange,
            onConfirmSaveDeleteChange = onConfirmSaveDeleteChange,
            onPromptTagLimitChange = onPromptTagLimitChange,
            inferencePerfMode = inferencePerfMode,
            onInferencePerfModeChange = onInferencePerfModeChange,
            precisionMode = precisionMode,
            onPrecisionModeChange = onPrecisionModeChange,
            detReady = detReady,
            detLoadError = detLoadError,
            detNnapiEnabled = detNnapiEnabled,
            onDetNnapiChange = onDetNnapiChange,
            detConfidence = detConfidence,
            onDetConfidenceChange = onDetConfidenceChange,
            selectedSegModelName = selectedSegModelName
        )
        FileManagerEntryCard(
            onOpenFileManager = onOpenFileManager
        )
    }
}

@Composable
private fun FileManagerEntryCard(
    onOpenFileManager: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onOpenFileManager() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.file_manager_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.file_manager_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileManagerDialog(
    onReloadAiModels: () -> Unit,
    onSelectAiModel: (String) -> Unit,
    onConfirmDelete: (() -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var modelGroups by remember { mutableStateOf(listModelGroups(context)) }
    var selectedModelId by remember {
        mutableStateOf(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SELECTED_AI_MODEL_ID, TaggerEngine.DEFAULT_MODEL_ID) ?: TaggerEngine.DEFAULT_MODEL_ID
        )
    }
    var expandedGroupId by remember { mutableStateOf<String?>(null) }
    val totalCacheSize = remember(modelGroups) { modelGroups.fold(0L) { acc, g -> acc + g.totalSizeBytes } }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Column {
                    Text(
                        stringResource(R.string.file_manager_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.file_manager_total_size, formatFileSize(totalCacheSize)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (modelGroups.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.FolderOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(
                                stringResource(R.string.file_manager_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                modelGroups.forEach { group ->
                    ModelGroupCard(
                        group = group,
                        isExpanded = expandedGroupId == group.id,
                        isSelected = group.id == selectedModelId,
                        onToggleExpand = {
                            expandedGroupId = if (expandedGroupId == group.id) null else group.id
                        },
                        onSelect = {
                            if (!group.isBuiltIn) {
                                selectedModelId = group.id
                                onSelectAiModel(group.id)
                            }
                        },
                        onDeleteFile = { fileItem ->
                            onConfirmDelete {
                                fileItem.file.delete()
                                modelGroups = listModelGroups(context)
                                onReloadAiModels()
                                // 如果删除的是当前选中模型的所有文件，切换到第一个可用模型
                                val updated = listModelGroups(context)
                                val stillExists = updated.any { it.id == selectedModelId }
                                if (!stillExists) {
                                    val first = updated.firstOrNull()
                                    if (first != null) {
                                        selectedModelId = first.id
                                        onSelectAiModel(first.id)
                                    }
                                }
                            }
                        },
                        onDeleteGroup = {
                            onConfirmDelete {
                                group.files.forEach { it.file.delete() }
                                modelGroups = listModelGroups(context)
                                onReloadAiModels()
                                if (group.id == selectedModelId) {
                                    val updated = listModelGroups(context)
                                    val first = updated.firstOrNull()
                                    if (first != null) {
                                        selectedModelId = first.id
                                        onSelectAiModel(first.id)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (modelGroups.isNotEmpty()) {
                    TextButton(onClick = {
                        onConfirmDelete {
                            modelGroups.filter { !it.isBuiltIn }.forEach { group ->
                                group.files.forEach { it.file.delete() }
                            }
                            modelGroups = listModelGroups(context)
                            onReloadAiModels()
                            val updated = listModelGroups(context)
                            selectedModelId = updated.firstOrNull()?.id ?: TaggerEngine.DEFAULT_MODEL_ID
                            onSelectAiModel(selectedModelId)
                        }
                    }) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.file_manager_clear_cache), color = MaterialTheme.colorScheme.error, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) }
            }
        }
    )
}

data class ModelFileItem(
    val displayName: String,
    val file: File,
    val sizeBytes: Long,
    val isBuiltIn: Boolean
)

data class ModelGroup(
    val id: String,
    val displayName: String,
    val files: List<ModelFileItem>,
    val totalSizeBytes: Long,
    val isBuiltIn: Boolean,
    val supportedImageTypes: String = "anime"
)

private fun listModelGroups(context: Context): List<ModelGroup> {
    val dir = TaggerEngine.modelDirectory(context)
    val allFiles = dir.listFiles()?.toList() ?: emptyList()
    // 从目录加载模型类型映射
    val catalog = ModelRegistry.loadCatalog(context)
    val typeById = catalog.associateBy { it.id }
    val typeByRepo = catalog.associateBy { it.repoName }
    // 按 nameWithoutExtension 分组（onnx + tag 标签文件同名归一组）
    val grouped: List<ModelGroup> = allFiles
        .filter { it.isFile && it.extension.lowercase() in setOf("onnx", "csv", "json", "txt") }
        .groupBy { it.nameWithoutExtension }
        .map { (name, files) ->
            val onnxFile = files.firstOrNull { it.extension.equals("onnx", ignoreCase = true) }
            val id = onnxFile?.nameWithoutExtension ?: files.first().nameWithoutExtension
            val sortedFiles = files.sortedByDescending { it.length() }
            val total: Long = sortedFiles.fold(0L) { acc, f -> acc + f.length() }
            val imgType = typeById[id]?.supportedImageTypes
                ?: typeByRepo[id]?.supportedImageTypes
                ?: "anime"
            ModelGroup(
                id = id,
                displayName = friendlyModelName(name),
                files = sortedFiles.map { f ->
                    ModelFileItem(
                        displayName = f.name,
                        file = f,
                        sizeBytes = f.length(),
                        isBuiltIn = false
                    )
                },
                totalSizeBytes = total,
                isBuiltIn = false,
                supportedImageTypes = imgType
            )
        }
        .sortedByDescending { it.totalSizeBytes }

    val builtIn = mutableListOf<ModelGroup>()
    if (TaggerEngine.hasBuiltInModelAsset(context)) {
        builtIn.add(
            ModelGroup(
                id = TaggerEngine.DEFAULT_MODEL_ID,
                displayName = context.getString(R.string.file_manager_builtin),
                files = listOf(
                    ModelFileItem(
                        displayName = "model.onnx",
                        file = File(context.filesDir, "model.onnx"),
                        sizeBytes = 0L,
                        isBuiltIn = true
                    )
                ),
                totalSizeBytes = 0L,
                isBuiltIn = true,
                supportedImageTypes = "anime"
            )
        )
    }
    return builtIn + grouped
}

@Composable
private fun ModelGroupCard(
    group: ModelGroup,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit,
    onDeleteFile: (ModelFileItem) -> Unit,
    onDeleteGroup: () -> Unit
) {
    val isBuiltIn = group.isBuiltIn
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.7f),
        label = "arrowRotation"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = !isBuiltIn) {
                onToggleExpand()
                if (!isSelected) onSelect()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    if (isBuiltIn) Icons.Filled.Lock else Icons.Filled.Folder,
                    contentDescription = null,
                    tint = if (isBuiltIn) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        group.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isBuiltIn) {
                        Text(
                            stringResource(R.string.file_count_size_value, group.files.size, formatFileSize(group.totalSizeBytes)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            stringResource(R.string.file_manager_builtin),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isSelected) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                if (!isBuiltIn) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded && !isBuiltIn) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    group.files.forEach { fileItem ->
                        FileRow(
                            fileItem = fileItem,
                            onDelete = { onDeleteFile(fileItem) }
                        )
                    }
                    TextButton(
                        onClick = onDeleteGroup,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.file_manager_delete_group), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun FileRow(
    fileItem: ModelFileItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Filled.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                fileItem.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                formatFileSize(fileItem.sizeBytes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(10.dp))) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.file_manager_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "%.2f GB".format(gb)
        mb >= 1 -> "%.1f MB".format(mb)
        kb >= 1 -> "%.1f KB".format(kb)
        else -> "$bytes B"
    }
}

private fun uriFileName(context: Context, uri: Uri): String {
    var name: String? = null
    runCatching {
        context.contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) name = c.getString(0)
        }
    }
    return name ?: uri.lastPathSegment ?: uri.toString()
}

@Composable
fun CompareModelsDialog(
    availableModels: List<TaggerEngine.ModelConfig>,
    model1Id: String?,
    model2Id: String?,
    result1: List<TaggerEngine.Tag>,
    result2: List<TaggerEngine.Tag>,
    isComparing: Boolean,
    optimized: List<TaggerEngine.Tag>,
    canRun: Boolean,
    onModel1Change: (String) -> Unit,
    onModel2Change: (String) -> Unit,
    onRun: () -> Unit,
    onOptimize: () -> Unit,
    onCopyOptimized: () -> Unit,
    onTranslateOptimized: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val model1 = availableModels.firstOrNull { it.id == model1Id }
    val model2 = availableModels.firstOrNull { it.id == model2Id }
    val names1 = result1.map { it.name }.toSet()
    val names2 = result2.map { it.name }.toSet()
    // 模型1 独有（蓝色）、模型2 独有（绿色）
    val onlyIn1 = result1.filter { it.name !in names2 }
    val onlyIn2 = result2.filter { it.name !in names1 }
    // 共有
    val common = result1.filter { it.name in names2 }
    // 模型1 缺少的（红色，即模型2有但模型1没有）
    val missingIn1 = onlyIn2
    // 模型2 缺少的（红色，即模型1有但模型2没有）
    val missingIn2 = onlyIn1

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp)
                    .heightIn(max = 680.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Compare, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Text(
                        stringResource(R.string.compare_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                if (availableModels.size < 2) {
                    Text(
                        stringResource(R.string.compare_need_two_models),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        stringResource(R.string.compare_select_two),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // 模型选择
                    ModelDropdownPicker(
                        label = stringResource(R.string.compare_model_a),
                        models = availableModels,
                        selectedId = model1Id,
                        onSelected = onModel1Change
                    )
                    ModelDropdownPicker(
                        label = stringResource(R.string.compare_model_b),
                        models = availableModels,
                        selectedId = model2Id,
                        onSelected = onModel2Change
                    )
                    Button(
                        onClick = onRun,
                        enabled = canRun && !isComparing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isComparing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.comparing))
                        } else {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.compare_run))
                        }
                    }
                    if (result1.isNotEmpty() || result2.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                        // 对比结果
                        Text(
                            stringResource(R.string.compare_results),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // 模型A 独有（蓝色）
                        CompareResultSection(
                            title = stringResource(R.string.compare_only_in_a, model1?.displayName ?: "A"),
                            tags = onlyIn1,
                            color = Color(0xFF1976D2) // 蓝色
                        )
                        // 模型B 独有（绿色）
                        CompareResultSection(
                            title = stringResource(R.string.compare_only_in_b, model2?.displayName ?: "B"),
                            tags = onlyIn2,
                            color = Color(0xFF388E3C) // 绿色
                        )
                        // 共有
                        if (common.isNotEmpty()) {
                            CompareResultSection(
                                title = stringResource(R.string.compare_common, common.size),
                                tags = common,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // 红色：缺少的提示词
                        if (missingIn1.isNotEmpty() || missingIn2.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                            Text(
                                stringResource(R.string.compare_missing),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            if (missingIn1.isNotEmpty()) {
                                Text(
                                    stringResource(R.string.compare_missing_in_a, model1?.displayName ?: "A", missingIn1.size),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    missingIn1.joinToString(", ") { it.name },
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.error,
                                    softWrap = true
                                )
                            }
                            if (missingIn2.isNotEmpty()) {
                                Text(
                                    stringResource(R.string.compare_missing_in_b, model2?.displayName ?: "B", missingIn2.size),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    missingIn2.joinToString(", ") { it.name },
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.error,
                                    softWrap = true
                                )
                            }
                        }
                        // 优化结果
                        if (optimized.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.42f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        stringResource(R.string.compare_optimized, optimized.size),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Button(
                                        onClick = onTranslateOptimized,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.translate_tags))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // 底部按钮
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result1.isNotEmpty() && result2.isNotEmpty() && optimized.isEmpty()) {
                    Button(
                        onClick = onOptimize,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.compare_optimize),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_close), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ModelDropdownPicker(
    label: String,
    models: List<TaggerEngine.ModelConfig>,
    selectedId: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = models.firstOrNull { it.id == selectedId }
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        selected?.displayName ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            models.forEach { m ->
                DropdownMenuItem(
                    text = { Text(m.displayName) },
                    onClick = {
                        onSelected(m.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CompareResultSection(
    title: String,
    tags: List<TaggerEngine.Tag>,
    color: Color
) {
    if (tags.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.07f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            softWrap = true
        )
        Text(
            tags.joinToString(", ") { it.name },
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            color = color,
            softWrap = true
        )
    }
}

@Composable
fun TranslateLanguageDialog(
    currentLang: String,
    isTranslating: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "zh" to stringResource(R.string.translate_lang_zh),
        "en" to stringResource(R.string.translate_lang_en),
        "ja" to stringResource(R.string.translate_lang_ja),
        "ko" to stringResource(R.string.translate_lang_ko),
        "ru" to stringResource(R.string.translate_lang_ru)
    )
    AlertDialog(
        onDismissRequest = { if (!isTranslating) onDismiss() },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Text(
                    stringResource(R.string.translate_select_lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isTranslating) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            stringResource(R.string.translating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    languages.forEach { (code, name) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelect(code) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentLang == code) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (currentLang == code) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Language,
                                    contentDescription = null,
                                    tint = if (currentLang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (currentLang == code) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isTranslating) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}

/**
 * 纯 API 翻译：所有标签通过 MyMemory API 翻译，不使用本地词典。
 * 每个标签都调用 API，确保翻译完整性。
 * 带有 responseStatus 检查，避免 API 配额超限时返回警告文本。
 * 每次请求间加入延迟，避免触发 API 速率限制。
 */
private fun translateTagsOnline(tags: List<String>, targetLang: String): List<Pair<String, String>> {
    val langCode = when (targetLang) {
        "zh" -> "zh-CN"
        "ja" -> "ja"
        "ko" -> "ko"
        "ru" -> "ru"
        else -> "en"
    }
    val results = mutableListOf<Pair<String, String>>()
    for ((index, tag) in tags.withIndex()) {
        // 直接调用 API 翻译，不使用本地词典
        val translated = translateSingleTag(tag, langCode)
        results.add(tag to translated)
        // 每次请求后加 300ms 延迟，避免触发 MyMemory API 速率限制
        // 最后一个标签不需要等待
        if (index < tags.size - 1) {
            try { Thread.sleep(300L) } catch (_: InterruptedException) {}
        }
    }
    return results
}
private fun translateSingleTag(tag: String, langCode: String): String {
    // 带括号的标签：如 hex_maniac_(pokemon), haruka_(blue_archive)
    val parenPattern = Regex("^(.+?)_?\\((.+)\\)$")
    val parenMatch = parenPattern.matchEntire(tag)
    if (parenMatch != null) {
        val mainPart = parenMatch.groupValues[1].replace('_', ' ').replace('-', ' ').trim()
        val parenContent = parenMatch.groupValues[2].replace('_', ' ').replace('-', ' ').trim()
        val mainTranslated = if (mainPart.isNotBlank()) callMyMemory(mainPart, langCode) else ""
        // 括号内容翻译后加短暂延迟
        if (mainPart.isNotBlank() && parenContent.isNotBlank()) {
            try { Thread.sleep(200L) } catch (_: InterruptedException) {}
        }
        val parenTranslated = if (parenContent.isNotBlank()) callMyMemory(parenContent, langCode) else ""
        val targetLang = when (langCode) {
            "zh-CN" -> "zh"
            else -> langCode
        }
        val paren = if (targetLang == "zh") "（$parenTranslated）" else "($parenTranslated)"
        return "$mainTranslated $paren".trim().ifBlank { tag.replace('_', ' ') }
    }
    // 数字 + 单词 的组合（1girl、2boys、3d）
    val numericPrefix = Regex("^(\\d+)([a-zA-Z].*)$")
    val numericMatch = numericPrefix.matchEntire(tag)
    if (numericMatch != null) {
        val number = numericMatch.groupValues[1]
        val rest = numericMatch.groupValues[2]
        val restTranslated = callMyMemory(rest, langCode)
        return number + restTranslated
    }
    // 普通标签：下划线/连字符转空格
    val cleaned = tag.replace('_', ' ').replace('-', ' ').trim()
    return callMyMemory(cleaned, langCode)
}

private fun callMyMemory(text: String, langCode: String): String {
    if (text.isBlank()) return text
    val maxRetries = 4
    var lastResult: String = text
    for (attempt in 1..maxRetries) {
        val result = callMyMemoryOnce(text, langCode)
        // 如果翻译结果和原文不同，说明翻译成功
        if (result != text && result.isNotBlank()) return result
        // 如果失败，等待后重试（递增等待：500ms, 1000ms, 1500ms）
        if (attempt < maxRetries) {
            try { Thread.sleep((500L * attempt)) } catch (_: InterruptedException) {}
        }
        lastResult = result
    }
    // 所有重试都返回原文，尝试用首字母大写格式再翻译一次
    if (lastResult == text) {
        val capitalized = text.split(' ').joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.lowercase().replaceFirstChar { c -> c.uppercaseChar() }
            } else {
                word
            }
        }
        if (capitalized != text) {
            val capResult = callMyMemoryOnce(capitalized, langCode)
            if (capResult != capitalized && capResult.isNotBlank() && capResult != text) {
                return capResult
            }
        }
    }
    return lastResult
}

private fun callMyMemoryOnce(text: String, langCode: String): String {
    if (text.isBlank()) return text
    return try {
        val encoded = java.net.URLEncoder.encode(text, "UTF-8")
        // 使用有效邮箱格式，将每日配额从 5000 提升到 50000 字符
        val url = java.net.URL("https://api.mymemory.translated.net/get?q=$encoded&langpair=en|$langCode&de=waifutagger@gmail.com")
        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("User-Agent", "Mozilla/5.0 (Android; WaifuTaggerCN)")
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) return text
            conn.inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                val json = JSONObject(response)
                // 检查 responseStatus，403 表示配额超限
                val status = json.optInt("responseStatus", 200)
                if (status == 403 || status == 429) return text
                val rawTranslated = json
                    .optJSONObject("responseData")
                    ?.optString("translatedText")
                    ?.takeIf { it.isNotBlank() }
                    ?: return text
                // 先解码 unicode 转义，再过滤警告
                // （API 可能以 \\uXXXX 格式返回警告文本，不解码就检测不到）
                val decoded = decodeUnicode(rawTranslated)
                // 过滤 MyMemory 警告信息（配额超限时 API 返回的文本）
                if (decoded.contains("MYMEMORY WARNING", ignoreCase = true) ||
                    decoded.contains("USAGE LIMIT", ignoreCase = true) ||
                    decoded.contains("INVALID", ignoreCase = true) ||
                    decoded.contains("QUERY LENGTH LIMIT", ignoreCase = true)) {
                    return text
                }
                // 过滤掉过长的翻译结果（单个标签翻译不应超过 100 字符）
                if (decoded.length > 100) return text
                decoded.takeIf { it != text } ?: text
            }
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        text
    }
}

private fun decodeUnicode(s: String): String {
    return try {
        if (!s.contains("\\u")) return s
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            if (i + 6 <= s.length && s[i] == '\\' && s[i + 1] == 'u') {
                val hex = s.substring(i + 2, i + 6)
                // 检查是否为有效的 4 位十六进制
                if (hex.all { it in "0123456789abcdefABCDEF" }) {
                    sb.append(hex.toInt(16).toChar())
                    i += 6
                } else {
                    // 不是有效的 unicode 转义，原样输出
                    sb.append(s[i])
                    i++
                }
            } else if (i + 2 <= s.length && s[i] == '\\' && s[i + 1] == 'n') {
                sb.append('\n')
                i += 2
            } else if (i + 2 <= s.length && s[i] == '\\' && s[i + 1] == 't') {
                sb.append('\t')
                i += 2
            } else if (i + 2 <= s.length && s[i] == '\\' && s[i + 1] == '"') {
                sb.append('"')
                i += 2
            } else if (i + 2 <= s.length && s[i] == '\\' && s[i + 1] == '\\') {
                sb.append('\\')
                i += 2
            } else {
                sb.append(s[i])
                i++
            }
        }
        sb.toString()
    } catch (e: Exception) {
        s
    }
}

@Composable
fun BatchConfirmDialog(
    uris: List<Uri>,
    onCancel: () -> Unit,
    onToggle: (Uri) -> Unit,
    onStart: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Column {
                    Text(
                        stringResource(R.string.batch_confirm_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        stringResource(R.string.batch_confirm_count, uris.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.batch_confirm_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uris.forEach { uri ->
                    val isSelected = uris.contains(uri)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggle(uri) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Filled.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                uriFileName(context, uri),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = onStart,
                    enabled = uris.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.batch_start, uris.size))
                }
            }
        }
    )
}

@Composable
fun BatchProgressDialog(
    total: Int,
    currentIndex: Int,
    isRunning: Boolean,
    results: List<BatchResultItem>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val completedCount = currentIndex.coerceIn(0, total)
    val progress = if (total > 0) (completedCount.toFloat() / total) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "batchProgress"
    )
    val progressPercent = ((animatedProgress * 100f) + 0.5f).toInt().coerceIn(0, 100)
    val successCount = results.count { it.success }
    val failedCount = results.count { !it.success }

    AlertDialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    Icons.Filled.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    stringResource(R.string.batch_progress_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "$progressPercent% · ${stringResource(R.string.batch_progress_count, completedCount, total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LinearProgressIndicator(
                    progress = { animatedProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
                )
                if (successCount + failedCount > 0) {
                    Text(
                        "✓ $successCount    ✗ $failedCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                results.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.success) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (item.success) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = if (item.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.fileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.success && item.tags.isNotEmpty()) {
                                    Text(
                                        item.tags.take(8).joinToString(", ") { it.name } + if (item.tags.size > 8) "…" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else if (!item.success) {
                                    Text(
                                        item.errorMessage ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isRunning) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.batch_stop), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AppearanceSettingsCard(
    useDynamicColor: Boolean,
    themeStyle: String,
    monetPalette: String,
    customBackgroundImagePath: String,
    customBackgroundOpacity: Float,
    customBackgroundDimAmount: Float,
    customBackgroundTabBarOpacity: Float,
    heroSubtitleMode: String,
    heroCustomSubtitle: String,
    heroSubtitleFontSize: Int,
    darkModeOption: String,
    onDynamicColorChange: (Boolean) -> Unit,
    onThemeStyleChange: (String) -> Unit,
    onMonetPaletteChange: (String) -> Unit,
    onPickCustomBackground: () -> Unit,
    onClearCustomBackground: () -> Unit,
    onCustomBackgroundOpacityChange: (Float) -> Unit,
    onCustomBackgroundDimAmountChange: (Float) -> Unit,
    onCustomBackgroundTabBarOpacityChange: (Float) -> Unit,
    onHeroSubtitleModeChange: (String) -> Unit,
    onHeroCustomSubtitleChange: (String) -> Unit,
    onHeroSubtitleFontSizeChange: (Int) -> Unit,
    onDarkModeChange: (String) -> Unit
) {
    val isCustomBackgroundStyle = themeStyle == THEME_STYLE_CUSTOM_BACKGROUND
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(R.string.settings_appearance_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 14.dp)
                ) {
                    Text(
                        stringResource(R.string.settings_dynamic_color_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.settings_dynamic_color_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = useDynamicColor && themeStyle == THEME_STYLE_MONET,
                    enabled = themeStyle == THEME_STYLE_MONET,
                    onCheckedChange = onDynamicColorChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.settings_theme_style),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSegmentButton(
                        value = THEME_STYLE_MONET,
                        current = themeStyle,
                        label = stringResource(R.string.settings_theme_style_monet_short),
                        onSelect = onThemeStyleChange,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsSegmentButton(
                        value = THEME_STYLE_CUSTOM_BACKGROUND,
                        current = themeStyle,
                        label = stringResource(R.string.settings_theme_style_custom_background_short),
                        onSelect = onThemeStyleChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.settings_subtitle_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.settings_subtitle_summary),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsSegmentButton(
                        value = HERO_SUBTITLE_MODE_DEFAULT,
                        current = heroSubtitleMode,
                        label = stringResource(R.string.settings_subtitle_default),
                        onSelect = onHeroSubtitleModeChange,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsSegmentButton(
                        value = HERO_SUBTITLE_MODE_POETRY,
                        current = heroSubtitleMode,
                        label = stringResource(R.string.settings_subtitle_poetry),
                        onSelect = onHeroSubtitleModeChange,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsSegmentButton(
                        value = HERO_SUBTITLE_MODE_CUSTOM,
                        current = heroSubtitleMode,
                        label = stringResource(R.string.settings_subtitle_custom),
                        onSelect = onHeroSubtitleModeChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                AnimatedVisibility(visible = heroSubtitleMode == HERO_SUBTITLE_MODE_CUSTOM) {
                    OutlinedTextField(
                        value = heroCustomSubtitle,
                        onValueChange = {
                            onHeroCustomSubtitleChange(
                                sanitizeHeroSubtitle(it).take(HERO_SUBTITLE_MAX_LENGTH)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.settings_subtitle_custom_input)) },
                        supportingText = {
                            Text(
                                stringResource(
                                    R.string.settings_subtitle_custom_counter,
                                    heroCustomSubtitle.length,
                                    HERO_SUBTITLE_MAX_LENGTH
                                )
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = heroSubtitleFontSize.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.50f),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.settings_subtitle_font_size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.settings_subtitle_font_size_value, heroSubtitleFontSize),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = heroSubtitleFontSize.toFloat(),
                    onValueChange = { onHeroSubtitleFontSizeChange(it.toInt()) },
                    valueRange = MIN_HERO_SUBTITLE_FONT_SIZE.toFloat()..MAX_HERO_SUBTITLE_FONT_SIZE.toFloat(),
                    steps = MAX_HERO_SUBTITLE_FONT_SIZE - MIN_HERO_SUBTITLE_FONT_SIZE - 1,
                    colors = themedSliderColors()
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f))

            AnimatedVisibility(visible = isCustomBackgroundStyle) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.settings_custom_background_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.settings_custom_background_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onPickCustomBackground,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_custom_background_pick), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = onClearCustomBackground,
                            enabled = customBackgroundImagePath.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(stringResource(R.string.settings_custom_background_clear), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Text(
                        stringResource(R.string.settings_custom_background_opacity, "${(customBackgroundOpacity * 100).toInt()}%"),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = customBackgroundOpacity,
                        onValueChange = onCustomBackgroundOpacityChange,
                        valueRange = 0f..1f,
                        colors = themedSliderColors()
                    )
                    Text(
                        stringResource(R.string.settings_custom_background_dim_amount, "${(customBackgroundDimAmount * 100).toInt()}%"),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        stringResource(R.string.settings_custom_background_dim_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = customBackgroundDimAmount,
                        onValueChange = onCustomBackgroundDimAmountChange,
                        valueRange = 0f..0.85f,
                        colors = themedSliderColors()
                    )
                    Text(
                        stringResource(R.string.settings_custom_background_tab_bar_opacity, "${(customBackgroundTabBarOpacity * 100).toInt()}%"),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        stringResource(R.string.settings_custom_background_tab_bar_opacity_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = customBackgroundTabBarOpacity,
                        onValueChange = onCustomBackgroundTabBarOpacityChange,
                        valueRange = 0f..1f,
                        colors = themedSliderColors()
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f))
                    Text(
                        stringResource(R.string.settings_custom_background_main_color),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.settings_custom_background_main_color_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 5
                    ) {
                        PaletteBlobOption(
                            value = MONET_PALETTE_WHITE,
                            current = monetPalette,
                            label = stringResource(R.string.settings_custom_background_main_color_white),
                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE5E5EA), Color(0xFFC7C7CC)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_BLACK,
                            current = monetPalette,
                            label = stringResource(R.string.settings_custom_background_main_color_black),
                            colors = listOf(Color(0xFF1C1C1E), Color(0xFFFFFFFF), Color(0xFF8E8E93)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_GREEN,
                            current = monetPalette,
                            label = stringResource(R.string.settings_monet_palette_green),
                            colors = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A), Color(0xFFA5D6A7)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_PURPLE,
                            current = monetPalette,
                            label = stringResource(R.string.settings_monet_palette_purple),
                            colors = listOf(Color(0xFF7C4DFF), Color(0xFFB388FF), Color(0xFFD1A3FF)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_YELLOW,
                            current = monetPalette,
                            label = stringResource(R.string.settings_monet_palette_yellow),
                            colors = listOf(Color(0xFFF9A825), Color(0xFFFFD54F), Color(0xFFFFECB3)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_PINK,
                            current = monetPalette,
                            label = stringResource(R.string.settings_monet_palette_pink),
                            colors = listOf(Color(0xFFC2185B), Color(0xFFF06292), Color(0xFFF8BBD0)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_ORANGE,
                            current = monetPalette,
                            label = stringResource(R.string.settings_custom_background_main_color_orange),
                            colors = listOf(Color(0xFFFF8A00), Color(0xFFFFA726), Color(0xFFFFCC80)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_BROWN,
                            current = monetPalette,
                            label = stringResource(R.string.settings_monet_palette_brown),
                            colors = listOf(Color(0xFF5D4037), Color(0xFF8D6E63), Color(0xFFBCAAA4)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_BLUE,
                            current = monetPalette,
                            label = stringResource(R.string.settings_monet_palette_blue),
                            colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF90CAF9)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                    }
                }
            }

            if (!isCustomBackgroundStyle) Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.settings_palette_theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.settings_palette_theme_summary),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 6
                ) {
                    PaletteBlobOption(
                        value = MONET_PALETTE_DEVICE,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_device),
                        colors = listOf(
                            Color(0xFFEA4335),
                            Color(0xFFFBBC05),
                            Color(0xFFFF8A00),
                            Color(0xFF4285F4)
                        ),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_GREEN,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_green),
                        colors = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A), Color(0xFFA5D6A7)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_BLUE,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_blue),
                        colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF90CAF9)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_PINK,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_pink),
                        colors = listOf(Color(0xFFC2185B), Color(0xFFF06292), Color(0xFFF8BBD0)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_YELLOW,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_yellow),
                        colors = listOf(Color(0xFFF9A825), Color(0xFFFFD54F), Color(0xFFFFECB3)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_PURPLE,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_purple),
                        colors = listOf(Color(0xFF7C4DFF), Color(0xFFB388FF), Color(0xFFD1A3FF)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_RAINBOW,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_rainbow),
                        colors = listOf(Color(0xFFFF8A00), Color(0xFFFFA726), Color(0xFFFFCC80), Color(0xFFFFE0B2)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_BROWN,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_brown),
                        colors = listOf(Color(0xFF5D4037), Color(0xFF8D6E63), Color(0xFFBCAAA4)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                    PaletteBlobOption(
                        value = MONET_PALETTE_BLACK,
                        current = monetPalette,
                        label = stringResource(R.string.settings_monet_palette_black),
                        colors = listOf(Color(0xFF1C1C1E), Color(0xFF3A3A3C), Color(0xFF636366)),
                        enabled = true,
                        onSelect = onMonetPaletteChange
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.settings_dark_mode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsSegmentButton("system", darkModeOption, stringResource(R.string.settings_dark_mode_system), onDarkModeChange, Modifier.weight(1f))
                    SettingsSegmentButton("light", darkModeOption, stringResource(R.string.settings_dark_mode_light), onDarkModeChange, Modifier.weight(1f))
                    SettingsSegmentButton("dark", darkModeOption, stringResource(R.string.settings_dark_mode_dark), onDarkModeChange, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun LanguageSettingsCard(
    languageOption: String,
    onLanguageChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.settings_language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val languageItems = listOf(
                "system" to stringResource(R.string.settings_language_system),
                "zh" to stringResource(R.string.settings_language_zh),
                "zh-rTW" to stringResource(R.string.settings_language_zh_rTW),
                "en" to stringResource(R.string.settings_language_en),
                "ja" to stringResource(R.string.settings_language_ja),
                "ko" to stringResource(R.string.settings_language_ko),
                "ru" to stringResource(R.string.settings_language_ru)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 3
            ) {
                languageItems.forEach { (code, label) ->
                    SettingsSegmentButton(code, languageOption, label, onLanguageChange, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeatureSettingsCard(
    experienceEnabled: Boolean,
    confirmSaveDelete: Boolean,
    promptTagLimit: Int,
    themeStyle: String,
    onExperienceEnabledChange: (Boolean) -> Unit,
    onConfirmSaveDeleteChange: (Boolean) -> Unit,
    onPromptTagLimitChange: (Int) -> Unit,
    inferencePerfMode: String,
    onInferencePerfModeChange: (String) -> Unit,
    precisionMode: Boolean,
    onPrecisionModeChange: (Boolean) -> Unit,
    detReady: Boolean,
    detLoadError: String?,
    detNnapiEnabled: Boolean,
    onDetNnapiChange: (Boolean) -> Unit,
    detConfidence: Float,
    onDetConfidenceChange: (Float) -> Unit,
    selectedSegModelName: String?
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = themedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 14.dp)
                ) {
                    Text(
                        stringResource(R.string.settings_feature_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.settings_experience_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = experienceEnabled,
                    onCheckedChange = onExperienceEnabledChange
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 14.dp)
                ) {
                    Text(
                        stringResource(R.string.settings_confirm_save_delete_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        stringResource(R.string.settings_confirm_save_delete_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = confirmSaveDelete,
                    onCheckedChange = onConfirmSaveDeleteChange
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.SettingsSuggest,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(R.string.settings_inference_perf_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                listOf(
                    PERF_MODE_POWER_SAVING to stringResource(R.string.settings_inference_perf_power_saving),
                    PERF_MODE_PERFORMANCE to stringResource(R.string.settings_inference_perf_performance),
                    PERF_MODE_AUTO to stringResource(R.string.settings_inference_perf_auto)
                ).forEach { (mode, label) ->
                    val selected = inferencePerfMode == mode
                    val subtitle = when (mode) {
                        PERF_MODE_POWER_SAVING -> stringResource(R.string.settings_inference_perf_power_saving_desc)
                        PERF_MODE_PERFORMANCE -> stringResource(R.string.settings_inference_perf_performance_desc)
                        else -> stringResource(R.string.settings_inference_perf_auto_desc)
                    }
                    val optionContainer by animateColorAsState(
                        targetValue = when {
                            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)
                        },
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "inferencePerfOptionContainer"
                    )
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = optionContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                width = if (selected) 1.dp else 0.7.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                                },
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onInferencePerfModeChange(mode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { onInferencePerfModeChange(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f))

            // ===== 精准模式：YOLO 检测 → 裁剪 → WD Tagger =====
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.AutoFixHigh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                stringResource(R.string.settings_precision_mode_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                stringResource(R.string.settings_precision_mode_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Switch(
                        checked = precisionMode,
                        onCheckedChange = onPrecisionModeChange
                    )
                }

                // 普通模式 / 精准模式 切换按钮
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        false to stringResource(R.string.settings_precision_mode_normal),
                        true to stringResource(R.string.settings_precision_mode_precise)
                    ).forEach { (mode, label) ->
                        val selected = precisionMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { onPrecisionModeChange(mode) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = FilterChipDefaults.filterChipElevation(elevation = 0.dp),
                            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                // 精准模式开启时的子选项
                AnimatedVisibility(
                    visible = precisionMode,
                    enter = fadeIn(spring(stiffness = 380f, dampingRatio = 0.7f)) +
                            expandVertically(spring(stiffness = 350f, dampingRatio = 0.7f)),
                    exit = fadeOut(spring(stiffness = 500f, dampingRatio = 0.9f)) +
                           shrinkVertically(spring(stiffness = 400f, dampingRatio = 0.8f))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!detReady) {
                        Text(
                            if (detLoadError != null) detLoadError
                            else stringResource(R.string.settings_det_loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_det_nnapi_title),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                stringResource(R.string.settings_det_nnapi_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Switch(
                            checked = detNnapiEnabled,
                            onCheckedChange = onDetNnapiChange
                        )
                    }
                    Text(
                        stringResource(R.string.settings_det_confidence_title,
                            "%.2f".format(detConfidence)),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = detConfidence,
                        onValueChange = { onDetConfidenceChange(it) },
                        valueRange = 0.1f..0.6f
                    )
                    Text(
                        stringResource(R.string.settings_det_model_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f))

            // ===== 检测/分割模型状态 =====
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(R.string.settings_model_status_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                // 检测模型状态（精准模式使用）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_det_model_status),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            selectedSegModelName
                                ?: stringResource(R.string.settings_model_not_selected),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedSegModelName != null)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 提示词数量上限已移至识别页面（开始识别按钮上方）
        }
    }
}

@Composable
private fun themedSliderColors(): SliderColors {
    return SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)
    )
}

/**
 * 返回带全局透明度的 CardColors。
 * 在自定义背景模式下，所有卡片统一使用 LocalCardOpacity 中的透明度值。
 */
@Composable
private fun themedCardColors(): CardColors {
    val alpha = LocalCardOpacity.current
    return CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
    )
}

@Composable
private fun SettingsSegmentButton(
    value: String,
    current: String,
    label: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val selected = value == current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = spring(stiffness = 800f, dampingRatio = 0.55f),
        label = "settingsSegmentPressScale"
    )
    val shape = RoundedCornerShape(22.dp)
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.86f)
        } else {
            blendColor(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface, 0.42f).copy(alpha = 0.74f)
        },
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "settingsSegmentContainer"
    )
    Box(
        modifier = modifier
            .height(46.dp)
            .alpha(if (enabled) 1f else 0.42f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(container)
            .border(
                width = 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                },
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) { onSelect(value) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PaletteBlobOption(
    value: String,
    current: String,
    label: String,
    colors: List<Color>,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    val selected = value == current
    Column(
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                } else {
                    Color.Transparent
                }
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = enabled) { onSelect(value) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.40f))
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            val primaryPreview = colors.getOrElse(0) { MaterialTheme.colorScheme.primary }
            val secondaryPreview = colors.getOrElse(1) { primaryPreview.copy(alpha = 0.78f) }
            val tertiaryPreview = colors.getOrElse(2) { secondaryPreview.copy(alpha = 0.78f) }
            Box(
                modifier = Modifier
                    .size(31.dp)
                    .offset(x = (-4).dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(primaryPreview)
            )
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .offset(x = 7.dp, y = (-5).dp)
                    .clip(CircleShape)
                    .background(secondaryPreview)
            )
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .offset(x = 7.dp, y = 7.dp)
                    .clip(CircleShape)
                    .background(tertiaryPreview)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun Modifier.appThemedBackground(): Modifier {
    val bg = MaterialTheme.colorScheme.background
    val tintedSurface = blendColor(bg, MaterialTheme.colorScheme.surfaceVariant, 0.38f)
    return this.background(
        Brush.verticalGradient(
            colors = listOf(
                tintedSurface,
                bg
            )
        )
    )
        .background(
            Brush.radialGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(360f, -120f),
                radius = 1000f
            )
        )
}

private fun blendColor(c1: Color, c2: Color, ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return Color(
        red = c1.red * (1f - r) + c2.red * r,
        green = c1.green * (1f - r) + c2.green * r,
        blue = c1.blue * (1f - r) + c2.blue * r,
        alpha = 1f
    )
}

private fun Color.isVisuallyDark(): Boolean {
    return red * 0.299f + green * 0.587f + blue * 0.114f < 0.45f
}

@Stable
@Composable
private fun Modifier.softEnter(index: Int = 0): Modifier {
    var visible by remember { mutableStateOf(false) }
    // 使用 index 作为 LaunchedEffect key，确保索引变化时动画重新触发
    LaunchedEffect(index) {
        visible = false
        delay(index * 40L)
        visible = true
    }
    // 单一 alpha 动画 + graphicsLayer translationY，更高性能
    val enterAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "softEnterAlpha"
    )
    val enterOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 14f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "softEnterOffset"
    )
    return this.graphicsLayer {
        alpha = enterAlpha
        translationY = enterOffset
    }
}

fun loadBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        // 第一步：获取图片尺寸（stream 读取后不可复用，需要重新打开）
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        // 第二步：计算采样率并重新打开 stream 解码
        // 限制最大 2048px，足以满足显示和推理需求（推理会 resize 到 448/640）
        // 4096px 的 ARGB_8888 位图占 64MB，容易触发 OOM 闪退
        options.inJustDecodeBounds = false
        val maxPx = 2048
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxPx, maxPx)
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    } catch (e: OutOfMemoryError) {
        android.util.Log.w("loadBitmap", "OOM loading uri, retrying with smaller size")
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            if (options.outWidth <= 0 || options.outHeight <= 0) return null
            options.inJustDecodeBounds = false
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, 1024, 1024)
            options.inPreferredConfig = Bitmap.Config.RGB_565
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e2: Exception) {
            android.util.Log.e("loadBitmap", "Retry also failed", e2)
            null
        }
    } catch (e: SecurityException) {
        android.util.Log.e("loadBitmap", "SecurityException: no permission to read uri", e)
        null
    } catch (e: Exception) {
        android.util.Log.e("loadBitmap", "Failed to load bitmap from uri", e)
        null
    }
}

private fun calculateInSampleSize(outWidth: Int, outHeight: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1
    if (outHeight > reqHeight || outWidth > reqWidth) {
        val halfHeight: Int = outHeight / 2
        val halfWidth: Int = outWidth / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun saveCustomBackgroundBitmap(context: Context, bitmap: Bitmap): String? {
    return try {
        val targetDir = File(context.filesDir, "custom_background").apply { mkdirs() }
        val targetFile = File(targetDir, "background_image.jpg")
        FileOutputStream(targetFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)
        }
        if (targetFile.exists() && targetFile.length() > 0L) targetFile.absolutePath else null
    } catch (e: Exception) {
        null
    }
}

fun cropBackgroundBitmap(
    source: Bitmap,
    cropWidthPx: Float,
    cropHeightPx: Float,
    scale: Float,
    offset: Offset
): Bitmap {
    val cropW = cropWidthPx.coerceAtLeast(1f)
    val cropH = cropHeightPx.coerceAtLeast(1f)
    val baseScale = min(cropW / source.width.toFloat(), cropH / source.height.toFloat())
    val totalScale = (baseScale * scale).coerceAtLeast(0.0001f)
    val drawnW = source.width * totalScale
    val drawnH = source.height * totalScale
    val left = cropW / 2f - drawnW / 2f + offset.x
    val top = cropH / 2f - drawnH / 2f + offset.y

    val srcLeft = ((-left) / totalScale).coerceIn(0f, source.width.toFloat())
    val srcTop = ((-top) / totalScale).coerceIn(0f, source.height.toFloat())
    val srcRight = ((cropW - left) / totalScale).coerceIn(0f, source.width.toFloat())
    val srcBottom = ((cropH - top) / totalScale).coerceIn(0f, source.height.toFloat())
    val srcX = srcLeft.toInt().coerceIn(0, source.width - 1)
    val srcY = srcTop.toInt().coerceIn(0, source.height - 1)
    val srcW = max(1, min(source.width - srcX, (srcRight - srcLeft).toInt().coerceAtLeast(1)))
    val srcH = max(1, min(source.height - srcY, (srcBottom - srcTop).toInt().coerceAtLeast(1)))
    return Bitmap.createBitmap(source, srcX, srcY, srcW, srcH)
}

fun importAiModelFile(context: Context, uri: Uri): String {
    return try {
        val rawName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "ai_model_file"
        val safeName = rawName
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .ifBlank { "ai_model_file" }
        val extension = safeName.substringAfterLast('.', "").lowercase()
        if (extension !in setOf("onnx", "csv", "json", "txt", "zip")) {
            return context.getString(R.string.ai_model_import_unsupported)
        }
        val targetDir = TaggerEngine.modelDirectory(context)
        if (extension == "zip") {
            var importedCount = 0
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        val entryName = entry.name.substringAfterLast('/').substringAfterLast('\\')
                        val entryExtension = entryName.substringAfterLast('.', "").lowercase()
                        if (!entry.isDirectory && entryName.isNotBlank() && entryExtension in setOf("onnx", "csv", "json", "txt")) {
                            val safeEntryName = entryName
                                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                                .trim()
                                .ifBlank { "ai_model_file.$entryExtension" }
                            FileOutputStream(File(targetDir, safeEntryName)).use { output ->
                                val buffer = ByteArray(1 shl 20)
                                while (true) {
                                    val read = zip.read(buffer)
                                    if (read == -1) break
                                    output.write(buffer, 0, read)
                                }
                            }
                            importedCount += 1
                        }
                        zip.closeEntry()
                    }
                }
            } ?: return context.getString(R.string.ai_model_import_failed)
            normalizeImportedModelTagPairs(targetDir)
            return if (importedCount > 0) {
                context.getString(R.string.ai_model_import_zip_success, importedCount)
            } else {
                context.getString(R.string.ai_model_import_zip_empty)
            }
        }
        val targetFile = File(targetDir, safeName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                val buffer = ByteArray(1 shl 20)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                }
            }
        } ?: return context.getString(R.string.ai_model_import_failed)
        normalizeImportedModelTagPairs(targetDir)
        // 检测/分割模型（YOLO 系列）不需要标签文件，只有标签模型需要
        val isDetectionOrSegModel = extension == "onnx" && isDetectionOrSegmentationModelName(safeName)
        val needsTags = extension == "onnx" && !isDetectionOrSegModel &&
            findMatchingTagFileForImportedModel(targetFile, targetDir.listFiles()?.filter { it.isFile && it.extension.lowercase() in setOf("csv", "json", "txt") }.orEmpty(), targetDir.listFiles()?.filter { it.isFile && it.extension.equals("onnx", ignoreCase = true) }.orEmpty()) == null
        if (needsTags) {
            "${context.getString(R.string.ai_model_import_success, safeName)}\n${context.getString(R.string.import_needs_tags_hint)}"
        } else {
            context.getString(R.string.ai_model_import_success, safeName)
        }
    } catch (e: Exception) {
        context.getString(R.string.ai_model_import_failed_with_reason, e.message ?: "")
    }
}

private fun normalizeImportedModelTagPairs(targetDir: File) {
    val files = targetDir.listFiles()?.filter { it.isFile }.orEmpty()
    val modelFiles = files.filter { it.extension.equals("onnx", ignoreCase = true) }
    val tagFiles = files.filter { it.extension.lowercase() in setOf("csv", "json", "txt") }
    if (modelFiles.isEmpty() || tagFiles.isEmpty()) return

    modelFiles.forEach { modelFile ->
        val alreadyPaired = tagFiles.any {
            it.nameWithoutExtension.equals(modelFile.nameWithoutExtension, ignoreCase = true)
        }
        if (alreadyPaired) return@forEach

        val bestTag = findMatchingTagFileForImportedModel(modelFile, tagFiles, modelFiles) ?: return@forEach
        val targetTag = File(targetDir, "${modelFile.nameWithoutExtension}.${bestTag.extension.lowercase()}")
        if (!targetTag.exists()) {
            runCatching { bestTag.copyTo(targetTag, overwrite = false) }
        }
    }
}

/**
 * 判断文件名是否为检测/分割模型（YOLO 系列）。
 * 这类模型不需要标签文件（CSV/JSON/TXT），只有标签模型（WD-tagger）才需要。
 */
private fun isDetectionOrSegmentationModelName(fileName: String): Boolean {
    val lower = fileName.lowercase()
    return lower.contains("yolo") || lower.contains("-seg") || lower.contains("_seg") ||
        lower.contains("yolov") || lower.contains("yolo11") || lower.contains("yolos")
}

private fun findMatchingTagFileForImportedModel(
    modelFile: File,
    tagFiles: List<File>,
    modelFiles: List<File>
): File? {
    val modelKey = normalizeModelPairingName(modelFile.nameWithoutExtension)
    tagFiles.firstOrNull {
        normalizeModelPairingName(it.nameWithoutExtension) == modelKey
    }?.let { return it }

    // 只有一个模型时，才允许 selected_tags/tags/classes/labels 这种通用标签名自动配对。
    // 多模型场景下自动共用标签表会造成输出下标错位，最终提示词乱生成。
    if (modelFiles.size == 1 && tagFiles.size == 1) return tagFiles.first()
    if (modelFiles.size == 1) {
        return tagFiles.firstOrNull {
            it.nameWithoutExtension.equals("selected_tags", ignoreCase = true) ||
                it.nameWithoutExtension.equals("tags", ignoreCase = true) ||
                it.nameWithoutExtension.equals("classes", ignoreCase = true) ||
                it.nameWithoutExtension.equals("labels", ignoreCase = true)
        }
    }
    return null
}

private fun normalizeModelPairingName(name: String): String {
    return name
        .lowercase()
        .replace(Regex("\\.(onnx|csv|json|txt)$"), "")
        .replace(Regex("[^a-z0-9]+"), "")
}

fun downloadAiModelBundle(
    context: Context,
    model: DownloadableAiModel,
    source: String,
    onProgress: (DownloadProgress) -> Unit = {},
    isCancelled: () -> Boolean = { false }
): AiModelDownloadResult {
    val targetDir = TaggerEngine.modelDirectory(context)
    val modelFile = File(targetDir, "${model.repoName}.onnx")
    val modelTemp = File(targetDir, "${model.repoName}.onnx.downloading")
    // 仅 tagger 模型需要标签文件
    val needsTagsFile = model.category == "tagger"
    // 标签文件扩展名取决于 tagFile 字段（Camie 用 .json，其他用 .csv）
    val tagFileExt = if (model.tagFile.isNotEmpty()) model.tagFile.substringAfterLast('.', "csv") else "csv"
    val tagsFile = if (needsTagsFile) File(targetDir, "${model.repoName}.$tagFileExt") else null
    val tagsTemp = if (needsTagsFile) File(targetDir, "${model.repoName}.$tagFileExt.downloading") else null
    try {
        // 断点续传：不删除已有的临时文件，只删除已完成的最终文件（防止半成品）
        // modelTemp 保留以支持续传；modelFile 删除以确保原子性
        modelFile.delete()
        tagsFile?.delete()
        // 如果临时文件已存在且大小>0，说明之前下载过，保留以支持续传
        val existingModelSize = modelTemp.length()
        val existingTagsSize = tagsTemp?.length() ?: 0L
        android.util.Log.d("AiModelDownload", "[Model Download] Model=${model.id} Source=$source Repo=${model.hfRepo.ifEmpty {"SmilingWolf/${model.repoName}"}}, File=${model.onnxFile.ifEmpty{"model.onnx"}}, ExistingPartSize=${existingModelSize}B, Resume=${existingModelSize > 0}")
        var currentSource = source
        // 内部下载函数：根据指定源构建 URL 并下载所有文件。
        // 国内源(hf-mirror.com)添加 ?download=true 参数以触发附件下载响应头。
        // 大文件会 302 重定向到 Xet CDN（us.aws.cdn.hf.co 等），这些 CDN 实际可正常访问且支持 Range 并行下载。
        fun performDownload(src: String) {
            val baseUrl = aiModelDownloadBaseUrl(src, model.repoName, model.category, model.hfRepo)
            val downloadParams = if (src == AI_MODEL_SOURCE_HF_MIRROR) "?download=true" else ""
            if (needsTagsFile) {
                // Tagger 模型：ONNX 文件 (95%) + 标签文件 (5%)
                // 支持自定义文件名（如 Camie 用 camie-tagger-v2.onnx + camie-tagger-v2-metadata.json）
                val onnxFileName = if (model.onnxFile.isNotEmpty()) model.onnxFile else "model.onnx"
                val tagFileName = if (model.tagFile.isNotEmpty()) model.tagFile else "selected_tags.csv"
                var firstFileDone = false
                val cumulativeProgress: (DownloadProgress) -> Unit = { progress ->
                    val adjustedPercent = if (!firstFileDone) {
                        // 并行下载会在 phase 后追加 "· 12线程加速" 后缀，
                        // 用 startsWith 匹配避免因后缀不同导致 firstFileDone 永远不为 true
                        if (progress.phase.startsWith(onnxFileName) && progress.percent >= 100) {
                            firstFileDone = true
                        }
                        (progress.percent * 95 / 100).coerceIn(0, 95)
                    } else {
                        95 + (progress.percent * 5 / 100).coerceIn(0, 5)
                    }
                    onProgress(progress.copy(percent = adjustedPercent))
                }
                downloadUrlToFile(context, "$baseUrl/$onnxFileName$downloadParams", modelTemp, model.id, onnxFileName, cumulativeProgress, isCancelled)
                if (isCancelled()) throw java.io.IOException("cancelled")
                downloadUrlToFile(context, "$baseUrl/$tagFileName$downloadParams", tagsTemp!!, model.id, tagFileName, cumulativeProgress, isCancelled)
                if (isCancelled()) throw java.io.IOException("cancelled")

                // Adapter-owned thresholds/preprocessing are used by TaggerEngine.
                // Auxiliary JSON/CSV files are optional and must never make an otherwise
                // valid model bundle fail to install.
            } else {
                // 检测/分割模型：仅下载 model.onnx
                // 优先使用模型条目指定的 onnxFile（如 yolov8n-seg.onnx），
                // 分割模型在 HF 仓库中文件名为 {repoName}.onnx（如 yolo11s-seg.onnx），
                // 检测模型在 deepghs/yolos 仓库中文件名为 model.onnx（在子目录下）
                val onnxFileName = when {
                    model.onnxFile.isNotEmpty() -> model.onnxFile
                    model.category == "segmentation" -> "${model.repoName}.onnx"
                    else -> "model.onnx"
                }
                downloadUrlToFile(context, "$baseUrl/$onnxFileName$downloadParams", modelTemp, model.id, onnxFileName, onProgress, isCancelled)
                if (isCancelled()) throw java.io.IOException("cancelled")
            }
        }
        try {
            performDownload(currentSource)
        } catch (e: Exception) {
            // 国内源(hf-mirror)下载失败（连接超时/拒绝等），回退到国外源(huggingface.co)重试
            // 注意：不再预阻断 Xet CDN 重定向——CDN 地址（us.aws.cdn.hf.co 等）实际可正常访问，
            // 且支持 Range 请求，支持并行下载。只有真正连接失败时才回退。
            val isConnectionError = e.message?.let { msg ->
                msg.contains("timeout", ignoreCase = true) ||
                msg.contains("refused", ignoreCase = true) ||
                msg.contains("reset", ignoreCase = true) ||
                msg.contains("Unable to resolve", ignoreCase = true) ||
                msg.contains("EOF") ||
                msg.contains("No route to host", ignoreCase = true) ||
                msg.contains("HTTP 5", ignoreCase = false) ||
                msg.contains("HTTP 401", ignoreCase = false) ||
                msg.contains("HTTP 403", ignoreCase = false) ||
                msg.contains("Access denied", ignoreCase = true) ||
                msg.contains("Gated", ignoreCase = true)
            } ?: false
            if (isConnectionError && !isCancelled()) {
                val fallbackSource = if (currentSource == AI_MODEL_SOURCE_HF_MIRROR)
                    AI_MODEL_SOURCE_HUGGING_FACE
                else
                    AI_MODEL_SOURCE_HF_MIRROR

                android.util.Log.w(
                    "AiModelDownload",
                    "下载源 $currentSource 失败，自动切换到 $fallbackSource 重试: ${e.message}"
                )
                currentSource = fallbackSource
                // 不同源可能返回不同 CDN URL，不能安全续传，清理临时分片。
                modelTemp.delete()
                tagsTemp?.delete()
                File(modelTemp.parentFile, "${modelTemp.name}.meta").delete()
                tagsTemp?.let { File(it.parentFile, "${it.name}.meta").delete() }
                performDownload(currentSource)
            } else {
                throw e
            }
        }
        // 下载完成，进入校验阶段
        onProgress(
            DownloadProgress(
                model.id,
                context.getString(R.string.ai_model_verifying_title),
                100,
                modelTemp.length() + (tagsTemp?.length() ?: 0L),
                modelTemp.length() + (tagsTemp?.length() ?: 0L),
                isVerifying = true
            )
        )
        val modelSize = modelTemp.length()
        val tagsSize = tagsTemp?.length() ?: 0L
        android.util.Log.d("AiModelDownload", "Downloaded temp files: model=${modelSize}B, tags=${tagsSize}B, dir=${targetDir.absolutePath}")
        if (modelSize == 0L || (needsTagsFile && tagsSize == 0L)) {
            modelTemp.delete()
            tagsTemp?.delete()
            return AiModelDownloadResult(false, context.getString(R.string.ai_model_download_failed))
        }
        // renameTo 在跨挂载点时会失败，改用 copyTo + delete 更可靠
        if (!moveFile(modelTemp, modelFile) || (needsTagsFile && !moveFile(tagsTemp!!, tagsFile!!))) {
            modelTemp.delete()
            tagsTemp?.delete()
            return AiModelDownloadResult(false, context.getString(R.string.ai_model_download_failed))
        }
        // 验证最终文件存在且非空
        if (!modelFile.exists() || modelFile.length() == 0L ||
            (needsTagsFile && (!tagsFile!!.exists() || tagsFile.length() == 0L))) {
            return AiModelDownloadResult(false, context.getString(R.string.ai_model_download_failed))
        }
        android.util.Log.d("AiModelDownload", "Final files OK: ${modelFile.name} (${modelFile.length()}B)${if (tagsFile != null) ", ${tagsFile.name} (${tagsFile.length()}B)" else ""}")
        // 扫描确认（仅 tagger 模型需要扫描）
        if (needsTagsFile) {
            val scanned = TaggerEngine.scanModelConfigs(context)
            android.util.Log.d("AiModelDownload", "Scan after download: ${scanned.size} models found")
            return AiModelDownloadResult(
                success = true,
                message = context.getString(R.string.ai_model_download_success, model.displayName),
                modelId = modelFile.absolutePath
            )
        } else {
            // 检测模型：直接返回成功（不自动切换）
            return AiModelDownloadResult(
                success = true,
                message = context.getString(R.string.ai_model_download_success_det, model.displayName),
                modelId = modelFile.absolutePath
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("AiModelDownload", "[Download Failed] Model=${model.id} Source=$source Reason=${e.message} ExistingPartSize=${modelTemp.length()}B")
        // 失败时保留临时文件以支持断点续传
        // 仅在用户主动取消时删除临时文件
        if (e.message == "cancelled") {
            modelTemp.delete()
            tagsTemp?.delete()
            // 清理并行下载的 meta 文件
            File(modelTemp.parentFile, "${modelTemp.name}.meta").delete()
            tagsTemp?.let { File(it.parentFile, "${it.name}.meta").delete() }
            if (modelFile.exists()) modelFile.delete()
            tagsFile?.let { if (it.exists()) it.delete() }
        }
        return AiModelDownloadResult(
            success = false,
            message = if (e.message == "cancelled")
                context.getString(R.string.ai_model_download_cancelled)
            else
                context.getString(R.string.ai_model_download_failed_with_reason, e.message ?: "")
        )
    }
}

private fun moveFile(src: File, dst: File): Boolean {
    if (src.renameTo(dst)) return true
    // renameTo 失败时用复制
    return try {
        src.inputStream().use { input ->
            dst.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        src.delete()
        dst.exists() && dst.length() > 0L
    } catch (e: Exception) {
        android.util.Log.e("AiModelDownload", "moveFile failed: ${src.name} -> ${dst.name}", e)
        false
    }
}

private fun aiModelDownloadBaseUrl(source: String, repoName: String, category: String = "tagger", hfRepo: String = ""): String {
    val host = when (source) {
        AI_MODEL_SOURCE_HF_MIRROR -> "https://hf-mirror.com"
        else -> "https://huggingface.co"
    }
    return when (category) {
        // 标签模型：优先使用 hfRepo 指定的仓库，否则默认 SmilingWolf 仓库
        "tagger" -> {
            if (hfRepo.isNotEmpty()) "$host/$hfRepo/resolve/main"
            else "$host/SmilingWolf/$repoName/resolve/main"
        }
        // 检测模型：deepghs/yolos 仓库，按 repoName 子目录仅含 model.onnx（无标签文件）
        "detection" -> "$host/deepghs/yolos/resolve/main/$repoName"
        // 分割模型：优先使用 hfRepo 指定的仓库（如 yolo11 系列从 MikeLud/ObjectDetectionYOLO11-ONNX，
        // yolov8n-seg 从 mobilint/YOLOv8n-seg），否则回退到 MikeLud 仓库。
        "segmentation" -> {
            if (hfRepo.isNotEmpty()) "$host/$hfRepo/resolve/main"
            else "$host/MikeLud/ObjectDetectionYOLO11-ONNX/resolve/main"
        }
        // 兜底
        else -> "$host/deepghs/yolos/resolve/main/$repoName"
    }
}

private data class DownloadProbe(
    val url: String,
    val totalBytes: Long,
    val supportsRange: Boolean
)

private const val PARALLEL_DOWNLOAD_THRESHOLD_BYTES = 8L * 1024L * 1024L
private const val PARALLEL_DOWNLOAD_CHUNK_BYTES = 4L * 1024L * 1024L
private const val PARALLEL_DOWNLOAD_MAX_THREADS = 12

private fun downloadUrlToFile(
    context: Context,
    url: String,
    targetFile: File,
    modelId: String,
    phase: String,
    onProgress: (DownloadProgress) -> Unit,
    isCancelled: () -> Boolean = { false }
) {
    val probe = probeDownloadTarget(url, isCancelled)
    // 使用 probe 解析后的 CDN 直链，避免每个并行 chunk 都重新跟随重定向
    val resolvedUrl = probe.url
    // 降低并行下载门槛：8MB 以上且是 .onnx 文件即启用并行下载。
    // 如果 probe 失败（totalBytes < 0 或 supportsRange=false），
    // 仍然尝试并行下载 — 服务器很可能支持 Range，只是探测请求出了问题。
    val shouldUseParallel = phase.endsWith(".onnx") &&
        (probe.totalBytes >= PARALLEL_DOWNLOAD_THRESHOLD_BYTES ||
         (probe.totalBytes < 0L && probe.supportsRange))
    android.util.Log.d("AiModelDownload", "downloadUrlToFile: phase=$phase supportsRange=${probe.supportsRange} totalBytes=${probe.totalBytes} parallel=$shouldUseParallel resolvedUrl=$resolvedUrl")
    if (shouldUseParallel) {
        try {
            parallelDownloadUrlToFile(
                url = resolvedUrl,
                targetFile = targetFile,
                modelId = modelId,
                phase = "$phase · ${context.getString(R.string.parallel_download_phase_suffix, PARALLEL_DOWNLOAD_MAX_THREADS)}",
                totalLen = if (probe.totalBytes > 0L) probe.totalBytes else 0L,
                onProgress = onProgress,
                isCancelled = isCancelled
            )
        } catch (e: Exception) {
            android.util.Log.w("AiModelDownload", "并行下载失败，回退到顺序下载: ${e.message}")
            // 并行下载失败时，预分配文件中的数据可能不连续（某些 chunk 完成而其他未完成）。
            // 直接截断到"已下载字节数"会导致文件中间有空洞，顺序下载续传时数据损坏。
            // 正确做法：删除预分配文件和 meta 文件，从零开始顺序下载。
            // 顺序下载自带 3 次重试和 Range 断点续传，可靠性有保障。
            val metaFile = File(targetFile.parentFile, "${targetFile.name}.meta")
            metaFile.delete()
            targetFile.delete()
            android.util.Log.i("AiModelDownload", "并行下载失败，清理预分配文件，从零开始顺序下载")
            sequentialDownloadUrlToFile(
                url = resolvedUrl,
                targetFile = targetFile,
                modelId = modelId,
                phase = phase,
                knownTotalLen = probe.totalBytes,
                onProgress = onProgress,
                isCancelled = isCancelled
            )
        }
    } else {
        sequentialDownloadUrlToFile(
            url = resolvedUrl,
            targetFile = targetFile,
            modelId = modelId,
            phase = phase,
            knownTotalLen = probe.totalBytes,
            onProgress = onProgress,
            isCancelled = isCancelled
        )
    }
}

private fun sequentialDownloadUrlToFile(
    url: String,
    targetFile: File,
    modelId: String,
    phase: String,
    knownTotalLen: Long,
    onProgress: (DownloadProgress) -> Unit,
    isCancelled: () -> Boolean = { false }
) {
    val maxRetries = 3
    var lastError: Exception? = null
    for (attempt in 1..maxRetries) {
        try {
            // 断点续传：传入已下载的字节数，从断点继续
            val existingSize = if (targetFile.exists()) targetFile.length() else 0L
            sequentialDownloadUrlToFileOnce(url, targetFile, modelId, phase, knownTotalLen, onProgress, isCancelled, existingSize)
            return
        } catch (e: Exception) {
            if (e.message == "cancelled") throw e
            lastError = e
            val partSize = if (targetFile.exists()) targetFile.length() else 0L
            android.util.Log.w("AiModelDownload", "顺序下载第 $attempt 次失败: ${e.message}, 保留已下载 ${partSize}B 用于续传")
            // 不删除 targetFile，保留已下载部分用于断点续传
            if (attempt < maxRetries) {
                Thread.sleep(2000L * attempt)
            }
        }
    }
    throw lastError ?: IllegalStateException("Download failed")
}

private fun sequentialDownloadUrlToFileOnce(
    url: String,
    targetFile: File,
    modelId: String,
    phase: String,
    knownTotalLen: Long,
    onProgress: (DownloadProgress) -> Unit,
    isCancelled: () -> Boolean = { false },
    resumeFrom: Long = 0L
) {
    var currentUrl = url
    var redirectCount = 0
    val maxRedirects = 20
    var connection: HttpURLConnection? = null
    var code = 0
    try {
        // 如果有已下载的部分，发送 Range 请求进行断点续传
        val rangeHeader = if (resumeFrom > 0L) "bytes=$resumeFrom-" else null
        while (true) {
            if (isCancelled()) throw java.io.IOException("cancelled")
            connection = if (rangeHeader != null) {
                openDownloadConnection(currentUrl, rangeHeader)
            } else {
                openDownloadConnection(currentUrl)
            }
            code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                DownloadConnectionTracker.unregister(connection)
                connection.disconnect()
                connection = null
                if (location.isNullOrBlank()) {
                    throw IllegalStateException("HTTP $code (no Location)")
                }
                // 跟随重定向（包括 CDN 地址），不预阻断——CDN 实际可访问
                redirectCount++
                if (redirectCount > maxRedirects) {
                    throw IllegalStateException("Too many redirects")
                }
                currentUrl = if (location.startsWith("http")) location else {
                    val base = URL(currentUrl)
                    URL(base, location).toString()
                }
                continue
            }
            if (code == 403) {
                throw IllegalStateException("HTTP 403 (Access denied, please switch download source: HF mirror or official)")
            }
            // 如果服务器返回 200 而非 206（不支持 Range），从头开始
            if (code == 200 && rangeHeader != null) {
                android.util.Log.w("AiModelDownload", "服务器不支持 Range 续传(返回200)，从头下载")
                // 从头开始，重置 resumeFrom
                targetFile.delete()
            }
            if (code !in 200..299 && code != 206) {
                throw IllegalStateException("HTTP $code")
            }
            break
        }
        val totalLen: Long = connection?.contentLengthLong.let { len ->
            if (code == 206 && rangeHeader != null) {
                // 206 响应：Content-Length 是剩余字节数，总大小 = 已下载 + 剩余
                resumeFrom + (len ?: 0L)
            } else if (len != null && len > 0L) {
                len
            } else {
                knownTotalLen
            }
        } ?: knownTotalLen
        // 使用 RandomAccessFile 支持断点续传写入
        val startOffset = if (code == 206 && rangeHeader != null) resumeFrom else 0L
        var received = startOffset
        connection?.inputStream?.use { rawInput ->
            BufferedInputStream(rawInput, 4 * 1024 * 1024).use { input ->
            RandomAccessFile(targetFile, "rw").use { output ->
                output.seek(startOffset)
                val buffer = ByteArray(4 * 1024 * 1024)
                var lastProgressAt = 0L
                while (true) {
                    if (isCancelled()) throw java.io.IOException("cancelled")
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    received += read
                    val percent = if (totalLen > 0L) ((received * 100L) / totalLen).toInt().coerceIn(0, 100) else 0
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastProgressAt > 350L || percent >= 100) {
                        onProgress(DownloadProgress(modelId, phase, percent, received, totalLen))
                        lastProgressAt = now
                    }
                }
            }
            }
        } ?: throw IllegalStateException("No connection")
    } finally {
        connection?.let {
            DownloadConnectionTracker.unregister(it)
            it.disconnect()
        }
    }
}

private fun parallelDownloadUrlToFile(
    url: String,
    targetFile: File,
    modelId: String,
    phase: String,
    totalLen: Long,
    onProgress: (DownloadProgress) -> Unit,
    isCancelled: () -> Boolean
) {
    // 如果 probe 未能获取文件大小，无法进行并行下载，直接抛异常让调用方回退到顺序下载
    if (totalLen <= 0L) {
        throw IllegalStateException("Unknown file size, cannot use parallel download")
    }
    targetFile.parentFile?.mkdirs()
    val metaFile = File(targetFile.parentFile, "${targetFile.name}.meta")

    // 读取已完成的 chunk 索引（断点续传）
    val completedChunks = mutableSetOf<Int>()
    if (metaFile.exists()) {
        try {
            metaFile.readLines().forEach { line ->
                line.trim().toIntOrNull()?.let { completedChunks.add(it) }
            }
            android.util.Log.i("AiModelDownload", "并行下载续传：已完成 ${completedChunks.size} 个 chunk")
        } catch (e: Exception) {
            android.util.Log.w("AiModelDownload", "读取 meta 文件失败，从头开始: ${e.message}")
            completedChunks.clear()
        }
    }

    // 如果 meta 文件存在但目标文件不存在（被删除了），清除 meta 重新开始
    if (completedChunks.isNotEmpty() && !targetFile.exists()) {
        android.util.Log.w("AiModelDownload", "meta 文件存在但目标文件不存在，重新开始并行下载")
        completedChunks.clear()
        metaFile.delete()
    }

    // 预分配文件大小（如果文件不存在或大小不对）
    if (!targetFile.exists() || targetFile.length() != totalLen) {
        RandomAccessFile(targetFile, "rw").use { it.setLength(totalLen) }
    }

    val chunkCount = ((totalLen + PARALLEL_DOWNLOAD_CHUNK_BYTES - 1) / PARALLEL_DOWNLOAD_CHUNK_BYTES).toInt()
    val threadCount = min(PARALLEL_DOWNLOAD_MAX_THREADS, max(2, chunkCount))
    val executor = Executors.newFixedThreadPool(threadCount)

    // 计算已下载字节数（已完成的 chunk）
    var initialReceived = 0L
    for (i in completedChunks) {
        val start = i * PARALLEL_DOWNLOAD_CHUNK_BYTES
        val end = min(totalLen - 1, start + PARALLEL_DOWNLOAD_CHUNK_BYTES - 1)
        initialReceived += (end - start + 1)
    }
    val received = AtomicLong(initialReceived)
    val failed = AtomicBoolean(false)
    val lastProgressAt = AtomicLong(0L)
    val futures = mutableListOf<java.util.concurrent.Future<*>>()
    val metaLock = Any()

    android.util.Log.i("AiModelDownload", "并行下载: totalLen=$totalLen chunkCount=$chunkCount threadCount=$threadCount alreadyDone=${completedChunks.size} received=$initialReceived")

    try {
        for (index in 0 until chunkCount) {
            if (index in completedChunks) {
                continue // 跳过已完成的 chunk
            }
            val start = index * PARALLEL_DOWNLOAD_CHUNK_BYTES
            val end = min(totalLen - 1, start + PARALLEL_DOWNLOAD_CHUNK_BYTES - 1)
            futures += executor.submit {
                if (failed.get() || isCancelled()) throw java.io.IOException("cancelled")
                downloadRangeToFile(
                    url = url,
                    targetFile = targetFile,
                    start = start,
                    end = end,
                    received = received,
                    totalLen = totalLen,
                    modelId = modelId,
                    phase = phase,
                    onProgress = onProgress,
                    lastProgressAt = lastProgressAt,
                    isCancelled = {
                        failed.get() || isCancelled()
                    }
                )
                // chunk 完成后写入 meta 文件
                synchronized(metaLock) {
                    try {
                        metaFile.appendText("$index\n")
                    } catch (e: Exception) {
                        android.util.Log.w("AiModelDownload", "写入 meta 文件失败: ${e.message}")
                    }
                }
            }
        }
        futures.forEach { it.get() }
        onProgress(DownloadProgress(modelId, phase, 100, totalLen, totalLen))
        // 全部完成，删除 meta 文件
        metaFile.delete()
    } catch (e: Exception) {
        failed.set(true)
        futures.forEach { it.cancel(true) }
        // 不在此处删除文件和 meta 文件，保留以便下次重试时续传
        val cause = e.cause ?: e
        if (cause.message == "cancelled") throw java.io.IOException("cancelled")
        throw cause
    } finally {
        executor.shutdownNow()
        executor.awaitTermination(2, TimeUnit.SECONDS)
    }
}

private fun downloadRangeToFile(
    url: String,
    targetFile: File,
    start: Long,
    end: Long,
    received: AtomicLong,
    totalLen: Long,
    modelId: String,
    phase: String,
    onProgress: (DownloadProgress) -> Unit,
    lastProgressAt: AtomicLong,
    isCancelled: () -> Boolean
) {
    var currentUrl = url
    var redirectCount = 0
    var connection: HttpURLConnection? = null
    try {
        while (true) {
            if (isCancelled()) throw java.io.IOException("cancelled")
            connection = openDownloadConnection(currentUrl, "bytes=$start-$end")
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                DownloadConnectionTracker.unregister(connection)
                connection.disconnect()
                connection = null
                if (location.isNullOrBlank()) throw IllegalStateException("HTTP $code (no Location)")
                // 跟随重定向（包括 CDN 地址），不预阻断
                redirectCount++
                if (redirectCount > 20) throw IllegalStateException("Too many redirects")
                currentUrl = if (location.startsWith("http")) location else URL(URL(currentUrl), location).toString()
                continue
            }
            if (code == 403) {
                throw IllegalStateException("Range HTTP 403 (CDN rejected chunked download)")
            }
            if (code == 200) {
                throw IllegalStateException("Range HTTP 200 (CDN does not support Range, falling back to sequential download)")
            }
            if (code != 206) throw IllegalStateException("Range HTTP $code")
            break
        }
        connection?.inputStream?.use { rawInput ->
            BufferedInputStream(rawInput, 1024 * 1024).use { input ->
                RandomAccessFile(targetFile, "rw").use { output ->
                    output.seek(start)
                    val buffer = ByteArray(1024 * 1024)
                    while (true) {
                        if (isCancelled()) throw java.io.IOException("cancelled")
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        val done = received.addAndGet(read.toLong())
                        val percent = ((done * 100L) / totalLen).toInt().coerceIn(0, 100)
                        val now = SystemClock.elapsedRealtime()
                        val last = lastProgressAt.get()
                        if ((now - last > 300L || percent >= 100) && lastProgressAt.compareAndSet(last, now)) {
                            onProgress(DownloadProgress(modelId, phase, percent, done, totalLen))
                        }
                    }
                }
            }
        } ?: throw IllegalStateException("No connection")
    } finally {
        connection?.let {
            DownloadConnectionTracker.unregister(it)
            it.disconnect()
        }
    }
}

private fun probeDownloadTarget(url: String, isCancelled: () -> Boolean): DownloadProbe {
    var currentUrl = url
    var redirectCount = 0
    var connection: HttpURLConnection? = null
    try {
        while (true) {
            if (isCancelled()) throw java.io.IOException("cancelled")
            connection = openDownloadConnection(currentUrl, "bytes=0-0")
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                DownloadConnectionTracker.unregister(connection)
                connection.disconnect()
                connection = null
                if (location.isNullOrBlank()) throw IllegalStateException("HTTP $code (no Location)")
                // 不再预阻断 Xet CDN URL（如 us.aws.cdn.hf.co）——这些 CDN 实际可正常访问，
                // 且支持 Range 请求用于并行下载。跟随重定向到最终地址。
                redirectCount++
                if (redirectCount > 20) throw IllegalStateException("Too many redirects")
                currentUrl = if (location.startsWith("http")) location else URL(URL(currentUrl), location).toString()
                continue
            }
            // 403: 某些 CDN 不允许 Range 请求，回退到不带 Range 的普通请求
            if (code == 403) {
                DownloadConnectionTracker.unregister(connection)
                connection.disconnect()
                connection = null
                return probeWithoutRange(currentUrl, isCancelled)
            }
            if (code !in listOf(200, 206)) {
                android.util.Log.w("AiModelDownload", "probeDownloadTarget: HTTP $code, 跳过探测直接尝试下载")
                return DownloadProbe(currentUrl, -1L, false)
            }
            val contentRange = connection.getHeaderField("Content-Range")
            val totalFromRange = contentRange
                ?.substringAfterLast("/", "")
                ?.toLongOrNull()
                ?: -1L
            val supportsRange = code == 206 && totalFromRange > 0L
            val total = if (supportsRange) totalFromRange else connection.contentLengthLong.takeIf { it > 0L } ?: -1L
            connection.inputStream?.close()
            // 返回 currentUrl（已解析重定向后的最终 CDN 直链），
            // 后续下载直接使用此 URL，避免每个并行 chunk 都重新跟随重定向
            return DownloadProbe(currentUrl, total, supportsRange)
        }
    } catch (e: Exception) {
        if (e.message == "cancelled") throw e
        android.util.Log.w("AiModelDownload", "probeDownloadTarget 探测失败，回退到直接下载: ${e.message}", e)
        return DownloadProbe(currentUrl, -1L, false)
    } finally {
        connection?.let {
            DownloadConnectionTracker.unregister(it)
            it.disconnect()
        }
    }
}

/**
 * 不带 Range 头的探测请求，作为 Range 请求被 403 拒绝时的回退方案。
 */
private fun probeWithoutRange(url: String, isCancelled: () -> Boolean): DownloadProbe {
    var currentUrl = url
    var redirectCount = 0
    var connection: HttpURLConnection? = null
    try {
        while (true) {
            if (isCancelled()) throw java.io.IOException("cancelled")
            connection = openDownloadConnection(currentUrl, null)
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                DownloadConnectionTracker.unregister(connection)
                connection.disconnect()
                connection = null
                if (location.isNullOrBlank()) throw IllegalStateException("HTTP $code (no Location)")
                // 跟随重定向（包括 CDN 地址），不预阻断
                redirectCount++
                if (redirectCount > 20) throw IllegalStateException("Too many redirects")
                currentUrl = if (location.startsWith("http")) location else URL(URL(currentUrl), location).toString()
                continue
            }
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code")
            }
            val total = connection.contentLengthLong.takeIf { it > 0L } ?: -1L
            connection.inputStream?.close()
            return DownloadProbe(currentUrl, total, false)
        }
    } catch (e: Exception) {
        if (e.message == "cancelled") throw e
        android.util.Log.w("AiModelDownload", "probeWithoutRange 探测失败: ${e.message}", e)
        return DownloadProbe(currentUrl, -1L, false)
    } finally {
        connection?.let {
            DownloadConnectionTracker.unregister(it)
            it.disconnect()
        }
    }
}

/**
 * 全局连接跟踪器：记录所有活跃的下载连接。
 * 取消下载时调用 [disconnectAll] 可以立即中断所有阻塞的 input.read()，
 * 避免 waiting readTimeout（原来 300 秒）才能响应取消。
 */
object DownloadConnectionTracker {
    private val connections = java.util.concurrent.ConcurrentLinkedQueue<HttpURLConnection>()

    fun register(conn: HttpURLConnection) { connections.add(conn) }

    fun unregister(conn: HttpURLConnection) { connections.remove(conn) }

    fun disconnectAll() {
        while (true) {
            val conn = connections.poll() ?: break
            try { conn.disconnect() } catch (_: Exception) {}
        }
    }
}

/**
 * 判断 URL 是否指向 Xet CDN（cdn.hf.co / xethub.hf.co）。
 * 国内源(hf-mirror.com)下载时，服务器可能 302 重定向到这些 CDN 域名，
 * 而 us.aws.cdn.hf.co 等在国内被墙，导致下载失败。
 */
private fun isXetCdnUrl(url: String): Boolean {
    return url.contains("cdn.hf.co") || url.contains("xethub.hf.co")
}

private fun openDownloadConnection(url: String, range: String? = null): HttpURLConnection {
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        // 降低 readTimeout：取消时如果不主动断开连接，
        // input.read() 最多会阻塞 readTimeout 毫秒才能响应取消。
        // 30 秒足以覆盖正常网络波动，同时在取消时最多等待 30 秒而非 5 分钟。
        readTimeout = 30_000
        instanceFollowRedirects = false
        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
        // HuggingFace gated models (AnimeTimm DBv4) require Bearer authentication.
        HFTokenManagerHolder.instance?.authHeader()?.let { setRequestProperty("Authorization", it) }
        setRequestProperty("Accept", "*/*")
        setRequestProperty("Accept-Encoding", "identity")
        setRequestProperty("Connection", "keep-alive")
        setRequestProperty("Cache-Control", "no-cache")
        range?.let { setRequestProperty("Range", it) }
    }
    DownloadConnectionTracker.register(conn)
    return conn
}

fun formatInferenceSpeed(elapsedMs: Long): String {
    return "%.1fs".format(elapsedMs / 1000f)
}

fun getDeviceName(): String {
    val socName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else ""
    return socName.ifBlank {
        listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { Build.HARDWARE.ifBlank { "Android Device" } }
    }
}

fun detectResult(tags: List<TaggerEngine.Tag>): String {
    if (tags.isEmpty()) return "Unknown"
    val names = tags.map { it.name.lowercase() }.toSet()
    val animeSignals = setOf(
        "1girl", "1boy", "2girls", "solo", "anime", "manga", "blue_hair",
        "long_hair", "short_hair", "school_uniform", "looking_at_viewer"
    )
    return if (names.any { it in animeSignals || it.contains("hair") || it.contains("girl") || it.contains("boy") }) {
        "Anime"
    } else {
        "General"
    }
}

fun recommendModels(detectionResult: String): List<String> {
    return when (detectionResult) {
        "Anime" -> listOf("Animagine XL", "AnythingXL", "NoobAI")
        "General" -> listOf("SDXL", "RealVisXL", "Juggernaut XL")
        else -> listOf("Animagine XL", "AnythingXL", "NoobAI")
    }
}

fun generateAutoPromptDraft(tags: List<TaggerEngine.Tag>, promptTagLimit: Int = DEFAULT_PROMPT_TAG_LIMIT): AutoPromptDraft {
    if (tags.isEmpty()) {
        return AutoPromptDraft(
            quality = emptyList(),
            subject = emptyList(),
            appearance = emptyList(),
            scene = emptyList(),
            action = emptyList(),
            fullPrompt = ""
        )
    }

    val safeLimit = promptTagLimit.coerceIn(MIN_PROMPT_TAG_LIMIT, MAX_PROMPT_TAG_LIMIT)
    val tagNames = tags
        .filter { it.score >= 0.25f || it.score == 1f }
        .sortedByDescending { it.score }
        .take(safeLimit)
        .map { it.name.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    fun pretty(tag: String): String = tag
        .replace("_", " ")
        .replace("(", "")
        .replace(")", "")
        .trim()

    fun pick(limit: Int, predicate: (String) -> Boolean): List<String> {
        return tagNames
            .filter { predicate(it.lowercase()) }
            .map(::pretty)
            .distinct()
            .take(limit)
    }

    // 各分类按比例分配名额，总和不超 safeLimit
    val qualityLimit = (safeLimit * 0.15).toInt().coerceIn(2, 6)
    val subjectLimit = (safeLimit * 0.15).toInt().coerceIn(2, 8)
    val appearanceLimit = (safeLimit * 0.25).toInt().coerceIn(3, 12)
    val sceneLimit = (safeLimit * 0.20).toInt().coerceIn(2, 10)
    val actionLimit = (safeLimit * 0.15).toInt().coerceIn(2, 10)
    val extrasLimit = (safeLimit - qualityLimit - subjectLimit - appearanceLimit - sceneLimit - actionLimit).coerceAtLeast(0)

    val quality = pick(qualityLimit) {
        it.contains("masterpiece") ||
            it.contains("best_quality") ||
            it.contains("highres") ||
            it.contains("absurdres") ||
            it.contains("detailed") ||
            it.contains("beautiful")
    }

    val subject = pick(subjectLimit) {
        it in setOf("solo", "1girl", "1boy", "2girls", "2boys", "multiple_girls", "multiple_boys") ||
            it.contains("girl") ||
            it.contains("boy") ||
            it.contains("animal") ||
            it.contains("cat") ||
            it.contains("dog") ||
            it.contains("chibi")
    }

    val appearance = pick(appearanceLimit) {
        it.contains("hair") ||
            it.contains("eyes") ||
            it.contains("dress") ||
            it.contains("skirt") ||
            it.contains("shirt") ||
            it.contains("jacket") ||
            it.contains("uniform") ||
            it.contains("kimono") ||
            it.contains("ribbon") ||
            it.contains("hat") ||
            it.contains("clothes") ||
            it.contains("sleeves")
    }

    val scene = pick(sceneLimit) {
        it.contains("background") ||
            it.contains("outdoors") ||
            it.contains("indoors") ||
            it.contains("sky") ||
            it.contains("night") ||
            it.contains("day") ||
            it.contains("city") ||
            it.contains("room") ||
            it.contains("school") ||
            it.contains("forest") ||
            it.contains("beach") ||
            it.contains("scenery")
    }

    val action = pick(actionLimit) {
        it.contains("looking") ||
            it.contains("smile") ||
            it.contains("standing") ||
            it.contains("sitting") ||
            it.contains("holding") ||
            it.contains("open_mouth") ||
            it.contains("upper_body") ||
            it.contains("cowboy_shot") ||
            it.contains("portrait") ||
            it.startsWith("from_")
    }

    val used = (quality + subject + appearance + scene + action)
        .map { it.replace(" ", "_").lowercase() }
        .toSet()
    val usedCount = (quality + subject + appearance + scene + action).distinct().size
    val extras = tagNames
        .filterNot { it.lowercase() in used }
        .map(::pretty)
        .take(extrasLimit)

    val fullPrompt = (quality + subject + appearance + action + scene + extras)
        .distinct()
        .take(safeLimit)
        .joinToString(", ")

    return AutoPromptDraft(
        quality = quality,
        subject = subject,
        appearance = appearance,
        scene = scene,
        action = action,
        fullPrompt = fullPrompt
    )
}

fun generateNegativePrompt(tags: List<TaggerEngine.Tag>): String {
    val names = tags.map { it.name.lowercase() }.toSet()
    val base = mutableListOf(
        "low quality",
        "worst quality",
        "normal quality",
        "lowres",
        "bad anatomy",
        "bad hands",
        "missing fingers",
        "extra fingers",
        "deformed",
        "blurry",
        "jpeg artifacts",
        "watermark",
        "signature",
        "text"
    )
    if (names.any { it.contains("girl") || it.contains("boy") || it.contains("hair") || it == "solo" }) {
        base += listOf("extra limbs", "mutated hands", "poorly drawn face", "poorly drawn hands")
    }
    if (names.any { it.contains("animal") || it.contains("cat") || it.contains("dog") }) {
        base += listOf("mutated animal", "extra tail", "bad animal anatomy")
    }
    return base.distinct().joinToString(", ")
}

fun scoreImage(bitmap: Bitmap?, tags: List<TaggerEngine.Tag>): ImageScore {
    val names = tags.map { it.name.lowercase() }.toSet()
    val averageConfidence = tags.take(12).map { it.score }.ifEmpty { listOf(0.65f) }.average().toFloat()

    var composition = 7
    if (names.any { it == "solo" || it == "1girl" || it == "1boy" }) composition += 1
    if (names.any { it == "looking_at_viewer" || it == "upper_body" || it == "cowboy_shot" }) composition += 1
    if (names.any { it.contains("multiple") || it == "crowd" || it == "bad_composition" }) composition -= 1
    if (bitmap != null) {
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        if (ratio in 0.65f..1.8f) composition += 1
    }

    var quality = 7
    if (bitmap != null) {
        val pixels = bitmap.width.toLong() * bitmap.height.toLong()
        quality += when {
            pixels >= 2_000_000L -> 2
            pixels >= 900_000L -> 1
            pixels < 350_000L -> -2
            else -> 0
        }
    }
    if (names.any { it in setOf("lowres", "blurry", "jpeg_artifacts", "watermark") }) quality -= 2
    if (averageConfidence > 0.75f) quality += 1

    var art = 7
    if (names.any { it.contains("detailed") || it.contains("beautiful") || it.contains("masterpiece") }) art += 2
    if (names.any { it.contains("hair") || it.contains("eyes") || it.contains("dress") || it.contains("uniform") }) art += 1
    if (names.any { it in setOf("monochrome", "sketch", "simple_background") }) art -= 1
    if (averageConfidence > 0.8f) art += 1

    val compositionScore = composition.coerceIn(1, 10)
    val qualityScore = quality.coerceIn(1, 10)
    val artScore = art.coerceIn(1, 10)
    val overall = ((compositionScore * 0.3f + qualityScore * 0.35f + artScore * 0.35f) * 10).toInt().coerceIn(1, 100)

    return ImageScore(
        composition = compositionScore,
        quality = qualityScore,
        art = artScore,
        overall = overall
    )
}

fun List<TaggerEngine.Tag>.toTagText(): String {
    return joinToString(", ") { it.name }
}

fun List<TaggerEngine.Tag>.filterPromptNoiseTags(): List<TaggerEngine.Tag> {
    return filterNot { tag ->
        isReliableColorBodyPartTag(tag.name) &&
            tag.originalScore < MIN_RELIABLE_COLOR_BODY_PART_SCORE
    }
}

fun isReliableColorBodyPartTag(tagName: String): Boolean {
    val normalizedName = tagName.lowercase()
    return normalizedName in LOW_CONFIDENCE_COLOR_BODY_PART_TAGS ||
        (
            LOW_CONFIDENCE_COLOR_BODY_PART_PREFIXES.any { prefix ->
                normalizedName.startsWith("${prefix}_")
            } &&
                LOW_CONFIDENCE_COLOR_BODY_PART_SUFFIXES.any { suffix ->
                    normalizedName.endsWith("_$suffix")
                }
        )
}

fun String.toTags(): List<TaggerEngine.Tag> {
    return split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { TaggerEngine.Tag(name = it, category = 0, score = 1f) }
}

fun loadTagRecords(context: Context, key: String): List<TagRecord> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(key, "[]") ?: "[]"
    return try {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val text = item.optString("text").trim()
                if (text.isNotEmpty()) {
                    add(
                        TagRecord(
                            id = item.optLong("id", item.optLong("createdAt", System.currentTimeMillis())),
                            text = text,
                            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                            imagePath = item.optString("imagePath").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveTagRecord(context: Context, key: String, text: String, imagePath: String? = null): List<TagRecord> {
    val cleanText = text.trim()
    if (cleanText.isEmpty()) return loadTagRecords(context, key)
    val now = System.currentTimeMillis()
    val next = listOf(TagRecord(id = now, text = cleanText, createdAt = now, imagePath = imagePath)) +
        loadTagRecords(context, key).filterNot { it.text == cleanText && it.imagePath == imagePath }
    return saveTagRecords(context, key, next.take(MAX_TAG_RECORDS))
}

fun recordsToSaveUnique(records: List<TagRecord>): List<TagRecord> {
    return records
        .filter { it.text.isNotBlank() }
        .distinctBy { "${it.text}|${it.imagePath.orEmpty()}" }
}

fun deleteTagRecord(context: Context, key: String, id: Long): List<TagRecord> {
    return saveTagRecords(context, key, loadTagRecords(context, key).filterNot { it.id == id })
}

fun deleteTagRecordByText(context: Context, key: String, text: String): List<TagRecord> {
    return saveTagRecords(context, key, loadTagRecords(context, key).filterNot { it.text == text })
}

fun loadBitmapFromRecord(record: TagRecord): Bitmap? {
    val path = record.imagePath ?: return null
    return loadBitmapFromPath(path, 4096)
}

/**
 * 从文件路径加载 Bitmap，带内存安全保护。
 * maxPx 控制最大边长，超出时自动降采样。
 * 捕获 OOM 异常，避免直接闪退。
 */
fun loadBitmapFromPath(path: String, maxPx: Int): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        options.inJustDecodeBounds = false
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxPx, maxPx)
        // 使用 RGB_565 节省内存（ thumbnail 不需要 alpha 通道）
        if (maxPx <= 1024) {
            options.inPreferredConfig = Bitmap.Config.RGB_565
        }
        BitmapFactory.decodeFile(path, options)
    } catch (e: OutOfMemoryError) {
        android.util.Log.w("loadBitmap", "OOM loading $path, retrying with smaller size")
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) return null
            options.inJustDecodeBounds = false
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, 256, 256)
            options.inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeFile(path, options)
        } catch (e2: Exception) {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun saveHistoryImage(context: Context, bitmap: Bitmap): String? {
    return try {
        val dir = File(context.noBackupFilesDir, ".lcw/.media")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

fun todayKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

fun sanitizeHeroSubtitle(text: String): String {
    return text
        .replace(Regex("[\\r\\n\\t]+"), "")
        .trim()
        .take(HERO_SUBTITLE_MAX_LENGTH)
}

fun resolveHeroSubtitle(
    mode: String,
    customSubtitle: String,
    poetrySubtitle: String,
    defaultSubtitle: String
): String {
    return when (mode) {
        HERO_SUBTITLE_MODE_CUSTOM -> sanitizeHeroSubtitle(customSubtitle).ifBlank { defaultSubtitle }
        HERO_SUBTITLE_MODE_POETRY -> sanitizeHeroSubtitle(poetrySubtitle).ifBlank { fallbackPoetrySubtitle() }
        else -> defaultSubtitle
    }
}

fun heroSubtitleFontSize(text: String): Int {
    val length = sanitizeHeroSubtitle(text).length
    return when {
        length <= 8 -> 22
        length <= 12 -> 20
        length <= 16 -> 18
        else -> 16
    }
}

fun fallbackPoetrySubtitle(): String {
    val samples = listOf(
        "春眠不觉晓，处处闻啼鸟",
        "海上生明月，天涯共此时",
        "明月松间照，清泉石上流",
        "行到水穷处，坐看云起时",
        "山随平野尽，江入大荒流"
    )
    return samples[(System.currentTimeMillis() % samples.size).toInt()]
}

fun fetchDailyPoetrySubtitle(): String {
    var connection: HttpURLConnection? = null
    return runCatching {
        connection = (URL(CHINESE_POETRY_API_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        val conn = connection ?: return@runCatching ""
        val responseCode = conn.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.w("Poetry", "HTTP $responseCode from poetry API")
            return@runCatching ""
        }
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        extractPoetrySubtitleFromJson(body)
    }.getOrDefault("").also {
        connection?.disconnect()
    }
}

fun extractPoetrySubtitleFromJson(json: String): String {
    val root = runCatching { JSONObject(json) }.getOrNull() ?: return ""
    val candidates = mutableListOf<String>()

    fun addCandidate(value: String?) {
        val clean = sanitizePoetryLine(value ?: "")
        if (clean.isNotBlank() && clean.length <= HERO_SUBTITLE_MAX_LENGTH) candidates += clean
    }

    addCandidate(root.optString("content"))
    addCandidate(root.optString("sentence"))
    addCandidate(root.optString("text"))

    val data = root.optJSONObject("data")
    if (data != null) {
        addCandidate(data.optString("content"))
        addCandidate(data.optString("sentence"))
        addCandidate(data.optString("text"))
        val origin = data.optJSONObject("origin")
        val contentArray = origin?.optJSONArray("content")
        if (contentArray != null) {
            addCandidate(firstTwoShortPoetryLines(contentArray))
        }
    }

    val paragraphs = root.optJSONArray("paragraphs")
    if (paragraphs != null) addCandidate(firstTwoShortPoetryLines(paragraphs))

    return candidates.firstOrNull { it.length <= HERO_SUBTITLE_MAX_LENGTH } ?: ""
}

fun firstTwoShortPoetryLines(array: JSONArray): String {
    val lines = (0 until array.length())
        .mapNotNull { index -> array.optString(index).takeIf { it.isNotBlank() } }
        .map(::sanitizePoetryLine)
        .filter { it.isNotBlank() }
    for (firstIndex in lines.indices) {
        for (second in lines.drop(firstIndex + 1)) {
            val first = lines[firstIndex]
            val joined = sanitizePoetryLine("$first，$second")
            if (joined.length <= HERO_SUBTITLE_MAX_LENGTH) return joined
        }
    }
    return lines.firstOrNull { it.length <= HERO_SUBTITLE_MAX_LENGTH } ?: ""
}

fun sanitizePoetryLine(text: String): String {
    return text
        .replace(Regex("[\\r\\n\\t\\s]+"), "")
        .replace("。", "")
        .replace("！", "")
        .replace("？", "")
        .replace("；", "，")
        .trim('，', ',', '。', '.', '、')
}

fun loadAnalysisStats(context: Context): AnalysisStats {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val today = todayKey()
    val savedDate = prefs.getString(KEY_ANALYTICS_DATE, today) ?: today
    val todayCount = if (savedDate == today) prefs.getInt(KEY_ANALYTICS_TODAY_COUNT, 0) else 0
    val totalCount = prefs.getInt(KEY_ANALYTICS_TOTAL_COUNT, 0)
    val totalTimeMs = prefs.getLong(KEY_ANALYTICS_TOTAL_TIME_MS, 0L)
    val averageTimeMs = if (totalCount > 0) totalTimeMs / totalCount else 0L
    if (savedDate != today) {
        prefs.edit()
            .putString(KEY_ANALYTICS_DATE, today)
            .putInt(KEY_ANALYTICS_TODAY_COUNT, 0)
            .apply()
    }
    return AnalysisStats(todayCount = todayCount, totalCount = totalCount, averageTimeMs = averageTimeMs)
}

fun recordAnalysis(context: Context, elapsedMs: Long): AnalysisStats {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val today = todayKey()
    val current = loadAnalysisStats(context)
    val totalCount = current.totalCount + 1
    val totalTimeMs = prefs.getLong(KEY_ANALYTICS_TOTAL_TIME_MS, 0L) + elapsedMs
    val next = AnalysisStats(
        todayCount = current.todayCount + 1,
        totalCount = totalCount,
        averageTimeMs = if (totalCount > 0) totalTimeMs / totalCount else 0L
    )
    prefs.edit()
        .putString(KEY_ANALYTICS_DATE, today)
        .putInt(KEY_ANALYTICS_TODAY_COUNT, next.todayCount)
        .putInt(KEY_ANALYTICS_TOTAL_COUNT, next.totalCount)
        .putLong(KEY_ANALYTICS_TOTAL_TIME_MS, totalTimeMs)
        .apply()
    return next
}

fun loadExperienceState(context: Context): ExperienceState {
    val totalExp = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_TOTAL_EXP, 0)
        .coerceIn(0, MAX_TOTAL_EXP)
    return buildExperienceState(totalExp)
}

fun recordExperience(context: Context): ExperienceState {
    val current = loadExperienceState(context)
    if (current.level >= MAX_EXPERIENCE_LEVEL) {
        return current
    }
    val nextTotal = (current.totalExp + current.nextGain)
        .coerceAtMost(MAX_TOTAL_EXP)
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_TOTAL_EXP, nextTotal)
        .apply()
    return buildExperienceState(nextTotal)
}

fun buildExperienceState(totalExp: Int): ExperienceState {
    val cappedExp = totalExp.coerceIn(0, MAX_TOTAL_EXP)
    val level = ((cappedExp / EXP_PER_LEVEL) + 1).coerceAtMost(MAX_EXPERIENCE_LEVEL)
    val currentLevelStart = (level - 1) * EXP_PER_LEVEL
    val currentLevelExp = (cappedExp - currentLevelStart).coerceIn(0, EXP_PER_LEVEL)
    val gain = (BASE_EXP_GAIN * (1f - 0.1f * (level - 1)))
        .toInt()
        .coerceAtLeast(10)
    return ExperienceState(
        totalExp = cappedExp,
        level = level,
        currentLevelExp = if (level >= MAX_EXPERIENCE_LEVEL) EXP_PER_LEVEL else currentLevelExp,
        nextLevelExp = EXP_PER_LEVEL,
        nextGain = if (level >= MAX_EXPERIENCE_LEVEL) 0 else gain
    )
}

private fun saveTagRecords(context: Context, key: String, records: List<TagRecord>): List<TagRecord> {
    val array = JSONArray()
    records.forEach { record ->
        array.put(
            JSONObject()
                .put("id", record.id)
                .put("text", record.text)
                .put("createdAt", record.createdAt)
                .put("imagePath", record.imagePath ?: "")
        )
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(key, array.toString())
        .apply()
    return records
}

fun formatRecordShortTime(createdAt: Long): String {
    return SimpleDateFormat("MM/dd HH:mm", Locale.US).format(Date(createdAt))
}

fun copyTagsToClipboard(context: Context, tags: List<TaggerEngine.Tag>) {
    if (tags.isEmpty()) return
    copyTextToClipboard(context, tags.toTagText(), "tags")
}

fun copyTextToClipboard(context: Context, text: String, label: String = "tags") {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, context.getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
}

fun shareTags(context: Context, tags: List<TaggerEngine.Tag>) {
    if (tags.isEmpty()) return
    shareSpecialTagLink(context, tags.toTagText())
}

fun shareSpecialTagLink(context: Context, text: String) {
    if (text.isBlank()) return
    val link = createSpecialTagLink(text)
    val shareText = context.getString(R.string.special_link_share_text, link)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

fun sharePlainText(context: Context, text: String) {
    if (text.isBlank()) return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

fun shareSpecialTagLinks(context: Context, records: List<TagRecord>) {
    val links = recordsToSaveUnique(records)
        .map { createSpecialTagLink(it.text) }
    if (links.isEmpty()) return
    val shareText = context.getString(
        R.string.special_link_batch_share_text,
        links.size,
        links.joinToString("\n")
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

fun createSpecialTagLink(text: String): String {
    return Uri.Builder()
        .scheme("localcueword")
        .authority("tags")
        .appendQueryParameter("text", text)
        .build()
        .toString()
}

fun parseSpecialTagLink(intent: Intent?): TagRecord? {
    val uri = intent?.data ?: return null
    return parseSpecialTagUri(uri)
}

fun parseSpecialTagLinkText(rawText: String): TagRecord? {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) return null
    val link = Regex("""localcueword://tags\?\S+""").find(trimmed)
        ?.value
        ?: trimmed
    return try {
        parseSpecialTagUri(Uri.parse(link))
    } catch (e: Exception) {
        null
    }
}

private fun parseSpecialTagUri(uri: Uri): TagRecord? {
    if (uri.scheme != "localcueword" || uri.host != "tags") return null
    val text = uri.getQueryParameter("text")?.trim().orEmpty()
    if (text.isEmpty()) return null
    val now = System.currentTimeMillis()
    return TagRecord(id = now, text = text, createdAt = now)
}

private fun friendlyModelName(baseName: String): String {
    return when (baseName.lowercase()) {
        "wd-convnext-tagger-v3" -> "WD ConvNeXt Tagger v3"
        "wd-swinv2-tagger-v3" -> "WD SwinV2 Tagger v3"
        "wd-vit-tagger-v3" -> "WD ViT Tagger v3"
        "wd-eva02-large-tagger-v3" -> "WD EVA02 Large Tagger v3"
        else -> baseName
            .replace('_', ' ')
            .replace('-', ' ')
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }
}
