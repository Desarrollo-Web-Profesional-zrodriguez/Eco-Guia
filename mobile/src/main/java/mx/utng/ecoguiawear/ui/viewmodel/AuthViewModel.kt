/**
 * Archivo: AuthViewModel.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Gestiona el estado de la autenticación del usuario y la lógica de negocio para login y registro.
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
 * Representa los diferentes estados de un proceso de autenticación.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: RemoteUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object Registered : AuthState()
}

class AuthViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    /**
     * Referencia opcional al sistema de notificaciones para disparar alertas globales.
     */
    private var notificationViewModel: NotificationViewModel? = null

    /**
     * Configura el ViewModel de notificaciones para ser usado por este AuthViewModel.
     */
    fun initNotifications(nv: NotificationViewModel) {
        notificationViewModel = nv
    }

    /**
     * Obtiene el usuario actualmente autenticado si el estado es Success.
     */
    val currentUser: RemoteUser?
        get() = (authState.value as? AuthState.Success)?.user

    /**
     * Intenta iniciar sesión con el correo y contraseña proporcionados.
     */
    fun login(email: String, password_hash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = repository.login(email, password_hash)
            if (user != null) {
                _authState.value = AuthState.Success(user)
                notificationViewModel?.showNotification("¡Bienvenido, ${user.displayName}!", NotificationType.SUCCESS)
            } else {
                _authState.value = AuthState.Error("Credenciales incorrectas o usuario inactivo.")
                notificationViewModel?.showNotification("Error de acceso: Credenciales no válidas.", NotificationType.ERROR)
            }
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos remota.
     */
    fun register(name: String, email: String, password_hash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = repository.register(name, email, password_hash)
            if (success) {
                _authState.value = AuthState.Registered
                notificationViewModel?.showNotification("Cuenta creada con éxito. Ya puedes iniciar sesión.", NotificationType.SUCCESS)
            } else {
                _authState.value = AuthState.Error("Error al crear la cuenta. El correo podría ya estar registrado.")
                notificationViewModel?.showNotification("No se pudo completar el registro.", NotificationType.ERROR)
            }
        }
    }

    /**
     * Actualiza el nombre del perfil del usuario actual.
     */
    fun updateProfile(newName: String) {
        val user = currentUser ?: return
        viewModelScope.launch {
            val success = repository.updateUser(user.id, newName)
            if (success) {
                // Actualizar estado local inmediatamente para reactividad
                val updatedUser = user.copy(displayName = newName)
                _authState.value = AuthState.Success(updatedUser)
                notificationViewModel?.showNotification("Perfil actualizado con éxito.", NotificationType.SUCCESS)
            } else {
                notificationViewModel?.showNotification("Error al actualizar el perfil.", NotificationType.ERROR)
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual y reinicia el estado.
     */
    fun logout() {
        _authState.value = AuthState.Idle
        notificationViewModel?.showNotification("Sesión cerrada.", NotificationType.INFO)
    }

    /**
     * Reinicia el estado de autenticación a Idle.
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
