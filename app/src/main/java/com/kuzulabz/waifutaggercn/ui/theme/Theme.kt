package com.kuzulabz.waifutaggercn.ui.theme

import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkFallback = darkColorScheme(
    primary = FallbackPrimary,
    secondary = FallbackSecondary,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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

/**
 * 检测颜色是否处于紫色/品红色相范围（hue 240°-345°，饱和度 > 10%）。
 * 覆盖：蓝紫(240-270)、紫(270-300)、品红紫(300-345)。
 * 用于过滤设备动态取色（Monet / Material You）中不相干的紫色元素。
 * Material You 的 tertiary 默认就是紫/品红色，无论壁纸是什么。
 */
private fun isPurpleish(color: Color): Boolean {
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(
        (color.red * 255f).toInt().coerceIn(0, 255),
        (color.green * 255f).toInt().coerceIn(0, 255),
        (color.blue * 255f).toInt().coerceIn(0, 255),
        hsv
    )
    return hsv[0] in 240f..345f && hsv[1] > 0.10f
}

/**
 * 将紫色/品红色相偏移至蓝色范围，保持亮度和饱和度不变。
 * 240°-345° → 200°-235°，映射到舒适的蓝色区间。
 * 非紫色颜色原样返回。
 */
private fun shiftPurpleToBlue(color: Color): Color {
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(
        (color.red * 255f).toInt().coerceIn(0, 255),
        (color.green * 255f).toInt().coerceIn(0, 255),
        (color.blue * 255f).toInt().coerceIn(0, 255),
        hsv
    )
    val hue = hsv[0]
    if (hue in 240f..345f) {
        // 将紫色 240°-345° 线性映射到蓝色 200°-235°
        hsv[0] = 200f + (hue - 240f) * (35f / 105f)
    }
    val androidColor = AndroidColor.HSVToColor(hsv)
    return Color(
        red = AndroidColor.red(androidColor) / 255f,
        green = AndroidColor.green(androidColor) / 255f,
        blue = AndroidColor.blue(androidColor) / 255f,
        alpha = color.alpha
    )
}

/**
 * 将静态主题中的 M3 默认紫色角色替换为主色派生的颜色，保持色调一致。
 *
 * 静态主题（绿/蓝/粉/黄/棕/黑等）只显式指定了 primary/secondary 等少量角色，
 * 其余角色（tertiary、primaryContainer 等）使用 M3 默认值，而 M3 默认 tertiary 是紫色。
 *
 * 旧方案 filterPurpleFromScheme 将紫色色相机械偏移为蓝色，导致棕色主题中出现蓝色元素。
 * 新方案：检测到紫色角色时，用 primary 或 secondary 派生替代，确保整体色调统一。
 */
private fun harmonizeScheme(scheme: ColorScheme): ColorScheme {
    // tertiary 系列：M3 默认是紫色，用 secondary 替代
    val harmonizedTertiary = if (isPurpleish(scheme.tertiary)) scheme.secondary else scheme.tertiary
    val harmonizedTertiaryContainer = if (isPurpleish(scheme.tertiaryContainer))
        blendColors(scheme.secondaryContainer, scheme.secondary, 0.3f) else scheme.tertiaryContainer
    val harmonizedOnTertiary = if (isPurpleish(scheme.onTertiary)) scheme.onSecondary else scheme.onTertiary
    val harmonizedOnTertiaryContainer = if (isPurpleish(scheme.onTertiaryContainer))
        scheme.onSecondaryContainer else scheme.onTertiaryContainer

    // primaryContainer：如果未被静态主题显式设置且看起来像 M3 默认紫色，用 primary 派生
    val harmonizedPrimaryContainer = if (isPurpleish(scheme.primaryContainer))
        blendColors(scheme.surface, scheme.primary, 0.18f) else scheme.primaryContainer
    val harmonizedOnPrimaryContainer = if (isPurpleish(scheme.onPrimaryContainer))
        scheme.onPrimary else scheme.onPrimaryContainer

    // surfaceTint：通常应等于 primary
    val harmonizedSurfaceTint = if (isPurpleish(scheme.surfaceTint)) scheme.primary else scheme.surfaceTint

    // inversePrimary：用 primary 的反色
    val harmonizedInversePrimary = if (isPurpleish(scheme.inversePrimary))
        scheme.primary else scheme.inversePrimary

    // outline / outlineVariant：如果偏紫，用 onSurfaceVariant 派生
    val harmonizedOutline = if (isPurpleish(scheme.outline)) scheme.onSurfaceVariant else scheme.outline
    val harmonizedOutlineVariant = if (isPurpleish(scheme.outlineVariant))
        blendColors(scheme.surfaceVariant, scheme.onSurfaceVariant, 0.3f) else scheme.outlineVariant

    // inverseSurface / inverseOnSurface：如果偏紫，用 surface 系列替代
    val harmonizedInverseSurface = if (isPurpleish(scheme.inverseSurface))
        scheme.surface else scheme.inverseSurface
    val harmonizedInverseOnSurface = if (isPurpleish(scheme.inverseOnSurface))
        scheme.onSurface else scheme.inverseOnSurface

    return scheme.copy(
        primaryContainer = harmonizedPrimaryContainer,
        onPrimaryContainer = harmonizedOnPrimaryContainer,
        inversePrimary = harmonizedInversePrimary,
        tertiary = harmonizedTertiary,
        onTertiary = harmonizedOnTertiary,
        tertiaryContainer = harmonizedTertiaryContainer,
        onTertiaryContainer = harmonizedOnTertiaryContainer,
        surfaceTint = harmonizedSurfaceTint,
        inverseSurface = harmonizedInverseSurface,
        inverseOnSurface = harmonizedInverseOnSurface,
        outline = harmonizedOutline,
        outlineVariant = harmonizedOutlineVariant
    )
}

private val LightMonetGreen = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF4CAF50),
    background = Color(0xFFF5FBF4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE1F0DF),
    secondaryContainer = Color(0xFFCDECCB),
    onSecondaryContainer = Color(0xFF102A12),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkMonetGreen = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF07120A),
    surface = Color(0xFF101C12),
    surfaceVariant = Color(0xFF203023),
    secondaryContainer = Color(0xFF1F3A22),
    onSecondaryContainer = Color(0xFFE8F5E9),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightMonetBlue = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF42A5F5),
    background = Color(0xFFF4F8FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFDDEBFF),
    secondaryContainer = Color(0xFFCFE3FF),
    onSecondaryContainer = Color(0xFF0B1D34),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkMonetBlue = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF64B5F6),
    background = Color(0xFF07101F),
    surface = Color(0xFF101A2A),
    surfaceVariant = Color(0xFF1F2B3F),
    secondaryContainer = Color(0xFF19324D),
    onSecondaryContainer = Color(0xFFE3F2FD),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightMonetPink = lightColorScheme(
    primary = Color(0xFFC2185B),
    secondary = Color(0xFFF06292),
    background = Color(0xFFFFF6FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE0EC),
    secondaryContainer = Color(0xFFFFD4E5),
    onSecondaryContainer = Color(0xFF3A071B),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkMonetPink = darkColorScheme(
    primary = Color(0xFFF48FB1),
    secondary = Color(0xFFF8BBD0),
    background = Color(0xFF1C0710),
    surface = Color(0xFF2A1019),
    surfaceVariant = Color(0xFF40202D),
    secondaryContainer = Color(0xFF4D1830),
    onSecondaryContainer = Color(0xFFFFE4EF),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightMonetYellow = lightColorScheme(
    primary = Color(0xFFF9A825),
    secondary = Color(0xFFFFCA28),
    background = Color(0xFFFFFBF0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFF3CD),
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF2B2100),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkMonetYellow = darkColorScheme(
    primary = Color(0xFFFFD54F),
    secondary = Color(0xFFFFE082),
    background = Color(0xFF171204),
    surface = Color(0xFF241C08),
    surfaceVariant = Color(0xFF3A2E10),
    secondaryContainer = Color(0xFF4A390C),
    onSecondaryContainer = Color(0xFFFFF8E1),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
    onSecondaryContainer = Color(0xFF0F172A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
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
    onSecondaryContainer = Color(0xFF0F172A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
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
    onSecondaryContainer = Color(0xFFF5F5F7),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
    onSecondaryContainer = Color(0xFFF5F5F7),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// 紫色主题 — Purple
private val LightMonetPurple = lightColorScheme(
    primary = Color(0xFF7C4DFF),
    secondary = Color(0xFFB388FF),
    background = Color(0xFFF7F4FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9DFFF),
    secondaryContainer = Color(0xFFDDCCFF),
    onSecondaryContainer = Color(0xFF1B0A3D),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkMonetPurple = darkColorScheme(
    primary = Color(0xFFB388FF),
    secondary = Color(0xFFD1A3FF),
    background = Color(0xFF100620),
    surface = Color(0xFF1C1030),
    surfaceVariant = Color(0xFF2C1A48),
    secondaryContainer = Color(0xFF33205A),
    onSecondaryContainer = Color(0xFFEBDCFF),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
    onSecondaryContainer = Color(0xFF2B1700),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkMonetRainbow = darkColorScheme(
    primary = Color(0xFFFFB15C),
    secondary = Color(0xFFFFA03A),
    tertiary = Color(0xFFFFD29A),
    background = Color(0xFF1A1006),
    surface = Color(0xFF241507),
    surfaceVariant = Color(0xFF3A240D),
    secondaryContainer = Color(0xFF4A2A08),
    onSecondaryContainer = Color(0xFFFFE1BD),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// 棕色主题 — Brown
private val LightMonetBrown = lightColorScheme(
    primary = Color(0xFF8D6E63),
    secondary = Color(0xFFA1887F),
    background = Color(0xFFFBF6F1),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFE0D6),
    secondaryContainer = Color(0xFFE0CCC0),
    onSecondaryContainer = Color(0xFF2C1A10),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkMonetBrown = darkColorScheme(
    primary = Color(0xFFBCAAA4),
    secondary = Color(0xFFD7CCC8),
    background = Color(0xFF1A0F08),
    surface = Color(0xFF26180E),
    surfaceVariant = Color(0xFF3A261A),
    secondaryContainer = Color(0xFF44301C),
    onSecondaryContainer = Color(0xFFFFE0D1),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
    onSecondaryContainer = Color(0xFF0F0F12),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
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
    onSecondaryContainer = Color(0xFFE5E5EA),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
        onSurfaceVariant = Color(0xFFD6D6D6),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
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
        onSurfaceVariant = Color(0xFF55555C),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002)
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
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // 动态取色（Monet）：直接使用系统提取的颜色方案，不做任何过滤。
            // 系统会从壁纸中提取主色调并生成协调的色调系列（primary/secondary/tertiary 等）。
            // 如果对个别颜色做紫→蓝偏移，会破坏色调系列的一致性，
            // 导致 primary 是棕色但 primaryContainer 变成蓝色的不协调现象。
            // 如果用户不希望出现紫色，可以选择静态主题（蓝/绿/棕等）。
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkFallback
        else -> LightFallback
    }
    // 仅对静态主题（非动态取色）和谐化 M3 默认紫色角色。
    // 动态取色方案直接使用系统颜色，不经过此处理。
    val isDynamic = useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        monetPalette == "device" && !useCustomBackgroundStyle && !useIos27Style
    val filteredBaseScheme = if (isDynamic) baseColorScheme else harmonizeScheme(baseColorScheme)
    
    // 固定文字颜色：无论动态取色还是静态主题，浅色模式强制黑色文字、深色模式强制白色文字。
    // 解决部分壁纸动态取色导致 onSurface/onSurfaceVariant 过浅、文字不可读的问题。
    val fixedTextScheme = filteredBaseScheme.copy(
        // 黑色 Monet 预设在浅色模式也必须保持可读：禁止继承白色文字。
        onSurface = if (dark) Color(0xFFF5F5F7) else Color(0xFF1C1C1E),
        onSurfaceVariant = if (dark) Color(0xFFAEAEB2) else Color(0xFF55555C),
        onBackground = if (dark) Color(0xFFF5F5F7) else Color(0xFF1C1C1E),
        onPrimary = if (dark) Color.Black else Color.White,
        onPrimaryContainer = if (dark) Color.White else Color.Black,
        onSecondary = if (dark) Color.Black else Color.White,
        onSecondaryContainer = if (dark) Color.White else Color(0xFF1C1C1E)
    )
    
    val colorScheme = if (useCustomBackgroundStyle || useIos27Style || dark || monetPalette == "black") {
        fixedTextScheme
    } else {
        fixedTextScheme.copy(
            background = blendColors(fixedTextScheme.background, fixedTextScheme.primary, 0.06f),
            surface = blendColors(fixedTextScheme.surface, fixedTextScheme.primary, 0.025f),
            secondaryContainer = blendColors(fixedTextScheme.secondaryContainer, fixedTextScheme.primary, 0.035f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
