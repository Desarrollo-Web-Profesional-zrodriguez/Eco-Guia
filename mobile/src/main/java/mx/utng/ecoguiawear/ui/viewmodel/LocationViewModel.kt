/**
 * Archivo: LocationViewModel.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Gestiona la ubicación del usuario en tiempo real, carga sitios históricos cercanos
 * utilizando geofencing lógico y dispara alertas de proximidad para interactuar con el entorno.
 * También controla el ciclo de vida del ProximityService (Opción C).
 *
 * Funciones destacadas:
 * - startLocationUpdates: Inicia el rastreo GPS con alta precisión (foreground).
 * - fetchNearbySites: Consulta Neon para obtener sitios en un radio de 50km y calcula distancias.
 * - syncTargetWithWatch: Envía el sitio objetivo al reloj Wear OS.
 * - startProximityService: Inicia el ForegroundService de geofencing en segundo plano.
 * - stopProximityService: Detiene el ForegroundService de geofencing.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
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
import mx.utng.ecoguiawear.data.ProximityService
import mx.utng.ecoguiawear.data.wear.WearMessageClient

class LocationViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private var wearMessageClient: WearMessageClient? = null

    private val _currentLocation = mutableStateOf<Location?>(null)
    val currentLocation: State<Location?> = _currentLocation

    private val _nearbySites = mutableStateOf<List<RemoteHistoricalSite>>(emptyList())
    val nearbySites: State<List<RemoteHistoricalSite>> = _nearbySites

    private val _closestSite = mutableStateOf<RemoteHistoricalSite?>(null)
    val closestSite: State<RemoteHistoricalSite?> = _closestSite

    private val _dbCategories = mutableStateOf<List<mx.utng.ecoguia.shared.domain.model.RemoteCategory>>(emptyList())
    val dbCategories: State<List<mx.utng.ecoguia.shared.domain.model.RemoteCategory>> = _dbCategories

    /** Estado observable del ProximityService para que la UI pueda mostrar el toggle. */
    private val _isProximityServiceActive = mutableStateOf(false)
    val isProximityServiceActive: State<Boolean> = _isProximityServiceActive

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    /**
     * Inicia el rastreo de ubicación en tiempo real.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        if (wearMessageClient == null) {
            wearMessageClient = WearMessageClient(context)
        }
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
     * Carga todos los sitios históricos activos sin restricción de distancia (ideal para crear rutas).
     */
    fun loadAllHistoricalSites() {
        viewModelScope.launch {
            try {
                val sites = repository.getHistoricalSites()
                _nearbySites.value = sites
                val cats = repository.getSiteCategories()
                _dbCategories.value = cats
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error al cargar sitios/categorías: ${e.message}")
            }
        }
    }

    /**
     * Consulta el repositorio para obtener sitios en un radio de 50km.
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

    /**
     * Sincroniza un sitio seleccionado con el reloj inteligente.
     */
    fun syncTargetWithWatch(site: RemoteHistoricalSite) {
        viewModelScope.launch {
            val siteLat = site.latitude ?: return@launch
            val siteLng = site.longitude ?: return@launch
            wearMessageClient?.syncTarget(site.id, site.name, siteLat, siteLng)
        }
    }

    /**
     * Inicia el [ProximityService] como ForegroundService de tipo location.
     * Debe llamarse después de confirmar que el usuario concedió ACCESS_BACKGROUND_LOCATION
     * y POST_NOTIFICATIONS (API 33+).
     * @param context Contexto de la aplicación o actividad.
     */
    fun startProximityService(context: Context) {
        if (_isProximityServiceActive.value) return
        val intent = Intent(context, ProximityService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _isProximityServiceActive.value = true
        Log.d("LocationViewModel", "ProximityService iniciado.")
    }

    /**
     * Detiene el [ProximityService] y actualiza el estado observable.
     * @param context Contexto de la aplicación o actividad.
     */
    fun stopProximityService(context: Context) {
        if (!_isProximityServiceActive.value) return
        context.stopService(Intent(context, ProximityService::class.java))
        _isProximityServiceActive.value = false
        Log.d("LocationViewModel", "ProximityService detenido.")
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
