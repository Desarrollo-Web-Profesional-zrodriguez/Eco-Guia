package mx.utng.ecoguiawear.data.wear

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

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

    fun stopUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
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

        fun calculateDistance(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
            val results = FloatArray(1)
            Location.distanceBetween(startLat, startLng, endLat, endLng, results)
            return results[0]
        }
    }
}
