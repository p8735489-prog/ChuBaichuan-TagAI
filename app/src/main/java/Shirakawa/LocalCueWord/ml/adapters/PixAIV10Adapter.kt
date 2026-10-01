package Shirakawa.LocalCueWord.ml.adapters

/**
 * PixAI Tagger v1.0（ONNX 导出仓库：noaione/pixai-tagger-v1.0-onnx）。
 *
 * 与 v0.9 的关键差异：
 *  - 输出维度 30877（v0.9 为 13461）；
 *  - tags.json 自带 `categories` 数组，其中的 category 是**该文件内的 0..5 自定义序**，
 *    并非 Danbooru 的 0/4/9 编号：
 *        0=general  1=character  2=copyright  3=style  4=meta  5=rating
 *    因此下面的阈值表按 0..5 直接索引，不要做 Danbooru 数字映射。
 *  - 官方输出为**未过 sigmoid** 的 logits，由 TaggerEngine 的 `pixai_v1` 分支统一 sigmoid。
 */
class PixAIV10Adapter : TaggerAdapter {
    override val modelName = "PixAI Tagger v1.0"
    override val labelCount = 30877

    override fun selectOutput(
        candidates: List<Pair<String, FloatArray>>,
        tagCount: Int
    ): FloatArray? =
        candidates.firstOrNull { it.second.size == labelCount }?.second
            ?: candidates.firstOrNull { it.second.size == tagCount }?.second

    /** 官方推荐阈值：general 0.17 / character 0.27 / copyright 0.24 / style 0.15 / meta 0.17 / rating 0.41 */
    override fun threshold(category: Int, fallback: Float): Float =
        when (category) {
            0 -> 0.17f
            1 -> 0.27f
            2 -> 0.24f
            3 -> 0.15f
            4 -> 0.17f
            5 -> 0.41f
            else -> fallback
        }

    /** 精准模式在官方阈值基础上收紧，抑制低置信噪声。 */
    override fun precisionThreshold(category: Int, fallback: Float): Float =
        when (category) {
            0 -> 0.25f
            1 -> 0.40f
            2 -> 0.35f
            3 -> 0.20f
            4 -> 0.25f
            5 -> 0.55f
            else -> fallback
        }
}
