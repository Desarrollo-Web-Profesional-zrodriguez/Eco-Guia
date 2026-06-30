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
    val bearingDegrees: Float
)

data class RouteSummary(
    val title: String,
    val visitedStops: Int,
    val totalStops: Int,
    val nextStop: String,
    val estimatedMinutes: Int
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
        id = "geo-drop-museo-01",
        title = "Geo-Drop Museo",
        subtitle = "Museo de la Independencia Nacional",
        type = TargetType.GEO_DROP,
        distanceMeters = 64,
        bearingDegrees = 34f
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
