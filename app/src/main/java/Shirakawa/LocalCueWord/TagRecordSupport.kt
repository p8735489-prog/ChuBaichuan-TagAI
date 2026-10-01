package Shirakawa.LocalCueWord

/** A persisted recognition/gallery record. Optional fields preserve compatibility with old records. */
data class TagRecord(
    val id: Long,
    val text: String,
    val createdAt: Long,
    val imagePath: String? = null,
    val negativeText: String? = null,
    val translations: Map<String, String> = emptyMap()
)

private const val UNTRANSLATED_LABEL = "暂未翻译"

/** Merge only usable translations; blank results never overwrite an existing translation. */
fun mergeTranslations(
    existing: Map<String, String>,
    updates: Iterable<Pair<String, String>>
): Map<String, String> {
    val result = existing.toMutableMap()
    updates.forEach { (original, translated) ->
        val source = original.trim()
        val target = translated.trim()
        if (source.isNotEmpty() && target.isNotEmpty()) result[source] = target
    }
    return result
}

/** Returns the stored translation or the exact UI fallback requested by the product design. */
fun translationForTag(translations: Map<String, String>, original: String): String =
    translations[original]?.takeIf { it.isNotBlank() } ?: UNTRANSLATED_LABEL

/** Full, non-ellipsized translation output suitable for copying or a wrapping Text composable. */
fun fullTranslationText(tags: List<String>, translations: Map<String, String>): String =
    tags.filter { it.isNotBlank() }
        .joinToString("\n") { tag -> "$tag → ${translationForTag(translations, tag)}" }
