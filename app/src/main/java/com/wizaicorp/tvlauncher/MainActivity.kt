package com.wizaicorp.tvlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf

/** Ekrana her dönüldüğünde uygulama listesini yenilemek için sayaç. */
object ResumeSignal {
    val tick = mutableStateOf(0)
}

/**
 * Duvar kağıdı resmi seçimi Activity sonucu gerektirdiği için burada,
 * Compose tarafı (AppearanceScreen) [launch] ile tetikler, sonucu [pickedUri] ile okur.
 */
object WallpaperPicker {
    val pickedUri = mutableStateOf<Uri?>(null)
    var launch: (() -> Unit)? = null
}

class MainActivity : ComponentActivity() {
    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // izin kalıcı olmayabilir; yine de bu oturumda kullanılabilir
            }
            WallpaperPicker.pickedUri.value = uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WallpaperPicker.launch = { pickImage.launch(arrayOf("image/*")) }
        setContent {
            HomeScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        ResumeSignal.tick.value = ResumeSignal.tick.value + 1
    }
}
