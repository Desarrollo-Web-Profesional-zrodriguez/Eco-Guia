package mx.utng.ecoguiawear.data.wear

import android.content.Context
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.domain.repository.RadarRepository

class WearMessageListener(
    private val repository: RadarRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun onMessageReceived(event: MessageEvent) {
        val payload = String(event.data)
        when (event.path) {
            "/eco-guia/simulate/link" -> {
                val linked = payload.toBoolean()
                repository.setLinkedToPhone(linked)
            }
            "/eco-guia/simulate/proximity" -> {
                if (payload == "step") {
                    repository.simulateApproach()
                } else {
                    val distance = payload.toIntOrNull() ?: return
                    repository.setDistance(distance)
                }
            }
            "/eco-guia/simulate/route" -> {
                val parts = payload.split("/")
                if (parts.size == 2) {
                    val visited = parts[0].toIntOrNull() ?: return
                    val total = parts[1].toIntOrNull() ?: return
                    repository.setRouteProgress(visited, total)
                }
            }
            "/eco-guia/simulate/stealth" -> {
                val enabled = when(payload) {
                    "1" -> true
                    "0" -> false
                    else -> payload.toBoolean()
                }
                repository.setStealthMode(enabled)
            }
            "/eco-guia/simulate/alerts" -> {
                // Payload: "id|msg|type;id|msg|type"
                val alertList = payload.split(";").filter { it.isNotBlank() }.map {
                    val parts = it.split("|")
                    mx.utng.ecoguiawear.domain.model.AlertEntity(
                        id = parts.getOrNull(0) ?: "0",
                        message = parts.getOrNull(1) ?: "",
                        type = parts.getOrNull(2) ?: "INFO",
                        timestamp = System.currentTimeMillis()
                    )
                }
                repository.setAlerts(alertList)
            }
            "/eco-guia/simulate/permissions" -> {
                val parts = payload.split("/")
                if (parts.size == 2) {
                    val gps = parts[0].toBoolean()
                    val camera = parts[1].toBoolean()
                    repository.setPermissions(gps, camera)
                }
            }
        }
    }
}
