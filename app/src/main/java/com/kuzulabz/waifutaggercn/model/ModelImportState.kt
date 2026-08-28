package com.kuzulabz.waifutaggercn.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ModelImportState {
    private val _refresh = MutableStateFlow(0)
    val refresh = _refresh.asStateFlow()

    fun notifyImported() {
        _refresh.value++
    }
}
