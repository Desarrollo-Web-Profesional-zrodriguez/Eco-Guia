/**
 * Archivo: DemoRadarRepository.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Repositorio principal de datos para la aplicación Wear OS. Gestiona el estado
 * del radar de proximidad, sincronización de rutas desde la base de datos Neon PostgreSQL y móvil,
 * alertas de geofencing pasivas (cada 30 min) y actualización de sensores GPS y compás.
 *
 * Funciones destacadas:
 * - setSyncRoute: Recibe la ruta activa transmitida desde el móvil y actualiza los waypoints.
 * - clearActiveRoute: Restablece el radar al estado libre de detección automática de Geo-Drops.
 * - performAutoSearch: Consulta Neon para detectar sitios históricos a 50km con throttle de 30 min.
 * - updateCurrentLocation: Recalcula distancias geodésicas y rumbo hacia la parada activa.
 */

package mx.utng.ecoguiawear.data.repository

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.ConfigEntity
import mx.utng.ecoguiawear.domain.model.*
import mx.utng.ecoguiawear.domain.repository.RadarRepository

class DemoRadarRepository(context: Context) : RadarRepository {

    private val db = EcoGuiaDatabase.getDatabase(context)
    private val dao = db.dao()
    private val remoteRepository = EcoGuiaRepositoryImpl()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val initialState = RadarUiState()
    private val _radarState = MutableStateFlow(initialState)
    override val radarState: StateFlow<RadarUiState> = _radarState.asStateFlow()

    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0
    private var lastAutoSearchTime: Long = 0
    private val AUTO_SEARCH_INTERVAL_MS = 30000 // 30 segundos

    /** Registro de última alerta sonora/vibración emitida por sitio (throttle de 30 minutos). */
    private val lastAlertPerSiteTime = mutableMapOf<String, Long>()
    private val THIRTY_MINUTES_MS = 30 * 60 * 1000L

    init {
        // Cargar configuración de sigilo desde DB local
        scope.launch {
            dao.getConfigFlow("stealth_mode").collect { config ->
                val isStealth = config?.value == "1"
                _radarState.update { it.copy(isStealthMode = isStealth) }
            }
        }
        // Cargar historial de alertas locales
        scope.launch {
            dao.getAllAlerts().collect { alerts ->
                val domainAlerts = alerts.map { 
                    mx.utng.ecoguiawear.domain.model.AlertEntity(
                        id = it.id,
                        message = it.message,
                        type = it.type,
                        timestamp = it.timestamp
                    )
                }
                _radarState.update { it.copy(alerts = domainAlerts) }
            }
        }
    }

    override fun setLinkedToPhone(linked: Boolean) {
        _radarState.update {
            it.copy(
                isLinkedToPhone = linked,
                lastAlert = if (linked) "Teléfono vinculado" else "Sin teléfono"
            )
        }
    }

    override fun startRadar() {
        _radarState.update {
            it.copy(
                isLinkedToPhone = true,
                mode = RadarMode.SCANNING,
                lastAlert = "Radar activo"
            )
        }
    }

    override fun toggleRadar() {
        _radarState.update {
            val nextMode = when (it.mode) {
                RadarMode.PAUSED -> RadarMode.SCANNING
                RadarMode.SCANNING -> RadarMode.PAUSED
                RadarMode.FOLLOWING_ARROW -> RadarMode.PAUSED
                RadarMode.ARRIVED -> RadarMode.SCANNING
            }
            it.copy(
                mode = nextMode,
                lastAlert = if (nextMode == RadarMode.SCANNING) "Radar activo" else "Radar pausado"
            )
        }
    }

    override fun toggleStealthMode() {
        val nextStealth = !_radarState.value.isStealthMode
        setStealthMode(nextStealth)
    }

    override fun setStealthMode(enabled: Boolean) {
        scope.launch {
            dao.saveConfig(ConfigEntity("stealth_mode", if (enabled) "1" else "0"))
        }
        _radarState.update { it.copy(isStealthMode = enabled) }
    }

