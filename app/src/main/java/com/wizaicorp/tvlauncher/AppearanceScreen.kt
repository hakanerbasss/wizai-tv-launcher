package com.wizaicorp.tvlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Görünüm ve davranış ayarları. Kaydırılabilir; her satır OK ile değişir. */
@Composable
fun AppearanceScreen(state: AppState) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(300)
        try {
            first.requestFocus()
        } catch (e: Exception) {
            // yoksay
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 28.dp)
    ) {
        item {
            Text("Görünüm ve ayarlar", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            Text("Yukarı/aşağı ile gez, OK ile değiştir. Geri tuşu ana ekrana döner.", color = TextMuted, fontSize = 14.sp)
        }
        item { SectionLabel("Arka plan") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)) {
                itemsIndexed(BgPresets) { i, p ->
                    Swatch(
                        brush = Brush.verticalGradient(listOf(p.top, p.bottom ?: p.top)),
                        label = p.name,
                        selected = state.bgIndex == i,
                        focusRequester = if (i == 0) first else null,
                        onClick = { state.setBg(i) }
                    )
                }
            }
        }
        item { SectionLabel("Vurgu rengi") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)) {
                itemsIndexed(AccentPresets) { i, c ->
                    Swatch(
                        brush = Brush.verticalGradient(listOf(c, c)),
                        label = "",
                        selected = state.accentIndex == i,
                        onClick = { state.setAccent(i) }
                    )
                }
            }
        }
        item { SectionLabel("Kutular") }
        item { OptionRow("Köşe şekli", CornerNames[state.corner.coerceIn(0, 2)]) { state.updateCorner((state.corner + 1) % CornerNames.size) } }
        item { OptionRow("Odak efekti", FocusFxNames[state.focusFx.coerceIn(0, 3)]) { state.updateFocusFx((state.focusFx + 1) % FocusFxNames.size) } }
        item { OptionRow("Uygulama satırı", if (state.tileBanner) "Logo kartları" else "Simgeler") { state.updateTileBanner(!state.tileBanner) } }
        item { SectionLabel("Davranış") }
        item { OptionRow("Animasyonlar", onOff(state.anim)) { state.updateAnim(!state.anim) } }
        item { OptionRow("Saat biçimi", if (state.clock24) "24 saat" else "12 saat") { state.updateClock24(!state.clock24) } }
        item { OptionRow("Üst kutularda OK", if (state.okOpens) "Direkt aç" else "Kart aç (Aç / Değiştir / Favori…)") { state.updateOkOpens(!state.okOpens) } }
        item { OptionRow("Satır sırası", if (state.sortRecent) "Son kullanılan önde" else "A-Z") { state.updateSortRecent(!state.sortRecent) } }
        item { OptionRow("Gezinme sesi", onOff(state.sounds)) { state.updateSounds(!state.sounds) } }
        item { OptionRow("Hava durumu şehri", Cities[state.cityIndex.coerceIn(0, Cities.lastIndex)].name) { state.setCity((state.cityIndex + 1) % Cities.size) } }
        item {
            OptionRow(
                "Gizlenen uygulamalar",
                if (state.hidden.isEmpty()) "Yok" else "${state.hidden.size} gizli — hepsini geri getir"
            ) { state.clearHidden() }
        }
        item { Spacer16() }
    }
}

private fun onOff(v: Boolean) = if (v) "Açık" else "Kapalı"

@Composable
private fun Spacer16() {
    Box(Modifier.height(16.dp))
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = LocalLook.current.accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun OptionRow(title: String, value: String, onClick: () -> Unit) {
    FocusTile(modifier = Modifier.fillMaxWidth(), onClick = onClick, padding = 12) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(value, color = TextMuted, fontSize = 16.sp, maxLines = 1)
        }
    }
}

@Composable
private fun Swatch(
    brush: Brush,
    label: String,
    selected: Boolean,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    FocusTile(
        modifier = Modifier.width(if (label.isEmpty()) 64.dp else 110.dp),
        onClick = onClick,
        focusRequester = focusRequester,
        padding = 6
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (label.isEmpty()) 40.dp else 52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(brush)
                .then(
                    if (selected) Modifier.border(3.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier
                )
        )
        if (label.isNotEmpty()) {
            Text(
                text = if (selected) "✓ $label" else label,
                color = TextMain,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
