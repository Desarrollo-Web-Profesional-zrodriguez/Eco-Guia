package mx.utng.ecoguia.shared.config

/**
 * Archivo de configuración central de constantes globales (estilo .env)
 * Permite ajustar umbrales de radar, distancias de búsqueda y filtrado de ruido de sensores.
 */
object EcoGuiaConfig {
    /** Radio máximo para filtrado espacial y búsqueda de sitios/rutas cercanas en metros. (Default: 50km = 50000m) */
    var SEARCH_RADIUS_METERS: Int = 50000

    /** Umbral de variación en grados para la brújula antes de actualizar la aguja (Filtro Anti-Temblores). (Default: 2.0f) */
    var COMPASS_HEADING_THRESHOLD_DEGREES: Float = 3.0f

    /** Factor de suavizado pasa-bajas para la brújula [0.0f a 1.0f]. Menor valor = más suave / menos temblor. (Default: 0.25f) */
    var COMPASS_SMOOTHING_FACTOR: Float = 0.25f

    /** Umbral de distancia para marcar llegada a una parada en metros. (Default: 50m) */
    var ARRIVAL_THRESHOLD_METERS: Int = 50

    /** Contexto global de la aplicación para fallback de Room */
    var appContext: android.content.Context? = null
}