    override fun setAlerts(alerts: List<mx.utng.ecoguiawear.domain.model.AlertEntity>) {
        scope.launch {
            alerts.forEach { 
                dao.insertAlert(mx.utng.ecoguia.shared.domain.model.AlertEntity(it.id, it.message, it.type, it.timestamp))
            }
        }
    }

    override fun setPermissions(gps: Boolean, camera: Boolean) {
        _radarState.update { it.copy(isGpsEnabled = gps, isCameraReady = camera) }
    }

    override fun setDistance(distance: Int) {
        _radarState.update { state ->
            val nextTarget = nextTargetForDistance(state.target, distance)
            val nextMode = if (distance == 0) RadarMode.ARRIVED else RadarMode.SCANNING
            state.copy(mode = nextMode, target = nextTarget)
        }
    }

    override fun setRouteProgress(visited: Int, total: Int) {
        _radarState.update { state ->
            val nextWaypoint = state.routeSummary.waypoints.getOrNull(visited)
            val updatedTarget = if (nextWaypoint != null) {
                state.target.copy(
                    id = nextWaypoint.id,
                    title = nextWaypoint.title,
                    subtitle = "Parada ${visited + 1} de $total",
                    distanceMeters = 50,
                    latitude = nextWaypoint.latitude,
                    longitude = nextWaypoint.longitude
                )
            } else {
                state.target
            }

            state.copy(
                target = updatedTarget,
                routeSummary = state.routeSummary.copy(visitedStops = visited, totalStops = total),
                lastAlert = if (visited >= total && total > 0) "🎉 Última parada alcanzada" else "Avanzando a parada ${visited + 1}"
            )
        }
    }

    override fun simulateApproach() {
        _radarState.update { state ->
            if (state.mode == RadarMode.PAUSED) {
                return@update state.copy(lastAlert = "Activa el radar primero")
            }

            val step = when {
                state.target.distanceMeters > 50 -> 25
                state.target.distanceMeters > 20 -> 15
                else -> 10
            }
            val nextDistance = (state.target.distanceMeters - step).coerceAtLeast(0)
            val nextTarget = nextTargetForDistance(state.target, nextDistance)
            
            val nextMode = when {
                nextDistance == 0 -> RadarMode.ARRIVED
                nextDistance <= 20 -> RadarMode.FOLLOWING_ARROW
                else -> RadarMode.SCANNING
            }

            val nextAlert = when (nextMode) {
                RadarMode.ARRIVED -> "¡Llegaste!"
                RadarMode.FOLLOWING_ARROW -> "Sigue la flecha"
                else -> "Escaneando..."
            }

            state.copy(
                mode = nextMode,
                target = nextTarget,
                lastAlert = nextAlert
            )
        }
    }

    override fun completeArrival() {
        _radarState.update { state ->
            val nextVisited = (state.routeSummary.visitedStops + 1).coerceAtMost(state.routeSummary.totalStops)
            val nextWaypoint = state.routeSummary.waypoints.getOrNull(nextVisited)
            val nextTitle = nextWaypoint?.title ?: "Siguiente Punto"

            state.copy(
                mode = RadarMode.SCANNING,
                target = state.target.copy(
                    id = nextWaypoint?.id ?: state.target.id,
                    title = nextTitle,
                    distanceMeters = if (nextWaypoint != null) 30 else 0,
                    latitude = nextWaypoint?.latitude,
                    longitude = nextWaypoint?.longitude
                ),
                routeSummary = state.routeSummary.copy(visitedStops = nextVisited),
                lastAlert = "Parada registrada"
            )
        }
    }

    override fun resetDemo() {
        _radarState.value = initialState.copy(isLinkedToPhone = true, mode = RadarMode.SCANNING)
    }

    override fun updateHaptics(enabled: Boolean, strength: HapticStrength) {
        _radarState.update {
            it.copy(
                hapticSettings = it.hapticSettings.copy(enabled = enabled, strength = strength),
                lastAlert = if (enabled) "Vibración activa" else "Vibración apagada"
            )
        }
    }

