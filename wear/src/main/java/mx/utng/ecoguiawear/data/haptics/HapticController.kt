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

        val duration = when (type) {
            HapticPulse.LINKED -> 80L
            HapticPulse.TOGGLE -> 50L
            HapticPulse.NEARBY -> 120L
            HapticPulse.ARRIVED -> 200L
        }
        val amplitude = when (strength) {
            HapticStrength.LOW -> 80
            HapticStrength.MEDIUM -> 170
            HapticStrength.HIGH -> 255
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, amplitude)
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
