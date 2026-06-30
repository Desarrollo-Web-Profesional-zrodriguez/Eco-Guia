package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun PairingScreen(
    state: RadarUiState,
    onPairWithPhone: () -> Unit,
    onStartDemo: () -> Unit,
    onViewAlerts: () -> Unit = {}
) {
    EcoWearScaffold(
        modifier = Modifier.clickable { 
            if (state.isLinkedToPhone) onStartDemo() else onPairWithPhone() 
        }
    ) {
        item {
            Text(
                text = "CONECTADO",
                style = MaterialTheme.typography.labelSmall,
                color = EcoGuiaColors.Muted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )
        }

        item {
            StatusCard(
                icon = Icons.Default.Smartphone,
                text = "Eco-Guía móvil",
                isActive = state.isLinkedToPhone
            )
        }

        item {
            StatusCard(
                icon = Icons.Default.LocationOn,
                text = "GPS preciso",
                isActive = state.isGpsEnabled
            )
        }

        item {
            StatusCard(
                icon = Icons.Default.CameraAlt,
                text = "Cámara lista",
                isActive = state.isCameraReady
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toca para continuar",
                style = MaterialTheme.typography.bodySmall,
                color = EcoGuiaColors.Jade,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatusCard(icon: ImageVector, text: String, isActive: Boolean) {
    val backgroundColor = if (isActive) {
        EcoGuiaColors.Surface.copy(alpha = 0.6f)
    } else {
        EcoGuiaColors.Surface.copy(alpha = 0.2f)
    }
    
    val iconGradient = if (isActive) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFE0E5A5), Color(0xFF98D8B1))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(EcoGuiaColors.Muted, EcoGuiaColors.Muted)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EcoGuiaColors.Background,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isActive) EcoGuiaColors.Text else EcoGuiaColors.Muted
        )
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PairingScreenPreview() {
    EcoGuiaWearTheme {
        PairingScreen(
            state = RadarUiState(isLinkedToPhone = true, isGpsEnabled = true, isCameraReady = false),
            onPairWithPhone = {},
            onStartDemo = {}
        )
    }
}
