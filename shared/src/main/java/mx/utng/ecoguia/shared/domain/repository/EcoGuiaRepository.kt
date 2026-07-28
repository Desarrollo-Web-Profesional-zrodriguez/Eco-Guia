/**
 * Archivo: EcoGuiaRepository.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Interfaz que define las operaciones de acceso a datos para la aplicación Eco-Guía.
 *
 * Funciones destacadas:
 * - getHistoricalSites: Obtiene todos los sitios históricos activos.
 * - getNearbySites: Busca sitios dentro de un radio dado en metros.
 * - getUserCollection: Recupera los elementos guardados del usuario desde user_saved_items.
 * - saveSite / removeSavedSite / isSiteSaved: CRUD de colección personal.
 * - login / register / updateUser: Gestión de autenticación.
 */

package mx.utng.ecoguia.shared.domain.repository

import mx.utng.ecoguia.shared.domain.model.*

interface EcoGuiaRepository {
    /**
     * Obtiene la lista de todos los sitios históricos activos.
     */
    suspend fun getHistoricalSites(): List<RemoteHistoricalSite>

    /**
     * Registra un nuevo sitio histórico en la base de datos Neon PostgreSQL.
     */
    suspend fun createHistoricalSite(
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
    ): Boolean


    /**
     * Obtiene la lista de todas las rutas turísticas activas.
     */
    suspend fun getRoutes(): List<RemoteRoute>

    /**
     * Busca rutas turísticas cuyas paradas estén dentro de un radio en metros (ej. 50km = 50000m).
     */
    suspend fun getNearbyRoutes(lat: Double, lng: Double, radiusM: Int = 50000): List<RemoteRoute>

    /**
     * Obtiene la lista de cápsulas de información (Geo-Drops) aprobadas.
     */
    suspend fun getGeoDrops(): List<RemoteGeoDrop>


    /**
     * Obtiene todas las cápsulas Geo-Drop o reportes pendientes de moderación.
     */
    suspend fun getPendingGeoDrops(): List<RemoteGeoDrop>

    /**
     * Actualiza el estado de moderación de un Geo-Drop (approved, rejected, pending).
     */
    suspend fun updateGeoDropStatus(id: String, status: String): Boolean


    /**
     * Registra una nueva cápsula de información (Geo-Drop) en la nube y la guarda en la colección del usuario.
     */
    suspend fun createGeoDrop(title: String, description: String, lat: Double, lng: Double, userId: String? = null, siteId: String? = null, mediaUrl: String? = null): Boolean



    /**
     * Busca sitios históricos cercanos a una ubicación específica.
     */
    suspend fun getNearbySites(lat: Double, lng: Double, radiusM: Int = 250): List<RemoteHistoricalSite>

    /**
     * Obtiene el catálogo de categorías para los sitios.
     */
    suspend fun getSiteCategories(): List<RemoteCategory>



    /**
     * Inicia sesión verificando credenciales en la base de datos remota.
     * Retorna el objeto RemoteUser si tiene éxito, null en caso contrario.
     */
    suspend fun login(email: String, password_hash: String): RemoteUser?

    /**
     * Registra un nuevo usuario en el sistema.
     * Retorna verdadero si la operación fue exitosa.
     */
    suspend fun register(displayName: String, email: String, password_hash: String): Boolean

    /**
     * Actualiza el nombre a mostrar de un usuario existente.
     * Retorna verdadero si la operación fue exitosa.
     */
    suspend fun updateUser(id: String, displayName: String): Boolean

    /**
     * Obtiene los artículos de conocimiento curados para la IA desde la tabla knowledge_articles de Neon.
     */
    suspend fun getKnowledgeArticles(): List<RemoteKnowledgeArticle>

    /**
     * Guarda una nueva pregunta/respuesta curada en la tabla knowledge_articles de Neon.
     */
    suspend fun createKnowledgeArticle(title: String, content: String, authorId: String? = null): Boolean

    /**
     * Obtiene todos los usuarios registrados excluyendo las cuentas con rol de super admin.
     */
    suspend fun getAllUsers(): List<RemoteUser>



    /**
     * Actualiza el rol de un usuario en la base de datos remota.
     */
    suspend fun updateUserRole(userId: String, newRole: String): Boolean

    /**
     * Prueba la conexión con el servidor de base de datos.
     */
    suspend fun testConnection(): String


    /**
     * Obtiene los elementos guardados por un usuario desde user_saved_items.
     */
    suspend fun getUserCollection(userId: String): List<RemoteCollectionItem>

    /**
     * Guarda un sitio histórico en la colección del usuario.
     */
    suspend fun saveSite(userId: String, siteId: String): Boolean

    /**
     * Elimina un sitio guardado de la colección del usuario.
     */
    suspend fun removeSavedSite(userId: String, siteId: String): Boolean

    /**
     * Verifica si un sitio ya fue guardado por el usuario.
     */
    suspend fun isSiteSaved(userId: String, siteId: String): Boolean

    /**
     * Obtiene la lista ordenada de paradas de una ruta turística específica.
     */
    suspend fun getRouteStops(routeId: String): List<RemoteRouteStop>

    /**
     * Crea una nueva ruta turística en Neon PostgreSQL junto con sus paradas ordenadas.
     * Retorna verdadero si la operación fue exitosa.
     */
    suspend fun createRoute(
        title: String,
        description: String,
        estimatedMinutes: Int,
        siteIds: List<String>
    ): Boolean

    /**
     * Elimina una ruta turística existente por su ID.
     */
    suspend fun deleteRoute(routeId: String): Boolean

    /**
     * Guarda un Geo-Drop público existente en la colección personal del usuario (user_saved_items).
     */
    suspend fun saveGeoDropToCollection(userId: String, geoDropId: String, siteId: String? = null): Boolean

    /**
     * Guarda una ruta completada en la colección personal del usuario (user_saved_items).
     */
    suspend fun saveRouteToCollection(userId: String, routeId: String): Boolean
}