    override fun refreshNearbyTargets() {
        scope.launch {
            try {
                _radarState.update { it.copy(lastAlert = "Buscando en Neon...") }
                val remoteDrops = remoteRepository.getGeoDrops()
                if (remoteDrops.isNotEmpty()) {
                    val firstDrop = remoteDrops.first()
                    val target = RadarTarget(
                        id = firstDrop.id ?: "0",
                        title = firstDrop.title,
                        subtitle = firstDrop.description ?: "Cápsula en la nube",
                        type = TargetType.GEO_DROP,
                        distanceMeters = 45,
                        bearingDegrees = 120f
                    )
                    _radarState.update { it.copy(target = target, lastAlert = "Cápsula encontrada") }
                } else {
                    _radarState.update { it.copy(lastAlert = "No hay cápsulas") }
                }
            } catch (e: Exception) {
                _radarState.update { it.copy(lastAlert = "Error Neon: ${e.message}") }
            }
        }
    }

    override fun setSyncTarget(id: String, name: String, lat: Double, lng: Double) {
        _radarState.update {
            it.copy(
                mode = RadarMode.SCANNING,
                target = RadarTarget(
                    id = id,
                    title = name,
                    subtitle = "Sincronizado desde móvil",
                    type = TargetType.HISTORIC_SITE,
                    distanceMeters = 50,
                    bearingDegrees = 0f,
                    latitude = lat,
                    longitude = lng,
                    isAutoTarget = false
                ),
                lastAlert = "Nuevo objetivo: $name"
            )
        }
        recalculateRadar()
    }

    override fun setSyncRoute(title: String, waypoints: List<Waypoint>) {
        android.util.Log.d("RadarRepo", "Cargando ruta: $title con ${waypoints.size} puntos.")
        _radarState.update {
            it.copy(
                mode = RadarMode.SCANNING,
                routeSummary = RouteSummary(
                    title = title,
                    visitedStops = waypoints.count { wp -> wp.isReached },
                    totalStops = waypoints.size,
                    nextStop = waypoints.firstOrNull { wp -> !wp.isReached }?.title ?: "Fin",
                    estimatedMinutes = waypoints.count { wp -> !wp.isReached } * 5,
                    waypoints = waypoints
                ),
                lastAlert = "Ruta cargada: $title"
            )
        }
        updateTargetFromRoute()
    }

    override fun clearActiveRoute() {
        android.util.Log.d("RadarRepo", "Cancelando ruta activa en el reloj.")
        _radarState.update {
            it.copy(
                mode = RadarMode.SCANNING,
                target = RadarTarget(
                    id = "none",
                    title = "Esperando objetivo",
                    subtitle = "Detección automática (50km)",
                    type = TargetType.HISTORIC_SITE,
                    distanceMeters = 0,
                    bearingDegrees = 0f,
                    isAutoTarget = true
                ),
                routeSummary = RouteSummary(
                    title = "Sin ruta activa",
                    visitedStops = 0,
                    totalStops = 0,
                    nextStop = "Detección de Geo-Drops activa",
                    estimatedMinutes = 0,
                    waypoints = emptyList()
                ),
                isRouteCompleted = false,
                lastAlert = "Ruta finalizada"
            )
        }
        if (currentLat != 0.0) {
            performAutoSearch(currentLat, currentLng)
        }
    }

    override fun markRouteCompleted() {
        _radarState.update {
            it.copy(
                mode = RadarMode.SCANNING,
                isRouteCompleted = true,
                lastAlert = "🎉 Ruta Completada"
            )
        }
    }

    override fun dismissRouteCompleted() {
        _radarState.update {
            it.copy(
                isRouteCompleted = false
            )
        }
        clearActiveRoute()
    }

    override fun updateCurrentLocation(lat: Double, lng: Double) {
        currentLat = lat
        currentLng = lng
        val currentState = _radarState.value
        android.util.Log.d("RadarRepo", "GPS: $lat, $lng. Objetivo: ${currentState.target.title}")
        
        // Si no hay objetivo manual ni ruta, buscar automáticamente cada 30 segundos
        if ((currentState.target.id == "none" || currentState.target.isAutoTarget) && 
            currentState.routeSummary.waypoints.isEmpty()) {
            val now = System.currentTimeMillis()
            if (now - lastAutoSearchTime > AUTO_SEARCH_INTERVAL_MS) {
                lastAutoSearchTime = now
                performAutoSearch(lat, lng)
            }
        }
        
        recalculateRadar()
    }

