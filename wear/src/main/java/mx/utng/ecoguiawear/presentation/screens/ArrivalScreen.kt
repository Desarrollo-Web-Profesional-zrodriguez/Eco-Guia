package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

@Composable
fun ArrivalScreen(
    state: RadarUiState,
    onOpenPhone: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoGuiaColors.Background)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EcoGuiaColors.Jade,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
            text = "¡LLEGASTE!",
            style = MaterialTheme.typography.title3,
            color = EcoGuiaColors.Gold,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        CircularStatus(
            progress = 1f,
            text = "0 m",
            progressColor = EcoGuiaColors.Jade,
            modifier = Modifier.height(60.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Usa el móvil para ver los detalles AR",
            style = MaterialTheme.typography.caption3,
            color = EcoGuiaColors.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.primaryButtonColors(
                backgroundColor = EcoGuiaColors.Jade
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                text = "Volver al radar",
                style = MaterialTheme.typography.button,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ArrivalScreenPreview() {
    EcoGuiaWearTheme {
        ArrivalScreen(state = RadarUiState(), onOpenPhone = {}, onContinue = {})
    }
}
