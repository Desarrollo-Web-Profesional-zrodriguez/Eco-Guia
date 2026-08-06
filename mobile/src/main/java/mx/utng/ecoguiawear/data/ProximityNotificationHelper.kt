/**
 * Archivo: ProximityNotificationHelper.kt
 *
 * Helper utilitario que centraliza la creación de canales de notificación y la construcción de
 * notificaciones del sistema para las alertas de proximidad a sitios históricos y el servicio en primer plano.
 *
 * @since 2026-08-05
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

/**
 * Proveedor de notificaciones para el sistema de alertas de proximidad y el Foreground Service.
 */
object ProximityNotificationHelper {

    /** Identificador del canal de notificaciones prioritarias de proximidad. */
    const val PROXIMITY_CHANNEL_ID = "eco_proximity"
    /** Identificador del canal de alertas de rutas turísticas. */
    const val ROUTES_CHANNEL_ID    = "eco_routes"
    /** Identificador del canal de notificación persistente para el servicio en segundo plano. */
    const val SERVICE_CHANNEL_ID   = "eco_service_bg"
    /** Identificador único de la notificación persistente del Foreground Service. */
    const val SERVICE_NOTIF_ID     = 1001

    /**
     * Registra los canales de notificación necesarios en el sistema:
     * - `eco_proximity`: Alertas sonoras y con vibración para sitios cercanos.
     * - `eco_routes`: Alertas de inicio y avance de ruta.
     * - `eco_service_bg`: Notificación persistente y silenciosa requerida por el Foreground Service.
     *
     * @param context Contexto de la aplicación Android.
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
     * Construye la notificación de alerta cuando el usuario entra en el radio de detección de un sitio.
     * Al tocar la notificación, abre la aplicación directamente en la pantalla de exploración.
     *
     * @param context Contexto de la aplicación Android.
     * @param siteName Nombre del sitio histórico detectado.
     * @param distance Distancia aproximada en metros hacia el sitio.
     * @return [Notification] configurada con canal prioritario y acción DeepLink.
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
     * Construye la notificación persistente silenciosa que mantiene al [ProximityService]
     * en primer plano (requerida por Android para ForegroundService de tipo location).
     *
     * @param context Contexto de la aplicación Android.
     * @return [Notification] persistente de baja prioridad.
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