    override fun selectNextAutoTarget() {
        _radarState.update { state ->
            val list = state.nearbyAutoTargets
            if (list.isEmpty()) return@update state
            val nextIndex = (state.selectedAutoIndex + 1) % list.size
            val selectedTarget = list[nextIndex]
            state.copy(
                selectedAutoIndex = nextIndex,
                target = selectedTarget,
                lastAlert = "Objetivo ${nextIndex + 1}/${list.size}: ${selectedTarget.title}"
            )
        }
        recalculateRadar()
    }

    override fun selectPreviousAutoTarget() {
        _radarState.update { state ->
            val list = state.nearbyAutoTargets
            if (list.isEmpty()) return@update state
            val prevIndex = if (state.selectedAutoIndex - 1 < 0) list.size - 1 else state.selectedAutoIndex - 1
            val selectedTarget = list[prevIndex]
            state.copy(
                selectedAutoIndex = prevIndex,
                target = selectedTarget,
                lastAlert = "Objetivo ${prevIndex + 1}/${list.size}: ${selectedTarget.title}"
            )
        }
        recalculateRadar()
    }

    private fun performAutoSearch(lat: Double, lng: Double) {
        scope.launch {
            try {
                val radius = mx.utng.ecoguia.shared.config.EcoGuiaConfig.SEARCH_RADIUS_METERS
                android.util.Log.d("RadarRepo", "Iniciando búsqueda automática de 3 sitios cercanos (${radius / 1000}km)...")
                val nearbySites = remoteRepository.getNearbySites(lat, lng, radius)
                if (nearbySites.isNotEmpty()) {
                    val top3 = nearbySites.map { site ->
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(lat, lng, site.latitude ?: 0.0, site.longitude ?: 0.0, results)
                        val dist = results[0].toInt()
                        RadarTarget(
                            id = site.id,
                            title = site.name,
                            subtitle = "Detección automática (${radius / 1000}km)",
                            type = TargetType.HISTORIC_SITE,
                            distanceMeters = dist,
                            bearingDegrees = 0f,
                            latitude = site.latitude,
                            longitude = site.longitude,
                            isAutoTarget = true
                        ) to dist
                    }.sortedBy { it.second }.take(3).map { it.first }

                    val activeTarget = top3.firstOrNull() ?: return@launch

                    val now = System.currentTimeMillis()
                    val lastAlertTime = lastAlertPerSiteTime[activeTarget.id] ?: 0L
                    val shouldNotifyHaptics = (now - lastAlertTime) > THIRTY_MINUTES_MS

                    if (shouldNotifyHaptics) {
                        lastAlertPerSiteTime[activeTarget.id] = now
                        dao.insertAlert(
                            mx.utng.ecoguia.shared.domain.model.AlertEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                message = "📍 Cerca de ${activeTarget.title}",
                                type = "SITE",
                                timestamp = now
                            )
                        )
                    }

                    _radarState.update { state ->
                        // Si el usuario ya está navegando un objetivo automático específico, conservar su índice si sigue en la lista
                        val existingIndex = top3.indexOfFirst { it.id == state.target.id }
                        val selectedIdx = if (existingIndex != -1) existingIndex else 0
                        state.copy(
                            nearbyAutoTargets = top3,
                            selectedAutoIndex = selectedIdx,
                            target = top3[selectedIdx],
                            lastAlert = if (shouldNotifyHaptics) "📍 Nuevo sitio en radio: ${top3[selectedIdx].title}" else state.lastAlert
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("RadarRepo", "Error en búsqueda automática: ${e.message}")
            }
        }
    }

    private var smoothedHeading: Float = 0f

    override fun updateHeading(heading: Float) {
        // Filtro pasa-bajas personalizable para suavizar temblores en la aguja de la brújula
        val diff = Math.abs(heading - smoothedHeading)
        if (diff > mx.utng.ecoguia.shared.config.EcoGuiaConfig.COMPASS_HEADING_THRESHOLD_DEGREES) {
            val factor = mx.utng.ecoguia.shared.config.EcoGuiaConfig.COMPASS_SMOOTHING_FACTOR
            smoothedHeading = smoothedHeading + factor * (heading - smoothedHeading)
            _radarState.update { it.copy(currentHeading = smoothedHeading) }
        }
    }

    private fun recalculateRadar() {
        _radarState.update { state ->
            val target = state.target
            val targetLat = target.latitude
            val targetLng = target.longitude

            if (targetLat == null || targetLng == null) {
                return@update state
            }

            if (currentLat == 0.0) return@update state

            val bearing = mx.utng.ecoguiawear.data.wear.LocationHelper.calculateBearing(
                currentLat, currentLng, targetLat, targetLng
            )
            val distance = mx.utng.ecoguiawear.data.wear.LocationHelper.calculateDistance(
                currentLat, currentLng, targetLat, targetLng
            ).toInt()

            android.util.Log.d("RadarRepo", "Recalculando para ${target.title}: Distancia=$distance m, Rumbo=$bearing")

            val nextMode = when {
                target.isAutoTarget -> RadarMode.SCANNING // En detección automática NO forzamos modo ARRIVED
                distance <= 50 -> RadarMode.ARRIVED // 50m coincide con el umbral del móvil
                else -> RadarMode.SCANNING
            }

            // Auto-progresión de ruta si llegamos en una ruta manual activa
            var shouldUpdateRoute = false
            if (nextMode == RadarMode.ARRIVED && state.routeSummary.waypoints.isNotEmpty() && !target.isAutoTarget) {
                shouldUpdateRoute = true
            }

            val nextState = state.copy(
                mode = nextMode,
                target = target.copy(
                    distanceMeters = distance,
                    bearingDegrees = bearing
                ),
                lastAlert = when {
                    target.isAutoTarget && distance <= 50 -> "📍 Sitio cercano (${distance}m) · Toca para Geo-Drop"
                    nextMode == RadarMode.ARRIVED -> "¡Llegaste a la parada!"
                    nextMode == RadarMode.FOLLOWING_ARROW -> "Sigue la flecha"
                    target.isAutoTarget -> "⊙ Detección automática (50km)"
                    else -> "Caminando..."
                }
            )

            if (shouldUpdateRoute) {
                scope.launch {
                    val updatedWaypoints = state.routeSummary.waypoints.map {
                        if (it.id == target.id) it.copy(isReached = true) else it
                    }
                    if (updatedWaypoints.any { !it.isReached }) {
                        setSyncRoute(state.routeSummary.title, updatedWaypoints)
                    }
                }
            }

            nextState
        }
    }

    private fun updateTargetFromRoute() {
        _radarState.update { state ->
            val waypoints = state.routeSummary.waypoints
            val nextWaypoint = waypoints.firstOrNull { !it.isReached }
            
            if (nextWaypoint != null) {
                android.util.Log.d("RadarRepo", "Actualizando objetivo a: ${nextWaypoint.title} (${nextWaypoint.latitude}, ${nextWaypoint.longitude})")
                state.copy(
                    target = RadarTarget(
                        id = nextWaypoint.id,
                        title = nextWaypoint.title,
                        subtitle = "Punto de ruta",
                        type = TargetType.HISTORIC_SITE,
                        distanceMeters = 0,
                        bearingDegrees = 0f,
                        latitude = nextWaypoint.latitude,
                        longitude = nextWaypoint.longitude,
                        isAutoTarget = false
                    ),
                    routeSummary = state.routeSummary.copy(
                        nextStop = nextWaypoint.title,
                        visitedStops = waypoints.count { wp -> wp.isReached }
                    )
                )
            } else {
                state
            }
        }
        recalculateRadar()
    }

    private fun nextTargetForDistance(current: RadarTarget, distance: Int): RadarTarget {
        return if (distance == 0) {
            current.copy(
                distanceMeters = 0,
                title = "Museo alcanzado",
                subtitle = "Abre el celular para ver la cápsula",
                type = TargetType.HISTORIC_SITE,
                bearingDegrees = 0f
            )
        } else {
            current.copy(distanceMeters = distance)
        }
    }
}
