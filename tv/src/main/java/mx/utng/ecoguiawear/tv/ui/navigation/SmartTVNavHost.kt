/**
 * Host de navegación de la aplicación Smart TV de Eco-Guía.
 *
 * Define el grafo de navegación completo para Android TV, administrando el destino inicial
 * (Lobby), las transiciones entre pantallas, el estado persistente del modo kiosco y la
 * escucha global de desvinculación remota mediante MQTT y sincronización con Neon DB.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.utng.ecoguiawear.tv.ui.screens.GalleryScreen
import mx.utng.ecoguiawear.tv.ui.screens.HeatmapScreen
import mx.utng.ecoguiawear.tv.ui.screens.LobbyScreen
import mx.utng.ecoguiawear.tv.ui.screens.Portal360Screen

/**
 * Rutas de navegación disponibles en la aplicación Smart TV.
 *
 * @param route Identificador único de la ruta para el [NavHost].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
sealed class TVRoutes(val route: String) {
    /** Ruta de la pantalla principal / lobby de bienvenida y emparejamiento. */
    object Lobby : TVRoutes("lobby")

    /** Ruta del mapa de calor de afluencia turística e interacciones. */
    object Heatmap : TVRoutes("heatmap")

    /** Ruta de la galería y slideshow de cápsulas culturales GeoDrops. */
    object Gallery : TVRoutes("gallery")

    /** Ruta del visor cartográfico 360° con rotación automática y perspectivas históricas. */
    object Portal360 : TVRoutes("portal360")
}

/**
 * Grafo principal de navegación para la Smart TV.
 *
 * Configura la pantalla de inicio en [TVRoutes.Lobby] y gestiona la navegación adaptada a control
 * remoto (D-Pad). Además, orquesta el ciclo de vida del modo de bloqueo kiosco y la recepción
 * de señales de cierre de sesión remoto transmitidas por el dispositivo móvil.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun SmartTVNavHost() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }

    // Estado global y persistente del Modo Bloqueo Kiosco en la TV
    var isKioskLocked by remember { mutableStateOf(false) }

    // Escucha global de órdenes MQTT/DB sin importar qué pantalla esté activa
    LaunchedEffect(Unit) {
        val pairingCode = prefs.getString("saved_pairing_code", null) ?: "000000"
        
        // 1. Escuchar orden MQTT instantánea de logout global
        mx.utng.ecoguia.shared.data.remote.HiveMQManager.subscribeToTvCommands(pairingCode) { command ->
            if (command == "logout") {
                mainHandler.post {
                    prefs.edit()
                        .remove("saved_is_paired")
                        .remove("saved_paired_user_id")
                        .remove("saved_paired_user_email")
                        .remove("saved_paired_user_name")
                        .remove("saved_paired_user_role")
                        .remove("saved_selected_site_id")
                        .remove("saved_pairing_code")
                        .apply()
                    isKioskLocked = false
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        // 2. Verificación secundaria periódica de sesión activa en BD
        while (true) {
            val isPaired = prefs.getBoolean("saved_is_paired", false)
            val userId = prefs.getString("saved_paired_user_id", null)
            if (isPaired && userId != null) {
                try {
                    val status = repository.getPairingStatus(userId)
                    if (status == null) {
                        // Sesión anulada en la base de datos
                        prefs.edit()
                            .remove("saved_is_paired")
                            .remove("saved_paired_user_id")
                            .remove("saved_paired_user_email")
                            .remove("saved_paired_user_name")
                            .remove("saved_paired_user_role")
                            .remove("saved_selected_site_id")
                            .remove("saved_pairing_code")
                            .apply()
                        isKioskLocked = false
                        navController.navigate(TVRoutes.Lobby.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SmartTVNavHost", "Error verificando sesión global: ${e.message}")
                }
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    NavHost(navController = navController, startDestination = TVRoutes.Lobby.route) {
        composable(TVRoutes.Lobby.route) {
            LobbyScreen(
                onNavigateToHeatmap = { navController.navigate(TVRoutes.Heatmap.route) },
                onNavigateToGallery = { navController.navigate(TVRoutes.Gallery.route) },
                onNavigateToPortal360 = { navController.navigate(TVRoutes.Portal360.route) }
            )
        }
        composable(TVRoutes.Heatmap.route) {
            HeatmapScreen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = {
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(TVRoutes.Lobby.route) { inclusive = true }
                    }
                }
            )
        }
        composable(TVRoutes.Gallery.route) {
            GalleryScreen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = {
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(TVRoutes.Lobby.route) { inclusive = true }
                    }
                }
            )
        }
        composable(TVRoutes.Portal360.route) {
            Portal360Screen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = {
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(TVRoutes.Lobby.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
