package com.wizaicorp.tvlauncher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Bir kutuya yerleştirilecek uygulamayı seçtiren ekran. */
@Composable
fun PickerScreen(
    apps: List<AppEntry>,
    onPick: (String) -> Unit
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
        Text("Kutu için uygulama seç", color = TextMain, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        Text("Geri tuşu vazgeçer.", color = TextMuted, fontSize = 16.sp)
        androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(130.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                FocusTile(
                    modifier = Modifier.width(130.dp),
                    onClick = { onPick("") },
                    focusRequester = firstFocus,
                    padding = 10
                ) {
                    Text(
                        text = "Boşalt",
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 16.dp)
                    )
                }
            }
            items(apps, key = { it.packageName }) { app ->
                AppIcon(app) { onPick(app.packageName) }
            }
        }
    }
}
