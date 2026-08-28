package com.kuzulabz.waifutaggercn.ml.adapters

/** AnimeTimm DBv4 ResNet34: 12,476 labels and official category thresholds. */
class AnimeTimmDBv4Adapter : TaggerAdapter {
    override val modelName = "AnimeTimm DBv4 (ResNet34)"
    override val labelCount = 12476

    override fun precisionThreshold(category: Int, fallback: Float): Float = when (category) {
        0 -> 0.55f
        else -> fallback
    }

    override fun selectOutput(
        candidates: List<Pair<String, FloatArray>>,
        tagCount: Int
    ): FloatArray? =
        candidates.firstOrNull { it.second.size == labelCount }?.second
            ?: candidates.firstOrNull { it.second.size == tagCount }?.second

    override fun threshold(category: Int, fallback: Float): Float =
        when (category) {
            0 -> 0.32f
            4 -> 0.48f
            9 -> 0.37f
            else -> fallback
        }
}
