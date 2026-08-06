/**
 * Archivo: ProximityService.kt
 *
 * Servicio en primer plano ([Service]) de tipo "location" que monitorea continuamente la posición GPS
 * del usuario en segundo plano y dispara notificaciones del sistema al aproximarse a un sitio histórico
 * registrado en la base de datos Neon, incluso con la aplicación cerrada.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.data

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl

/**
 * Foreground Service para geofencing y monitoreo continuo de proximidad a sitios de interés cultural.
 * Requiere el permiso `FOREGROUND_SERVICE_LOCATION` y `ACCESS_FINE_LOCATION`.
 */
class ProximityService : Service() {

    companion object {
        const val TAG = "ProximityService"
        /** Radio de búsqueda en Neon para la consulta inicial (5 km). */
        private const val SEARCH_RADIUS_M = 5000
        /** Intervalo de actualización GPS en milisegundos (10 segundos). */
        private const val LOCATION_INTERVAL_MS = 10_000L
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val repository   = EcoGuiaRepositoryImpl()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    /**
     * Conjunto de identificadores de sitios para los que ya se emitió notificación en la sesión activa.
     * Evita la duplicación o saturación de alertas si el usuario permanece en el área.
     */
    private val notifiedSites = mutableSetOf<String>()

    // ─────────────────────────────────────────────────────────────────────────
    // Ciclo de vida del Service
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Inicializa los canales de notificación y el cliente de ubicación fused de Google Play Services.
     */
    override fun onCreate() {
        super.onCreate()
        ProximityNotificationHelper.createChannels(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Inicia la suscripción a cambios de ubicación en segundo plano y promueve el servicio a primer plano.
     *
     * @param intent Intención de inicio del servicio.
     * @param flags Indicadores adicionales de lanzamiento.
     * @param startId Identificador único de solicitud.
     * @return [START_STICKY] para solicitar recreación automática por el sistema si el proceso es eliminado.
     */
    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Promover a ForegroundService con notificación persistente
        startForeground(
            ProximityNotificationHelper.SERVICE_NOTIF_ID,
            ProximityNotificationHelper.buildServiceNotification(this)
        )

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(5_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    serviceScope.launch { checkProximity(location) }
                }
            }
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    serviceScope.launch { checkProximity(lastLoc) }
                }
            }
            fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
            Log.d(TAG, "Servicio de proximidad iniciado correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar actualizaciones GPS: ${e.message}")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Garantiza el auto-reinicio del servicio mediante [AlarmManager] si el usuario remueve la aplicación de la vista de tareas recientes.
     *
     * @param rootIntent Intención raíz de la tarea removida.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "App removida de recientes. Programando auto-reinicio del ProximityService...")
        try {
            val restartServiceIntent = Intent(applicationContext, ProximityService::class.java).also {
                it.setPackage(packageName)
            }
            val restartPendingIntent = android.app.PendingIntent.getService(
                applicationContext, 1, restartServiceIntent,
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager
            alarmManager?.set(
                android.app.AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error programando reinicio en onTaskRemoved: ${e.message}")
        }
        super.onTaskRemoved(rootIntent)
    }

    /**
     * Libera los recursos de geolocalización, cancela el alcance de corrutinas y limpia el registro de alertas.
     */
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        notifiedSites.clear()
        Log.d(TAG, "Servicio de proximidad detenido.")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lógica de Geofencing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Consulta la base de datos buscando sitios históricos dentro de [SEARCH_RADIUS_M] metros.
     * Para cada sitio encontrado, calcula la distancia real y verifica si está dentro de su
     * propio radio de detección (detection_radius_m). Si el usuario está dentro y no se ha
     * notificado antes, emite una alerta del sistema.
     *
     * @param location Ubicación GPS actual del usuario.
     */
    private suspend fun checkProximity(location: Location) {
        try {
            val nearbySites = repository.getNearbySites(
                location.latitude,
                location.longitude,
                SEARCH_RADIUS_M
            )

            nearbySites.forEach { site ->
                // Soporte para coordenadas computadas (vía WKT PostGIS o campos directos)
                val siteLat = site.getComputedLatitude() ?: return@forEach
                val siteLng = site.getComputedLongitude() ?: return@forEach

                // Calcular distancia real entre usuario y sitio
                val results = FloatArray(1)
                Location.distanceBetween(
                    location.latitude, location.longitude,
                    siteLat, siteLng,
                    results
                )
                val distanceM = results[0].toInt()

                // Verificar si el usuario está dentro del radio de detección del sitio
                if (distanceM <= site.detectionRadiusM) {
                    if (site.id !in notifiedSites) {
                        Log.d(TAG, "Sitio detectado: ${site.name} a ${distanceM}m (radio: ${site.detectionRadiusM}m)")
                        emitSiteAlert(site.id, site.name, distanceM)
                        notifiedSites.add(site.id)
                    }
                } else {
                    // Si el usuario ya salió del radio de detección (con un margen de 20m), reseteamos la notificación
                    if (distanceM > site.detectionRadiusM + 20) {
                        notifiedSites.remove(site.id)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en checkProximity: ${e.message}")
        }
    }

    private fun emitSiteAlert(siteId: String, siteName: String, distanceM: Int) {
        // 1. Enviar Broadcast para mostrar Snackbar en la App (In-App notification)
        val localIntent = Intent("mx.utng.ecoguiawear.PROXIMITY_ALERT").apply {
            putExtra("siteName", siteName)
            putExtra("distance", distanceM)
        }
        sendBroadcast(localIntent)

        // 2. Enviar alerta al reloj Wear OS
        serviceScope.launch {
            try {
                mx.utng.ecoguiawear.data.wear.WearMessageClient(this@ProximityService).sendAlert(
                    id = siteId,
                    message = " $siteName ($distanceM m)",
                    type = "SITE"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando alerta a Wear OS: ${e.message}")
            }
        }

        // 3. Verificar permiso para notificación del sistema en Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido. Solo se mostrará alerta In-App.")
                return
            }
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notification = ProximityNotificationHelper.buildSiteAlertNotification(
            context = this,
            siteName = siteName,
            distance = distanceM
        )
        // Usamos un ID positivo basado en el hash del siteId para notificaciones independientes por sitio
        val notifId = kotlin.math.abs(siteId.hashCode()) + 2000
        notificationManager.notify(notifId, notification)
    }
}
