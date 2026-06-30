package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun AlertsScreen(
    state: RadarUiState,
    onBack: () -> Unit
) {
    if (state.alerts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        if (dragAmount.y < -50) { // Swipe up
                            onBack()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay alertas",
                style = MaterialTheme.typography.caption3,
                color = EcoGuiaColors.Muted
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { state.alerts.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (dragAmount.y < -50) { // Swipe up
                        onBack()
                    }
                }
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val alert = state.alerts[page]
            val icon = when (alert.type) {
                "GEODROP" -> Icons.Default.LocationOn
                "SITE" -> Icons.Default.Adjust
                else -> Icons.Default.Star
            }
            val color = if (alert.type == "SITE") EcoGuiaColors.Gold else EcoGuiaColors.Jade

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ALERTA ${page + 1}/${state.alerts.size}",
                    style = MaterialTheme.typography.caption2,
                    color = EcoGuiaColors.Gold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color, CircleShape),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EcoGuiaColors.Background,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.body2,
                    color = EcoGuiaColors.Text,
                    textAlign = TextAlign.Center
                )

                if (page < state.alerts.size - 1) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Desliza para ver más",
                        tint = EcoGuiaColors.Muted,
                        modifier = Modifier.padding(top = 8.dp).size(16.dp)
                    )
                }
            }
        }

        HorizontalPageIndicator(
            pageIndicatorState = object : PageIndicatorState {
                override val pageCount: Int = state.alerts.size
                override val pageOffset: Float = pagerState.currentPageOffsetFraction
                override val selectedPage: Int = pagerState.currentPage
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            selectedColor = EcoGuiaColors.Jade,
            unselectedColor = EcoGuiaColors.Muted
        )
    }
}

@Composable
fun AlertItem(icon: ImageVector, text: String, iconBackground: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(EcoGuiaColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .size(24.dp)
                .background(iconBackground, CircleShape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EcoGuiaColors.Background,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.caption2,
            color = EcoGuiaColors.Text
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun AlertsScreenPreview() {
    EcoGuiaWearTheme {
        AlertsScreen(state = RadarUiState(), onBack = {})
    }
}
