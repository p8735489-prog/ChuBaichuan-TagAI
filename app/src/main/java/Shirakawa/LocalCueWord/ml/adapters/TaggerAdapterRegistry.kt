package Shirakawa.LocalCueWord.ml.adapters

/** Single registration point for all model-specific tagger adapters. */
object TaggerAdapterRegistry {
    val camie: TaggerAdapter = CamieV2Adapter()
    val pixai: TaggerAdapter = PixAIV09Adapter()
    val pixaiV10: TaggerAdapter = PixAIV10Adapter()
    val animeTimm: TaggerAdapter = AnimeTimmDBv4Adapter()

    val all: List<TaggerAdapter> = listOf(camie, pixai, pixaiV10, animeTimm)

    fun forModel(modelId: String): TaggerAdapter? = when {
        modelId.contains("camie", ignoreCase = true) -> camie
        // ⚠️ 顺序陷阱：v1.0 的 id（pixai-tagger-v1.0）同样包含 "pixai"，
        // 若把这条放到下面那条之后，就会被 v0.9 截胡。必须先判 v1.0。
        modelId.contains("pixai", ignoreCase = true) && isV10(modelId) -> pixaiV10
        modelId.contains("pixai", ignoreCase = true) -> pixai
        modelId.contains("animetimm", ignoreCase = true) -> animeTimm
        else -> null
    }

    /** 兼容 `v1.0` / `v1-0` / `v10` 三种写法。 */
    private fun isV10(modelId: String): Boolean {
        val n = modelId.lowercase()
        return n.contains("v1.0") || n.contains("v1-0") || n.contains("v10")
    }
}
