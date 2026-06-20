package mx.utng.ecoguiawear.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import mx.utng.ecoguiawear.presentation.screens.CompassScreen
import mx.utng.ecoguiawear.presentation.screens.HapticSettingsScreen
import mx.utng.ecoguiawear.presentation.screens.PairingScreen
import mx.utng.ecoguiawear.presentation.screens.RadarScreen
import mx.utng.ecoguiawear.presentation.screens.RouteSummaryScreen
import mx.utng.ecoguiawear.presentation.screens.SiteNearbyScreen
import mx.utng.ecoguiawear.presentation.screens.StealthRadarScreen

object WearRoutes {
    const val PAIRING = "pairing"
    const val RADAR = "radar"
    const val COMPASS = "compass"
    const val ALERT = "alert"
    const val ARRIVAL = "arrival"
    const val SITE = "site"
    const val SUMMARY = "summary"
    const val SETTINGS = "settings"
    const val STEALTH = "stealth"
}

@Composable
fun EcoGuiaWearNavGraph(viewModel: RadarViewModel) {
    val navController = rememberSwipeDismissableNavController()
    val state by viewModel.state.collectAsState()

    AppScaffold(timeText = { TimeText() }) {
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
                            navController.navigate(WearRoutes.STEALTH)
                        },
                        onStartDemo = {
                            viewModel.startDemo()
                            navController.navigate(WearRoutes.STEALTH)
                        }
                    )
                }
            }
            composable(WearRoutes.RADAR) {
                ScreenScaffold {
                    RadarScreen(
                        state = state,
                        onToggleRadar = viewModel::toggleRadar,
                        onApproachDemo = {
                            viewModel.simulateApproach()
                            when {
                                viewModel.state.value.mode == RadarMode.ARRIVED -> navController.navigate(WearRoutes.ARRIVAL)
                                viewModel.state.value.target.distanceMeters <= 20 -> navController.navigate(WearRoutes.ALERT)
                            }
                        },
                        onOpenCompass = { navController.navigate(WearRoutes.COMPASS) },
                        onOpenAlert = { navController.navigate(WearRoutes.ALERT) },
                        onOpenArrival = { navController.navigate(WearRoutes.ARRIVAL) },
                        onOpenSite = { navController.navigate(WearRoutes.SITE) },
                        onOpenSummary = { navController.navigate(WearRoutes.SUMMARY) },
                        onOpenSettings = { navController.navigate(WearRoutes.SETTINGS) },
                        onOpenStealth = { navController.navigate(WearRoutes.STEALTH) }
                    )
                }
            }
            composable(WearRoutes.COMPASS) {
                ScreenScaffold {
                    CompassScreen(
                        state = state,
                        onNext = {
                            if (state.mode == RadarMode.ARRIVED) {
                                navController.navigate(WearRoutes.ARRIVAL)
                            } else {
                                navController.navigate(WearRoutes.SUMMARY)
                            }
                        }
                    )
                }
            }
            composable(WearRoutes.ALERT) {
                ScreenScaffold {
                    AlertsScreen(
                        state = state,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(WearRoutes.ARRIVAL) {
                ScreenScaffold {
                    ArrivalScreen(
                        state = state,
                        onOpenPhone = viewModel::openPhoneCamera,
                        onResetDemo = {
                            viewModel.resetDemo()
                            navController.navigate(WearRoutes.RADAR) {
                                popUpTo(WearRoutes.RADAR) { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable(WearRoutes.STEALTH) {
                ScreenScaffold {
                    StealthRadarScreen(
                        state = state,
                        onToggleStealth = {
                            viewModel.toggleStealthMode()
                            navController.navigate(WearRoutes.SITE)
                        }
                    )
                }
            }
            composable(WearRoutes.SITE) {
                ScreenScaffold {
                    SiteNearbyScreen(
                        state = state,
                        onBackToRadar = { navController.navigate(WearRoutes.COMPASS) }
                    )
                }
            }
            composable(WearRoutes.SUMMARY) {
                ScreenScaffold {
                    RouteSummaryScreen(
                        state = state,
                        onBackToRadar = { navController.popBackStack() }
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
    }
}
