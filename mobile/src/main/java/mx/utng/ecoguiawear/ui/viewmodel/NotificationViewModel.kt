/**
 * Archivo: NotificationViewModel.kt
 *
 * Sistema centralizado para gestionar banners y avisos emergentes reactivos en la interfaz de usuario.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Categorías de avisos emergentes del sistema.
 */
enum class NotificationType {
    /** Aviso informativo regular. */
    INFO,
    /** Operación completada satisfactoriamente. */
    SUCCESS,
    /** Error en la ejecución de la acción. */
    ERROR,
    /** Advertencia o precaución. */
    WARNING
}

/**
 * Modelo de datos representativo de una notificación emergente en pantalla.
 *
 * @property message Texto descriptivo a mostrar.
 * @property type Severidad o tipo visual de la notificación.
 */
data class AppNotification(
    val message: String,
    val type: NotificationType = NotificationType.INFO
)

/**
 * ViewModel que expone el estado de los banners de aviso y gestiona su temporizador de ocultamiento automático.
 */
class NotificationViewModel : ViewModel() {

    private val _currentNotification = mutableStateOf<AppNotification?>(null)
    val currentNotification: State<AppNotification?> = _currentNotification

    /**
     * Muestra un aviso emergente en la interfaz y lo desvanece tras 3.5 segundos.
     *
     * @param message Mensaje a desplegar.
     * @param type Nivel de severidad de la alerta.
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
