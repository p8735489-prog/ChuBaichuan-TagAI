package com.kuzulabz.waifutaggercn

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
import android.provider.OpenableColumns
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.kuzulabz.waifutaggercn.ui.components.MorphingBlobLoader
import com.kuzulabz.waifutaggercn.ui.theme.WaifuTaggerCNTheme
import com.kuzulabz.waifutaggercn.LocalLiquidGlassBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.isRuntimeShaderSupported
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
private const val KEY_THEME_STYLE = "theme_style" // "monet" | "ios27" | "custom_background"
private const val KEY_MONET_PALETTE = "monet_palette" // "device" | "green" | "blue" | "pink" | "yellow" | "purple" | "rainbow" | "brown" | "black"
private const val KEY_DARK_MODE = "dark_mode" // "system" | "light" | "dark"
private const val KEY_CUSTOM_BACKGROUND_IMAGE_PATH = "custom_background_image_path"
private const val KEY_CUSTOM_BACKGROUND_OPACITY = "custom_background_opacity"
private const val KEY_CUSTOM_BACKGROUND_DIM_AMOUNT = "custom_background_dim_amount"
private const val KEY_LIQUID_GLASS_ENABLED = "liquid_glass_enabled"
private const val KEY_HERO_SUBTITLE_MODE = "hero_subtitle_mode"
private const val KEY_HERO_CUSTOM_SUBTITLE = "hero_custom_subtitle"
private const val KEY_HERO_SUBTITLE_FONT_SIZE = "hero_subtitle_font_size"
private const val KEY_HERO_POETRY_SUBTITLE = "hero_poetry_subtitle"
private const val KEY_HERO_POETRY_DATE = "hero_poetry_date"
private const val KEY_HERO_POETRY_NOTICE_SHOWN = "hero_poetry_notice_shown"
private const val KEY_LANGUAGE = "language"   // "system" | "zh" | "en" | "ru" | "ja" | "ko"
private const val KEY_INTRO_SHOWN = "intro_shown"
private const val KEY_GENERAL_TAG_WEIGHT = "general_tag_weight"
private const val KEY_CHARACTER_TAG_WEIGHT = "character_tag_weight"
private const val KEY_PROMPT_TAG_LIMIT = "prompt_tag_limit"
private const val KEY_FAVORITE_TAG_RECORDS = "favorite_tag_records"
private const val KEY_HISTORY_TAG_RECORDS = "history_tag_records"
private const val KEY_ANALYTICS_DATE = "analytics_date"
private const val KEY_ANALYTICS_TODAY_COUNT = "analytics_today_count"
private const val KEY_ANALYTICS_TOTAL_COUNT = "analytics_total_count"
private const val KEY_ANALYTICS_TOTAL_TIME_MS = "analytics_total_time_ms"
private const val KEY_EXPERIENCE_ENABLED = "experience_enabled"
private const val KEY_EXPERIENCE_INTRO_SHOWN = "experience_intro_shown"
private const val KEY_TRANSLATE_NOTICE_SHOWN = "translate_notice_shown"
private const val KEY_CONFIRM_SAVE_DELETE = "confirm_save_delete"
private const val KEY_HIGH_PERFORMANCE_MODE = "inference_performance_mode" // "power_saving" | "performance" | "auto"
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
private const val CHINESE_POETRY_API_URL = "https://v2.jinrishici.com/one.json"
private const val MAX_TAG_RECORDS = 50
private const val MIN_PROMPT_TAG_LIMIT = 5
private const val DEFAULT_PROMPT_TAG_LIMIT = 30
private const val MAX_PROMPT_TAG_LIMIT = 150
private const val MAX_EXPERIENCE_LEVEL = 10
private const val EXP_PER_LEVEL = 1000
private const val MAX_TOTAL_EXP = (MAX_EXPERIENCE_LEVEL - 1) * EXP_PER_LEVEL
private const val BASE_EXP_GAIN = 100
private const val MIN_RELIABLE_COLOR_BODY_PART_SCORE = 0.75f
private const val AI_MODEL_NAME = "WD14 v3"
private const val PROJECT_URL = "https://github.com/p8735489-prog/ChuBaichuan-TagAI"
private const val QQ_GROUP_URL = "https://qm.qq.com/q/6jViPcR9le"
private const val TELEGRAM_URL = "https://t.me/Local_Cue_Word"

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
    val speedRank: Int
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

class MainActivity : ComponentActivity() {

