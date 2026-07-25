package mx.utng.ecoguiawear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearMessageClient(private val context: Context) {

    /**
     * Envía la información de un sitio histórico al reloj para sincronizar el radar.
     */
    suspend fun syncTarget(id: String, name: String, lat: Double, lng: Double) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            val payload = "$id|$name|$lat|$lng"
            
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, PATH_SYNC_TARGET, payload.toByteArray())
                    .await()
            }
            Log.d("WearMessageClient", "Sincronización enviada (Target): $name ($lat, $lng) a ${nodes.size} nodos")
        } catch (e: Exception) {
            Log.e("WearMessageClient", "Error al sincronizar con el reloj: ${e.message}")
        }
    }

    /**
     * Envía la información de una ruta completa al reloj.
     */
    suspend fun syncRoute(title: String, waypoints: List<Pair<String, Pair<Double, Double>>>) {
        try {
            val nodes = com.google.android.gms.wearable.Wearable.getNodeClient(context).connectedNodes.await()
            // Formato: Titulo|ID1,Name1,Lat1,Lng1;ID2,Name2,Lat2,Lng2
            val waypointsStr = waypoints.joinToString(";") { (idAndName, coords) ->
                val parts = idAndName.split("|")
                val id = if (parts.size > 1) parts[0] else "0"
                val name = if (parts.size > 1) parts[1] else idAndName
                "$id|$name|${coords.first}|${coords.second}"
            }
            val payload = "$title|$waypointsStr"
            
            nodes.forEach { node ->
                com.google.android.gms.wearable.Wearable.getMessageClient(context)
                    .sendMessage(node.id, PATH_SYNC_ROUTE, payload.toByteArray())
                    .await()
            }
            Log.d("WearMessageClient", "Sincronización enviada (Route): $title con ${waypoints.size} puntos a ${nodes.size} nodos")
        } catch (e: Exception) {
            Log.e("WearMessageClient", "Error al sincronizar ruta: ${e.message}")
        }
    }

    /**
     * Envía una señal al reloj para cancelar la ruta activa y volver al modo de detección automática de Geo-Drops.
     */
    suspend fun cancelRoute() {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, PATH_CANCEL_ROUTE, "cancel".toByteArray())
                    .await()
            }
            Log.d("WearMessageClient", "Señal de cancelación de ruta enviada a ${nodes.size} nodos")
        } catch (e: Exception) {
            Log.e("WearMessageClient", "Error al enviar cancelación de ruta: ${e.message}")
        }
    }

    /**
     * Sincroniza el progreso (paradas completadas y total) con el reloj.
     */
    suspend fun syncRouteProgress(completedCount: Int, totalStops: Int) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            val payload = "$completedCount|$totalStops"
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, PATH_SYNC_PROGRESS, payload.toByteArray())
                    .await()
            }
            Log.d("WearMessageClient", "Progreso de ruta enviado al reloj: $completedCount/$totalStops")
        } catch (e: Exception) {
            Log.e("WearMessageClient", "Error al enviar progreso a Wear OS: ${e.message}")
        }
    }

    companion object {
        const val PATH_SYNC_TARGET = "/eco-guia/sync/target"
        const val PATH_SYNC_ROUTE = "/eco-guia/sync/route"
        const val PATH_CANCEL_ROUTE = "/eco-guia/cancel/route"
        const val PATH_SYNC_PROGRESS = "/eco-guia/sync/progress"
    }
}
