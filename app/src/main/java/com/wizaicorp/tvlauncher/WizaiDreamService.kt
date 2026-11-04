package com.wizaicorp.tvlauncher

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

/**
 * Basit ekran koruyucu: koyu arka plan üzerinde saat, ekran yanmasını önlemek için
 * zaman zaman yer değiştirir. Sistem Ayarlar > Ekran koruyucu listesinde görünür.
 */
class WizaiDreamService : DreamService() {
    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()
    private var clock: TextView? = null
    private var root: FrameLayout? = null

    private val tick = object : Runnable {
        override fun run() {
            clock?.text = SimpleDateFormat("HH:mm", Locale("tr", "TR")).format(Date())
            handler.postDelayed(this, 30_000L)
        }
    }

    private val move = object : Runnable {
        override fun run() {
            val r = root
            val c = clock
            if (r != null && c != null && r.width > 0 && r.height > 0) {
                val params = c.layoutParams as FrameLayout.LayoutParams
                val maxW = (r.width - c.width).coerceAtLeast(1)
                val maxH = (r.height - c.height).coerceAtLeast(1)
                params.leftMargin = random.nextInt(maxW)
                params.topMargin = random.nextInt(maxH)
                c.layoutParams = params
            }
            handler.postDelayed(this, 60_000L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true

        val frame = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }
        val clockView = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 64f
            gravity = Gravity.CENTER
        }
        frame.addView(
            clockView,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        )
        setContentView(frame)
        root = frame
        clock = clockView

        handler.post(tick)
        handler.postDelayed(move, 2_000L)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
        root = null
        clock = null
    }
}
