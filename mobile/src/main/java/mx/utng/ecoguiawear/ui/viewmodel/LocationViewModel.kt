/**
 * Archivo: LocationViewModel.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Gestiona la ubicación del usuario en tiempo real, carga sitios históricos cercanos 
 * utilizando geofencing lógico y dispara alertas de proximidad para interactuar con el entorno.
 * 
 * Funciones destacadas:
 * - startLocationUpdates: Inicia el rastreo GPS con alta precisión.
 * - fetchNearbySites: Consulta la base de datos remota para obtener sitios en un radio de 1km y calcula distancias.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class LocationViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private val _currentLocation = mutableStateOf<Location?>(null)
    val currentLocation: State<Location?> = _currentLocation

    private val _nearbySites = mutableStateOf<List<RemoteHistoricalSite>>(emptyList())
    val nearbySites: State<List<RemoteHistoricalSite>> = _nearbySites
    
    private val _closestSite = mutableStateOf<RemoteHistoricalSite?>(null)
    val closestSite: State<RemoteHistoricalSite?> = _closestSite

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    /**
     * Inicia el rastreo de ubicación en tiempo real.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _currentLocation.value = location
                    fetchNearbySites(location)
                }
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error al solicitar actualizaciones de ubicación: ${e.message}")
        }
    }

    /**
     * Detiene el rastreo de ubicación.
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
    }

    /**
     * Consulta el repositorio para obtener sitios en un radio de 1km.
     */
    private fun fetchNearbySites(location: Location) {
        viewModelScope.launch {
            try {
                // Buscamos en un radio de 50000 metros (50km) para asegurar visibilidad de registros de prueba
                val sites = repository.getNearbySites(location.latitude, location.longitude, 50000)
                _nearbySites.value = sites
                
                // Lógica de proximidad: encontrar el sitio más cercano dentro de su propio radio de detección
                var closest: RemoteHistoricalSite? = null
                var minDistance = Double.MAX_VALUE

                sites.forEach { site ->
                    val siteLat = site.latitude ?: return@forEach
                    val siteLng = site.longitude ?: return@forEach
                    
                    val results = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, siteLat, siteLng, results)
                    val distance = results[0].toDouble()

                    if (distance <= site.detectionRadiusM && distance < minDistance) {
                        minDistance = distance
                        closest = site
                    }
                }
                _closestSite.value = closest
                
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error al obtener sitios cercanos: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
