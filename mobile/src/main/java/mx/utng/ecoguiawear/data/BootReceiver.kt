/**
 * Archivo: BootReceiver.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-08-02
 * Descripción: BroadcastReceiver que reinicia automáticamente el ProximityService
 * tras reiniciar el dispositivo o actualizar la aplicación, si el usuario tenía
 * activadas las alertas de proximidad en segundo plano.
 */

package mx.utng.ecoguiawear.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = context.getSharedPreferences("eco_prefs", Context.MODE_PRIVATE)
            val isProximityActive = prefs.getBoolean("proximity_active", false)
            if (isProximityActive) {
                Log.d("BootReceiver", "Reiniciando ProximityService tras reinicio del sistema ($action)...")
                try {
                    val serviceIntent = Intent(context, ProximityService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error al reiniciar ProximityService: ${e.message}")
                }
            }
        }
    }
}
