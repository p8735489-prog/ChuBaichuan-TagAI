package com.kuzulabz.waifutaggercn.auth

import android.content.Context

class HFTokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("hf_auth", Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token.trim()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object { private const val KEY_TOKEN = "hf_token" }
}
