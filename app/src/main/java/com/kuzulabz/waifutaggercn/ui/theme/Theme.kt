package com.kuzulabz.waifutaggercn.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Fallback palette (used on Android < 12, where wallpaper-based dynamic
// color / "Monet" isn't available). Use the liquid-glass blue accent instead
// of Material's default purple so controls do not fall back to purple.
private val FallbackPrimary = Color(0xFF0A84FF)
private val FallbackSecondary = Color(0xFF64D2FF)
private val IosPrimary = Color(0xFF0A84FF)
private val IosSecondary = Color(0xFF64D2FF)

private val LightFallback = lightColorScheme(
    primary = FallbackPrimary,
    secondary = FallbackSecondary,
)

private val DarkFallback = darkColorScheme(
    primary = FallbackPrimary,
    secondary = FallbackSecondary,
)

private fun blendColors(base: Color, overlay: Color, overlayAlpha: Float): Color {
    val alpha = overlayAlpha.coerceIn(0f, 1f)
    return Color(
        red = base.red * (1f - alpha) + overlay.red * alpha,
        green = base.green * (1f - alpha) + overlay.green * alpha,
        blue = base.blue * (1f - alpha) + overlay.blue * alpha,
        alpha = 1f
    )
}

private val LightMonetGreen = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF4CAF50),
    background = Color(0xFFF5FBF4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE1F0DF),
    secondaryContainer = Color(0xFFCDECCB),
    onSecondaryContainer = Color(0xFF102A12)
)

private val DarkMonetGreen = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF07120A),
    surface = Color(0xFF101C12),
    surfaceVariant = Color(0xFF203023),
    secondaryContainer = Color(0xFF1F3A22),
    onSecondaryContainer = Color(0xFFE8F5E9)
)

private val LightMonetBlue = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF42A5F5),
    background = Color(0xFFF4F8FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFDDEBFF),
    secondaryContainer = Color(0xFFCFE3FF),
    onSecondaryContainer = Color(0xFF0B1D34)
)

private val DarkMonetBlue = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF64B5F6),
    background = Color(0xFF07101F),
    surface = Color(0xFF101A2A),
    surfaceVariant = Color(0xFF1F2B3F),
    secondaryContainer = Color(0xFF19324D),
    onSecondaryContainer = Color(0xFFE3F2FD)
)

private val LightMonetPink = lightColorScheme(
    primary = Color(0xFFC2185B),
    secondary = Color(0xFFF06292),
    background = Color(0xFFFFF6FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE0EC),
    secondaryContainer = Color(0xFFFFD4E5),
    onSecondaryContainer = Color(0xFF3A071B)
)

private val DarkMonetPink = darkColorScheme(
    primary = Color(0xFFF48FB1),
    secondary = Color(0xFFF8BBD0),
    background = Color(0xFF1C0710),
    surface = Color(0xFF2A1019),
    surfaceVariant = Color(0xFF40202D),
    secondaryContainer = Color(0xFF4D1830),
    onSecondaryContainer = Color(0xFFFFE4EF)
)

private val LightMonetYellow = lightColorScheme(
    primary = Color(0xFFF9A825),
    secondary = Color(0xFFFFCA28),
    background = Color(0xFFFFFBF0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFF3CD),
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF2B2100)
)

private val DarkMonetYellow = darkColorScheme(
    primary = Color(0xFFFFD54F),
    secondary = Color(0xFFFFE082),
    background = Color(0xFF171204),
    surface = Color(0xFF241C08),
    surfaceVariant = Color(0xFF3A2E10),
    secondaryContainer = Color(0xFF4A390C),
    onSecondaryContainer = Color(0xFFFFF8E1)
)

private val LightIos27 = lightColorScheme(
    primary = IosPrimary,
    secondary = IosSecondary,
    background = Color(0xFFF7FAFF),
    surface = Color(0x66FFFFFF),
    surfaceVariant = Color(0x4DF2F7FF),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF636366),
    primaryContainer = Color(0x338EC5FF),
    secondaryContainer = Color(0x40F4F8FF),
    onSecondaryContainer = Color(0xFF0F172A)
)

private fun lightMinimalScheme(primary: Color, secondary: Color) = lightColorScheme(
    primary = primary,
    secondary = secondary,
    background = Color(0xFFF7FAFF),
    surface = Color(0x66FFFFFF),
    surfaceVariant = blendColors(Color(0x4DF5F8FF), primary, 0.04f),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF636366),
    primaryContainer = blendColors(Color(0x55FFFFFF), primary, 0.16f),
    secondaryContainer = blendColors(Color(0x44FFFFFF), primary, 0.10f),
    onSecondaryContainer = Color(0xFF0F172A)
)

