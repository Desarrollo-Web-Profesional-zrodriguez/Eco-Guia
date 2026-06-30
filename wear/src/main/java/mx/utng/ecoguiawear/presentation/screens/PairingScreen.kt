package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun PairingScreen(
    state: RadarUiState,
    onPairWithPhone: () -> Unit,
    onStartDemo: () -> Unit,
    onViewAlerts: () -> Unit = {}
) {
    EcoWearScaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount < -50 && state.isLinkedToPhone) { // Swipe Left (<-)
                    onStartDemo()
                }
            }
        }
    ) {
        item {
            Text(
                text = if (state.isLinkedToPhone) "CONECTADO" else "DESCONECTADO",
                style = MaterialTheme.typography.caption2,
                color = if (state.isLinkedToPhone) EcoGuiaColors.Gold else EcoGuiaColors.Muted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            StatusItem(
                icon = Icons.Default.PhoneAndroid,
                text = "Eco-Guía móvil",
                color = if (state.isLinkedToPhone) EcoGuiaColors.Jade else EcoGuiaColors.Muted
            )
        }
        item {
            StatusItem(
                icon = Icons.Default.LocationOn,
                text = "GPS preciso",
                color = if (state.isGpsEnabled) EcoGuiaColors.Jade else EcoGuiaColors.Muted
            )
        }
        item {
            StatusItem(
                icon = Icons.Default.Videocam,
                text = "Cámara lista",
                color = if (state.isCameraReady) EcoGuiaColors.Jade else EcoGuiaColors.Muted
            )
        }
        item {
            Spacer(modifier = Modifier.padding(top = 8.dp))
        }
        item {
            Chip(
                label = { Text("INICIAR RADAR") },
                onClick = onStartDemo,
                enabled = state.isLinkedToPhone,
                colors = androidx.wear.compose.material.ChipDefaults.primaryChipColors(
                    backgroundColor = EcoGuiaColors.Jade
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("VER ALERTAS") },
                onClick = onViewAlerts,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = androidx.wear.compose.material.ChipDefaults.secondaryChipColors(
                    backgroundColor = EcoGuiaColors.Surface
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun StatusItem(icon: ImageVector, text: String, color: androidx.compose.ui.graphics.Color = EcoGuiaColors.Jade) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(EcoGuiaColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.caption2,
            color = if (color == EcoGuiaColors.Muted) EcoGuiaColors.Muted else EcoGuiaColors.Text
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PairingScreenPreview() {
    EcoGuiaWearTheme {
        PairingScreen(state = RadarUiState(), onPairWithPhone = {}, onStartDemo = {})
    }
}
