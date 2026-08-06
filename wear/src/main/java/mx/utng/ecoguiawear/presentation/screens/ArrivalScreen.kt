/**
 * Pantalla de confirmación de llegada a un hito o monumento histórico en Wear OS.
 *
 * Muestra el indicador visual de 0 metros, mensaje de éxito y opciones para abrir el visor AR
 * en el teléfono o continuar con la siguiente parada de la ruta.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla modal o de destino para eventos de arribo a destino.
 *
 * @param state Estado global reactivo del radar.
 * @param onOpenPhone Callback para solicitar la apertura de la cámara en el teléfono.
 * @param onContinue Callback para registrar la parada y avanzar de waypoint.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun ArrivalScreen(
    state: RadarUiState,
    onOpenPhone: () -> Unit,
    onContinue: () -> Unit
) {
    EcoWearScaffold {
        item {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EcoGuiaColors.Jade,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        item {
            Text(
                text = "¡LLEGASTE!",
                style = MaterialTheme.typography.titleMedium,
                color = EcoGuiaColors.Gold,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CircularStatus(
                progress = 1f,
                text = "0 m",
                progressColor = EcoGuiaColors.Jade,
                modifier = Modifier.height(60.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Usa el móvil para ver los detalles AR",
                style = MaterialTheme.typography.bodySmall,
                color = EcoGuiaColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = "Volver al radar",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Previsualización en el entorno de desarrollo Compose. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ArrivalScreenPreview() {
    EcoGuiaWearTheme {
        ArrivalScreen(state = RadarUiState(), onOpenPhone = {}, onContinue = {})
    }
}
