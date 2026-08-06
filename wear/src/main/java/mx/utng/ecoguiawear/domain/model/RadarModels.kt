/**
 * Modelos de dominio y estructuras de datos para el radar háptico de proximidad en Wear OS.
 *
 * Define los estados de interfaz, objetivos geográficos, configuraciones hápticas,
 * paradas de ruta y entidades de alerta consumidas por el ViewModel y los controladores del reloj.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.domain.model

/**
 * Categoría del elemento objetivo rastreado por el radar.
 */
enum class TargetType {
    /** Monumento, parroquia o sitio histórico oficial de Dolores Hidalgo. */
    HISTORIC_SITE,

    /** Cápsula cultural o hallazgo comunitario GeoDrop. */
    GEO_DROP
}

/**
 * Modos de operación y estados de navegación del radar de proximidad.
 */
enum class RadarMode {
    /** El radar está en reposo o suspendido para ahorrar batería. */
    PAUSED,

    /** El radar está escaneando activamente la posición GPS y calculando proximidad. */
    SCANNING,

    /** El usuario se encuentra siguiendo la aguja de la brújula hacia el objetivo. */
    FOLLOWING_ARROW,

    /** El usuario ha llegado al perímetro de proximidad del objetivo (<30m). */
    ARRIVED
}

/**
 * Niveles de intensidad para la retroalimentación vibratoria del motor háptico.
 */
enum class HapticStrength {
    /** Vibración sutil de bajo impacto energético. */
    LOW,

    /** Vibración estándar balanceada para caminatas al aire libre. */
    MEDIUM,

    /** Vibración enérgica y prolongada para entornos con mucho movimiento. */
    HIGH
}

/**
 * Representa un objetivo geográfico hacia el cual apunta el radar.
 *
 * @property id Identificador único del sitio o cápsula.
 * @property title Nombre principal o título del objetivo.
 * @property subtitle Descripción corta o categoría del sitio.
 * @property type Tipo de objetivo ([TargetType.HISTORIC_SITE] o [TargetType.GEO_DROP]).
 * @property distanceMeters Distancia estimada en línea recta expresada en metros.
 * @property bearingDegrees Ángulo de azimut respecto al norte magnético (0° a 360°).
 * @property latitude Coordenada de latitud geográfica opcional.
 * @property longitude Coordenada de longitud geográfica opcional.
 * @property isAutoTarget Indica si el objetivo fue seleccionado automáticamente por cercanía.
 */
data class RadarTarget(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: TargetType,
    val distanceMeters: Int,
    val bearingDegrees: Float,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isAutoTarget: Boolean = false
)

/**
 * Resumen del progreso de una ruta turística activa transmitida desde el teléfono.
 *
 * @property title Nombre o temática de la ruta activa.
 * @property visitedStops Cantidad de sitios o paradas ya visitadas en la sesión.
 * @property totalStops Total de puntos de interés incluidos en la ruta.
 * @property nextStop Nombre del próximo punto de interés sugerido.
 * @property estimatedMinutes Tiempo estimado en minutos para completar el recorrido restante.
 * @property waypoints Lista detallada de coordenadas y puntos de paso que integran la ruta.
 */
data class RouteSummary(
    val title: String,
    val visitedStops: Int,
    val totalStops: Int,
    val nextStop: String,
    val estimatedMinutes: Int,
    val waypoints: List<Waypoint> = emptyList()
)

/**
 * Punto de parada o hito geográfico individual dentro de una ruta turística.
 *
 * @property id Identificador del punto de paso.
 * @property title Nombre del sitio histórico asociado.
 * @property latitude Coordenada de latitud del hito.
 * @property longitude Coordenada de longitud del hito.
 * @property isReached Indica si el usuario ya registró su llegada física a este punto.
 */
data class Waypoint(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val isReached: Boolean = false
)

/**
 * Ajustes de personalización para la respuesta háptica en el reloj.
 *
 * @property enabled Indica si las vibraciones de proximidad se encuentran activas.
 * @property strength Nivel de fuerza o intensidad configurado ([HapticStrength]).
 */
data class HapticSettings(
    val enabled: Boolean = true,
    val strength: HapticStrength = HapticStrength.MEDIUM
)

/**
 * Registro de una notificación o evento de proximidad emitido por el sistema.
 *
 * @property id Identificador único de la alerta.
 * @property message Contenido descriptivo del evento de proximidad.
 * @property type Categoría de la alerta (e.g., "GEODROP", "SITE", "INFO").
 * @property timestamp Marca de tiempo en milisegundos en que se generó la alerta.
 */
data class AlertEntity(
    val id: String,
    val message: String,
    val type: String,
    val timestamp: Long
)

/**
 * Estado inmutable completo de la interfaz de usuario del módulo Wear OS.
 *
 * @property isLinkedToPhone Indica si el reloj mantiene un canal de comunicación activo con la app móvil.
 * @property isStealthMode Modo discreto que apaga la pantalla y guía exclusivamente mediante pulsos hápticos.
 * @property mode Modo de navegación actual del radar ([RadarMode]).
 * @property isGpsEnabled Indica si el sensor GPS del dispositivo está encendido y accesible.
 * @property isCameraReady Indica si el hardware periférico y sensores de orientación se encuentran listos.
 * @property alerts Lista histórica de alertas de proximidad recibidas en la sesión.
 * @property currentHeading Orientación angular actual del reloj respecto al norte (grados).
 * @property target Objetivo geográfico activo hacia el cual se calculan distancia y rumbo.
 * @property routeSummary Información consolidada de la ruta turística sincronizada.
 * @property hapticSettings Preferencias activas de vibración.
 * @property lastAlert Mensaje o descripción del evento más reciente.
 * @property isRouteCompleted Indica si el usuario finalizó satisfactoriamente todos los hitos de la ruta.
 * @property nearbyAutoTargets Lista de objetivos cercanos descubiertos automáticamente por GPS.
 * @property selectedAutoIndex Índice del objetivo seleccionado dentro de la lista de auto-descubrimiento.
 */
data class RadarUiState(
    val isLinkedToPhone: Boolean = false,
    val isStealthMode: Boolean = false,
    val mode: RadarMode = RadarMode.PAUSED,
    val isGpsEnabled: Boolean = true,
    val isCameraReady: Boolean = true,
    val alerts: List<AlertEntity> = emptyList(),
    val currentHeading: Float = 0f,
    val target: RadarTarget = RadarTarget(
        id = "none",
        title = "Esperando objetivo",
        subtitle = "Selecciona un sitio en el móvil",
        type = TargetType.HISTORIC_SITE,
        distanceMeters = 0,
        bearingDegrees = 0f
    ),
    val routeSummary: RouteSummary = RouteSummary(
        title = "Sin ruta activa",
        visitedStops = 0,
        totalStops = 0,
        nextStop = "Esperando ruta desde móvil",
        estimatedMinutes = 0,
        waypoints = emptyList()
    ),
    val hapticSettings: HapticSettings = HapticSettings(),
    val lastAlert: String = "Radar listo",
    val isRouteCompleted: Boolean = false,
    val nearbyAutoTargets: List<RadarTarget> = emptyList(),
    val selectedAutoIndex: Int = 0
)
