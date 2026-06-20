package mx.utng.ecoguiawear.domain.model

enum class TargetType {
    HISTORIC_SITE,
    GEO_DROP
}

enum class RadarMode {
    PAUSED,
    SCANNING,
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

data class RadarUiState(
    val isLinkedToPhone: Boolean = false,
    val isStealthMode: Boolean = false,
    val mode: RadarMode = RadarMode.PAUSED,
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
        visitedStops = 1,
        totalStops = 5,
        nextStop = "Museo de la Independencia Nacional",
        estimatedMinutes = 18
    ),
    val hapticSettings: HapticSettings = HapticSettings(),
    val lastAlert: String = "Radar listo"
)
