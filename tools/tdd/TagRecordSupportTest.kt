package tdd

import Shirakawa.LocalCueWord.TagRecord
import Shirakawa.LocalCueWord.fullTranslationText
import Shirakawa.LocalCueWord.mergeTranslations
import Shirakawa.LocalCueWord.translationForTag

fun main() {
    val record = TagRecord(
        id = 1L,
        text = "1girl, long_hair",
        createdAt = 2L,
        imagePath = "/tmp/a.jpg",
        negativeText = "lowres",
        translations = mapOf("1girl" to "一个女孩")
    )
    check(record.negativeText == "lowres")
    check(translationForTag(record.translations, "1girl") == "一个女孩")
    check(translationForTag(record.translations, "long_hair") == "暂未翻译")
    val merged = mergeTranslations(record.translations, listOf("long_hair" to "长发", "1girl" to "女孩"))
    check(merged == mapOf("1girl" to "女孩", "long_hair" to "长发"))
    check(fullTranslationText(listOf("1girl", "long_hair"), merged) == "1girl → 女孩\nlong_hair → 长发")
    println("TagRecordSupportTest: PASS")
}
