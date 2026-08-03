/**
 * Archivo: RadarRouteSyncExt.kt
 * Descripción: Extensión para la sincronización de rutas activas, waypoints y cálculo GPS del reloj.
 */

package mx.utng.ecoguiawear.data.repository.extensions

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.haptics.HapticPulse
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.domain.model.*

internal fun RadarRepositoryImpl.setSyncTargetExt(id: String, name: String, lat: Double, lng: Double) {
    _radarState.update {
        it.copy(
            mode = RadarMode.FOLLOWING_ARROW,
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
    recalculateRadarExt()
}

internal fun RadarRepositoryImpl.setSyncRouteExt(title: String, waypoints: List<Waypoint>) {
    android.util.Log.d("RadarRepo", "Cargando ruta: $title con ${waypoints.size} puntos.")
    _radarState.update {
        it.copy(
            mode = RadarMode.FOLLOWING_ARROW,
            routeSummary = RouteSummary(
                title = title,
                visitedStops = waypoints.count { wp -> wp.isReached },
                totalStops = waypoints.size,
                nextStop = waypoints.firstOrNull { wp -> !wp.isReached }?.title ?: "Fin de ruta",
                estimatedMinutes = waypoints.count { wp -> !wp.isReached } * 5,
                waypoints = waypoints
            ),
            lastAlert = "Ruta activa: $title"
        )
    }
    updateTargetFromRouteExt()
}

internal fun RadarRepositoryImpl.clearActiveRouteExt() {
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
                nextStop = "Detección de sitios activa",
                estimatedMinutes = 0,
                waypoints = emptyList()
            ),
            isRouteCompleted = false,
            lastAlert = "Ruta finalizada"
        )
    }
    if (currentLat != 0.0) {
        performAutoSearchExt(currentLat, currentLng)
    }
}

internal fun RadarRepositoryImpl.setRouteProgressExt(visited: Int, total: Int) {
    _radarState.update { state ->
        val updatedWaypoints = state.routeSummary.waypoints.mapIndexed { index, wp ->
            wp.copy(isReached = index < visited)
        }
        val nextWaypoint = updatedWaypoints.getOrNull(visited) ?: updatedWaypoints.lastOrNull()
        val updatedTarget = if (nextWaypoint != null) {
            RadarTarget(
                id = nextWaypoint.id,
                title = nextWaypoint.title,
                subtitle = "Parada ${visited + 1} de $total",
                type = TargetType.HISTORIC_SITE,
                distanceMeters = state.target.distanceMeters,
                bearingDegrees = state.target.bearingDegrees,
                latitude = nextWaypoint.latitude,
                longitude = nextWaypoint.longitude,
                isAutoTarget = false
            )
        } else {
            state.target
        }

        state.copy(
            mode = if (visited >= total && total > 0) RadarMode.ARRIVED else RadarMode.FOLLOWING_ARROW,
            target = updatedTarget,
            routeSummary = state.routeSummary.copy(
                visitedStops = visited,
                totalStops = total,
                nextStop = nextWaypoint?.title ?: "Fin de ruta",
                waypoints = updatedWaypoints
            ),
            lastAlert = if (visited >= total && total > 0) "🎉 Ruta completada" else "Avanzando a parada ${visited + 1} de $total"
        )
    }
    recalculateRadarExt()
}

internal fun RadarRepositoryImpl.simulateApproachExt() {
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

internal fun RadarRepositoryImpl.completeArrivalExt() {
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

internal fun RadarRepositoryImpl.updateCurrentLocationExt(lat: Double, lng: Double) {
    currentLat = lat
    currentLng = lng
    val currentState = _radarState.value
    android.util.Log.d("RadarRepo", "GPS: $lat, $lng. Objetivo: ${currentState.target.title}")
    
    if ((currentState.target.id == "none" || currentState.target.isAutoTarget) && 
        currentState.routeSummary.waypoints.isEmpty()) {
        val now = System.currentTimeMillis()
        if (now - lastAutoSearchTime > AUTO_SEARCH_INTERVAL_MS) {
            lastAutoSearchTime = now
            performAutoSearchExt(lat, lng)
        }
    }
    
    recalculateRadarExt()
}

internal fun RadarRepositoryImpl.updateTargetFromRouteExt() {
    _radarState.update { state ->
        val waypoints = state.routeSummary.waypoints
        val nextWaypoint = waypoints.firstOrNull { !it.isReached }
        
        if (nextWaypoint != null) {
            android.util.Log.d("RadarRepo", "Actualizando objetivo a: ${nextWaypoint.title} (${nextWaypoint.latitude}, ${nextWaypoint.longitude})")
            state.copy(
                mode = RadarMode.FOLLOWING_ARROW,
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
            state.copy(
                mode = RadarMode.ARRIVED,
                isRouteCompleted = true,
                lastAlert = "🎉 Ruta completada"
            )
        }
    }
    recalculateRadarExt()
}

internal fun RadarRepositoryImpl.recalculateRadarExt() {
    _radarState.update { state ->
        val target = state.target
        val targetLat = target.latitude
        val targetLng = target.longitude

        if (targetLat == null || targetLng == null || currentLat == 0.0) {
            return@update state
        }

        val bearing = mx.utng.ecoguiawear.data.wear.LocationHelper.calculateBearing(
            currentLat, currentLng, targetLat, targetLng
        )
        val distance = mx.utng.ecoguiawear.data.wear.LocationHelper.calculateDistance(
            currentLat, currentLng, targetLat, targetLng
        ).toInt()

        val previousMode = state.mode
        val isRouteActive = state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted
        val nextMode = when {
            target.isAutoTarget -> RadarMode.SCANNING
            distance <= 50 -> RadarMode.ARRIVED
            isRouteActive -> RadarMode.FOLLOWING_ARROW
            else -> RadarMode.SCANNING
        }

        if (previousMode != RadarMode.ARRIVED && nextMode == RadarMode.ARRIVED && !target.isAutoTarget) {
            val currentStrength = if (state.isStealthMode) HapticStrength.LOW else state.hapticSettings.strength
            hapticController.pulse(HapticPulse.ARRIVED, currentStrength)
            val now = System.currentTimeMillis()
            scope.launch {
                try {
                    dao.insertAlert(
                        mx.utng.ecoguia.shared.domain.model.AlertEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            message = "¡Llegaste a ${target.title}!",
                            type = "SITE",
                            timestamp = now
                        )
                    )
                } catch (_: Exception) {}
            }
        }

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
                target.isAutoTarget && distance <= 50 -> "Sitio cercano (${distance}m) · Toca para detalles"
                nextMode == RadarMode.ARRIVED -> "¡Llegaste a ${target.title}!"
                nextMode == RadarMode.FOLLOWING_ARROW -> "Sigue la flecha a ${target.title}"
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
                    setSyncRouteExt(state.routeSummary.title, updatedWaypoints)
                }
            }
        }

        nextState
    }
}
