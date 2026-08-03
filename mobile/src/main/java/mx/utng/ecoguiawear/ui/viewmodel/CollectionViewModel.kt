/**
 * Archivo: CollectionViewModel.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Gestiona la colección personal del usuario. Soporta carga, guardado,
 * eliminación y verificación de sitios históricos en user_saved_items (Neon).
 *
 * Funciones destacadas:
 * - loadCollection: Carga los elementos guardados desde Neon y sincroniza savedSiteIds.
 * - saveSite: Guarda un sitio con optimistic update inmediato en la UI.
 * - removeSite: Elimina un sitio con optimistic update y recarga la colección.
 * - toggleSave: Alterna el estado guardado/no guardado de un sitio.
 * - checkIfSaved: Verifica y actualiza el estado de un sitio concreto en savedSiteIds.
 * - savedSiteIds: Mapa reactivo siteId → Boolean para estado inmediato en la UI.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteCollectionItem
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class CollectionViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    // --- Lista de elementos guardados ---
    private val _items = mutableStateOf<List<RemoteCollectionItem>>(emptyList())
    val items: State<List<RemoteCollectionItem>> = _items

    // --- Estado de carga de la lista ---
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // --- Estado de operación de guardado/eliminación ---
    private val _saveError = mutableStateOf<String?>(null)
    val saveError: State<String?> = _saveError

    /**
     * Mapa reactivo: siteId -> true si ya está guardado por el usuario.
     * Permite que la UI muestre el botón "Guardado" / "Guardar" sin llamadas extra.
     */
    val savedSiteIds = mutableStateMapOf<String, Boolean>()

    /**
     * Carga todos los elementos de la colección del usuario desde Neon.
     */
    fun loadCollection(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _saveError.value = null
            try {
                val result = repository.getUserCollection(userId)
                _items.value = result
                // Sincronizar el mapa de guardados con los IDs reales del sitio y de la colección
                savedSiteIds.clear()
                result.forEach { item ->
                    savedSiteIds[item.id] = true
                    item.rawId?.let { rawId ->
                        if (rawId.isNotBlank()) {
                            savedSiteIds[rawId] = true
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CollectionVM", "Error al cargar colección: ${e.message}")
                _saveError.value = "No se pudo cargar la colección."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Verifica si un sitio específico ya está guardado y actualiza el mapa local.
     * Útil al abrir el detalle de un sitio desde ExplorationScreen.
     */
    fun checkIfSaved(userId: String, siteId: String) {
        viewModelScope.launch {
            try {
                val saved = repository.isSiteSaved(userId, siteId)
                savedSiteIds[siteId] = saved
            } catch (e: Exception) {
                android.util.Log.e("CollectionVM", "Error al verificar guardado: ${e.message}")
            }
        }
    }

    /**
     * Guarda un sitio histórico en la colección del usuario.
     * Actualiza el mapa local inmediatamente (optimistic update) y recarga la lista.
     */
    fun saveSite(userId: String, siteId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            savedSiteIds[siteId] = true // State reactivo (Signal-like update instantáneo)
            try {
                val success = repository.saveSite(userId, siteId)
                if (success) {
                    loadCollection(userId)
                    onSuccess()
                } else {
                    savedSiteIds[siteId] = false
                    _saveError.value = "No se pudo guardar el sitio."
                }
            } catch (e: Exception) {
                savedSiteIds[siteId] = false
                android.util.Log.e("CollectionVM", "Error al guardar sitio: ${e.message}")
                _saveError.value = "Error de red al guardar."
            }
        }
    }

    /**
     * Elimina un elemento de la colección del usuario.
     * Si el usuario es el autor del GeoDrop, realiza un borrado definitivo en cascada de la base de datos.
     * De lo contrario, solo elimina el elemento de la colección del usuario.
     */
    fun removeItem(userId: String, item: RemoteCollectionItem, onSuccess: () -> Unit = {}) {
        val itemId = item.id
        viewModelScope.launch {
            savedSiteIds[itemId] = false // Optimistic update del mapa
            val previousItems = _items.value
            _items.value = previousItems.filter { it.id != itemId } // Actualización inmediata de la lista UI
            try {
                val authorId = item.authorId
                val isAuthor = !authorId.isNullOrBlank() && 
                    authorId.trim().equals(userId.trim(), ignoreCase = true)
                val targetId = item.rawId ?: item.id
                
                android.util.Log.d("CollectionVM", "Eliminando -> item.id=${item.id}, targetId=$targetId, type=${item.type}, isAuthor=$isAuthor, authorId=$authorId, userId=$userId")

                val success = if (item.type == "site") {
                    val deletedSite = repository.deleteHistoricalSite(targetId)
                    repository.removeSavedSite(userId, itemId)
                    deletedSite || repository.removeSavedSite(userId, itemId)
                } else if (isAuthor && item.type == "photo") {
                    val mediaUrl = item.mediaUrl
                    val deleted = repository.deleteGeoDrop(targetId, userId, mediaUrl)
                    if (deleted && !mediaUrl.isNullOrBlank()) {
                        try {
                            val storageRepo = mx.utng.ecoguiawear.data.remote.FirebaseStorageRepository()
                            storageRepo.deleteImageFromUrl(mediaUrl)
                        } catch (fe: Exception) {
                            android.util.Log.w("CollectionVM", "No se pudo borrar de Firebase Storage: ${fe.message}")
                        }
                    }
                    deleted
                } else if (isAuthor && item.type == "route") {
                    val deletedRoute = repository.deleteRoute(targetId)
                    repository.removeSavedSite(userId, itemId)
                    deletedRoute
                } else {
                    repository.removeSavedSite(userId, itemId)
                }
                
                if (success) {
                    onSuccess()
                } else {
                    savedSiteIds[itemId] = true
                    _items.value = previousItems // Revertir si falla en la BD
                    _saveError.value = "No se pudo eliminar el elemento."
                }
            } catch (e: Exception) {
                savedSiteIds[itemId] = true
                _items.value = previousItems // Revertir si hay error de red
                android.util.Log.e("CollectionVM", "Error al eliminar elemento: ${e.message}")
                _saveError.value = "Error de red al eliminar."
            }
        }
    }

    fun removeSite(userId: String, siteId: String, onSuccess: () -> Unit = {}) {
        savedSiteIds[siteId] = false // Actualización optimista instantánea para la UI
        val dummyItem = _items.value.firstOrNull { it.id == siteId || it.rawId == siteId }
        if (dummyItem != null) {
            removeItem(userId, dummyItem, onSuccess)
        } else {
            viewModelScope.launch {
                try {
                    val success = repository.removeSavedSite(userId, siteId)
                    if (success) {
                        onSuccess()
                    } else {
                        savedSiteIds[siteId] = true
                    }
                } catch (e: Exception) {
                    savedSiteIds[siteId] = true
                    android.util.Log.e("CollectionVM", "Error al remover sitio: ${e.message}")
                }
            }
        }
    }

    /**
     * Alterna el estado guardado/no guardado de un sitio (toggle).
     * Conveniente para llamar desde el botón de la UI sin lógica condicional.
     */
    fun toggleSave(userId: String, siteId: String) {
        val currentlySaved = savedSiteIds[siteId] == true
        if (currentlySaved) {
            savedSiteIds[siteId] = false
            removeSite(userId, siteId)
        } else {
            savedSiteIds[siteId] = true
            saveSite(userId, siteId)
        }
    }

    /** Limpia el error de guardado después de mostrarlo en la UI. */
    fun clearSaveError() {
        _saveError.value = null
    }
}
