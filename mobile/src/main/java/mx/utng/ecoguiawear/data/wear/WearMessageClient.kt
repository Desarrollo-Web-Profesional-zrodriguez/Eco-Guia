/**
 * Archivo: WearMessageClient.kt
 *
 * Cliente de mensajería bidireccional mediante Google Play Services Wearable Data Layer API.
 * Permite al smartphone transmitir destinos individuales, rutas turísticas completas y progreso
 * de navegación al smartwatch Wear OS emparejado.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Cliente de sincronización con dispositivos Wear OS mediante el protocolo de mensajes de Google Play Services.
 *
 * @param context Contexto de la aplicación Android.
 */
class WearMessageClient(private val context: Context) {

    /**
     * Envía la información de un sitio histórico al reloj para sincronizar el radar.
     *
     * @param id Identificador único del sitio histórico.
     * @param name Nombre del sitio.
     * @param lat Latitud geográfica.
     * @param lng Longitud geográfica.
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
     * Envía la información de una ruta turística completa y su secuencia de waypoints al reloj.
     *
     * @param title Título descriptivo de la ruta.
     * @param waypoints Lista de pares con formato "ID|Nombre" y coordenadas (Latitud, Longitud).
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
     * Envía una señal al reloj para cancelar la ruta activa y regresar al modo de detección general.
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
     * Sincroniza el progreso actual de navegación (paradas completadas y total) con el reloj.
     *
     * @param completedCount Número de paradas visitadas.
     * @param totalStops Total de paradas que componen la ruta.
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

    /**
     * Envía una señal al reloj notificando que la ruta fue completada satisfactoriamente.
     */
    suspend fun completeRoute() {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, PATH_COMPLETE_ROUTE, "completed".toByteArray())
                    .await()
            }
            Log.d("WearMessageClient", "Señal de ruta completada enviada a ${nodes.size} nodos")
        } catch (e: Exception) {
            Log.e("WearMessageClient", "Error al enviar señal de ruta completada: ${e.message}")
        }
    }

    /**
     * Envía una alerta de sitio o ruta al reloj Wear OS.
     */
    suspend fun sendAlert(id: String, message: String, type: String = "SITE") {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            val payload = "$id|$message|$type"
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, PATH_SEND_ALERT, payload.toByteArray())
                    .await()
            }
            Log.d("WearMessageClient", "Alerta enviada a ${nodes.size} nodos Wear OS: $message")
        } catch (e: Exception) {
            Log.e("WearMessageClient", "Error al enviar alerta a Wear OS: ${e.message}")
        }
    }

    companion object {
        /** Ruta de mensaje para sincronización de un sitio individual. */
        const val PATH_SYNC_TARGET = "/eco-guia/sync/target"
        /** Ruta de mensaje para sincronización de una ruta completa con paradas. */
        const val PATH_SYNC_ROUTE = "/eco-guia/sync/route"
        /** Ruta de mensaje para cancelación de ruta en progreso. */
        const val PATH_CANCEL_ROUTE = "/eco-guia/cancel/route"
        /** Ruta de mensaje para finalización exitosa de ruta. */
        const val PATH_COMPLETE_ROUTE = "/eco-guia/complete/route"
        /** Ruta de mensaje para actualización de contador de paradas. */
        const val PATH_SYNC_PROGRESS = "/eco-guia/sync/progress"
        const val PATH_SEND_ALERT = "/eco-guia/simulate/alerts"
    }
}
