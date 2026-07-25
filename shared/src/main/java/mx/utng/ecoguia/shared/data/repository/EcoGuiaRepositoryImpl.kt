/**
 * Archivo: EcoGuiaRepositoryImpl.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Implementación del repositorio de datos que utiliza Neon HTTP API para PostgreSQL.
 * Gestiona consultas espaciales complejas para detectar sitios históricos y Geo-Drops.
 * 
 * Funciones destacadas:
 * - getNearbySites: Utiliza PostGIS (ST_DWithin) para encontrar sitios en un radio geográfico.
 * - getHistoricalSites: Recupera sitios con sus coordenadas extraídas mediante ST_X y ST_Y.
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
        return neonClient.executeQuery("SELECT *, ST_Y(location::geometry) as latitude, ST_X(location::geometry) as longitude FROM historical_sites WHERE is_active = TRUE")
    }

    /**
     * Recupera todas las rutas turísticas marcadas como activas.
     */
    override suspend fun getRoutes(): List<RemoteRoute> {
        return neonClient.executeQuery("SELECT * FROM routes WHERE is_active = TRUE")
    }

    /**
     * Recupera el catálogo de categorías.
     */
    override suspend fun getSiteCategories(): List<RemoteCategory> {
        return neonClient.executeQuery("SELECT id, name, icon FROM site_categories WHERE is_active = TRUE ORDER BY name ASC")
    }

    /**
     * Recupera los Geo-Drops (cápsulas) ordenados por fecha de creación.
     */
    override suspend fun getGeoDrops(): List<RemoteGeoDrop> {
        return neonClient.executeQuery("SELECT *, ST_Y(location::geometry) as latitude, ST_X(location::geometry) as longitude FROM geo_drops ORDER BY created_at DESC")
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
            SELECT *, ST_Y(location::geometry) as latitude, ST_X(location::geometry) as longitude 
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

    /**
     * Recupera la colección del usuario. Por ahora simula datos si la tabla no existe.
     */
    override suspend fun getUserCollection(userId: String): List<RemoteCollectionItem> {
        return try {
            // Intentamos obtener datos reales (suponiendo que existe la tabla)
            neonClient.executeQuery<RemoteCollectionItem>(
                "SELECT id, title, subtitle, type, created_at FROM user_collections WHERE user_id = $1 ORDER BY created_at DESC",
                listOf(userId)
            )
        } catch (e: Exception) {
            // Fallback a datos simulados para no romper la UI durante el desarrollo
            listOf(
                RemoteCollectionItem("1", "Parroquia de Dolores", "Sitio Histórico", "site", "2026-07-24"),
                RemoteCollectionItem("2", "Mural de la Independencia", "Foto Guardada", "photo", "2026-07-23"),
                RemoteCollectionItem("3", "Ruta de los Conspiradores", "Ruta Turística", "route", "2026-07-22")
            )
        }
    }

    /**
     * Registra un nuevo sitio histórico utilizando PostGIS.
     */
    override suspend fun createHistoricalSite(
        name: String,
        siteType: String,
        address: String,
        shortDesc: String,
        historyDesc: String,
        lat: Double,
        lng: Double,
        radiusM: Int,
        hours: String,
        cost: String,
        accessibility: String
    ): Boolean {
        val query = """
            INSERT INTO historical_sites (
                name, slug, site_type, short_description, historical_description, 
                address, location, detection_radius_m, is_active,
                cost_info
            ) VALUES (
                $1, $2, $3, $4, $5, $6, 
                ST_SetSRID(ST_MakePoint($7::double precision, $8::double precision), 4326)::geography, 
                $9::integer, TRUE, $10
            )
        """.trimIndent()
        
        // Generar un slug único añadiendo un sufijo aleatorio corto para evitar colisiones (Error 23505)
        val randomSuffix = (1..4).map { (('a'..'z') + ('0'..'9')).random() }.joinToString("")
        val baseSlug = name.lowercase().trim().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
        val slug = "${baseSlug}-${randomSuffix}"
        
        return try {
            val rowsAffected = neonClient.executeCommand(
                query = query,
                params = listOf(
                    name, slug, siteType, shortDesc, historyDesc, 
                    address, lng.toString(), lat.toString(), radiusM.toString(), cost
                )
            )
            android.util.Log.d("EcoGuiaRepo", "Sitio creado: $rowsAffected filas afectadas.")
            rowsAffected > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al crear sitio: ${e.message}", e)
            false
        }
    }
}
