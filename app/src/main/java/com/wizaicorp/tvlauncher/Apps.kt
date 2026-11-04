package com.wizaicorp.tvlauncher

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap?
)

/** TV'de yüklü uygulamaları listeler (TV uygulamaları + normal Android uygulamaları). */
fun loadApps(context: Context): List<AppEntry> {
    val pm = context.packageManager
    val result = LinkedHashMap<String, AppEntry>()

    fun collect(category: String) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        for (info in pm.queryIntentActivities(intent, 0)) {
            val ai = info.activityInfo ?: continue
            if (ai.packageName == context.packageName) continue
            if (result.containsKey(ai.packageName)) continue
            val icon = try {
                info.loadIcon(pm).toBitmap(96, 96).asImageBitmap()
            } catch (e: Exception) {
                null
            }
            result[ai.packageName] = AppEntry(
                label = info.loadLabel(pm).toString(),
                packageName = ai.packageName,
                activityName = ai.name,
                icon = icon
            )
        }
    }

    collect(Intent.CATEGORY_LEANBACK_LAUNCHER)
    collect(Intent.CATEGORY_LAUNCHER)
    return result.values.sortedBy { it.label.lowercase() }
}

fun isInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
}

fun launchApp(context: Context, packageName: String): Boolean {
    val pm = context.packageManager
    val intent = pm.getLeanbackLaunchIntentForPackage(packageName)
        ?: pm.getLaunchIntentForPackage(packageName)
        ?: return false
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}
