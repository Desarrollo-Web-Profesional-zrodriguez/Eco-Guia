/**
 * Extensiones del repositorio para el auto-descubrimiento de sitios turísticos y monumentos en Neon DB.
 *
 * Realiza búsquedas georreferenciadas en un radio de hasta 50 km alrededor de las coordenadas GPS actuales,
 * seleccionando el top 3 de sitios más cercanos y administrando la alternancia rápida de objetivos.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.repository.extensions

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.domain.model.RadarTarget
import mx.utng.ecoguiawear.domain.model.TargetType

/**
 * Fuerza la actualización asíncrona de sitios históricos cercanos consumiendo el backend Neon PostgreSQL.
 */
internal fun RadarRepositoryImpl.refreshNearbyTargetsExt() {
    scope.launch {
        try {
            _radarState.update { it.copy(lastAlert = "Buscando sitios históricos...") }
            val radius = mx.utng.ecoguia.shared.config.EcoGuiaConfig.SEARCH_RADIUS_METERS
            val nearbySites = remoteRepository.getNearbySites(currentLat, currentLng, radius)
            if (nearbySites.isNotEmpty()) {
                val top3 = nearbySites.mapNotNull { site ->
                    val siteLat = site.getComputedLatitude() ?: return@mapNotNull null
                    val siteLng = site.getComputedLongitude() ?: return@mapNotNull null
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(currentLat, currentLng, siteLat, siteLng, results)
                    val dist = results[0].toInt()
                    RadarTarget(
                        id = site.id,
                        title = site.name,
                        subtitle = "Sitio histórico (50km)",
                        type = TargetType.HISTORIC_SITE,
                        distanceMeters = dist,
                        bearingDegrees = 0f,
                        latitude = siteLat,
                        longitude = siteLng,
                        isAutoTarget = true
                    ) to dist
                }.sortedBy { it.second }.take(3).map { it.first }

                val first = top3.firstOrNull() ?: return@launch
                _radarState.update { state ->
                    val isRouteActive = state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted
                    if (isRouteActive) {
                        state.copy(nearbyAutoTargets = top3)
                    } else {
                        state.copy(
                            nearbyAutoTargets = top3,
                            selectedAutoIndex = 0,
                            target = first,
                            lastAlert = "Sitios cercanos: ${top3.size}"
                        )
                    }
                }
            } else {
                _radarState.update { it.copy(lastAlert = if (it.routeSummary.waypoints.isNotEmpty()) it.lastAlert else "Sin sitios cercanos") }
            }
        } catch (e: Exception) {
            _radarState.update { it.copy(lastAlert = "Error Neon: ${e.message}") }
        }
    }
}

/**
 * Ejecuta la búsqueda automática periódica de puntos de interés y gestiona la emisión háptica de proximidad.
 *
 * @param lat Latitud actual del usuario.
 * @param lng Longitud actual del usuario.
 */
internal fun RadarRepositoryImpl.performAutoSearchExt(lat: Double, lng: Double) {
    scope.launch {
        try {
            val radius = mx.utng.ecoguia.shared.config.EcoGuiaConfig.SEARCH_RADIUS_METERS
            android.util.Log.d("RadarRepo", "Iniciando búsqueda automática de 3 sitios cercanos (${radius / 1000}km)...")
            val nearbySites = remoteRepository.getNearbySites(lat, lng, radius)
            if (nearbySites.isNotEmpty()) {
                val top3 = nearbySites.mapNotNull { site ->
                    val siteLat = site.getComputedLatitude() ?: return@mapNotNull null
                    val siteLng = site.getComputedLongitude() ?: return@mapNotNull null
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(lat, lng, siteLat, siteLng, results)
                    val dist = results[0].toInt()
                    RadarTarget(
                        id = site.id,
                        title = site.name,
                        subtitle = "Detección automática (${radius / 1000}km)",
                        type = TargetType.HISTORIC_SITE,
                        distanceMeters = dist,
                        bearingDegrees = 0f,
                        latitude = siteLat,
                        longitude = siteLng,
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
                            message = "Cerca de ${activeTarget.title}",
                            type = "SITE",
                            timestamp = now
                        )
                    )
                }

                _radarState.update { state ->
                    val isRouteActive = state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted
                    if (isRouteActive) {
                        return@update state.copy(nearbyAutoTargets = top3)
                    }
                    val existingIndex = top3.indexOfFirst { it.id == state.target.id }
                    val selectedIdx = if (existingIndex != -1) existingIndex else 0
                    state.copy(
                        nearbyAutoTargets = top3,
                        selectedAutoIndex = selectedIdx,
                        target = top3[selectedIdx],
                        lastAlert = if (shouldNotifyHaptics) "Nuevo sitio en radio: ${top3[selectedIdx].title}" else state.lastAlert
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RadarRepo", "Error en búsqueda automática: ${e.message}")
        }
    }
}

/**
 * Selecciona el siguiente elemento disponible en la lista de auto-descubrimiento y recalcula el radar.
 */
internal fun RadarRepositoryImpl.selectNextAutoTargetExt() {
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
    recalculateRadarExt()
}

/**
 * Selecciona el elemento anterior en la lista de auto-descubrimiento y recalcula el radar.
 */
internal fun RadarRepositoryImpl.selectPreviousAutoTargetExt() {
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
    recalculateRadarExt()
}
