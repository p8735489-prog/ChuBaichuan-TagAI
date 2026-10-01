from pathlib import Path

main = Path('app/src/main/java/Shirakawa/LocalCueWord/MainActivity.kt').read_text()
theme = Path('app/src/main/java/Shirakawa/LocalCueWord/ui/theme/Theme.kt').read_text()

assert 'val negativeText: String? = null' in Path('app/src/main/java/Shirakawa/LocalCueWord/TagRecordSupport.kt').read_text()
assert 'val translations: Map<String, String> = emptyMap()' in Path('app/src/main/java/Shirakawa/LocalCueWord/TagRecordSupport.kt').read_text()
assert '.put("translations", JSONObject().apply' in main
assert 'json.put("translations", translationObject)' in main
assert 'translations = translations' in main
assert 'padding(top = 30.dp, bottom = 16.dp)' in main
assert 'putString(KEY_MONET_PALETTE, MONET_PALETTE_BLACK)' in main
assert 'else -> if (dark) DarkMonetBlack else LightMonetBlack' in theme
assert 'containerForegroundArgb(container.toArgb())' in theme
assert 'maxLines = 2' not in main[main.index('private fun TagDetailRow'):main.index('private fun TagCopyChip')]
print('SourceRegressionTest: PASS')
