package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun RadarScreen(
    state: RadarUiState,
    onToggleRadar: () -> Unit,
    onApproachDemo: () -> Unit,
    onOpenCompass: () -> Unit,
    onOpenAlert: () -> Unit,
    onOpenArrival: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectNextAutoTarget: () -> Unit = {},
    onSelectPreviousAutoTarget: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenStealth: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    requestFocus: Boolean = true
) {
    val target = state.target

    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            ScreenHeader(
                title = "Sitio histórico",
                subtitle = state.lastAlert
            )
        }

        item {
            CompassArrow(
                bearingDegrees = target.bearingDegrees - state.currentHeading,
                modifier = Modifier.size(108.dp)
            )
        }
        item {
            Text(
                text = target.title,
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        // Subtítulo del objetivo con flechas para alternar entre los 3 sitios más cercanos
        if (target.subtitle.isNotBlank()) {
            item {
                if (target.isAutoTarget && state.nearbyAutoTargets.isNotEmpty()) {
                    val count = state.nearbyAutoTargets.size
                    val currentIndex = state.selectedAutoIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onSelectPreviousAutoTarget,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EcoGuiaColors.Surface,
                                contentColor = EcoGuiaColors.Jade
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text("<", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }

                        Text(
                            text = "${currentIndex + 1}/$count · ${target.title.take(12)}",
                            color = EcoGuiaColors.Jade,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onSelectNextAutoTarget,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EcoGuiaColors.Surface,
                                contentColor = EcoGuiaColors.Jade
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text(">", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = target.subtitle,
                        color = EcoGuiaColors.Muted,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    )
                }
            }
        }
        item {
            Text(
                text = "${target.distanceMeters} m",
                color = EcoGuiaColors.Gold,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Ver Brújula / Dirección",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = {
                    onOpenCompass()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                label = { 
                    Text(
                        text = "Ver Alertas",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onOpenAlert,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
                    contentColor = EcoGuiaColors.Text
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Ajustes",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
                    contentColor = EcoGuiaColors.Text
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RadarScreenPreview() {
    EcoGuiaWearTheme {
        RadarScreen(
            state = RadarUiState(),
            onToggleRadar = {},
            onApproachDemo = {},
            onOpenCompass = {},
            onOpenAlert = {},
            onOpenArrival = {},
            onOpenSummary = {},
            onOpenSettings = {}
        )
    }
}
