package Shirakawa.LocalCueWord.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 半透明圆形操作按钮（照搬 local-dream ZoomableImageOverlay.OverlayIconButton）：
 * scrim 底 + 白色图标，浮在图片查看器上任何主题色都清晰可读。
 */
@Composable
fun OverlayIconButton(
    icon: ImageVector,
    contentDescription: String?,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.22f),
            disabledContentColor = Color.White.copy(alpha = 0.38f)
        )
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp))
    }
}

/**
 * 图片缩放查看器（交互完全对齐 local-dream 的 ZoomableImageOverlay）：
 *  - 双指捏合缩放（0.5x~5x，围绕手势中心点），拖动平移；
 *  - 点击图片外的区域关闭；
 *  - 系统返回键关闭；
 *  - 右下角「重置缩放」按钮；
 *  - 底部居中显示当前缩放百分比；
 *  - 可选：多图时显示左右切换按钮与「当前/总数」指示。
 */
@Composable
fun ZoomableImageOverlay(
    bitmap: Bitmap?,
    onDismiss: () -> Unit,
    showScaleIndicator: Boolean = false,
    indexLabel: String? = null,
    canGoPrevious: Boolean = false,
    canGoNext: Boolean = false,
    onGoPrevious: () -> Unit = {},
    onGoNext: () -> Unit = {},
    modifier: Modifier = Modifier,
    topEndContent: @Composable RowScope.() -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    BackHandler(onBack = onDismiss)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    scale = (scale * zoom).coerceIn(0.5f, 5f)

                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    val focusX = (centroid.x - centerX - offsetX) / oldScale
                    val focusY = (centroid.y - centerY - offsetY) / oldScale

                    offsetX += focusX * oldScale - focusX * scale
                    offsetY += focusY * oldScale - focusY * scale

                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(bitmap) {
                detectTapGestures(onTap = { offset ->
                    val bmp = bitmap
                    if (bmp == null) {
                        onDismiss()
                        return@detectTapGestures
                    }
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    // 图片按「居中正方形 + ContentScale.Fit」布局，再经 graphicsLayer
                    // 缩放平移。这里还原出图片的真实可见矩形，让点在黑边（非正方形图）
                    // 时也能正确关闭，而不是只有点在正方形外才关闭。
                    val square = minOf(size.width, size.height).toFloat()
                    val aspect = bmp.width.toFloat() / bmp.height.toFloat()
                    val baseWidth = if (aspect >= 1f) square else square * aspect
                    val baseHeight = if (aspect >= 1f) square / aspect else square
                    val scaledWidth = baseWidth * scale
                    val scaledHeight = baseHeight * scale

                    val left = centerX + offsetX - scaledWidth / 2f
                    val right = centerX + offsetX + scaledWidth / 2f
                    val top = centerY + offsetY - scaledHeight / 2f
                    val bottom = centerY + offsetY + scaledHeight / 2f

                    if (offset.x < left || offset.x > right ||
                        offset.y < top || offset.y > bottom
                    ) {
                        onDismiss()
                    }
                })
            }
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "preview image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = topEndContent
        )

        // 多图时的左右切换按钮（与 local-dream 一致的半透明圆形样式）。
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (canGoPrevious || canGoNext) {
                OverlayIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "previous image",
                    enabled = canGoPrevious,
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        onGoPrevious()
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (canGoPrevious || canGoNext) {
                OverlayIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "next image",
                    enabled = canGoNext,
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        onGoNext()
                    }
                )
            }
        }

        // 重置缩放（照搬 local-dream 右下角 Refresh 按钮）。
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            OverlayIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = "reset zoom",
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                }
            )
        }

        // 底部居中：缩放百分比 + 多图索引指示。
        val indicatorText = buildString {
            if (showScaleIndicator) append("${(scale * 100).toInt()}%")
            if (!indexLabel.isNullOrBlank()) {
                if (isNotEmpty()) append("  ·  ")
                append(indexLabel)
            }
        }
        if (indicatorText.isNotBlank()) {
            Text(
                text = indicatorText,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
