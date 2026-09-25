package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {

    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Throwable) {
        null
    }

    fun vibrateTap() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 20, amplitude = 70)
    }

    fun vibrateBlocked() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 60, amplitude = 180)
    }

    fun vibrateSuccess() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 40, amplitude = 120)
    }

    fun vibrateLevelComplete() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 120, amplitude = 220)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            val vib = vibrator ?: return
            if (vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(durationMs)
                }
            }
        } catch (_: Throwable) {
            // Graceful fallback
        }
    }
}
