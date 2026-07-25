/**
 * Archivo: EcoGuiaRepositoryImpl.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Implementación del repositorio de datos que utiliza Neon HTTP API para PostgreSQL.
 * Gestiona consultas espaciales complejas para detectar sitios históricos y Geo-Drops,
 * y la persistencia real de la colección personal del usuario en user_saved_items.
 *
 * Funciones destacadas:
 * - getNearbySites: Utiliza PostGIS (ST_DWithin) para encontrar sitios en un radio geográfico.
 * - getHistoricalSites: Recupera sitios con sus coordenadas mediante ST_X y ST_Y.
 * - getUserCollection: Consulta user_saved_items JOIN historical_sites, devuelve site_id como id.
 * - saveSite: INSERT con ON CONFLICT DO NOTHING para evitar duplicados en user_saved_items.
 * - removeSavedSite: DELETE por user_id + site_id de user_saved_items.
 * - isSiteSaved: COUNT para verificar si un sitio ya está guardado por el usuario.
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
     * Recupera la colección real del usuario consultando user_saved_items unida con historical_sites.
     * IMPORTANTE: Selecciona usi.site_id como id (no usi.id) para que RemoteCollectionItem.id
     * coincida con el historical_site UUID usado en savedSiteIds y removeSavedSite.
     */
    override suspend fun getUserCollection(userId: String): List<RemoteCollectionItem> {
        val query = """
            SELECT
                usi.id::text                        AS id,
                COALESCE(hs.name, r.title, 'Elemento Guardado')   AS title,
                COALESCE(hs.site_type, r.description, 'Colección') AS subtitle,
                CASE WHEN usi.route_id IS NOT NULL THEN 'route' ELSE 'site' END AS type,
                usi.created_at
            FROM user_saved_items usi
            LEFT JOIN historical_sites hs ON hs.id = usi.site_id
            LEFT JOIN routes r ON r.id = usi.route_id
            WHERE usi.user_id = $1::uuid
            ORDER BY usi.created_at DESC
        """.trimIndent()
        return try {
            neonClient.executeQuery<RemoteCollectionItem>(query, listOf(userId))
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al cargar colección: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Guarda un sitio en la colección del usuario en user_saved_items.
     * ON CONFLICT DO NOTHING garantiza que no haya duplicados.
     */
    override suspend fun saveSite(userId: String, siteId: String): Boolean {
        val query = """
            INSERT INTO user_saved_items (user_id, site_id)
            VALUES ($1::uuid, $2::uuid)
            ON CONFLICT DO NOTHING
        """.trimIndent()
        return try {
            val rows = neonClient.executeCommand(query, listOf(userId, siteId))
            android.util.Log.d("EcoGuiaRepo", "Sitio guardado: $siteId para usuario $userId ($rows filas)")
            true // ON CONFLICT devuelve 0 filas pero no es un error
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al guardar sitio: ${e.message}", e)
            false
        }
    }

    /**
     * Elimina un sitio de la colección del usuario en user_saved_items.
     */
    override suspend fun removeSavedSite(userId: String, siteId: String): Boolean {
        val query = """
            DELETE FROM user_saved_items
            WHERE user_id = $1::uuid 
              AND (id = $2::uuid OR site_id = $2::uuid OR route_id = $2::uuid)
        """.trimIndent()
        return try {
            val rows = neonClient.executeCommand(query, listOf(userId, siteId))
            android.util.Log.d("EcoGuiaRepo", "Elemento eliminado de colección: $siteId ($rows filas)")
            rows > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al eliminar elemento de colección: ${e.message}", e)
            false
        }
    }

    /**
     * Verifica si un sitio ya está guardado por el usuario en user_saved_items.
     */
    override suspend fun isSiteSaved(userId: String, siteId: String): Boolean {
        val query = """
            SELECT COUNT(*) AS count
            FROM user_saved_items
            WHERE user_id = $1::uuid AND site_id = $2::uuid
        """.trimIndent()
        return try {
            val result = neonClient.executeQuery<kotlinx.serialization.json.JsonObject>(query, listOf(userId, siteId))
            val count = result.firstOrNull()?.get("count")?.toString()?.trim('"')?.toLongOrNull() ?: 0L
            count > 0L
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al verificar guardado: ${e.message}", e)
            android.util.Log.e("EcoGuiaRepo", "Error al eliminar ruta: ${e.message}", e)
            false
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

    /**
     * Obtiene las paradas ordenadas de una ruta turística, incluyendo nombre y coordenadas del sitio histórico.
     */
    override suspend fun getRouteStops(routeId: String): List<mx.utng.ecoguia.shared.domain.model.RemoteRouteStop> {
        val query = """
            SELECT
                rs.id::text           AS id,
                rs.route_id::text     AS route_id,
                rs.site_id::text      AS site_id,
                rs.stop_order         AS stop_order,
                rs.instruction        AS instruction,
                hs.name               AS site_name,
                ST_Y(hs.location::geometry) AS latitude,
                ST_X(hs.location::geometry) AS longitude
            FROM route_stops rs
            JOIN historical_sites hs ON hs.id = rs.site_id
            WHERE rs.route_id = $1::uuid
            ORDER BY rs.stop_order ASC
        """.trimIndent()
        return try {
            neonClient.executeQuery<mx.utng.ecoguia.shared.domain.model.RemoteRouteStop>(query, listOf(routeId))
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al obtener paradas de ruta: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Crea una ruta turística nueva e inserta sus paradas ordenadas en Neon PostgreSQL.
     */
    override suspend fun createRoute(
        title: String,
        description: String,
        estimatedMinutes: Int,
        siteIds: List<String>
    ): Boolean {
        return try {
            val slug = title.lowercase().trim().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "") + "-" + (System.currentTimeMillis() % 10000)
            val routeQuery = """
                INSERT INTO routes (title, slug, description, estimated_minutes)
                VALUES ($1, $2, $3, $4::integer)
                RETURNING id::text
            """.trimIndent()

            val routeResult = neonClient.executeQuery<kotlinx.serialization.json.JsonObject>(
                routeQuery,
                listOf(title, slug, description, estimatedMinutes.toString())
            )

            val routeId = routeResult.firstOrNull()?.get("id")?.toString()?.trim('"')
                ?: return false

            siteIds.forEachIndexed { index, siteId ->
                val stopQuery = """
                    INSERT INTO route_stops (route_id, site_id, stop_order)
                    VALUES ($1::uuid, $2::uuid, $3::integer)
                    ON CONFLICT DO NOTHING
                """.trimIndent()
                neonClient.executeCommand(stopQuery, listOf(routeId, siteId, (index + 1).toString()))
            }

            android.util.Log.d("EcoGuiaRepo", "Ruta creada con éxito: $routeId")
            true
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al crear ruta: ${e.message}", e)
            false
        }
    }

    /**
     * Elimina una ruta existente por su ID.
     */
    override suspend fun deleteRoute(routeId: String): Boolean {
        val query = "DELETE FROM routes WHERE id = $1::uuid"
        return try {
            val rows = neonClient.executeCommand(query, listOf(routeId))
            rows > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al eliminar ruta: ${e.message}", e)
            false
        }
    }

    /**
     * Guarda explícitamente una ruta completada en la colección del usuario en user_saved_items.
     */
    override suspend fun saveRouteToCollection(userId: String, routeId: String): Boolean {
        val query = """
            INSERT INTO user_saved_items (user_id, route_id)
            VALUES ($1::uuid, $2::uuid)
            ON CONFLICT DO NOTHING
        """.trimIndent()
        return try {
            val rows = neonClient.executeCommand(query, listOf(userId, routeId))
            android.util.Log.d("EcoGuiaRepo", "Ruta $routeId guardada explícitamente en colección para usuario $userId ($rows filas)")
            true
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al guardar ruta explícita en colección: ${e.message}", e)
            false
        }
    }
}
