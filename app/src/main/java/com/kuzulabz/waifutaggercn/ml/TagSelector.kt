package com.kuzulabz.waifutaggercn.ml

/**
 * TagSelector — 智能 Tag 上限筛选器
 *
 * 不再简单按照顺序截断 Tag，而是根据以下五个维度进行智能筛选：
 *   1. 置信度 (confidence) — 模型给出的 score
 *   2. 模型反馈 (modelFeedback) — 多模型交叉验证时的共识度
 *   3. Tag 重要性 (importance) — 基于标签类别的优先级
 *   4. 相似度 (similarity) — 语义相近的标签去重
 *   5. 冗余程度 (redundancy) — 同类标签过多时只保留最佳的
 *
 * 最终综合得分 = confidence * w1 + modelFeedback * w2 + importance * w3 - similarityPenalty - redundancyPenalty
 */
class TagSelector(
    private val tagFilter: TagFilter = TagFilter()
) {

    /** 标签来源标记 */
    enum class TagSource {
        GLOBAL,      // 整图识别
        LOCAL,       // 局部裁剪识别
        FUSED        // 融合后
    }

    /** 带元数据的标签 */
    data class AnnotatedTag(
        val tag: TaggerEngine.Tag,
        val source: TagSource = TagSource.FUSED,
        val modelCount: Int = 1,           // 多少个模型识别到了此标签
        val avgScore: Float = tag.score,    // 多模型平均分
        val isUncertain: Boolean = false    // 是否标记为不确定
    )

    /** 权重配置 */
    data class SelectionConfig(
        val confidenceWeight: Float = 0.35f,
        val modelFeedbackWeight: Float = 0.20f,
        val importanceWeight: Float = 0.25f,
        val similarityThreshold: Float = 0.75f,  // 相似度超过此值视为冗余
        val maxPerCategory: Int = 8,              // 同类标签最多保留数
        val uncertainScoreThreshold: Float = 0.35f  // 低于此分的标记为 UNCERTAIN
    )

    /** 标签重要性映射 */
    private fun tagImportance(tagName: String, tagType: TagFilter.TagType): Float {
        val baseImportance = when (tagType) {
            TagFilter.TagType.CHARACTER -> 1.0f
            TagFilter.TagType.HAIR_COLOR -> 0.90f
            TagFilter.TagType.EYE_COLOR -> 0.85f
            TagFilter.TagType.HAIR_STYLE -> 0.80f
            TagFilter.TagType.EYE_STYLE -> 0.75f
            TagFilter.TagType.CLOTHES -> 0.65f
            TagFilter.TagType.ACCESSORY -> 0.45f
            TagFilter.TagType.BACKGROUND -> 0.35f
            TagFilter.TagType.GENERAL -> 0.20f
        }
        // 高价值关键词额外加分
        val name = tagName.lowercase()
        val bonus = when {
            name in HIGH_VALUE_TAGS -> 0.15f
            name.contains("masterpiece") || name.contains("best_quality") -> 0.20f
            name.contains("1girl") || name.contains("1boy") || name.contains("solo") -> 0.12f
            else -> 0f
        }
        return (baseImportance + bonus).coerceAtMost(1.0f)
    }

    /** 高价值标签集合 */
    private val HIGH_VALUE_TAGS = setOf(
        "1girl", "1boy", "solo", "multiple_girls", "multiple_boys",
        "masterpiece", "best_quality", "highres", "absurdres"
    )

    /**
     * 计算两个标签名之间的语义相似度（基于字符级编辑距离的归一化）
     * 返回 0.0 ~ 1.0，1.0 表示完全相同
     */
    private fun tagSimilarity(a: String, b: String): Float {
        if (a == b) return 1.0f
        val na = a.lowercase().replace("_", "")
        val nb = b.lowercase().replace("_", "")
        if (na == nb) return 0.95f
        if (na.contains(nb) || nb.contains(na)) return 0.85f
        // 共同前缀比例
        val commonPrefix = na.commonPrefixWith(nb).length
        val maxLen = maxOf(na.length, nb.length)
        if (maxLen == 0) return 0f
        val prefixRatio = commonPrefix.toFloat() / maxLen
        if (prefixRatio > 0.7f) return prefixRatio
        return 0f
    }

    /**
     * 智能筛选标签
     *
     * @param tags 已过滤的标签列表
     * @param limit 最大保留数量
     * @param config 筛选配置
     * @return 智能筛选后的标签列表
     */
    fun select(
        tags: List<TaggerEngine.Tag>,
        limit: Int,
        config: SelectionConfig = SelectionConfig()
    ): List<TaggerEngine.Tag> {
        if (tags.size <= limit) return tags

        // Step 1: 标注每个标签
        val annotated = tags.map { tag ->
            val tagType = tagFilter.classifyTagPublic(tag)
            val importance = tagImportance(tag.name, tagType)
            val isUncertain = tag.score < config.uncertainScoreThreshold
            AnnotatedTag(
                tag = tag,
                source = TagSource.FUSED,
                modelCount = 1,
                avgScore = tag.score,
                isUncertain = isUncertain
            )
        }

        // Step 2: UNCERTAIN 过滤 — 标记为不确定的低分标签优先剔除
        val certainTags = annotated.filterNot { it.isUncertain && annotated.size > limit }

        // Step 3: 计算综合得分
        val scored = certainTags.map { at ->
            val confidence = at.tag.score.coerceIn(0f, 1f)
            val modelFeedback = (at.modelCount.toFloat() / 5f).coerceIn(0f, 1f) // 假设最多5个模型
            val importance = tagImportance(at.tag.name, tagFilter.classifyTagPublic(at.tag))

            val compositeScore = confidence * config.confidenceWeight +
                modelFeedback * config.modelFeedbackWeight +
                importance * config.importanceWeight

            ScoredTag(at, compositeScore, tagFilter.classifyTagPublic(at.tag))
        }

        // Step 4: 相似度去重 — 语义相近的标签只保留得分最高的
        val deduplicated = mutableListOf<ScoredTag>()
        val toRemove = mutableSetOf<Int>()

        for (i in scored.indices) {
            if (i in toRemove) continue
            for (j in (i + 1) until scored.size) {
                if (j in toRemove) continue
                val sim = tagSimilarity(scored[i].at.tag.name, scored[j].at.tag.name)
                if (sim >= config.similarityThreshold) {
                    // 保留得分高的，移除得分低的
                    if (scored[i].compositeScore >= scored[j].compositeScore) {
                        toRemove.add(j)
                    } else {
                        toRemove.add(i)
                        break
                    }
                }
            }
        }
        scored.forEachIndexed { idx, st ->
            if (idx !in toRemove) deduplicated.add(st)
        }

        // Step 5: 冗余控制 — 同类标签最多保留 maxPerCategory 个
        val byCategory = deduplicated.groupBy { it.tagType }
        val capped = mutableListOf<ScoredTag>()
        for ((_, group) in byCategory) {
            capped.addAll(group.sortedByDescending { it.compositeScore }.take(config.maxPerCategory))
        }

        // Step 6: 按综合得分降序取前 limit 个，再按原始 score 排序输出
        val finalSelection = capped
            .sortedByDescending { it.compositeScore }
            .take(limit)
            .map { it.at.tag }
            .sortedByDescending { it.score }

        return finalSelection
    }

    private data class ScoredTag(
        val at: AnnotatedTag,
        val compositeScore: Float,
        val tagType: TagFilter.TagType
    )
}
