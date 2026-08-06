/**
 * ViewModel central de la experiencia táctil, cartográfica y háptica en Wear OS.
 *
 * Mantiene el flujo observable [state], procesa eventos de usuario en pantalla, dispara pulsos hápticos
 * y sincroniza estados hacia el teléfono móvil emparejado mediante [PhoneMessageClient].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.haptics.HapticPulse
import mx.utng.ecoguiawear.data.wear.PhoneMessageClient
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.repository.RadarRepository

/**
 * ViewModel principal del smartwatch.
 *
 * @param repository Repositorio de acceso a datos y estado del radar.
 * @param hapticController Controlador del motor de vibración háptica.
 * @param phoneMessageClient Cliente de mensajería Wearable hacia el teléfono.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class RadarViewModel(
    private val repository: RadarRepository,
    private val hapticController: HapticController,
    private val phoneMessageClient: PhoneMessageClient
) : ViewModel() {

    /** Flujo de estado inmutable observado por los composables de la interfaz. */
    val state = repository.radarState

    /** Confirma el emparejamiento manual con el teléfono móvil. */
    fun pairWithPhone() {
        repository.setLinkedToPhone(true)
        pulse(HapticPulse.LINKED)
        sendPhoneEvent(PhoneMessageClient.PATH_LINKED, "linked")
    }

    /** Inicia el radar en modo demostración y actualiza sitios históricos desde Neon DB. */
    fun startDemo() {
        repository.startRadar()
        repository.refreshNearbyTargets()
        pulse(HapticPulse.LINKED)
        sendPhoneEvent(PhoneMessageClient.PATH_RADAR_STATE, "demo-started")
    }

    /** Solicita una recarga explícita de sitios cercanos desde el backend en la nube. */
    fun refreshFromCloud() {
        repository.refreshNearbyTargets()
    }

    /** Alterna el estado activo/pausa del radar emitiendo un pulso táctil. */
    fun toggleRadar() {
        repository.toggleRadar()
        pulse(HapticPulse.TOGGLE)
        sendPhoneEvent(PhoneMessageClient.PATH_RADAR_STATE, state.value.mode.name)
    }

    /** Selecciona el siguiente objetivo descubierto automáticamente por GPS. */
    fun selectNextAutoTarget() {
        repository.selectNextAutoTarget()
        pulse(HapticPulse.TOGGLE)
    }

    /** Selecciona el objetivo anterior en la lista de auto-descubrimiento. */
    fun selectPreviousAutoTarget() {
        repository.selectPreviousAutoTarget()
        pulse(HapticPulse.TOGGLE)
    }

    /** Alterna el modo discreto (apagado de pantalla para guiado exclusivo por vibración). */
    fun toggleStealthMode() {
        repository.toggleStealthMode()
        pulse(HapticPulse.TOGGLE)
    }

    /** Simula una aproximación secuencial hacia el objetivo para pruebas. */
    fun simulateApproach() {
        val previousDistance = state.value.target.distanceMeters
        repository.simulateApproach()
        val nextDistance = state.value.target.distanceMeters

        when {
            nextDistance == 0 -> pulse(HapticPulse.ARRIVED)
            previousDistance > 20 && nextDistance <= 20 -> pulse(HapticPulse.NEARBY)
        }
    }

    /** Restablece los datos de demostración a su estado inicial. */
    fun resetDemo() {
        repository.resetDemo()
        pulse(HapticPulse.TOGGLE)
    }

    /** Registra la llegada al objetivo y avanza a la siguiente parada turística. */
    fun completeArrival() {
        repository.completeArrival()
        pulse(HapticPulse.TOGGLE)
    }

    /** Cierra el diálogo de ruta completada. */
    fun dismissRouteCompleted() {
        repository.dismissRouteCompleted()
        pulse(HapticPulse.TOGGLE)
    }

    /** Solicita al teléfono móvil abrir el visor de cámara o realidad aumentada del sitio. */
    fun openPhoneCamera() {
        sendPhoneEvent(PhoneMessageClient.PATH_OPEN_CAMERA, state.value.target.id)
    }

    /**
     * Actualiza las preferencias de vibración háptica del reloj.
     *
     * @param enabled Verdadero para habilitar respuesta háptica.
     * @param strength Nivel de intensidad seleccionado ([HapticStrength]).
     */
    fun updateHaptics(enabled: Boolean, strength: HapticStrength = state.value.hapticSettings.strength) {
        repository.updateHaptics(enabled, strength)
        if (enabled) pulse(HapticPulse.TOGGLE)
    }

    /**
     * Elimina una alerta del historial por identificador.
     *
     * @param id Identificador de la alerta.
     */
    fun deleteAlert(id: String) {
        repository.deleteAlert(id)
        pulse(HapticPulse.TOGGLE)
    }

    /** Limpia todo el historial de alertas acumuladas. */
    fun clearAllAlerts() {
        repository.clearAllAlerts()
        pulse(HapticPulse.TOGGLE)
    }

    /**
     * Emite una vibración utilizando el controlador háptico si la opción está habilitada.
     *
     * @param type Tipo de pulso sensorial a ejecutar.
     */
    private fun pulse(type: HapticPulse) {
        val settings = state.value.hapticSettings
        if (settings.enabled) {
            hapticController.pulse(type, settings.strength)
        }
    }

    /**
     * Envía asíncronamente un mensaje de evento hacia el teléfono conectado.
     *
     * @param path Endpoint de mensajería Wearable.
     * @param payload Cuerpo del mensaje en texto plano.
     */
    private fun sendPhoneEvent(path: String, payload: String) {
        viewModelScope.launch {
            phoneMessageClient.sendRadarEvent(path, payload)
        }
    }
}

/**
 * Fábrica para instanciar [RadarViewModel] inyectando el repositorio y clientes periféricos.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class RadarViewModelFactory(
    private val repository: RadarRepository,
    private val hapticController: HapticController,
    private val phoneMessageClient: PhoneMessageClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RadarViewModel(repository, hapticController, phoneMessageClient) as T
    }
}
