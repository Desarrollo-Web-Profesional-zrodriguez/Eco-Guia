/**
 * Archivo: LocationViewModel.kt
 *
 * Gestiona la ubicación del usuario en tiempo real mediante el proveedor FusedLocationProviderClient,
 * consulta sitios históricos cercanos en un radio de cobertura, calcula distancias de proximidad,
 * sincroniza sitios objetivos con el dispositivo Wear OS y controla el ciclo de vida del [ProximityService].
 *
 * @since 2026-08-05
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

/**
 * ViewModel que expone el flujo reactivo de ubicación GPS, sitios históricos detectados y estado del servicio de geofencing.
 *
 * @param repository Repositorio de datos para consulta de sitios y categorías turísticas.
 */
class LocationViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private var wearMessageClient: WearMessageClient? = null

    private val _currentLocation = mutableStateOf<Location?>(null)
    val currentLocation: State<Location?> = _currentLocation

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _nearbySites = mutableStateOf<List<RemoteHistoricalSite>>(emptyList())
    val nearbySites: State<List<RemoteHistoricalSite>> = _nearbySites

    private val _closestSite = mutableStateOf<RemoteHistoricalSite?>(null)
    val closestSite: State<RemoteHistoricalSite?> = _closestSite

    private val _dbCategories = mutableStateOf<List<mx.utng.ecoguia.shared.domain.model.RemoteCategory>>(emptyList())
    val dbCategories: State<List<mx.utng.ecoguia.shared.domain.model.RemoteCategory>> = _dbCategories

    /** Estado observable del ProximityService para que la UI pueda mostrar el toggle. */
    private val _isProximityServiceActive = mutableStateOf(false)
    val isProximityServiceActive: State<Boolean> = _isProximityServiceActive

    private val notifiedSitesForeground = mutableSetOf<String>()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    /**
     * Inicia el rastreo continuo de ubicación en primer plano utilizando FusedLocationProviderClient.
     *
     * @param context Contexto de la aplicación o actividad.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        mx.utng.ecoguiawear.data.ProximityNotificationHelper.createChannels(context)
        syncProximityState(context)

        if (wearMessageClient == null) {
            wearMessageClient = WearMessageClient(context)
        }
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        // Si las alertas en segundo plano están activadas, asegurar que el servicio esté corriendo
        if (_isProximityServiceActive.value) {
            startProximityService(context)
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
     * Detiene el rastreo y suscripción a las actualizaciones de ubicación del proveedor GPS.
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
    }

    /**
     * Carga todos los sitios históricos activos y sus categorías sin restricción de distancia.
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
                    val siteLat = site.getComputedLatitude() ?: return@forEach
                    val siteLng = site.getComputedLongitude() ?: return@forEach
                    
                    val results = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, siteLat, siteLng, results)
                    val distance = results[0].toDouble()

                    if (distance <= site.detectionRadiusM) {
                        if (distance < minDistance) {
                            minDistance = distance
                            closest = site
                        }

                        // Emitir notificación si el usuario entra al sitio mientras la app está ABIERTA
                        if (site.id !in notifiedSitesForeground) {
                            notifiedSitesForeground.add(site.id)
                            emitForegroundAlert(site.id, site.name, distance.toInt())
                        }
                    } else if (distance > site.detectionRadiusM + 20) {
                        // Resetear para volver a notificar si sale y regresa al sitio
                        notifiedSitesForeground.remove(site.id)
                    }
                }
                _closestSite.value = closest
                
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error al obtener sitios cercanos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun emitForegroundAlert(siteId: String, siteName: String, distanceM: Int) {
        val ctx = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext ?: return

        // 1. Broadcast para alerta visual In-App (Snackbar)
        val localIntent = Intent("mx.utng.ecoguiawear.PROXIMITY_ALERT").apply {
            putExtra("siteName", siteName)
            putExtra("distance", distanceM)
        }
        ctx.sendBroadcast(localIntent)

        // 2. Alerta a Wear OS
        viewModelScope.launch {
            try {
                wearMessageClient?.sendAlert(
                    id = siteId,
                    message = " $siteName ($distanceM m)",
                    type = "SITE"
                )
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error enviando alerta a Wear OS: ${e.message}")
            }
        }

        // 3. Notificación flotante de sistema Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    ctx,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        try {
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val notification = mx.utng.ecoguiawear.data.ProximityNotificationHelper.buildSiteAlertNotification(
                context = ctx,
                siteName = siteName,
                distance = distanceM
            )
            notificationManager.notify(siteId.hashCode(), notification)
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error emitiendo notificación del sistema: ${e.message}")
        }
    }

    /**
     * Sincroniza un sitio seleccionado con el reloj inteligente Wear OS.
     *
     * @param site Sitio histórico a transmitir.
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
     *
     * @param context Contexto de la aplicación o actividad.
     */
    fun startProximityService(context: Context) {
        try {
            val intent = Intent(context, ProximityService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _isProximityServiceActive.value = true
            context.getSharedPreferences("eco_prefs", Context.MODE_PRIVATE).edit().putBoolean("proximity_active", true).apply()
            Log.d("LocationViewModel", "ProximityService iniciado correctamente.")
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error al iniciar ProximityService: ${e.message}")
        }
    }

    /**
     * Detiene el [ProximityService] y actualiza el estado observable.
     *
     * @param context Contexto de la aplicación o actividad.
     */
    fun stopProximityService(context: Context) {
        try {
            context.stopService(Intent(context, ProximityService::class.java))
        } catch (e: Exception) {
            Log.e("LocationViewModel", "Error al detener ProximityService: ${e.message}")
        }
        _isProximityServiceActive.value = false
        context.getSharedPreferences("eco_prefs", Context.MODE_PRIVATE).edit().putBoolean("proximity_active", false).apply()
        Log.d("LocationViewModel", "ProximityService detenido.")
    }

    /**
     * Sincroniza el estado visual con las preferencias guardadas, para que el Toggle
     * no se reinicie si el usuario sale y vuelve a la pantalla.
     *
     * @param context Contexto de la aplicación.
     */
    fun syncProximityState(context: Context) {
        val prefs = context.getSharedPreferences("eco_prefs", Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean("proximity_active", false)
        _isProximityServiceActive.value = isActive
        if (isActive) {
            startProximityService(context)
        }
    }

    /**
     * Libera las actualizaciones de ubicación al destruirse el ViewModel.
     */
    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
