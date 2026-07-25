package mx.utng.ecoguiawear.domain.model

enum class TargetType {
    HISTORIC_SITE,
    GEO_DROP
}

enum class RadarMode {
    PAUSED,
    SCANNING,
    FOLLOWING_ARROW,
    ARRIVED
}

enum class HapticStrength {
    LOW,
    MEDIUM,
    HIGH
}

data class RadarTarget(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: TargetType,
    val distanceMeters: Int,
    val bearingDegrees: Float,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class RouteSummary(
    val title: String,
    val visitedStops: Int,
    val totalStops: Int,
    val nextStop: String,
    val estimatedMinutes: Int,
    val waypoints: List<Waypoint> = emptyList()
)

data class Waypoint(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val isReached: Boolean = false
)

data class HapticSettings(
    val enabled: Boolean = true,
    val strength: HapticStrength = HapticStrength.MEDIUM
)

data class AlertEntity(
    val id: String,
    val message: String,
    val type: String, // "GEODROP", "SITE", "INFO"
    val timestamp: Long
)

data class RadarUiState(
    val isLinkedToPhone: Boolean = false,
    val isStealthMode: Boolean = false,
    val mode: RadarMode = RadarMode.PAUSED,
    val isGpsEnabled: Boolean = true,
    val isCameraReady: Boolean = true,
    val alerts: List<AlertEntity> = emptyList(),
    val target: RadarTarget = RadarTarget(
        id = "none",
        title = "Esperando objetivo",
        subtitle = "Selecciona un sitio en el móvil",
        type = TargetType.HISTORIC_SITE,
        distanceMeters = 0,
        bearingDegrees = 0f
    ),
    val routeSummary: RouteSummary = RouteSummary(
        title = "Ruta Independencia",
        visitedStops = 0,
        totalStops = 3,
        nextStop = "Punto 1",
        estimatedMinutes = 15
    ),
    val hapticSettings: HapticSettings = HapticSettings(),
    val lastAlert: String = "Radar listo"
)
