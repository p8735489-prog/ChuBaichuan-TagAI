package com.kuzulabz.waifutaggercn.ui.recommend

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kuzulabz.waifutaggercn.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// ─── Civitai API 路径 ────────────────────────────────────────────
private const val CIVITAI_API_PATH =
    "/api/v1/images?limit=30&sort=Most%20Reactions&period=Week&nsfw=false"

// 整体请求超时（秒），防止网络请求无限挂起
private const val FETCH_TIMEOUT_SECONDS = 15L

// ─── 首次使用弹窗标记 ──────────────────────────────────────────────
private const val PREFS_RECOMMEND = "recommend_prefs"
private const val KEY_FIRST_LAUNCH_SEEN = "first_launch_dialog_seen"
private const val KEY_RECOMMEND_REJECTED = "recommend_rejected"

// ─── 推荐页主入口 ──────────────────────────────────────────────────
@Composable
fun RecommendationPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<RecommendItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<RecommendItem?>(null) }

    // 当前 API 来源
    val apiSource = remember { RecommendApiConfig.currentSource }

    // 首次使用弹窗
    val prefs = remember { context.getSharedPreferences(PREFS_RECOMMEND, Context.MODE_PRIVATE) }
    var recommendRejected by remember { mutableStateOf(prefs.getBoolean(KEY_RECOMMEND_REJECTED, false)) }
    var showFirstLaunchDialog by remember {
        mutableStateOf(!prefs.getBoolean(KEY_FIRST_LAUNCH_SEEN, false) && !recommendRejected)
    }
    var hasAgreed by remember { mutableStateOf(prefs.getBoolean(KEY_FIRST_LAUNCH_SEEN, false)) }
    var showRejectedDialog by remember { mutableStateOf(false) }
    var dataLoaded by remember { mutableStateOf(false) }

    // 当前实际使用的数据源（支持自动降级）
    var activeSource by remember { mutableStateOf(RecommendApiConfig.currentSource) }

    // 加载数据
    LaunchedEffect(hasAgreed, recommendRejected) {
        if (!hasAgreed || recommendRejected) return@LaunchedEffect
        if (dataLoaded) return@LaunchedEffect

        if (!isNetworkAvailable(context)) {
            Log.w("Recommend", "Network unavailable, skipping fetch")
            hasError = true
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        hasError = false

        try {
            var result = withContext(Dispatchers.IO) {
                withTimeoutOrNull(FETCH_TIMEOUT_SECONDS * 1000) {
                    fetchRecommendImages(activeSource)
                }
            }

            // 如果主源失败且当前是 CIVITAI，尝试降级到 WAIFU_IM
            if ((result == null || result.isEmpty()) && activeSource == RecommendApiConfig.ApiSource.CIVITAI) {
                Log.w("Recommend", "Civitai failed, trying fallback to Waifu.im")
                activeSource = RecommendApiConfig.ApiSource.WAIFU_IM
                result = withContext(Dispatchers.IO) {
                    withTimeoutOrNull(FETCH_TIMEOUT_SECONDS * 1000) {
                        fetchRecommendImages(RecommendApiConfig.ApiSource.WAIFU_IM)
                    }
                }
            }

            if (result == null || result.isEmpty()) {
                Log.e("Recommend", "All fetch attempts failed")
                hasError = true
            } else {
                items = result
                dataLoaded = true
            }
        } catch (e: Exception) {
            Log.e("Recommend", "LaunchedEffect fetch crashed", e)
            hasError = true
        } finally {
            isLoading = false
        }
    }

    // 首次弹窗
    if (showFirstLaunchDialog) {
        RecommendFirstLaunchDialog(
            onAgree = {
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH_SEEN, true).apply()
                showFirstLaunchDialog = false
                hasAgreed = true
            },
            onLater = {
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH_SEEN, true).apply()
                showFirstLaunchDialog = false
                isLoading = false
            },
            onReject = {
                prefs.edit()
                    .putBoolean(KEY_FIRST_LAUNCH_SEEN, true)
                    .putBoolean(KEY_RECOMMEND_REJECTED, true)
                    .apply()
                recommendRejected = true
                showFirstLaunchDialog = false
                isLoading = false
            }
        )
    }

    // 拒绝后重新开启弹窗
    if (showRejectedDialog) {
        AlertDialog(
            onDismissRequest = { showRejectedDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(R.string.recommend_rejected_title)) },
            text = {
                Text(
                    stringResource(R.string.recommend_rejected_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectedDialog = false
                        recommendRejected = false
                        prefs.edit()
                            .putBoolean(KEY_RECOMMEND_REJECTED, false)
                            .putBoolean(KEY_FIRST_LAUNCH_SEEN, true)
                            .apply()
                        hasAgreed = true
                        dataLoaded = false
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.recommend_rejected_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(modifier = modifier) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.main_tab_recommend),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hasError) {
                IconButton(onClick = {
                    scope.launch {
                        if (!isNetworkAvailable(context)) {
                            Toast.makeText(context, context.getString(R.string.recommend_no_network), Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        isLoading = true
                        hasError = false
                        try {
                            val result = withContext(Dispatchers.IO) {
                                withTimeoutOrNull(FETCH_TIMEOUT_SECONDS * 1000) {
                                    fetchRecommendImages(activeSource)
                                }
                            }
                            if (result == null || result.isEmpty()) {
                                hasError = true
                            } else {
                                items = result
                                dataLoaded = true
                            }
                        } catch (e: Exception) {
                            Log.e("Recommend", "Retry fetch crashed", e)
                            hasError = true
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.recommend_retry),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 内容区域
        when {
            recommendRejected -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.recommend_rejected_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.recommend_rejected_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            onClick = { showRejectedDialog = true },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.recommend_rejected_yes))
                        }
                    }
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.recommend_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            hasError && items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.recommend_load_failed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    if (!isNetworkAvailable(context)) {
                                        Toast.makeText(context, context.getString(R.string.recommend_no_network), Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    isLoading = true
                                    hasError = false
                                    try {
                                        val result = withContext(Dispatchers.IO) {
                                            withTimeoutOrNull(FETCH_TIMEOUT_SECONDS * 1000) {
                                                fetchRecommendImages(activeSource)
                                            }
                                        }
                                        if (result == null || result.isEmpty()) {
                                            hasError = true
                                        } else {
                                            items = result
                                            dataLoaded = true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Recommend", "Retry fetch crashed", e)
                                        hasError = true
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.recommend_retry))
                        }
                    }
                }
            }
            items.isEmpty() && dataLoaded -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.recommend_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items.isNotEmpty() -> {
                // 瀑布流网格
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    items(items, key = { it.id }) { item ->
                        RecommendCard(
                            item = item,
                            onClick = { selectedItem = item }
                        )
                    }
                }
            }
        }
    }

    // 详情弹窗
    selectedItem?.let { item ->
        RecommendDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onCopyPrompt = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("prompt", item.prompt))
                Toast.makeText(context, context.getString(R.string.recommend_prompt_copied), Toast.LENGTH_SHORT).show()
            },
            onSaveImage = {
                scope.launch {
                    val saved = withContext(Dispatchers.IO) {
                        downloadAndSaveImage(context, item.imageUrl, item.id)
                    }
                    if (saved) {
                        Toast.makeText(context, context.getString(R.string.recommend_image_saved), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.recommend_load_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

// ─── 瀑布流卡片 ───────────────────────────────────────────────────
@Composable
private fun RecommendCard(
    item: RecommendItem,
    onClick: () -> Unit
) {
    val aspectRatio = if (item.height > 0) item.width.toFloat() / item.height.toFloat() else 0.75f
    val isWaifuIm = item.source == RecommendApiConfig.ApiSource.WAIFU_IM
    val localContext = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // 图片（使用 SubcomposeAsyncImage 避免加载失败时崩溃）
            val imageModel = remember(item.thumbnailUrl) {
                ImageRequest.Builder(localContext)
                    .data(item.thumbnailUrl)
                    .crossfade(true)
                    .build()
            }
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                success = {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )

            // 底部信息
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Waifu.im 来源时显示标签（截断2行）
                if (item.prompt.isNotBlank()) {
                    Text(
                        text = item.prompt,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }

                // 底部信息行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (item.username.isNotBlank())
                            stringResource(R.string.recommend_by) + item.username
                        else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Waifu.im 来源显示标记
                    if (isWaifuIm) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Text(
                                text = "Waifu.im",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                maxLines = 1
                            )
                        }
                    }
                    if (item.likeCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatCount(item.likeCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── 首次使用弹窗 ──────────────────────────────────────────────────
@Composable
private fun RecommendFirstLaunchDialog(
    onAgree: () -> Unit,
    onLater: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(
        onDismissRequest = onLater,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // 标题
                Text(
                    text = stringResource(R.string.recommend_first_launch_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 说明文字
                Text(
                    text = stringResource(R.string.recommend_first_launch_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                )

                // 按钮
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAgree,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.recommend_first_launch_agree),
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.recommend_first_launch_reject),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        TextButton(
                            onClick = onLater,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.recommend_first_launch_later),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 详情弹窗 ─────────────────────────────────────────────────────
@Composable
private fun RecommendDetailDialog(
    item: RecommendItem,
    onDismiss: () -> Unit,
    onCopyPrompt: () -> Unit,
    onSaveImage: () -> Unit
) {
    val isWaifuIm = item.source == RecommendApiConfig.ApiSource.WAIFU_IM

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.recommend_detail_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                }

                // 可滚动内容
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // 全图展示
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Waifu.im 来源标记
                    if (isWaifuIm) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Waifu.im · ${item.username}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 提示词/描述
                    if (item.prompt.isNotBlank()) {
                        Text(
                            text = if (isWaifuIm) stringResource(R.string.recommend_description_label) else stringResource(R.string.recommend_prompt_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 负面提示词（仅 Civitai）
                    if (!isWaifuIm && item.negativePrompt.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.recommend_negative_prompt_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.negativePrompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 模型名（仅 Civitai）
                    if (!isWaifuIm && item.modelName.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.recommend_model_label) + item.modelName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // 底部操作按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyPrompt,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.recommend_copy_prompt))
                    }
                    Button(
                        onClick = onSaveImage,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.recommend_save_image))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// 网络请求 — 区域分流
// ═══════════════════════════════════════════════════════════════════

/**
 * 统一入口：根据指定数据源获取图片。
 */
private fun fetchRecommendImages(source: RecommendApiConfig.ApiSource): List<RecommendItem> {
    return when (source) {
        RecommendApiConfig.ApiSource.WAIFU_IM -> fetchWaifuImImages()
        RecommendApiConfig.ApiSource.CIVITAI -> fetchCivitaiImages()
    }
}

private fun fetchRecommendImages(): List<RecommendItem> {
    return fetchRecommendImages(RecommendApiConfig.currentSource)
}

// ─── Civitai API（国际用户）────────────────────────────────────────

private fun fetchCivitaiImages(): List<RecommendItem> {
    for (host in RecommendApiConfig.civitaiApiHosts) {
        val result = tryFetchCivitaiFromHost(host)
        if (result.isNotEmpty()) return result
        Log.w("Recommend", "[$host] failed or empty, trying next host")
    }
    Log.e("Recommend", "All Civitai hosts failed")
    return emptyList()
}

private fun tryFetchCivitaiFromHost(host: String): List<RecommendItem> {
    var connection: HttpURLConnection? = null
    return try {
        val apiUrl = "https://$host$CIVITAI_API_PATH"
        Log.d("Recommend", "[Civitai] Fetching: $apiUrl")
        val url = URL(apiUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "ChuBaichuan-TagAI/1.0")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Connection", "close")
        connection.instanceFollowRedirects = true

        val responseCode = connection.responseCode
        Log.d("Recommend", "[Civitai][$host] Response code: $responseCode")
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.w("Recommend", "[Civitai][$host] Non-200: $responseCode")
            return emptyList()
        }

        val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
        Log.d("Recommend", "[Civitai][$host] Response length: ${jsonString.length}")
        val parsed = parseCivitaiImages(jsonString)
        Log.d("Recommend", "[Civitai][$host] Parsed ${parsed.size} items")

        val cdnHost = RecommendApiConfig.getCivitaiCdnHost(host)
        parsed.map { it.rewriteUrls(cdnHost) }
    } catch (e: Exception) {
        Log.e("Recommend", "[Civitai][$host] fetch failed", e)
        emptyList()
    } finally {
        connection?.disconnect()
    }
}

// ─── Waifu.im API（国内用户）─────────────────────────────────────────

/**
 * 从 Waifu.im 获取二次元动漫图片。
 * 无需 API Key，免费使用。
 * 每次随机选一个标签获取多样化内容。
 */
private fun fetchWaifuImImages(): List<RecommendItem> {
    val tag = RecommendApiConfig.waifuImSearchTags.random()
    Log.d("Recommend", "[Waifu.im] Searching with tag: $tag")

    // 构建 URL：标签过滤 + SFW + 分页30张
    val url = StringBuilder(RecommendApiConfig.WAIFU_IM_API_URL)
        .append("?IncludedTags=").append(URLEncoder.encode(tag, "UTF-8"))
        .append("&IsNsfw=False")
        .append("&PageSize=30")
        .toString()

    // 排除不合适标签
    val excludedParams = RecommendApiConfig.waifuImExcludedTags.joinToString("&") {
        "ExcludedTags=${URLEncoder.encode(it, "UTF-8")}"
    }
    val finalUrl = "$url&$excludedParams"

    return tryFetchWaifuImPage(finalUrl)
}

private fun tryFetchWaifuImPage(url: String): List<RecommendItem> {
    var connection: HttpURLConnection? = null
    return try {
        Log.d("Recommend", "[Waifu.im] Fetching: ${url.take(120)}...")
        connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "ChuBaichuan-TagAI/1.0")
        connection.setRequestProperty("Accept", "application/json")
        connection.instanceFollowRedirects = true

        val responseCode = connection.responseCode
        Log.d("Recommend", "[Waifu.im] Response code: $responseCode")
        if (responseCode != HttpURLConnection.HTTP_OK) {
            val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }?.take(300)
            Log.e("Recommend", "[Waifu.im] Error $responseCode: $errorBody")
            return emptyList()
        }

        val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
        Log.d("Recommend", "[Waifu.im] Response length: ${jsonString.length}")
        val parsed = parseWaifuImImages(jsonString)
        Log.d("Recommend", "[Waifu.im] Parsed ${parsed.size} items")
        parsed
    } catch (e: Exception) {
        Log.e("Recommend", "[Waifu.im] fetch failed", e)
        emptyList()
    } finally {
        connection?.disconnect()
    }
}

// ─── 图片下载保存 ─────────────────────────────────────────────────
private fun downloadAndSaveImage(context: Context, imageUrl: String, id: Long): Boolean {
    var connection: HttpURLConnection? = null
    return try {
        val url = URL(imageUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 30_000
        connection.readTimeout = 30_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "ChuBaichuan-TagAI/1.0")
        connection.instanceFollowRedirects = true

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.e("Recommend", "Image download failed: HTTP $responseCode for $imageUrl")
            return false
        }

        val inputStream: InputStream = connection.inputStream
        val bitmap = BitmapFactory.decodeStream(inputStream)

        if (bitmap == null) return false

        val filename = "recommend_${id}.jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CueWord")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)
            }
            true
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = java.io.File(dir, "CueWord/$filename")
            file.parentFile?.mkdirs()
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, file.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            true
        }
    } catch (e: Exception) {
        Log.e("Recommend", "Image download failed", e)
        false
    } finally {
        connection?.disconnect()
    }
}

// ─── 工具函数 ─────────────────────────────────────────────────────
private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> "${count / 1000}k"
        count >= 1000 -> "${String.format("%.1f", count / 1000f)}k"
        else -> count.toString()
    }
}

/**
 * 检查设备当前是否有可用的网络连接。
 */
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return true
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}