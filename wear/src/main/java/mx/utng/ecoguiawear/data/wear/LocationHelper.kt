/**
 * Utilidad de geolocalización GPS y cálculo de rumbos/distancias geodésicas en Wear OS.
 *
 * Utiliza [com.google.android.gms.location.FusedLocationProviderClient] para actualizaciones
 * periódicas de alta precisión y métodos estáticos para cálculos esféricos entre coordenadas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

/**
 * Gestor de ubicación en tiempo real para el smartwatch.
 *
 * @param context Contexto de la aplicación para inicializar el cliente de localización.
 * @param onLocationUpdate Función lambda invocada al registrar una nueva posición GPS válida.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class LocationHelper(
    private val context: Context,
    private val onLocationUpdate: (Location) -> Unit
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { onLocationUpdate(it) }
        }
    }

    /**
     * Inicia la suscripción a actualizaciones de ubicación continua cada 2 segundos.
     */
    @SuppressLint("MissingPermission")
    fun startUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()
        
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error al iniciar GPS: ${e.message}")
        }
    }

    /**
     * Cancela la recepción de actualizaciones GPS para ahorrar batería.
     */
    fun stopUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        /**
         * Calcula el ángulo de rumbo (bearing) en grados desde un punto inicial hacia un destino.
         *
         * @param startLat Latitud del punto de partida.
         * @param startLng Longitud del punto de partida.
         * @param endLat Latitud del punto de llegada.
         * @param endLng Longitud del punto de llegada.
         * @return Ángulo de rumbo en grados respecto al norte.
         */
        fun calculateBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
            val startLocation = Location("").apply {
                latitude = startLat
                longitude = startLng
            }
            val endLocation = Location("").apply {
                latitude = endLat
                longitude = endLng
            }
            return startLocation.bearingTo(endLocation)
        }

        /**
         * Calcula la distancia geodésica en línea recta expresada en metros entre dos coordenadas.
         *
         * @param startLat Latitud de origen.
         * @param startLng Longitud de origen.
         * @param endLat Latitud de destino.
         * @param endLng Longitud de destino.
         * @return Distancia en metros.
         */
        fun calculateDistance(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
            val results = FloatArray(1)
            Location.distanceBetween(startLat, startLng, endLat, endLng, results)
            return results[0]
        }
    }
}
