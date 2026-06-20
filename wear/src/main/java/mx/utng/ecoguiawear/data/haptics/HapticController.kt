package mx.utng.ecoguiawear.data.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import mx.utng.ecoguiawear.domain.model.HapticStrength

enum class HapticPulse {
    LINKED,
    TOGGLE,
    NEARBY,
    ARRIVED
}

class HapticController(context: Context) {

    private val appContext = context.applicationContext

    fun pulse(type: HapticPulse, strength: HapticStrength) {
        val vibrator = getVibrator()
        if (!vibrator.hasVibrator()) return

        val baseDuration = when (type) {
            HapticPulse.LINKED -> 60L
            HapticPulse.TOGGLE -> 40L
            HapticPulse.NEARBY -> 90L
            HapticPulse.ARRIVED -> 140L
        }
        val multiplier = when (strength) {
            HapticStrength.LOW -> 0.7f
            HapticStrength.MEDIUM -> 1f
            HapticStrength.HIGH -> 1.35f
        }
        val duration = (baseDuration * multiplier).toLong()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
