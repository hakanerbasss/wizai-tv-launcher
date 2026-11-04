# Wizai TV Launcher

Android TV ana ekran uygulaması (Kotlin + Jetpack Compose). Şu an: Tema A (Koyu Bento).

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
