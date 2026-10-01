package com.kuzulabz.waifutaggercn.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * 轻量加载动画 — 物理弹簧风格三圆点波浪变形。
 *
 * 使用位置驱动的 ping-pong 模式，三个圆点依次弹跳：
 * 1 → 2 → 3 → [保持3] → 2 → 1，形成完整的往返循环。
 *
 * 修复说明：旧版使用正弦波 + keyframes（1000ms 动画在 1400ms 周期中），
 * 导致末尾 400ms 停滞，最后一个圆点无法完成回弹，序列变为 1 2 3 3 2（缺少最后的 1）。
 * 新版使用三角波位置驱动，确保完整往返。
 */
@Composable
fun MorphingBlobLoader(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob")

    // 弹簧缓动曲线：缓入 → 过冲 → 回弹 → 稳定
    val springLikeEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    // 使用线性 tween 驱动 progress 0→1，然后用位置映射函数生成 ping-pong 效果
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "loaderProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val baseRadius = this.size.minDimension * 0.10f
        val spacing = this.size.minDimension * 0.22f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // 位置映射：0→2→0 的三角波，在 peak 处短暂保持
        // progress 0.0~0.4: position 0→2（正向：dot 1→2→3）
        // progress 0.4~0.6: position 保持 2（dot 3 保持亮起）
        // progress 0.6~1.0: position 2→0（反向：dot 3→2→1）
        val position = when {
            progress <= 0.4f -> progress * 5f           // 0 → 2
            progress <= 0.6f -> 2f                       // 保持 2
            else -> 2f - (progress - 0.6f) * 5f         // 2 → 0
        }

        repeat(3) { index ->
            // 每个圆点的激活程度基于与当前位置的距离
            val distance = abs(position - index.toFloat())
            // 使用平滑的 bump 函数：距离越近，激活程度越高
            val wave = (1f - distance).coerceIn(0f, 1f)

            // 应用弹簧缓动，产生过冲和回弹的物理感
            val easedWave = springLikeEasing.transform(wave)

            // 缩放范围：从 0.75 到 1.35，产生更明显的弹跳效果
            val radius = baseRadius * (0.75f + easedWave * 0.60f)

            // 透明度范围：从 0.35 到 1.0，物理感更强
            val alpha = 0.35f + easedWave * 0.65f

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(
                    x = center.x + (index - 1) * spacing,
                    y = center.y
                )
            )
        }
    }
}
