package com.kuzulabz.waifutaggercn.ml

/**
 * TagOutputFormatter — 标签输出格式化
 *
 * 支持两种输出模式：
 *
 * 1. 普通模式（NORMAL）：
 *    输出融合后的干净 Prompt，所有标签用逗号连接
 *    例：blue_hair, red_eyes, school_uniform, smile
 *
 * 2. 角色模式（CHARACTER）：
 *    每个角色独立显示标签，用于多人图片分析
 *    例：
 *      Character 1 (Main, Score: 0.85):
 *        blue_hair, red_eyes, school_uniform
 *      Character 2 (Score: 0.62):
 *        red_hair, green_eyes, dress
 *      Background:
 *        outdoors, sky
 *
 * 还提供调试信息格式化功能
 */
class TagOutputFormatter {

    /** 输出模式 */
    enum class FormatMode {
        /** 普通模式：融合后的干净 Prompt */
        NORMAL,
        /** 角色模式：每个角色独立显示 */
        CHARACTER
    }

    /**
     * 格式化标签列表为普通模式文本
     *
     * @param tags 标签列表
     * @return 逗号分隔的标签文本
     */
    fun formatNormal(tags: List<TaggerEngine.Tag>): String {
        return tags.joinToString(", ") { it.name }
    }

    /**
     * 格式化为角色模式文本
     *
     * @param characters 角色实例列表
     * @param backgroundTags 背景标签
     * @return 格式化的角色模式文本
     */
    fun formatCharacter(
        characters: List<CharacterInstance>,
        backgroundTags: List<TaggerEngine.Tag> = emptyList()
    ): String {
        if (characters.isEmpty()) return formatNormal(backgroundTags)

        return buildString {
            characters.forEach { char ->
                val roleLabel = if (char.isMain) "(Main)" else ""
                appendLine("Character ${char.id + 1} $roleLabel Score: ${"%.2f".format(char.score)}:")

                if (char.filteredTags.isNotEmpty()) {
                    val tagText = char.filteredTags.joinToString(", ") { it.name }
                    appendLine("  $tagText")
                } else {
                    appendLine("  (no tags)")
                }
                appendLine()
            }

            if (backgroundTags.isNotEmpty()) {
                appendLine("Background:")
                val bgText = backgroundTags.joinToString(", ") { it.name }
                appendLine("  $bgText")
            }
        }.trimEnd()
    }

    /**
     * 格式化调试信息
     *
     * @param characters 角色实例列表
     * @param totalTagsBeforeFilter 过滤前总标签数
     * @param totalTagsAfterFilter 过滤后总标签数
     * @param pipeline 使用的流水线名称
     * @return 调试信息文本
     */
    fun formatDebug(
        characters: List<CharacterInstance>,
        totalTagsBeforeFilter: Int,
        totalTagsAfterFilter: Int,
        pipeline: String
    ): String {
        return buildString {
            appendLine("═══ Debug Info ═══")
            appendLine("Pipeline: $pipeline")
            appendLine("Character Count: ${characters.size}")
            appendLine("Tags (before filter): $totalTagsBeforeFilter")
            appendLine("Tags (after filter): $totalTagsAfterFilter")
            appendLine()

            characters.forEach { char ->
                appendLine(char.debugSummary())
            }

            appendLine("═══ End Debug ═══")
        }.trimEnd()
    }

    /**
     * 将标签列表转换为带权重的调试文本
     */
    fun formatTagsWithScores(tags: List<TaggerEngine.Tag>): String {
        return tags.joinToString("\n") { tag ->
            "  ${tag.name} (score: ${"%.3f".format(tag.score)}, cat: ${tag.category})"
        }
    }
}
