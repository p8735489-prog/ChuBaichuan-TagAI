package com.kuzulabz.waifutaggercn.ml.adapters

/** Common contract for model-specific tagger behavior. */
interface TaggerAdapter {
    val modelName: String
    val labelCount: Int?

    /** Pick the model's semantic output from ONNX outputs. */
    fun selectOutput(
        candidates: List<Pair<String, FloatArray>>,
        tagCount: Int
    ): FloatArray?

    /** Category-specific threshold; null means use caller/default threshold. */
    fun threshold(category: Int, fallback: Float): Float = fallback

    /** 精准模式额外阈值调整。 */
    fun precisionThreshold(category: Int, fallback: Float): Float = fallback
}