private fun darkMinimalScheme(primary: Color, secondary: Color) = darkColorScheme(
    primary = primary,
    secondary = secondary,
    background = Color(0xFF111216),
    surface = Color(0x6624252B),
    surfaceVariant = blendColors(Color(0x772A2B32), primary, 0.08f),
    onSurface = Color(0xFFF5F5F7),
    onSurfaceVariant = Color(0xFFAEAEB2),
    primaryContainer = blendColors(Color(0x772A2B32), primary, 0.24f),
    secondaryContainer = blendColors(Color(0x66343438), primary, 0.22f),
    onSecondaryContainer = Color(0xFFF5F5F7)
)

private val DarkIos27 = darkColorScheme(
    primary = Color(0xFF64D2FF),
    secondary = Color(0xFFBF5AF2),
    background = Color(0xFF111216),
    surface = Color(0x6624252B),
    surfaceVariant = Color(0x772A2B32),
    onSurface = Color(0xFFF5F5F7),
    onSurfaceVariant = Color(0xFFAEAEB2),
    primaryContainer = Color(0x7744758F),
    secondaryContainer = Color(0x66343438),
    onSecondaryContainer = Color(0xFFF5F5F7)
)

// 紫色主题 — Purple
private val LightMonetPurple = lightColorScheme(
    primary = Color(0xFF7C4DFF),
    secondary = Color(0xFFB388FF),
    background = Color(0xFFF7F4FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9DFFF),
    secondaryContainer = Color(0xFFDDCCFF),
    onSecondaryContainer = Color(0xFF1B0A3D)
)

private val DarkMonetPurple = darkColorScheme(
    primary = Color(0xFFB388FF),
    secondary = Color(0xFFD1A3FF),
    background = Color(0xFF100620),
    surface = Color(0xFF1C1030),
    surfaceVariant = Color(0xFF2C1A48),
    secondaryContainer = Color(0xFF33205A),
    onSecondaryContainer = Color(0xFFEBDCFF)
)

// 彩色主题 — Rainbow (融合紫蓝绿粉)
private val LightMonetRainbow = lightColorScheme(
    primary = Color(0xFFFF8A00),
    secondary = Color(0xFFE07B00),
    tertiary = Color(0xFFFFB15C),
    background = Color(0xFFFFFBF6),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE2C2),
    secondaryContainer = Color(0xFFFFE8CC),
    onSecondaryContainer = Color(0xFF2B1700)
)

private val DarkMonetRainbow = darkColorScheme(
    primary = Color(0xFFFFB15C),
    secondary = Color(0xFFFFA03A),
    tertiary = Color(0xFFFFD29A),
    background = Color(0xFF1A1006),
    surface = Color(0xFF241507),
    surfaceVariant = Color(0xFF3A240D),
    secondaryContainer = Color(0xFF4A2A08),
    onSecondaryContainer = Color(0xFFFFE1BD)
)

// 棕色主题 — Brown
private val LightMonetBrown = lightColorScheme(
    primary = Color(0xFF8D6E63),
    secondary = Color(0xFFA1887F),
    background = Color(0xFFFBF6F1),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFE0D6),
    secondaryContainer = Color(0xFFE0CCC0),
    onSecondaryContainer = Color(0xFF2C1A10)
)

private val DarkMonetBrown = darkColorScheme(
    primary = Color(0xFFBCAAA4),
    secondary = Color(0xFFD7CCC8),
    background = Color(0xFF1A0F08),
    surface = Color(0xFF26180E),
    surfaceVariant = Color(0xFF3A261A),
    secondaryContainer = Color(0xFF44301C),
    onSecondaryContainer = Color(0xFFFFE0D1)
)

// 黑色主题 — Black (纯黑 + 深灰，如截图所示)
private val LightMonetBlack = lightColorScheme(
    primary = Color(0xFF1C1C1E),
    secondary = Color(0xFF3A3A3C),
    tertiary = Color(0xFF636366),
    background = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF636366),
    secondaryContainer = Color(0xFFE8E8ED),
    onSecondaryContainer = Color(0xFF0F0F12)
)

private val DarkMonetBlack = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    secondary = Color(0xFFC7C7CC),
    tertiary = Color(0xFF8E8E93),
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF8E8E93),
    secondaryContainer = Color(0xFF1C1C1E),
    onSecondaryContainer = Color(0xFFE5E5EA)
)

