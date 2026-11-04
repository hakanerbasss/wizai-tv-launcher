package com.wizaicorp.tvlauncher

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF0E1116)
private val TileBg = Color(0xFF1A1F29)
private val TextMain = Color(0xFFF2F4F8)
private val TextMuted = Color(0xFF9AA4B5)
private val Accent = Color(0xFFFF6B4A)

private const val PKG_TIS = "com.wizaicorp.tisasistani"
private const val PKG_NETFLIX = "com.netflix.ninja"
private const val PKG_YOUTUBE = "com.google.android.youtube.tv"

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    val firstFocus = remember { FocusRequester() }

    // Ana ekranda Geri tuşu hiçbir şey yapmasın
    BackHandler(enabled = true) { }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { loadApps(context) }
    }
    LaunchedEffect(Unit) {
        delay(400)
        try {
            firstFocus.requestFocus()
        } catch (e: Exception) {
            // odak henüz hazır değilse sorun değil
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 36.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        TopBar(context)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTileBig(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                focusRequester = firstFocus,
                title = "TİS Asistanı",
                subtitle = if (isInstalled(context, PKG_TIS)) "Çalışma takvimi" else "Yüklü değil",
                onClick = { launchApp(context, PKG_TIS) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTileSmall(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = "Netflix",
                    onClick = { launchApp(context, PKG_NETFLIX) }
                )
                AppTileSmall(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = "YouTube",
                    onClick = { launchApp(context, PKG_YOUTUBE) }
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTileSmall(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = "Hava durumu",
                    subtitle = "Yakında",
                    onClick = { }
                )
                AppTileSmall(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = "Takvim",
                    subtitle = "Yakında",
                    onClick = { }
                )
            }
        }

        Text(
            text = "Tüm uygulamalar",
            color = TextMuted,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppIcon(app) { launchApp(context, app.packageName) }
            }
        }
    }
}

@Composable
private fun TopBar(context: Context) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("tr", "TR")) }
    val dayFormat = remember { SimpleDateFormat("EEEE", Locale("tr", "TR")) }
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")) }
    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(10_000)
        }
    }
    val date = Date(now)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = timeFormat.format(date),
                color = TextMain,
                fontSize = 56.sp,
                fontWeight = FontWeight.SemiBold
            )
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(
                    text = dayFormat.format(date),
                    color = TextMain,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(date),
                    color = TextMuted,
                    fontSize = 18.sp
                )
            }
        }
        FocusTile(
            modifier = Modifier.width(150.dp),
            onClick = {
                val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // ayarlar açılamazsa sessizce geç
                }
            },
            padding = 12
        ) {
            Text(
                text = "Ayarlar",
                color = TextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AppTileBig(
    modifier: Modifier,
    focusRequester: FocusRequester,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    FocusTile(modifier = modifier, onClick = onClick, focusRequester = focusRequester) {
        Text(text = title, color = TextMain, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, color = TextMuted, fontSize = 20.sp)
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(width = 60.dp, height = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Accent)
        )
    }
}

@Composable
private fun AppTileSmall(
    modifier: Modifier,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    FocusTile(modifier = modifier, onClick = onClick) {
        Text(text = title, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Text(text = subtitle, color = TextMuted, fontSize = 16.sp)
        }
    }
}

@Composable
private fun AppIcon(app: AppEntry, onClick: () -> Unit) {
    FocusTile(
        modifier = Modifier.width(112.dp),
        onClick = onClick,
        padding = 10
    ) {
        if (app.icon != null) {
            Image(
                bitmap = app.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
        Text(
            text = app.label,
            color = TextMain,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

/** Kumandayla odaklanınca beyaz çerçeve ve hafif büyüme gösteren kutu. */
@Composable
private fun FocusTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
    padding: Int = 20,
    content: @Composable ColumnScope.() -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (focused) 1.04f else 1f, label = "tileScale")
    val shape = RoundedCornerShape(24.dp)
    val base = if (focusRequester != null) modifier.focusRequester(focusRequester) else modifier

    Column(
        modifier = base
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(TileBg)
            .border(width = if (focused) 3.dp else 0.dp, color = Color.White, shape = shape)
            .clickable(onClick = onClick)
            .padding(padding.dp),
        content = content
    )
}
