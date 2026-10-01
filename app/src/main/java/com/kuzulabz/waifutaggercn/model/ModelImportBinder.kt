package com.kuzulabz.waifutaggercn.model

import com.kuzulabz.waifutaggercn.adapter.AdapterResolver

object ModelImportBinder {
    fun register(path: String) {
        val name = path.substringAfterLast('/')
        ModelRegistry.add(
            ModelInfo(
                name = name,
                path = path,
                type = "ONNX",
                adapter = AdapterResolver.resolve(name),
                loaded = true
            )
        )
    }
}
