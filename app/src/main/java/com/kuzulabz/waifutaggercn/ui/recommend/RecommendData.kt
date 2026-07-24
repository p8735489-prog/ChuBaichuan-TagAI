package com.kuzulabz.waifutaggercn.ui.recommend

import org.json.JSONArray
import org.json.JSONObject

/**
 * 推荐作品数据模型。
 * 统一表示 Civitai（国际）和 Waifu.im（国内）两种数据源。
 */
data class RecommendItem(
    val id: Long,
    val imageUrl: String,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val prompt: String,
    val negativePrompt: String,
    val modelName: String,
    val username: String,
    val likeCount: Int,
    /** 数据来源类型 */
    val source: RecommendApiConfig.ApiSource = RecommendApiConfig.ApiSource.CIVITAI
)

// ═══════════════════════════════════════════════════════════════════
// Civitai 数据解析（国际用户）
// ═══════════════════════════════════════════════════════════════════

/**
 * 将图片 URL 中的官方 CDN 域名替换为镜像域名。
 */
fun RecommendItem.rewriteUrls(cdnHost: String): RecommendItem {
    fun rewrite(url: String): String {
        var result = url
        for (host in RecommendApiConfig.officialCivitaiCdnHosts) {
            result = result.replace(host, cdnHost)
        }
        return result
    }
    return copy(
        imageUrl = rewrite(imageUrl),
        thumbnailUrl = rewrite(thumbnailUrl)
    )
}

/**
 * 解析 Civitai API 响应为 RecommendItem 列表。
 */
fun parseCivitaiImages(jsonString: String): List<RecommendItem> {
    val result = mutableListOf<RecommendItem>()
    try {
        val root = JSONObject(jsonString)
        val items = root.optJSONArray("items") ?: return result
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val id = item.optLong("id", 0)
            val url = item.optString("url", "")
            val width = item.optInt("width", 512)
            val height = item.optInt("height", 512)
            val meta = item.optJSONObject("meta")
            val prompt = meta?.optString("prompt", "") ?: ""
            val negativePrompt = meta?.optString("negativePrompt", "") ?: ""
            val username = item.optString("username", "")
            val stats = item.optJSONObject("stats")
            val likeCount = stats?.optInt("likeCount", 0) ?: 0
            val modelName = parseCivitaiModelName(item)

            // 生成缩略图 URL（Civitai 图片 URL 加 width 参数）
            val thumbnailUrl = if (url.contains("?")) "$url&width=480" else "$url?width=480"

            if (url.isNotBlank()) {
                result.add(
                    RecommendItem(
                        id = id,
                        imageUrl = url,
                        thumbnailUrl = thumbnailUrl,
                        width = width,
                        height = height,
                        prompt = prompt,
                        negativePrompt = negativePrompt,
                        modelName = modelName,
                        username = username,
                        likeCount = likeCount,
                        source = RecommendApiConfig.ApiSource.CIVITAI
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

private fun parseCivitaiModelName(item: JSONObject): String {
    val meta = item.optJSONObject("meta")
    val resource = meta?.optJSONObject("Model")
    if (resource != null) {
        return resource.optString("name", "")
    }
    val post = item.optJSONObject("post")
    if (post != null) {
        val resources = post.optJSONArray("resources")
        if (resources != null && resources.length() > 0) {
            return resources.getJSONObject(0).optString("name", "")
        }
    }
    return ""
}

// ═══════════════════════════════════════════════════════════════════
// Waifu.im 数据解析（国内用户）
// ═══════════════════════════════════════════════════════════════════

/**
 * 解析 Waifu.im API 响应为 RecommendItem 列表。
 * Waifu.im 返回的是精选二次元动漫图片（来源 Pixiv），
 * 带有标签（tags）和画师（artists）信息。
 *
 * Waifu.im API 响应格式：
 * {
 *   "items": [
 *     {
 *       "id": 6758,
 *       "url": "https://cdn.waifu.im/6758.png",
 *       "extension": ".png",
 *       "width": 1600,
 *       "height": 2088,
 *       "source": "https://www.pixiv.net/en/artworks/81300646",
 *       "tags": [
 *         { "id": 7, "name": "Oppai", "slug": "oppai", "description": "..." }
 *       ],
 *       "artists": [
 *         { "id": 838, "name": "ネコサン＠お仕事募集中", "pixiv": "...", "twitter": "..." }
 *       ],
 *       "favorites": 20,
 *       "isNsfw": false,
 *       "dominantColor": "#eee6eb"
 *     }
 *   ],
 *   "pageNumber": 1,
 *   "totalPages": 5,
 *   "totalCount": 100,
 *   "hasNextPage": true
 * }
 */
fun parseWaifuImImages(jsonString: String): List<RecommendItem> {
    val result = mutableListOf<RecommendItem>()
    try {
        val root = JSONObject(jsonString)
        val items = root.optJSONArray("items") ?: return result
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val id = item.optLong("id", 0)
            val url = item.optString("url", "")
            val width = item.optInt("width", 0)
            val height = item.optInt("height", 0)

            if (url.isBlank()) continue

            // 提取标签作为"提示词"——标签是 AI 生图常用的关键词
            val tags = item.optJSONArray("tags")
            val tagNames = mutableListOf<String>()
            if (tags != null) {
                for (j in 0 until tags.length()) {
                    val tag = tags.getJSONObject(j)
                    val name = tag.optString("name", "")
                    if (name.isNotBlank()) tagNames.add(name)
                }
            }
            val prompt = tagNames.joinToString(", ")

            // 提取画师名
            val artists = item.optJSONArray("artists")
            val artistName = if (artists != null && artists.length() > 0) {
                artists.getJSONObject(0).optString("name", "")
            } else ""

            // 收藏数
            val favorites = item.optInt("favorites", 0)

            // 缩略图用同一 URL（cdn.waifu.im 的图片已经是优化过的）
            val thumbnailUrl = url

            // 生成唯一 ID（加偏移避免和 Civitai 冲突）
            val waifuId = 30_000_000L + id

            result.add(
                RecommendItem(
                    id = waifuId,
                    imageUrl = url,
                    thumbnailUrl = thumbnailUrl,
                    width = width,
                    height = height,
                    prompt = prompt,
                    negativePrompt = "",
                    modelName = "",
                    username = artistName,
                    likeCount = favorites,
                    source = RecommendApiConfig.ApiSource.WAIFU_IM
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}