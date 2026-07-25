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
                // Sincronizar el mapa de guardados con los IDs reales de la colección
                savedSiteIds.clear()
                result.forEach { savedSiteIds[it.id] = true }
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
            savedSiteIds[siteId] = true // Optimistic update para respuesta inmediata en UI
            try {
                val success = repository.saveSite(userId, siteId)
                if (success) {
                    onSuccess()
                    loadCollection(userId) // Recarga para sincronizar lista
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
     * Elimina un sitio de la colección del usuario.
     * Actualiza el mapa local inmediatamente y recarga la lista.
     */
    fun removeSite(userId: String, siteId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            savedSiteIds[siteId] = false // Optimistic update
            try {
                val success = repository.removeSavedSite(userId, siteId)
                if (success) {
                    onSuccess()
                    loadCollection(userId)
                } else {
                    savedSiteIds[siteId] = true
                    _saveError.value = "No se pudo eliminar el sitio."
                }
            } catch (e: Exception) {
                savedSiteIds[siteId] = true
                android.util.Log.e("CollectionVM", "Error al eliminar sitio: ${e.message}")
                _saveError.value = "Error de red al eliminar."
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
            removeSite(userId, siteId)
        } else {
            saveSite(userId, siteId)
        }
    }

    /** Limpia el error de guardado después de mostrarlo en la UI. */
    fun clearSaveError() {
        _saveError.value = null
    }
}
