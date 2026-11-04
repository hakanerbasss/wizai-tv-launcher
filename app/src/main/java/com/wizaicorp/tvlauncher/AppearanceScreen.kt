package com.wizaicorp.tvlauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AppearanceScreen(
    bgIndex: Int,
    accentIndex: Int,
    animOn: Boolean,
    onBg: (Int) -> Unit,
    onAccent: (Int) -> Unit,
    onAnim: (Boolean) -> Unit
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(300)
        try {
            firstFocus.requestFocus()
        } catch (e: Exception) {
            // yoksay
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 36.dp)
    ) {
        Text("Görünüm", color = TextMain, fontSize = 36.sp, fontWeight = FontWeight.SemiBold)
        Text("Seçtiğin renk anında uygulanır. Geri tuşu ana ekrana döner.", color = TextMuted, fontSize = 16.sp)

        Spacer(Modifier.height(24.dp))
        Text("Arka plan", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BgPresets.forEachIndexed { i, preset ->
                FocusTile(
                    modifier = Modifier
                        .width(120.dp)
                        .height(84.dp),
                    onClick = { onBg(i) },
                    focusRequester = if (i == 0) firstFocus else null,
                    padding = 10,
                    fill = preset.second
                ) {
                    Text(preset.first, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    if (i == bgIndex) {
                        Text("Seçili", color = LocalLook.current.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Vurgu rengi", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AccentPresets.forEachIndexed { i, color ->
                FocusTile(
                    modifier = Modifier
                        .width(120.dp)
                        .height(84.dp),
                    onClick = { onAccent(i) },
                    padding = 10,
                    fill = color
                ) {
                    if (i == accentIndex) {
                        Text("Seçili", color = Color(0xFF0E1116), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Animasyonlar", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Text("Eski ya da yavaş TV'lerde kapatmak akıcılığı artırır.", color = TextMuted, fontSize = 15.sp)
        Spacer(Modifier.height(10.dp))
        FocusTile(
            modifier = Modifier.width(160.dp),
            onClick = { onAnim(!animOn) },
            padding = 14
        ) {
            Text(
                text = if (animOn) "Açık" else "Kapalı",
                color = TextMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(Modifier.size(1.dp))
    }
}
