/**
 * Archivo: RadarRepositoryImpl.kt
 * Autor: EcoGuia Team
 * Fecha de última actualización: 2026-08-02
 * Descripción: Repositorio principal de datos para la aplicación Wear OS.
 * Implementa RadarRepository delegando responsabilidades complejas en extensiones modulares.
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
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.repository.extensions.*
import mx.utng.ecoguiawear.domain.model.*
import mx.utng.ecoguiawear.domain.repository.RadarRepository

class RadarRepositoryImpl(context: Context) : RadarRepository {

    internal val db = EcoGuiaDatabase.getDatabase(context)
    internal val dao = db.dao()
    internal val remoteRepository = EcoGuiaRepositoryImpl()
    internal val hapticController = HapticController(context)
    internal val scope = CoroutineScope(Dispatchers.IO)

    internal val _radarState = MutableStateFlow(RadarUiState())
    override val radarState: StateFlow<RadarUiState> = _radarState.asStateFlow()

    internal var currentLat: Double = 0.0
    internal var currentLng: Double = 0.0
    internal var lastAutoSearchTime: Long = 0
    internal val AUTO_SEARCH_INTERVAL_MS = 30000L

    internal val lastAlertPerSiteTime = mutableMapOf<String, Long>()
    internal val THIRTY_MINUTES_MS = 30 * 60 * 1000L

    private var smoothedHeading: Float = 0f

    init {
        initStealthModeListener()
        initAlertsListener()
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
        _radarState.update { state ->
            val isRouteActive = state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted
            val nextMode = if (isRouteActive) RadarMode.FOLLOWING_ARROW else RadarMode.SCANNING
            state.copy(
                isLinkedToPhone = true,
                mode = nextMode,
                lastAlert = if (isRouteActive) "Ruta activa" else "Radar activo"
            )
        }
    }

    override fun toggleRadar() {
        _radarState.update { state ->
            val isRouteActive = state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted
            val activeMode = if (isRouteActive) RadarMode.FOLLOWING_ARROW else RadarMode.SCANNING
            val nextMode = when (state.mode) {
                RadarMode.PAUSED -> activeMode
                RadarMode.SCANNING -> RadarMode.PAUSED
                RadarMode.FOLLOWING_ARROW -> RadarMode.PAUSED
                RadarMode.ARRIVED -> activeMode
            }
            state.copy(
                mode = nextMode,
                lastAlert = if (nextMode == RadarMode.PAUSED) "Radar pausado" else "Radar activo"
            )
        }
    }

    override fun toggleStealthMode() {
        setStealthMode(!_radarState.value.isStealthMode)
    }

    override fun setStealthMode(enabled: Boolean) {
        scope.launch {
            dao.saveConfig(ConfigEntity("stealth_mode", if (enabled) "1" else "0"))
        }
        _radarState.update { it.copy(isStealthMode = enabled) }
    }

    override fun setAlerts(alerts: List<AlertEntity>) {
        saveAlertsExt(alerts)
    }

    override fun deleteAlert(id: String) {
        deleteAlertExt(id)
    }

    override fun clearAllAlerts() {
        clearAllAlertsExt()
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
        setRouteProgressExt(visited, total)
    }

    override fun simulateApproach() {
        simulateApproachExt()
    }

    override fun completeArrival() {
        completeArrivalExt()
    }

    override fun resetDemo() {
        _radarState.value = RadarUiState(isLinkedToPhone = true, mode = RadarMode.SCANNING)
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
        refreshNearbyTargetsExt()
    }

    override fun setSyncTarget(id: String, name: String, lat: Double, lng: Double) {
        setSyncTargetExt(id, name, lat, lng)
    }

    override fun setSyncRoute(title: String, waypoints: List<Waypoint>) {
        setSyncRouteExt(title, waypoints)
    }

    override fun clearActiveRoute() {
        clearActiveRouteExt()
    }

    override fun markRouteCompleted() {
        _radarState.update {
            it.copy(
                mode = RadarMode.SCANNING,
                isRouteCompleted = true,
                lastAlert = "Ruta Completada"
            )
        }
    }

    override fun dismissRouteCompleted() {
        _radarState.update { it.copy(isRouteCompleted = false) }
        clearActiveRoute()
    }

    override fun updateCurrentLocation(lat: Double, lng: Double) {
        updateCurrentLocationExt(lat, lng)
    }

    override fun selectNextAutoTarget() {
        selectNextAutoTargetExt()
    }

    override fun selectPreviousAutoTarget() {
        selectPreviousAutoTargetExt()
    }

    override fun updateHeading(heading: Float) {
        val diff = Math.abs(heading - smoothedHeading)
        if (diff > mx.utng.ecoguia.shared.config.EcoGuiaConfig.COMPASS_HEADING_THRESHOLD_DEGREES) {
            val factor = mx.utng.ecoguia.shared.config.EcoGuiaConfig.COMPASS_SMOOTHING_FACTOR
            smoothedHeading = smoothedHeading + factor * (heading - smoothedHeading)
            _radarState.update { it.copy(currentHeading = smoothedHeading) }
        }
    }

    internal fun nextTargetForDistance(current: RadarTarget, distance: Int): RadarTarget {
        return if (distance == 0) {
            current.copy(
                distanceMeters = 0,
                title = "Museo alcanzado",
                subtitle = "Abre el celular para ver detalles del sitio",
                type = TargetType.HISTORIC_SITE,
                bearingDegrees = 0f
            )
        } else {
            current.copy(distanceMeters = distance)
        }
    }
}
