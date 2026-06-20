package mx.utng.ecoguiawear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class PhoneMessageClient(private val context: Context) {

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
        const val PHONE_CAPABILITY = "eco_guia_phone_receiver"
        const val PATH_LINKED = "/eco-guia/wear/linked"
        const val PATH_RADAR_STATE = "/eco-guia/wear/radar-state"
        const val PATH_OPEN_CAMERA = "/eco-guia/phone/open-camera"
    }
}
