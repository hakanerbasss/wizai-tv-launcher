# Wizai-Launcher

Xiaomi / Android TV için sıfırdan yazılmış, özelleştirilebilir ve animasyonlu ana ekran (launcher).
Kotlin + Jetpack Compose. İleride Google Play'de ücretli yayınlanacak (FLauncher tabanlı DEĞİL, GPL kodu yok).

Hedef cihaz: Xiaomi MiTV-MSSP2, Android 9, 1280x720, armeabi-v7a (32-bit).
Paket adı: `com.wizaicorp.tvlauncher`

## Şu an çalışanlar (v0.1.x)
- Saat/tarih, bento düzeni: 1 büyük + 2 küçük uygulama kutusu + hava durumu kutusu
- Kutulara istediğin uygulamayı koyma (hiçbir uygulama koda gömülü değil; varsayılan TİS / Netflix / YouTube)
- TV uygulamalarının kendi logo kartları, alt satırda tüm uygulamalar
- Açılır uygulama kartı: Aç, başka uygulama seç, boşalt, favori, gizle, kutuya koy, bilgi, kaldır
- 5 günlük animasyonlu hava durumu (Open-Meteo, anahtarsız) + ayrıntı ekranı + 12 şehir
- Görünüm ekranı: 10 arka plan (4 gradyan), 5 vurgu rengi, köşe şekli, 4 odak efekti, logo/simge satırı,
  animasyon aç/kapa, 12/24 saat, satır sırası, gezinme sesi, gizlenenleri geri getir
- Wi-Fi / Bluetooth / Ayarlar kısayolları

## Yol haritası (20 özellik)
1. Açılır uygulama kartı ✅
2. Favori uygulamalar ✅
3. Uygulama gizleme ✅
4. Uygulamayı üst kutuya koyma ✅
5. Hava durumu ayrıntı ekranı ✅
6. Şehir seçimi ✅
7. Odak efektleri ✅
8. Köşe şekli ✅
9. Gradyan arka planlar ✅
10. 12/24 saat ✅
11. Logo kartı / simge modu ✅
12. Son kullanılana göre sıralama ✅
13. Wi-Fi / Bluetooth kısayolları ✅
14. Gezinme sesi ✅
15. Uygulama bilgisi / kaldır ✅
16. "İzlemeye devam et" (Watch Next) satırı — TV'de erişim denenecek, çalışırsa kartlara (YouTube son izlenenler) eklenecek
17. Netflix "yeni çıkanlar" kartı (TMDB; ticari lisans şartı kontrol edilecek)
18. Tema B (Neon Oyun) ve C (Hava Merkezli) ✅ (ilk sürüm) — tasarımlar `docs/tasarim/` içinde, ayrıntı aşağıda "Temalar"
19. Duvar kağıdı resmi + ekran koruyucu
20. TİS takvim kutusu (ayrı TV'ye özel TİS uygulaması hazır olunca; paket `com.wizaicorp.tisasistani`)

## Temalar (3 tane, kullanıcı Görünüm ekranından seçer)
Tasarım dosyaları: `docs/tasarim/` (tarayıcıda açılır, 1280x720). Tema = sadece renk değil, ana ekran DÜZENİ de değişir.
Arka plan/vurgu rengi/köşe/odak ayarları her temada çalışır; animasyon kapalıysa hepsi sabit.

**A — Koyu Bento** (`Main.dc.html`, şu an kodlu olan): büyük saat, bento kutular.
Netflix "Yeni çıkanlar" kartı (3 afiş), TİS Asistanı takvim kutusu (ay + çalışma günü işaretleri),
YouTube Music "Favori listem" kutusu, Bluetooth/kumanda durum kutusu, hava kutusu. Renkler: #1A1F29 kutu, #F2F4F8 yazı, #9AA4B5 soluk.

**B — Neon Oyun** (`Neon.dc.html`): mor/camgöbeği/pembe neon vurgular (#8B5CF6, #22D3EE, #FF3D8B, #FDE047),
selamlama ("İYİ ÖĞLEDENLER") + saat, yatay 5 günlük hava şeridi, Netflix "YENİ" satırı (Oynat düğmesi),
YouTube Music "Hızlı çal" (Favorilerim, Çalıyor), parlayan odak çerçevesi.

**C — Hava Merkezli** (`Hava.dc.html`): lacivert gökyüzü (#0B1B36 → #2C5F9E), ortada dev hava durumu
(18°, Hissedilen / Rüzgar / Nem), 5 günlük tahmin, altta küçük TİS, Netflix ve diğer kutular.

### Tema durumu
A, B ve C kodlandı (Görünüm → "Ana ekran teması", OK ile değişir; tema değişince arka plan/vurgu rengi temaya göre ayarlanır). Kutu içerikleri (Netflix yeni çıkanlar, YouTube Music, TİS takvimi) veri kaynağı gelince eklenecek.

### Tema notları (tasarım ilkeleri)
- `AppState`'e `theme` (0=A, 1=B, 2=C) ekle, `AppearanceScreen`'de "Tema" satırı (OK ile değişir).
- `HomeContent` içinde temaya göre ayrı layout composable'ları: `ThemeABento`, `ThemeBNeon`, `ThemeCWeather`.
  Ortak parçalar (FocusTile, AppIcon, AppCard, WeatherTile, Picker) paylaşılsın, tekrar yazılmasın.
- Kutu içerikleri placeholder'dır ([Film adı] gibi). Gerçek veri gelene kadar boş kart yerine
  "yakında" göstermeyin; veri yoksa o kutu sadece uygulama logosu olsun (Netflix: TMDB, YouTube: Watch Next, TİS: ayrı TV uygulaması).
- Her temada tüm kutular kullanıcı tarafından değiştirilebilir kalsın (hardcode yok).

## Diğer yapılacaklar
- Chrome / tarayıcı kısayolu (TV Bro vb.)
- Depoyu private yapma
- Play Store için ayrı release imza anahtarı (GitHub Secret), Play TV launcher politikası kontrolü
- Varsayılan ana ekran yapma (stok launcher'ı silmeden; `pm disable-user` / geri alma `pm enable`)

## Derleme
GitHub Actions: `.github/workflows/build.yml` → Actions → Build APK → Run workflow.
Çıktı: `WizaiTV-debug` artifact (APK). Test imzası `app/debug.keystore` (yalnızca debug; Play anahtarı ayrı olacak).

## Kurulum (Termux)
```
adb connect 192.168.1.101:5555
adb install -r app-debug.apk
```
İlk kez imza değiştiyse önce: `adb uninstall com.wizaicorp.tvlauncher`

Geri dönüş: `adb shell pm enable com.google.android.tvlauncher`

## Geliştirici notları
- Ayarlar: `AppState.kt` (SharedPreferences `wizai_prefs`), görünüm: `Look.kt`
- Ekranlar: `HomeScreen.kt`, `AppearanceScreen.kt`, `PickerScreen.kt`, `WeatherUi.kt`
- Kotlin 1.9.24, AGP 8.5.2, Gradle 8.7, Compose BOM 2024.09.03, minSdk 21, targetSdk 34
