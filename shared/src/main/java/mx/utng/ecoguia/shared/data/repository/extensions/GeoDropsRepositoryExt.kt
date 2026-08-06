/**
 * Archivo: GeoDropsRepositoryExt.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-30
 * Descripción: Extensiones de repositorio para cápsulas Geo-Drop, ranking, likes y comentarios.
 */
package mx.utng.ecoguia.shared.data.repository.extensions

import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop

suspend fun EcoGuiaRepositoryImpl.getGeoDropsExt(): List<RemoteGeoDrop> {
    val query = """
        SELECT 
            gd.id, 
            gd.site_id, 
            gd.title, 
            gd.description, 
            gd.media_url, 
            gd.likes_count,
            ST_Y(gd.location::geometry)::double precision AS latitude,
            ST_X(gd.location::geometry)::double precision AS longitude,
            gd.detection_radius_m,
            gd.status::text,
            gd.created_at::text,
            COALESCE(hs.name, 'Sitio Histórico') AS site_name
        FROM geo_drops gd
        LEFT JOIN historical_sites hs ON hs.id = gd.site_id
        WHERE gd.status = 'approved'::content_status
        ORDER BY gd.created_at DESC
    """.trimIndent()
    return try {
        neonClient.executeQuery(query)
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo GeoDrops: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.getGeoDropsBySiteExt(siteId: String): List<RemoteGeoDrop> {
    val query = """
        SELECT 
            gd.id, 
            gd.site_id, 
            gd.title, 
            gd.description, 
            gd.media_url, 
            gd.likes_count,
            ST_Y(gd.location::geometry)::double precision AS latitude,
            ST_X(gd.location::geometry)::double precision AS longitude,
            gd.detection_radius_m,
            gd.status::text,
            gd.created_at::text,
            COALESCE(hs.name, 'Sitio Histórico') AS site_name
        FROM geo_drops gd
        LEFT JOIN historical_sites hs ON hs.id = gd.site_id
        WHERE gd.site_id::text = $1 AND gd.status = 'approved'::content_status
        ORDER BY gd.created_at DESC
    """.trimIndent()
    return try {
        neonClient.executeQuery(query, listOf(siteId))
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo GeoDrops del sitio: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.getTopRankingGeoDropsExt(limit: Int): List<RemoteGeoDrop> {
    val query = """
        SELECT 
            gd.id, 
            gd.site_id, 
            gd.title, 
            gd.description, 
            gd.media_url, 
            gd.likes_count,
            ST_Y(gd.location::geometry)::double precision AS latitude,
            ST_X(gd.location::geometry)::double precision AS longitude,
            gd.detection_radius_m,
            gd.status::text,
            gd.created_at::text,
            COALESCE(hs.name, 'Sitio Histórico') AS site_name
        FROM geo_drops gd
        LEFT JOIN historical_sites hs ON hs.id = gd.site_id
        WHERE gd.status = 'approved'::content_status
        ORDER BY gd.likes_count DESC, gd.created_at DESC
        LIMIT $1
    """.trimIndent()
    return try {
        neonClient.executeQuery(query, listOf(limit.toString()))
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo ranking de GeoDrops: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.createGeoDropExt(
    siteId: String,
    userId: String,
    title: String,
    textContent: String,
    audioUrl: String?,
    imageUrl: String?,
    lat: Double? = null,
    lng: Double? = null
): String {
    val query = if (siteId.isNotBlank() && lat != null && lng != null && lat != 0.0 && lng != 0.0) {
        """
            INSERT INTO geo_drops (site_id, author_id, title, description, media_url, location, status)
            VALUES ($1::uuid, NULLIF($7, '')::uuid, $2, $3, $4, ST_SetSRID(ST_MakePoint($6, $5), 4326)::geography, 'pending'::content_status)
            RETURNING id
        """.trimIndent()
    } else if (siteId.isNotBlank()) {
        """
            INSERT INTO geo_drops (site_id, author_id, title, description, media_url, location, status)
            VALUES ($1::uuid, NULLIF($5, '')::uuid, $2, $3, $4, (SELECT location FROM historical_sites WHERE id::text = $1 LIMIT 1), 'pending'::content_status)
            RETURNING id
        """.trimIndent()
    } else {
        """
            INSERT INTO geo_drops (site_id, author_id, title, description, media_url, location, status)
            VALUES (NULL, NULLIF($6, '')::uuid, $1, $2, $3, ST_SetSRID(ST_MakePoint($5, $4), 4326)::geography, 'pending'::content_status)
            RETURNING id
        """.trimIndent()
    }
    
    return try {
        val params = if (siteId.isNotBlank() && lat != null && lng != null && lat != 0.0 && lng != 0.0) {
            listOf(siteId, title, textContent, imageUrl.orEmpty(), lat.toString(), lng.toString(), userId)
        } else if (siteId.isNotBlank()) {
            listOf(siteId, title, textContent, imageUrl.orEmpty(), userId)
        } else {
            listOf(title, textContent, imageUrl.orEmpty(), (lat ?: 0.0).toString(), (lng ?: 0.0).toString(), userId)
        }
        val result = neonClient.executeQuery<Map<String, String>>(query, params)
        val newId = result.firstOrNull()?.get("id")?.toString()?.trim('"')
        if (!newId.isNullOrBlank()) {
            if (userId.isNotBlank()) {
                saveGeoDropToCollectionExt(userId, newId)
            }
            newId
        } else "SUCCESS"
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error creando GeoDrop: ${e.message}", e)
        ""
    }
}

suspend fun EcoGuiaRepositoryImpl.likeGeoDropExt(geoDropId: String): Boolean {
    val query = """
        UPDATE geo_drops
        SET likes_count = likes_count + 1
        WHERE id::text = $1
    """.trimIndent()
    return try {
        val rows = neonClient.executeCommand(query, listOf(geoDropId))
        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al dar like a GeoDrop: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.getCommentsByGeoDropExt(geoDropId: String): List<Map<String, String>> {
    val query = """
        SELECT c.id, c.geo_drop_id, c.user_id, c.text_content, c.created_at::text, u.display_name as user_name
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.geo_drop_id::text = $1
        ORDER BY c.created_at ASC
    """.trimIndent()
    return try {
        neonClient.executeQuery<Map<String, String>>(query, listOf(geoDropId))
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error obteniendo comentarios: ${e.message}", e)
        emptyList()
    }
}

suspend fun EcoGuiaRepositoryImpl.addCommentToGeoDropExt(geoDropId: String, userId: String, textContent: String): Boolean {
    val queryInsert = """
        INSERT INTO comments (geo_drop_id, user_id, text_content)
        VALUES ($1::uuid, $2::uuid, $3)
    """.trimIndent()
    val queryUpdateCount = """
        UPDATE geo_drops
        SET comments_count = comments_count + 1
        WHERE id::text = $1
    """.trimIndent()
    return try {
        neonClient.executeCommand(queryInsert, listOf(geoDropId, userId, textContent))
        neonClient.executeCommand(queryUpdateCount, listOf(geoDropId))
        true
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al comentar GeoDrop: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.saveGeoDropToCollectionExt(userId: String, geoDropId: String): Boolean {
    val query = """
        INSERT INTO user_saved_items (user_id, geo_drop_id)
        VALUES ($1::uuid, $2::uuid)
        ON CONFLICT DO NOTHING
    """.trimIndent()
    return try {
        neonClient.executeCommand(query, listOf(userId, geoDropId))
        true
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error al guardar GeoDrop en colección: ${e.message}", e)
        false
    }
}

suspend fun EcoGuiaRepositoryImpl.deleteGeoDropExt(
    geoDropId: String, 
    ownerUserId: String,
    providedMediaUrl: String? = null,
    onDeleteMediaUrl: (suspend (String) -> Unit)? = null
): Boolean {
    val getMediaUrlQuery = "SELECT media_url FROM geo_drops WHERE id::text = $1"
    val deleteSavedQuery = "DELETE FROM user_saved_items WHERE geo_drop_id::text = $1"
    val deleteGeoDropQuery = """
        DELETE FROM geo_drops
        WHERE id::text = $1 AND (author_id::text = $2 OR author_id IS NULL OR $2 = '')
    """.trimIndent()
    return try {
        val targetMediaUrl = if (!providedMediaUrl.isNullOrBlank()) {
            providedMediaUrl
        } else {
            val mediaResult: List<Map<String, Any?>> = neonClient.executeQuery(getMediaUrlQuery, listOf(geoDropId))
            mediaResult.firstOrNull()?.get("media_url")?.toString()
        }

        neonClient.executeCommand(deleteSavedQuery, listOf(geoDropId))
        val rows = neonClient.executeCommand(deleteGeoDropQuery, listOf(geoDropId, ownerUserId))
        android.util.Log.d("EcoGuiaRepo", "Borrado definitivo de GeoDrop $geoDropId: filas afectadas = $rows")

        if (rows > 0 && !targetMediaUrl.isNullOrBlank() && onDeleteMediaUrl != null) {
            try {
                onDeleteMediaUrl(targetMediaUrl)
            } catch (fe: Exception) {
                android.util.Log.w("EcoGuiaRepo", "No se pudo notificar el borrado de imagen: ${fe.message}")
            }
        }

        rows > 0
    } catch (e: Exception) {
        android.util.Log.e("EcoGuiaRepo", "Error eliminando GeoDrop $geoDropId: ${e.message}", e)
        false
    }
}
