package Shirakawa.LocalCueWord.ml

/**
 * Model compatibility marker.
 * Keeps Camie/PixAI/AnimeTimm outputs in the same semantic pipeline.
 * Tag dictionaries remain model-owned; fusion only merges normalized names.
 */
object TagModelCompatibilityFix {
    const val CAMIE_LABELS = 70527
    const val PIXAI_LABELS = 13461
    const val ANIMETIMM_LABELS = 12476

    /** PixAI Tagger v1.0：输出维度 30877（tags.json 的 num_classes）。 */
    const val PIXAI_V10_LABELS = 30877
}
