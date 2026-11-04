package com.wizaicorp.tvlauncher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class DayForecast(val label: String, val code: Int, val max: Int, val min: Int)

data class Weather(val temp: Int, val code: Int, val days: List<DayForecast>)

enum class WeatherKind { SUN, PARTLY, CLOUD, RAIN, SNOW }

fun kindOf(code: Int): WeatherKind = when (code) {
    0, 1 -> WeatherKind.SUN
    2 -> WeatherKind.PARTLY
    in 51..67, in 80..82, in 95..99 -> WeatherKind.RAIN
    in 71..77, 85, 86 -> WeatherKind.SNOW
    else -> WeatherKind.CLOUD
}

fun labelOf(code: Int): String = when (code) {
    0 -> "Açık"
    1 -> "Çoğunlukla açık"
    2 -> "Parçalı bulutlu"
    3 -> "Bulutlu"
    45, 48 -> "Sisli"
    in 51..57 -> "Çisenti"
    in 61..67, in 80..82 -> "Yağmurlu"
    in 71..77, 85, 86 -> "Karlı"
    in 95..99 -> "Fırtına"
    else -> "Bulutlu"
}

// İstanbul koordinatları (şehir seçimi sonraki sürümde eklenecek)
private const val LAT = 41.01
private const val LON = 28.98

/** Open-Meteo'dan (ücretsiz, anahtarsız) güncel hava ve 5 günlük tahmini çeker. Hata olursa null döner. */
suspend fun fetchWeather(): Weather? = withContext(Dispatchers.IO) {
    try {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$LAT&longitude=$LON" +
                "&current=temperature_2m,weather_code" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                "&forecast_days=5&timezone=auto"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        val text = try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }

        val root = JSONObject(text)
        val cur = root.getJSONObject("current")
        val daily = root.getJSONObject("daily")
        val times = daily.getJSONArray("time")
        val codes = daily.getJSONArray("weather_code")
        val maxes = daily.getJSONArray("temperature_2m_max")
        val mins = daily.getJSONArray("temperature_2m_min")

        val parse = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val out = SimpleDateFormat("EEE", Locale("tr", "TR"))
        val days = ArrayList<DayForecast>()
        for (i in 0 until times.length()) {
            val label = if (i == 0) {
                "Bugün"
            } else {
                out.format(parse.parse(times.getString(i)) ?: Date())
            }
            days.add(
                DayForecast(
                    label = label,
                    code = codes.getInt(i),
                    max = maxes.getDouble(i).roundToInt(),
                    min = mins.getDouble(i).roundToInt()
                )
            )
        }
        Weather(
            temp = cur.getDouble("temperature_2m").roundToInt(),
            code = cur.getInt("weather_code"),
            days = days
        )
    } catch (e: Exception) {
        null
    }
}
