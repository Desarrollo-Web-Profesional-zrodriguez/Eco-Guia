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

    init {
        // Cargar estado inicial desde DB
        scope.launch {
            dao.getConfigFlow("stealth_mode").collect { config ->
                val isStealth = config?.value == "1"
                _radarState.update { it.copy(isStealthMode = isStealth) }
            }
        }
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
                lastAlert = if (linked) "Telefono vinculado" else "Sin telefono"
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
            state.copy(routeSummary = state.routeSummary.copy(visitedStops = visited, totalStops = total))
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
            state.copy(
                mode = RadarMode.SCANNING,
                target = state.target.copy(distanceMeters = 80, title = "Siguiente Punto"),
                routeSummary = state.routeSummary.copy(visitedStops = nextVisited),
                lastAlert = "Continuando ruta..."
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
                lastAlert = if (enabled) "Vibracion activa" else "Vibracion apagada"
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
                    longitude = lng
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

    override fun updateCurrentLocation(lat: Double, lng: Double) {
        currentLat = lat
        currentLng = lng
        val targetName = _radarState.value.target.title
        android.util.Log.d("RadarRepo", "Ubicación: $lat, $lng. Objetivo actual: $targetName")
        recalculateRadar()
    }

    private fun recalculateRadar() {
        _radarState.update { state ->
            val target = state.target
            val targetLat = target.latitude
            val targetLng = target.longitude

            if (targetLat == null || targetLng == null) {
                android.util.Log.w("RadarRepo", "No hay coordenadas en el objetivo actual (${target.title}).")
                return@update state
            }

            if (currentLat == 0.0) {
                android.util.Log.w("RadarRepo", "Sin ubicación GPS aún para calcular rumbo.")
                return@update state
            }

            val bearing = mx.utng.ecoguiawear.data.wear.LocationHelper.calculateBearing(
                currentLat, currentLng, targetLat, targetLng
            )
            val distance = mx.utng.ecoguiawear.data.wear.LocationHelper.calculateDistance(
                currentLat, currentLng, targetLat, targetLng
            ).toInt()

            android.util.Log.d("RadarRepo", "Recalculando para ${target.title}: Distancia=$distance m, Rumbo=$bearing")

            val nextMode = when {
                distance <= 5 -> RadarMode.ARRIVED
                distance <= 20 -> RadarMode.FOLLOWING_ARROW
                else -> RadarMode.SCANNING
            }

            // Auto-progresión de ruta si llegamos
            var shouldUpdateRoute = false
            if (nextMode == RadarMode.ARRIVED && state.routeSummary.waypoints.isNotEmpty()) {
                shouldUpdateRoute = true
            }

            val nextState = state.copy(
                mode = nextMode,
                target = target.copy(
                    distanceMeters = distance,
                    bearingDegrees = bearing
                ),
                lastAlert = when(nextMode) {
                    RadarMode.ARRIVED -> "¡Llegaste!"
                    RadarMode.FOLLOWING_ARROW -> "Sigue la flecha"
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
                    } else {
                        // Fin de la ruta
                        _radarState.update { it.copy(lastAlert = "Ruta completada 🏁") }
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
                        longitude = nextWaypoint.longitude
                    ),
                    routeSummary = state.routeSummary.copy(
                        nextStop = nextWaypoint.title,
                        visitedStops = waypoints.count { wp -> wp.isReached }
                    )
                )
            } else {
                android.util.Log.w("RadarRepo", "No hay más waypoints pendientes en la ruta.")
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
                subtitle = "Abre el celular para ver la capsula",
                type = TargetType.HISTORIC_SITE,
                bearingDegrees = 0f
            )
        } else {
            current.copy(
                distanceMeters = distance,
                bearingDegrees = (current.bearingDegrees + 18f) % 360f
            )
        }
    }
}
