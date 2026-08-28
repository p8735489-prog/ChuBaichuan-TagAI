package com.kuzulabz.waifutaggercn.ml.adapters

/** PixAI v0.9: official category thresholds, general 0.30 / character 0.85. */
class PixAIV09Adapter : TaggerAdapter {
    override val modelName = "PixAI Tagger v0.9"
    override val labelCount = 13461

    override fun selectOutput(
        candidates: List<Pair<String, FloatArray>>,
        tagCount: Int
    ): FloatArray? =
        candidates.firstOrNull { it.second.size == labelCount }?.second
            ?: candidates.firstOrNull { it.second.size == tagCount }?.second

    override fun threshold(category: Int, fallback: Float): Float =
        when (category) {
            0 -> 0.30f
            4 -> 0.85f
            else -> fallback
        }

    override fun precisionThreshold(category: Int, fallback: Float): Float =
        when (category) {
            0 -> 0.40f
            4 -> 0.90f
            else -> fallback
        }
}
