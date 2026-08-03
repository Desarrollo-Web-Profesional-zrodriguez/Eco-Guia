/**
 * Archivo: ProximityNotificationHelper.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Centraliza la creación de canales de notificación y la construcción de
 * notificaciones del sistema para las alertas de proximidad y el servicio en primer plano.
 *
 * Funciones destacadas:
 * - createChannels: Registra los canales PROXIMITY_CHANNEL y SERVICE_CHANNEL en el sistema.
 * - buildSiteAlertNotification: Construye una notificación de alta importancia con el nombre
 *   del sitio histórico detectado y un DeepLink a ExplorationScreen.
 * - buildServiceNotification: Construye la notificación persistente y silenciosa del servicio.
 */

package mx.utng.ecoguiawear.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import mx.utng.ecoguiawear.MainActivity

object ProximityNotificationHelper {

    const val PROXIMITY_CHANNEL_ID = "eco_proximity"
    const val ROUTES_CHANNEL_ID    = "eco_routes"
    const val SERVICE_CHANNEL_ID   = "eco_service_bg"
    const val SERVICE_NOTIF_ID     = 1001

    /**
     * Registra los canales de notificación necesarios:
     * - eco_proximity: alertas de sitio (alta importancia, vibra y suena).
     * - eco_routes: alertas de inicio y avance de ruta.
     * - eco_service_bg: notificación silenciosa del ForegroundService.
     */
    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal de alertas de proximidad — visible y sonoro
        val proximityChannel = NotificationChannel(
            PROXIMITY_CHANNEL_ID,
            "Alertas de Proximidad",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones al acercarte a un sitio histórico."
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        // Canal de alertas de ruta — visible y sonoro
        val routesChannel = NotificationChannel(
            ROUTES_CHANNEL_ID,
            "Alertas de Ruta",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones sobre el progreso e inicio de tu ruta activa."
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        // Canal del servicio en segundo plano — silencioso
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "EcoGuía Activo",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Indica que EcoGuía está monitoreando tu ubicación."
            setShowBadge(false)
        }

        manager.createNotificationChannel(proximityChannel)
        manager.createNotificationChannel(routesChannel)
        manager.createNotificationChannel(serviceChannel)
    }

    /**
     * Construye la notificación de alerta cuando el usuario entra en el radio de un sitio.
     * Al tocar la notificación, abre la app directamente en la pantalla de exploración.
     */
    fun buildSiteAlertNotification(
        context: Context,
        siteName: String,
        distance: Int
    ): Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "exploration")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(context, PROXIMITY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sitio histórico cercano")
            .setContentText("$siteName a ${distance}m de ti")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Te encuentras a ${distance}m de «$siteName». Toca para explorar su historia.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSound)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .build()
    }

    /**
     * Notificación cuando se inicia o se mantiene activa una ruta.
     */
    fun buildRouteActiveNotification(
        context: Context,
        routeTitle: String,
        totalWaypoints: Int
    ): Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "exploration")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 101, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(context, ROUTES_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Ruta turística iniciada")
            .setContentText("Navegando: $routeTitle ($totalWaypoints paradas)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Has iniciado la ruta «$routeTitle» con $totalWaypoints paradas. Tu avance se sincroniza con tu reloj.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSound)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .build()
    }

    /**
     * Notificación cuando se completa un sitio/parada de la ruta activa.
     */
    fun buildSiteCompletedNotification(
        context: Context,
        siteTitle: String,
        visitedCount: Int,
        totalCount: Int
    ): Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "exploration")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 102, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isCompleted = visitedCount >= totalCount
        val title = if (isCompleted) "¡Ruta completada!" else "Parada completada"
        val text = if (isCompleted) "¡Felicidades! Has visitado todas las paradas de la ruta turística." else "Has completado «$siteTitle» ($visitedCount de $totalCount)."

        val defaultSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(context, ROUTES_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSound)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .build()
    }

    /**
     * Construye la notificación persistente silenciosa que mantiene al ProximityService
     * en primer plano.
     */
    fun buildServiceNotification(context: Context): Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("EcoGuía activo")
            .setContentText("Monitoreando sitios históricos cercanos.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
