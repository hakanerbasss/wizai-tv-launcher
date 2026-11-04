package com.wizaicorp.tvlauncher

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val BgPresets: List<Pair<String, Color>> = listOf(
    "Antrasit" to Color(0xFF0E1116),
    "Gece mavisi" to Color(0xFF0B1B3A),
    "Mor" to Color(0xFF1B1038),
    "Orman" to Color(0xFF0F2A22),
    "Bordo" to Color(0xFF2A1218),
    "Siyah" to Color(0xFF000000)
)

val AccentPresets: List<Color> = listOf(
    Color(0xFFFF6B4A),
    Color(0xFF2DD4BF),
    Color(0xFFA78BFA),
    Color(0xFFFBBF24),
    Color(0xFF60A5FA)
)

val TextMain = Color(0xFFF2F4F8)
val TextMuted = Color(0xFF9AA4B5)

data class Look(val bg: Color, val accent: Color, val anim: Boolean) {
    val tile: Color get() = lerp(bg, Color.White, 0.08f)
}

val LocalLook = compositionLocalOf { Look(BgPresets[0].second, AccentPresets[0], true) }
