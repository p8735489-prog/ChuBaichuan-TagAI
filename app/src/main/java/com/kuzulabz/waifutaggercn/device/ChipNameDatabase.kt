package com.kuzulabz.waifutaggercn.device

object ChipNameDatabase {
    private val chips = mapOf(
        "SM8650" to mapOf("zh" to "骁龙 8 Gen 3", "en" to "Snapdragon 8 Gen 3"),
        "SM8550" to mapOf("zh" to "骁龙 8 Gen 2", "en" to "Snapdragon 8 Gen 2"),
        "SM8475" to mapOf("zh" to "骁龙 8+ Gen 1", "en" to "Snapdragon 8+ Gen 1"),
        "MT6989" to mapOf("zh" to "天玑 9300", "en" to "Dimensity 9300"),
        "MT6985" to mapOf("zh" to "天玑 9200", "en" to "Dimensity 9200"),
        "TENSOR G3" to mapOf("zh" to "Tensor G3", "en" to "Google Tensor G3")
    )
    fun resolve(id:String, lang:String):String = chips[id.uppercase()]?.get(lang.substringBefore('-')) ?: id
}
