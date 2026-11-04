package com.wizaicorp.tvlauncher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private val SunColor = Color(0xFFFFC857)
private val CloudLight = Color(0xFFC9D6EA)
private val CloudDark = Color(0xFF9FB0C8)
private val RainBlue = Color(0xFF60A5FA)

private fun DrawScope.drawSun(cx: Float, cy: Float, r: Float, rotDeg: Float) {
    drawCircle(SunColor, radius = r * 0.5f, center = Offset(cx, cy))
    for (i in 0 until 8) {
        val a = Math.toRadians(i * 45.0 + rotDeg)
        val c = cos(a).toFloat()
        val s = sin(a).toFloat()
        drawLine(
            color = SunColor,
            start = Offset(cx + c * r * 0.68f, cy + s * r * 0.68f),
            end = Offset(cx + c * r * 0.92f, cy + s * r * 0.92f),
            strokeWidth = r * 0.12f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloud(cx: Float, cy: Float, s: Float, color: Color) {
    drawCircle(color, radius = s * 0.28f, center = Offset(cx - s * 0.28f, cy + s * 0.05f))
    drawCircle(color, radius = s * 0.36f, center = Offset(cx, cy - s * 0.08f))
    drawCircle(color, radius = s * 0.26f, center = Offset(cx + s * 0.30f, cy + s * 0.06f))
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - s * 0.50f, cy + s * 0.05f),
        size = Size(s * 1.0f, s * 0.26f),
        cornerRadius = CornerRadius(s * 0.13f, s * 0.13f)
    )
}

private fun DrawScope.drawDrops(cx: Float, top: Float, s: Float, phase: Float, color: Color, snow: Boolean) {
    for (i in 0 until 3) {
        val p = (phase + i / 3f) % 1f
        val x = cx + (i - 1) * s * 0.28f
        val y = top + p * s * 0.35f
        val c = color.copy(alpha = 1f - p)
        if (snow) {
            drawCircle(c, radius = s * 0.04f, center = Offset(x, y))
        } else {
            drawLine(
                color = c,
                start = Offset(x, y),
                end = Offset(x - s * 0.03f, y + s * 0.09f),
                strokeWidth = s * 0.04f,
                cap = StrokeCap.Round
            )
        }
    }
}

/** Animasyon değerleri çizim sırasında okunur, böylece ekran sürekli yeniden kurulmaz. */
@Composable
fun WeatherIcon(
    kind: WeatherKind,
    iconSize: Dp,
    rot: State<Float>,
    phase: State<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.size(iconSize)) {
        val w = this.size.minDimension
        when (kind) {
            WeatherKind.SUN -> drawSun(w / 2f, w / 2f, w / 2f, rot.value)
            WeatherKind.PARTLY -> {
                drawSun(w * 0.36f, w * 0.34f, w * 0.34f, rot.value)
                drawCloud(w * 0.58f, w * 0.62f, w * 0.72f, CloudLight)
            }
            WeatherKind.CLOUD -> drawCloud(w / 2f, w * 0.5f, w * 0.9f, CloudLight)
            WeatherKind.RAIN -> {
                drawCloud(w / 2f, w * 0.38f, w * 0.9f, CloudDark)
                drawDrops(w / 2f, w * 0.62f, w * 0.9f, phase.value, RainBlue, false)
            }
            WeatherKind.SNOW -> {
                drawCloud(w / 2f, w * 0.38f, w * 0.9f, CloudDark)
                drawDrops(w / 2f, w * 0.62f, w * 0.9f, phase.value, Color.White, true)
            }
        }
    }
}

/** Hava ikonları için dönme ve damla animasyon değerleri. Animasyon kapalıysa sabit kalır. */
@Composable
fun rememberWeatherAnim(anim: Boolean): Pair<State<Float>, State<Float>> {
    if (anim) {
        val t = rememberInfiniteTransition(label = "weather")
        val rot = t.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart),
            label = "rot"
        )
        val phase = t.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
            label = "phase"
        )
        return Pair(rot, phase)
    }
    val rot = remember { mutableStateOf(0f) }
    val phase = remember { mutableStateOf(0.4f) }
    return Pair(rot, phase)
}

