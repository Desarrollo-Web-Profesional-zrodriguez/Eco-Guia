/**
 * Archivo: RouteViewModel.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Gestiona el ciclo de vida de las rutas turísticas: carga de catálogo, selección
 * y seguimiento en tiempo real de paradas de la ruta activa, sincronización con Wear OS
 * y creación de rutas turísticas para administradores.
 *
 * Funciones destacadas:
 * - loadRoutes: Carga la lista de rutas activas desde la base de datos Neon PostgreSQL.
 * - startRoute: Activa una ruta, consulta sus paradas ordenadas y sincroniza con el reloj Wear OS.
 * - updateProgressWithLocation: Comprueba si la ubicación GPS del usuario está a <50m de alguna parada para marcarla como completada.
 * - createRoute: Publica una nueva ruta turística con paradas seleccionadas en la base de datos Neon.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteRoute
import mx.utng.ecoguia.shared.domain.model.RemoteRouteStop
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository
import mx.utng.ecoguiawear.data.wear.WearMessageClient

class RouteViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private var wearMessageClient: WearMessageClient? = null

    // Lista de rutas disponibles en el catálogo
    private val _routes = mutableStateOf<List<RemoteRoute>>(emptyList())
    val routes: State<List<RemoteRoute>> = _routes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Ruta actualmente activa por el turista
    private val _activeRoute = mutableStateOf<RemoteRoute?>(null)
    val activeRoute: State<RemoteRoute?> = _activeRoute

    // Paradas de la ruta activa
    private val _activeStops = mutableStateOf<List<RemoteRouteStop>>(emptyList())
    val activeStops: State<List<RemoteRouteStop>> = _activeStops

    // Mapa de paradas completadas (stopId -> true)
    val completedStops = mutableStateMapOf<String, Boolean>()

    // Estado de creación de ruta
    private val _createSuccess = mutableStateOf<Boolean?>(null)
    val createSuccess: State<Boolean?> = _createSuccess

    init {
        loadRoutes()
    }

    /**
     * Carga el catálogo de rutas activas desde Neon.
     */
    fun loadRoutes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _routes.value = repository.getRoutes()
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Error al cargar rutas: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga las rutas turísticas cercanas a la ubicación actual del usuario (radio 50km).
     */
    fun loadNearbyRoutes(lat: Double, lng: Double, radiusM: Int = 50000) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _routes.value = repository.getNearbyRoutes(lat, lng, radiusM)
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Error al cargar rutas cercanas: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Inicia una ruta turística, consulta sus paradas y las sincroniza con Wear OS.
     */
    fun startRoute(context: Context, route: RemoteRoute) {
        if (wearMessageClient == null) {
            wearMessageClient = WearMessageClient(context)
        }

        viewModelScope.launch {
            _isLoading.value = true
            _activeRoute.value = route
            completedStops.clear()

            try {
                val stops = repository.getRouteStops(route.id)
                _activeStops.value = stops

                // Sincronizar paradas con Wear OS (id|nombre|lat|lng)
                val waypoints = stops.mapNotNull { stop ->
                    val lat = stop.latitude
                    val lng = stop.longitude
                    val name = stop.siteName ?: "Parada ${stop.stopOrder}"
                    val id = stop.siteId.ifEmpty { stop.id }
                    if (lat != null && lng != null) "${id}|${name}" to (lat to lng) else null
                }

                if (waypoints.isNotEmpty()) {
                    wearMessageClient?.syncRoute(route.title, waypoints)
                    Log.d("RouteViewModel", "Ruta '${route.title}' sincronizada con Wear OS (${waypoints.size} paradas)")
                }
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Error al iniciar ruta: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza el progreso de la ruta según la posición GPS en vivo del usuario.
     * Respeta el orden secuencial estricto: solo permite completar la primera parada no visitada.
     */
    fun updateProgressWithLocation(location: Location) {
        val stops = _activeStops.value
        val firstUnvisited = stops.firstOrNull { completedStops[it.id] != true } ?: return
        val lat = firstUnvisited.latitude ?: return
        val lng = firstUnvisited.longitude ?: return

        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, lat, lng, results)
        val distance = results[0].toDouble()

        if (distance <= 50.0) {
            completedStops[firstUnvisited.id] = true
            Log.d("RouteViewModel", "Parada secuencial completada: ${firstUnvisited.siteName} (${distance.toInt()}m)")
            notifyWearProgress()
        }
    }

    /**
     * Marca manualmente una parada como completada o no completada (toggle).
     * En caso de marcar completada, exige que todas las paradas anteriores estén completadas.
     * En caso de desmarcar, desmarca también todas las paradas posteriores para mantener orden estricto.
     */
    fun toggleStopCompleted(stopId: String) {
        val stops = _activeStops.value
        val targetIndex = stops.indexOfFirst { it.id == stopId }
        if (targetIndex == -1) return

        val isCurrentlyDone = completedStops[stopId] == true

        if (!isCurrentlyDone) {
            // Para completar esta parada, debemos asegurar que las anteriores estén completadas
            val canComplete = (0 until targetIndex).all { idx -> completedStops[stops[idx].id] == true }
            if (canComplete) {
                completedStops[stopId] = true
                notifyWearProgress()
            } else {
                Log.w("RouteViewModel", "No se puede completar la parada $stopId antes de las anteriores.")
            }
        } else {
            // Al desmarcar, desmarcamos esta parada y todas las posteriores
            for (idx in targetIndex until stops.size) {
                completedStops[stops[idx].id] = false
            }
            notifyWearProgress()
        }
    }

    private fun notifyWearProgress() {
        val completed = completedStops.values.count { it }
        val total = _activeStops.value.size
        viewModelScope.launch {
            try {
                wearMessageClient?.syncRouteProgress(completed, total)
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Error sincronizando avance con Wear OS: ${e.message}")
            }
        }
    }

    /**
     * Cancela o da por terminada la ruta activa actual y envía la señal de cancelación al reloj.
     */
    fun stopActiveRoute() {
        _activeRoute.value = null
        _activeStops.value = emptyList()
        completedStops.clear()
        viewModelScope.launch {
            try {
                wearMessageClient?.cancelRoute()
                Log.d("RouteViewModel", "Ruta cancelada en el móvil y señal enviada al reloj")
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Error al enviar cancelación a Wear OS: ${e.message}")
            }
        }
    }

    /**
     * Marca la ruta como completada y envía la señal de felicitación al reloj.
     */
    fun completeActiveRoute() {
        _activeRoute.value = null
        _activeStops.value = emptyList()
        completedStops.clear()
        viewModelScope.launch {
            try {
                wearMessageClient?.completeRoute()
                Log.d("RouteViewModel", "Ruta completada enviada al reloj")
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Error al enviar señal de completado a Wear OS: ${e.message}")
            }
        }
    }

    /**
     * Publica una nueva ruta turística en Neon PostgreSQL.
     */
    fun createRoute(
        title: String,
        description: String,
        estimatedMinutes: Int,
        selectedSiteIds: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _createSuccess.value = null
            try {
                val success = repository.createRoute(title, description, estimatedMinutes, selectedSiteIds)
                _createSuccess.value = success
                if (success) loadRoutes()
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Error al crear ruta: ${e.message}")
                _createSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Resetea el estado de éxito de creación. */
    fun resetCreateState() {
        _createSuccess.value = null
    }
}
