package com.kuzulabz.waifutaggercn.ml

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF

/**
 * CharacterScorer — 角色实例评分系统
 *
 * 综合评分公式：
 *   score = w1 * areaScore + w2 * yoloConfidence + w3 * wdTagConfidence + w4 * characterTagScore
 *
 * 其中：
 *   - areaScore: mask 面积占原图比例（0..1），反映角色在画面中的占比
 *   - yoloConfidence: YOLO 检测置信度（0..1）
 *   - wdTagConfidence: WD Tagger 输出的标签平均置信度（0..1）
 *   - characterTagScore: 角色相关标签（category=4）的最高置信度（0..1），无角色标签则为 0
 *
 * 权重默认值：
 *   - 面积权重 0.35（画面占比大 = 主角可能性高）
 *   - YOLO 置信度权重 0.25
 *   - WD 标签置信度权重 0.20
 *   - 角色标签权重 0.20（有明确角色名 = 可信度高）
 */
class CharacterScorer(
    /** 面积权重 */
    private val areaWeight: Float = 0.35f,
    /** YOLO 置信度权重 */
    private val yoloConfidenceWeight: Float = 0.25f,
    /** WD Tagger 标签置信度权重 */
    private val wdTagConfidenceWeight: Float = 0.20f,
    /** 角色相关标签权重 */
    private val characterTagWeight: Float = 0.20f
) {

    /** 评分配置 */
    data class ScoreConfig(
        val areaWeight: Float = 0.35f,
        val yoloConfidenceWeight: Float = 0.25f,
        val wdTagConfidenceWeight: Float = 0.20f,
        val characterTagWeight: Float = 0.20f
    )

    /**
     * 计算单个实例的面积比例
     * @param mask 实例 mask（原图尺寸二值图）
     * @param imageWidth 原图宽度
     * @param imageHeight 原图高度
     * @return 面积比例 (0..1)
     */
    fun calculateAreaRatio(mask: Bitmap?, imageWidth: Int, imageHeight: Int): Float {
        if (mask == null || imageWidth <= 0 || imageHeight <= 0) return 0f
        val totalPixels = imageWidth * imageHeight
        if (totalPixels <= 0) return 0f

        // 采样计算 mask 白色像素数（步长优化，避免全图扫描过慢）
        val step = if (mask.width * mask.height > 500_000) 4 else 1
        var whitePixels = 0
        var sampled = 0
        val w = mask.width
        val h = mask.height
        val pixels = IntArray(w * h)
        mask.getPixels(pixels, 0, w, 0, 0, w, h)
        for (y in 0 until h step step) {
            for (x in 0 until w step step) {
                sampled++
                if (pixels[y * w + x] == Color.WHITE) whitePixels++
            }
        }
        if (sampled == 0) return 0f
        val ratio = whitePixels.toFloat() / sampled
        return ratio.coerceIn(0f, 1f)
    }

    /**
     * 计算 WD Tagger 标签的平均置信度
     * @param tags WD Tagger 输出的标签列表
     * @return 平均置信度 (0..1)，无标签返回 0
     */
    fun calculateWdTagConfidence(tags: List<TaggerEngine.Tag>): Float {
        if (tags.isEmpty()) return 0f
        val avgScore = tags.map { it.originalScore }.average().toFloat()
        return avgScore.coerceIn(0f, 1f)
    }

    /**
     * 计算角色相关标签的最高置信度
     * @param tags WD Tagger 输出的标签列表
     * @return 最高角色标签置信度 (0..1)，无角色标签返回 0
     */
    fun calculateCharacterTagScore(tags: List<TaggerEngine.Tag>): Float {
        val characterTags = tags.filter { it.category == 4 }
        if (characterTags.isEmpty()) return 0f
        return characterTags.maxOf { it.originalScore }.coerceIn(0f, 1f)
    }

    /**
     * 对单个角色实例进行综合评分
     *
     * @param mask 实例 mask
     * @param imageWidth 原图宽度
     * @param imageHeight 原图高度
     * @param yoloConfidence YOLO 检测置信度
     * @param tags WD Tagger 标签
     * @return 综合评分 (0..1)
     */
    fun score(
        mask: Bitmap?,
        imageWidth: Int,
        imageHeight: Int,
        yoloConfidence: Float,
        tags: List<TaggerEngine.Tag>
    ): Float {
        val areaScore = calculateAreaRatio(mask, imageWidth, imageHeight)
        val wdConfidence = calculateWdTagConfidence(tags)
        val charTagScore = calculateCharacterTagScore(tags)

        val score = areaWeight * areaScore +
            yoloConfidenceWeight * yoloConfidence.coerceIn(0f, 1f) +
            wdTagConfidenceWeight * wdConfidence +
            characterTagWeight * charTagScore

        return score.coerceIn(0f, 1f)
    }

    /**
     * 对一组角色实例进行评分并标记主/次角色
     *
     * @param instances 原始实例列表（score 未计算）
     * @param imageWidth 原图宽度
     * @param imageHeight 原图高度
     * @return 评分后的实例列表，按 score 降序排列，最高分标记为 isMain=true
     */
    fun scoreAndRank(
        instances: List<CharacterInstance>,
        imageWidth: Int,
        imageHeight: Int
    ): List<CharacterInstance> {
        if (instances.isEmpty()) return emptyList()

        val scored = instances.map { inst ->
            val calculatedScore = score(
                mask = inst.mask,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                yoloConfidence = inst.yoloConfidence,
                tags = inst.tags
            )
            inst.copy(score = calculatedScore)
        }

        // 按 score 降序排列
        val sorted = scored.sortedByDescending { it.score }

        // 标记主角色（score 最高）
        return if (sorted.isEmpty()) {
            emptyList()
        } else {
            sorted.mapIndexed { index, inst ->
                inst.copy(isMain = index == 0)
            }
        }
    }
}
