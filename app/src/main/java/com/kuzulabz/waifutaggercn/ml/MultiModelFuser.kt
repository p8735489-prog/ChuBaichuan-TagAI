package com.kuzulabz.waifutaggercn.ml

/**
 * MultiModelFuser — 多模型标签融合器
 *
 * 将多个 Tagger 模型（WD 系列、Camie、PixAI、AnimeTimm 等）的输出进行融合：
 *   1. 标签归一化（统一命名）
 *   2. 交叉验证投票（多模型共识度）
 *   3. 加权置信度融合（按模型精度加权）
 *   4. 冲突检测（不同模型给出矛盾标签时采信高精度模型）
 *   5. UNCERTAIN 过滤（仅一个低精度模型识别到且分数低的标签视为不确定）
 *
 * 融合流程：
 *   各模型输出 → 归一化 → 投票统计 → 加权融合 → 冲突解决 → UNCERTAIN 过滤 → 最终 Tag
 */
class MultiModelFuser {

    /** 单个模型的识别结果 */
    data class ModelResult(
        val modelId: String,
        val modelName: String,
        val accuracyRank: Int,        // 模型精度评级 0-100
        val tags: List<TaggerEngine.Tag>
    )

    /** 融合后的带元数据标签 */
    data class FusedTag(
        val name: String,
        val category: Int,
        val fusedScore: Float,
        val originalScore: Float,
        val modelCount: Int,           // 多少个模型识别到了此标签
        val modelIds: List<String>,    // 哪些模型识别到了
        val consensusScore: Float,     // 共识度 = modelCount / totalModels
        val isUncertain: Boolean       // 是否不确定
    )

    /** 融合配置 */
    data class FusionConfig(
        val minConsensusRatio: Float = 0.2f,     // 最低共识度：低于此值的标签标记为 UNCERTAIN
        val singleModelThreshold: Float = 0.55f,  // 单模型识别时，分数需超过此值才保留
        val conflictResolution: ConflictResolution = ConflictResolution.TRUST_BEST
    )

    enum class ConflictResolution {
        TRUST_BEST,     // 采信精度最高模型的输出
        MAJORITY_VOTE,   // 多数投票
        AVERAGE          // 取平均
    }

    /**
     * 融合多个模型的输出
     *
     * @param results 各模型的识别结果列表
     * @param config 融合配置
     * @return 融合后的标签列表（已按融合分数降序排列）
     */
    fun fuse(
        results: List<ModelResult>,
        config: FusionConfig = FusionConfig()
    ): List<TaggerEngine.Tag> {
        if (results.isEmpty()) return emptyList()
        if (results.size == 1) return results.first().tags

        val totalModels = results.size
        val maxAccuracy = results.maxOf { it.accuracyRank }.coerceAtLeast(1)

        // Step 1: 归一化 + 收集所有标签
        // key = normalized tag name, value = list of (score, category, modelId, accuracyRank)
        val tagMap = mutableMapOf<String, MutableList<TagEntry>>()

        for (result in results) {
            for (tag in result.tags) {
                val normalizedName = normalizeTagName(tag.name)
                val entry = TagEntry(
                    name = tag.name,
                    normalizedName = normalizedName,
                    category = tag.category,
                    score = tag.score,
                    modelId = result.modelId,
                    modelName = result.modelName,
                    accuracyRank = result.accuracyRank,
                    weight = result.accuracyRank.toFloat() / maxAccuracy
                )
                tagMap.getOrPut(normalizedName) { mutableListOf() }.add(entry)
            }
        }

        // Step 2: 投票统计 + 加权融合
        val fusedTags = mutableListOf<FusedTag>()
        for ((normalizedName, entries) in tagMap) {
            val modelCount = entries.map { it.modelId }.distinct().size
            val consensusScore = modelCount.toFloat() / totalModels

            // 加权平均分数（按模型精度加权）
            val totalWeight = entries.sumOf { it.weight.toDouble() }
            val weightedScore = if (totalWeight > 0) {
                (entries.sumOf { it.score.toDouble() * it.weight } / totalWeight).toFloat()
            } else {
                entries.map { it.score }.average().toFloat()
            }

            // 取最高原始分数
            val originalScore = entries.maxOf { it.score }

            // UNCERTAIN 判定：共识度低于阈值 且 (分数低 或 仅一个模型识别)
            val isUncertain = consensusScore < config.minConsensusRatio &&
                (modelCount == 1 && originalScore < config.singleModelThreshold)

            // 冲突检测：同一标签被多个模型给出不同 category
            val categories = entries.map { it.category }.distinct()
            val resolvedCategory = if (categories.size > 1) {
                // 冲突：采信精度最高模型的 category
                entries.maxByOrNull { it.accuracyRank }?.category ?: categories.first()
            } else {
                categories.first()
            }

            fusedTags.add(FusedTag(
                name = entries.maxByOrNull { it.accuracyRank }?.name ?: normalizedName,
                category = resolvedCategory,
                fusedScore = weightedScore,
                originalScore = originalScore,
                modelCount = modelCount,
                modelIds = entries.map { it.modelId }.distinct(),
                consensusScore = consensusScore,
                isUncertain = isUncertain
            ))
        }

        // Step 3: UNCERTAIN 过滤
        val certainTags = fusedTags.filterNot { it.isUncertain }

        // Step 4: 跨模型冲突检测
        // 例如：一个模型说 "blonde_hair"，另一个说 "black_hair" → 采信高精度模型
        val conflictResolved = resolveCrossModelConflicts(certainTags, results, config)

        // Step 5: 转换为 TaggerEngine.Tag 并按融合分数降序排列
        return conflictResolved
            .map { ft ->
                TaggerEngine.Tag(
                    name = ft.name,
                    category = ft.category,
                    score = ft.fusedScore,
                    originalScore = ft.originalScore
                )
            }
            .sortedByDescending { it.score }
    }

