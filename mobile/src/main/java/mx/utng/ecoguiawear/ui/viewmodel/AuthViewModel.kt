/**
 * Archivo: AuthViewModel.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
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
    private val repository: EcoGuiaRepositoryImpl = EcoGuiaRepositoryImpl()
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

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
            } else {
                _authState.value = AuthState.Error("Credenciales incorrectas o usuario inactivo.")
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
            } else {
                _authState.value = AuthState.Error("Error al crear la cuenta. El correo podría ya estar registrado.")
            }
        }
    }

    /**
     * Reinicia el estado de autenticación a Idle.
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
