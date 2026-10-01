package Shirakawa.LocalCueWord.ui.components

// 直接从 local-dream 源码复制，未作任何改动。
// 来源：local-dream-master/app/src/main/java/io/github/xororz/localdream/ui/components/SmoothWavyProgressIndicators.kt
//
// 仅替换 package 与 import 路径，使其可在本工程（Shirakawa.LocalCueWord 包）下编译。
// 实现细节参考 local-dream 原文，这里不做注释以保持与原版完全一致。

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import Shirakawa.LocalCueWord.ui.theme.Motion

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SmoothLinearWavyProgressIndicator(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = Motion.Progress,
        label = "linearProgress",
    )
    LinearWavyProgressIndicator(
        progress = { animated },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SmoothCircularWavyProgressIndicator(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = Motion.Progress,
        label = "circularProgress",
    )
    CircularWavyProgressIndicator(
        progress = { animated },
        modifier = modifier
    )
}