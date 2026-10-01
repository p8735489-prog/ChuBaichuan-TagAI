package com.kuzulabz.waifutaggercn.ml

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * CharacterInstance — 角色实例数据模型
 *
 * 每个角色实例保存独立的标签、mask、裁剪图、评分信息，
 * 用于多角色场景下的属性隔离与主/次角色区分。
 *
 * @param id 角色实例唯一 ID（从 0 递增）
 * @param bbox YOLO 检测框（原图坐标）
 * @param mask 实例分割 mask（原图尺寸二值图）
 * @param cropImage 裁剪后的角色图片
 * @param tags WD Tagger 识别的原始标签
 * @param filteredTags 经 TagFilter 处理后的标签
 * @param yoloConfidence YOLO 检测置信度
 * @param area 实例面积（mask 像素数 / 原图像素数）
 * @param className YOLO 类别名（如 "person"）
 * @param classId YOLO 类别 ID
 * @param score 综合评分（由 CharacterScorer 计算）
 * @param isMain 是否为主角色（score 最高的角色）
 */
data class CharacterInstance(
    val id: Int,
    val bbox: RectF,
    val mask: Bitmap?,
    val cropImage: Bitmap?,
    val tags: List<TaggerEngine.Tag>,
    val filteredTags: List<TaggerEngine.Tag> = emptyList(),
    val yoloConfidence: Float,
    val area: Float,
    val className: String,
    val classId: Int,
    val score: Float = 0f,
    val isMain: Boolean = false
) {
    /** 角色标签数量 */
    val tagCount: Int get() = tags.size

    /** 角色过滤后标签数量 */
    val filteredTagCount: Int get() = filteredTags.size

    /**
     * 角色调试信息摘要
     * 用于开发模式显示，方便定位识别问题
     */
    fun debugSummary(): String = buildString {
        appendLine("── Character ${id + 1} ${if (isMain) "(Main)" else ""} ──")
        appendLine("  YOLO Area: ${"%.2f".format(area * 100)}%")
        appendLine("  YOLO Confidence: ${"%.3f".format(yoloConfidence)}")
        appendLine("  WD Tags (before filter): $tagCount")
        appendLine("  WD Tags (after filter): $filteredTagCount")
        appendLine("  Final Score: ${"%.3f".format(score)}")
        if (filteredTags.isNotEmpty()) {
            appendLine("  Top tags: ${filteredTags.take(5).joinToString(", ") { it.name }}")
        }
    }
}
