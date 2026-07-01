package mx.utng.ecoguiawear.presentation.navigation

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.presentation.RadarViewModel
import mx.utng.ecoguiawear.presentation.screens.CompassScreen
import mx.utng.ecoguiawear.presentation.screens.RadarScreen
import mx.utng.ecoguiawear.presentation.screens.RouteSummaryScreen
import mx.utng.ecoguiawear.presentation.screens.StealthRadarScreen
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun RadarPagerScreen(
    viewModel: RadarViewModel,
    onNavigateToPairing: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Feedback háptico seguro al cambiar de página
    LaunchedEffect(pagerState.currentPage) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        vibrator?.let {
            if (it.hasVibrator()) {
                it.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1 
        ) { page ->
            // Forzamos un pequeño retraso y re-evaluación del foco cuando la página cambia
            // para asegurar que el sistema rotativo se enganche a la nueva pantalla activa.
            val isActive = pagerState.currentPage == page
            
            when (page) {
                0 -> StealthRadarScreen(
                    state = state,
                    onToggleStealth = viewModel::toggleStealthMode,
                    onNavigateNext = { 
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    onNavigateBack = onNavigateToPairing,
                    requestFocus = isActive
                )
                1 -> RadarScreen(
                    state = state,
                    onToggleRadar = viewModel::toggleRadar,
                    onApproachDemo = viewModel::simulateApproach,
                    onOpenCompass = { 
                        scope.launch { pagerState.animateScrollToPage(2) }
                    },
                    onOpenAlert = onNavigateToAlerts,
                    onOpenArrival = { /* Handled by arrival mode */ },
                    onOpenSummary = { 
                        scope.launch { pagerState.animateScrollToPage(3) }
                    },
                    onOpenSettings = { /* Page 0 handles settings back */ },
                    onNavigateBack = { 
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    requestFocus = isActive
                )
                2 -> CompassScreen(
                    state = state,
                    onNext = { 
                        scope.launch { pagerState.animateScrollToPage(3) }
                    },
                    onBack = { 
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    requestFocus = isActive
                )
                3 -> RouteSummaryScreen(
                    state = state,
                    onBackToRadar = { 
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    requestFocus = isActive
                )
            }
        }

        HorizontalPageIndicator(
            pageIndicatorState = object : PageIndicatorState {
                override val pageCount: Int = 4
                override val pageOffset: Float = pagerState.currentPageOffsetFraction
                override val selectedPage: Int = pagerState.currentPage
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedColor = EcoGuiaColors.Jade,
            unselectedColor = EcoGuiaColors.Muted
        )
    }
}
