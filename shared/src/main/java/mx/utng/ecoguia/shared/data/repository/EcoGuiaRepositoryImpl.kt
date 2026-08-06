/**
 * Archivo: EcoGuiaRepositoryImpl.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-30
 * Descripción: Implementación delegadora limpia y modularizada del repositorio EcoGuía.
 * Cada dominio de negocio (Auth, Sitios, GeoDrops, TV/Pairing) se encuentra
 * segmentado en archivos de extensión independientes en el paquete .extensions.
 */
package mx.utng.ecoguia.shared.data.repository

import kotlinx.coroutines.flow.firstOrNull
import mx.utng.ecoguia.shared.data.remote.NeonClient
import mx.utng.ecoguia.shared.data.repository.extensions.*
import mx.utng.ecoguia.shared.domain.model.*
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class EcoGuiaRepositoryImpl(
    val neonClient: NeonClient = NeonClient(),
    val context: android.content.Context? = mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext
) : EcoGuiaRepository {

    // ── Autenticación y Usuarios ──────────────────────────────────────────────
    override suspend fun login(email: String, password_hash: String): RemoteUser? =
        loginExt(email, password_hash)

    override suspend fun register(displayName: String, email: String, password_hash: String): Boolean =
        registerExt(email, password_hash, displayName, "visitor") != null

    override suspend fun updateUser(id: String, displayName: String, bio: String?): Boolean =
        updateUserProfileExt(id, displayName, bio, null)

    override suspend fun resetPassword(email: String, newPasswordHash: String): Boolean =
        resetUserPasswordExt(email, newPasswordHash)

    suspend fun getUserProfile(userId: String): RemoteUser? =
        getUserProfileExt(userId)

    suspend fun updateUserProfile(userId: String, displayName: String, avatarUrl: String?): Boolean =
        updateUserProfileExt(userId, displayName, null, avatarUrl)

    override suspend fun getUsersByRole(role: String): List<RemoteUser> =
        getUsersByRoleExt(role)

    override suspend fun getAllUsers(): List<RemoteUser> {
        return try {
            neonClient.executeQuery("SELECT id, email, display_name, role::text, bio, avatar_url, created_at::text FROM users WHERE role::text NOT IN ('super_admin', 'admin')")
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateUserRole(userId: String, newRole: String): Boolean {
        return try {
            neonClient.executeCommand("UPDATE users SET role = $1::user_role WHERE id::text = $2", listOf(newRole, userId)) > 0
        } catch (e: Exception) {
            false
        }
    }

    // ── Sitios Históricos y Categorías ────────────────────────────────────────
    override suspend fun getHistoricalSites(): List<RemoteHistoricalSite> =
        getHistoricalSitesExt()

    override suspend fun getHistoricalSiteById(siteId: String): RemoteHistoricalSite? =
        getHistoricalSiteByIdExt(siteId)

    override suspend fun getNearbySites(lat: Double, lng: Double, radiusM: Int): List<RemoteHistoricalSite> {
        val query = """
            SELECT id, name, slug, site_type::text, short_description, historical_description, address,
                   ST_AsText(location) as location,
                   ST_Y(location::geometry)::double precision AS latitude,
                   ST_X(location::geometry)::double precision AS longitude,
                   detection_radius_m, is_active
            FROM historical_sites
            WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography, $3)
              AND is_active = TRUE
        """.trimIndent()
        return try {
            val sites: List<RemoteHistoricalSite> = neonClient.executeQuery(query, listOf(lng.toString(), lat.toString(), radiusM.toString()))
            if (sites.isNotEmpty()) sites else getHistoricalSites()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.w("EcoGuiaRepo", "getNearbySites falló en SQL (${e.message}). Usando fallback getHistoricalSites().")
            getHistoricalSites()
        }
    }

    override suspend fun getSiteCategories(): List<RemoteCategory> {
        val query = "SELECT id::text, name, COALESCE(icon, 'general') as slug FROM site_categories ORDER BY name ASC"
        return try {
            val categories: List<RemoteCategory> = neonClient.executeQuery(query)
            if (categories.isNotEmpty()) {
                categories
            } else {
                android.util.Log.w("EcoGuiaRepo", "La tabla site_categories existe pero está vacía. Usando categorías base.")
                getDefaultCategoriesFallback()
            }
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error consultando categorías de la base de datos: ${e.message}", e)
            getDefaultCategoriesFallback()
        }
    }

    private fun getDefaultCategoriesFallback(): List<RemoteCategory> = listOf(
        RemoteCategory(id = "1", name = "Museo", slug = "museum"),
        RemoteCategory(id = "2", name = "Monumento Histórico", slug = "monument"),
        RemoteCategory(id = "3", name = "Plaza Principal", slug = "plaza"),
        RemoteCategory(id = "4", name = "Templo / Iglesia", slug = "church"),
        RemoteCategory(id = "5", name = "Sitio Arqueológico", slug = "archaeological")
    )

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
        accessibility: String,
        ownerUserId: String?
    ): String = createHistoricalSiteExt(name, siteType, address, shortDesc, historyDesc, lat, lng, radiusM, hours, cost, accessibility, ownerUserId)

    override suspend fun getSiteByOwner(userId: String): RemoteHistoricalSite? =
        getSiteByOwnerExt(userId)

    override suspend fun getSitesByOwnerOrAdmin(userId: String, isAdmin: Boolean): List<RemoteHistoricalSite> =
        getSitesByOwnerOrAdminExt(userId, isAdmin)

    override suspend fun deleteHistoricalSite(siteId: String): Boolean =
        deleteHistoricalSiteExt(siteId)
    override suspend fun assignSiteOwner(siteId: String, userId: String): Boolean =
        assignSiteOwnerExt(siteId, userId)

    override suspend fun createHistoricalSiteForOwner(
        name: String,
        slug: String,
        siteType: String,
        shortDescription: String?,
        address: String?,
        ownerUserId: String
    ): Boolean = assignSiteOwner(slug, ownerUserId)

    // ── Rutas Turísticas y Paradas ─────────────────────────────────────────────
    override suspend fun getRoutes(): List<RemoteRoute> {
        return try {
            neonClient.executeQuery("SELECT * FROM routes WHERE is_active = TRUE")
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getNearbyRoutes(lat: Double, lng: Double, radiusM: Int): List<RemoteRoute> = getRoutes()

    override suspend fun getRouteStops(routeId: String): List<RemoteRouteStop> {
        val query = """
            SELECT 
                rs.id, 
                rs.route_id, 
                rs.site_id,
                rs.site_id AS geo_drop_id,
                rs.stop_order, 
                rs.instruction, 
                rs.created_at::text,
                COALESCE(hs.name, 'Sitio Histórico') AS site_name,
                ST_Y(hs.location::geometry)::double precision AS latitude,
                ST_X(hs.location::geometry)::double precision AS longitude
            FROM route_stops rs
            LEFT JOIN historical_sites hs ON hs.id = rs.site_id
            WHERE rs.route_id::text = $1
            ORDER BY rs.stop_order ASC
        """.trimIndent()
        return try {
            val stops: List<RemoteRouteStop> = neonClient.executeQuery(query, listOf(routeId))
            if (stops.isNotEmpty() && context != null) {
                val database = mx.utng.ecoguia.shared.data.EcoGuiaDatabase.getDatabase(context)
                val entities = stops.map {
                    GeoDropEntity(
                        id = it.id,
                        routeId = routeId,
                        title = it.siteName ?: "Parada ${it.stopOrder}",
                        description = it.instruction.orEmpty(),
                        latitude = it.latitude ?: 0.0,
                        longitude = it.longitude ?: 0.0,
                        order = it.stopOrder,
                        isVisited = false
                    )
                }
                database.dao().insertGeoDrops(entities)
            }
            stops
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al consultar paradas remotas de la ruta: ${e.message}")
            if (context != null) {
                try {
                    val database = mx.utng.ecoguia.shared.data.EcoGuiaDatabase.getDatabase(context)
                    val localDrops = database.dao().getGeoDropsForRoute(routeId).firstOrNull() ?: emptyList()
                    localDrops.map { drop ->
                        RemoteRouteStop(
                            id = drop.id,
                            routeId = routeId,
                            siteId = drop.id,
                            geoDropId = drop.id,
                            stopOrder = drop.order,
                            instruction = drop.description,
                            siteName = drop.title,
                            latitude = drop.latitude,
                            longitude = drop.longitude
                        )
                    }
                } catch (localEx: Exception) {
                    emptyList()
                }
            } else emptyList()
        }
    }

    override suspend fun createRoute(title: String, description: String, estimatedMinutes: Int, siteIds: List<String>, ownerUserId: String?): Boolean {
        val slugBase = title.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .trim()
            .replace(Regex("\\s+"), "-")
        val slug = if (slugBase.isNotBlank()) "$slugBase-${System.currentTimeMillis().toString().takeLast(4)}" else "ruta-${System.currentTimeMillis()}"

        val insertRoute = """
            INSERT INTO routes (title, slug, description, estimated_minutes, created_by, is_active) 
            VALUES ($1, $2, $3, $4, NULLIF($5, '')::uuid, TRUE) 
            RETURNING id
        """.trimIndent()
        return try {
            val res = neonClient.executeQuery<Map<String, String>>(insertRoute, listOf(title, slug, description, estimatedMinutes.toString(), ownerUserId.orEmpty()))
            val routeId = res.firstOrNull()?.get("id")?.toString()?.trim('"')
            if (!routeId.isNullOrBlank()) {
                siteIds.forEachIndexed { index, sId ->
                    neonClient.executeCommand("INSERT INTO route_stops (route_id, site_id, stop_order) VALUES ($1::uuid, $2::uuid, $3)", listOf(routeId, sId, (index + 1).toString()))
                }
                true
            } else false
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error creando ruta: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteRoute(routeId: String): Boolean {
        val purgeSavedQuery = "DELETE FROM user_saved_items WHERE route_id::text = $1"
        val deleteRouteQuery = "DELETE FROM routes WHERE id::text = $1"
        return try {
            neonClient.executeCommand(purgeSavedQuery, listOf(routeId))
            val rows = neonClient.executeCommand(deleteRouteQuery, listOf(routeId))
            if (rows == 0) {
                // Fallback a soft-delete si hay otras dependencias FK
                neonClient.executeCommand("UPDATE routes SET is_active = FALSE WHERE id::text = $1", listOf(routeId)) > 0
            } else true
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al eliminar ruta $routeId: ${e.message}", e)
            try {
                // Intento fallback de desactivación
                neonClient.executeCommand("UPDATE routes SET is_active = FALSE WHERE id::text = $1", listOf(routeId)) > 0
            } catch (ex: Exception) {
                false
            }
        }
    }

    // ── Geo-Drops, Likes y Moderación ──────────────────────────────────────────
    override suspend fun getGeoDrops(): List<RemoteGeoDrop> =
        getGeoDropsExt()

    override suspend fun getGeoDropsBySite(siteId: String): List<RemoteGeoDrop> =
        getGeoDropsBySiteExt(siteId)

    override suspend fun getTopRankingGeoDrops(limit: Int): List<RemoteGeoDrop> =
        getTopRankingGeoDropsExt(limit)

    override suspend fun getPendingGeoDrops(): List<RemoteGeoDrop> {
        val query = """
            SELECT 
                gd.id, 
                gd.site_id, 
                gd.title, 
                gd.description, 
                gd.type::text, 
                gd.media_url,
                ST_Y(gd.location::geometry)::double precision AS latitude,
                ST_X(gd.location::geometry)::double precision AS longitude,
                gd.detection_radius_m, 
                gd.status::text, 
                gd.visible_on_tv, 
                gd.likes_count, 
                gd.reports_count,
                gd.created_at::text,
                COALESCE(hs.name, 'Sitio Histórico') AS site_name
            FROM geo_drops gd
            LEFT JOIN historical_sites hs ON hs.id = gd.site_id
            WHERE gd.status::text = 'pending'
            ORDER BY gd.created_at DESC
        """.trimIndent()
        return try {
            neonClient.executeQuery(query)
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error obteniendo GeoDrops pendientes: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun updateGeoDropStatus(id: String, status: String): Boolean {
        val isTvVisible = status == "approved"
        val query = "UPDATE geo_drops SET status = $1::content_status, visible_on_tv = $2 WHERE id::text = $3"
        return try {
            neonClient.executeCommand(query, listOf(status, isTvVisible.toString().uppercase(), id)) > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error actualizando estado de GeoDrop $id: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteGeoDrop(geoDropId: String, ownerUserId: String, mediaUrl: String?): Boolean =
        deleteGeoDropExt(geoDropId, ownerUserId, mediaUrl)

    override suspend fun createGeoDrop(title: String, description: String, lat: Double, lng: Double, userId: String?, siteId: String?, mediaUrl: String?): Boolean {
        val sId = siteId.orEmpty()
        val uId = userId.orEmpty()
        val res = createGeoDropExt(sId, uId, title, description, null, mediaUrl, lat, lng)
        return res.isNotBlank()
    }

    suspend fun createGeoDrop(siteId: String, userId: String, title: String, textContent: String, audioUrl: String?, imageUrl: String?): String =
        createGeoDropExt(siteId, userId, title, textContent, audioUrl, imageUrl)

    suspend fun likeGeoDrop(geoDropId: String): Boolean =
        likeGeoDropExt(geoDropId)

    suspend fun addCommentToGeoDrop(geoDropId: String, userId: String, textContent: String): Boolean =
        addCommentToGeoDropExt(geoDropId, userId, textContent)

    // ── Colección Personal ────────────────────────────────────────────────────
    override suspend fun getUserCollection(userId: String): List<RemoteCollectionItem> {
        val query = """
            SELECT 
                usi.id,
                COALESCE(usi.site_id::text, usi.geo_drop_id::text, usi.route_id::text) AS raw_id,
                COALESCE(hs.name, gd.title, r.title, 'Elemento Guardado') AS title,
                COALESCE(hs.short_description, gd.description, r.description, 'Sin descripción') AS subtitle,
                CASE 
                    WHEN usi.site_id IS NOT NULL THEN 'site'
                    WHEN usi.geo_drop_id IS NOT NULL THEN 'photo'
                    WHEN usi.route_id IS NOT NULL THEN 'route'
                    ELSE 'site'
                END AS type,
                COALESCE(gd.status::text, 'approved') AS status,
                gd.media_url AS media_url,
                COALESCE(gd.author_id::text, hs.created_by::text, r.created_by::text, '') AS author_id,
                usi.created_at::text AS created_at,
                
                -- Detalle extendido de Sitios
                hs.site_type::text AS site_type,
                hs.address AS address,
                hs.historical_description AS historical_description,
                hs.cost_info AS cost_info,
                COALESCE(hs.opening_hours::text, '') AS opening_hours,
                COALESCE(hs.accessibility::text, '') AS accessibility,
                
                -- Detalle extendido de GeoDrops
                COALESCE(hs_gd.name, hs.name, '') AS site_name,
                
                -- Detalle extendido de Rutas
                r.estimated_minutes AS estimated_minutes,
                r.distance_m AS distance_m,
                
                -- Coordenadas Geográficas
                COALESCE(
                    ST_Y(hs.location::geometry)::double precision,
                    ST_Y(gd.location::geometry)::double precision
                ) AS latitude,
                COALESCE(
                    ST_X(hs.location::geometry)::double precision,
                    ST_X(gd.location::geometry)::double precision
                ) AS longitude

            FROM user_saved_items usi
            LEFT JOIN historical_sites hs ON usi.site_id = hs.id
            LEFT JOIN geo_drops gd ON usi.geo_drop_id = gd.id
            LEFT JOIN historical_sites hs_gd ON gd.site_id = hs_gd.id
            LEFT JOIN routes r ON usi.route_id = r.id
            WHERE usi.user_id::text = $1
            ORDER BY usi.created_at DESC
        """.trimIndent()
        return try {
            neonClient.executeQuery(query, listOf(userId))
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error obteniendo colección: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun saveSite(userId: String, siteId: String): Boolean {
        val query = "INSERT INTO user_saved_items (user_id, site_id) VALUES ($1::uuid, $2::uuid) ON CONFLICT DO NOTHING"
        return try { neonClient.executeCommand(query, listOf(userId, siteId)) > 0 } catch (e: Exception) { false }
    }

    override suspend fun removeSavedSite(userId: String, itemId: String): Boolean {
        val query = """
            DELETE FROM user_saved_items 
            WHERE user_id::text = $1 
              AND (id::text = $2 OR site_id::text = $2 OR geo_drop_id::text = $2 OR route_id::text = $2)
        """.trimIndent()
        return try { neonClient.executeCommand(query, listOf(userId, itemId)) > 0 } catch (e: Exception) { false }
    }

    override suspend fun isSiteSaved(userId: String, siteId: String): Boolean {
        val query = "SELECT id FROM user_saved_items WHERE user_id::text = $1 AND site_id::text = $2 LIMIT 1"
        return try { neonClient.executeQuery<Map<String, String>>(query, listOf(userId, siteId)).isNotEmpty() } catch (e: Exception) { false }
    }

    override suspend fun isGeoDropCollected(userId: String, geoDropId: String): Boolean {
        val query = "SELECT id FROM user_saved_items WHERE user_id::text = $1 AND geo_drop_id::text = $2 LIMIT 1"
        return try { neonClient.executeQuery<Map<String, String>>(query, listOf(userId, geoDropId)).isNotEmpty() } catch (e: Exception) { false }
    }

    override suspend fun saveGeoDropToCollection(userId: String, geoDropId: String, siteId: String?): Boolean =
        saveGeoDropToCollectionExt(userId, geoDropId)

    override suspend fun saveRouteToCollection(userId: String, routeId: String): Boolean {
        val query = "INSERT INTO user_saved_items (user_id, route_id) VALUES ($1::uuid, $2::uuid) ON CONFLICT DO NOTHING"
        return try { 
            val success = neonClient.executeCommand(query, listOf(userId, routeId)) > 0
            if (success) true else saveRouteToCollectionOfflineFallback(userId, routeId)
        } catch (e: Exception) { 
            saveRouteToCollectionOfflineFallback(userId, routeId)
        }
    }

    private suspend fun saveRouteToCollectionOfflineFallback(userId: String, routeId: String): Boolean {
        if (context == null) return false
        return try {
            val database = mx.utng.ecoguia.shared.data.EcoGuiaDatabase.getDatabase(context)
            database.dao().insertPendingSyncAction(
                mx.utng.ecoguia.shared.domain.model.SyncPendingActionEntity(
                    actionType = "SAVE_ROUTE",
                    payloadJson = "{\"user_id\": \"$userId\", \"route_id\": \"$routeId\"}"
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    // ── Artículos de IA ───────────────────────────────────────────────────────
    override suspend fun getKnowledgeArticles(): List<RemoteKnowledgeArticle> {
        return try {
            neonClient.executeQuery("SELECT id, title, content, created_at::text FROM knowledge_articles ORDER BY created_at DESC")
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createKnowledgeArticle(title: String, content: String, authorId: String?): Boolean {
        val query = "INSERT INTO knowledge_articles (title, content) VALUES ($1, $2)"
        return try {
            neonClient.executeCommand(query, listOf(title, content)) > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al crear artículo de IA: ${e.message}", e)
            false
        }
    }

    override suspend fun testConnection(): String {
        return try {
            val res: List<Map<String, String>> = neonClient.executeQuery("SELECT NOW() as now_time")
            res.firstOrNull()?.get("now_time") ?: "OK"
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }

    // ── Dispositivos y TV Pairing ──────────────────────────────────────────────
    override suspend fun getUserDevices(userId: String): List<RemoteDevice> =
        getUserDevicesExt(userId)

    override suspend fun registerDevice(userId: String, name: String, type: String, deviceIdentifier: String): Boolean =
        registerDeviceExt(userId, name, type, deviceIdentifier)

    override suspend fun unlinkDevice(deviceId: String): Boolean =
        unlinkDeviceExt(deviceId)

    override suspend fun unlinkTvSession(pairingCode: String): Boolean =
        unlinkTvSessionExt(pairingCode)

    override suspend fun deactivateAllUserPairings(userId: String): Boolean =
        deactivateAllUserPairingsExt(userId)

    override suspend fun pairDeviceByCode(userId: String, pairingCode: String): Boolean =
        pairDeviceByCodeExt(userId, pairingCode)

    override suspend fun getPairingStatus(pairingCode: String): RemoteUser? =
        getPairingStatusExt(pairingCode)

    override suspend fun setTvTransmissionProgram(pairingCode: String, programType: String): Boolean =
        setTvTransmissionProgramExt(pairingCode, programType)

    override suspend fun getTvActiveProgram(pairingCode: String): String? =
        getTvActiveProgramExt(pairingCode)
    override suspend fun deleteUserCascade(userId: String): Boolean {
        val mainAdminEmail = "rodriguez.mora.zahir.15@gmail.com"
        return try {
            // Verificar si el usuario que se intenta eliminar es la cuenta principal protegida
            val checkUser: List<RemoteUser> = neonClient.executeQuery(
                "SELECT id, email, display_name, role FROM users WHERE id = $1::uuid",
                listOf(userId)
            )
            val userToDelete = checkUser.firstOrNull()
            if (userToDelete != null && userToDelete.email.equals(mainAdminEmail, ignoreCase = true)) {
                android.util.Log.w("EcoGuiaRepo", "Intento de eliminación de la cuenta de administración protegida ($mainAdminEmail) denegado.")
                return false
            }

            // Reasignar la propiedad de los sitios al usuario principal en lugar de NULL
            val updateSitesQuery = """
                UPDATE historical_sites 
                SET created_by = (SELECT id FROM users WHERE LOWER(email) = LOWER($2) LIMIT 1) 
                WHERE created_by = $1::uuid
            """.trimIndent()
            neonClient.executeCommand(updateSitesQuery, listOf(userId, mainAdminEmail))

            // Ejecución segura de borrados individuales para evitar fallas por tablas opcionales o inexistentes
            val deleteQueries = listOf(
                "DELETE FROM user_saved_items WHERE user_id = $1::uuid",
                "DELETE FROM user_route_progress WHERE user_id = $1::uuid",
                "DELETE FROM devices WHERE user_id = $1::uuid",
                "DELETE FROM device_pairings WHERE user_id = $1::uuid",
                "DELETE FROM user_reviews WHERE user_id = $1::uuid",
                "DELETE FROM sync_queue WHERE user_id = $1::uuid"
            )

            for (query in deleteQueries) {
                try {
                    neonClient.executeCommand(query, listOf(userId))
                } catch (subEx: Exception) {
                    android.util.Log.w("EcoGuiaRepo", "Consulta de borrado ignorada (tabla inexistente o vacía): ${subEx.message}")
                }
            }

            neonClient.executeCommand("DELETE FROM users WHERE id = $1::uuid AND LOWER(email) != LOWER($2)", listOf(userId, mainAdminEmail)) > 0
        } catch (e: Exception) {
            android.util.Log.e("EcoGuiaRepo", "Error al eliminar usuario en cascada: ${e.message}", e)
            false
        }
    }
}
