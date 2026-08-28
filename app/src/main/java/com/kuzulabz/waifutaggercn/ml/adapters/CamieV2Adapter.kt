package com.kuzulabz.waifutaggercn.ml.adapters

/** Camie v2: refined_predictions is the authoritative 70,527-label output. */
class CamieV2Adapter : TaggerAdapter {
    override val modelName = "Camie Tagger v2"
    override val labelCount = 70527

    override fun precisionThreshold(category: Int, fallback: Float): Float = when (category) {
        0 -> 0.90f
        else -> fallback
    }

    override fun selectOutput(
        candidates: List<Pair<String, FloatArray>>,
        tagCount: Int
    ): FloatArray? =
        candidates.firstOrNull {
            it.first.contains("refined_predictions", ignoreCase = true) &&
                it.second.size == labelCount
        }?.second
            ?: candidates.firstOrNull {
                it.first.contains("refined", ignoreCase = true) &&
                    it.second.size == tagCount
            }?.second
            ?: candidates.firstOrNull {
                it.second.size == labelCount
            }?.second
}
