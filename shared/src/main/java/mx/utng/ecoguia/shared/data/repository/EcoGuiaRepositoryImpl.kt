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
     * Recupera todos los sitios históricos activos ordenados por nombre.
     */
    override suspend fun getHistoricalSites(): List<RemoteHistoricalSite> {
        return neonClient.executeQuery(
            "SELECT id, name, slug, site_type, address, short_description, historical_description, ST_Y(location::geometry) as latitude, ST_X(location::geometry) as longitude, detection_radius_m, is_active FROM historical_sites WHERE is_active = TRUE ORDER BY name ASC"
        )
    }

    /**
     * Registra un nuevo sitio histórico en Neon PostgreSQL usando las columnas existentes en el esquema real.
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
        val slug = name.lowercase().trim().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "") + "-" + (System.currentTimeMillis() % 10000)
        val query = """
            INSERT INTO historical_sites (name, slug, site_type, address, short_description, historical_description, location, detection_radius_m, is_active)
            VALUES ($1, $2, $3, $4, $5, $6, ST_SetSRID(ST_MakePoint($7, $8), 4326)::geography, $9::integer, TRUE)
        """.trimIndent()
        return try {
            val rows = neonClient.executeCommand(
                query,
                listOf(name, slug, siteType, address, shortDesc, historyDesc, lng.toString(), lat.toString(), radiusM.toString())
            )
            android.util.Log.d("EcoGuiaRepo", "Sitio registrado con éxito: $name ($rows filas)")
            rows > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al crear sitio histórico: ${e.message}", e)
            false
        }
    }


    /**
     * Recupera todas las rutas turísticas marcadas como activas.
     */
    override suspend fun getRoutes(): List<RemoteRoute> {
        return neonClient.executeQuery("SELECT * FROM routes WHERE is_active = TRUE")
    }

    /**
     * Obtiene rutas turísticas cercanas a un radio especificado (por defecto 50km = 50000m) usando PostGIS.
     */
    override suspend fun getNearbyRoutes(lat: Double, lng: Double, radiusM: Int): List<RemoteRoute> {
        val query = """
            SELECT DISTINCT r.*
            FROM routes r
            JOIN route_stops rs ON rs.route_id = r.id
            JOIN historical_sites hs ON hs.id = rs.site_id
            WHERE r.is_active = TRUE
              AND ST_DWithin(
                  hs.location,
                  ST_SetSRID(ST_MakePoint($1::double precision, $2::double precision), 4326)::geography,
                  $3::double precision
              )
        """.trimIndent()
        return try {
            neonClient.executeQuery<RemoteRoute>(query, listOf(lng.toString(), lat.toString(), radiusM.toString()))
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al buscar rutas cercanas: ${e.message}", e)
            getRoutes()
        }
    }

    /**
     * Recupera el catálogo de categorías.
     */
    override suspend fun getSiteCategories(): List<RemoteCategory> {

        return neonClient.executeQuery("SELECT id, name, icon FROM site_categories WHERE is_active = TRUE ORDER BY name ASC")
    }

    /**
     * Recupera los Geo-Drops (cápsulas) aprobados para el mapa y exploración.
     */
    override suspend fun getGeoDrops(): List<RemoteGeoDrop> {
        return neonClient.executeQuery("SELECT *, ST_Y(location::geometry) as latitude, ST_X(location::geometry) as longitude FROM geo_drops WHERE status = 'approved' ORDER BY created_at DESC")
    }

    /**
     * Recupera las cápsulas pendientes o reportadas para la lista de moderación del admin/moderador.
     */
    override suspend fun getPendingGeoDrops(): List<RemoteGeoDrop> {
        return try {
            neonClient.executeQuery("SELECT *, ST_Y(location::geometry) as latitude, ST_X(location::geometry) as longitude FROM geo_drops ORDER BY created_at DESC")
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al cargar cápsulas pendientes: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Actualiza el estado de moderación (approved/rejected) de un Geo-Drop.
     */
    override suspend fun updateGeoDropStatus(id: String, status: String): Boolean {
        val query = "UPDATE geo_drops SET status = $1 WHERE id = $2::uuid"
        return try {
            val rows = neonClient.executeCommand(query, listOf(status, id))
            rows > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al actualizar estado de moderación: ${e.message}", e)
            false
        }
    }


    /**
     * Crea un nuevo Geo-Drop usando PostGIS para la localización y lo vincula al usuario en user_saved_items.
     * Verifica la validez de las llaves foráneas para evitar violaciones de restricción en cuentas de prueba o UUIDs sintéticos.
     */
    override suspend fun createGeoDrop(title: String, description: String, lat: Double, lng: Double, userId: String?, siteId: String?, mediaUrl: String?): Boolean {
        val cleanUserId = if (userId == "guest") "" else userId.orEmpty()
        val insertGeoDropQuery = """
            INSERT INTO geo_drops (title, description, location, status, type, author_id, site_id, media_url) 
            VALUES ($1, $2, ST_SetSRID(ST_MakePoint($3, $4), 4326)::geography, 'pending', 'photo', 
                    CASE WHEN EXISTS (SELECT 1 FROM users WHERE id::text = $5) THEN $5::uuid ELSE NULL END, 
                    CASE WHEN EXISTS (SELECT 1 FROM historical_sites WHERE id::text = $6) THEN $6::uuid ELSE NULL END,
                    CASE WHEN $7 = '' THEN NULL ELSE $7 END)
            RETURNING id::text
        """.trimIndent()
        return try {
            val response = neonClient.executeQuery<kotlinx.serialization.json.JsonObject>(
                query = insertGeoDropQuery, 
                params = listOf(title, description, lng.toString(), lat.toString(), cleanUserId, siteId.orEmpty(), mediaUrl.orEmpty())
            )
            val newGeoDropId = response.firstOrNull()?.get("id")?.toString()?.replace("\"", "")
            
            if (newGeoDropId != null && cleanUserId.isNotBlank()) {
                val saveItemQuery = """
                    INSERT INTO user_saved_items (user_id, geo_drop_id, site_id)
                    SELECT $1::uuid, $2::uuid, CASE WHEN EXISTS (SELECT 1 FROM historical_sites WHERE id::text = $3) THEN $3::uuid ELSE NULL END
                    WHERE EXISTS (SELECT 1 FROM users WHERE id::text = $1)
                """.trimIndent()
                neonClient.executeCommand(saveItemQuery, listOf(cleanUserId, newGeoDropId, siteId.orEmpty()))
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al crear Geo-Drop: ${e.message}", e)
            false
        }
    }



    /**
     * Recupera la colección real del usuario consultando user_saved_items unida con historical_sites, routes y geo_drops.
     */
    override suspend fun getUserCollection(userId: String): List<RemoteCollectionItem> {
        val query = """
            SELECT
                COALESCE(gd.id::text, hs.id::text, r.id::text, usi.id::text) AS id,
                COALESCE(gd.title, hs.name, r.title, 'Elemento Guardado')    AS title,
                COALESCE(gd.description, hs.site_type, r.description, 'Colección') AS subtitle,
                CASE 
                    WHEN usi.geo_drop_id IS NOT NULL THEN 'photo'
                    WHEN usi.route_id IS NOT NULL THEN 'route' 
                    ELSE 'site' 
                END AS type,
                usi.created_at
            FROM user_saved_items usi
            LEFT JOIN geo_drops gd ON gd.id = usi.geo_drop_id
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
     * Verifica las credenciales de un usuario únicamente consultando la base de datos remota Neon PostgreSQL.
     */
    override suspend fun login(email: String, password_hash: String): RemoteUser? {
        val input = email.trim().lowercase()

        val query = "SELECT id, email, display_name, role, avatar_url, created_at FROM users WHERE (LOWER(email) = LOWER($1) OR LOWER(display_name) = LOWER($1)) AND password_hash = crypt($2, password_hash) AND is_active = TRUE"
        return try {
            val result = neonClient.executeQuery<RemoteUser>(query, listOf(input, password_hash))
            result.firstOrNull()
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error en login remotos: ${e.message}", e)
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
     * Obtiene los artículos de conocimiento curados desde la tabla knowledge_articles de Neon.
     */
    override suspend fun getKnowledgeArticles(): List<RemoteKnowledgeArticle> {
        val query = "SELECT id, title, content, created_by, is_active, created_at FROM knowledge_articles WHERE is_active = TRUE ORDER BY created_at DESC"
        return try {
            neonClient.executeQuery<RemoteKnowledgeArticle>(query)
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al obtener conocimiento de IA: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Guarda una nueva pregunta/respuesta curada en la tabla knowledge_articles de Neon.
     */
    override suspend fun createKnowledgeArticle(title: String, content: String, authorId: String?): Boolean {
        val cleanAuthorId = if (authorId.isNullOrEmpty()) "" else authorId

        val query = """
            INSERT INTO knowledge_articles (title, content, created_by, is_active)
            VALUES ($1, $2, CASE WHEN $3 <> '' AND EXISTS (SELECT 1 FROM users WHERE id::text = $3) THEN $3::uuid ELSE NULL END, TRUE)
        """.trimIndent()
        return try {
            val rows = neonClient.executeCommand(query, listOf(title, content, cleanAuthorId))
            rows > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al guardar conocimiento de IA: ${e.message}", e)
            false
        }
    }



    /**
     * Obtiene todos los usuarios registrados en Neon PostgreSQL.
     * Convierte el tipo de dato enum user_role a texto (role::text) para evitar errores de parseo en PostgreSQL.
     */
    override suspend fun getAllUsers(): List<RemoteUser> {
        val query = "SELECT id, email, display_name, role::text AS role, avatar_url, created_at FROM users WHERE role::text NOT IN ('admin') ORDER BY created_at DESC"
        return try {
            neonClient.executeQuery<RemoteUser>(query)
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al obtener usuarios: ${e.message}", e)
            emptyList()
        }
    }


    /**
     * Actualiza el rol de un usuario en Neon PostgreSQL.
     */
    override suspend fun updateUserRole(userId: String, newRole: String): Boolean {
        val query = "UPDATE users SET role = $1::user_role WHERE id = $2::uuid"
        return try {
            val rows = neonClient.executeCommand(query, listOf(newRole, userId))
            rows > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al actualizar rol de usuario: ${e.message}", e)
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
     * Guarda un sitio en la colección del usuario en user_saved_items.

     * ON CONFLICT DO NOTHING garantiza que no haya duplicados.
     */
    override suspend fun saveSite(userId: String, siteId: String): Boolean {
        val query = """
            INSERT INTO user_saved_items (user_id, site_id)
            SELECT $1::uuid, $2::uuid
            WHERE NOT EXISTS (
                SELECT 1 FROM user_saved_items WHERE user_id = $1::uuid AND site_id = $2::uuid
            )
        """.trimIndent()
        return try {
            val rows = neonClient.executeCommand(query, listOf(userId, siteId))
            android.util.Log.d("EcoGuiaRepo", "Sitio guardado: $siteId para usuario $userId ($rows filas)")
            true
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
            true
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
