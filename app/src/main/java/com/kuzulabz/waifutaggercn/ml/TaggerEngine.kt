package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.Process
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.kuzulabz.waifutaggercn.R
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.FloatBuffer
import kotlin.math.exp

/**
 * Wraps an ONNX Runtime session for a WD-tagger-style image tagging model.
 *
 * Expects two files in app/src/main/assets/, supplied by you (not bundled
 * here):
 *   - model.onnx           the tagger model, e.g. a WD14/WD-v3 ONNX export
 *   - selected_tags.csv    header row + one row per class: id,name,category
 *
 * Compatibility mode tries common image tagger input variants automatically:
 * NHWC / NCHW layout, RGB / BGR channel order, and 0-255 / 0-1 float range.
 * It also flattens common ONNX output structures and can convert logits to probabilities.
 */
class TaggerEngine(private val context: Context) {

    /**
     * Inference performance mode: "power_saving" (big cores only),
     * "performance" (big + super cores), or "auto" (system default).
     */
    var highPerformanceMode: String = "performance"

    data class ModelConfig(
        val id: String,
        val displayName: String,
        val modelFile: File? = null,
        val tagsFile: File? = null,
        val isBuiltIn: Boolean = false
    )

    data class Tag(
        val name: String,
        val category: Int,
        val score: Float,
        val originalScore: Float = score
    )

    private var session: OrtSession? = null
    private var environment: OrtEnvironment? = null
    private var tagNames: List<String> = emptyList()
    private var tagCategories: List<Int> = emptyList()
    private var inputName: String = "input"
    private var inputLayout: InputLayout = InputLayout.NHWC
    private var preferredPreprocessMode: PreprocessMode? = null
    var inputSize: Int = 448
        private set
    var currentModelName: String = "WD Tagger"
        private set

    val isReady: Boolean
        get() = session != null && tagNames.isNotEmpty()

    private enum class InputLayout { NHWC, NCHW }

    private data class PreprocessMode(
        val layout: InputLayout,
        val rgb: Boolean,
        val scaleToUnit: Boolean
    )

    private data class PreparedInput(
        val buffer: FloatBuffer,
        val shape: LongArray
    )

