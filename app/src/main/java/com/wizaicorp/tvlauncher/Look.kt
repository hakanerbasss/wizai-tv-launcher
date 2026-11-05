package com.wizaicorp.tvlauncher

import androidx.compose.foundation.background
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BgPreset(val name: String, val top: Color, val bottom: Color?)

val BgPresets: List<BgPreset> = listOf(
    BgPreset("Antrasit", Color(0xFF0E1116), null),
    BgPreset("Gece mavisi", Color(0xFF0B1B3A), null),
    BgPreset("Mor", Color(0xFF1B1038), null),
    BgPreset("Orman", Color(0xFF0F2A22), null),
    BgPreset("Bordo", Color(0xFF2A1218), null),
    BgPreset("Siyah", Color(0xFF000000), null),
    BgPreset("Okyanus", Color(0xFF0B1B3A), Color(0xFF0F4C5C)),
    BgPreset("Gün batımı", Color(0xFF2A1218), Color(0xFF5B2A1E)),
    BgPreset("Aurora", Color(0xFF0F2A22), Color(0xFF2B1B5A)),
    BgPreset("Gece", Color(0xFF000000), Color(0xFF1B1038))
)

val AccentPresets: List<Color> = listOf(
    Color(0xFFFF6B4A),
    Color(0xFF2DD4BF),
    Color(0xFFA78BFA),
    Color(0xFFFBBF24),
    Color(0xFF60A5FA)
)

val ThemeNames: List<String> = listOf("A · Koyu Bento", "B · Neon Oyun", "C · Hava Merkezli")
val CornerNames: List<String> = listOf("Keskin", "Orta", "Yuvarlak")
val CornerSizes: List<Dp> = listOf(12.dp, 24.dp, 36.dp)
val FocusFxNames: List<String> = listOf("Çerçeve", "Büyüme", "Çerçeve + büyüme", "Parlama")

val TextMain = Color(0xFFF2F4F8)
val TextMuted = Color(0xFF9AA4B5)

data class Look(
    val bg: Color,
    val bg2: Color?,
    val accent: Color,
    val anim: Boolean,
    val corner: Dp,
    val focusFx: Int,
    val sounds: Boolean,
    val tileBanner: Boolean,
    val clock24: Boolean
) {
    val tile: Color get() = lerp(bg, Color.White, 0.08f)
}

val LocalLook = compositionLocalOf {
    Look(
        bg = BgPresets[0].top,
        bg2 = null,
        accent = AccentPresets[0],
        anim = true,
        corner = CornerSizes[1],
        focusFx = 0,
        sounds = false,
        tileBanner = true,
        clock24 = true
    )
}

fun Modifier.screenBackground(look: Look): Modifier {
    val second = look.bg2
    return if (second != null) {
        this.background(Brush.verticalGradient(listOf(look.bg, second)))
    } else {
        this.background(look.bg)
    }
}
