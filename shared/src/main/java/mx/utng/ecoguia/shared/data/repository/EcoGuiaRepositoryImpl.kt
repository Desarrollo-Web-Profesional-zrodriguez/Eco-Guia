package mx.utng.ecoguia.shared.data.repository

import mx.utng.ecoguia.shared.data.remote.NeonClient
import mx.utng.ecoguia.shared.domain.model.*
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class EcoGuiaRepositoryImpl(
    private val neonClient: NeonClient = NeonClient()
) : EcoGuiaRepository {

    override suspend fun getHistoricalSites(): List<RemoteHistoricalSite> {
        return neonClient.executeQuery("SELECT * FROM historical_sites WHERE is_active = TRUE")
    }

    override suspend fun getRoutes(): List<RemoteRoute> {
        return neonClient.executeQuery("SELECT * FROM routes WHERE is_active = TRUE")
    }

    override suspend fun getGeoDrops(): List<RemoteGeoDrop> {
        return neonClient.executeQuery("SELECT * FROM geo_drops ORDER BY created_at DESC")
    }

    override suspend fun createGeoDrop(title: String, description: String, lat: Double, lng: Double): Boolean {
        val query = """
            INSERT INTO geo_drops (title, description, location, status, type) 
            VALUES ('$title', '$description', ST_SetSRID(ST_MakePoint($lng, $lat), 4326)::geography, 'approved', 'text')
        """.trimIndent()
        return try {
            val rowsAffected = neonClient.executeCommand(query)
            rowsAffected > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getNearbySites(lat: Double, lng: Double, radiusM: Int): List<RemoteHistoricalSite> {
        val query = """
            SELECT *, ST_AsText(location) as location_text 
            FROM historical_sites 
            WHERE is_active = TRUE 
            AND ST_DWithin(
                location, 
                ST_SetSRID(ST_MakePoint($lng, $lat), 4326)::geography, 
                $radiusM
            )
        """.trimIndent()
        // Note: Using ST_AsText to ensure location comes back as readable string if needed
        return neonClient.executeQuery(query)
    }

    override suspend fun testConnection(): String {
        return try {
            // A very simple query that returns one row with one column
            val query = "SELECT now() as current_time"
            val response = neonClient.executeQuery<kotlinx.serialization.json.JsonObject>(query)
            val time = response.firstOrNull()?.get("current_time")?.toString()
            if (time != null) "Connected! Database time: $time" else "Connected, but no data."
        } catch (e: Exception) {
            "Connection failed: ${e.message}"
        }
    }
}
