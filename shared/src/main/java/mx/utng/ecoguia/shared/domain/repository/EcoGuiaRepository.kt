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
     * Elimina un usuario definitivamente y todas sus referencias en cascada en Neon PostgreSQL.
     */
    suspend fun deleteUserCascade(userId: String): Boolean

    /**
     * Obtiene la lista de todos los sitios históricos activos.
     */
    suspend fun getHistoricalSites(): List<RemoteHistoricalSite>

    /**
     * Obtiene un sitio histórico específico por su ID desde Neon PostgreSQL.
     */
    suspend fun getHistoricalSiteById(siteId: String): RemoteHistoricalSite?

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
        accessibility: String,
        ownerUserId: String? = null
    ): String



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
     * Obtiene las cápsulas asociadas a un sitio histórico específico.
     */
    suspend fun getGeoDropsBySite(siteId: String): List<RemoteGeoDrop>

    /**
     * Obtiene el ranking semanal de cápsulas con mayor número de interacciones/likes.
     */
    suspend fun getTopRankingGeoDrops(limit: Int = 10): List<RemoteGeoDrop>



    /**
     * Obtiene todas las cápsulas Geo-Drop o reportes pendientes de moderación.
     */
    suspend fun getPendingGeoDrops(): List<RemoteGeoDrop>

    /**
     * Actualiza el estado de moderación de un Geo-Drop (approved, rejected, pending).
     */
    suspend fun updateGeoDropStatus(id: String, status: String): Boolean


    /**
     * Elimina definitivamente una cápsula Geo-Drop si el usuario que lo solicita es su autor.
     * Opcionalmente recibe la mediaUrl para notificar a la capa superior si debe borrar de Storage.
     */
    suspend fun deleteGeoDrop(geoDropId: String, ownerUserId: String, mediaUrl: String? = null): Boolean


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
     * Actualiza el nombre y biografía de un usuario existente.
     * Retorna verdadero si la operación fue exitosa.
     */
    suspend fun updateUser(id: String, displayName: String, bio: String? = null): Boolean

    /**
     * Actualiza la contraseña de un usuario a partir de su correo electrónico.
     */
    suspend fun resetPassword(email: String, newPasswordHash: String): Boolean

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
     * Obtiene los dispositivos o sesiones reales registrados para un usuario desde la tabla devices de Neon PostgreSQL.
     */
    suspend fun getUserDevices(userId: String): List<RemoteDevice>

    /**
     * Registra un nuevo dispositivo o sesión activa para el usuario en la tabla devices.
     */
    suspend fun registerDevice(userId: String, name: String, type: String, deviceIdentifier: String): Boolean

    /**
     * Desvincula / elimina la sesión de un dispositivo específico de la tabla devices.
     */
    suspend fun unlinkDevice(deviceId: String): Boolean

    /**
     * Cierra la sesión completa de una Smart TV:
     * - Desactiva el registro en device_pairings (is_active = FALSE)
     * - Elimina el dispositivo TV de la tabla devices
     * El dispositivo desaparece de 'Mis Dispositivos' en la app móvil.
     */
    suspend fun unlinkTvSession(pairingCode: String): Boolean

    /**
     * Desactiva todos los registros de vinculación activos del usuario en device_pairings (is_active = FALSE).
     */
    suspend fun deactivateAllUserPairings(userId: String): Boolean

    /**
     * Inicia sesión o vincula un dispositivo mediante código / QR rápido en device_pairings.
     */
    suspend fun pairDeviceByCode(userId: String, pairingCode: String): Boolean

    /**
     * Consulta si un código PIN de Smart TV ya fue vinculado por un usuario en device_pairings.
     */
    suspend fun getPairingStatus(pairingCode: String): RemoteUser?

    /**
     * Obtiene el Sitio Histórico asociado / administrado por el usuario (Opción A).
     */
    suspend fun getSiteByOwner(userId: String): RemoteHistoricalSite?

    /**
     * Obtiene TODOS los sitios históricos de un propietario.
     * Si isAdmin es true, retorna todos los sitios activos del sistema.
     */
    suspend fun getSitesByOwnerOrAdmin(userId: String, isAdmin: Boolean): List<RemoteHistoricalSite>

    /**
     * Desactiva lógicamente un sitio histórico (is_active = false).
     * Solo el propietario o un administrador pueden hacerlo.
     */
    suspend fun deleteHistoricalSite(siteId: String): Boolean

    /**
     * Crea un nuevo Sitio Histórico asignándolo a un propietario responsable (ownerUserId).
     * Pensado para uso del Administrador.
     */
    suspend fun createHistoricalSiteForOwner(
        name: String,
        slug: String,
        siteType: String,
        shortDescription: String?,
        address: String?,
        ownerUserId: String
    ): Boolean

    /**
     * Transmite una orden de programa (gallery, public, ranking) a una Smart TV en tv_displays.
     */
    suspend fun setTvTransmissionProgram(pairingCode: String, programType: String): Boolean

    /**
     * Consulta el programa de transmisión actual ordenado remotamente para la Smart TV.
     */
    suspend fun getTvActiveProgram(pairingCode: String): String?

    /**
     * Obtiene los usuarios que pertenecen a un rol específico (ej: museum_hotel).
     */
    suspend fun getUsersByRole(role: String): List<RemoteUser>

    /**
     * Asigna un Sitio Histórico a un Usuario (Encargado / Museo / Hotel) en historical_sites.
     */
    suspend fun assignSiteOwner(siteId: String, userId: String): Boolean

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
     * Verifica si un Geo-Drop ya está en la colección o fue creado por el usuario.
     */
    suspend fun isGeoDropCollected(userId: String, geoDropId: String): Boolean


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
        siteIds: List<String>,
        ownerUserId: String? = null
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

