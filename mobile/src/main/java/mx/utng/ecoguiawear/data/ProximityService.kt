/**
 * Archivo: ProximityService.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Servicio en primer plano (ForegroundService) de tipo "location" que monitorea
 * la posición GPS del usuario en segundo plano y dispara notificaciones del sistema al acercarse
 * a un sitio histórico registrado en la base de datos Neon, incluso con la app cerrada.
 *
 * Funciones destacadas:
 * - onStartCommand: Inicia las actualizaciones de ubicación y registra el servicio como foreground.
 * - checkProximity: Consulta getNearbySites() con el radio exacto de cada sitio y verifica
 *   si el usuario está dentro del detection_radius_m. Emite notificación si aplica.
 * - onDestroy: Detiene el cliente de ubicación y limpia el estado de sitios notificados.
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

class ProximityService : Service() {

    companion object {
        const val TAG = "ProximityService"
        /** Radio de búsqueda en Neon para la consulta inicial (1 km). */
        private const val SEARCH_RADIUS_M = 1000
        /** Intervalo de actualización GPS en milisegundos (10 segundos). */
        private const val LOCATION_INTERVAL_MS = 10_000L
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val repository   = EcoGuiaRepositoryImpl()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    /**
     * Conjunto de siteIds para los que ya se emitió notificación en esta sesión.
     * Evita spam de alertas si el usuario permanece en el área.
     */
    private val notifiedSites = mutableSetOf<String>()

    // ─────────────────────────────────────────────────────────────────────────
    // Ciclo de vida del Service
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        ProximityNotificationHelper.createChannels(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

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
            fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
            Log.d(TAG, "Servicio de proximidad iniciado correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar actualizaciones GPS: ${e.message}")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

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
                // Ignorar sitios sin coordenadas o ya notificados
                val siteLat = site.latitude ?: return@forEach
                val siteLng = site.longitude ?: return@forEach
                if (site.id in notifiedSites) return@forEach

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
                    Log.d(TAG, "Sitio detectado: ${site.name} a ${distanceM}m (radio: ${site.detectionRadiusM}m)")
                    emitSiteAlert(site.id, site.name, distanceM)
                    notifiedSites.add(site.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en checkProximity: ${e.message}")
        }
    }

    private fun emitSiteAlert(siteId: String, siteName: String, distanceM: Int) {
        // Enviar Broadcast para mostrar Snackbar en la App (In-App notification)
        val localIntent = Intent("mx.utng.ecoguiawear.PROXIMITY_ALERT").apply {
            putExtra("siteName", siteName)
            putExtra("distance", distanceM)
        }
        sendBroadcast(localIntent)

        // Verificar permiso para notificación del sistema en Android 13+
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
        // Usamos un ID basado en el hash del siteId para notificaciones independientes por sitio
        notificationManager.notify(siteId.hashCode(), notification)
    }
}
