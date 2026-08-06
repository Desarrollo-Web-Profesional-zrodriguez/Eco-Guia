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
    val isRouteActive = state.routeSummary.waypoints.isNotEmpty()
    val pageCount = if (isRouteActive) 4 else 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
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
                    onOpenArrival = { },
                    onOpenSummary = { 
                        if (isRouteActive) scope.launch { pagerState.animateScrollToPage(3) }
                    },
                    onOpenSettings = { },
                    onSelectNextAutoTarget = viewModel::selectNextAutoTarget,
                    onSelectPreviousAutoTarget = viewModel::selectPreviousAutoTarget,
                    onRefresh = viewModel::refreshFromCloud,
                    onNavigateBack = { 
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    requestFocus = isActive
                )
                2 -> CompassScreen(
                    state = state,
                    onNext = { 
                        if (isRouteActive) scope.launch { pagerState.animateScrollToPage(3) }
                    },
                    onBack = { 
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    requestFocus = isActive
                )
                3 -> {
                    if (isRouteActive) {
                        RouteSummaryScreen(
                            state = state,
                            onBackToRadar = { 
                                scope.launch { pagerState.animateScrollToPage(1) }
                            },
                            requestFocus = isActive
                        )
                    }
                }
            }
        }

        HorizontalPageIndicator(
            pageIndicatorState = object : PageIndicatorState {
                override val pageCount: Int = pageCount
                override val pageOffset: Float = pagerState.currentPageOffsetFraction
                override val selectedPage: Int = pagerState.currentPage
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedColor = EcoGuiaColors.Jade,
            unselectedColor = EcoGuiaColors.Muted
        )
    }
}
