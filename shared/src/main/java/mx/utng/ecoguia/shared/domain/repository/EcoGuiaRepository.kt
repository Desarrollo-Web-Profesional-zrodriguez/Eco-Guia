package mx.utng.ecoguia.shared.domain.repository

import mx.utng.ecoguia.shared.domain.model.*

interface EcoGuiaRepository {
    suspend fun getHistoricalSites(): List<RemoteHistoricalSite>
    suspend fun getRoutes(): List<RemoteRoute>
    suspend fun getGeoDrops(): List<RemoteGeoDrop>
    suspend fun createGeoDrop(title: String, description: String, lat: Double, lng: Double): Boolean
    suspend fun getNearbySites(lat: Double, lng: Double, radiusM: Int = 250): List<RemoteHistoricalSite>
    suspend fun testConnection(): String
}