    private lateinit var engine: TaggerEngine
    private lateinit var prefs: SharedPreferences
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
        val initialLanguageOption = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "system") ?: "system"
        applyLocaleToResources(this, normalizeLanguageOption(initialLanguageOption))
        super.onCreate(savedInstanceState)
        runCatching { enableEdgeToEdge() }
        engine = TaggerEngine(applicationContext)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        engine.highPerformanceMode = prefs.getString(KEY_HIGH_PERFORMANCE_MODE, DEFAULT_PERF_MODE) ?: DEFAULT_PERF_MODE
        incomingSpecialLinkRecord = parseSpecialTagLink(intent)
        availableAiModels = TaggerEngine.scanModelConfigs(applicationContext)
        // 如果选中的模型已不存在（比如没有内置模型时默认选 built_in_model 会失效），
        // 自动回退到第一个可用模型；没有可用模型时才用默认占位。
        val savedModelId = prefs.getString(KEY_SELECTED_AI_MODEL_ID, TaggerEngine.DEFAULT_MODEL_ID)
            ?: TaggerEngine.DEFAULT_MODEL_ID
        selectedAiModelId = availableAiModels.firstOrNull { it.id == savedModelId }?.id
            ?: availableAiModels.firstOrNull()?.id
            ?: TaggerEngine.DEFAULT_MODEL_ID

        loadAiModel(selectedAiModelId)

        setContent {
            var useDynamicColor by remember { mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_COLOR, true)) }
            var themeStyle by remember { mutableStateOf(prefs.getString(KEY_THEME_STYLE, THEME_STYLE_MONET) ?: THEME_STYLE_MONET) }
            var monetPalette by remember { mutableStateOf(prefs.getString(KEY_MONET_PALETTE, MONET_PALETTE_DEVICE) ?: MONET_PALETTE_DEVICE) }
            var darkModeOption by remember { mutableStateOf(prefs.getString(KEY_DARK_MODE, "system") ?: "system") }
            var customBackgroundImagePath by remember { mutableStateOf(prefs.getString(KEY_CUSTOM_BACKGROUND_IMAGE_PATH, "") ?: "") }
            var customBackgroundOpacity by remember { mutableStateOf(prefs.getFloat(KEY_CUSTOM_BACKGROUND_OPACITY, 0.38f).coerceIn(0f, 1f)) }
            var customBackgroundDimAmount by remember { mutableStateOf(prefs.getFloat(KEY_CUSTOM_BACKGROUND_DIM_AMOUNT, 0.12f).coerceIn(0f, 1f)) }
            var liquidGlassEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_LIQUID_GLASS_ENABLED, false)) }
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
            var languageOption by remember { mutableStateOf(normalizeLanguageOption(prefs.getString(KEY_LANGUAGE, "system") ?: "system")) }
            var showIntroDialog by remember { mutableStateOf(!prefs.getBoolean(KEY_INTRO_SHOWN, false)) }
            var generalTagWeight by remember { mutableStateOf(prefs.getFloat(KEY_GENERAL_TAG_WEIGHT, 1f)) }
            var characterTagWeight by remember { mutableStateOf(prefs.getFloat(KEY_CHARACTER_TAG_WEIGHT, 1f)) }
            var promptTagLimit by remember {
                mutableStateOf(prefs.getInt(KEY_PROMPT_TAG_LIMIT, DEFAULT_PROMPT_TAG_LIMIT).coerceIn(MIN_PROMPT_TAG_LIMIT, MAX_PROMPT_TAG_LIMIT))
            }
            var experienceEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_EXPERIENCE_ENABLED, false)) }
            var confirmSaveDelete by remember { mutableStateOf(prefs.getBoolean(KEY_CONFIRM_SAVE_DELETE, true)) }
            var inferencePerfMode by remember { mutableStateOf(prefs.getString(KEY_HIGH_PERFORMANCE_MODE, DEFAULT_PERF_MODE) ?: DEFAULT_PERF_MODE) }
            var showExperienceIntroDialog by remember { mutableStateOf(false) }

            val darkThemeOverride = when (darkModeOption) {
                "light" -> false
                "dark" -> true
                else -> null // follow system
            }

            val useIos27Style = themeStyle == THEME_STYLE_IOS27
            val useCustomBackground = themeStyle == THEME_STYLE_CUSTOM_BACKGROUND
            val effectiveDynamicColor = useDynamicColor && themeStyle == THEME_STYLE_MONET

            WaifuTaggerCNTheme(
                    useDynamicColor = effectiveDynamicColor,
                    useIos27Style = useIos27Style,
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
                            window.statusBarColor = if (useCustomBackground || useIos27Style) Color.Transparent.toArgb() else systemBarColor.toArgb()
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
                        // 背景由 TaggerScreen 内部统一处理（含液态玻璃 backdrop）
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
                            liquidGlassEnabled = liquidGlassEnabled,
                            heroSubtitleMode = heroSubtitleMode,
                            heroCustomSubtitle = heroCustomSubtitle,
                            heroSubtitleFontSize = heroSubtitleFontSize,
                            heroPoetrySubtitle = heroPoetrySubtitle,
                            heroPoetryDate = heroPoetryDate,
                            heroPoetryNoticeShown = heroPoetryNoticeShown,
                            experienceEnabled = experienceEnabled,
                            confirmSaveDelete = confirmSaveDelete,
                            inferencePerfMode = inferencePerfMode,
                            onDynamicColorChange = {
                                useDynamicColor = it
                                prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, it).apply()
                            },
                            onThemeStyleChange = {
                                themeStyle = it
                                val nextPalette = if (it == THEME_STYLE_IOS27) {
                                    if (monetPalette in minimalPaletteOptions()) monetPalette else MONET_PALETTE_DEVICE
                                } else if (it == THEME_STYLE_CUSTOM_BACKGROUND) {
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
                            onLiquidGlassEnabledChange = {
                                liquidGlassEnabled = it
                                prefs.edit().putBoolean(KEY_LIQUID_GLASS_ENABLED, it).apply()
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
                            onHeroPoetryNoticeShown = {
                                heroPoetryNoticeShown = true
                                prefs.edit().putBoolean(KEY_HERO_POETRY_NOTICE_SHOWN, true).apply()
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
                            onReloadAiModels = {
                                availableAiModels = TaggerEngine.scanModelConfigs(applicationContext)
                                android.util.Log.d("MainActivity", "onReloadAiModels: ${availableAiModels.size} models")
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
                            showIntroDialog = showIntroDialog,
                            onIntroDismiss = {
                                showIntroDialog = false
                                prefs.edit().putBoolean(KEY_INTRO_SHOWN, true).apply()
                            },
                            showExperienceIntroDialog = showExperienceIntroDialog,
                            onExperienceIntroDismiss = { showExperienceIntroDialog = false }
                        )
                    }
                }
        }
    }

    private fun loadAiModel(modelId: String) {
        isLoadingModel = true
        loadError = null
        lifecycleScope.launch {
            availableAiModels = TaggerEngine.scanModelConfigs(applicationContext)
            val modelConfig = availableAiModels.firstOrNull { it.id == modelId }
                ?: availableAiModels.firstOrNull()
                ?: TaggerEngine.builtInModelConfig()
            val error = withContext(Dispatchers.IO) { engine.load(modelConfig) }
            if (error == null) {
                selectedAiModelId = modelConfig.id
                prefs.edit().putString(KEY_SELECTED_AI_MODEL_ID, modelConfig.id).apply()
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
private const val THEME_STYLE_IOS27 = "ios27"
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
        "system", "zh", "en", "ru", "ja", "ko" -> option
        else -> "system"
    }
}

private fun createLocalizedContext(context: Context, option: String): Context {
    return runCatching {
        val locale = when (normalizeLanguageOption(option)) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            "ru" -> Locale("ru")
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
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
        "en" -> "en"
        "ru" -> "ru"
        "ja" -> "ja"
        "ko" -> "ko"
        else -> ""
    }
}

private fun applyAppLocale(context: Context, option: String) {
    val normalizedOption = normalizeLanguageOption(option)
    applyLocaleToResources(context, normalizedOption)
}

private fun applyLocaleToResources(context: Context, option: String) {
    val localizedContext = createLocalizedContext(context, option)
    val localizedConfiguration = localizedContext.resources.configuration
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(localizedConfiguration, context.resources.displayMetrics)
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
        "zh", "en", "ru", "ja", "ko" -> systemLocale
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

@OptIn(ExperimentalMaterial3Api::class)
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
    liquidGlassEnabled: Boolean,
    heroSubtitleMode: String,
    heroCustomSubtitle: String,
    heroSubtitleFontSize: Int,
    heroPoetrySubtitle: String,
    heroPoetryDate: String,
    heroPoetryNoticeShown: Boolean,
    experienceEnabled: Boolean,
    confirmSaveDelete: Boolean,
    inferencePerfMode: String,
    onDynamicColorChange: (Boolean) -> Unit,
    onThemeStyleChange: (String) -> Unit,
    onMonetPaletteChange: (String) -> Unit,
    onGeneralTagWeightChange: (Float) -> Unit,
    onCharacterTagWeightChange: (Float) -> Unit,
    onPromptTagLimitChange: (Int) -> Unit,
    onCustomBackgroundImagePathChange: (String) -> Unit,
    onCustomBackgroundOpacityChange: (Float) -> Unit,
    onCustomBackgroundDimAmountChange: (Float) -> Unit,
    onLiquidGlassEnabledChange: (Boolean) -> Unit,
    onHeroSubtitleModeChange: (String) -> Unit,
    onHeroCustomSubtitleChange: (String) -> Unit,
    onHeroSubtitleFontSizeChange: (Int) -> Unit,
    onHeroPoetrySubtitleChange: (String, String) -> Unit,
    onHeroPoetryNoticeShown: () -> Unit,
    onExperienceEnabledChange: (Boolean) -> Unit,
    onConfirmSaveDeleteChange: (Boolean) -> Unit,
    onInferencePerfModeChange: (String) -> Unit,
    onReloadAiModels: () -> Unit,
    onSelectAiModel: (String) -> Unit,
    darkModeOption: String,
    onDarkModeChange: (String) -> Unit,
    languageOption: String,
    incomingSpecialLinkRecord: TagRecord?,
    onLanguageChange: (String) -> Unit,
    showIntroDialog: Boolean,
    onIntroDismiss: () -> Unit,
    showExperienceIntroDialog: Boolean,
    onExperienceIntroDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tags by remember { mutableStateOf<List<TaggerEngine.Tag>>(emptyList()) }
    var imageScore by remember { mutableStateOf<ImageScore?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var inferenceProgressTargetPercent by remember { mutableStateOf(0f) }
    val animatedInferenceProgressPercent by animateFloatAsState(
        targetValue = inferenceProgressTargetPercent.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
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
    var showPoetryApiNotice by remember { mutableStateOf(false) }
    var translateNoticeShown by remember {
        mutableStateOf(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TRANSLATE_NOTICE_SHOWN, false))
    }
    var isTranslating by remember { mutableStateOf(false) }
    var translateTargetLang by remember { mutableStateOf("zh") }
    var translatedTags by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val mainScrollState = rememberScrollState()
    val safePromptTagLimit = promptTagLimit.coerceIn(MIN_PROMPT_TAG_LIMIT, MAX_PROMPT_TAG_LIMIT)
    var favoriteRecords by remember { mutableStateOf(loadTagRecords(context, KEY_FAVORITE_TAG_RECORDS)) }
    var historyRecords by remember { mutableStateOf(loadTagRecords(context, KEY_HISTORY_TAG_RECORDS)) }
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
        if (translateNoticeShown) {
            showTranslateDialog = true
        } else {
            showTranslateNetworkNotice = true
        }
    }

    LaunchedEffect(translatedTags.size) {
        if (translatedTags.isNotEmpty()) {
            delay(280L)
            mainScrollState.animateScrollTo(
                value = mainScrollState.maxValue,
                animationSpec = tween(durationMillis = 720)
            )
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        inferenceProgressTargetPercent = 0f
        while (inferenceProgressTargetPercent < 94f) {
            delay(90L)
            inferenceProgressTargetPercent = when {
                inferenceProgressTargetPercent < 42f -> inferenceProgressTargetPercent + 5.5f
                inferenceProgressTargetPercent < 74f -> inferenceProgressTargetPercent + 3.2f
                else -> inferenceProgressTargetPercent + 1.15f
            }.coerceAtMost(94f)
        }
    }

    // 批量识别：逐张处理选中的图片
    LaunchedEffect(isBatchRunning, batchProgressIndex) {
        if (!isBatchRunning || selectedBatchUris.isEmpty()) return@LaunchedEffect
        if (batchProgressIndex >= selectedBatchUris.size) {
            isBatchRunning = false
            return@LaunchedEffect
        }
        val uri = selectedBatchUris[batchProgressIndex]
        val bmp = withContext(Dispatchers.Default) { loadBitmap(context, uri) }
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
        val result = withContext(Dispatchers.Default) {
            runCatching {
                engine.tag(
                    bitmap = bmp,
                    threshold = threshold,
                    generalWeight = generalTagWeight,
                    characterWeight = characterTagWeight
                )
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
        // 存历史
        if (err == null && tags.isNotEmpty()) {
            val savedImagePath = saveHistoryImage(context, bmp)
            historyRecords = saveTagRecord(context, KEY_HISTORY_TAG_RECORDS, tags.take(safePromptTagLimit).toTagText(), savedImagePath)
        }
        batchProgressIndex = batchProgressIndex + 1
    }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showParseLinkDialog by remember { mutableStateOf(false) }
    var showAiModelDialog by remember { mutableStateOf(false) }
    var showImportGuideDialog by remember { mutableStateOf(false) }
    var pendingCustomBackgroundBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var downloadingAiModelId by remember { mutableStateOf<String?>(null) }
    var isDownloadCancelled by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf<DownloadProgress?>(null) }
    var aiDownloadSource by remember {
        mutableStateOf(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_AI_MODEL_DOWNLOAD_SOURCE, AI_MODEL_SOURCE_HUGGING_FACE)
                ?: AI_MODEL_SOURCE_HUGGING_FACE
        )
    }
    var selectedMainTab by remember { mutableStateOf(0) }
    var analysisStats by remember { mutableStateOf(loadAnalysisStats(context)) }
    var experienceState by remember { mutableStateOf(loadExperienceState(context)) }
    val useIos27Style = themeStyle == THEME_STYLE_IOS27
    val useCustomBackgroundStyle = themeStyle == THEME_STYLE_CUSTOM_BACKGROUND
    // 液态玻璃效果在 iOS27 主题或独立开关开启时生效
    val useGlassEffect = useIos27Style || liquidGlassEnabled
    val systemDarkTheme = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val effectiveDarkTheme = when (darkModeOption) {
        "light" -> false
        "dark" -> true
        else -> systemDarkTheme
    }
    val limitedTags = tags.filterPromptNoiseTags().take(safePromptTagLimit)
    val currentTagText = limitedTags.toTagText()
    val currentLimitedTagNames = limitedTags.map { it.name }.toSet()
    val isCurrentFavorite = currentTagText.isNotEmpty() && favoriteRecords.any { it.text == currentTagText }
    val detectionResult = detectResult(limitedTags)
    val recommendedModels = recommendModels(detectionResult)
    val autoPromptDraft = generateAutoPromptDraft(limitedTags, safePromptTagLimit)
    val negativePrompt = generateNegativePrompt(limitedTags)
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
                fetchDailyPoetrySubtitle().ifBlank { fallbackPoetrySubtitle() }
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
                        intent.getStringExtra(AiModelDownloadService.EXTRA_MODEL_PATH)?.let(onSelectAiModel)
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
        bitmap = loadBitmapFromRecord(record)
        imageUri = null
        historyRecords = saveTagRecord(context, KEY_HISTORY_TAG_RECORDS, record.text)
        Toast.makeText(context, context.getString(R.string.special_link_loaded), Toast.LENGTH_SHORT).show()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            bitmap = loadBitmap(context, uri)
            tags = emptyList()
            imageScore = null
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
            val message = importAiModelFile(context, uri)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onReloadAiModels()
        }
    }

    val pickCustomBackgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val selectedBitmap = loadBitmap(context, uri)
            if (selectedBitmap == null) {
                Toast.makeText(context, context.getString(R.string.settings_custom_background_import_failed), Toast.LENGTH_LONG).show()
            } else {
                pendingCustomBackgroundBitmap = selectedBitmap
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
                        onHeroPoetryNoticeShown()
                        onHeroSubtitleModeChange(HERO_SUBTITLE_MODE_POETRY)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.settings_subtitle_poetry_notice_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPoetryApiNotice = false }) {
                    Text(stringResource(R.string.cancel))
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

    if (showIntroDialog) {
        IntroDialog(onDismiss = onIntroDismiss)
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
            text = { Text(stringResource(R.string.translate_network_notice_message)) },
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
                TextButton(onClick = { showTranslateNetworkNotice = false }) {
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
            text = { Text(stringResource(R.string.compare_need_models_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showCompareNeedModelsDialog = false
                        selectedMainTab = 1
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
                    val (r1, r2) = withContext(Dispatchers.IO) {
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
                    bitmap = loadBitmapFromRecord(record)
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
                val restoredBitmap = loadBitmapFromRecord(it)
                tags = restoredTags
                bitmap = restoredBitmap
                imageUri = null
                imageScore = scoreImage(restoredBitmap, restoredTags)
                showFavoritesDialog = false
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
                val restoredBitmap = loadBitmapFromRecord(it)
                tags = restoredTags
                bitmap = restoredBitmap
                imageUri = null
                imageScore = scoreImage(restoredBitmap, restoredTags)
                showHistoryDialog = false
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
    // 液态玻璃：完全匹配 AndroidLiquidGlass 2.0.0 (LiquidGlassWeather) 模式
    // layerBackdrop 直接修饰背景元素，只录制背景内容供玻璃元素采样
    val liquidGlassBackdrop = rememberLayerBackdrop()
    val useLiquidGlass = liquidGlassEnabled && isRenderEffectSupported()
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景层：layerBackdrop 直接修饰背景 Box（含图片+遮罩），匹配 LiquidGlassWeather 模式
        if (useCustomBackgroundStyle && customBackgroundImagePath.isNotBlank()) {
            val bitmap = remember(customBackgroundImagePath) {
                customBackgroundImagePath.takeIf { it.isNotBlank() }
                    ?.let { path ->
                        runCatching {
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeFile(path, options)
                            options.inJustDecodeBounds = false
                            val maxPx = 2048 // 最大边长限制，足够屏幕显示
                            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxPx, maxPx)
                            BitmapFactory.decodeFile(path, options)
                        }.getOrNull()
                    }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (useLiquidGlass) Modifier.layerBackdrop(liquidGlassBackdrop) else Modifier)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
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
                    .then(if (useLiquidGlass) Modifier.layerBackdrop(liquidGlassBackdrop) else Modifier)
                    .appThemedBackground()
            )
        }
        CompositionLocalProvider(
            LocalLiquidGlassBackdrop provides if (useLiquidGlass) liquidGlassBackdrop else null,
            LocalSubtitleFontSize provides heroSubtitleFontSize
        ) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        floatingActionButton = {
            // 一键复制提示词 — dedicated one-tap "copy as prompt" action,
            // always reachable without scrolling to the button row below.
            if (selectedMainTab == 0 && tags.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = stringResource(R.string.copy_prompt),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 5.dp,
                        pressedElevation = 4.dp,
                        focusedElevation = 5.dp,
                        hoveredElevation = 6.dp
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .navigationBarsPadding()
                        .padding(bottom = 56.dp),
                    onClick = { copyTextToClipboard(context, autoPromptDraft.fullPrompt, "auto_prompt") }
                )
            }
        },
        bottomBar = {
            IosMorphingSegmentedControl(
                options = listOf(
                    "0" to stringResource(R.string.main_tab_recognition),
                    "1" to stringResource(R.string.main_tab_records),
                    "2" to stringResource(R.string.main_tab_models),
                    "3" to stringResource(R.string.settings_title)
                ),
                current = selectedMainTab.toString(),
                onSelect = {
                    selectedMainTab = it.toInt()
                    scope.launch { mainScrollState.animateScrollTo(0) }
                },
                useGlassEffect = useGlassEffect,
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
            AnimatedVisibility(visible = selectedMainTab != 3 && loadError != null) {
                val localizedLoadError = localizedLoadErrorText(loadError)
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (useGlassEffect) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.74f) else MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .softEnter(0)
                        .liquidGlassBorder(useGlassEffect, 24)
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
                    modifier = Modifier.softEnter(1)
                )
            }

            AnimatedContent(
                targetState = selectedMainTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 8 }) togetherWith
                    (fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(180)) { -it / 8 })
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
                    .softEnter(3)
                    .liquidGlassBorder(useGlassEffect, 28),
                shape = RoundedCornerShape(28.dp),
                colors = glassCardColors(useGlassEffect)
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
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = LocalSubtitleFontSize.current.sp
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
                    onClick = { pickImageLauncher.launch("image/*") },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
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
                        containerColor = MaterialTheme.colorScheme.surface,
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
                    state = experienceState,
                    useIos27Style = useGlassEffect
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassBorder(useGlassEffect, 24),
                colors = glassCardColors(useGlassEffect)
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
                            isRunning = true
                            scope.launch {
                                try {
                                    val startedAt = SystemClock.elapsedRealtime()
                                    val result = withContext(Dispatchers.Default) {
                                        engine.tag(
                                            bitmap = bmp,
                                            threshold = threshold,
                                            generalWeight = generalTagWeight,
                                            characterWeight = characterTagWeight
                                        )
                                    }
                                    val elapsedMs = SystemClock.elapsedRealtime() - startedAt
                                    lastInferenceTimeMs = elapsedMs
                                    val cleanResult = result.filterPromptNoiseTags()
                                    tags = cleanResult
                                    imageScore = scoreImage(bmp, cleanResult)
                                    val savedImagePath = saveHistoryImage(context, bmp)
                                    analysisStats = recordAnalysis(context, elapsedMs)
                                    if (experienceEnabled) {
                                        experienceState = recordExperience(context)
                                    }
                                    historyRecords = saveTagRecord(context, KEY_HISTORY_TAG_RECORDS, cleanResult.take(safePromptTagLimit).toTagText(), savedImagePath)
                                } finally {
                                    inferenceProgressTargetPercent = 100f
                                    delay(360L)
                                    isRunning = false
                                }
                            }
                        }
                    ) {
                        Text(
                            if (isRunning) stringResource(R.string.running) else stringResource(R.string.run_tagging),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isRunning,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(180))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassBorder(useGlassEffect, 18),
                    shape = RoundedCornerShape(18.dp),
                    colors = glassCardColors(useGlassEffect),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (useGlassEffect) 0.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${(animatedInferenceProgressPercent + 0.5f).toInt().coerceIn(0, 100)}% · ${stringResource(R.string.model_inference_progress)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animatedInferenceProgressPercent.coerceIn(0f, 100f) / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = imageScore != null,
                enter = fadeIn(spring(stiffness = 350f, dampingRatio = 0.75f)) + slideInVertically(spring(stiffness = 300f, dampingRatio = 0.75f)) { it / 4 },
                exit = fadeOut(tween(120))
            ) {
                imageScore?.let {
                    ImageScoreCard(
                        score = it,
                        useIos27Style = useGlassEffect
                    )
                }
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(spring(stiffness = 300f, dampingRatio = 0.75f)) + slideInVertically(spring(stiffness = 280f, dampingRatio = 0.75f)) { it / 5 },
                exit = fadeOut(tween(120))
            ) {
                AutoPromptWriterCard(
                    promptDraft = autoPromptDraft,
                    useIos27Style = useGlassEffect,
                    isTranslating = isTranslating,
                    onTranslate = openTranslateWithNotice,
                    onCopyPrompt = { copyTextToClipboard(context, autoPromptDraft.fullPrompt, "auto_prompt") }
                )
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(spring(stiffness = 300f, dampingRatio = 0.75f)) + slideInVertically(spring(stiffness = 280f, dampingRatio = 0.75f)) { it / 5 },
                exit = fadeOut(tween(120))
            ) {
                NegativePromptCard(
                    negativePrompt = negativePrompt,
                    useIos27Style = useGlassEffect,
                    onCopy = { copyTextToClipboard(context, negativePrompt, "negative_prompt") }
                )
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(spring(stiffness = 300f, dampingRatio = 0.75f)) + slideInVertically(spring(stiffness = 280f, dampingRatio = 0.75f)) { it / 5 },
                exit = fadeOut(tween(120))
            ) {
                ModelRecommendationCard(
                    detectionResult = detectionResult,
                    recommendedModels = recommendedModels,
                    useIos27Style = useGlassEffect
                )
            }

            AnimatedVisibility(visible = tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            onClick = { copyTagsToClipboard(context, limitedTags) }
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.copy_tags), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            onClick = { shareTags(context, limitedTags) }
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.share_tags), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = tags.isEmpty() && !isRunning && bitmap != null) {
                Text(
                    stringResource(R.string.no_tags),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = tags.isNotEmpty(),
                enter = fadeIn(spring(stiffness = 380f, dampingRatio = 0.82f)) + slideInVertically(spring(stiffness = 380f, dampingRatio = 0.82f)) { it / 8 },
                exit = fadeOut(tween(100))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    limitedTags.forEach { tag ->
                        // 每个标签卡片带交错出现动画
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(spring(stiffness = 380f, dampingRatio = 0.82f)) + slideInVertically(spring(stiffness = 380f, dampingRatio = 0.82f)) { it / 8 },
                            exit = fadeOut(tween(100))
                        ) {
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
            }

            val visibleTranslatedTags = translatedTags.filter { (original, _) ->
                original in currentLimitedTagNames
            }
            AnimatedVisibility(visible = tab == 0 && visibleTranslatedTags.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = glassCardColors(useGlassEffect),
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassBorder(useGlassEffect, 20)
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
            DeviceStatusCard(
                modelName = selectedAiModelName,
                speedText = lastInferenceTimeMs?.let { formatInferenceSpeed(it) } ?: stringResource(R.string.ai_status_speed_empty),
                deviceName = getDeviceName(),
                useIos27Style = useGlassEffect
            )

            TodayAnalysisCard(
                stats = analysisStats,
                useIos27Style = useGlassEffect
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = glassCardColors(useGlassEffect),
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassBorder(useGlassEffect, 24)
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
                AiModelPage(
                    models = aiModels,
                    downloadableModels = BUILT_IN_DOWNLOADABLE_AI_MODELS,
                    selectedModelId = selectedAiModelId,
                    isLoadingModel = isLoadingModel,
                    downloadingModelId = downloadingAiModelId,
                    downloadProgress = downloadProgress,
                    downloadSource = aiDownloadSource,
                    onDownloadSourceChange = changeAiDownloadSource,
                    onImportFile = { showImportGuideDialog = true },
                    onDownloadModel = startAiModelDownload,
                    onCancelDownload = cancelAiModelDownload,
                    onSelectModel = selectAiModelFromUi,
                    themeStyle = themeStyle,
                    modifier = Modifier.softEnter(3)
                )
            }

            if (tab == 3) {
                SettingsPage(
                    useDynamicColor = useDynamicColor,
                    themeStyle = themeStyle,
                    monetPalette = monetPalette,
                    customBackgroundImagePath = customBackgroundImagePath,
                    customBackgroundOpacity = customBackgroundOpacity,
                    customBackgroundDimAmount = customBackgroundDimAmount,
                    liquidGlassEnabled = liquidGlassEnabled,
                    heroSubtitleMode = heroSubtitleMode,
                    heroCustomSubtitle = heroCustomSubtitle,
                    heroSubtitleFontSize = heroSubtitleFontSize,
                    experienceEnabled = experienceEnabled,
                    confirmSaveDelete = confirmSaveDelete,
                    promptTagLimit = safePromptTagLimit,
                    onDynamicColorChange = onDynamicColorChange,
                    onThemeStyleChange = onThemeStyleChange,
                    onMonetPaletteChange = onMonetPaletteChange,
                    onPickCustomBackground = { pickCustomBackgroundLauncher.launch("image/*") },
                    onClearCustomBackground = { onCustomBackgroundImagePathChange("") },
                    onCustomBackgroundOpacityChange = onCustomBackgroundOpacityChange,
                    onCustomBackgroundDimAmountChange = onCustomBackgroundDimAmountChange,
                    onLiquidGlassEnabledChange = onLiquidGlassEnabledChange,
                    onHeroSubtitleModeChange = { mode ->
                        if (mode == HERO_SUBTITLE_MODE_POETRY && !heroPoetryNoticeShown) {
                            showPoetryApiNotice = true
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

            if (selectedMainTab != 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    var showRecommendDialog by remember { mutableStateOf(false) }
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
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            IconButton(
                onClick = { showRecommendDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.welcome_dialog_recommend),
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (showRecommendDialog) {
        RecommendFriendDialog(
            onDismiss = { showRecommendDialog = false }
        )
    }
}

@Composable
fun TopActionRow(
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier
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
            onClick = onHistoryClick
        )
        TopActionButton(
            icon = Icons.Filled.Favorite,
            label = stringResource(R.string.favorites_title),
            iconTint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = onFavoritesClick
        )
        TopActionButton(
            icon = Icons.Filled.Compare,
            label = stringResource(R.string.compare_title),
            iconTint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = onCompareClick
        )
    }
}

@Composable
private fun IosMorphingSegmentedControl(
    options: List<Pair<String, String>>,
    current: String,
    onSelect: (String) -> Unit,
    useGlassEffect: Boolean = false,
    modifier: Modifier = Modifier
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
    // 液态玻璃模式下，长按时增强模糊和折射
    val glassBlur by animateFloatAsState(
        targetValue = if (isLongPressed && useGlassEffect) 16f else 8f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "tabBarGlassBlur"
    )

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .then(
                if (useGlassEffect) {
                    Modifier.liquidGlassBorder(true, 26)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f))
                }
            )
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
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
    Row(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
fun TodayAnalysisCard(
    stats: AnalysisStats,
    useIos27Style: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = glassCardColors(useIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(useIos27Style, 20)
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
    state: ExperienceState,
    useIos27Style: Boolean
) {
    val levelEmoji = experienceLevelEmoji(state.level)
    val progress = if (state.level >= MAX_EXPERIENCE_LEVEL) {
        1f
    } else {
        state.currentLevelExp.toFloat() / state.nextLevelExp.toFloat()
    }
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = glassCardColors(useIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(useIos27Style, 22)
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
    useIos27Style: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = glassCardColors(useIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(useIos27Style, 20)
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
        }
    }
}

@Composable
private fun AIStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ImageScoreCard(
    score: ImageScore,
    useIos27Style: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = glassCardColors(useIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(useIos27Style, 20)
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

@Composable
fun AutoPromptWriterCard(
    promptDraft: AutoPromptDraft,
    useIos27Style: Boolean,
    isTranslating: Boolean,
    onTranslate: () -> Unit,
    onCopyPrompt: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = glassCardColors(useIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(useIos27Style, 22)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = LocalSubtitleFontSize.current.sp
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

            Text(
                promptDraft.fullPrompt.ifBlank { stringResource(R.string.auto_prompt_empty) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = LocalSubtitleFontSize.current.sp,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )

            OutlinedButton(
                onClick = onCopyPrompt,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.copy_prompt_button))
            }

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
            fontWeight = FontWeight.Bold
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun NegativePromptCard(
    negativePrompt: String,
    useIos27Style: Boolean,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = glassCardColors(useIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(useIos27Style, 20)
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = LocalSubtitleFontSize.current.sp
            )
            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.copy_negative_prompt))
            }
        }
    }
}

@Composable
fun ModelRecommendationCard(
    detectionResult: String,
    recommendedModels: List<String>,
    useIos27Style: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = glassCardColors(useIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(useIos27Style, 20)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = LocalSubtitleFontSize.current.sp
            )
            Text(
                stringResource(R.string.recommended_models_label),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = LocalSubtitleFontSize.current.sp
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
                        IconButton(onClick = onDismiss) {
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
                            }
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
    val previewBitmap = remember(record.imagePath) { loadBitmapFromRecord(record) }
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
            if (previewBitmap != null) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
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
            IconButton(onClick = onCancel) {
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
            IconButton(onClick = onSelectAll) {
                Icon(
                    if (allSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = stringResource(R.string.record_select_all),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(enabled = hasSelection, onClick = onSave) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(R.string.record_batch_save),
                    tint = if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
            IconButton(enabled = hasSelection, onClick = onShare) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = stringResource(R.string.record_batch_share),
                    tint = if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
            IconButton(enabled = hasSelection, onClick = onDelete) {
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
    val previewBitmap = remember(record.imagePath) { loadBitmapFromRecord(record) }
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
                .clickable { onDismiss() }
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
                        bitmap = previewBitmap.asImageBitmap(),
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
        Text(label, fontWeight = FontWeight.Bold)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    copyTextToClipboard(
                        context,
                        context.getString(R.string.record_prompt_copy_text, positivePrompt, negativePrompt)
                    )
                }
            ) {
                Text(stringResource(R.string.record_copy))
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.community_qq), modifier = Modifier.fillMaxWidth())
                }
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL)))
                        onDismiss()
                    },
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = MaterialTheme.colorScheme.error
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
    val isIos27Style = themeStyle == THEME_STYLE_IOS27
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
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = glassCardColors(isIos27Style),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassBorder(isIos27Style, 24)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.ai_model_supported_models),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isIos27Style) liquidGlassSurfaceColor() else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.42f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassBorder(isIos27Style, 22)
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
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    stringResource(R.string.ai_model_import_rules_zip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.ai_model_import_rules_pairing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.ai_model_import_rules_guard),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = glassCardColors(isIos27Style),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassBorder(isIos27Style, 22)
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
                    Text(stringResource(R.string.ai_model_import_file), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = glassCardColors(isIos27Style),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassBorder(isIos27Style, 22)
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
                isIos27Style = isIos27Style,
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
                colors = glassCardColors(isIos27Style),
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassBorder(isIos27Style, 18)
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
                            if (isIos27Style) blendColor(liquidGlassSurfaceColor(), MaterialTheme.colorScheme.primaryContainer, 0.34f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        } else {
                            if (isIos27Style) liquidGlassSurfaceColor().copy(alpha = 0.58f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        }
                    )
                    .liquidGlassBorder(isIos27Style, 18)
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
                    Text(
                        model.displayName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
    isIos27Style: Boolean,
    onDownloadModel: (DownloadableAiModel) -> Unit,
    onCancelDownload: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = glassCardColors(isIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(isIos27Style, 18)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(stringResource(model.descriptionResId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(model.family, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(model.sizeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.ai_model_strength_label, model.strengthRank), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.ai_model_speed_label, model.speedRank), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                TextButton(
                    enabled = !isLoadingModel && !isThisDownloading && !downloaded,
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
                    Text("${downloadProgress.percent}%  ·  ${downloadProgress.phase}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onCancelDownload, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                        Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.ai_model_download_abandon), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
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
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(R.string.ai_model_supported_models),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
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
                                Text(
                                    model.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
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
                            Text(
                                model.displayName,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
fun IntroDialog(onDismiss: () -> Unit) {
    var showRecommendDialog by remember { mutableStateOf(false) }
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
                lineHeight = 21.sp
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
            TextButton(onClick = { showRecommendDialog = true }) {
                Text(stringResource(R.string.welcome_dialog_recommend))
            }
        }
    )

    if (showRecommendDialog) {
        RecommendFriendDialog(
            onDismiss = { showRecommendDialog = false },
            onAfterAction = onDismiss
        )
    }
}

@Composable
fun RecommendFriendDialog(
    onDismiss: () -> Unit,
    onAfterAction: () -> Unit = {}
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.welcome_recommend_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        sharePlainText(context, PROJECT_URL)
                        onDismiss()
                        onAfterAction()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.welcome_share_source), modifier = Modifier.fillMaxWidth())
                }
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(QQ_GROUP_URL)))
                        onDismiss()
                        onAfterAction()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.welcome_share_qq), modifier = Modifier.fillMaxWidth())
                }
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL)))
                        onDismiss()
                        onAfterAction()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.welcome_share_telegram), modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
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
    liquidGlassEnabled: Boolean,
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
    onLiquidGlassEnabledChange: (Boolean) -> Unit,
    onHeroSubtitleModeChange: (String) -> Unit,
    onHeroCustomSubtitleChange: (String) -> Unit,
    onHeroSubtitleFontSizeChange: (Int) -> Unit,
    onExperienceEnabledChange: (Boolean) -> Unit,
    onConfirmSaveDeleteChange: (Boolean) -> Unit,
    onPromptTagLimitChange: (Int) -> Unit,
    inferencePerfMode: String,
    onInferencePerfModeChange: (String) -> Unit,
    darkModeOption: String,
    onDarkModeChange: (String) -> Unit,
    languageOption: String,
    onLanguageChange: (String) -> Unit,
    onOpenFileManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIos27Style = themeStyle == THEME_STYLE_IOS27
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isIos27Style) liquidGlassDialogColor() else MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassBorder(isIos27Style, 24)
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
            isIos27Style = isIos27Style,
            themeStyle = themeStyle,
            monetPalette = monetPalette,
            customBackgroundImagePath = customBackgroundImagePath,
            customBackgroundOpacity = customBackgroundOpacity,
            customBackgroundDimAmount = customBackgroundDimAmount,
            liquidGlassEnabled = liquidGlassEnabled,
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
            onLiquidGlassEnabledChange = onLiquidGlassEnabledChange,
            onHeroSubtitleModeChange = onHeroSubtitleModeChange,
            onHeroCustomSubtitleChange = onHeroCustomSubtitleChange,
            onHeroSubtitleFontSizeChange = onHeroSubtitleFontSizeChange,
            onDarkModeChange = onDarkModeChange
        )
        LanguageSettingsCard(
            languageOption = languageOption,
            onLanguageChange = onLanguageChange,
            isIos27Style = isIos27Style
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
            onInferencePerfModeChange = onInferencePerfModeChange
        )
        FileManagerEntryCard(
            onOpenFileManager = onOpenFileManager,
            isIos27Style = isIos27Style
        )
    }
}

