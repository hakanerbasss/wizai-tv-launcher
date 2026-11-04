package com.wizaicorp.tvlauncher

import android.content.Context

/** Kullanıcının seçimlerini cihazda saklar. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("wizai_prefs", Context.MODE_PRIVATE)

    /** Kayıt yoksa varsayılan, boş bırakıldıysa "" döner. */
    fun slot(index: Int, default: String): String = sp.getString("slot_$index", null) ?: default

    fun setSlot(index: Int, packageName: String) {
        sp.edit().putString("slot_$index", packageName).apply()
    }

    var bgIndex: Int
        get() = sp.getInt("bg_index", 0)
        set(value) {
            sp.edit().putInt("bg_index", value).apply()
        }

    var accentIndex: Int
        get() = sp.getInt("accent_index", 0)
        set(value) {
            sp.edit().putInt("accent_index", value).apply()
        }

    var anim: Boolean
        get() = sp.getBoolean("anim", true)
        set(value) {
            sp.edit().putBoolean("anim", value).apply()
        }
}