private fun customBackgroundPrimary(palette: String, dark: Boolean): Color {
    return when (palette) {
        "white" -> Color(0xFFFFFFFF)
        "green" -> if (dark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
        "blue" -> if (dark) Color(0xFF90CAF9) else Color(0xFF1565C0)
        "pink" -> if (dark) Color(0xFFF48FB1) else Color(0xFFC2185B)
        "yellow" -> if (dark) Color(0xFFFFD54F) else Color(0xFFF9A825)
        "purple" -> if (dark) Color(0xFFB388FF) else Color(0xFF7C4DFF)
        "orange" -> if (dark) Color(0xFFFFB15C) else Color(0xFFFF8A00)
        "rainbow" -> if (dark) Color(0xFFFFB15C) else Color(0xFFFF8A00)
        "brown" -> if (dark) Color(0xFFBCAAA4) else Color(0xFF8D6E63)
        "deep_blue" -> if (dark) Color(0xFF64D2FF) else Color(0xFF0057D9)
        "lava_orange" -> if (dark) Color(0xFFFF9F0A) else Color(0xFFFF5A1F)
        "sweet_pink" -> if (dark) Color(0xFFFF8BD2) else Color(0xFFFF2D8F)
        else -> if (dark) Color(0xFFFFFFFF) else Color(0xFF1C1C1E)
    }
}

private fun customBackgroundScheme(palette: String, dark: Boolean) = if (dark) {
    val primary = customBackgroundPrimary(palette, true)
    darkColorScheme(
        primary = primary,
        secondary = primary,
        tertiary = primary,
        background = Color.Black,
        surface = Color(0xFF151515),
        surfaceVariant = Color(0xFF242424),
        secondaryContainer = Color(0xCC202020),
        onSecondaryContainer = Color(0xFFF5F5F5),
        onSurface = Color(0xFFF5F5F5),
        onSurfaceVariant = Color(0xFFD6D6D6)
    )
} else {
    val primary = customBackgroundPrimary(palette, false)
    lightColorScheme(
        primary = primary,
        secondary = primary,
        tertiary = primary,
        background = Color.White,
        surface = Color.White,
        surfaceVariant = Color(0xFFF2F2F7),
        secondaryContainer = Color(0xEAF7F7FA),
        onSecondaryContainer = Color(0xFF1C1C1E),
        onSurface = Color(0xFF1C1C1E),
        onSurfaceVariant = Color(0xFF55555C)
    )
}

@Composable
fun WaifuTaggerCNTheme(
    useDynamicColor: Boolean = true,
    useIos27Style: Boolean = false,
    useCustomBackgroundStyle: Boolean = false,
    monetPalette: String = "device",
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val dark = darkTheme ?: isSystemInDarkTheme()
    val context = LocalContext.current

    val baseColorScheme = when {
        useCustomBackgroundStyle -> customBackgroundScheme(monetPalette, dark)
        useIos27Style && monetPalette == "deep_blue" ->
            if (dark) darkMinimalScheme(Color(0xFF64D2FF), Color(0xFF0A84FF)) else lightMinimalScheme(Color(0xFF0057D9), Color(0xFF4DA3FF))
        useIos27Style && monetPalette == "lava_orange" ->
            if (dark) darkMinimalScheme(Color(0xFFFF9F0A), Color(0xFFFF453A)) else lightMinimalScheme(Color(0xFFFF5A1F), Color(0xFFFF9F0A))
        useIos27Style && monetPalette == "sweet_pink" ->
            if (dark) darkMinimalScheme(Color(0xFFFF8BD2), Color(0xFFBF5AF2)) else lightMinimalScheme(Color(0xFFFF2D8F), Color(0xFFAF52DE))
        useIos27Style -> if (dark) DarkIos27 else LightIos27
        monetPalette == "green" -> if (dark) DarkMonetGreen else LightMonetGreen
        monetPalette == "blue" -> if (dark) DarkMonetBlue else LightMonetBlue
        monetPalette == "pink" -> if (dark) DarkMonetPink else LightMonetPink
        monetPalette == "yellow" -> if (dark) DarkMonetYellow else LightMonetYellow
        monetPalette == "purple" -> if (dark) DarkMonetPurple else LightMonetPurple
        monetPalette == "rainbow" -> if (dark) DarkMonetRainbow else LightMonetRainbow
        monetPalette == "brown" -> if (dark) DarkMonetBrown else LightMonetBrown
        monetPalette == "black" -> if (dark) DarkMonetBlack else LightMonetBlack
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkFallback
        else -> LightFallback
    }
    val colorScheme = if (useCustomBackgroundStyle || useIos27Style || dark || monetPalette == "black") {
        baseColorScheme
    } else {
        baseColorScheme.copy(
            background = blendColors(baseColorScheme.background, baseColorScheme.primary, 0.06f),
            surface = blendColors(baseColorScheme.surface, baseColorScheme.primary, 0.025f),
            secondaryContainer = blendColors(baseColorScheme.secondaryContainer, baseColorScheme.primary, 0.035f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