    /** 标签名归一化 */
    private fun normalizeTagName(name: String): String {
        return when (name.trim().lowercase()
            .replace(" ", "_")
            .replace("(", "")
            .replace(")", "")) {
            "blue_hair_style", "hair_blue" -> "blue_hair"
            "blackhair" -> "black_hair"
            "whitehair" -> "white_hair"
            "1girl", "female_character", "girl" -> "1girl"
            "solo_character" -> "solo"
            "high_quality", "masterpiece" -> "masterpiece"
            else -> name.trim().lowercase()
                .replace(" ", "_")
                .replace("(", "")
                .replace(")", "")
        }
    }

    /** 跨模型冲突解决 */
    private fun resolveCrossModelConflicts(
        tags: List<FusedTag>,
        results: List<ModelResult>,
        config: FusionConfig
    ): List<FusedTag> {
        // 互斥标签组：同一组内只保留得分最高的
        val mutexGroups = listOf(
            setOf("blonde_hair", "black_hair", "brown_hair", "red_hair", "blue_hair", "pink_hair", "purple_hair", "white_hair", "green_hair", "orange_hair", "silver_hair", "grey_hair"),
            setOf("blue_eyes", "red_eyes", "brown_eyes", "green_eyes", "purple_eyes", "yellow_eyes", "pink_eyes", "orange_eyes", "aqua_eyes", "gray_eyes", "black_eyes"),
            setOf("dress", "school_uniform", "serafuku", "armor", "coat", "kimono", "bikini", "swimsuit"),
            setOf("solo", "multiple_girls", "multiple_boys", "0girls", "0boys")
        )

        val result = mutableListOf<FusedTag>()
        val usedIndices = mutableSetOf<Int>()

        for (group in mutexGroups) {
            val groupTags = tags.mapIndexed { idx, t -> idx to t }
                .filter { (idx, t) -> idx !in usedIndices && normalizeTagName(t.name) in group }

            if (groupTags.size > 1) {
                // 保留得分最高的
                val best = groupTags.maxByOrNull { it.second.fusedScore }
                if (best != null) {
                    groupTags.forEach { (idx, _) ->
                        if (idx != best.first) usedIndices.add(idx)
                    }
                }
            }
        }

        tags.forEachIndexed { idx, t ->
            if (idx !in usedIndices) result.add(t)
        }

        return result
    }

    private data class TagEntry(
        val name: String,
        val normalizedName: String,
        val category: Int,
        val score: Float,
        val modelId: String,
        val modelName: String,
        val accuracyRank: Int,
        val weight: Float
    )
}
