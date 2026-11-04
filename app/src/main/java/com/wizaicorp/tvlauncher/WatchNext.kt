package com.wizaicorp.tvlauncher

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.tv.TvContract
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.net.HttpURLConnection
import java.net.URL

data class WatchNextItem(
    val id: Long,
    val title: String,
    val posterUri: Uri?,
    val intent: Intent?
)

/**
 * Android TV'nin Watch Next (android.media.tv) sağlayıcısından diğer uygulamaların
 * "izlemeye devam et" kayıtlarını okur. Bu sağlayıcıya tam erişim genelde yalnızca
 * varsayılan launcher'a/ayrıcalıklı uygulamalara açıktır; erişim yoksa veya
 * cihaz desteklemiyorsa sessizce boş liste döner.
 */
fun queryWatchNext(context: Context): List<WatchNextItem> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()
    val items = mutableListOf<WatchNextItem>()
    try {
        val projection = arrayOf(
            TvContract.WatchNextPrograms._ID,
            TvContract.WatchNextPrograms.COLUMN_TITLE,
            TvContract.WatchNextPrograms.COLUMN_POSTER_ART_URI,
            TvContract.WatchNextPrograms.COLUMN_INTENT_URI
        )
        context.contentResolver.query(
            TvContract.WatchNextPrograms.CONTENT_URI, projection, null, null, null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndex(TvContract.WatchNextPrograms._ID)
            val titleCol = cursor.getColumnIndex(TvContract.WatchNextPrograms.COLUMN_TITLE)
            val posterCol = cursor.getColumnIndex(TvContract.WatchNextPrograms.COLUMN_POSTER_ART_URI)
            val intentCol = cursor.getColumnIndex(TvContract.WatchNextPrograms.COLUMN_INTENT_URI)
            while (cursor.moveToNext() && items.size < 15) {
                val title = titleCol.takeIf { it >= 0 }?.let { cursor.getString(it) }
                if (title.isNullOrBlank()) continue
                val id = idCol.takeIf { it >= 0 }?.let { cursor.getLong(it) } ?: 0L
                val poster = posterCol.takeIf { it >= 0 }?.let { cursor.getString(it) }?.let {
                    try {
                        Uri.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }
                val intent = intentCol.takeIf { it >= 0 }?.let { cursor.getString(it) }?.let {
                    try {
                        Intent.parseUri(it, Intent.URI_INTENT_SCHEME)
                    } catch (e: Exception) {
                        null
                    }
                }
                items.add(WatchNextItem(id, title, poster, intent))
            }
        }
    } catch (e: SecurityException) {
        Log.w("WatchNext", "Erişim izni yok: ${e.message}")
    } catch (e: Exception) {
        Log.w("WatchNext", "Sorgu hatası: ${e.message}")
    }
    return items
}

private val watchNextPosterCache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap>()

/** Watch Next afiş görselini indirir (content:// ya da http(s) kaynağından). Hata olursa null döner. */
fun loadWatchNextPoster(context: Context, uri: Uri): ImageBitmap? {
    val key = uri.toString()
    watchNextPosterCache[key]?.let { return it }
    return try {
        val bytes = when (uri.scheme) {
            "http", "https" -> {
                val conn = URL(key).openConnection() as HttpURLConnection
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                try {
                    conn.inputStream.use { it.readBytes() }
                } finally {
                    conn.disconnect()
                }
            }
            else -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } ?: return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 480 || bounds.outHeight / sample > 480) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null
        bitmap.asImageBitmap().also { watchNextPosterCache[key] = it }
    } catch (e: Exception) {
        null
    }
}
