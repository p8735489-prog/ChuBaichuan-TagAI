package com.kuzulabz.waifutaggercn.auth

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL

class HFTokenManager(context: Context) {
    private val store = HFTokenStore(context)

    fun token(): String? = store.getToken()

    fun save(token: String) = store.saveToken(token)

    fun logout() = store.clear()

    fun authHeader(): String? = token()?.let { "Bearer $it" }

    fun validate(): Boolean {
        val t = token() ?: return false
        return runCatching {
            val c = URL("https://huggingface.co/api/whoami").openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            c.setRequestProperty("Authorization", "Bearer $t")
            c.responseCode in 200..299
        }.getOrDefault(false)
    }
}
