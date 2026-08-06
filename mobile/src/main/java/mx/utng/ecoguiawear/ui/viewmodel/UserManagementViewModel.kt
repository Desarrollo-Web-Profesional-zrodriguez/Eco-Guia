/**
 * Archivo: UserManagementViewModel.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: ViewModel para gestionar la lista de usuarios y la promoción/degradación de sus roles.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class UserManagementViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private val _users = mutableStateOf<List<RemoteUser>>(emptyList())
    val users: State<List<RemoteUser>> = _users

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadUsers()
    }

    /**
     * Carga la lista de usuarios desde la BD remota excluyendo al super admin.
     */
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _users.value = repository.getAllUsers()
            } catch (e: Exception) {
                android.util.Log.e("UserManagementVM", "Error al cargar usuarios: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cambia el rol de un usuario (ej: de visitor a moderator) en la BD.
     */
    fun changeRole(
        userId: String,
        newRole: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateUserRole(userId, newRole)
                if (success) {
                    loadUsers()
                    onSuccess()
                } else {
                    onError("No se pudo actualizar el rol en la base de datos.")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina definitivamente a un usuario en cascada desde la vista de administración.
     */
    fun deleteUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.deleteUserCascade(userId)
                if (success) {
                    loadUsers()
                    onSuccess()
                } else {
                    onError("No se pudo eliminar el usuario (cuenta protegida o error en la BD).")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
