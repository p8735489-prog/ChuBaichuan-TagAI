package com.kuzulabz.waifutaggercn.ui.recommend

import java.util.Locale

/**
 * 推荐 API 配置。
 *
 * 根据用户区域自动选择数据源：
 * - 中国大陆 → Waifu.im API（免费，国内可直连，二次元图片+标签，无需 Key）
 * - 海外      → Civitai API（免费，AI 作品社区，含提示词数据）
 *
 * 2026年4月 Civitai 域名变更：
 * - civitai.com → civitai.red（NSFW 门户）
 * - civitai.green → civitai.com（SFW 门户，主站）
 */
object RecommendApiConfig {

    // ═══════════════════════════════════════════════════════════════
    // 区域检测
    // ═══════════════════════════════════════════════════════════════

    /**
     * 判断当前用户是否在中国大陆。
     * 通过系统语言/地区判断：简体中文 + 中国地区 = 国内用户。
     */
    fun isChinaRegion(): Boolean {
        val locale = Locale.getDefault()
        val language = locale.language.lowercase()
        val country = locale.country.uppercase()
        // 简体中文 + 中国地区
        if (language == "zh" && (country == "CN" || country == "")) return true
        // 繁体中文但地区是中国
        if (language == "zh" && country == "CN") return true
        return false
    }

    /** 当前使用的 API 来源类型 */
    enum class ApiSource { CIVITAI, WAIFU_IM }

    /** 根据区域获取当前 API 来源 */
    val currentSource: ApiSource
        get() = if (isChinaRegion()) ApiSource.WAIFU_IM else ApiSource.CIVITAI

    // ═══════════════════════════════════════════════════════════════
    // Civitai API（国际用户）
    // ═══════════════════════════════════════════════════════════════

    // API 主机列表（按优先级排序，依次尝试）
    val civitaiApiHosts: List<String> = listOf(
        "civitai.com",
        "civitai.green"
    )

    // 根据 API 主机获取对应的 CDN 主机
    fun getCivitaiCdnHost(apiHost: String): String = when (apiHost) {
        "civitai.com" -> "image.civitai.com"
        "civitai.green" -> "image.civitai.com"
        else -> "image.civitai.com"
    }

    // 需要重写的官方 CDN 域名列表
    val officialCivitaiCdnHosts: List<String> = listOf(
        "image.civitai.com",
        "civitai.com"
    )

    // ═══════════════════════════════════════════════════════════════
    // Waifu.im API（国内用户）
    // ═══════════════════════════════════════════════════════════════

    /**
     * Waifu.im — 免费二次元动漫图片 API。
     * 无需 API Key，4000+ 张精选图片（来源 Pixiv），带标签系统。
     * 文档：https://docs.waifu.im/
     * API 端点：https://api.waifu.im/images
     *
     * 特点：
     * - 完全免费，无需注册/Key
     * - 图片来源 Pixiv，纯二次元
     * - 每张图片有标签（tags）、画师（artists）、来源链接
     * - 默认 SFW（全年龄），通过 IsNsfw 参数控制
     * - 支持标签过滤、分页、排序
     */

    /** Waifu.im API 基础地址 */
    const val WAIFU_IM_API_URL = "https://api.waifu.im/images"

    // 国内推荐使用的标签（二次元常用标签，轮换获取多样化内容）
    // 注意：Waifu.im 使用 IncludedTags 参数（AND 逻辑），每次用 1-2 个标签
    val waifuImSearchTags: List<String> = listOf(
        "waifu",
        "uniform",
        "maid",
        "smile",
        "blush",
        "long-hair",
        "school-uniform",
        "kimono",
        "swimsuit",
        "cat-girl",
        "ponytail",
        "glasses",
        "twintails",
        "ribbon",
        "flower"
    )

    // 排除的标签（避免不合适的内容）
    val waifuImExcludedTags: List<String> = listOf(
        "nude",
        "nsfw",
        "hentai",
        "lingerie",
        "bikini"
    )

    // ═══════════════════════════════════════════════════════════════
    // 兼容旧代码
    // ═══════════════════════════════════════════════════════════════

    @Deprecated("使用 civitaiApiHosts 替代")
    val apiHosts: List<String> get() = civitaiApiHosts

    @Deprecated("使用 getCivitaiCdnHost 替代")
    fun getCdnHost(apiHost: String): String = getCivitaiCdnHost(apiHost)

    @Deprecated("使用 officialCivitaiCdnHosts 替代")
    val officialCdnHosts: List<String> get() = officialCivitaiCdnHosts

    val cdnHost: String get() = "image.civitai.com"
    val isUsingMirror: Boolean get() = false
}