package Shirakawa.LocalCueWord.ui.theme

/** Pure ARGB helpers so foreground contrast decisions are deterministic and testable. */
fun isLightArgb(argb: Int): Boolean {
    val red = (argb shr 16 and 0xFF) / 255f
    val green = (argb shr 8 and 0xFF) / 255f
    val blue = (argb and 0xFF) / 255f
    return red * 0.299f + green * 0.587f + blue * 0.114f >= 0.5f
}

fun containerForegroundArgb(argb: Int): Int =
    if (isLightArgb(argb)) 0xFF1C1C1E.toInt() else 0xFFF5F5F7.toInt()