    companion object {
        const val DEFAULT_MODEL_ID = "built_in_model"
        private const val MODEL_DIR_NAME = "ai_models"

        fun modelDirectory(context: Context): File {
            return File(context.filesDir, MODEL_DIR_NAME).apply { mkdirs() }
        }

        fun builtInModelConfig(): ModelConfig {
            return ModelConfig(
                id = DEFAULT_MODEL_ID,
                displayName = "assets/model.onnx",
                isBuiltIn = true
            )
        }

        /**
         * 判断内置 assets/model.onnx 是否真的存在。
         * APK 没有内置模型时，不应该把"内置模型"显示在列表里，否则会误导用户。
         */
        fun hasBuiltInModelAsset(context: Context): Boolean {
            return try {
                context.assets.open("model.onnx").use { stream ->
                    stream.read() >= 0
                }
            } catch (e: Exception) {
                false
            }
        }

        fun scanModelConfigs(context: Context): List<ModelConfig> {
            val modelDir = modelDirectory(context)
            android.util.Log.d("TaggerEngine", "scanModelConfigs: dir=${modelDir.absolutePath}, exists=${modelDir.exists()}")
            val allFiles = modelDir.listFiles()
            android.util.Log.d("TaggerEngine", "scanModelConfigs: listFiles=${allFiles?.size ?: "null"} files")
            allFiles?.forEach { f ->
                android.util.Log.d("TaggerEngine", "  file: ${f.name} (${f.length()}B, ext=${f.extension})")
            }
            val tagFiles = allFiles
                ?.filter { it.isFile && isSupportedTagFile(it) && it.length() > 0L }
                ?.sortedBy { file ->
                    tagFileSortKey(file)
                }
                .orEmpty()
            val modelFiles = allFiles
                ?.filter { it.isFile && it.extension.equals("onnx", ignoreCase = true) }
                ?.sortedBy { it.name.lowercase() }
                .orEmpty()
            val customModels = allFiles
                ?.filter { it.isFile && it.extension.equals("onnx", ignoreCase = true) }
                ?.sortedBy { it.name.lowercase() }
                ?.map { modelFile ->
                    val sameNameTags = tagFiles.firstOrNull {
                        it.nameWithoutExtension.equals(modelFile.nameWithoutExtension, ignoreCase = true)
                    }
                    ModelConfig(
                        id = modelFile.absolutePath,
                        displayName = friendlyModelName(modelFile.nameWithoutExtension),
                        modelFile = modelFile,
                        tagsFile = sameNameTags ?: findUnambiguousTagFile(modelFile, modelFiles, tagFiles),
                        isBuiltIn = false
                    )
                }
                .orEmpty()
            // 只有真的内置了 model.onnx 才显示"内置模型"，否则会误导用户
            val builtIn = if (hasBuiltInModelAsset(context)) listOf(builtInModelConfig()) else emptyList()
            return builtIn + customModels
        }

        private fun isSupportedTagFile(file: File): Boolean {
            return file.extension.lowercase() in setOf("csv", "json", "txt")
        }

        private fun tagFileSortKey(file: File): String {
            val name = file.name.lowercase()
            return when (name) {
                "selected_tags.csv" -> "00_$name"
                "tags.json", "selected_tags.json", "classes.json", "labels.json" -> "01_$name"
                "tags.txt", "selected_tags.txt", "classes.txt", "labels.txt" -> "02_$name"
                else -> "10_$name"
            }
        }

        private fun findUnambiguousTagFile(
            modelFile: File,
            modelFiles: List<File>,
            tagFiles: List<File>
        ): File? {
            if (tagFiles.isEmpty()) return null
            val modelKey = normalizePairingName(modelFile.nameWithoutExtension)
            tagFiles.firstOrNull {
                normalizePairingName(it.nameWithoutExtension) == modelKey
            }?.let { return it }

            // 只有“一个模型 + 一个标签表”时，才允许 selected_tags/tags 这种通用文件名自动配对。
            // 多模型共用一个标签表很容易错配，错配后会把输出下标解释成错误标签，导致提示词乱生成。
            if (modelFiles.size == 1 && tagFiles.size == 1) return tagFiles.first()
            if (modelFiles.size == 1) {
                return tagFiles.firstOrNull {
                    it.nameWithoutExtension.equals("selected_tags", ignoreCase = true) ||
                        it.nameWithoutExtension.equals("tags", ignoreCase = true) ||
                        it.nameWithoutExtension.equals("classes", ignoreCase = true) ||
                        it.nameWithoutExtension.equals("labels", ignoreCase = true)
                }
            }
            return null
        }

        private fun normalizePairingName(name: String): String {
            return name
                .lowercase()
                .replace(Regex("\\.(onnx|csv|json|txt)$"), "")
                .replace(Regex("[^a-z0-9]+"), "")
        }

        private fun friendlyModelName(baseName: String): String {
            return when (baseName.lowercase()) {
                "wd-convnext-tagger-v3" -> "WD ConvNeXt Tagger v3"
                "wd-swinv2-tagger-v3" -> "WD SwinV2 Tagger v3"
                "wd-vit-tagger-v3" -> "WD ViT Tagger v3"
                "wd-eva02-large-tagger-v3" -> "WD EVA02 Large Tagger v3"
                else -> baseName
                    .replace('_', ' ')
                    .replace('-', ' ')
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
            }
        }
    }

    private fun parseTagFile(file: File): List<Pair<String, Int>> {
        return when (file.extension.lowercase()) {
            "csv" -> parseCsvTagLines(file.readLines())
            "txt" -> parseTxtTagLines(file.readLines())
            "json" -> parseJsonTagText(file.readText())
            else -> emptyList()
        }
    }

