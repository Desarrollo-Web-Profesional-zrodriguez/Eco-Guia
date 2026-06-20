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
            "/eco-guia/simulate/proximity" -> {
                val distance = payload.toIntOrNull() ?: return
                repository.setDistance(distance)
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
                val enabled = payload.toBoolean()
                repository.setStealthMode(enabled)
            }
        }
    }
}
