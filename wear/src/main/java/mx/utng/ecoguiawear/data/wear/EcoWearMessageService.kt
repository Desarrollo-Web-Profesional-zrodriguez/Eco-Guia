package mx.utng.ecoguiawear.data.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import mx.utng.ecoguiawear.data.repository.DemoRadarRepository

/**
 * Servicio del sistema Wear OS que escucha los mensajes enviados desde la app del teléfono móvil
 * incluso si la aplicación del reloj no está en primer plano.
 */
class EcoWearMessageService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d("EcoWearMessageService", "Mensaje recibido en segundo plano: ${messageEvent.path}")
        
        val repository = DemoRadarRepository(applicationContext)
        val listener = WearMessageListener(repository)
        listener.onMessageReceived(messageEvent)
    }
}
