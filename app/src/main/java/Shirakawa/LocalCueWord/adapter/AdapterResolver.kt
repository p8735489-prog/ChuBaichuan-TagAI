package Shirakawa.LocalCueWord.adapter

object AdapterResolver {
    fun resolve(fileName: String): String {
        val n = fileName.lowercase()
        return when {
            n.contains("camie") -> "CamieV2Adapter"
            // v1.0 判定必须先于 v0.9，否则 "pixai-tagger-v1.0" 会被下面的分支吃掉
            n.contains("pixai") && (n.contains("v1.0") || n.contains("v1-0") || n.contains("v10")) -> "PixAIV10Adapter"
            n.contains("pixai") -> "PixAIV09Adapter"
            n.contains("animetimm") || n.contains("dbv4") -> "AnimeTimmDBv4Adapter"
            n.contains("yolo") -> "YOLOAdapter"
            else -> "GenericAdapter"
        }
    }
}
