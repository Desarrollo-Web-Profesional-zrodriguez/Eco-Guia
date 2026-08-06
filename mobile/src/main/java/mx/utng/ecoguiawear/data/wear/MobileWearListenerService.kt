/**
 * Archivo: MobileWearListenerService.kt
 * Descripción: Servicio de escucha Wear OS en la app móvil.
 * Recibe mensajes desde el reloj (como abrir la cámara/GeoDrop de un sitio) y lanza la actividad.
 */

package mx.utng.ecoguiawear.data.wear

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import mx.utng.ecoguiawear.MainActivity

class MobileWearListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        val path = messageEvent.path
        val payload = String(messageEvent.data, Charsets.UTF_8)
        Log.d("MobileWearListener", "Mensaje recibido del reloj: path=$path, payload=$payload")

        if (path == "/eco-guia/phone/open-camera" || path == "/eco-guia/phone/open-geodrop") {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = "mx.utng.ecoguiawear.OPEN_GEODROP"
                putExtra("siteId", payload)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }
}
