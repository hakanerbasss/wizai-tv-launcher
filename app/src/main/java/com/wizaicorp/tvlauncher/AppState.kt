package com.wizaicorp.tvlauncher

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

const val PKG_TIS = "com.wizaicorp.tisasistani"
const val PKG_NETFLIX = "com.netflix.ninja"
const val PKG_YOUTUBE = "com.google.android.youtube.tv"

/** Launcher'ın bütün kullanıcı ayarları ve kısa süreli durumu. Değişince ekran kendiliğinden yenilenir. */
class AppState(context: Context) {
    private val prefs = Prefs(context)

    val slots = mutableStateListOf(
        prefs.getStringOrNull("slot_0") ?: PKG_TIS,
        prefs.getStringOrNull("slot_1") ?: PKG_NETFLIX,
        prefs.getStringOrNull("slot_2") ?: PKG_YOUTUBE
    )

    var bgIndex by mutableIntStateOf(prefs.getInt("bg_index", 0))
        private set
    var accentIndex by mutableIntStateOf(prefs.getInt("accent_index", 0))
        private set
    var anim by mutableStateOf(prefs.getBool("anim", true))
        private set
    var corner by mutableIntStateOf(prefs.getInt("corner", 1))
        private set
    var focusFx by mutableIntStateOf(prefs.getInt("focus_fx", 0))
        private set
    var clock24 by mutableStateOf(prefs.getBool("clock24", true))
        private set
    var tileBanner by mutableStateOf(prefs.getBool("tile_banner", true))
        private set
    var sortRecent by mutableStateOf(prefs.getBool("sort_recent", false))
        private set
    var sounds by mutableStateOf(prefs.getBool("sounds", false))
        private set
    var okOpens by mutableStateOf(prefs.getBool("ok_opens", false))
        private set
    var cityIndex by mutableIntStateOf(prefs.getInt("city", 0))
        private set

    var favorites by mutableStateOf(prefs.getSet("fav"))
        private set
    var hidden by mutableStateOf(prefs.getSet("hidden"))
        private set
    var recents by mutableStateOf(
        (prefs.getStringOrNull("recents") ?: "").split(",").filter { it.isNotEmpty() }
    )
        private set

    // Hava durumu: ana ekran kutusu ve ayrıntı ekranı aynı veriyi kullanır
    var weather by mutableStateOf<Weather?>(null)
    var weatherFailed by mutableStateOf(false)

    // İzlemeye devam et (Watch Next): erişim yoksa boş kalır, satır gösterilmez
    var watchNext by mutableStateOf<List<WatchNextItem>>(emptyList())
        private set

    fun refreshWatchNext(context: Context) {
        watchNext = queryWatchNext(context)
    }

    // Netflix'te yeni (TMDB): anahtar boşsa her zaman boş kalır, satır gösterilmez
    var netflixNew by mutableStateOf<List<NetflixItem>>(emptyList())
        private set

    fun updateNetflixNew(items: List<NetflixItem>) {
        netflixNew = items
    }

    fun setSlot(index: Int, packageName: String) {
        slots[index] = packageName
        prefs.putString("slot_$index", packageName)
    }

    fun setBg(value: Int) {
        bgIndex = value
        prefs.putInt("bg_index", value)
    }

    fun setAccent(value: Int) {
        accentIndex = value
        prefs.putInt("accent_index", value)
    }

    fun updateAnim(value: Boolean) {
        anim = value
        prefs.putBool("anim", value)
    }

    fun updateCorner(value: Int) {
        corner = value
        prefs.putInt("corner", value)
    }

    fun updateFocusFx(value: Int) {
        focusFx = value
        prefs.putInt("focus_fx", value)
    }

    fun updateClock24(value: Boolean) {
        clock24 = value
        prefs.putBool("clock24", value)
    }

    fun updateTileBanner(value: Boolean) {
        tileBanner = value
        prefs.putBool("tile_banner", value)
    }

    fun updateSortRecent(value: Boolean) {
        sortRecent = value
        prefs.putBool("sort_recent", value)
    }

    fun updateSounds(value: Boolean) {
        sounds = value
        prefs.putBool("sounds", value)
    }

    fun updateOkOpens(value: Boolean) {
        okOpens = value
        prefs.putBool("ok_opens", value)
    }

    fun setCity(value: Int) {
        cityIndex = value
        prefs.putInt("city", value)
    }

    fun toggleFavorite(packageName: String) {
        favorites = if (packageName in favorites) favorites - packageName else favorites + packageName
        prefs.putSet("fav", favorites)
    }

    fun toggleHidden(packageName: String) {
        hidden = if (packageName in hidden) hidden - packageName else hidden + packageName
        prefs.putSet("hidden", hidden)
    }

    fun clearHidden() {
        hidden = emptySet()
        prefs.putSet("hidden", hidden)
    }

    fun pushRecent(packageName: String) {
        val updated = listOf(packageName) + recents.filter { it != packageName }
        recents = updated.take(12)
        prefs.putString("recents", recents.joinToString(","))
    }

    /** Uygulamayı açar ve "son kullanılanlar" listesine ekler. */
    fun open(context: Context, packageName: String) {
        if (launchApp(context, packageName)) {
            pushRecent(packageName)
        }
    }

    fun look(): Look {
        val preset = BgPresets[bgIndex.coerceIn(0, BgPresets.lastIndex)]
        return Look(
            bg = preset.top,
            bg2 = preset.bottom,
            accent = AccentPresets[accentIndex.coerceIn(0, AccentPresets.lastIndex)],
            anim = anim,
            corner = CornerSizes[corner.coerceIn(0, CornerSizes.lastIndex)],
            focusFx = focusFx,
            sounds = sounds,
            tileBanner = tileBanner,
            clock24 = clock24
        )
    }

    /** Ana ekrandaki uygulama sırası: favoriler önde, gizlenenler yok, ayara göre sıralı. */
    fun orderedApps(apps: List<AppEntry>): List<AppEntry> {
        val visible = apps.filter { it.packageName !in hidden }
        val favs = visible.filter { it.packageName in favorites }
        val rest = visible.filter { it.packageName !in favorites }
        val sortedRest = if (sortRecent) {
            rest.sortedWith(
                compareBy<AppEntry>(
                    { val i = recents.indexOf(it.packageName); if (i < 0) Int.MAX_VALUE else i },
                    { it.label.lowercase() }
                )
            )
        } else {
            rest
        }
        return favs + sortedRest
    }
}
