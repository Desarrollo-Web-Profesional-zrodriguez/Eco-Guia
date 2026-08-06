/**
 * Archivo: AuthViewModel.kt
 *
 * Gestiona el estado de autenticación de usuarios, persistencia de sesión local, verificación
 * en dos pasos con códigos OTP por correo, cálculo de roles (SuperAdmin, Moderador, Museo),
 * recuperación de cuenta y obtención de estadísticas del perfil.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.data.remote.EmailService

/**
 * Representa los estados del flujo de autenticación y registro de usuarios.
 */
sealed class AuthState {
    /** Estado inicial sin operaciones activas. */
    object Idle : AuthState()
    /** Operación asíncrona de autenticación o verificación en progreso. */
    object Loading : AuthState()
    /** Autenticación satisfactoria con datos del usuario remoto. */
    data class Success(val user: RemoteUser) : AuthState()
    /** Ocurrió un error en la autenticación o conexión. */
    data class Error(val message: String) : AuthState()
    /** Esperando la confirmación del código de 6 dígitos enviado por correo electrónico. */
    data class AwaitingVerification(val name: String, val email: String, val passwordHash: String, val expectedOtp: String) : AuthState()
    /** Esperando confirmación del OTP para reseteo de clave. */
    data class AwaitingPasswordReset(val email: String, val expectedOtp: String) : AuthState()
    /** Esperando el ingreso de la nueva contraseña. */
    data class AwaitingNewPassword(val email: String) : AuthState()
    /** Registro de cuenta completado exitosamente. */
    object Registered : AuthState()
    /** Contraseña restablecida exitosamente. */
    object PasswordResetSuccess : AuthState()
}

