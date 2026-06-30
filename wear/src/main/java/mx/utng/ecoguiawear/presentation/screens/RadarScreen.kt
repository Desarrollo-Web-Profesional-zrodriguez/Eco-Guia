package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarMode
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

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
    onOpenStealth: () -> Unit = {} // Added for demo
) {
    val target = state.target
    val isPaused = state.mode == RadarMode.PAUSED

    EcoWearScaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount < -50) { // Swipe Left (<-)
                    onOpenCompass()
                }
            }
        }
    ) {
        item {
            ScreenHeader(
                title = "Radar activo",
                subtitle = state.lastAlert
            )
        }
        item {
            CompassArrow(
                bearingDegrees = target.bearingDegrees,
                modifier = Modifier.size(108.dp)
            )
        }
        item {
            Text(
                text = target.title,
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = "${target.distanceMeters} m",
                color = EcoGuiaColors.Gold,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text(if (isPaused) "Activar radar" else "Pausar radar") },
                onClick = onToggleRadar,
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = if (isPaused) EcoGuiaColors.Jade else EcoGuiaColors.Surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Simular avance") },
                onClick = onApproachDemo,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Ver Brújula") },
                onClick = onOpenCompass,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Ver Alertas") },
                onClick = onOpenAlert,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Ver Progreso Ruta") },
                onClick = onOpenSummary,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Modo Sigilo") },
                onClick = onOpenStealth,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Ajustes") },
                onClick = onOpenSettings,
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
