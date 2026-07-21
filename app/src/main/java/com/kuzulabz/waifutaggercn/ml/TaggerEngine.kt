package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.kuzulabz.waifutaggercn.R
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.nio.FloatBuffer

/**
 * Wraps an ONNX Runtime session for a WD-tagger-style image tagging model.
 *
 * Expects two files in app/src/main/assets/, supplied by you (not bundled
 * here):
 *   - model.onnx           the tagger model, e.g. a WD14/WD-v3 ONNX export
 *   - selected_tags.csv    header row + one row per class: id,name,category
 *
 * The model is assumed to take a single NHWC float32 input of shape
 * [1, size, size, 3] with values in [0,255].
 * Common SmilingWolf / WD-tagger ONNX exports expect BGR channel order.
 * If your model differs, adjust `inputSize` and `preprocess()` accordingly.
 */
class TaggerEngine(private val context: Context) {

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
    var inputSize: Int = 448
        private set
    var currentModelName: String = "WD Tagger"
        private set

    val isReady: Boolean
        get() = session != null && tagNames.isNotEmpty()

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
            val sharedTags = tagFiles.firstOrNull()
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
                        tagsFile = sameNameTags ?: sharedTags,
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
        return lines
            .drop(1)
            .mapNotNull { line ->
                val cols = line.split(",")
                when {
                    cols.size >= 3 -> cols[1].trim().takeIf { it.isNotEmpty() }?.let { it to (cols[2].trim().toIntOrNull() ?: 0) }
                    cols.isNotEmpty() -> cols.last().trim().takeIf { it.isNotEmpty() }?.let { it to 0 }
                    else -> null
                }
            }
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
                val h = shape[1]
                if (h > 0) inputSize = h.toInt()
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
                    val fallbackCount = inferOutputTagCount().takeIf { it > 0 } ?: 10_000
                    repeat(fallbackCount) { index ->
                        names.add("tag_$index")
                        cats.add(0)
                    }
                }
            }
        } catch (e: Throwable) {
            return context.getString(R.string.selected_tags_missing, e.message ?: "")
        }
        tagNames = names
        tagCategories = cats
        currentModelName = modelConfig.displayName

        return null
    }

    private fun inferOutputTagCount(): Int {
        val outputInfo = session?.outputInfo?.values?.firstOrNull()?.info as? ai.onnxruntime.TensorInfo
        val shape = outputInfo?.shape ?: return 0
        return shape.lastOrNull { it > 0 }?.toInt() ?: 0
    }

    private fun preprocess(bitmap: Bitmap): FloatBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val buffer = FloatBuffer.allocate(inputSize * inputSize * 3)
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        // NHWC, BGR, 0-255 float — common WD-tagger ONNX exports expect
        // OpenCV-style BGR input. Feeding RGB swaps red/blue and can make
        // normal skin/lips/hair look like blue elements to the model.
        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            buffer.put(b.toFloat())
            buffer.put(g.toFloat())
            buffer.put(r.toFloat())
        }
        buffer.rewind()
        return resized.let { buffer }
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
        val inputBuffer = runCatching { preprocess(bitmap) }.getOrElse { return emptyList() }
        val shape = longArrayOf(1, inputSize.toLong(), inputSize.toLong(), 3)

        return runCatching {
            OnnxTensor.createTensor(env, inputBuffer, shape).use { tensor ->
                currentSession.run(mapOf(inputName to tensor)).use { results ->
                    val iterator = results.iterator()
                    if (!iterator.hasNext()) return emptyList()
                    val output = iterator.next().value.value
                    val scores: FloatArray = when (output) {
                        is Array<*> -> (output[0] as? FloatArray) ?: return emptyList()
                        is FloatArray -> output
                        else -> return emptyList()
                    }
                    val bestByName = linkedMapOf<String, Tag>()
                    for (i in scores.indices) {
                        if (i >= tagNames.size) break
                        val rawScore = scores[i]
                        val name = tagNames[i].trim()
                        if (name.isEmpty()) continue
                        val category = tagCategories.getOrElse(i) { 0 }
                        val adjustedScore = rawScore * weightForCategory(category, generalWeight, characterWeight)
                        if (adjustedScore >= threshold) {
                            val key = normalizeTagName(name)
                            val candidate = Tag(name, category, adjustedScore, rawScore)
                            val existing = bestByName[key]
                            if (existing == null || candidate.score > existing.score) {
                                bestByName[key] = candidate
                            }
                        }
                    }
                    bestByName.values.sortedWith(
                        compareByDescending<Tag> { it.score }.thenBy { it.name.lowercase() }
                    )
                }
            }
        }.getOrDefault(emptyList())
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
