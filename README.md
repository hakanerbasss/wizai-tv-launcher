# Wizai-Launcher

Android TV ana ekran uygulaması (Kotlin + Jetpack Compose).

## Özellikler
- Koyu bento tasarım, kumanda (D-pad) ile gezinme
- 3 kutu kullanıcı seçimli: OK = aç, OK'e uzun bas = başka uygulama seç
- Görünüm: arka plan rengi, vurgu rengi, animasyonları aç/kapat
- Animasyonlu hava durumu (Open-Meteo, İstanbul, 5 gün)

## APK'yı indir
GitHub → Actions → son "Build APK" çalışması → Artifacts → `WizaiTV-debug`.

## TV'ye kur (Termux)
```
adb connect TV_IP:5555
adb install -r app-debug.apk
adb shell monkey -p com.wizaicorp.tvlauncher -c android.intent.category.LEANBACK_LAUNCHER 1
```

## Kaldır
```
adb uninstall com.wizaicorp.tvlauncher
```
