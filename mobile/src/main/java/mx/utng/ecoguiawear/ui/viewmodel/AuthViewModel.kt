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

import mx.utng.ecoguiawear.data.remote.EmailService

class AuthViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl(),
    private val emailService: EmailService = EmailService()
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // Stats para el Perfil
    private val _capsulesCount = mutableStateOf(0)
    val capsulesCount: State<Int> = _capsulesCount

    private val _savedItemsCount = mutableStateOf(0)
    val savedItemsCount: State<Int> = _savedItemsCount

    private val _explorerLevel = mutableStateOf("Nivel 1 - Turista Reciente")
    val explorerLevel: State<String> = _explorerLevel

    private var sharedPreferences: android.content.SharedPreferences? = null

    /**
     * Inicializa el almacenamiento de sesión persistente con SharedPreferences.
     */
    fun initSessionPersistence(context: android.content.Context) {
        sharedPreferences = context.getSharedPreferences("user_session_prefs", android.content.Context.MODE_PRIVATE)
        val savedUserId = sharedPreferences?.getString("saved_user_id", null)
        val savedUserEmail = sharedPreferences?.getString("saved_user_email", null)
        val savedUserName = sharedPreferences?.getString("saved_user_name", null)
        val savedUserRole = sharedPreferences?.getString("saved_user_role", null)

        if (!savedUserId.isNull_or_blank_helper() && !savedUserEmail.isNull_or_blank_helper()) {
            val restoredUser = RemoteUser(
                id = savedUserId!!,
                email = savedUserEmail!!,
                displayName = savedUserName ?: "Usuario",
                role = savedUserRole ?: "visitor"
            )
            _authState.value = AuthState.Success(restoredUser)
        }
    }

    private fun saveSessionLocally(user: RemoteUser) {
        sharedPreferences?.edit()?.apply {
            putString("saved_user_id", user.id)
            putString("saved_user_email", user.email)
            putString("saved_user_name", user.displayName)
            putString("saved_user_role", user.role)
            apply()
        }
    }

    private fun clearSessionLocally() {
        sharedPreferences?.edit()?.clear()?.apply()
    }

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
     * Determina si el usuario autenticado es Super Admin (Desarrollador / Administrador total).
     */
    val isSuperAdmin: Boolean
        get() = currentUser?.role?.lowercase() in listOf("super_admin", "admin", "administrator")

    /**
     * Determina si el usuario autenticado tiene rol de Moderador, Gestor Cultural o Museo.
     */
    val isModerator: Boolean
        get() = isSuperAdmin || isMuseumHotel || currentUser?.role?.lowercase() in listOf("moderator", "mod")

    /**
     * Determina si la cuenta tiene asignado el rol de Museo / Hotel / Establecimiento (cualquier correo).
     */
    val isMuseumHotel: Boolean
        get() = currentUser?.role?.lowercase() in listOf("museum_hotel", "museum", "hotel")

    /**
     * Determina si el usuario tiene privilegios administrativos o de gestión (SuperAdmin, Moderador o Museo).
     */
    val isAdmin: Boolean
        get() = isSuperAdmin || isModerator || isMuseumHotel

    /**
     * Determina si es un usuario normal (visitante/turista).
     */
    val isUser: Boolean
        get() = currentUser != null

    /**
     * Intenta iniciar sesión con el correo y contraseña proporcionados.
     */
    fun login(email: String, password_hash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = repository.login(email, password_hash)
            if (user != null) {
                saveSessionLocally(user)
                _authState.value = AuthState.Success(user)
                notificationViewModel?.showNotification("Bienvenido, ${user.displayName}", NotificationType.SUCCESS)
            } else {
                _authState.value = AuthState.Error("Credenciales incorrectas o usuario inactivo.")
                notificationViewModel?.showNotification("Credenciales no válidas", NotificationType.ERROR)
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
                val updatedUser = user.copy(displayName = newName)
                saveSessionLocally(updatedUser)
                _authState.value = AuthState.Success(updatedUser)
                notificationViewModel?.showNotification("Perfil actualizado con éxito.", NotificationType.SUCCESS)
            } else {
                notificationViewModel?.showNotification("Error al actualizar el perfil.", NotificationType.ERROR)
            }
        }
    }

    /**
     * Envía un correo de recuperación al usuario usando Brevo.
     */
    fun sendRecoveryEmail(email: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = emailService.sendPasswordRecoveryEmail(email, "https://ecoguia.com/reset?token=demo123")
            if (success) {
                notificationViewModel?.showNotification("Correo de recuperación enviado a $email", NotificationType.SUCCESS)
                onSuccess()
            } else {
                notificationViewModel?.showNotification("Error al enviar el correo", NotificationType.ERROR)
                onError()
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual y reinicia el estado.
     */
    fun logout() {
        clearSessionLocally()
        _authState.value = AuthState.Idle
        _capsulesCount.value = 0
        _savedItemsCount.value = 0
        _explorerLevel.value = "Nivel 1 - Turista Reciente"
        notificationViewModel?.showNotification("Sesión cerrada", NotificationType.INFO)
    }

    /**
     * Carga las estadísticas reales del usuario desde la base de datos (Colección).
     */
    fun fetchUserStats() {
        val user = currentUser ?: return
        viewModelScope.launch {
            try {
                val collection = repository.getUserCollection(user.id)
                val capsules = collection.count { it.id.startsWith("author_") }
                val saved = collection.size - capsules

                _capsulesCount.value = capsules
                _savedItemsCount.value = saved

                _explorerLevel.value = when {
                    capsules >= 20 -> "Nivel 4 - Guardián del Patrimonio"
                    capsules >= 10 -> "Nivel 3 - Curador Comunitario"
                    capsules >= 3 -> "Nivel 2 - Explorador Activo"
                    else -> "Nivel 1 - Turista Reciente"
                }
            } catch (e: Exception) {
                // Si falla la red, mantenemos los valores en 0
                android.util.Log.e("AuthViewModel", "Error fetching stats: ${e.message}")
            }
        }
    }

    /**
     * Reinicia el estado de autenticación a Idle.
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun String?.isNull_or_blank_helper(): Boolean = this == null || this.trim().isEmpty()
}

