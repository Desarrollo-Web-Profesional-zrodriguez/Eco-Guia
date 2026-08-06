/**
 * Extensiones del repositorio para la administración, persistencia y depuración de alertas en Room DB.
 *
 * Mantiene la reactividad del modo discreto y asegura que el historial de notificaciones
 * de proximidad en el smartwatch se mantenga optimizado y libre de registros obsoletos (>3 horas).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.repository.extensions

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.domain.model.AlertEntity

/**
 * Inicializa el observador reactivo sobre la clave de configuración del modo discreto en Room DB.
 */
internal fun RadarRepositoryImpl.initStealthModeListener() {
    scope.launch {
        dao.getConfigFlow("stealth_mode").collect { config ->
            val isStealth = config?.value == "1"
            _radarState.update { it.copy(isStealthMode = isStealth) }
        }
    }
}

/**
 * Inicializa la escucha de alertas registradas en base de datos local y purga registros anteriores a 3 horas.
 */
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

/**
 * Guarda una colección de alertas de proximidad en la base de datos Room.
 *
 * @param alerts Lista de entidades [AlertEntity] a persistir.
 */
internal fun RadarRepositoryImpl.saveAlertsExt(alerts: List<AlertEntity>) {
    scope.launch {
        alerts.forEach { 
            dao.insertAlert(mx.utng.ecoguia.shared.domain.model.AlertEntity(it.id, it.message, it.type, it.timestamp))
        }
    }
}

/**
 * Elimina una alerta específica de la base de datos y del estado de la interfaz.
 *
 * @param id Identificador único de la alerta.
 */
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

/**
 * Purga todas las alertas almacenadas en el reloj.
 */
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
