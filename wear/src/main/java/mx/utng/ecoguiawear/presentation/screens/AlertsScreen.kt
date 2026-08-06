/**
 * Pantalla de historial y administración de alertas de proximidad en Wear OS.
 *
 * Muestra las notificaciones recientes de sitios turísticos y geodrops mediante un carrusel horizontal
 * con soporte para descarte individual o purga masiva de registros.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla carrusel de alertas de proximidad.
 *
 * @param state Estado global reactivo del radar.
 * @param onBack Callback para cerrar o retroceder de pantalla.
 * @param onDeleteAlert Callback para eliminar una alerta específica por su ID.
 * @param onClearAll Callback para vaciar todo el historial de alertas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun AlertsScreen(
    state: RadarUiState,
    onBack: () -> Unit,
    onDeleteAlert: (String) -> Unit = {},
    onClearAll: () -> Unit = {}
) {
    if (state.alerts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoGuiaColors.Background)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        if (dragAmount.y < -50) { // Swipe up
                            onBack()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay alertas",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcoGuiaColors.Text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Se purgan tras 3h",
                    style = MaterialTheme.typography.labelSmall,
                    color = EcoGuiaColors.Muted,
                    fontSize = 11.sp
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { state.alerts.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoGuiaColors.Background)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
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
            if (page >= state.alerts.size) return@HorizontalPager
            val alert = state.alerts[page]
            val icon = when (alert.type) {
                "GEODROP" -> Icons.Default.LocationOn
                "SITE" -> Icons.Default.Adjust
                else -> Icons.Default.Star
            }
            val color = if (alert.type == "SITE") EcoGuiaColors.Gold else EcoGuiaColors.Jade

            val ageMins = ((System.currentTimeMillis() - alert.timestamp) / 60000L).coerceAtLeast(0)
            val timeText = if (ageMins < 1) "Ahora" else "Hace ${ageMins}m"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "ALERTA ${page + 1}/${state.alerts.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EcoGuiaColors.Gold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = EcoGuiaColors.Muted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EcoGuiaColors.Background,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoGuiaColors.Text,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón para borrar alerta individual
                    Chip(
                        label = { Text("Borrar", fontSize = 10.sp) },
                        onClick = { onDeleteAlert(alert.id) },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        colors = ChipDefaults.secondaryChipColors(backgroundColor = EcoGuiaColors.Surface),
                        modifier = Modifier.weight(1f).height(32.dp)
                    )

                    // Botón para borrar todas las alertas
                    Chip(
                        label = { Text("Todas", fontSize = 10.sp) },
                        onClick = onClearAll,
                        icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Gold),
                        modifier = Modifier.weight(1f).height(32.dp)
                    )
                }
            }
        }

        HorizontalPageIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
            selectedColor = EcoGuiaColors.Jade,
            unselectedColor = EcoGuiaColors.Muted
        )
    }
}

/**
 * Componente auxiliar para listar alertas en formato de renglón compacto.
 *
 * @param icon Vector del icono descriptivo.
 * @param text Mensaje de la alerta.
 * @param iconBackground Color de fondo circular del icono.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
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
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
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
            style = MaterialTheme.typography.labelSmall,
            color = EcoGuiaColors.Text
        )
    }
}

/** Previsualización en herramientas de diseño Compose. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun AlertsScreenPreview() {
    EcoGuiaWearTheme {
        AlertsScreen(state = RadarUiState(), onBack = {})
    }
}
