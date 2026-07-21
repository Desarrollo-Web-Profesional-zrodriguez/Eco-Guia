/**
 * Archivo: EcoGuiaRepositoryImpl.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Implementación del repositorio de datos que utiliza Neon HTTP API para PostgreSQL.
 */

package mx.utng.ecoguia.shared.data.repository

import mx.utng.ecoguia.shared.data.remote.NeonClient
import mx.utng.ecoguia.shared.domain.model.*
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class EcoGuiaRepositoryImpl(
    private val neonClient: NeonClient = NeonClient()
) : EcoGuiaRepository {

    /**
     * Recupera todos los sitios históricos marcados como activos.
     */
    override suspend fun getHistoricalSites(): List<RemoteHistoricalSite> {
        return neonClient.executeQuery("SELECT * FROM historical_sites WHERE is_active = TRUE")
    }

    /**
     * Recupera todas las rutas turísticas marcadas como activas.
     */
    override suspend fun getRoutes(): List<RemoteRoute> {
        return neonClient.executeQuery("SELECT * FROM routes WHERE is_active = TRUE")
    }

    /**
     * Recupera los Geo-Drops (cápsulas) ordenados por fecha de creación.
     */
    override suspend fun getGeoDrops(): List<RemoteGeoDrop> {
        return neonClient.executeQuery("SELECT * FROM geo_drops ORDER BY created_at DESC")
    }

    /**
     * Crea un nuevo Geo-Drop usando PostGIS para la localización.
     */
    override suspend fun createGeoDrop(title: String, description: String, lat: Double, lng: Double): Boolean {
        val query = """
            INSERT INTO geo_drops (title, description, location, status, type) 
            VALUES ($1, $2, ST_SetSRID(ST_MakePoint($3, $4), 4326)::geography, 'approved', 'text')
        """.trimIndent()
        return try {
            val rowsAffected = neonClient.executeCommand(
                query = query, 
                params = listOf(title, description, lng.toString(), lat.toString())
            )
            rowsAffected > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Busca sitios cercanos utilizando ST_DWithin de PostGIS.
     */
    override suspend fun getNearbySites(lat: Double, lng: Double, radiusM: Int): List<RemoteHistoricalSite> {
        val query = """
            SELECT *, ST_AsText(location) as location_text 
            FROM historical_sites 
            WHERE is_active = TRUE 
            AND ST_DWithin(
                location, 
                ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography, 
                $3
            )
        """.trimIndent()
        return neonClient.executeQuery(
            query = query, 
            params = listOf(lng.toString(), lat.toString(), radiusM.toString())
        )
    }

    /**
     * Verifica las credenciales de un usuario. Utiliza crypt() de pgcrypto.
     */
    override suspend fun login(email: String, password_hash: String): RemoteUser? {
        val query = "SELECT id, email, display_name, role, avatar_url, created_at FROM users WHERE email = $1 AND password_hash = crypt($2, password_hash) AND is_active = TRUE"
        return try {
            val result = neonClient.executeQuery<RemoteUser>(query, listOf(email, password_hash))
            result.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Registra un nuevo usuario. Cifra la contraseña con gen_salt('bf').
     */
    override suspend fun register(displayName: String, email: String, password_hash: String): Boolean {
        val query = "INSERT INTO users (display_name, email, password_hash, role) VALUES ($1, $2, crypt($3, gen_salt('bf')), 'visitor')"
        return try {
            val rowsAffected = neonClient.executeCommand(query, listOf(displayName, email, password_hash))
            rowsAffected > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Actualiza la información del perfil del usuario.
     */
    override suspend fun updateUser(id: String, displayName: String): Boolean {
        val query = "UPDATE users SET display_name = $1 WHERE id = $2"
        return try {
            val rowsAffected = neonClient.executeCommand(query, listOf(displayName, id))
            rowsAffected > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Ejecuta una consulta simple para validar la disponibilidad de la base de datos.
     */
    override suspend fun testConnection(): String {
        return try {
            val query = "SELECT now() as current_time"
            val response = neonClient.executeQuery<kotlinx.serialization.json.JsonObject>(query)
            val time = response.firstOrNull()?.get("current_time")?.toString()
            if (time != null) "Connected! Database time: $time" else "Connected, but no data."
        } catch (e: Exception) {
            "Connection failed: ${e.message}"
        }
    }
}
