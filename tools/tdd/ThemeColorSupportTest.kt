package tdd

import Shirakawa.LocalCueWord.ui.theme.containerForegroundArgb
import Shirakawa.LocalCueWord.ui.theme.isLightArgb

fun main() {
    check(isLightArgb(0xFFFFFFFF.toInt()))
    check(!isLightArgb(0xFF000000.toInt()))
    check(containerForegroundArgb(0xFFFFFFFF.toInt()) == 0xFF1C1C1E.toInt())
    check(containerForegroundArgb(0xFF000000.toInt()) == 0xFFF5F5F7.toInt())
    println("ThemeColorSupportTest: PASS")
}
