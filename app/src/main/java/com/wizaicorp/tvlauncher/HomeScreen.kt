package com.wizaicorp.tvlauncher

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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

private const val PKG_TIS = "com.wizaicorp.tisasistani"
private const val PKG_NETFLIX = "com.netflix.ninja"
private const val PKG_YOUTUBE = "com.google.android.youtube.tv"

sealed class Screen {
    object Home : Screen()
    object Appearance : Screen()
    data class Picker(val slot: Int) : Screen()
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    val slots = remember {
        mutableStateListOf(
            prefs.slot(0, PKG_TIS),
            prefs.slot(1, PKG_NETFLIX),
            prefs.slot(2, PKG_YOUTUBE)
        )
    }
    var bgIndex by remember { mutableStateOf(prefs.bgIndex) }
    var accentIndex by remember { mutableStateOf(prefs.accentIndex) }
    var animOn by remember { mutableStateOf(prefs.anim) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    val look = Look(
        bg = BgPresets[bgIndex.coerceIn(0, BgPresets.lastIndex)].second,
        accent = AccentPresets[accentIndex.coerceIn(0, AccentPresets.lastIndex)],
        anim = animOn
    )

    // Ana ekrana her dönüşte uygulama listesini yenile
    LaunchedEffect(ResumeSignal.tick.value) {
        apps = withContext(Dispatchers.Default) { loadApps(context) }
    }

    // Geri tuşu: alt ekranlardan ana ekrana döner, ana ekranda hiçbir şey yapmaz
    BackHandler(enabled = true) { screen = Screen.Home }

    CompositionLocalProvider(LocalLook provides look) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(look.bg)) {
            when (val s = screen) {
                is Screen.Home -> HomeContent(
                    context = context,
                    apps = apps,
                    slots = slots,
                    onEditSlot = { screen = Screen.Picker(it) },
                    onAppearance = { screen = Screen.Appearance }
                )
                is Screen.Appearance -> AppearanceScreen(
                    bgIndex = bgIndex,
                    accentIndex = accentIndex,
                    animOn = animOn,
                    onBg = {
                        bgIndex = it
                        prefs.bgIndex = it
                    },
                    onAccent = {
                        accentIndex = it
                        prefs.accentIndex = it
                    },
                    onAnim = {
                        animOn = it
                        prefs.anim = it
                    }
                )
                is Screen.Picker -> PickerScreen(
                    apps = apps,
                    onPick = { pkg ->
                        slots[s.slot] = pkg
                        prefs.setSlot(s.slot, pkg)
                        screen = Screen.Home
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    context: Context,
    apps: List<AppEntry>,
    slots: List<String>,
    onEditSlot: (Int) -> Unit,
    onAppearance: () -> Unit
) {
    val firstFocus = remember { FocusRequester() }
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
            .padding(horizontal = 36.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(context = context, onAppearance = onAppearance)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppSlotTile(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                pkg = slots[0],
                apps = apps,
                focusRequester = firstFocus,
                onOpen = { launchApp(context, slots[0]) },
                onEdit = { onEditSlot(0) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppSlotTile(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    pkg = slots[1],
                    apps = apps,
                    onOpen = { launchApp(context, slots[1]) },
                    onEdit = { onEditSlot(1) }
                )
                AppSlotTile(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    pkg = slots[2],
                    apps = apps,
                    onOpen = { launchApp(context, slots[2]) },
                    onEdit = { onEditSlot(2) }
                )
            }
            WeatherTile(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tüm uygulamalar",
                color = TextMuted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Kutuyu değiştirmek için OK tuşuna uzun bas",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
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
private fun TopBar(context: Context, onAppearance: () -> Unit) {
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FocusTile(
                modifier = Modifier.width(140.dp),
                onClick = onAppearance,
                padding = 12
            ) {
                Text(
                    text = "Görünüm",
                    color = TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            FocusTile(
                modifier = Modifier.width(140.dp),
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
}

/** Kullanıcının seçtiği uygulamayı gösteren kutu. OK = aç, OK'e uzun bas = değiştir. */
@Composable
private fun AppSlotTile(
    modifier: Modifier,
    pkg: String,
    apps: List<AppEntry>,
    focusRequester: FocusRequester? = null,
    onOpen: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val entry = apps.firstOrNull { it.packageName == pkg }
    val empty = pkg.isEmpty()
    val missing = pkg.isNotEmpty() && apps.isNotEmpty() && entry == null
    val banner by produceState<ImageBitmap?>(initialValue = null, pkg) {
        value = if (pkg.isEmpty()) {
            null
        } else {
            withContext(Dispatchers.IO) { loadBanner(context, pkg) }
        }
    }
    val bannerImage = banner
    val showBanner = entry != null && bannerImage != null

    FocusTile(
        modifier = modifier,
        onClick = { if (empty || missing) onEdit() else onOpen() },
        onLongClick = onEdit,
        focusRequester = focusRequester,
        padding = if (showBanner) 0 else 20
    ) {
        if (showBanner && bannerImage != null) {
            Image(
                bitmap = bannerImage,
                contentDescription = entry?.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (empty) {
                    Text("+", color = LocalLook.current.accent, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Text("Uygulama seç", color = TextMuted, fontSize = 18.sp)
                } else if (missing) {
                    Text("Yüklü değil", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Text("OK ile başka uygulama seç", color = TextMuted, fontSize = 15.sp)
                } else if (entry != null) {
                    if (entry.icon != null) {
                        Image(
                            bitmap = entry.icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Text(
                        text = entry.label,
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun AppIcon(app: AppEntry, onClick: () -> Unit) {
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FocusTile(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    padding: Int = 20,
    fill: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val look = LocalLook.current
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
            .background(fill ?: look.tile)
            .then(if (focused) Modifier.border(3.dp, Color.White, shape) else Modifier)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(padding.dp),
        content = content
    )
}
