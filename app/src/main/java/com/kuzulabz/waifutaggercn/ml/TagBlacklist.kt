package com.kuzulabz.waifutaggercn.ml

/**
 * TagBlacklist — 低价值标签过滤
 *
 * 过滤无意义、低信息量的标签，避免输出无意义关键词。
 *
 * 过滤类别：
 * 1. 精确匹配黑名单 — 标签名完全匹配时过滤
 * 2. 前缀黑名单 — 标签名以指定前缀开头时过滤
 * 3. 后缀黑名单 — 标签名以指定后缀结尾时过滤
 * 4. 低置信度过滤 — 置信度低于阈值的标签过滤
 * 5. 低信息量过滤 — 过于宽泛、无描述价值的标签
 */
class TagBlacklist(
    /** 低置信度阈值，低于此值的标签被过滤 */
    private val minConfidence: Float = 0.35f
) {

    /** 精确匹配黑名单 — 这些标签名直接过滤 */
    private val exactBlacklist = setOf(
        // 无意义/未知标签
        "unknown", "general", "duplicate", "other", "misc", "miscellaneous",
        "null", "none", "n/a", "unidentified", "unrecognized",
        "tagme", "request_tag", "bad_tag", "tagme_needed",
        // 过于宽泛、无描述价值
        "image", "photo", "picture", "screenshot", "scan", "scan_artifact",
        "art", "artwork", "drawing", "illustration", "fanart", "sketch",
        "color", "colored", "black_and_white", "monochrome", "greyscale",
        "highres", "lowres", "hi_res", "low_res", "wallpaper",
        "text", "logo", "watermark", "signature", "artist_name",
        "border", "frame", "crop", "cropped", "thumbnail",
        "jpeg_artifacts", "compression_artifacts", "pixel_art",
        "blurry", "blurry_background", "depth_of_field",
        // 常见噪声标签
        "1girl", "2girls", "3girls", "4girls", "5girls", "6+girls",
        "1boy", "2boys", "3boys", "4boys", "5boys", "6+boys",
        "1other", "multiple_girls", "multiple_boys", "6+boys",
        "solo", "duo", "group", "crowd", "multiple_boys", "multiple_girls",
        // WD Tagger 元标签
        "rating_general", "rating_sensitive", "rating_questionable", "rating_explicit",
        "rating_safe", "rating_e"
    )

    /** 前缀黑名单 — 以这些前缀开头的标签过滤 */
    private val prefixBlacklist = setOf(
        "artist:", "character:", "copyright:", "meta:",
        "rating:", "approver:", "commentary:", "source:",
        "translated_", "tagme_"
    )

    /** 后缀黑名单 — 以这些后缀结尾的标签过滤 */
    private val suffixBlacklist = setOf(
        "_watermark", "_signature", "_logo", "_border",
        "_text", "_caption", "_subtitle"
    )

    /** 低信息量标签 — 这些标签过于宽泛，不提供有用描述 */
    private val lowInfoTags = setOf(
        "happy", "sad", "angry", "surprised", "neutral",
        "smile", "frown", "open_mouth", "closed_mouth",
        "standing", "sitting", "walking", "running",
        "looking_at_viewer", "looking_away", "looking_back",
        "from_above", "from_below", "from_side", "from_behind",
        "upper_body", "lower_body", "full_body", "portrait",
        "close-up", "dutch_angle", "pov", "first_person_view"
    )

    /**
     * 过滤低价值标签
     *
     * @param tags 原始标签列表
     * @return 过滤后的标签列表
     */
    fun filter(tags: List<TaggerEngine.Tag>): List<TaggerEngine.Tag> {
        return tags.filterNot { tag ->
            val name = tag.name.trim().lowercase()

            // 1. 低置信度过滤
            if (tag.originalScore < minConfidence) return@filterNot true

            // 2. 精确匹配黑名单
            if (name in exactBlacklist) return@filterNot true

            // 3. 前缀黑名单
            if (prefixBlacklist.any { name.startsWith(it) }) return@filterNot true

            // 4. 后缀黑名单
            if (suffixBlacklist.any { name.endsWith(it) }) return@filterNot true

            // 5. 低信息量标签（仅在置信度不高时过滤）
            if (name in lowInfoTags && tag.originalScore < 0.60f) return@filterNot true

            false
        }
    }

    /**
     * 检查单个标签是否为低价值标签
     */
    fun isLowValue(tag: TaggerEngine.Tag): Boolean {
        val name = tag.name.trim().lowercase()
        if (tag.originalScore < minConfidence) return true
        if (name in exactBlacklist) return true
        if (prefixBlacklist.any { name.startsWith(it) }) return true
        if (suffixBlacklist.any { name.endsWith(it) }) return true
        if (name in lowInfoTags && tag.originalScore < 0.60f) return true
        return false
    }
}
