package com.kuzulabz.waifutaggercn.ml.adapters

/** Single registration point for all model-specific tagger adapters. */
object TaggerAdapterRegistry {
    val camie: TaggerAdapter = CamieV2Adapter()
    val pixai: TaggerAdapter = PixAIV09Adapter()
    val animeTimm: TaggerAdapter = AnimeTimmDBv4Adapter()

    val all: List<TaggerAdapter> = listOf(camie, pixai, animeTimm)

    fun forModel(modelId: String): TaggerAdapter? = when {
        modelId.contains("camie", ignoreCase = true) -> camie
        modelId.contains("pixai", ignoreCase = true) -> pixai
        modelId.contains("animetimm", ignoreCase = true) -> animeTimm
        else -> null
    }
}
