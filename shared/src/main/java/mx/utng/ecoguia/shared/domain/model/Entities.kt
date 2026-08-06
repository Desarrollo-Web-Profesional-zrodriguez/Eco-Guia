package mx.utng.ecoguia.shared.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val totalStops: Int,
    val estimatedMinutes: Int
)

@Entity(tableName = "geo_drops")
data class GeoDropEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int,
    val isVisited: Boolean = false
)

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val message: String,
    val type: String, // "GEODROP", "SITE", "INFO"
    val timestamp: Long
)

@Entity(tableName = "sync_pending_actions")
data class SyncPendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // "MARK_VISITED", "SAVE_GEODROP", "DELETE_SITE"
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
