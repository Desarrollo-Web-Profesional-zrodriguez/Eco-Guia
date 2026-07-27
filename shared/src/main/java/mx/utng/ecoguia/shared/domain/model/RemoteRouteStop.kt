/**
 * Archivo: RemoteRouteStop.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Modelo de datos serializable que representa una parada individual dentro de una ruta turística.
 * Incluye datos de la parada (orden, instrucción) y del sitio histórico asociado (nombre, coordenadas).
 *
 * Propiedades destacadas:
 * - routeId: UUID de la ruta a la que pertenece.
 * - siteId: UUID del sitio histórico asignado a la parada.
 * - stopOrder: Número de orden secuencial en el recorrido (1, 2, 3...).
 * - siteName / latitude / longitude: Información enriquecida del sitio histórico.
 */

package mx.utng.ecoguia.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteRouteStop(
    val id: String,
    @SerialName("route_id") val routeId: String,
    @SerialName("site_id") val siteId: String,
    @SerialName("stop_order") val stopOrder: Int,
    val instruction: String? = null,
    @SerialName("site_name") val siteName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
