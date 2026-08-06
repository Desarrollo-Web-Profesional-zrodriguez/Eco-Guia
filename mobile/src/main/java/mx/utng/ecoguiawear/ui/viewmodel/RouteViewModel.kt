/**
 * Archivo: RouteViewModel.kt
 *
 * Gestiona el ciclo de vida de las rutas turísticas: carga de catálogo, selección y seguimiento en tiempo real
 * de paradas de la ruta activa, sincronización con Wear OS y creación de nuevas rutas turísticas.
 *
 * @since 2026-08-05
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

/**
 * ViewModel que expone el catálogo de rutas turísticas, el estado de navegación de la ruta activa y su comunicación con Wear OS.
 *
 * @param repository Repositorio de datos para operaciones de rutas y paradas.
 */
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
        restoreActiveRouteFromPreferences()
        loadRoutes()
    }

    private fun restoreActiveRouteFromPreferences() {
        val context = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext ?: return
        val prefs = context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE)
        val routeId = prefs.getString("active_route_id", null)
        val routeTitle = prefs.getString("active_route_title", null)
        if (!routeId.isNullOrBlank() && !routeTitle.isNullOrBlank()) {
            val routeDesc = prefs.getString("active_route_desc", "").orEmpty()
            val estMins = prefs.getInt("active_route_est", 45)
            val restoredRoute = RemoteRoute(
                id = routeId,
                title = routeTitle,
                description = routeDesc,
                estimatedMinutes = estMins
            )
            _activeRoute.value = restoredRoute

            // Restaurar paradas desde JSON guardado
            val stopsJson = prefs.getString("active_stops_json", null)
            if (!stopsJson.isNullOrBlank()) {
                try {
                    val array = org.json.JSONArray(stopsJson)
                    val list = mutableListOf<RemoteRouteStop>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            RemoteRouteStop(
                                id = obj.getString("id"),
                                routeId = routeId,
                                siteId = obj.optString("site_id", ""),
                                geoDropId = obj.optString("geo_drop_id", ""),
                                stopOrder = obj.optInt("stop_order", i + 1),
                                instruction = obj.optString("instruction", ""),
                                siteName = obj.optString("site_name", "Parada ${i + 1}"),
                                latitude = if (obj.has("lat")) obj.getDouble("lat") else null,
                                longitude = if (obj.has("lng")) obj.getDouble("lng") else null
                            )
                        )
                    }
                    _activeStops.value = list
                } catch (e: Exception) {
                    Log.e("RouteViewModel", "Error leyendo paradas desde JSON: ${e.message}")
                }
            }

            // Restaurar paradas completadas
            val completedSet = prefs.getStringSet("completed_stops_set", emptySet())
            completedStops.clear()
            completedSet?.forEach { completedStops[it] = true }

            viewModelScope.launch {
                try {
                    val freshStops = repository.getRouteStops(routeId)
                    if (freshStops.isNotEmpty()) {
                        _activeStops.value = freshStops
                        saveStopsToPreferences(freshStops)
                    }
                } catch (e: Exception) {
                    Log.e("RouteViewModel", "Error al consultar paradas remotas: ${e.message}")
                }
            }
        }
    }

    private fun saveActiveRouteToPreferences(route: RemoteRoute?) {
        val context = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext ?: return
        val prefs = context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE)
        if (route != null) {
            prefs.edit()
                .putString("active_route_id", route.id)
                .putString("active_route_title", route.title)
                .putString("active_route_desc", route.description.orEmpty())
                .putInt("active_route_est", route.estimatedMinutes ?: 45)
                .apply()
        } else {
            prefs.edit().clear().apply()
        }
    }

    private fun saveStopsToPreferences(stops: List<RemoteRouteStop>) {
        val context = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext ?: return
        val prefs = context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE)
        val array = org.json.JSONArray()
        stops.forEach { stop ->
            val obj = org.json.JSONObject()
            obj.put("id", stop.id)
            obj.put("site_id", stop.siteId.orEmpty())
            obj.put("geo_drop_id", stop.geoDropId.orEmpty())
            obj.put("stop_order", stop.stopOrder)
            obj.put("instruction", stop.instruction.orEmpty())
            obj.put("site_name", stop.siteName.orEmpty())
            stop.latitude?.let { obj.put("lat", it) }
            stop.longitude?.let { obj.put("lng", it) }
            array.put(obj)
        }
        prefs.edit().putString("active_stops_json", array.toString()).apply()
    }

    fun saveCompletedStopsToPreferences() {
        val context = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext ?: return
        val prefs = context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE)
        val completedIds = completedStops.filterValues { it }.keys.toSet()
        prefs.edit().putStringSet("completed_stops_set", completedIds).apply()
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
     * Carga las rutas turísticas cercanas a la ubicación actual del usuario (por defecto radio 50km).
     *
     * @param lat Latitud GPS de referencia.
     * @param lng Longitud GPS de referencia.
     * @param radiusM Radio de búsqueda en metros.
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
     * Inicia la navegación de una ruta turística, consulta sus paradas y las sincroniza con el reloj Wear OS.
     *
     * @param context Contexto de la aplicación.
     * @param route Ruta seleccionada por el usuario.
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
                saveActiveRouteToPreferences(route)
                saveStopsToPreferences(stops)
                saveCompletedStopsToPreferences()

                val context = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext
                if (context != null) {
                    wearMessageClient = wearMessageClient ?: WearMessageClient(context)
                }

                // Sincronizar paradas con Wear OS (id|nombre|lat|lng)
                val waypoints = stops.mapNotNull { stop ->
                    val lat = stop.latitude
                    val lng = stop.longitude
                    val name = stop.siteName ?: "Parada ${stop.stopOrder}"
                    val id = stop.effectiveSiteId.ifEmpty { stop.id }
                    if (lat != null && lng != null) "${id}|${name}" to (lat to lng) else null
                }

                if (waypoints.isNotEmpty()) {
                    wearMessageClient?.syncRoute(route.title, waypoints)
                    Log.d("RouteViewModel", "Ruta '${route.title}' sincronizada con Wear OS (${waypoints.size} paradas)")
                }

                // Emitir notificación del sistema y Wear OS alert de Inicio de Ruta
                if (context != null) {
                    mx.utng.ecoguiawear.data.ProximityNotificationHelper.createChannels(context)
                    val notif = mx.utng.ecoguiawear.data.ProximityNotificationHelper.buildRouteActiveNotification(
                        context, route.title, stops.size
                    )
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                    manager?.notify(route.id.hashCode(), notif)

                    wearMessageClient?.sendAlert(
                        id = route.id,
                        message = "Ruta Activa: ${route.title}",
                        type = "ROUTE"
                    )
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
     *
     * @param location Ubicación GPS actual del usuario.
     */
    fun updateProgressWithLocation(location: Location) {
        val stops = _activeStops.value
        val firstUnvisited = stops.firstOrNull { completedStops[it.id] != true } ?: return
        val lat = firstUnvisited.latitude ?: return
        val lng = firstUnvisited.longitude ?: return

        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, lat, lng, results)
        val distance = results[0].toDouble()

        // Reducido a 20 metros para exigir estar realmente dentro del área del sitio
        if (distance <= 10.0) {
            completedStops[firstUnvisited.id] = true
            saveCompletedStopsToPreferences()
            Log.d("RouteViewModel", "Parada secuencial completada: ${firstUnvisited.siteName} (${distance.toInt()}m)")
            notifyWearProgress(completedStopTitle = firstUnvisited.siteName ?: "Parada")
        }
    }

    /**
     * Marca manualmente una parada como completada o no completada (toggle).
     * En caso de marcar completada, exige que todas las paradas anteriores estén completadas.
     * En caso de desmarcar, desmarca también todas las paradas posteriores para mantener orden estricto.
     *
     * @param stopId Identificador de la parada.
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
                saveCompletedStopsToPreferences()
                val stopTitle = stops[targetIndex].siteName ?: "Parada ${targetIndex + 1}"
                notifyWearProgress(completedStopTitle = stopTitle)
            } else {
                Log.w("RouteViewModel", "No se puede completar la parada $stopId antes de las anteriores.")
            }
        } else {
            // Al desmarcar, desmarcamos esta parada y todas las posteriores
            for (idx in targetIndex until stops.size) {
                completedStops[stops[idx].id] = false
            }
            saveCompletedStopsToPreferences()
            notifyWearProgress()
        }
    }

    private fun notifyWearProgress(completedStopTitle: String? = null) {
        val completed = completedStops.values.count { it }
        val total = _activeStops.value.size
        viewModelScope.launch {
            try {
                val context = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext
                if (context != null) {
                    wearMessageClient = wearMessageClient ?: WearMessageClient(context)
                }

                wearMessageClient?.syncRouteProgress(completed, total)

                // Emitir notificación si se completó una parada
                if (context != null && !completedStopTitle.isNullOrBlank()) {
                    mx.utng.ecoguiawear.data.ProximityNotificationHelper.createChannels(context)

                    // 1. Broadcast In-App (Snackbar)
                    val localIntent = android.content.Intent("mx.utng.ecoguiawear.PROXIMITY_ALERT").apply {
                        putExtra("siteName", completedStopTitle)
                        putExtra("distance", 0)
                    }
                    context.sendBroadcast(localIntent)

                    // 2. Notificación del Sistema Android
                    val notif = mx.utng.ecoguiawear.data.ProximityNotificationHelper.buildSiteCompletedNotification(
                        context, completedStopTitle, completed, total
                    )
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                    manager?.notify(completedStopTitle.hashCode(), notif)

                    // 3. Alerta a Wear OS
                    wearMessageClient?.sendAlert(
                        id = completedStopTitle,
                        message = "Sitio completado: $completedStopTitle ($completed/$total)",
                        type = "ROUTE"
                    )
                }
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
        saveActiveRouteToPreferences(null)
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
        saveActiveRouteToPreferences(null)
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
     * Publica una nueva ruta turística con paradas seleccionadas en Neon PostgreSQL.
     *
     * @param title Título descriptivo de la ruta.
     * @param description Resumen del recorrido temático.
     * @param estimatedMinutes Duración estimada en minutos.
     * @param selectedSiteIds Lista ordenada de identificadores de sitios turísticos.
     */
    fun createRoute(
        title: String,
        description: String,
        estimatedMinutes: Int,
        selectedSiteIds: List<String>,
        ownerUserId: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _createSuccess.value = null
            try {
                val success = repository.createRoute(title, description, estimatedMinutes, selectedSiteIds, ownerUserId)
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

    /**
     * Resetea el indicador de éxito de creación de ruta.
     */
    fun resetCreateState() {
        _createSuccess.value = null
    }
}
