/**
 * Archivo: EcoGuiaRepository.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Interfaz que define las operaciones de acceso a datos para la aplicación Eco-Guía.
 */

package mx.utng.ecoguia.shared.domain.repository

import mx.utng.ecoguia.shared.domain.model.*

interface EcoGuiaRepository {
    /**
     * Obtiene la lista de todos los sitios históricos activos.
     */
    suspend fun getHistoricalSites(): List<RemoteHistoricalSite>

    /**
     * Obtiene la lista de todas las rutas turísticas activas.
     */
    suspend fun getRoutes(): List<RemoteRoute>

    /**
     * Obtiene la lista de cápsulas de información (Geo-Drops) aprobadas.
     */
    suspend fun getGeoDrops(): List<RemoteGeoDrop>

    /**
     * Registra una nueva cápsula de información en la nube.
     */
    suspend fun createGeoDrop(title: String, description: String, lat: Double, lng: Double): Boolean

    /**
     * Busca sitios históricos cercanos a una ubicación específica.
     */
    suspend fun getNearbySites(lat: Double, lng: Double, radiusM: Int = 250): List<RemoteHistoricalSite>

    /**
     * Obtiene el catálogo de categorías para los sitios.
     */
    suspend fun getSiteCategories(): List<RemoteCategory>

    /**
     * Registra un nuevo sitio histórico en la nube.
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
     * Prueba la conexión con el servidor de base de datos.
     */
    suspend fun testConnection(): String

    /**
     * Obtiene los elementos guardados por un usuario desde user_saved_items.
     */
    suspend fun getUserCollection(userId: String): List<RemoteCollectionItem>

    /**
     * Guarda un sitio histórico en la colección del usuario.
     * Usa ON CONFLICT DO NOTHING para evitar duplicados.
     * Retorna verdadero si la operación fue exitosa.
     */
    suspend fun saveSite(userId: String, siteId: String): Boolean

    /**
     * Elimina un sitio guardado de la colección del usuario.
     * Retorna verdadero si se eliminó al menos un registro.
     */
    suspend fun removeSavedSite(userId: String, siteId: String): Boolean

    /**
     * Verifica si un sitio ya fue guardado por el usuario.
     * Retorna verdadero si existe en user_saved_items.
     */
    suspend fun isSiteSaved(userId: String, siteId: String): Boolean
}
