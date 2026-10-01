package com.kuzulabz.waifutaggercn.ml

/**
 * PromptWeightMapper — 根据标签置信度自动生成带权重的提示词
 *
 * 支持两种模式：
 *   - ENHANCE_ONLY：仅对高置信度标签（>= minConfidence）添加增强权重
 *   - FULL：对所有超过 minConfidence 的标签添加权重标注
 *   - OFF：关闭权重，输出纯文本标签
 *
 * 输出格式为 WebUI 兼容语法： (tag:1.15)
 */
class PromptWeightMapper {

    enum class PromptWeightMode {
        OFF,
        ENHANCE_ONLY,
        FULL
    }

    data class PromptWeightConfig(
        val enabled: Boolean,
        val mode: PromptWeightMode,
        val strength: Float,
        val minConfidence: Float,
        val maxWeight: Float
    ) {
        companion object {
            fun sanitized(
                enabled: Boolean,
                mode: PromptWeightMode,
                strength: Float,
                minConfidence: Float,
                maxWeight: Float
            ): PromptWeightConfig {
                return PromptWeightConfig(
                    enabled = enabled,
                    mode = mode,
                    strength = strength.coerceIn(0f, 1f),
                    minConfidence = minConfidence.coerceIn(0f, 1f),
                    maxWeight = maxWeight.coerceIn(1f, 1.5f)
                )
            }
        }
    }

    /**
     * 将标签列表转换为带权重的提示词字符串
     *
     * @param tags 已排序的标签列表
     * @param config 权重配置
     * @return 格式化后的提示词，如 "(1girl:1.12), (long_hair:1.08), smile"
     */
    fun applyWeights(
        tags: List<TaggerEngine.Tag>,
        config: PromptWeightConfig
    ): String {
        if (tags.isEmpty()) return ""
        if (!config.enabled || config.mode == PromptWeightMode.OFF) {
            return tags.joinToString(", ") { it.name }
        }

        return tags.joinToString(", ") { tag ->
            val weight = calculateWeight(tag.score, config)
            if (weight > 1.001f) {
                "(${tag.name}:${String.format("%.2f", weight)})"
            } else {
                tag.name
            }
        }
    }

    /**
     * 计算单个标签的权重
     *
     * 权重映射逻辑：
     *   - score < minConfidence → 1.0（不加权）
     *   - score >= minConfidence → 1.0 + (score - minConfidence) / (1 - minConfidence) * (maxWeight - 1) * strength
     *
     * ENHANCE_ONLY 模式下，只有 score >= 0.7 的标签才会被增强
     * FULL 模式下，所有 score >= minConfidence 的标签都会被加权
     */
    private fun calculateWeight(score: Float, config: PromptWeightConfig): Float {
        val effectiveThreshold = when (config.mode) {
            PromptWeightMode.ENHANCE_ONLY -> maxOf(config.minConfidence, 0.7f)
            PromptWeightMode.FULL -> config.minConfidence
            PromptWeightMode.OFF -> return 1.0f
        }

        if (score < effectiveThreshold) return 1.0f

        val range = (1.0f - effectiveThreshold).coerceAtLeast(0.001f)
        val normalized = ((score - effectiveThreshold) / range).coerceIn(0f, 1f)
        val weight = 1.0f + normalized * (config.maxWeight - 1.0f) * config.strength

        return weight.coerceIn(1.0f, config.maxWeight)
    }
}
