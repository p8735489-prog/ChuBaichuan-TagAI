package com.kuzulabz.waifutaggercn.adapter

object AdapterResolver {
    fun resolve(fileName: String): String {
        val n = fileName.lowercase()
        return when {
            n.contains("camie") -> "CamieV2Adapter"
            n.contains("pixai") -> "PixAIV09Adapter"
            n.contains("animetimm") || n.contains("dbv4") -> "AnimeTimmDBv4Adapter"
            n.contains("yolo") -> "YOLOAdapter"
            else -> "GenericAdapter"
        }
    }
}
