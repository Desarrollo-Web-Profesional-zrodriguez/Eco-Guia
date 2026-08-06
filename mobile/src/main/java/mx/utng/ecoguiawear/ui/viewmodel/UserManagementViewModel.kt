/**
 * Archivo: UserManagementViewModel.kt
 *
 * ViewModel para el panel de administración encargado de consultar la lista de usuarios registrados
 * y gestionar la asignación, promoción o degradación de roles y permisos.
 *
 * @since 2026-08-05
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

/**
 * ViewModel que expone el catálogo de usuarios del sistema y permite a los administradores modificar sus roles.
 *
 * @param repository Repositorio de datos para operaciones de administración de usuarios.
 */
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
     * Carga la lista de usuarios registrados en el sistema desde la base de datos remota.
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
     * Actualiza el rol de un usuario (ej: visitor, moderator, museum_hotel) en la base de datos remota.
     *
     * @param userId Identificador único del usuario a modificar.
     * @param newRole Clave del nuevo rol a asignar.
     * @param onSuccess Callback ejecutado al actualizar exitosamente.
     * @param onError Callback invocado si ocurre un error.
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
