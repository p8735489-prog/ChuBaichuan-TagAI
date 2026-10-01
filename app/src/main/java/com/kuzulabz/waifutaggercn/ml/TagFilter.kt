package com.kuzulabz.waifutaggercn.ml

/**
 * TagFilter — 标签分类、冲突解决与数量限制（第二阶段升级版）
 *
 * 处理流水线：
 *   WD输出 → 黑名单过滤 → 去重 → 分类 → 置信度排序 → 冲突检测 → 数量限制 → 排序 → 输出
 *
 * 第二阶段新增：
 *  - 黑名单过滤（低价值标签）
 *  - 置信度排序步骤
 *  - 服装冲突组（dress vs school_uniform vs armor vs coat 互斥）
 *  - 配饰去重（避免多个蝴蝶结、多个发饰、多个花朵堆积）
 *  - 角色名数量根据实例数动态决定
 */
class TagFilter(
    /** 黑名单过滤器，null 则不启用黑名单过滤 */
    private val blacklist: TagBlacklist? = TagBlacklist()
) {

    // ============================================================
    // 标签类型定义
    // ============================================================

    enum class TagType {
        CHARACTER,     // 角色名（WD Tagger category = 4）
        HAIR_COLOR,    // 头发颜色（互斥：只能有一种发色）
        HAIR_STYLE,    // 发型（非互斥：可同时存在多种发型描述）
        EYE_COLOR,     // 眼睛颜色（互斥：只能有一种瞳色，异色瞳除外）
        EYE_STYLE,     // 眼睛样式（非互斥）
        CLOTHES,       // 主要衣物（限制数量 + 冲突组互斥）
        ACCESSORY,     // 配饰（限制数量 + 同类去重）
        BACKGROUND,    // 背景/场景
        GENERAL        // 其他通用标签
    }

    /** 数量限制配置 */
    data class TagLimits(
        val maxCharacters: Int = 2,
        val maxHairColors: Int = 1,
        val maxEyeColors: Int = 1,
        val maxMainClothes: Int = 2,
        val maxAccessories: Int = 5,
        /** 角色实例数量（用于动态决定角色名保留数） */
        val instanceCount: Int = 0
    )

    /** 标签分类结果 */
    data class ClassifiedTag(
        val tag: TaggerEngine.Tag,
        val type: TagType
    )

    // ============================================================
    // 分类词典
    // ============================================================

    /** 头发颜色关键词（互斥组） */
    private val hairColorTags = setOf(
        "blonde_hair", "black_hair", "brown_hair", "red_hair", "blue_hair",
        "pink_hair", "purple_hair", "white_hair", "green_hair", "silver_hair",
        "grey_hair", "gray_hair", "orange_hair", "aqua_hair", "cyan_hair",
        "magenta_hair", "yellow_hair", "violet_hair", "indigo_hair",
        "light_brown_hair", "dark_brown_hair", "dark_blue_hair", "light_blue_hair",
        "light_pink_hair", "dark_pink_hair", "pale_blue_hair", "pale_pink_hair",
        "platinum_blonde_hair", "strawberry_blonde_hair"
    )

    /** 多色头发（特殊处理：不与单色冲突） */
    private val multiColorHairTags = setOf(
        "multicolored_hair", "gradient_hair", "two-tone_hair", "streaked_hair",
        "split-color_hair", "rainbow_hair"
    )

    /** 发型关键词（非互斥） */
    private val hairStyleKeywords = setOf(
        "long_hair", "short_hair", "very_long_hair", "very_short_hair",
        "medium_hair", "absurdly_long_hair", "ponytail", "twintails",
        "drill_hair", "curly_hair", "straight_hair", "wavy_hair",
        "braided_hair", "braid", "side_braid", "single_braid", "twin_braids",
        "bun", "double_bun", "single_bun", "side_ponytail", "hime_cut",
        "bob_cut", "bob", "ahoge", "sidelocks", "bangs", "blunt_bangs",
        "swept_bangs", "hair_over_one_eye", "hair_over_eyes", "two_side_up",
        "hair_flaps", "hair_bun", "topknot", "chignon", "ringlets",
        "drills", "side_drills", "low_twintails", "high_ponytail",
        "low_ponytail", "messy_hair", "spiked_hair", "wavy_bangs",
        "crossed_bangs", "parted_bangs", "swept_back_hair", "floating_hair",
        "hair_pulled_back", "tied_hair", "loose_hair", "cinna_hair"
    )

    /** 眼睛颜色关键词（互斥组） */
    private val eyeColorTags = setOf(
        "blue_eyes", "red_eyes", "green_eyes", "brown_eyes", "purple_eyes",
        "yellow_eyes", "pink_eyes", "black_eyes", "orange_eyes", "aqua_eyes",
        "grey_eyes", "gray_eyes", "amber_eyes", "gold_eyes", "golden_eyes",
        "silver_eyes", "white_eyes", "violet_eyes", "indigo_eyes",
        "light_blue_eyes", "dark_blue_eyes", "light_brown_eyes", "dark_brown_eyes",
        "light_green_eyes", "dark_green_eyes", "pale_blue_eyes", "pale_red_eyes",
        "crimson_eyes", "scarlet_eyes", "ruby_eyes", "sapphire_eyes",
        "emerald_eyes", "teal_eyes", "cyan_eyes", "magenta_eyes"
    )

    /** 异色瞳（特殊：不与单色冲突） */
    private val heterochromiaTags = setOf(
        "heterochromia", "red_and_blue_eyes", "blue_and_red_eyes",
        "heterochromia_blue_and_red", "different_colored_eyes"
    )

    /** 眼睛样式（非互斥） */
    private val eyeStyleKeywords = setOf(
        "closed_eyes", "half-closed_eyes", "one_eye_closed", "one_eye_open",
        "narrowed_eyes", "wide_eyes", "tearing_up", "crying", "tears",
        "sparkling_eyes", "glowing_eyes", "empty_eyes", "heart-shaped_pupils",
        "star-shaped_pupils", "spiral_pupils", "slit_pupils", "dilated_pupils",
        "constricted_pupils", "under-eye_blush", "eyeshadow", "eyelashes",
        "long_eyelashes", "thick_eyelashes", "mascara", "eyeliner"
    )

    /** 主要衣物（限制数量） */
    private val clothesKeywords = setOf(
        "dress", "shirt", "skirt", "pants", "shorts", "short_shorts",
        "uniform", "school_uniform", "serafuku", "sailor_collar", "blazer",
        "kimono", "yukata", "hoodie", "jacket", "coat", "sweater",
        "cardigan", "blouse", "vest", "leotard", "bodysuit",
        "swimsuit", "bikini", "one-piece_swimsuit", "bra", "panties",
        "lingerie", "underwear", "robe", "cloak", "cape",
        "t-shirt", "tank_top", "crop_top", "tube_top", "turtleneck",
        "poncho", "raincoat", "windbreaker", "pullover", "sailor_dress",
        "cheongsam", "qipao", "hanfu", "ao_dai", "sari",
        "suit", "tuxedo", "waistcoat", "overalls",
        "jumpsuit", "romper", "sundress", "gown", "evening_gown",
        "wedding_dress", "bride_dress", "maid_uniform", "nurse_uniform",
        "miko", "nun", "habit", "apron_dress", "pinafore",
        "military_uniform", "police_uniform", "pilot_suit",
        "lab_coat", "doctor_coat", "gym_uniform", "bloomers",
        "buruma", "sukumizu", "school_swimsuit", "competition_swimsuit",
        "thighhigh_socks", "knee_socks", "pantyhose", "stockings",
        "leggings", "torn_pantyhose", "fishnet_pantyhose"
    )

    /** 衣物前缀/包含匹配 */
    private val clothesContainsKeywords = setOf(
        "_dress", "_shirt", "_skirt", "_uniform", "_coat", "_jacket",
        "_sweater", "_hoodie", "_bikini", "_swimsuit", "_kimono",
        "_yukata", "_leotard", "_blouse", "_cardigan", "_vest",
        "_cloak", "_cape", "_robe", "_gown", "_sari"
    )

    /**
     * 服装冲突组 — 同组内互斥，只保留最高分
     * 例如：dress 和 school_uniform 不能同时出现
     */
    private val clothesConflictGroups = listOf(
        setOf("dress", "school_uniform", "serafuku", "maid_uniform", "miko"),
        setOf("armor", "coat", "jacket", "hoodie", "sweater", "cardigan"),
        setOf("kimono", "yukata", "cheongsam", "qipao", "hanfu", "ao_dai"),
        setOf("bikini", "swimsuit", "one-piece_swimsuit", "school_swimsuit"),
        setOf("suit", "tuxedo", "military_uniform", "police_uniform")
    )

    /** 配饰（限制数量） */
    private val accessoryKeywords = setOf(
        "glasses", "sunglasses", "hat", "cap", "beret", "beanie",
        "ribbon", "hair_ribbon", "hairband", "headband", "headwear",
        "hairpin", "hair_clip", "hairclip", "bow", "hair_bow",
        "earrings", "ear_piercing", "necklace", "choker", "collar",
        "bracelet", "wristband", "ring", "gloves", "fingerless_gloves",
        "mittens", "scarf", "muffler", "tie", "bowtie", "bow_tie",
        "necktie", "headphones", "earphones", "mask", "face_mask",
        "veil", "tiara", "crown", "circlet",
        "hair_ornament", "hair_accessory", "hair_flower", "star_ornament",
        "angel_wings", "demon_wings", "fairy_wings", "insect_wings",
        "wing_hair_ornament", "rabbit_ear_ornament", "cat_ear_ornament",
        "fake_animal_ears", "garter_strap", "garter_belt",
        "suspender", "suspenders", "belt", "apron", "pinafore_apron",
        "shoulder_pad", "epaulette", "badge", "pin", "brooch",
        "pocket_watch", "wristwatch", "watch", "anklet", "ankle_bracelet",
        "nose_ring", "lip_piercing", "eyebrow_piercing", "tongue_piercing",
        "contacts", "colored_contacts", "fake_glasses"
    )

    /** 配饰包含匹配 */
    private val accessoryContainsKeywords = setOf(
        "_hat", "_ribbon", "_glasses", "_glove", "_scarf", "_bowtie",
        "_bow_tie", "_necktie", "_headphone", "_mask", "_veil",
        "_tiara", "_crown", "_earring", "_necklace", "_choker",
        "_bracelet", "_ring", "_pin", "_clip", "_ornament", "_apron"
    )

    /**
     * 配饰去重组 — 同组内只保留最高分，避免堆积
     * 例如：多个蝴蝶结只保留一个，多个发饰只保留一个
     */
    private val accessoryDedupGroups = listOf(
        // 蝴蝶结类
        setOf("bow", "hair_bow", "bowtie", "bow_tie"),
        // 发饰类
        setOf("hair_ornament", "hair_accessory", "hair_clip", "hairclip", "hairpin", "hair_flower"),
        // 花朵类
        setOf("hair_flower", "star_ornament"),
        // 耳饰类
        setOf("earrings", "ear_piercing"),
        // 项链类
        setOf("necklace", "choker", "collar"),
        // 手腕类
        setOf("bracelet", "wristband"),
        // 手套类
        setOf("gloves", "fingerless_gloves", "mittens"),
        // 领带类
        setOf("tie", "necktie", "bowtie", "bow_tie"),
        // 翅膀类
        setOf("angel_wings", "demon_wings", "fairy_wings", "insect_wings"),
        // 手表类
        setOf("pocket_watch", "wristwatch", "watch"),
        // 头戴类
        setOf("hat", "cap", "beret", "beanie", "tiara", "crown", "circlet")
    )

    /** 背景/场景关键词 */
    private val backgroundKeywords = setOf(
        "outdoors", "indoors", "sky", "water", "ocean", "sea", "beach",
        "forest", "mountain", "city", "street", "building", "classroom",
        "bedroom", "kitchen", "bathroom", "living_room", "night", "day",
        "sunset", "sunrise", "clouds", "cloudy_sky", "blue_sky",
        "grass", "flowers", "flower_field", "tree", "rain", "snow",
        "cherry_blossoms", "snowing", "raining", "starry_sky", "moon",
        "sun", "sunlight", "window", "door", "corridor", "hallway",
        "library", "cafe", "restaurant", "shop", "store", "park",
        "garden", "field", "meadow", "lake", "river", "pond",
        "waterfall", "cave", "ruins", "castle", "shrine", "temple",
        "church", "bridge", "dock", "harbor", "port", "alley",
        "rooftop", "balcony", "veranda", "stairs", "elevator",
        "train", "bus", "car", "airplane", "boat", "ship",
        "simple_background", "white_background", "black_background",
        "colored_background", "gradient_background", "blurred_background",
        "abstract_background", "nature", "landscape", "scenery",
        "underwater", "space", "galaxy", "nebula"
    )

    // ============================================================
    // 主处理入口
    // ============================================================

    /**
     * 处理标签流水线：
     * 黑名单过滤 → 去重 → 分类 → 置信度排序 → 冲突检测 → 数量限制 → 排序
     *
     * @param tags 原始标签列表
     * @param limits 数量限制配置
     * @return 处理后的标签列表
     */
    fun process(
        tags: List<TaggerEngine.Tag>,
        limits: TagLimits = TagLimits()
    ): List<TaggerEngine.Tag> {
        if (tags.isEmpty()) return emptyList()

        // Step 0: 黑名单过滤 — 过滤低价值标签
        val blacklisted = blacklist?.filter(tags) ?: tags
        if (blacklisted.isEmpty()) return emptyList()

        // Step 1: 去重 — 同名标签保留最高分
        val deduped = deduplicate(blacklisted)

        // Step 2: 分类
        val classified = deduped.map { ClassifiedTag(it, classifyTag(it)) }

        // Step 3: 置信度排序 — 同类型内按分数降序排列
        val sorted = classified.sortedWith(
            compareByDescending<ClassifiedTag> { it.tag.score }
                .thenBy { it.tag.name.lowercase() }
        )

        // Step 4 & 5: 冲突检测 + 数量限制
        val filtered = applyConflictsAndLimits(sorted, limits)

        // Step 6: 最终排序 — 按分数降序，同分按名称字母序
        return filtered.sortedWith(
            compareByDescending<TaggerEngine.Tag> { it.score }
                .thenBy { it.name.lowercase() }
        )
    }

    /**
     * 多区域标签融合 + 过滤一体化处理（增强版 v3）
     *
     * 增强流程：
     *   GLOBAL 整图识别 + LOCAL 分割识别
     *   → 来源区分（标记每个标签来源）
     *   → 语义风险判断（LOCAL 标签中的高风险身份标签需 GLOBAL 确认）
     *   → 冲突检测（GLOBAL vs LOCAL 矛盾时优先 GLOBAL）
     *   → 可信度融合（加权 max）
     *   → UNCERTAIN 过滤（低置信度且仅来自单一来源的标签标记为不确定并过滤）
     *   → 最终 Tag
     *
     * @param subjectTagsList 多个主体（角色）的标签列表（LOCAL 来源）
     * @param backgroundTags 背景标签（GLOBAL 来源）
     * @param subjectWeight 主体标签权重
     * @param backgroundWeight 背景标签权重
     * @param limits 数量限制
     */
    fun fuseAndFilter(
        subjectTagsList: List<List<TaggerEngine.Tag>>,
        backgroundTags: List<TaggerEngine.Tag>,
        subjectWeight: Float = 1.15f,
        backgroundWeight: Float = 0.85f,
        limits: TagLimits = TagLimits()
    ): List<TaggerEngine.Tag> {
        // === 来源区分 ===
        // LOCAL 标签：来自裁剪区域识别
        // GLOBAL 标签：来自整图识别
        data class SourcedTag(
            val tag: TaggerEngine.Tag,
            val isGlobal: Boolean,
            val sourceCount: Int  // 来自几个区域
        )

        val localTags = mutableMapOf<String, SourcedTag>()
        val globalTags = mutableMapOf<String, SourcedTag>()

        // 收集 LOCAL 标签
        for (subjectTags in subjectTagsList) {
            for (tag in subjectTags) {
                val key = normalizeKey(tag.name)
                val existing = localTags[key]
                if (existing == null) {
                    localTags[key] = SourcedTag(tag, isGlobal = false, sourceCount = 1)
                } else {
                    // 多区域识别到同一标签，取最高分
                    val better = if (tag.score > existing.tag.score) tag else existing.tag
                    localTags[key] = SourcedTag(better, isGlobal = false, sourceCount = existing.sourceCount + 1)
                }
            }
        }

        // 收集 GLOBAL 标签
        for (tag in backgroundTags) {
            val key = normalizeKey(tag.name)
            globalTags[key] = SourcedTag(tag, isGlobal = true, sourceCount = 1)
        }

        // === 语义风险判断 ===
        // 高风险身份标签（角色名、发色、瞳色）如果仅来自 LOCAL 且 LOCAL 分数低，
        // 需要 GLOBAL 确认才能保留，避免局部区域因外观相似产生误判
        val highRiskTypes = setOf(TagType.CHARACTER, TagType.HAIR_COLOR, TagType.EYE_COLOR)
        val semanticRiskTags = mutableSetOf<String>()

        for ((key, sourced) in localTags) {
            val tagType = classifyTag(sourced.tag)
            if (tagType in highRiskTypes && sourced.sourceCount == 1) {
                // 仅来自单一局部区域的高风险标签
                val globalConfirm = globalTags[key]
                if (globalConfirm == null || globalConfirm.tag.score < 0.35f) {
                    // GLOBAL 未确认或确认分数太低 → 标记为语义风险
                    semanticRiskTags.add(key)
                }
            }
        }

        // === 可信度融合 ===
        val merged = linkedMapOf<String, TaggerEngine.Tag>()

        // 先处理 GLOBAL 标签（作为基准）
        for ((key, sourced) in globalTags) {
            val weighted = sourced.tag.copy(score = sourced.tag.score * backgroundWeight)
            merged[key] = weighted
        }

        // 再处理 LOCAL 标签（与 GLOBAL 融合）
        for ((key, sourced) in localTags) {
            val isSemanticRisk = key in semanticRiskTags
            // 语义风险标签的 LOCAL 权重降低
            val effectiveWeight = if (isSemanticRisk) subjectWeight * 0.6f else subjectWeight
            val weighted = sourced.tag.copy(
                score = (sourced.tag.score * effectiveWeight).coerceAtMost(1.5f)
            )
            val existing = merged[key]
            if (existing == null) {
                // 仅 LOCAL 有的标签
                merged[key] = weighted
            } else {
                // GLOBAL 和 LOCAL 都有：冲突检测
                // 如果 GLOBAL 分数明显更高，优先 GLOBAL（防止 LOCAL 污染）
                if (existing.score >= weighted.score * 0.9f) {
                    // GLOBAL 可信度足够，保留 GLOBAL
                    // 不覆盖
                } else {
                    // LOCAL 明显更高，用 LOCAL 覆盖
                    merged[key] = weighted
                }
            }
        }

        // === UNCERTAIN 过滤 ===
        // 标签同时满足以下条件视为 UNCERTAIN 并过滤：
        //   1. 仅来自单一来源（LOCAL only 或 GLOBAL only）
        //   2. 融合后分数低于 0.30
        //   3. 不属于高价值标签（1girl/1boy/solo 等）
        val uncertainThreshold = 0.30f
        val highValueKeys = setOf("1girl", "1boy", "solo", "masterpiece", "best_quality", "highres")

        val certainTags = merged.values.filter { tag ->
            val key = normalizeKey(tag.name)
            val isHighValue = key in highValueKeys
            val isFromBothSources = localTags.containsKey(key) && globalTags.containsKey(key)
            // 保留条件：高价值标签 / 双来源 / 分数足够高
            isHighValue || isFromBothSources || tag.score >= uncertainThreshold
        }

        return process(certainTags, limits)
    }

    /**
     * 对单个角色的标签独立过滤（角色隔离模式）
     * 每个角色的标签独立处理，不与其他角色混合
     *
     * @param tags 单个角色的原始标签
     * @param limits 数量限制（角色模式：单人最多1角色名）
     * @return 过滤后的标签
     */
    fun processSingleCharacter(
        tags: List<TaggerEngine.Tag>,
        limits: TagLimits = TagLimits(maxCharacters = 1)
    ): List<TaggerEngine.Tag> {
        return process(tags, limits)
    }

    // ============================================================
    // Step 0: 黑名单过滤（委托给 TagBlacklist）
    // ============================================================

    // （由构造函数注入的 blacklist 在 process() 中调用）

    // ============================================================
    // Step 1: 去重
    // ============================================================

    private fun deduplicate(tags: List<TaggerEngine.Tag>): List<TaggerEngine.Tag> {
        val merged = linkedMapOf<String, TaggerEngine.Tag>()
        for (tag in tags) {
            val key = normalizeKey(tag.name)
            val existing = merged[key]
            if (existing == null || tag.score > existing.score) {
                merged[key] = tag
            }
        }
        return merged.values.toList()
    }

    // ============================================================
    // Step 2: 分类
    // ============================================================

    /** 对单个标签进行分类（内部使用） */
    private fun classifyTag(tag: TaggerEngine.Tag): TagType {
        val name = normalizeKey(tag.name)

        if (tag.category == 4) return TagType.CHARACTER

        if (name in hairColorTags) return TagType.HAIR_COLOR
        if (name in eyeColorTags) return TagType.EYE_COLOR
        if (name in heterochromiaTags) return TagType.EYE_COLOR
        if (name in multiColorHairTags) return TagType.HAIR_STYLE
        if (name in hairStyleKeywords) return TagType.HAIR_STYLE
        if (name.endsWith("_hair") && name !in hairColorTags) return TagType.HAIR_STYLE
        if (name in eyeStyleKeywords) return TagType.EYE_STYLE
        if (name.endsWith("_eyes") && name !in eyeColorTags) return TagType.EYE_STYLE
        if (name in clothesKeywords) return TagType.CLOTHES
        if (clothesContainsKeywords.any { name.contains(it) }) return TagType.CLOTHES
        if (name in accessoryKeywords) return TagType.ACCESSORY
        if (accessoryContainsKeywords.any { name.contains(it) }) return TagType.ACCESSORY
        if (name in backgroundKeywords) return TagType.BACKGROUND
        if (backgroundKeywords.any { name.contains("_${it}_") || name.startsWith("${it}_") || name == it }) {
            return TagType.BACKGROUND
        }
        return TagType.GENERAL
    }

    /** 公开分类方法，供 PromptLengthController 使用 */
    fun classifyTagPublic(tag: TaggerEngine.Tag): TagType = classifyTag(tag)

    // ============================================================
    // Step 4 & 5: 冲突检测 + 数量限制
    // ============================================================

    private fun applyConflictsAndLimits(
        classified: List<ClassifiedTag>,
        limits: TagLimits
    ): List<TaggerEngine.Tag> {
        val result = mutableListOf<TaggerEngine.Tag>()
        val byType = classified.groupBy { it.type }

        // --- 角色名处理 ---
        // 如果有实例数信息，按实例数决定角色名保留数；否则用 maxCharacters
        val maxChars = if (limits.instanceCount > 0) {
            minOf(limits.instanceCount, limits.maxCharacters)
        } else {
            limits.maxCharacters
        }
        val characters = byType[TagType.CHARACTER]?.sortedByDescending { it.tag.score } ?: emptyList()
        characters.take(maxChars).forEachIndexed { index, ct ->
            val adjusted = if (index == 0) {
                ct.tag
            } else {
                ct.tag.copy(score = ct.tag.score * 0.6f)
            }
            result.add(adjusted)
        }

        // --- 头发颜色（互斥：保留最高分） ---
        val hairColors = byType[TagType.HAIR_COLOR]?.sortedByDescending { it.tag.score } ?: emptyList()
        result.addAll(hairColors.take(limits.maxHairColors).map { it.tag })

        // --- 发型（非互斥：全部保留） ---
        byType[TagType.HAIR_STYLE]?.let { result.addAll(it.map { ct -> ct.tag }) }

        // --- 眼睛颜色（互斥：保留最高分，异色瞳除外） ---
        val eyeColors = byType[TagType.EYE_COLOR]?.sortedByDescending { it.tag.score } ?: emptyList()
        val hasHeterochromia = eyeColors.any { normalizeKey(it.tag.name) in heterochromiaTags }
        val topEyeColors = if (hasHeterochromia) {
            val hetero = eyeColors.filter { normalizeKey(it.tag.name) in heterochromiaTags }
            val single = eyeColors.filter { normalizeKey(it.tag.name) !in heterochromiaTags }
            (hetero + single.take(limits.maxEyeColors - hetero.size.coerceAtMost(limits.maxEyeColors)))
                .take(limits.maxEyeColors + 1)
        } else {
            eyeColors.take(limits.maxEyeColors)
        }
        result.addAll(topEyeColors.map { it.tag })

        // --- 眼睛样式（非互斥：全部保留） ---
        byType[TagType.EYE_STYLE]?.let { result.addAll(it.map { ct -> ct.tag }) }

        // --- 衣物（冲突组互斥 + 数量限制） ---
        val clothes = byType[TagType.CLOTHES]?.sortedByDescending { it.tag.score } ?: emptyList()
        result.addAll(resolveClothesConflicts(clothes, limits.maxMainClothes).map { it.tag })

        // --- 配饰（同类去重 + 数量限制） ---
        val accessories = byType[TagType.ACCESSORY]?.sortedByDescending { it.tag.score } ?: emptyList()
        result.addAll(resolveAccessoryConflicts(accessories, limits.maxAccessories).map { it.tag })

        // --- 背景（全部保留） ---
        byType[TagType.BACKGROUND]?.let { result.addAll(it.map { ct -> ct.tag }) }

        // --- 其他通用标签（全部保留） ---
        byType[TagType.GENERAL]?.let { result.addAll(it.map { ct -> ct.tag }) }

        return result
    }

    /**
     * 服装冲突组解决：同组内只保留最高分
     * 例如：dress 和 school_uniform 不能同时出现
     */
    private fun resolveClothesConflicts(
        clothes: List<ClassifiedTag>,
        maxCount: Int
    ): List<ClassifiedTag> {
        val result = mutableListOf<ClassifiedTag>()
        val usedGroupKeys = mutableSetOf<String>()

        for (ct in clothes) {
            if (result.size >= maxCount) break
            val name = normalizeKey(ct.tag.name)

            // 检查是否在某个冲突组中
            val conflictGroup = clothesConflictGroups.firstOrNull { group -> name in group }

            if (conflictGroup != null) {
                val groupKey = "clothes_${conflictGroup.hashCode()}"
                if (groupKey in usedGroupKeys) continue // 同组已有更高分的，跳过
                usedGroupKeys.add(groupKey)
            }
            result.add(ct)
        }
        return result
    }

    /**
     * 配饰去重：同组内只保留最高分，避免堆积
     * 例如：多个蝴蝶结只保留一个
     */
    private fun resolveAccessoryConflicts(
        accessories: List<ClassifiedTag>,
        maxCount: Int
    ): List<ClassifiedTag> {
        val result = mutableListOf<ClassifiedTag>()
        val usedGroupKeys = mutableSetOf<String>()

        for (ct in accessories) {
            if (result.size >= maxCount) break
            val name = normalizeKey(ct.tag.name)

            // 检查是否在某个去重组中
            val dedupGroup = accessoryDedupGroups.firstOrNull { group -> name in group }

            if (dedupGroup != null) {
                val groupKey = "accessory_${dedupGroup.hashCode()}"
                if (groupKey in usedGroupKeys) continue // 同类已有更高分的，跳过
                usedGroupKeys.add(groupKey)
            }
            result.add(ct)
        }
        return result
    }

    // ============================================================
    // 工具方法
    // ============================================================

    private fun normalizeKey(name: String): String =
        name.trim().lowercase().replace(' ', '_').replace(Regex("_+"), "_")
}
