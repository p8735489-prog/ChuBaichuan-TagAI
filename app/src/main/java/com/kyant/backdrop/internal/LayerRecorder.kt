package com.kyant.backdrop.internal

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize

// Kotlin 2.x 中 context 参数必须命名（不能匿名）。原始代码用的是 1.6-1.9 的
// 「context(DelegatableNode)」匿名 context receiver 语法，这里把它命名为 node 以便
// 函数体内能继续用 `node.requireDensity()` 这样的扩展调用。
context(node: DelegatableNode)
internal fun DrawScope.recordLayer(
    layer: GraphicsLayer,
    size: IntSize = this.size.toIntSize(),
    block: DrawScope.() -> Unit
) {
    val density = node.requireDensity()
    layer.record(size) {
        val prevDensity = drawContext.density
        drawContext.density = density
        try {
            this.block()
        } finally {
            drawContext.density = prevDensity
        }
    }
}
