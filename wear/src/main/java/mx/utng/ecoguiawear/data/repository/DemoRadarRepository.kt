package mx.utng.ecoguiawear.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.model.RadarMode
import mx.utng.ecoguiawear.domain.model.RadarTarget
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.domain.model.TargetType
import mx.utng.ecoguiawear.domain.repository.RadarRepository

class DemoRadarRepository : RadarRepository {

    private val initialState = RadarUiState()
    private val _radarState = MutableStateFlow(initialState)
    override val radarState: StateFlow<RadarUiState> = _radarState.asStateFlow()

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
                RadarMode.ARRIVED -> RadarMode.SCANNING
            }
            it.copy(
                mode = nextMode,
                lastAlert = if (nextMode == RadarMode.SCANNING) "Radar activo" else "Radar pausado"
            )
        }
    }

    override fun toggleStealthMode() {
        _radarState.update {
            it.copy(isStealthMode = !it.isStealthMode)
        }
    }

    override fun setStealthMode(enabled: Boolean) {
        _radarState.update { it.copy(isStealthMode = enabled) }
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
                state.target.distanceMeters > 45 -> 18
                state.target.distanceMeters > 20 -> 9
                else -> 5
            }
            val nextDistance = (state.target.distanceMeters - step).coerceAtLeast(0)
            val nextTarget = nextTargetForDistance(state.target, nextDistance)
            val nextMode = if (nextDistance == 0) RadarMode.ARRIVED else RadarMode.SCANNING
            val nextAlert = when {
                nextDistance == 0 -> "Llegaste al punto"
                nextDistance <= 20 -> "Geo-Drop cercano"
                else -> "Sigue la flecha"
            }

            state.copy(
                mode = nextMode,
                target = nextTarget,
                routeSummary = if (nextDistance == 0) {
                    state.routeSummary.copy(visitedStops = 2, nextStop = "Parroquia de Dolores")
                } else {
                    state.routeSummary
                },
                lastAlert = nextAlert
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
