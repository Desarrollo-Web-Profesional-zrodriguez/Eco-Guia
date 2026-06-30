package mx.utng.ecoguiawear.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import mx.utng.ecoguiawear.presentation.RadarViewModel
import mx.utng.ecoguiawear.presentation.screens.CompassScreen
import mx.utng.ecoguiawear.presentation.screens.RadarScreen
import mx.utng.ecoguiawear.presentation.screens.RouteSummaryScreen
import mx.utng.ecoguiawear.presentation.screens.StealthRadarScreen
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun RadarPagerScreen(
    viewModel: RadarViewModel,
    onNavigateToPairing: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val context = LocalContext.current
    
    // Feedback háptico al cambiar de página
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            // Vibra al cambiar de pantalla
            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(45, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
            // Se eliminó edgeSwipeToDismiss para corregir el crash "offset was read before being initialized"
        ) { page ->
            when (page) {
                0 -> StealthRadarScreen(
                    state = state,
                    onToggleStealth = viewModel::toggleStealthMode,
                    onNavigateNext = { /* Managed by Pager */ },
                    onNavigateBack = onNavigateToPairing
                )
                1 -> RadarScreen(
                    state = state,
                    onToggleRadar = viewModel::toggleRadar,
                    onApproachDemo = viewModel::simulateApproach,
                    onOpenCompass = { /* Managed by Pager */ },
                    onOpenAlert = { /* Managed by Pager */ },
                    onOpenArrival = { /* Managed by Pager */ },
                    onOpenSummary = { /* Managed by Pager */ },
                    onOpenSettings = { /* Add if needed */ },
                    onNavigateBack = { /* Managed by Pager */ }
                )
                2 -> CompassScreen(
                    state = state,
                    onNext = { /* Managed by Pager */ },
                    onBack = { /* Managed by Pager */ }
                )
                3 -> RouteSummaryScreen(
                    state = state,
                    onBackToRadar = { /* Managed by Pager */ }
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
