/**
 * Lector de orientación espacial y compás magnético para relojes Wear OS.
 *
 * Combina las lecturas del acelerómetro ([android.hardware.Sensor.TYPE_ACCELEROMETER]) y del
 * magnetómetro ([android.hardware.Sensor.TYPE_MAGNETIC_FIELD]) mediante matrices de rotación
 * para calcular el azimut absoluto normalizado (0° - 360°).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Gestor de sensores de movimiento y orientación del reloj.
 *
 * @param context Contexto de la aplicación para obtener el [SensorManager].
 * @param onHeadingUpdate Callback invocado con el ángulo de orientación actualizado (en grados).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class SensorHelper(
    context: Context,
    private val onHeadingUpdate: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    /**
     * Registra los observadores de sensores con tasa de muestreo de interfaz de usuario ([SensorManager.SENSOR_DELAY_UI]).
     */
    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    /**
     * Desregistra los listeners de sensores para liberar hardware y ahorrar energía.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Callback de cambio de valores en sensores de aceleración o campo magnético.
     *
     * @param event Datos del evento del sensor físico.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values
        }

        if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                
                // Azimuth (heading) is orientation[0] in radians
                val azimuthRadians = orientation[0]
                val azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
                
                // Normalizar a 0-360
                val normalizedHeading = (azimuthDegrees + 360) % 360
                onHeadingUpdate(normalizedHeading.toFloat())
            }
        }
    }

    /**
     * Callback de cambio en la precisión del sensor.
     *
     * @param sensor Instancia del sensor involucrado.
     * @param accuracy Nuevo nivel de precisión reportado.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
