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
            val waypointsStr = waypoints.joinToString(";") { (name, coords) ->
                "id|${name}|${coords.first}|${coords.second}"
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

    companion object {
        const val PATH_SYNC_TARGET = "/eco-guia/sync/target"
        const val PATH_SYNC_ROUTE = "/eco-guia/sync/route"
    }
}
