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
import mx.utng.ecoguia.shared.domain.model.ConfigEntity
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.model.RadarMode
import mx.utng.ecoguiawear.domain.model.RadarTarget
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.domain.model.TargetType
import mx.utng.ecoguiawear.domain.repository.RadarRepository

class DemoRadarRepository(context: Context) : RadarRepository {

    private val db = EcoGuiaDatabase.getDatabase(context)
    private val dao = db.dao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val initialState = RadarUiState()
    private val _radarState = MutableStateFlow(initialState)
    override val radarState: StateFlow<RadarUiState> = _radarState.asStateFlow()

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
        // El Flow de Room actualizará el estado automáticamente
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
