package Shirakawa.LocalCueWord.model

import android.content.Context
import java.io.File
import Shirakawa.LocalCueWord.ml.ModelRegistry

/**
 * Fixes model import visibility by rescanning user model locations and registering files.
 */
object ImportedModelScanner {
    data class ImportedModel(val name: String, val file: File, val type: String)

    fun scan(context: Context): List<ImportedModel> {
        val roots = listOf(
            File(context.filesDir, "ai_models"),
            File(context.getExternalFilesDir(null), "models"),
            File(context.getExternalFilesDir(null), "ai_models")
        )
        return roots.flatMap { root ->
            if (!root.exists()) emptyList() else root.walkTopDown()
                .filter { it.isFile && isModel(it) }
                .map { ImportedModel(it.name, it, detectType(it.name)) }
                .toList()
        }.distinctBy { it.file.absolutePath }
    }

    private fun isModel(file: File): Boolean {
        val n = file.name.lowercase()
        return n.endsWith(".onnx") || n.endsWith(".tflite") || n.endsWith(".pt") || n.endsWith(".bin") || n.endsWith(".safetensors")
    }

    private fun detectType(name: String): String {
        val n = name.lowercase()
        return when {
            "camie" in n -> "camie"
            "pixai" in n && ("v1.0" in n || "v1-0" in n || "v10" in n) -> "pixai_v1"
            "pixai" in n -> "pixai"
            "animetimm" in n || "dbv4" in n -> "animetimm"
            "yolo" in n -> "detection"
            else -> "custom"
        }
    }
}
