/**
 * Emisor de mensajes y comandos desde el reloj inteligente hacia el teléfono móvil emparejado.
 *
 * Utiliza la API Wearable [com.google.android.gms.wearable.MessageClient] y [com.google.android.gms.wearable.CapabilityClient]
 * para enviar eventos de llegada, cambios de estado del radar o solicitudes de apertura de cámara.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Cliente de mensajería Wearable para comunicación bidireccional Reloj -> Teléfono.
 *
 * @param context Contexto de la aplicación para instanciar las APIs de Google Play Services Wearable.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class PhoneMessageClient(private val context: Context) {

    /**
     * Envía asíncronamente un mensaje a todos los nodos de teléfono conectados con la capacidad registrada.
     *
     * @param path Ruta o endpoint del mensaje Wearable.
     * @param payload Contenido en texto plano del mensaje a transmitir.
     */
    suspend fun sendRadarEvent(path: String, payload: String) {
        try {
            val capabilityInfo = Wearable.getCapabilityClient(context)
                .getCapability(PHONE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
            val nodes = capabilityInfo.nodes.ifEmpty {
                Wearable.getNodeClient(context).connectedNodes.await()
            }

            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, path, payload.toByteArray())
                    .await()
            }
        } catch (error: Exception) {
            Log.w("EcoGuiaWear", "No se pudo enviar evento al telefono", error)
        }
    }

    companion object {
        /** Identificador de capacidad que debe declarar el dispositivo móvil para recibir mensajes. */
        const val PHONE_CAPABILITY = "eco_guia_phone_receiver"

        /** Ruta para confirmar la vinculación activa entre dispositivos. */
        const val PATH_LINKED = "/eco-guia/wear/linked"

        /** Ruta para sincronizar el estado actual del radar con el teléfono. */
        const val PATH_RADAR_STATE = "/eco-guia/wear/radar-state"

        /** Ruta para solicitar al teléfono la apertura del módulo de realidad aumentada o cámara. */
        const val PATH_OPEN_CAMERA = "/eco-guia/phone/open-camera"
    }
}