    private fun parseCsvTagLines(lines: List<String>): List<Pair<String, Int>> {
        val usefulLines = lines.filter { it.isNotBlank() }
        if (usefulLines.isEmpty()) return emptyList()

        val header = splitCsvLine(usefulLines.first()).map { it.trim().trim('\uFEFF').lowercase() }
        val hasHeader = header.any { it in setOf("name", "tag", "label", "class", "caption", "category", "type", "tag_id", "id") }
        val nameIndex = if (hasHeader) {
            listOf("name", "tag", "label", "class", "caption")
                .firstNotNullOfOrNull { key -> header.indexOf(key).takeIf { it >= 0 } }
        } else null
        val categoryIndex = if (hasHeader) {
            listOf("category", "type")
                .firstNotNullOfOrNull { key -> header.indexOf(key).takeIf { it >= 0 } }
        } else null
        val dataLines = if (hasHeader) usefulLines.drop(1) else usefulLines

        return dataLines.mapNotNull { line ->
            val cols = splitCsvLine(line)
            if (cols.isEmpty()) return@mapNotNull null
            val name = when {
                nameIndex != null && nameIndex < cols.size -> cols[nameIndex].trim()
                cols.size >= 3 -> cols[1].trim()
                else -> cols.last().trim()
            }
            val category = when {
                categoryIndex != null && categoryIndex < cols.size -> cols[categoryIndex].trim().toIntOrNull() ?: 0
                cols.size >= 3 -> cols[2].trim().toIntOrNull() ?: 0
                else -> 0
            }
            name.takeIf { it.isNotEmpty() && !it.equals("name", ignoreCase = true) }?.let { it to category }
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i += 1
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i += 1
        }
        result.add(current.toString())
        return result
    }

    private fun parseTxtTagLines(lines: List<String>): List<Pair<String, Int>> {
        return lines.mapNotNull { line ->
            val tag = line.trim()
                .removePrefix("-")
                .trim()
                .substringBefore(",")
                .substringBefore("\t")
                .trim()
            tag.takeIf { it.isNotEmpty() }?.let { it to 0 }
        }
    }

    private fun parseJsonTagText(text: String): List<Pair<String, Int>> {
        val root = JSONTokener(text).nextValue()
        return when (root) {
            is JSONArray -> parseJsonArrayTags(root)
            is JSONObject -> parseJsonObjectTags(root)
            else -> emptyList()
        }
    }