@Composable
private fun FileManagerEntryCard(
    onOpenFileManager: () -> Unit,
    isIos27Style: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = glassCardColors(isIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(isIos27Style, 24)
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
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    stringResource(R.string.file_manager_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        stringResource(R.string.file_manager_total_size, formatFileSize(totalCacheSize)),
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
                        Text(stringResource(R.string.file_manager_clear_cache), color = MaterialTheme.colorScheme.error)
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
    val isBuiltIn: Boolean
)

private fun listModelGroups(context: Context): List<ModelGroup> {
    val dir = TaggerEngine.modelDirectory(context)
    val allFiles = dir.listFiles()?.toList() ?: emptyList()
    // 按 nameWithoutExtension 分组（onnx + tag 标签文件同名归一组）
    val grouped: List<ModelGroup> = allFiles
        .filter { it.isFile && it.extension.lowercase() in setOf("onnx", "csv", "json", "txt") }
        .groupBy { it.nameWithoutExtension }
        .map { (name, files) ->
            val onnxFile = files.firstOrNull { it.extension.equals("onnx", ignoreCase = true) }
            val id = onnxFile?.absolutePath ?: files.first().absolutePath
            val sortedFiles = files.sortedByDescending { it.length() }
            val total: Long = sortedFiles.fold(0L) { acc, f -> acc + f.length() }
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
                isBuiltIn = false
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
                isBuiltIn = true
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
                            "${group.files.size} 个文件 · ${formatFileSize(group.totalSizeBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Text(stringResource(R.string.file_manager_delete_group), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
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
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
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
                                    OutlinedButton(
                                        onClick = onCopyOptimized,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(stringResource(R.string.compare_copy))
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
 * 调用 MyMemory 免费翻译 API 批量翻译标签。
 * 一次翻译一个标签，逐个累加结果，避免超长请求被拒绝。
 *
 * 标签格式处理：
 * - 下划线 `long_hair` → 空格 `long hair` 再翻译，翻译后保留原下划线版作为显示来源
 * - 连字符 `flat-chest` → 空格再翻译
 * - 数字开头 `1girl`、`2boys` → 数字保留，文字部分单独翻译
 * - 括号权重 `1girl, (masterpiece:1.2)` → 标签本身不含权重符号（WD tagger 输出纯标签名）
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
    for (tag in tags) {
        val translated = translateSingleTag(tag, langCode)
        results.add(tag to translated)
    }
    return results
}

/**
 * 翻译单个标签。
 * 对带下划线/连字符的标签，转换为空格后再翻译。
 * 对数字开头的标签（1girl、2boys），分离数字和文字分别翻译。
 */
private fun translateSingleTag(tag: String, langCode: String): String {
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
    return try {
        val encoded = java.net.URLEncoder.encode(text, "UTF-8")
        val url = java.net.URL("https://api.mymemory.translated.net/get?q=$encoded&langpair=en|$langCode")
        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("User-Agent", "LocalCueWord/1.0 (Android)")
            instanceFollowRedirects = true
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) return text
            conn.inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                val translated = JSONObject(response)
                    .optJSONObject("responseData")
                    ?.optString("translatedText")
                    ?.takeIf { it.isNotBlank() }
                translated?.let(::decodeUnicode)?.takeIf { it != text } ?: text
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
        if (s.contains("\\u")) {
            val sb = StringBuilder()
            var i = 0
            while (i < s.length) {
                if (i + 6 <= s.length && s[i] == '\\' && s[i + 1] == 'u') {
                    val hex = s.substring(i + 2, i + 6)
                    sb.append(hex.toInt(16).toChar())
                    i += 6
                } else {
                    sb.append(s[i])
                    i++
                }
            }
            sb.toString()
        } else {
            s
        }
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
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
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
                            if (item.success && item.tags.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        copyTextToClipboard(context, item.tags.toTagText(), "batch_result")
                                        Toast.makeText(context, context.getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
                    if (successCount > 0) {
                        TextButton(onClick = {
                            val allTags = results.filter { it.success }.flatMap { it.tags }.toTagText()
                            copyTextToClipboard(context, allTags, "batch_all")
                            Toast.makeText(context, context.getString(R.string.batch_copied_all, successCount), Toast.LENGTH_LONG).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.batch_copy_all))
                        }
                    }
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) }
                }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.batch_stop)) }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AppearanceSettingsCard(
    useDynamicColor: Boolean,
    isIos27Style: Boolean,
    themeStyle: String,
    monetPalette: String,
    customBackgroundImagePath: String,
    customBackgroundOpacity: Float,
    customBackgroundDimAmount: Float,
    liquidGlassEnabled: Boolean,
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
    onLiquidGlassEnabledChange: (Boolean) -> Unit,
    onHeroSubtitleModeChange: (String) -> Unit,
    onHeroCustomSubtitleChange: (String) -> Unit,
    onHeroSubtitleFontSizeChange: (Int) -> Unit,
    onDarkModeChange: (String) -> Unit
) {
    val isCustomBackgroundStyle = themeStyle == THEME_STYLE_CUSTOM_BACKGROUND
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = glassCardColors(isIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(isIos27Style, 30)
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
                        value = THEME_STYLE_IOS27,
                        current = themeStyle,
                        label = stringResource(R.string.settings_theme_style_ios27_short),
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f))
                    // 液态玻璃开关
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
                                stringResource(R.string.settings_liquid_glass_toggle),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.settings_liquid_glass_toggle_summary),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Switch(
                            checked = liquidGlassEnabled,
                            onCheckedChange = onLiquidGlassEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                        )
                    }
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
                    maxItemsInEachRow = if (isIos27Style) 4 else 6
                ) {
                    if (isIos27Style) {
                        PaletteBlobOption(
                            value = MONET_PALETTE_DEVICE,
                            current = monetPalette,
                            label = stringResource(R.string.settings_minimal_palette_native),
                            colors = listOf(Color(0xFF0A84FF), Color(0xFF64D2FF), Color(0xFFFFFFFF)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_DEEP_BLUE,
                            current = monetPalette,
                            label = stringResource(R.string.settings_minimal_palette_deep_blue),
                            colors = listOf(Color(0xFF003A8C), Color(0xFF0057D9), Color(0xFF4DA3FF)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_LAVA_ORANGE,
                            current = monetPalette,
                            label = stringResource(R.string.settings_minimal_palette_lava_orange),
                            colors = listOf(Color(0xFFFF3B30), Color(0xFFFF5A1F), Color(0xFFFF9F0A)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                        PaletteBlobOption(
                            value = MONET_PALETTE_SWEET_PINK,
                            current = monetPalette,
                            label = stringResource(R.string.settings_minimal_palette_sweet_pink),
                            colors = listOf(Color(0xFFFF2D8F), Color(0xFFFF8BD2), Color(0xFFAF52DE)),
                            enabled = true,
                            onSelect = onMonetPaletteChange
                        )
                    } else {
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
    onLanguageChange: (String) -> Unit,
    isIos27Style: Boolean
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = glassCardColors(isIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(isIos27Style, 26)
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
    onInferencePerfModeChange: (String) -> Unit
) {
    val isIos27Style = themeStyle == THEME_STYLE_IOS27
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = glassCardColors(isIos27Style),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassBorder(isIos27Style, 26)
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
                            selected && isIos27Style -> blendColor(liquidGlassSurfaceColor(), MaterialTheme.colorScheme.primaryContainer, 0.38f)
                            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            isIos27Style -> liquidGlassSurfaceColor().copy(alpha = 0.48f)
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
                            .liquidGlassBorder(isIos27Style, 18)
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

            Text(
                stringResource(R.string.settings_prompt_tag_limit_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.settings_prompt_tag_limit_summary, promptTagLimit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = promptTagLimit.toFloat(),
                onValueChange = { onPromptTagLimitChange(it.toInt()) },
                valueRange = MIN_PROMPT_TAG_LIMIT.toFloat()..MAX_PROMPT_TAG_LIMIT.toFloat(),
                steps = MAX_PROMPT_TAG_LIMIT - MIN_PROMPT_TAG_LIMIT - 1,
                colors = themedSliderColors()
            )
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
private fun glassCardColors(enabled: Boolean): CardColors {
    // 有真正的 backdrop 时，卡片背景透明，让 drawBackdrop 的玻璃效果可见
    val backdrop = LocalLiquidGlassBackdrop.current
    if (enabled && backdrop != null && isRenderEffectSupported()) {
        return CardDefaults.cardColors(containerColor = Color.Transparent)
    }
    val container = if (enabled) {
        liquidGlassSurfaceColor()
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f)
    }
    return CardDefaults.cardColors(containerColor = container)
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

@Composable
private fun liquidGlassSurfaceColor(): Color {
    return if (MaterialTheme.colorScheme.background.isVisuallyDark()) {
        Color(0x4D2C2C2E)
    } else {
        Color(0x52FFFFFF)
    }
}

@Composable
private fun liquidGlassDialogColor(): Color {
    return if (MaterialTheme.colorScheme.background.isVisuallyDark()) {
        Color(0x962C2C2E)
    } else {
        Color(0x8AF8FBFF)
    }
}

@Composable
private fun Modifier.liquidGlassBorder(enabled: Boolean, radius: Int): Modifier {
    if (!enabled) return this
    val shape = RoundedCornerShape(radius.dp)
    val backdrop = LocalLiquidGlassBackdrop.current
    // 完全匹配 LiquidGlassWeather 的 GlassCard 模式
    if (backdrop != null && isRenderEffectSupported()) {
        return this.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(10.dp.toPx())
                if (isRuntimeShaderSupported()) {
                    lens(
                        refractionHeight = 14.dp.toPx(),
                        refractionAmount = 32.dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                }
            },
            highlight = { Highlight.Plain }
        )
    }
    return this
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
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(stream, null, options)
            options.inJustDecodeBounds = false
            val maxPx = 2048
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxPx, maxPx)
            BitmapFactory.decodeStream(stream, null, options)
        }
    } catch (e: Exception) {
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
        val needsTags = extension == "onnx" &&
            findMatchingTagFileForImportedModel(targetFile, targetDir.listFiles()?.filter { it.isFile && it.extension.lowercase() in setOf("csv", "json", "txt") }.orEmpty(), targetDir.listFiles()?.filter { it.isFile && it.extension.equals("onnx", ignoreCase = true) }.orEmpty()) == null
        if (needsTags) {
            "${context.getString(R.string.ai_model_import_success, safeName)}\n请继续导入与该模型同源的标签表文件，否则模型不会用于生成提示词。"
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
    val tagsFile = File(targetDir, "${model.repoName}.csv")
    val modelTemp = File(targetDir, "${model.repoName}.onnx.downloading")
    val tagsTemp = File(targetDir, "${model.repoName}.csv.downloading")
    try {
        modelTemp.delete()
        tagsTemp.delete()
        modelFile.delete()
        tagsFile.delete()
        val baseUrl = aiModelDownloadBaseUrl(source, model.repoName)
        downloadUrlToFile("$baseUrl/model.onnx", modelTemp, model.id, "model.onnx", onProgress, isCancelled)
        if (isCancelled()) throw java.io.IOException("cancelled")
        downloadUrlToFile("$baseUrl/selected_tags.csv", tagsTemp, model.id, "selected_tags.csv", onProgress, isCancelled)
        if (isCancelled()) throw java.io.IOException("cancelled")
        onProgress(
            DownloadProgress(
                model.id,
                context.getString(R.string.ai_model_verifying),
                100,
                modelTemp.length() + tagsTemp.length(),
                modelTemp.length() + tagsTemp.length(),
                isVerifying = true
            )
        )
        val modelSize = modelTemp.length()
        val tagsSize = tagsTemp.length()
        android.util.Log.d("AiModelDownload", "Downloaded temp files: model=${modelSize}B, tags=${tagsSize}B, dir=${targetDir.absolutePath}")
        if (modelSize == 0L || tagsSize == 0L) {
            modelTemp.delete()
            tagsTemp.delete()
            return AiModelDownloadResult(false, context.getString(R.string.ai_model_download_failed))
        }
        // renameTo 在跨挂载点时会失败，改用 copyTo + delete 更可靠
        if (!moveFile(modelTemp, modelFile) || !moveFile(tagsTemp, tagsFile)) {
            modelTemp.delete()
            tagsTemp.delete()
            return AiModelDownloadResult(false, context.getString(R.string.ai_model_download_failed))
        }
        // 验证最终文件存在且非空
        if (!modelFile.exists() || modelFile.length() == 0L || !tagsFile.exists() || tagsFile.length() == 0L) {
            return AiModelDownloadResult(false, context.getString(R.string.ai_model_download_failed))
        }
        android.util.Log.d("AiModelDownload", "Final files OK: ${modelFile.name} (${modelFile.length()}B), ${tagsFile.name} (${tagsFile.length()}B)")
        // 扫描确认
        val scanned = TaggerEngine.scanModelConfigs(context)
        android.util.Log.d("AiModelDownload", "Scan after download: ${scanned.size} models found")
        return AiModelDownloadResult(
            success = true,
            message = context.getString(R.string.ai_model_download_success, model.displayName),
            modelId = modelFile.absolutePath
        )
    } catch (e: Exception) {
        android.util.Log.e("AiModelDownload", "Download failed: ${e.message}", e)
        // 取消或失败时，删除所有临时文件和半成品
        modelTemp.delete()
        tagsTemp.delete()
        // 如果是取消，也删掉可能已 move 过去的最终文件（防止半成品残留）
        if (e.message == "cancelled") {
            if (modelFile.exists()) modelFile.delete()
            if (tagsFile.exists()) tagsFile.delete()
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

private fun aiModelDownloadBaseUrl(source: String, repoName: String): String {
    val host = when (source) {
        AI_MODEL_SOURCE_HF_MIRROR -> "https://hf-mirror.com"
        else -> "https://huggingface.co"
    }
    return "$host/SmilingWolf/$repoName/resolve/main"
}

private data class DownloadProbe(
    val url: String,
    val totalBytes: Long,
    val supportsRange: Boolean
)

private const val PARALLEL_DOWNLOAD_THRESHOLD_BYTES = 32L * 1024L * 1024L
private const val PARALLEL_DOWNLOAD_CHUNK_BYTES = 16L * 1024L * 1024L
private const val PARALLEL_DOWNLOAD_MAX_THREADS = 12

private fun downloadUrlToFile(
    url: String,
    targetFile: File,
    modelId: String,
    phase: String,
    onProgress: (DownloadProgress) -> Unit,
    isCancelled: () -> Boolean = { false }
) {
    val probe = probeDownloadTarget(url, isCancelled)
    val shouldUseParallel = phase.endsWith(".onnx") &&
        probe.supportsRange &&
        probe.totalBytes >= PARALLEL_DOWNLOAD_THRESHOLD_BYTES
    if (shouldUseParallel) {
        parallelDownloadUrlToFile(
            url = probe.url,
            targetFile = targetFile,
            modelId = modelId,
            phase = "$phase · ${PARALLEL_DOWNLOAD_MAX_THREADS}线程加速",
            totalLen = probe.totalBytes,
            onProgress = onProgress,
            isCancelled = isCancelled
        )
    } else {
        sequentialDownloadUrlToFile(
            url = probe.url,
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
    var currentUrl = url
    var redirectCount = 0
    val maxRedirects = 20
    var connection: HttpURLConnection? = null
    try {
        while (true) {
            if (isCancelled()) throw java.io.IOException("cancelled")
            connection = openDownloadConnection(currentUrl)
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                connection = null
                if (location.isNullOrBlank()) {
                    throw IllegalStateException("HTTP $code (no Location)")
                }
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
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code")
            }
            break
        }
        val totalLen: Long = connection?.contentLengthLong.let { len ->
            if (len != null && len > 0L) len else knownTotalLen
        } ?: knownTotalLen
        var received = 0L
        connection?.inputStream?.use { rawInput ->
            BufferedInputStream(rawInput, 4 * 1024 * 1024).use { input ->
            BufferedOutputStream(FileOutputStream(targetFile), 4 * 1024 * 1024).use { output ->
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
        connection?.disconnect()
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
    targetFile.parentFile?.mkdirs()
    RandomAccessFile(targetFile, "rw").use { it.setLength(totalLen) }
    val chunkCount = ((totalLen + PARALLEL_DOWNLOAD_CHUNK_BYTES - 1) / PARALLEL_DOWNLOAD_CHUNK_BYTES).toInt()
    val threadCount = min(PARALLEL_DOWNLOAD_MAX_THREADS, max(2, chunkCount))
    val executor = Executors.newFixedThreadPool(threadCount)
    val received = AtomicLong(0L)
    val failed = AtomicBoolean(false)
    val lastProgressAt = AtomicLong(0L)
    val futures = mutableListOf<java.util.concurrent.Future<*>>()
    try {
        for (index in 0 until chunkCount) {
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
            }
        }
        futures.forEach { it.get() }
        onProgress(DownloadProgress(modelId, phase, 100, totalLen, totalLen))
    } catch (e: Exception) {
        failed.set(true)
        futures.forEach { it.cancel(true) }
        targetFile.delete()
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
                connection.disconnect()
                connection = null
                if (location.isNullOrBlank()) throw IllegalStateException("HTTP $code (no Location)")
                redirectCount++
                if (redirectCount > 20) throw IllegalStateException("Too many redirects")
                currentUrl = if (location.startsWith("http")) location else URL(URL(currentUrl), location).toString()
                continue
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
        connection?.disconnect()
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
                connection.disconnect()
                connection = null
                if (location.isNullOrBlank()) throw IllegalStateException("HTTP $code (no Location)")
                redirectCount++
                if (redirectCount > 20) throw IllegalStateException("Too many redirects")
                currentUrl = if (location.startsWith("http")) location else URL(URL(currentUrl), location).toString()
                continue
            }
            if (code !in listOf(200, 206)) {
                throw IllegalStateException("HTTP $code")
            }
            val contentRange = connection.getHeaderField("Content-Range")
            val totalFromRange = contentRange
                ?.substringAfterLast("/", "")
                ?.toLongOrNull()
                ?: -1L
            val supportsRange = code == 206 && totalFromRange > 0L
            val total = if (supportsRange) totalFromRange else connection.contentLengthLong.takeIf { it > 0L } ?: -1L
            connection.inputStream?.close()
            return DownloadProbe(currentUrl, total, supportsRange)
        }
    } finally {
        connection?.disconnect()
    }
}

private fun openDownloadConnection(url: String, range: String? = null): HttpURLConnection {
    return (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 300_000
        instanceFollowRedirects = false
        setRequestProperty("User-Agent", "LocalCueWord/1.0 Android DownloadAccelerator")
        setRequestProperty("Accept", "*/*")
        setRequestProperty("Accept-Encoding", "identity")
        setRequestProperty("Connection", "keep-alive")
        setRequestProperty("Cache-Control", "no-cache")
        range?.let { setRequestProperty("Range", it) }
    }
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

    val quality = (
        pick(5) {
            it.contains("masterpiece") ||
                it.contains("best_quality") ||
                it.contains("highres") ||
                it.contains("absurdres") ||
                it.contains("detailed") ||
                it.contains("beautiful")
        } + listOf("masterpiece", "best quality", "highres")
    ).distinct().take(6)

    val subject = pick(8) {
        it in setOf("solo", "1girl", "1boy", "2girls", "2boys", "multiple_girls", "multiple_boys") ||
            it.contains("girl") ||
            it.contains("boy") ||
            it.contains("animal") ||
            it.contains("cat") ||
            it.contains("dog") ||
            it.contains("chibi")
    }

    val appearance = pick(12) {
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

    val scene = pick(10) {
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

    val action = pick(10) {
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
        .take((safeLimit - usedCount).coerceAtLeast(0))

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
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)
        options.inJustDecodeBounds = false
        val maxPx = 2048
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxPx, maxPx)
        BitmapFactory.decodeFile(path, options)
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
    }.take(HERO_SUBTITLE_MAX_LENGTH)
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
    return runCatching {
        val connection = (URL(CHINESE_POETRY_API_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()
        extractPoetrySubtitleFromJson(body)
    }.getOrDefault("")
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