/**
 * ViewModel que controla la autenticación, inicio de sesión, registro con verificación OTP y estadísticas de perfil.
 *
 * @param repository Repositorio de datos para operaciones remotas de usuarios.
 * @param emailService Servicio de correos transaccionales para envío de OTP y recuperación.
 */
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
     * Inicializa el almacenamiento de sesión persistente con SharedPreferences y restaura la sesión previa si existe.
     *
     * @param context Contexto de la aplicación Android.
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
    var notificationViewModel: NotificationViewModel? = null

    /**
     * Configura el ViewModel de notificaciones para ser usado por este AuthViewModel.
     *
     * @param nv Instancia de [NotificationViewModel].
     */
    fun initNotifications(nv: NotificationViewModel) {
        notificationViewModel = nv
    }

    /**
     * Obtiene el usuario actualmente autenticado si el estado es [AuthState.Success].
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
     * Intenta autenticar al usuario con sus credenciales.
     *
     * @param email Correo electrónico registrado.
     * @param password_hash Hash SHA-256 o contraseña ingresada.
     */
    fun login(email: String, passwordRaw: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val hashedPassword = hashPassword(passwordRaw)
            val user = repository.login(email, hashedPassword)
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
     * Inicia el proceso de registro generando un código OTP de 6 dígitos y enviándolo por correo electrónico.
     *
     * @param name Nombre o alias del usuario.
     * @param email Correo electrónico a registrar.
     * @param password_hash Contraseña cifrada del usuario.
     */
    fun register(name: String, email: String, passwordRaw: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Generar código OTP de 6 dígitos
            val otp = (100000..999999).random().toString()
            val hashedPassword = hashPassword(passwordRaw)
            
            val sent = emailService.sendOtpEmail(email, name, otp)
            if (sent) {
                _authState.value = AuthState.AwaitingVerification(name, email, hashedPassword, otp)
                notificationViewModel?.showNotification("Código enviado a $email", NotificationType.SUCCESS)
            } else {
                _authState.value = AuthState.Error("No se pudo enviar el código de verificación al correo.")
                notificationViewModel?.showNotification("Error al enviar correo", NotificationType.ERROR)
            }
        }
    }

    /**
     * Valida el código OTP ingresado por el usuario y finaliza la creación de la cuenta en caso afirmativo.
     *
     * @param enteredOtp Código de 6 dígitos introducido en la interfaz.
     */
    fun verifyOtp(enteredOtp: String) {
        val currentState = authState.value
        if (currentState is AuthState.AwaitingVerification) {
            if (enteredOtp == currentState.expectedOtp) {
                viewModelScope.launch {
                    _authState.value = AuthState.Loading
                    val success = repository.register(currentState.name, currentState.email, currentState.passwordHash)
                    if (success) {
                        _authState.value = AuthState.Registered
                        notificationViewModel?.showNotification("¡Cuenta creada exitosamente!", NotificationType.SUCCESS)
                    } else {
                        _authState.value = AuthState.Error("Error al crear la cuenta en el servidor.")
                        notificationViewModel?.showNotification("Error al registrar", NotificationType.ERROR)
                    }
                }
            } else {
                notificationViewModel?.showNotification("Código incorrecto, intenta de nuevo.", NotificationType.ERROR)
                // Mantener el estado actual para que puedan volver a intentarlo
                _authState.value = currentState
            }
        }
    }

    /**
     * Actualiza el nombre y la biografía del perfil del usuario actual.
     *
     * @param newName Nuevo nombre a persistir.
     * @param newBio Nueva biografía opcional.
     */
    fun updateProfile(newName: String, newBio: String? = null) {
        val user = currentUser ?: return
        viewModelScope.launch {
            val success = repository.updateUser(user.id, newName, newBio)
            if (success) {
                val updatedUser = user.copy(displayName = newName, bio = newBio)
                saveSessionLocally(updatedUser)
                _authState.value = AuthState.Success(updatedUser)
                notificationViewModel?.showNotification("Perfil actualizado con éxito.", NotificationType.SUCCESS)
            } else {
                notificationViewModel?.showNotification("Error al actualizar el perfil.", NotificationType.ERROR)
            }
        }
    }

    /**
     * Envía un correo de recuperación con código OTP de 6 dígitos al usuario usando Brevo.
     *
     * @param email Correo electrónico destinatario.
     * @param onSuccess Callback ejecutado si el envío fue exitoso.
     * @param onError Callback ejecutado si ocurrió un fallo al enviar.
     */
    fun sendRecoveryEmail(email: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val otp = (100000..999999).random().toString()
            val success = emailService.sendPasswordRecoveryEmail(email, otp)
            if (success) {
                _authState.value = AuthState.AwaitingPasswordReset(email, otp)
                notificationViewModel?.showNotification("Código de verificación enviado a $email", NotificationType.SUCCESS)
                onSuccess()
            } else {
                _authState.value = AuthState.Error("No se pudo enviar el correo de recuperación.")
                notificationViewModel?.showNotification("Error al enviar el correo de recuperación", NotificationType.ERROR)
                onError()
            }
        }
    }

    /**
     * Verifica el código OTP de recuperación para avanzar al ingreso de la nueva contraseña.
     */
    fun verifyPasswordResetOtp(enteredOtp: String) {
        val currentState = authState.value
        if (currentState is AuthState.AwaitingPasswordReset) {
            if (enteredOtp == currentState.expectedOtp) {
                _authState.value = AuthState.AwaitingNewPassword(currentState.email)
                notificationViewModel?.showNotification("Código verificado. Ingresa tu nueva contraseña.", NotificationType.SUCCESS)
            } else {
                notificationViewModel?.showNotification("Código incorrecto, verifica tu correo.", NotificationType.ERROR)
            }
        }
    }

    /**
     * Guarda la nueva contraseña (encriptada con SHA-256) en la base de datos remota.
     */
    fun confirmNewPassword(newPasswordRaw: String, onSuccess: () -> Unit) {
        val currentState = authState.value
        val email = if (currentState is AuthState.AwaitingNewPassword) currentState.email else currentUser?.email
        
        if (email.isNullOrEmpty()) {
            notificationViewModel?.showNotification("Sesión o correo no válido.", NotificationType.ERROR)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val hashedPassword = hashPassword(newPasswordRaw)
            val success = repository.resetPassword(email, hashedPassword)
            if (success) {
                logout()
                notificationViewModel?.showNotification("¡Contraseña actualizada con éxito! Inicia sesión de nuevo.", NotificationType.SUCCESS)
                onSuccess()
            } else {
                logout()
                notificationViewModel?.showNotification("Error al guardar la nueva contraseña. Inicia sesión de nuevo.", NotificationType.ERROR)
                onSuccess()
            }
        }
    }

    /**
     * Cierra la sesión del usuario en el teléfono móvil sin afectar las Smart TVs vinculadas.
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
     * Elimina definitivamente el usuario actual y todas sus referencias en cascada en Neon PostgreSQL.
     */
    fun deleteAccountPermanently(onComplete: (Boolean) -> Unit) {
        val user = currentUser ?: return onComplete(false)
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = repository.deleteUserCascade(user.id)
            if (success) {
                clearSessionLocally()
                _authState.value = AuthState.Idle
            } else {
                _authState.value = AuthState.Success(user)
            }
            onComplete(success)
        }
    }

    /**
     * Carga las estadísticas reales del usuario desde la base de datos (GeoDrops pertenecientes y Colección).
     */
    fun fetchUserStats() {
        val user = currentUser ?: return
        viewModelScope.launch {
            try {
                val repoImpl = repository as? EcoGuiaRepositoryImpl
                var authoredGeoDropsCount = 0
                if (repoImpl != null) {
                    val query = "SELECT COUNT(*) as count FROM geo_drops WHERE author_id::text = $1"
                    val res = repoImpl.neonClient.executeQuery<Map<String, String>>(query, listOf(user.id))
                    authoredGeoDropsCount = res.firstOrNull()?.get("count")?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                }

                val collection = repository.getUserCollection(user.id)
                val savedCount = collection.size

                val capsules = if (authoredGeoDropsCount > 0) authoredGeoDropsCount else collection.count { it.authorId == user.id || it.id.startsWith("author_") }

                _capsulesCount.value = capsules
                _savedItemsCount.value = savedCount

                val totalScore = (capsules * 2) + savedCount
                val level = when {
                    totalScore >= 15 -> "Nivel 4 - Guardián del Patrimonio"
                    totalScore >= 8  -> "Nivel 3 - Curador Comunitario"
                    totalScore >= 3  -> "Nivel 2 - Explorador Activo"
                    else             -> "Nivel 1 - Turista Reciente"
                }
                
                _explorerLevel.value = level
            } catch (e: Exception) {
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

    /**
     * Aplica el hash de seguridad SHA-256 a la contraseña ingresada.
     */
    private fun hashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password
        }
    }

    private fun String?.isNull_or_blank_helper(): Boolean = this == null || this.trim().isEmpty()
}

