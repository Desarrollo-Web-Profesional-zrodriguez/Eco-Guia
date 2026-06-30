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

class RadarViewModel(
    private val repository: RadarRepository,
    private val hapticController: HapticController,
    private val phoneMessageClient: PhoneMessageClient
) : ViewModel() {

    val state = repository.radarState

    fun pairWithPhone() {
        repository.setLinkedToPhone(true)
        pulse(HapticPulse.LINKED)
        sendPhoneEvent(PhoneMessageClient.PATH_LINKED, "linked")
    }

    fun startDemo() {
        repository.startRadar()
        pulse(HapticPulse.LINKED)
        sendPhoneEvent(PhoneMessageClient.PATH_RADAR_STATE, "demo-started")
    }

    fun toggleRadar() {
        repository.toggleRadar()
        pulse(HapticPulse.TOGGLE)
        sendPhoneEvent(PhoneMessageClient.PATH_RADAR_STATE, state.value.mode.name)
    }

    fun toggleStealthMode() {
        repository.toggleStealthMode()
        pulse(HapticPulse.TOGGLE)
    }

    fun simulateApproach() {
        val previousDistance = state.value.target.distanceMeters
        repository.simulateApproach()
        val nextDistance = state.value.target.distanceMeters

        when {
            nextDistance == 0 -> pulse(HapticPulse.ARRIVED)
            previousDistance > 20 && nextDistance <= 20 -> pulse(HapticPulse.NEARBY)
        }
    }

    fun resetDemo() {
        repository.resetDemo()
        pulse(HapticPulse.TOGGLE)
    }

    fun completeArrival() {
        repository.completeArrival()
        pulse(HapticPulse.TOGGLE)
    }

    fun openPhoneCamera() {
        sendPhoneEvent(PhoneMessageClient.PATH_OPEN_CAMERA, state.value.target.id)
    }

    fun updateHaptics(enabled: Boolean, strength: HapticStrength = state.value.hapticSettings.strength) {
        repository.updateHaptics(enabled, strength)
        if (enabled) pulse(HapticPulse.TOGGLE)
    }

    private fun pulse(type: HapticPulse) {
        val settings = state.value.hapticSettings
        if (settings.enabled) {
            hapticController.pulse(type, settings.strength)
        }
    }

    private fun sendPhoneEvent(path: String, payload: String) {
        viewModelScope.launch {
            phoneMessageClient.sendRadarEvent(path, payload)
        }
    }
}

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
