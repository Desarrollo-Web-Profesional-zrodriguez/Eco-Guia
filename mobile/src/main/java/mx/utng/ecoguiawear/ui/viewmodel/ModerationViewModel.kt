/**
 * Archivo: ModerationViewModel.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: ViewModel que gestiona la carga y la toma de decisiones sobre las cápsulas Geo-Drop
 * y reportes pendientes de moderación enviando las resoluciones (approved/rejected) a Neon PostgreSQL.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class ModerationViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private val _pendingDrops = mutableStateOf<List<RemoteGeoDrop>>(emptyList())
    val pendingDrops: State<List<RemoteGeoDrop>> = _pendingDrops

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _selectedDrop = mutableStateOf<RemoteGeoDrop?>(null)
    val selectedDrop: State<RemoteGeoDrop?> = _selectedDrop

    init {
        loadPendingContent()
    }

    /**
     * Carga la lista de cápsulas pendientes o reportadas desde la base de datos remota.
     */
    fun loadPendingContent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _pendingDrops.value = repository.getPendingGeoDrops()
            } catch (e: Exception) {
                android.util.Log.e("ModerationVM", "Error al cargar contenido: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Establece la cápsula seleccionada para revisar su detalle.
     */
    fun selectDrop(drop: RemoteGeoDrop?) {
        _selectedDrop.value = drop
    }

    /**
     * Resuelve un reporte cambiando el estado a approved o rejected en la base de datos.
     */
    fun resolveDrop(
        dropId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateGeoDropStatus(dropId, newStatus)
                if (success) {
                    loadPendingContent()
                    onSuccess()
                } else {
                    onError("No se pudo actualizar el estado del reporte.")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
