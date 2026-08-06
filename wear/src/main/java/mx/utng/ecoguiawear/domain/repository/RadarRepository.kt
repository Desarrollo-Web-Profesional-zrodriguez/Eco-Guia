/**
 * Contrato de repositorio para la gestión del radar, cálculo de proximidad y sincronización en Wear OS.
 *
 * Expone flujos observables del estado de navegación y operaciones para actualizar coordenadas,
 * rutas turísticas, alertas y retroalimentación háptica.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.domain.repository

import kotlinx.coroutines.flow.StateFlow
import mx.utng.ecoguiawear.domain.model.AlertEntity
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.domain.model.Waypoint

/**
 * Interfaz que define las operaciones del radar de proximidad y sincronización con el teléfono móvil.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
interface RadarRepository {
    /** Flujo inmutable y observable del estado global del radar. */
    val radarState: StateFlow<RadarUiState>

    /**
     * Actualiza el estado de vinculación física o por Bluetooth con el teléfono móvil.
     *
     * @param linked Verdadero si el canal con el teléfono está conectado.
     */
    fun setLinkedToPhone(linked: Boolean)

    /** Inicia el servicio de escaneo de proximidad continuo. */
    fun startRadar()

    /** Alterna entre el estado de escaneo activo y pausa del radar. */
    fun toggleRadar()

    /** Alterna la activación del modo discreto (pantalla apagada, solo háptico). */
    fun toggleStealthMode()

    /**
     * Establece explícitamente el estado del modo discreto.
     *
     * @param enabled Verdadero para activar el modo discreto.
     */
    fun setStealthMode(enabled: Boolean)

    /**
     * Asigna la lista de alertas históricas o en cola para la interfaz del reloj.
     *
     * @param alerts Colección de entidades [AlertEntity].
     */
    fun setAlerts(alerts: List<AlertEntity>)

    /**
     * Notifica el estado de disponibilidad de los sensores requeridos.
     *
     * @param gps Verdadero si el permiso y sensor GPS están disponibles.
     * @param camera Verdadero si los periféricos auxiliares están listos.
     */
    fun setPermissions(gps: Boolean, camera: Boolean)

    /**
     * Ajusta manualmente la distancia calculada hacia el objetivo activo.
     *
     * @param distance Distancia en metros.
     */
    fun setDistance(distance: Int)

    /**
     * Actualiza las estadísticas de progreso en la ruta turística sincronizada.
     *
     * @param visited Número de hitos visitados.
     * @param total Total de hitos de la ruta.
     */
    fun setRouteProgress(visited: Int, total: Int)

    /** Simula una aproximación secuencial paso a paso hacia el objetivo para pruebas. */
    fun simulateApproach()

    /** Restablece los valores del radar a su configuración de demostración inicial. */
    fun resetDemo()

    /** Registra la llegada formal al objetivo actual, emitiendo vibración háptica de éxito. */
    fun completeArrival()

    /**
     * Actualiza las preferencias de vibración del reloj.
     *
     * @param enabled Verdadero para habilitar respuesta táctil.
     * @param strength Nivel de fuerza seleccionado ([HapticStrength]).
     */
    fun updateHaptics(enabled: Boolean, strength: HapticStrength)

    /** Re-evalúa los sitios históricos y cápsulas más cercanas en base a las coordenadas actuales. */
    fun refreshNearbyTargets()

    /**
     * Sincroniza un objetivo turístico individual transmitido desde el teléfono móvil.
     *
     * @param id Identificador del sitio.
     * @param name Nombre comercial o monumental del sitio.
     * @param lat Latitud geográfica.
     * @param lng Longitud geográfica.
     */
    fun setSyncTarget(id: String, name: String, lat: Double, lng: Double)

    /**
     * Sincroniza una ruta turística guiada con múltiples puntos de paso.
     *
     * @param title Nombre de la ruta.
     * @param waypoints Lista ordenada de hitos [Waypoint].
     */
    fun setSyncRoute(title: String, waypoints: List<Waypoint>)

    /** Limpia la ruta activa del estado del reloj. */
    fun clearActiveRoute()

    /** Marca la ruta turística activa como completada en su totalidad. */
    fun markRouteCompleted()

    /** Cierra el diálogo o banner de felicitación de ruta completada. */
    fun dismissRouteCompleted()

    /** Selecciona el siguiente objetivo disponible en la lista de auto-descubrimiento. */
    fun selectNextAutoTarget()

    /**
     * Elimina una alerta específica de la lista por su identificador.
     *
     * @param id Identificador de la alerta a remover.
     */
    fun deleteAlert(id: String)

    /** Remueve todas las alertas acumuladas en la sesión. */
    fun clearAllAlerts()

    /** Selecciona el objetivo previo en la lista de auto-descubrimiento. */
    fun selectPreviousAutoTarget()

    /**
     * Actualiza las coordenadas GPS actuales del reloj y recalcula distancias hacia el objetivo.
     *
     * @param lat Latitud actual del usuario.
     * @param lng Longitud actual del usuario.
     */
    fun updateCurrentLocation(lat: Double, lng: Double)

    /**
     * Actualiza la orientación del compás magnético.
     *
     * @param heading Ángulo de orientación actual en grados (0° - 360°).
     */
    fun updateHeading(heading: Float)
}
