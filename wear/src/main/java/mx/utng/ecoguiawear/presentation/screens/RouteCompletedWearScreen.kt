/**
 * Pantalla de felicitación y fin de ruta turística completada en Wear OS.
 *
 * Muestra el trofeo de logro turístico, el nombre del recorrido completado y un botón
 * para descartar el diálogo modal y volver al modo de auto-escaneo libre.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla de confirmación de ruta turística completada.
 *
 * @param state Estado global reactivo del radar.
 * @param onDismiss Callback para cerrar el diálogo modal de felicitación.
 * @param requestFocus Solicita foco rotatorio si está en primer plano.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun RouteCompletedWearScreen(
    state: RadarUiState,
    onDismiss: () -> Unit,
    requestFocus: Boolean = true
) {
    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            Text(
                text = "🏆",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            Text(
                text = "¡RUTA COMPLETADA!",
                color = EcoGuiaColors.Gold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
        item {
            Text(
                text = if (state.routeSummary.title.isNotBlank()) state.routeSummary.title else "Recorrido Turístico",
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
            )
        }
        item {
            Text(
                text = "Se guardó el logro en tu colección del teléfono.",
                color = EcoGuiaColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
        }
        item {
            Button(
                label = {
                    Text(
                        text = "Aceptar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Gold,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
    }
}

/** Previsualización de la pantalla de ruta completada. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RouteCompletedWearScreenPreview() {
    EcoGuiaWearTheme {
        RouteCompletedWearScreen(state = RadarUiState(), onDismiss = {})
    }
}
