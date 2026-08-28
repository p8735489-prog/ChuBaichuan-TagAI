package com.kuzulabz.waifutaggercn.model

data class ModelUiState(
    val models: List<ModelInfo> = emptyList(),
    val refreshing: Boolean = false
)

data class ModelInfo(
    val name: String,
    val path: String,
    val type: String,
    val adapter: String,
    val loaded: Boolean = false
)
