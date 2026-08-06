/**
 * Controlador de efectos hápticos y patrones de vibración para relojes inteligentes Wear OS.
 *
 * Centraliza la emisión de pulsos sensoriales para avisos de emparejamiento, cambios de estado,
 * proximidad a monumentos históricos y llegada a coordenadas de interés turístico.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import mx.utng.ecoguiawear.domain.model.HapticStrength

/**
 * Tipos de eventos sensoriales o pulsos hápticos reconocidos por el sistema Wear OS.
 */
enum class HapticPulse {
    /** Pulso de confirmación tras vincular exitosamente el reloj con el teléfono. */
    LINKED,

    /** Pulso breve de interacción o alternancia de interruptores/botones. */
    TOGGLE,

    /** Pulso de advertencia al ingresar al radio de proximidad intermedia (<100m). */
    NEARBY,

    /** Pulso sostenido de éxito al alcanzar físicamente el objetivo (<30m). */
    ARRIVED
}

/**
 * Administra el actuador vibratorio del smartwatch según la versión del sistema operativo.
 *
 * @param context Contexto de la aplicación para acceder a los servicios de hardware [Vibrator] o [VibratorManager].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class HapticController(context: Context) {

    private val appContext = context.applicationContext

    /**
     * Emite un pulso háptico con la duración e intensidad configuradas.
     *
     * @param type Tipo de pulso táctil a emitir ([HapticPulse]).
     * @param strength Nivel de fuerza de la vibración ([HapticStrength]).
     */
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

    /**
     * Obtiene la instancia del motor de vibración compatible con la versión de API del dispositivo.
     *
     * @return Instancia activa de [Vibrator].
     */
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
