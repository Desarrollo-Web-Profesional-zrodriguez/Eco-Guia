package mx.utng.ecoguiawear.domain.repository

import kotlinx.coroutines.flow.StateFlow
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.domain.model.Waypoint

interface RadarRepository {
    val radarState: StateFlow<RadarUiState>

    fun setLinkedToPhone(linked: Boolean)
    fun startRadar()
    fun toggleRadar()
    fun toggleStealthMode()
    fun setStealthMode(enabled: Boolean)
    fun setAlerts(alerts: List<mx.utng.ecoguiawear.domain.model.AlertEntity>)
    fun setPermissions(gps: Boolean, camera: Boolean)
    fun setDistance(distance: Int)
    fun setRouteProgress(visited: Int, total: Int)
    fun simulateApproach()
    fun resetDemo()
    fun completeArrival()
    fun updateHaptics(enabled: Boolean, strength: HapticStrength)
    fun refreshNearbyTargets()
    fun setSyncTarget(id: String, name: String, lat: Double, lng: Double)
    fun setSyncRoute(title: String, waypoints: List<Waypoint>)
    fun clearActiveRoute()
    fun markRouteCompleted()
    fun dismissRouteCompleted()
    fun updateCurrentLocation(lat: Double, lng: Double)
    fun updateHeading(heading: Float)
}
