/**
 * Archivo: SitesRepositoryExt.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-30
 * Descripción: Extensiones de repositorio para gestión de Sitios Históricos y propietarios.
 */
package mx.utng.ecoguia.shared.data.repository.extensions

import kotlinx.coroutines.flow.firstOrNull
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite

suspend fun EcoGuiaRepositoryImpl.createHistoricalSiteExt(
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
): String {
    val slug = name.lowercase().trim().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "") + "-" + (System.currentTimeMillis() % 10000)
    val hoursJson = if (hours.isNotBlank()) "{\"schedule\": \"$hours\"}" else null
    val accessibilityJson = if (accessibility.isNotBlank()) {
        val items = accessibility.split(",").map { it.trim() }.joinToString("\", \"")
        "{\"features\": [\"$items\"]}"
    } else null

    // Asignar created_by al usuario creador
    val validOwnerUserId = if (!ownerUserId.isNullOrBlank()) ownerUserId else null

    val query = """
        INSERT INTO historical_sites (
            name, slug, site_type, address, short_description, historical_description, 
            location, detection_radius_m, opening_hours, cost_info, accessibility, created_by, is_active
        )
        VALUES (
            $1, $2, $3, $4, $5, $6, 
            ST_SetSRID(ST_MakePoint($7, $8), 4326)::geography, $9::integer, 
            $10::jsonb, $11, $12::jsonb, NULLIF($13, '')::uuid, TRUE
        )
        RETURNING id
    """.trimIndent()
    return try {
        val result = neonClient.executeQuery<Map<String, String>>(
            query,
            listOf(
                name, slug, siteType, address, shortDesc, historyDesc,
                lng.toString(), lat.toString(), radiusM.toString(),
                hoursJson.orEmpty(), cost, accessibilityJson.orEmpty(), validOwnerUserId.orEmpty()
            )
        )
        val newId = result.firstOrNull()?.get("id")?.toString()?.trim('"')
        android.util.Log.d("EcoGuiaRepo", "Sitio registrado con éxito: $name (ID: $newId, Propietario Asignado: $validOwnerUserId)")
        if (!newId.isNullOrBlank()) {
            if (!validOwnerUserId.isNullOrBlank()) {
                val saveQuery = """
                    INSERT INTO user_saved_items (user_id, site_id)
                    VALUES ($1::uuid, $2::uuid)
                    ON CONFLICT DO NOTHING
                """.trimIndent()
                neonClient.executeCommand(saveQuery, listOf(validOwnerUserId, newId))
            }
            newId
        } else "SUCCESS"
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al crear sitio histórico: ${e.message}", e)
        ""
    }
}

