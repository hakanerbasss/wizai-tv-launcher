package com.wizaicorp.tvlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.SoundEffectConstants
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalView
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

sealed class Screen {
    object Home : Screen()
    object Appearance : Screen()
    object Weather : Screen()
    data class Picker(val slot: Int) : Screen()
    data class Card(val pkg: String, val slot: Int) : Screen()
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val state = remember { AppState(context) }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    val look = state.look()

    LaunchedEffect(ResumeSignal.tick.value) {
        apps = withContext(Dispatchers.Default) { loadApps(context) }
    }
    LaunchedEffect(ResumeSignal.tick.value) {
        withContext(Dispatchers.IO) { state.refreshWatchNext(context) }
    }
    WeatherLoader(state)
    NetflixLoader(state)

    // Geri tuşu: alt ekranlardan ana ekrana döner, ana ekranda hiçbir şey yapmaz
    BackHandler(enabled = true) { screen = Screen.Home }

    CompositionLocalProvider(LocalLook provides look) {
        Box(modifier = Modifier
            .fillMaxSize()
            .screenBackground(look)) {
            when (val s = screen) {
                is Screen.Home -> HomeContent(
                    context = context,
                    state = state,
                    apps = apps,
                    onEditSlot = { screen = Screen.Picker(it) },
                    onCard = { pkg, slot -> screen = Screen.Card(pkg, slot) },
                    onAppearance = { screen = Screen.Appearance },
                    onWeather = { screen = Screen.Weather }
                )
                is Screen.Appearance -> AppearanceScreen(state)
                is Screen.Weather -> WeatherDetailScreen(state)
                is Screen.Picker -> PickerScreen(
                    apps = apps,
                    onPick = { pkg ->
                        state.setSlot(s.slot, pkg)
                        screen = Screen.Home
                    }
                )
                is Screen.Card -> AppCard(
                    context = context,
                    state = state,
                    apps = apps,
                    pkg = s.pkg,
                    slot = s.slot,
                    onClose = { screen = Screen.Home },
                    onEditSlot = { screen = Screen.Picker(it) }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    context: Context,
    state: AppState,
    apps: List<AppEntry>,
    onEditSlot: (Int) -> Unit,
    onCard: (String, Int) -> Unit,
    onAppearance: () -> Unit,
    onWeather: () -> Unit
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

    // Üst kutuya OK: boşsa seçtir, ayara göre direkt aç ya da kart göster
    fun slotClick(i: Int) {
        val pkg = state.slots[i]
        val installed = apps.any { it.packageName == pkg }
        when {
            pkg.isEmpty() || (apps.isNotEmpty() && !installed) -> onEditSlot(i)
            state.okOpens -> state.open(context, pkg)
            else -> onCard(pkg, i)
        }
    }

    fun slotLong(i: Int) {
        val pkg = state.slots[i]
        if (pkg.isEmpty()) onEditSlot(i) else onCard(pkg, i)
    }

    val list = state.orderedApps(apps)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TopBar(context = context, state = state, onAppearance = onAppearance)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppSlotTile(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                pkg = state.slots[0],
                apps = apps,
                focusRequester = firstFocus,
                onClick = { slotClick(0) },
                onLong = { slotLong(0) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AppSlotTile(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    pkg = state.slots[1],
                    apps = apps,
                    onClick = { slotClick(1) },
                    onLong = { slotLong(1) }
                )
                AppSlotTile(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    pkg = state.slots[2],
                    apps = apps,
                    onClick = { slotClick(2) },
                    onLong = { slotLong(2) }
                )
            }
            WeatherTile(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                state = state,
                onClick = onWeather
            )
        }

        if (state.watchNext.isNotEmpty()) {
            Text(
                text = "İzlemeye devam et",
                color = TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.watchNext, key = { it.id }) { item ->
                    WatchNextCard(
                        item = item,
                        onClick = { item.intent?.let { safeStart(context, it) } }
                    )
                }
            }
        }

        if (state.netflixNew.isNotEmpty()) {
            Text(
                text = "Netflix'te yeni",
                color = TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.netflixNew, key = { it.id }) { item ->
                    NetflixCard(
                        item = item,
                        onClick = { state.open(context, PKG_NETFLIX) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tüm uygulamalar",
                color = TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "OK: aç  ·  OK'e uzun bas: menü (favori, gizle, kutuya koy…)",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(list, key = { it.packageName }) { app ->
                AppIcon(
                    app = app,
                    favorite = app.packageName in state.favorites,
                    banner = state.tileBanner,
                    onClick = { state.open(context, app.packageName) },
                    onLongClick = { onCard(app.packageName, -1) }
                )
            }
        }
    }
}

/** TMDB anahtarı tanımlıysa Netflix'teki yeni filmleri periyodik olarak çeker. */
@Composable
private fun NetflixLoader(state: AppState) {
    LaunchedEffect(Unit) {
        if (TMDB_API_KEY.isBlank()) return@LaunchedEffect
        while (true) {
            state.setNetflixNew(fetchNetflixNewReleases())
            delay(6 * 60 * 60_000L)
        }
    }
}

@Composable
private fun TopBar(context: Context, state: AppState, onAppearance: () -> Unit) {
    val timeFormat = remember(state.clock24) {
        SimpleDateFormat(if (state.clock24) "HH:mm" else "h:mm a", Locale("tr", "TR"))
    }
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
                fontSize = 52.sp,
                fontWeight = FontWeight.SemiBold
            )
            Column(modifier = Modifier.padding(start = 18.dp)) {
                Text(
                    text = dayFormat.format(date),
                    color = TextMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(date),
                    color = TextMuted,
                    fontSize = 16.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BarButton("Wi-Fi") { safeStart(context, Intent(Settings.ACTION_WIFI_SETTINGS)) }
            BarButton("Bluetooth") { safeStart(context, Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
            BarButton("Görünüm", onAppearance)
            BarButton("Ayarlar") { safeStart(context, Intent(Settings.ACTION_SETTINGS)) }
        }
    }
}

@Composable
private fun BarButton(text: String, onClick: () -> Unit) {
    FocusTile(
        modifier = Modifier.width(116.dp),
        onClick = onClick,
        padding = 10
    ) {
        Text(
            text = text,
            color = TextMain,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun rememberBanner(pkg: String): ImageBitmap? {
    val context = LocalContext.current
    val banner by produceState<ImageBitmap?>(initialValue = null, pkg) {
        value = if (pkg.isEmpty()) null else withContext(Dispatchers.IO) { loadBanner(context, pkg) }
    }
    return banner
}

/** Üstteki büyük/küçük kutular. Logo varsa kutuyu tamamen doldurur, yoksa simge + ad gösterir. */
@Composable
private fun AppSlotTile(
    modifier: Modifier,
    pkg: String,
    apps: List<AppEntry>,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
    onLong: () -> Unit
) {
    val entry = apps.firstOrNull { it.packageName == pkg }
    val empty = pkg.isEmpty()
    val missing = pkg.isNotEmpty() && apps.isNotEmpty() && entry == null
    val bannerImage = rememberBanner(pkg)
    val showBanner = entry != null && bannerImage != null

    FocusTile(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLong,
        focusRequester = focusRequester,
        padding = if (showBanner) 0 else 16
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
                            modifier = Modifier.size(72.dp)
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

/** Alt satırdaki uygulama kartı. Logo kartı: 16:9 logo + altında ad. Simge modu: büyük simge + ad. */
@Composable
internal fun AppIcon(
    app: AppEntry,
    favorite: Boolean = false,
    banner: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val bannerImage = if (banner) rememberBanner(app.packageName) else null
    FocusTile(
        modifier = Modifier.width(if (bannerImage != null) 150.dp else 112.dp),
        onClick = onClick,
        onLongClick = onLongClick,
        padding = 0
    ) {
        if (bannerImage != null) {
            Image(
                bitmap = bannerImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Spacer(Modifier.size(64.dp))
                }
            }
        }
        Text(
            text = (if (favorite) "★ " else "") + app.label,
            color = TextMain,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

/** Watch Next kaydı: afiş varsa afiş, yoksa yalnızca başlık. */
@Composable
private fun WatchNextCard(item: WatchNextItem, onClick: () -> Unit) {
    val context = LocalContext.current
    val poster by produceState<ImageBitmap?>(initialValue = null, item.posterUri) {
        value = item.posterUri?.let { uri ->
            withContext(Dispatchers.IO) { loadWatchNextPoster(context, uri) }
        }
    }
    FocusTile(
        modifier = Modifier.width(150.dp),
        onClick = onClick,
        padding = 0
    ) {
        if (poster != null) {
            Image(
                bitmap = poster!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", color = LocalLook.current.accent, fontSize = 28.sp)
            }
        }
        Text(
            text = item.title,
            color = TextMain,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

/** Netflix'te yeni çıkan film kartı (TMDB afişi). Dokununca Netflix açılır. */
@Composable
private fun NetflixCard(item: NetflixItem, onClick: () -> Unit) {
    val poster by produceState<ImageBitmap?>(initialValue = null, item.posterPath) {
        value = item.posterPath?.let { loadNetflixPoster(it) }
    }
    FocusTile(
        modifier = Modifier.width(120.dp),
        onClick = onClick,
        padding = 0
    ) {
        if (poster != null) {
            Image(
                bitmap = poster!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
                contentAlignment = Alignment.Center
            ) {
                Text("N", color = LocalLook.current.accent, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = item.title,
            color = TextMain,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

/** Uygulamaya OK'e basınca (ya da uzun basınca) açılan kart: aç, değiştir, favori, gizle, bilgi, kaldır. */
@Composable
private fun AppCard(
    context: Context,
    state: AppState,
    apps: List<AppEntry>,
    pkg: String,
    slot: Int,
    onClose: () -> Unit,
    onEditSlot: (Int) -> Unit
) {
    val entry = apps.firstOrNull { it.packageName == pkg }
    val banner = rememberBanner(pkg)
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(250)
        try {
            first.requestFocus()
        } catch (e: Exception) {
            // yoksay
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(560.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(LocalLook.current.tile)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(176.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (banner != null) {
                        Image(
                            bitmap = banner,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (entry?.icon != null) {
                        Image(bitmap = entry.icon, contentDescription = null, modifier = Modifier.size(64.dp))
                    }
                }
                Column(modifier = Modifier.padding(start = 18.dp)) {
                    Text(
                        text = entry?.label ?: pkg,
                        color = TextMain,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (slot >= 0) "Üst kutu ${slot + 1}" else "Uygulama",
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            CardButton("Aç", first) {
                state.open(context, pkg)
                onClose()
            }
            if (slot >= 0) {
                CardButton("Bu kutuya başka uygulama seç") { onEditSlot(slot) }
                CardButton("Kutuyu boşalt") {
                    state.setSlot(slot, "")
                    onClose()
                }
            } else {
                CardButton("Üst kutuya koy: 1 (büyük)") {
                    state.setSlot(0, pkg)
                    onClose()
                }
                CardButton("Üst kutuya koy: 2") {
                    state.setSlot(1, pkg)
                    onClose()
                }
                CardButton("Üst kutuya koy: 3") {
                    state.setSlot(2, pkg)
                    onClose()
                }
            }
            val fav = pkg in state.favorites
            CardButton(if (fav) "Favorilerden çıkar" else "Favorilere ekle (satırın başına)") {
                state.toggleFavorite(pkg)
                onClose()
            }
            CardButton("Satırdan gizle") {
                state.toggleHidden(pkg)
                onClose()
            }
            CardButton("Uygulama bilgisi") {
                safeStart(
                    context,
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                )
            }
            CardButton("Kaldır") {
                safeStart(context, Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")))
            }
        }
    }
}

@Composable
private fun CardButton(text: String, focusRequester: FocusRequester? = null, onClick: () -> Unit) {
    FocusTile(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        focusRequester = focusRequester,
        padding = 10
    ) {
        Text(text, color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Kumandayla odaklanınca vurgulanan kutu. Köşe şekli ve odak efekti kullanıcı ayarından gelir.
 * Efekt 0: çerçeve, 1: büyüme, 2: çerçeve + büyüme, 3: parlama (vurgu renginde çerçeve).
 */
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
    val view = LocalView.current
    var focused by remember { mutableStateOf(false) }
    val grows = look.focusFx != 0
    val target = if (focused && grows) (if (look.focusFx == 1) 1.08f else 1.04f) else 1f
    val scale by animateFloatAsState(targetValue = target, label = "tileScale")
    val shape = RoundedCornerShape(look.corner)
    val borderColor = if (look.focusFx == 3) look.accent else Color.White
    val showBorder = focused && look.focusFx != 1
    val base = if (focusRequester != null) modifier.focusRequester(focusRequester) else modifier

    Column(
        modifier = base
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused && look.sounds) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                }
            }
            .clip(shape)
            .background(fill ?: look.tile)
            .then(if (showBorder) Modifier.border(3.dp, borderColor, shape) else Modifier)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(padding.dp),
        content = content
    )
}
