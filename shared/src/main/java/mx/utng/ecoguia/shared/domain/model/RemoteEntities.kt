package mx.utng.ecoguia.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteUser(
    val id: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
    val role: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class RemoteHistoricalSite(
    val id: String,
    val name: String,
    val slug: String,
    @SerialName("site_type") val siteType: String,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("historical_description") val historicalDescription: String? = null,
    val address: String? = null,
    // Note: location is GEOGRAPHY, might need special handling if not returned as String/JSON
    // By default Neon /sql might return it as a Hex EWKB string.
    val location: String? = null,
    @SerialName("detection_radius_m") val detectionRadiusM: Int = 50,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class RemoteRoute(
    val id: String,
    val title: String,
    val slug: String,
    val description: String? = null,
    @SerialName("estimated_minutes") val estimatedMinutes: Int? = null,
    @SerialName("distance_m") val distanceM: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class RemoteGeoDrop(
    val id: String? = null,
    @SerialName("site_id") val siteId: String? = null,
    @SerialName("author_id") val authorId: String? = null,
    val title: String,
    val description: String? = null,
    val type: String = "photo",
    @SerialName("media_url") val mediaUrl: String? = null,
    val location: String? = null,
    @SerialName("status") val status: String = "pending",
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)