suspend fun EcoGuiaRepositoryImpl.getHistoricalSitesExt(): List<RemoteHistoricalSite> {
    val query = """
        SELECT id, name, slug, site_type::text, short_description, historical_description, address,
               ST_AsText(location) as location, ST_Y(location::geometry)::double precision AS latitude, ST_X(location::geometry)::double precision AS longitude,
               detection_radius_m, created_by::text AS created_by, is_active
        FROM historical_sites
        WHERE is_active = TRUE
        ORDER BY name ASC
    """.trimIndent()
    return try {
        neonClient.executeQuery(query)
    } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo sitios históricos: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.getHistoricalSiteByIdExt(siteId: String): RemoteHistoricalSite? {
    val query = """
        SELECT id, name, slug, site_type::text, short_description, historical_description, address,
               ST_AsText(location) as location, ST_Y(location::geometry)::double precision AS latitude, ST_X(location::geometry)::double precision AS longitude,
               detection_radius_m, created_by::text AS created_by, is_active
        FROM historical_sites
        WHERE id::text = $1
        LIMIT 1
    """.trimIndent()
    return try {
        val sites: List<RemoteHistoricalSite> = neonClient.executeQuery(query, listOf(siteId))
        sites.firstOrNull()
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo sitio $siteId: ${e.message}", e)
        null
    }
}

suspend fun EcoGuiaRepositoryImpl.getSiteByOwnerExt(userId: String): RemoteHistoricalSite? {
    val queryByOwner = """
        SELECT id, name, slug, site_type::text, short_description, historical_description, address,
               ST_AsText(location) as location, ST_Y(location::geometry)::double precision AS latitude, ST_X(location::geometry)::double precision AS longitude,
               detection_radius_m, created_by::text AS created_by, is_active
        FROM historical_sites
        WHERE created_by::text = $1 AND is_active = TRUE
        LIMIT 1
    """.trimIndent()
    return try {
        val sites: List<RemoteHistoricalSite> = neonClient.executeQuery(queryByOwner, listOf(userId))
        sites.firstOrNull()
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo sitio por dueño: ${e.message}", e)
        null
    }
}

suspend fun EcoGuiaRepositoryImpl.getSitesByOwnerOrAdminExt(userId: String, isAdmin: Boolean): List<RemoteHistoricalSite> {
    val query = if (isAdmin) {
        """
            SELECT id, name, slug, site_type::text, short_description, historical_description, address,
                   ST_AsText(location) as location, ST_Y(location::geometry)::double precision AS latitude, ST_X(location::geometry)::double precision AS longitude,
                   detection_radius_m, is_active
            FROM historical_sites
            WHERE is_active = TRUE
            ORDER BY name ASC
        """.trimIndent()
    } else {
        """
            SELECT id, name, slug, site_type::text, short_description, historical_description, address,
                   ST_AsText(location) as location, ST_Y(location::geometry)::double precision AS latitude, ST_X(location::geometry)::double precision AS longitude,
                   detection_radius_m, is_active
            FROM historical_sites
            WHERE created_by::text = $1 AND is_active = TRUE
            ORDER BY name ASC
        """.trimIndent()
    }
    return try {
        val params = if (isAdmin) emptyList() else listOf(userId)
        neonClient.executeQuery(query, params)
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error listando sitios por propietario/admin: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.deleteHistoricalSiteExt(siteId: String): Boolean {
    val query = """
        UPDATE historical_sites
        SET is_active = FALSE
        WHERE id::text = $1
    """.trimIndent()
    val purgeSavedQuery = "DELETE FROM user_saved_items WHERE site_id::text = $1"
    val purgeStopsQuery = "DELETE FROM route_stops WHERE site_id::text = $1"
    val disableGeoDropsQuery = "UPDATE geo_drops SET status = 'rejected'::content_status WHERE site_id::text = $1"
    return try {
        neonClient.executeCommand(purgeSavedQuery, listOf(siteId))
        neonClient.executeCommand(purgeStopsQuery, listOf(siteId))
        neonClient.executeCommand(disableGeoDropsQuery, listOf(siteId))
        val rows = neonClient.executeCommand(query, listOf(siteId))
        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error desactivando sitio $siteId: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.assignSiteOwnerExt(siteId: String, userId: String): Boolean {
    val checkRoleQuery = "SELECT role::text FROM users WHERE id::text = $1 LIMIT 1"
    val updateOwnerQuery = """
        UPDATE historical_sites
        SET created_by = NULLIF($1, '')::uuid
        WHERE id::text = $2
    """.trimIndent()

    return try {
        if (userId.isNotBlank()) {
            val userRows: List<Map<String, String>> = neonClient.executeQuery(checkRoleQuery, listOf(userId))
            val role = userRows.firstOrNull()?.get("role")?.lowercase().orEmpty()
            if (role !in listOf("museum_hotel", "museum", "hotel")) {
                android.util.Log.w("EcoGuiaRepo", "Solo usuarios con rol 'museum_hotel' pueden ser propietarios de un sitio. Rol recibido: $role")
                return false
            }
        }
        val rows = neonClient.executeCommand(updateOwnerQuery, listOf(userId, siteId))
        if (rows > 0 && userId.isNotBlank()) {
            val saveQuery = "INSERT INTO user_saved_items (user_id, site_id) VALUES ($1::uuid, $2::uuid) ON CONFLICT DO NOTHING"
            neonClient.executeCommand(saveQuery, listOf(userId, siteId))
        }
        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al asignar propietario al sitio $siteId: ${e.message}", e)
        false
    }
}
