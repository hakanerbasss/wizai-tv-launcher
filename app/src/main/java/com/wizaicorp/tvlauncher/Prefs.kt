package com.wizaicorp.tvlauncher

import android.content.Context

/** Kullanıcının seçimlerini cihazda saklar. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("wizai_prefs", Context.MODE_PRIVATE)

    fun getInt(key: String, def: Int): Int = sp.getInt(key, def)

    fun putInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    fun getBool(key: String, def: Boolean): Boolean = sp.getBoolean(key, def)

    fun putBool(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun getStringOrNull(key: String): String? = sp.getString(key, null)

    fun putString(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    fun getSet(key: String): Set<String> = HashSet(sp.getStringSet(key, emptySet()) ?: emptySet())

    fun putSet(key: String, value: Set<String>) {
        sp.edit().putStringSet(key, HashSet(value)).apply()
    }
}
