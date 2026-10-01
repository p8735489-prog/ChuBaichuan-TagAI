package com.kuzulabz.waifutaggercn.ml

object PrecisionProfile {
    fun threshold(category: Int, fallback: Float): Float = when (category) {
        0 -> 0.90f
        1 -> 0.40f
        else -> fallback
    }
}