/** Seçili şehrin havasını 30 dakikada bir yeniler. Ana ekran ve ayrıntı ekranı aynı veriyi kullanır. */
@Composable
fun WeatherLoader(state: AppState) {
    LaunchedEffect(state.cityIndex) {
        state.weather = null
        state.weatherFailed = false
        while (true) {
            val w = fetchWeather(Cities[state.cityIndex.coerceIn(0, Cities.lastIndex)])
            if (w != null) {
                state.weather = w
                state.weatherFailed = false
            } else if (state.weather == null) {
                state.weatherFailed = true
            }
            delay(if (w == null) 60_000L else 30 * 60_000L)
        }
    }
}

@Composable
fun WeatherTile(modifier: Modifier, state: AppState, onClick: () -> Unit) {
    val look = LocalLook.current
    val (rot, phase) = rememberWeatherAnim(look.anim)
    val city = Cities[state.cityIndex.coerceIn(0, Cities.lastIndex)]

    FocusTile(modifier = modifier, onClick = onClick) {
        val w = state.weather
        if (w == null) {
            Text(city.name, color = TextMuted, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (state.weatherFailed) "Hava durumu alınamadı" else "Hava durumu yükleniyor…",
                color = TextMain,
                fontSize = 18.sp
            )
        } else {
            Text("${city.name}  ·  ayrıntı için OK", color = TextMuted, fontSize = 14.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                WeatherIcon(kindOf(w.code), 52.dp, rot, phase)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("${w.temp}°", color = TextMain, fontSize = 38.sp, fontWeight = FontWeight.SemiBold)
                    Text(labelOf(w.code), color = TextMuted, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (d in w.days) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(d.label, color = TextMain, fontSize = 16.sp, modifier = Modifier.width(60.dp))
                        WeatherIcon(kindOf(d.code), 26.dp, rot, phase)
                        Spacer(Modifier.weight(1f))
                        Text("${d.max}°", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(" / ${d.min}°", color = TextMuted, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Hava tutulan kutuya basınca açılan tam ekran ayrıntı. */
@Composable
fun WeatherDetailScreen(state: AppState) {
    val look = LocalLook.current
    val (rot, phase) = rememberWeatherAnim(look.anim)
    val city = Cities[state.cityIndex.coerceIn(0, Cities.lastIndex)]
    val first = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) {
        delay(300)
        try {
            first.requestFocus()
        } catch (e: Exception) {
            // yoksay
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 36.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(city.name, color = TextMain, fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
            val w = state.weather
            if (w == null) {
                Text(
                    text = if (state.weatherFailed) "Hava durumu alınamadı" else "Yükleniyor…",
                    color = TextMuted,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    WeatherIcon(kindOf(w.code), 120.dp, rot, phase)
                    Column(modifier = Modifier.padding(start = 20.dp)) {
                        Text("${w.temp}°", color = TextMain, fontSize = 72.sp, fontWeight = FontWeight.SemiBold)
                        Text(labelOf(w.code), color = TextMuted, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Hissedilen: ${w.feels}°", color = TextMain, fontSize = 20.sp)
                Text("Nem: %${w.humidity}", color = TextMain, fontSize = 20.sp)
                Text("Rüzgar: ${w.wind} km/sa", color = TextMain, fontSize = 20.sp)
            }
            Spacer(Modifier.height(24.dp))
            FocusTile(
                modifier = Modifier.width(260.dp),
                onClick = { state.setCity((state.cityIndex + 1) % Cities.size) },
                focusRequester = first,
                padding = 14
            ) {
                Text(
                    text = "Şehir: ${city.name}  ›",
                    color = TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text("OK ile sıradaki şehre geç. Geri tuşu ana ekrana döner.", color = TextMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
        val w = state.weather
        if (w != null) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("5 günlük tahmin", color = TextMuted, fontSize = 18.sp)
                for (d in w.days) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(d.label, color = TextMain, fontSize = 22.sp, modifier = Modifier.width(100.dp))
                        WeatherIcon(kindOf(d.code), 44.dp, rot, phase)
                        Text(labelOf(d.code), color = TextMuted, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp))
                        Spacer(Modifier.weight(1f))
                        Text("${d.max}°", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Text(" / ${d.min}°", color = TextMuted, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}
