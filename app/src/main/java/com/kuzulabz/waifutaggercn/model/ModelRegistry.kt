package com.kuzulabz.waifutaggercn.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ModelRegistry {
    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    fun update(list: List<ModelInfo>) {
        _models.value = list
    }

    fun add(model: ModelInfo) {
        _models.value = (_models.value.filter { it.path != model.path } + model)
    }
}
