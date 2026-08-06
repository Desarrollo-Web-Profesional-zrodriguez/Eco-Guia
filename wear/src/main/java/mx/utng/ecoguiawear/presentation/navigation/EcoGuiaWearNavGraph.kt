/**
 * Grafo de navegación y orquestación de pantallas para la aplicación Wear OS.
 *
 * Utiliza [androidx.wear.compose.navigation.SwipeDismissableNavHost] para permitir el gesto
 * de deslizamiento lateral hacia atrás y renderiza diálogos modales para llegadas y rutas completadas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScreenScaffold
import mx.utng.ecoguiawear.domain.model.RadarMode
import mx.utng.ecoguiawear.presentation.RadarViewModel
import mx.utng.ecoguiawear.presentation.screens.AlertsScreen
import mx.utng.ecoguiawear.presentation.screens.ArrivalScreen
import mx.utng.ecoguiawear.presentation.screens.HapticSettingsScreen
import mx.utng.ecoguiawear.presentation.screens.PairingScreen

/**
 * Constantes de ruta para la navegación en el reloj inteligente.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
object WearRoutes {
    /** Pantalla inicial de emparejamiento con el móvil o modo demo. */
    const val PAIRING = "pairing"

    /** Carrusel horizontal que contiene Modo Discreto, Radar, Brújula y Resumen de Ruta. */
    const val RADAR_PAGER = "radar_pager"

    /** Pantalla de historial y gestión de alertas de proximidad. */
    const val ALERT = "alert"

    /** Pantalla de confirmación de llegada a un sitio histórico. */
    const val ARRIVAL = "arrival"

    /** Pantalla de configuración de intensidad y vibración háptica. */
    const val SETTINGS = "settings"
}

/**
 * Composible raíz que aloja el grafo de navegación SwipeDismissable.
 *
 * @param viewModel Instancia compartida del [RadarViewModel].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun EcoGuiaWearNavGraph(viewModel: RadarViewModel) {
    val navController = rememberSwipeDismissableNavController()
    val state by viewModel.state.collectAsState()

    AppScaffold(timeText = { TimeText() }) {
        Box(modifier = Modifier.fillMaxSize()) {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = WearRoutes.PAIRING
            ) {
                composable(WearRoutes.PAIRING) {
                    ScreenScaffold {
                        PairingScreen(
                            state = state,
                            onPairWithPhone = {
                                viewModel.pairWithPhone()
                                navController.navigate(WearRoutes.RADAR_PAGER)
                            },
                            onStartDemo = {
                                viewModel.startDemo()
                                navController.navigate(WearRoutes.RADAR_PAGER)
                            },
                            onViewAlerts = {
                                navController.navigate(WearRoutes.ALERT)
                            }
                        )
                    }
                }
                
                composable(WearRoutes.RADAR_PAGER) {
                    ScreenScaffold {
                        RadarPagerScreen(
                            viewModel = viewModel,
                            onNavigateToPairing = {
                                navController.navigate(WearRoutes.PAIRING) {
                                    popUpTo(WearRoutes.PAIRING) { inclusive = true }
                                }
                            },
                            onNavigateToAlerts = {
                                navController.navigate(WearRoutes.ALERT)
                            }
                        )
                    }
                }

                composable(WearRoutes.ALERT) {
                    ScreenScaffold {
                        AlertsScreen(
                            state = state,
                            onBack = {
                                navController.popBackStack()
                            },
                            onDeleteAlert = viewModel::deleteAlert,
                            onClearAll = viewModel::clearAllAlerts
                        )
                    }
                }
                
                composable(WearRoutes.ARRIVAL) {
                    ScreenScaffold {
                        ArrivalScreen(
                            state = state,
                            onOpenPhone = viewModel::openPhoneCamera,
                            onContinue = {
                                viewModel.completeArrival()
                                navController.navigate(WearRoutes.RADAR_PAGER) {
                                    popUpTo(WearRoutes.RADAR_PAGER) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable(WearRoutes.SETTINGS) {
                    ScreenScaffold {
                        HapticSettingsScreen(
                            state = state,
                            onToggleHaptics = viewModel::updateHaptics,
                            onSelectStrength = { strength ->
                                viewModel.updateHaptics(true, strength)
                            },
                            onBackToRadar = { navController.popBackStack() }
                        )
                    }
                }
            }

            // Diálogo emergente de llegada: se activa únicamente si hay una RUTA ACTIVA y NO ha finalizado
            if (state.mode == RadarMode.ARRIVED && state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted) {
                Dialog(onDismissRequest = { }) {
                    ArrivalScreen(
                        state = state,
                        onOpenPhone = viewModel::openPhoneCamera,
                        onContinue = viewModel::completeArrival
                    )
                }
            }

            // Diálogo emergente de Felicitación / Ruta Completada desde Móvil
            if (state.isRouteCompleted) {
                Dialog(onDismissRequest = { viewModel.dismissRouteCompleted() }) {
                    mx.utng.ecoguiawear.presentation.screens.RouteCompletedWearScreen(
                        state = state,
                        onDismiss = { viewModel.dismissRouteCompleted() }
                    )
                }
            }
        }
    }
}
