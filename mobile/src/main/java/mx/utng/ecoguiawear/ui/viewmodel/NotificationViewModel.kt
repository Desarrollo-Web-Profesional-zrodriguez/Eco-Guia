/**
 * Archivo: NotificationViewModel.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Sistema centralizado para gestionar notificaciones reactivas y normalizadas en la interfaz de usuario.
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tipos de notificaciones soportadas.
 */
enum class NotificationType {
    INFO, SUCCESS, ERROR, WARNING
}

/**
 * Datos de una notificación.
 */
data class AppNotification(
    val message: String,
    val type: NotificationType = NotificationType.INFO
)

class NotificationViewModel : ViewModel() {

    private val _currentNotification = mutableStateOf<AppNotification?>(null)
    val currentNotification: State<AppNotification?> = _currentNotification

    /**
     * Muestra una notificación y la oculta automáticamente después de un tiempo.
     */
    fun showNotification(message: String, type: NotificationType = NotificationType.INFO) {
        viewModelScope.launch {
            _currentNotification.value = AppNotification(message, type)
            delay(3500) // Duración de la notificación
            if (_currentNotification.value?.message == message) {
                _currentNotification.value = null
            }
        }
    }

    /**
     * Limpia la notificación actual inmediatamente.
     */
    fun clearNotification() {
        _currentNotification.value = null
    }
}
