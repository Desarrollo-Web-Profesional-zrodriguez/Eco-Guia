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

object WearRoutes {
    const val PAIRING = "pairing"
    const val RADAR_PAGER = "radar_pager"
    const val ALERT = "alert"
    const val ARRIVAL = "arrival"
    const val SETTINGS = "settings"
}

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
                            }
                        )
                    }
                }

                composable(WearRoutes.ALERT) {
                    ScreenScaffold {
                        AlertsScreen(
                            state = state,
                            onBack = {
                                navController.navigate(WearRoutes.PAIRING) {
                                    popUpTo(WearRoutes.PAIRING) { inclusive = true }
                                }
                            }
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

            // Overlay ArrivalScreen
            if (state.mode == RadarMode.ARRIVED) {
                Dialog(onDismissRequest = { }) {
                    ArrivalScreen(
                        state = state,
                        onOpenPhone = viewModel::openPhoneCamera,
                        onContinue = viewModel::completeArrival
                    )
                }
            }
        }
    }
}
