/**
 * Archivo: RadarAlertsExt.kt
 * Descripción: Extensión para la gestión y purga de alertas en Room DB para el reloj.
 */

package mx.utng.ecoguiawear.data.repository.extensions

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.domain.model.AlertEntity

internal fun RadarRepositoryImpl.initStealthModeListener() {
    scope.launch {
        dao.getConfigFlow("stealth_mode").collect { config ->
            val isStealth = config?.value == "1"
            _radarState.update { it.copy(isStealthMode = isStealth) }
        }
    }
}

internal fun RadarRepositoryImpl.initAlertsListener() {
    scope.launch {
        val threeHoursAgo = System.currentTimeMillis() - (3 * 3600 * 1000L)
        try {
            dao.deleteOldAlerts(threeHoursAgo)
        } catch (_: Exception) {}

        dao.getAllAlerts().collect { alerts ->
            val now = System.currentTimeMillis()
            val validAlerts = alerts
                .filter { now - it.timestamp <= 3 * 3600 * 1000L }
                .map { 
                    AlertEntity(
                        id = it.id,
                        message = it.message,
                        type = it.type,
                        timestamp = it.timestamp
                    )
                }
            _radarState.update { it.copy(alerts = validAlerts) }
        }
    }
}

internal fun RadarRepositoryImpl.saveAlertsExt(alerts: List<AlertEntity>) {
    scope.launch {
        alerts.forEach { 
            dao.insertAlert(mx.utng.ecoguia.shared.domain.model.AlertEntity(it.id, it.message, it.type, it.timestamp))
        }
    }
}

internal fun RadarRepositoryImpl.deleteAlertExt(id: String) {
    scope.launch {
        try {
            dao.deleteAlert(id)
        } catch (e: Exception) {
            android.util.Log.e("RadarRepo", "Error borrando alerta $id: ${e.message}")
        }
    }
    _radarState.update { state ->
        state.copy(alerts = state.alerts.filterNot { it.id == id })
    }
}

internal fun RadarRepositoryImpl.clearAllAlertsExt() {
    scope.launch {
        try {
            dao.clearAllAlerts()
        } catch (e: Exception) {
            android.util.Log.e("RadarRepo", "Error limpiando todas las alertas: ${e.message}")
        }
    }
    _radarState.update { state ->
        state.copy(alerts = emptyList())
    }
}
