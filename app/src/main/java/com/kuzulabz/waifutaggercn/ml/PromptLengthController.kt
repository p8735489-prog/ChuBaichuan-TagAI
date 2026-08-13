package com.kuzulabz.waifutaggercn.ml

/**
 * PromptLengthController — 提示词长度控制
 *
 * 根据标签优先级控制最终输出数量，超限时按优先级删除低价值标签。
 *
 * 数量限制：
 *   - 精准模式（PrecisionMode）：最多 50 个标签
 *   - 普通模式（NormalMode）：最多 30 个标签
 *
 * 优先保留顺序（高 → 低）：
 *   1. 角色名（CHARACTER）
 *   2. 人物外观（发色、瞳色、发型、眼睛样式）
 *   3. 服装（CLOTHES）
 *   4. 配饰（ACCESSORY）
 *   5. 背景（BACKGROUND）
 *   6. 其他通用（GENERAL）— 最后保留
 *
 * 超限时按优先级从低到高删除：
 *   1. 先删普通细节、小装饰（GENERAL + ACCESSORY）
 *   2. 再删背景（BACKGROUND）
 *   3. 再删配饰（ACCESSORY）
 *   4. 再删服装（CLOTHES）
 *   5. 最后删人物外观和角色（HAIR/EYE/CHARACTER）
 */
class PromptLengthController(
    /** 精准模式最大标签数 */
    val precisionModeLimit: Int = 50,
    /** 普通模式最大标签数 */
    val normalModeLimit: Int = 30
) {

    /** 输出模式 */
    enum class OutputMode {
        /** 普通模式：输出融合后的干净 Prompt */
        NORMAL,
        /** 精准模式：更多标签 + 详细分析 */
        PRECISION
    }

    /** 标签优先级（数值越大越优先保留） */
    private fun tagPriority(tagType: TagFilter.TagType): Int = when (tagType) {
        TagFilter.TagType.CHARACTER -> 100
        TagFilter.TagType.HAIR_COLOR -> 90
        TagFilter.TagType.EYE_COLOR -> 85
        TagFilter.TagType.HAIR_STYLE -> 80
        TagFilter.TagType.EYE_STYLE -> 75
        TagFilter.TagType.CLOTHES -> 60
        TagFilter.TagType.ACCESSORY -> 40
        TagFilter.TagType.BACKGROUND -> 30
        TagFilter.TagType.GENERAL -> 10
    }

    /**
     * 控制标签数量，超限时按优先级删除
     *
     * @param tags 已处理的标签列表
     * @param tagFilter 用于分类的 TagFilter 实例
     * @param mode 输出模式
     * @return 裁剪后的标签列表
     */
    fun limitTagCount(
        tags: List<TaggerEngine.Tag>,
        tagFilter: TagFilter,
        mode: OutputMode = OutputMode.NORMAL
    ): List<TaggerEngine.Tag> {
        val limit = when (mode) {
            OutputMode.NORMAL -> normalModeLimit
            OutputMode.PRECISION -> precisionModeLimit
        }

        if (tags.size <= limit) return tags

        // 对每个标签计算优先级（基于分类 + 置信度）
        val prioritized = tags.map { tag ->
            val tagType = tagFilter.classifyTagPublic(tag)
            val priority = tagPriority(tagType)
            // 同类型中，置信度高的优先
            val compositeScore = priority.toFloat() + tag.score
            Triple(tag, tagType, compositeScore)
        }

        // 按综合分数降序排列，取前 limit 个
        val kept = prioritized
            .sortedByDescending { it.third }
            .take(limit)
            .map { it.first }

        // 保持原始顺序（按 score 降序）
        val keptSet = kept.map { it.name.lowercase() }.toSet()
        return tags.filter { it.name.lowercase() in keptSet }
    }

    /**
     * 控制标签数量（简化版，不需要 TagFilter 分类）
     * 仅按 score 降序取前 limit 个
     *
     * @param tags 已处理的标签列表
     * @param mode 输出模式
     * @return 裁剪后的标签列表
     */
    fun limitByScore(
        tags: List<TaggerEngine.Tag>,
        mode: OutputMode = OutputMode.NORMAL
    ): List<TaggerEngine.Tag> {
        val limit = when (mode) {
            OutputMode.NORMAL -> normalModeLimit
            OutputMode.PRECISION -> precisionModeLimit
        }
        if (tags.size <= limit) return tags
        return tags
            .sortedWith(compareByDescending<TaggerEngine.Tag> { it.score }.thenBy { it.name.lowercase() })
            .take(limit)
    }
}
