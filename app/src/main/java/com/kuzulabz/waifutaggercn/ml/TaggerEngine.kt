package com.kuzulabz.waifutaggercn.ml

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.kuzulabz.waifutaggercn.R
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
 * [1, size, size, 3] with values in [0,255] (this matches the common
 * SmilingWolf WD-tagger ONNX exports). If your model differs, adjust
 * `inputSize` and `preprocess()` accordingly.
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
                displayName = "内置模型 model.onnx",
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
            val sharedTags = File(modelDir, "selected_tags.csv").takeIf { it.exists() && it.length() > 0L }
            val customModels = allFiles
                ?.filter { it.isFile && it.extension.equals("onnx", ignoreCase = true) }
                ?.sortedBy { it.name.lowercase() }
                ?.map { modelFile ->
                    val sameNameTags = File(modelDir, "${modelFile.nameWithoutExtension}.csv")
                        .takeIf { it.exists() && it.length() > 0L }
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

    /** Returns null on success, or a human-readable error message. */
    fun load(modelConfig: ModelConfig = builtInModelConfig()): String? {
        val env = OrtEnvironment.getEnvironment()
        session?.close()
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
        } catch (e: Exception) {
            return context.getString(R.string.model_onnx_missing, e.message ?: "")
        }

        session = try {
            env.createSession(modelFile.absolutePath)
        } catch (e: Exception) {
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
                BufferedReader(FileReader(tagsFile)).useLines { lines ->
                    lines.drop(1).forEach { line ->
                        val cols = line.split(",")
                        if (cols.size >= 3) {
                            names.add(cols[1].trim())
                            cats.add(cols[2].trim().toIntOrNull() ?: 0)
                        }
                    }
                }
            } else {
                context.assets.open("selected_tags.csv").use { stream ->
                    BufferedReader(InputStreamReader(stream)).useLines { lines ->
                        lines.drop(1).forEach { line ->
                            val cols = line.split(",")
                            if (cols.size >= 3) {
                                names.add(cols[1].trim())
                                cats.add(cols[2].trim().toIntOrNull() ?: 0)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return context.getString(R.string.selected_tags_missing, e.message ?: "")
        }
        tagNames = names
        tagCategories = cats
        currentModelName = modelConfig.displayName

        return null
    }

    private fun preprocess(bitmap: Bitmap): FloatBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val buffer = FloatBuffer.allocate(inputSize * inputSize * 3)
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        // NHWC, RGB, 0-255 float — matches common WD-tagger ONNX exports.
        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            buffer.put(r.toFloat())
            buffer.put(g.toFloat())
            buffer.put(b.toFloat())
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
        val env = OrtEnvironment.getEnvironment()
        val inputBuffer = preprocess(bitmap)
        val shape = longArrayOf(1, inputSize.toLong(), inputSize.toLong(), 3)

        OnnxTensor.createTensor(env, inputBuffer, shape).use { tensor ->
            currentSession.run(mapOf(inputName to tensor)).use { results ->
                val iterator = results.iterator()
                if (!iterator.hasNext()) return emptyList()
                val output = iterator.next().value.value
                val scores: FloatArray = when (output) {
                    is Array<*> -> (output[0] as FloatArray)
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
                return bestByName.values.sortedWith(
                    compareByDescending<Tag> { it.score }.thenBy { it.name.lowercase() }
                )
            }
        }
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
        session?.close()
        session = null
    }
}