    private fun parseJsonObjectTags(json: JSONObject): List<Pair<String, Int>> {
        val preferredKeys = listOf("tags", "labels", "classes", "names")
        preferredKeys.forEach { key ->
            val value = json.opt(key)
            if (value is JSONArray) return parseJsonArrayTags(value)
        }
        val result = mutableListOf<Pair<String, Int>>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.opt(key)
            when (value) {
                is String -> result.add(value to 0)
                is Number -> result.add(key to 0)
                is JSONObject -> extractJsonTagName(value)?.let { result.add(it) }
            }
        }
        return result
    }

    private fun parseJsonArrayTags(array: JSONArray): List<Pair<String, Int>> {
        val result = mutableListOf<Pair<String, Int>>()
        for (i in 0 until array.length()) {
            when (val item = array.opt(i)) {
                is String -> result.add(item to 0)
                is JSONObject -> extractJsonTagName(item)?.let { result.add(it) }
            }
        }
        return result
    }

    private fun extractJsonTagName(json: JSONObject): Pair<String, Int>? {
        val name = listOf("name", "tag", "label", "class", "caption")
            .firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
            ?: return null
        val category = json.optInt("category", json.optInt("type", 0))
        return name.trim() to category
    }

    /** Returns null on success, or a human-readable error message. */
    fun load(modelConfig: ModelConfig = builtInModelConfig()): String? {
        runCatching { session?.close() }
        session = null
        tagNames = emptyList()
        tagCategories = emptyList()
        inputSize = 448
        inputLayout = InputLayout.NHWC
        preferredPreprocessMode = null

        val modelFile = modelConfig.modelFile ?: File(context.filesDir, "model.onnx")
        try {
            if (modelConfig.isBuiltIn && (!modelFile.exists() || modelFile.length() == 0L)) {
                context.assets.open("model.onnx").use { input ->
                    modelFile.outputStream().use { output ->
                        val buffer = ByteArray(1 shl 20) // 1MB chunks
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                        }
                    }
                }
            }
            if (!modelFile.exists() || modelFile.length() == 0L) {
                return context.getString(R.string.model_onnx_missing, modelConfig.displayName)
            }
        } catch (e: Throwable) {
            return context.getString(R.string.model_onnx_missing, e.message ?: "")
        }

        val env = try {
            OrtEnvironment.getEnvironment().also { environment = it }
        } catch (e: Throwable) {
            return context.getString(R.string.model_load_failed, e.message ?: e.javaClass.simpleName)
        }

        session = try {
            env.createSession(modelFile.absolutePath)
        } catch (e: Throwable) {
            return context.getString(R.string.model_load_failed, e.message ?: "")
        }

        session?.inputNames?.firstOrNull()?.let { inputName = it }
        session?.inputInfo?.get(inputName)?.info?.let { info ->
            // Try to read the model's expected spatial size if it's static.
            val shape = (info as? ai.onnxruntime.TensorInfo)?.shape
            if (shape != null && shape.size == 4) {
                when {
                    shape[1] == 3L -> {
                        inputLayout = InputLayout.NCHW
                        val spatial = listOf(shape[2], shape[3]).firstOrNull { it > 0 } ?: 448L
                        inputSize = spatial.toInt()
                    }
                    shape[3] == 3L -> {
                        inputLayout = InputLayout.NHWC
                        val spatial = listOf(shape[1], shape[2]).firstOrNull { it > 0 } ?: 448L
                        inputSize = spatial.toInt()
                    }
                    else -> {
                        val spatial = shape.firstOrNull { it > 3 } ?: 448L
                        inputSize = spatial.toInt()
                    }
                }
            }
        }

        val names = mutableListOf<String>()
        val cats = mutableListOf<Int>()
        try {
            val tagsFile = modelConfig.tagsFile
            if (tagsFile != null) {
                val parsedTags = parseTagFile(tagsFile)
                names.addAll(parsedTags.map { it.first })
                cats.addAll(parsedTags.map { it.second })
            } else if (!modelConfig.isBuiltIn) {
                return "找不到与 ${modelConfig.displayName} 匹配的标签表。请导入同源的 selected_tags.csv / tags.csv / labels.txt / classes.json，最好与模型文件同名。"
            } else {
                try {
                    context.assets.open("selected_tags.csv").use { stream ->
                        BufferedReader(InputStreamReader(stream)).useLines { lines ->
                            parseCsvTagLines(lines.toList()).forEach { tag ->
                                names.add(tag.first)
                                cats.add(tag.second)
                            }
                        }
                    }
                } catch (_: Throwable) {
                    return context.getString(R.string.selected_tags_missing)
                }
            }
        } catch (e: Throwable) {
            return context.getString(R.string.selected_tags_missing, e.message ?: "")
        }
        if (names.isEmpty()) {
            return context.getString(R.string.selected_tags_missing)
        }
        val outputTagCount = inferOutputTagCount()
        if (outputTagCount > 0 && outputTagCount != names.size) {
            return "模型标签数量不匹配：ONNX 输出 $outputTagCount 项，但标签表有 ${names.size} 项。请导入与该模型同源的标签文件，避免提示词错位。"
        }
        tagNames = names
        tagCategories = cats
        currentModelName = modelConfig.displayName

        return null
    }

    private fun inferOutputTagCount(): Int {
        val outputInfo = session?.outputInfo?.values?.firstOrNull()?.info as? ai.onnxruntime.TensorInfo
        val shape = outputInfo?.shape ?: return 0
        val lastDim = shape.lastOrNull() ?: return 0
        if (lastDim > 1) return lastDim.toInt()
        return shape.drop(1).firstOrNull { it > 1 }?.toInt() ?: 0
    }

    private fun preprocess(bitmap: Bitmap, mode: PreprocessMode): PreparedInput {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val buffer = FloatBuffer.allocate(inputSize * inputSize * 3)
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        val scale = if (mode.scaleToUnit) 1f / 255f else 1f
        if (mode.layout == InputLayout.NHWC) {
            for (p in pixels) {
                putPixelChannels(buffer, p, mode.rgb, scale)
            }
        } else {
            val channelValues = Array(3) { FloatArray(inputSize * inputSize) }
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = ((p shr 16) and 0xFF) * scale
                val g = ((p shr 8) and 0xFF) * scale
                val b = (p and 0xFF) * scale
                if (mode.rgb) {
                    channelValues[0][i] = r
                    channelValues[1][i] = g
                    channelValues[2][i] = b
                } else {
                    channelValues[0][i] = b
                    channelValues[1][i] = g
                    channelValues[2][i] = r
                }
            }
            channelValues.forEach { values ->
                values.forEach { buffer.put(it) }
            }
        }
        buffer.rewind()
        val shape = when (mode.layout) {
            InputLayout.NHWC -> longArrayOf(1, inputSize.toLong(), inputSize.toLong(), 3)
            InputLayout.NCHW -> longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())
        }
        return PreparedInput(buffer, shape)
    }

    private fun putPixelChannels(buffer: FloatBuffer, pixel: Int, rgb: Boolean, scale: Float) {
        val r = ((pixel shr 16) and 0xFF) * scale
        val g = ((pixel shr 8) and 0xFF) * scale
        val b = (pixel and 0xFF) * scale
        if (rgb) {
            buffer.put(r)
            buffer.put(g)
            buffer.put(b)
        } else {
            buffer.put(b)
            buffer.put(g)
            buffer.put(r)
        }
    }

    /**
     * Runs inference and returns smart-processed tags:
     * - duplicate names are merged automatically
     * - general and character tags can be re-weighted
     * - final tags are sorted by adjusted score in descending order
     */
    fun tag(
        bitmap: Bitmap,
        threshold: Float,
        generalWeight: Float = 1f,
        characterWeight: Float = 1f
    ): List<Tag> {
        val currentSession = session ?: return emptyList()
        val env = environment ?: return emptyList()

        val prevPriority = when (highPerformanceMode) {
            "power_saving" -> Process.getThreadPriority(Process.myTid()).also {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE)
            }
            "performance" -> Process.getThreadPriority(Process.myTid()).also {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            }
            else -> null // "auto" — let system scheduler decide
        }

        return try {
            var bestFallback: List<Tag> = emptyList()
            for (mode in compatiblePreprocessModes()) {
                val result = runCatching {
                    val prepared = preprocess(bitmap, mode)
                    OnnxTensor.createTensor(env, prepared.buffer, prepared.shape).use { tensor ->
                        currentSession.run(mapOf(inputName to tensor)).use { results ->
                            val scores = extractBestScores(results)
                            if (scores.isEmpty()) return@use emptyList()
                            val tagsAboveThreshold = buildTagsFromScores(
                                scores = scores,
                                threshold = threshold,
                                generalWeight = generalWeight,
                                characterWeight = characterWeight,
                                fallbackTopTags = false
                            )
                            val fallbackCandidate = buildTagsFromScores(
                                scores = scores,
                                threshold = threshold,
                                generalWeight = generalWeight,
                                characterWeight = characterWeight,
                                fallbackTopTags = true
                            )
                            if (fallbackCandidate.firstOrNull()?.score ?: 0f > (bestFallback.firstOrNull()?.score ?: 0f)) {
                                bestFallback = fallbackCandidate
                            }
                            tagsAboveThreshold
                        }
                    }
                }.getOrDefault(emptyList())
                if (result.isNotEmpty()) {
                    preferredPreprocessMode = mode
                    return result
                }
            }
            bestFallback
        } finally {
            prevPriority?.let { Process.setThreadPriority(it) }
        }
    }

    private fun compatiblePreprocessModes(): List<PreprocessMode> {
        val base = listOf(
            PreprocessMode(inputLayout, rgb = false, scaleToUnit = false),
            PreprocessMode(inputLayout, rgb = true, scaleToUnit = false),
            PreprocessMode(inputLayout, rgb = false, scaleToUnit = true),
            PreprocessMode(inputLayout, rgb = true, scaleToUnit = true),
            PreprocessMode(if (inputLayout == InputLayout.NHWC) InputLayout.NCHW else InputLayout.NHWC, rgb = false, scaleToUnit = false),
            PreprocessMode(if (inputLayout == InputLayout.NHWC) InputLayout.NCHW else InputLayout.NHWC, rgb = true, scaleToUnit = false),
            PreprocessMode(if (inputLayout == InputLayout.NHWC) InputLayout.NCHW else InputLayout.NHWC, rgb = false, scaleToUnit = true),
            PreprocessMode(if (inputLayout == InputLayout.NHWC) InputLayout.NCHW else InputLayout.NHWC, rgb = true, scaleToUnit = true)
        )
        return (listOfNotNull(preferredPreprocessMode) + base).distinct()
    }

    private fun extractBestScores(results: OrtSession.Result): FloatArray {
        val candidates = mutableListOf<FloatArray>()
        val iterator = results.iterator()
        while (iterator.hasNext()) {
            val output = iterator.next().value.value
            val values = mutableListOf<Float>()
            flattenOutputValues(output, values)
            if (values.isNotEmpty()) candidates.add(values.toFloatArray())
        }
        if (candidates.isEmpty()) return FloatArray(0)
        return candidates.maxByOrNull { scores ->
            if (tagNames.isEmpty()) scores.size else minOf(scores.size, tagNames.size)
        } ?: FloatArray(0)
    }

    private fun flattenOutputValues(value: Any?, out: MutableList<Float>) {
        when (value) {
            is FloatArray -> out.addAll(value.toList())
            is DoubleArray -> value.forEach { out.add(it.toFloat()) }
            is IntArray -> value.forEach { out.add(it.toFloat()) }
            is LongArray -> value.forEach { out.add(it.toFloat()) }
            is Array<*> -> value.forEach { flattenOutputValues(it, out) }
            is Number -> out.add(value.toFloat())
        }
    }

    private fun buildTagsFromScores(
        scores: FloatArray,
        threshold: Float,
        generalWeight: Float,
        characterWeight: Float,
        fallbackTopTags: Boolean
    ): List<Tag> {
        val normalizedScores = normalizeOutputScores(scores)
        val bestByName = linkedMapOf<String, Tag>()
        val maxCount = minOf(normalizedScores.size, tagNames.size)
        for (i in 0 until maxCount) {
            val rawScore = normalizedScores[i]
            val name = tagNames[i].trim()
            if (name.isEmpty()) continue
            val category = tagCategories.getOrElse(i) { 0 }
            val adjustedScore = rawScore * weightForCategory(category, generalWeight, characterWeight)
            if (fallbackTopTags || adjustedScore >= threshold) {
                val key = normalizeTagName(name)
                val candidate = Tag(name, category, adjustedScore, rawScore)
                val existing = bestByName[key]
                if (existing == null || candidate.score > existing.score) {
                    bestByName[key] = candidate
                }
            }
        }
        val sorted = bestByName.values.sortedWith(
            compareByDescending<Tag> { it.score }.thenBy { it.name.lowercase() }
        )
        return if (fallbackTopTags) sorted.take(20) else sorted
    }

    private fun normalizeOutputScores(scores: FloatArray): FloatArray {
        val finite = scores.filter { it.isFinite() }
        if (finite.isEmpty()) return scores
        val minScore = finite.minOrNull() ?: 0f
        val maxScore = finite.maxOrNull() ?: 0f
        val looksLikeLogits = minScore < 0f || maxScore > 1.5f
        return if (looksLikeLogits) {
            FloatArray(scores.size) { index ->
                sigmoid(scores[index])
            }
        } else {
            scores
        }
    }

    private fun sigmoid(value: Float): Float {
        return (1.0 / (1.0 + exp(-value.toDouble()))).toFloat()
    }

    private fun weightForCategory(category: Int, generalWeight: Float, characterWeight: Float): Float {
        return when (category) {
            0 -> generalWeight
            4 -> characterWeight
            else -> 1f
        }.coerceIn(0.1f, 2.5f)
    }

    private fun normalizeTagName(name: String): String {
        return name.trim()
            .lowercase()
            .replace(' ', '_')
            .replace(Regex("_+"), "_")
    }

    fun close() {
        runCatching { session?.close() }
        session = null
    }
}
