package mx.utng.ecoguia.shared.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request body for the Neon HTTP /sql endpoint.
 */
@Serializable
data class NeonSqlRequest(
    val query: String,
    val params: List<String>? = null,
    val arrayMode: Boolean = false
)

/**
 * Generic response for a single query.
 */
@Serializable
data class NeonSqlResponse(
    val rows: List<JsonObject>,
    val command: String? = null,
    val rowCount: Int? = null
)

/**
 * Error response from Neon API.
 */
@Serializable
data class NeonErrorResponse(
    val message: String,
    val code: String? = null
)
