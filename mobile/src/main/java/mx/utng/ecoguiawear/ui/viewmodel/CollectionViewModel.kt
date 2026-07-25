/**
 * Archivo: CollectionViewModel.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Gestiona la colección de elementos guardados del usuario.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
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

    private val _items = mutableStateOf<List<RemoteCollectionItem>>(emptyList())
    val items: State<List<RemoteCollectionItem>> = _items

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun loadCollection(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _items.value = repository.getUserCollection(userId)
            } catch (e: Exception) {
                android.util.Log.e("CollectionVM", "Error al cargar colección: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
