package com.wizaicorp.tvlauncher

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * TMDB (themoviedb.org) API anahtarı. Ücretsiz anahtar: themoviedb.org/settings/api.
 * Boş bırakılırsa "Netflix'te yeni" satırı hiç gösterilmez (ağ isteği de atılmaz).
 * NOT: TMDB verisinin bu uygulamada ticari kullanımı için TMDB'nin şartlarına bakılmalı
 * (bkz. README "Diğer yapılacaklar").
 */
const val TMDB_API_KEY = ""

private const val NETFLIX_PROVIDER_ID = 8

data class NetflixItem(val id: Int, val title: String, val posterPath: String?)

/** Netflix'te (TMDB watch-provider filtresiyle) yeni eklenen filmleri getirir. Anahtar yoksa/hata olursa boş liste. */
suspend fun fetchNetflixNewReleases(): List<NetflixItem> = withContext(Dispatchers.IO) {
    if (TMDB_API_KEY.isBlank()) return@withContext emptyList()
    try {
        val url = URL(
            "https://api.themoviedb.org/3/discover/movie?api_key=$TMDB_API_KEY" +
                "&watch_region=TR&with_watch_providers=$NETFLIX_PROVIDER_ID" +
                "&sort_by=primary_release_date.desc&page=1"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        val text = try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
        val results = JSONObject(text).getJSONArray("results")
        val items = ArrayList<NetflixItem>()
        for (i in 0 until minOf(results.length(), 15)) {
            val r = results.getJSONObject(i)
            val posterPath = r.optString("poster_path", "").ifBlank { null }
            items.add(
                NetflixItem(
                    id = r.getInt("id"),
                    title = r.optString("title", r.optString("name", "")),
                    posterPath = posterPath
                )
            )
        }
        items
    } catch (e: Exception) {
        emptyList()
    }
}

private val netflixPosterCache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap>()

/** TMDB afiş görselini indirir. Hata olursa null döner. */
suspend fun loadNetflixPoster(posterPath: String): ImageBitmap? = withContext(Dispatchers.IO) {
    val url = "https://image.tmdb.org/t/p/w342$posterPath"
    netflixPosterCache[url]?.let { return@withContext it }
    try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        val bytes = try {
            conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?.asImageBitmap()
            ?.also { netflixPosterCache[url] = it }
    } catch (e: Exception) {
        null
    }
}
