/**
 * Servicio en segundo plano para la recepción de mensajes del teléfono en Wear OS.
 *
 * Escucha eventos del [com.google.android.gms.wearable.WearableListenerService], procesa comandos
 * de rutas turísticas, paradas y sitios históricos, y genera notificaciones del sistema en el reloj.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl

/**
 * Servicio del sistema Wear OS que escucha los mensajes enviados desde la app del teléfono móvil
 * incluso si la aplicación del reloj no está en primer plano.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class EcoWearMessageService : WearableListenerService() {

    /**
     * Callback invocado automáticamente al recibir un mensaje del teléfono conectado.
     *
     * @param messageEvent Evento que encapsula la ruta y los datos binarios transmitidos.
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        val path = messageEvent.path
        val payload = String(messageEvent.data, Charsets.UTF_8)
        android.util.Log.d("EcoWearMessageService", "Mensaje recibido en segundo plano: $path ($payload)")

        val repository = RadarRepositoryImpl(applicationContext)
        val listener = WearMessageListener(repository)
        listener.onMessageReceived(messageEvent)

        if (path.startsWith("/eco-guia/")) {
            showSystemNotification(path, payload)
        }
    }

    /**
     * Muestra una notificación visual y sonora en el sistema operativo del reloj inteligente.
     *
     * @param path Ruta del comando recibido.
     * @param payload Contenido del mensaje en formato de texto plano.
     */
    private fun showSystemNotification(path: String, payload: String) {
        try {
            val channelId = "wear_eco_alerts"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(
                    channelId,
                    "Alertas EcoGuía",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertas de proximidad y rutas turísticas"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 150, 80, 200)
                    setSound(soundUri, audioAttributes)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val messageParts = payload.split("|")
            val rawText = if (messageParts.size >= 2) messageParts[1] else payload

            val (title, body) = when {
                path == "/eco-guia/sync/route" -> {
                    val routeName = messageParts.firstOrNull()?.ifEmpty { "Turística" } ?: "Turística"
                    Pair("Ruta turística iniciada", "Navegando por $routeName. Tu avance se sincroniza con el reloj.")
                }
                path == "/eco-guia/sync/progress" -> {
                    val completed = messageParts.getOrNull(0) ?: "0"
                    val total = messageParts.getOrNull(1) ?: "0"
                    Pair("Progreso de ruta", "Has completado $completed de $total paradas en la ruta activa.")
                }
                path == "/eco-guia/complete/route" -> {
                    Pair("Ruta completada", "Has visitado todas las paradas de la ruta turística.")
                }
                path == "/eco-guia/cancel/route" -> {
                    Pair("Ruta finalizada", "La navegación de la ruta activa ha finalizado.")
                }
                path == "/eco-guia/sync/target" -> {
                    val siteName = messageParts.getOrNull(1) ?: "Sitio histórico"
                    Pair("Sitio histórico seleccionado", "Dirección hacia $siteName.")
                }
                else -> {
                    Pair("Sitio histórico cercano", if (rawText.isNotEmpty()) rawText else "Te encuentras dentro del rango de detección de un sitio histórico.")
                }
            }

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(mx.utng.ecoguiawear.R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .build()

            val notifId = (System.currentTimeMillis() % 10000).toInt()
            notificationManager.notify(notifId, notification)
        } catch (e: Exception) {
            android.util.Log.e("EcoWearMessageService", "Error mostrando notificación en el reloj: ${e.message}")
        }
    }
}
