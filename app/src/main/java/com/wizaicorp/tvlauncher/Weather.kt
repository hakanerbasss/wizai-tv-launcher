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

data class Weather(
    val temp: Int,
    val code: Int,
    val feels: Int,
    val humidity: Int,
    val wind: Int,
    val days: List<DayForecast>
)

data class City(val name: String, val lat: Double, val lon: Double)

val Cities: List<City> = listOf(
    City("İstanbul", 41.01, 28.98),
    City("Ankara", 39.93, 32.86),
    City("İzmir", 38.42, 27.14),
    City("Bursa", 40.19, 29.06),
    City("Antalya", 36.90, 30.69),
    City("Adana", 37.00, 35.32),
    City("Konya", 37.87, 32.48),
    City("Trabzon", 41.00, 39.72),
    City("Gaziantep", 37.07, 37.38),
    City("Kayseri", 38.72, 35.49),
    City("Samsun", 41.29, 36.33),
    City("Eskişehir", 39.78, 30.52)
)

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

/** Open-Meteo'dan (ücretsiz, anahtarsız) güncel hava ve 5 günlük tahmini çeker. Hata olursa null döner. */
suspend fun fetchWeather(city: City): Weather? = withContext(Dispatchers.IO) {
    try {
        val lat = city.lat
        val lon = city.lon
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,weather_code,apparent_temperature,relative_humidity_2m,wind_speed_10m" +
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
            feels = cur.getDouble("apparent_temperature").roundToInt(),
            humidity = cur.getDouble("relative_humidity_2m").roundToInt(),
            wind = cur.getDouble("wind_speed_10m").roundToInt(),
            days = days
        )
    } catch (e: Exception) {
        null
    }
}
