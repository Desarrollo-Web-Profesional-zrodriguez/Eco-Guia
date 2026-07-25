/**
 * Archivo: ActiveRouteScreen.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Muestra el avance en tiempo real de la ruta turística seleccionada por el usuario,
 * calculando la distancia GPS a cada parada y marcando el progreso automáticamente al acercarse.
 *
 * Funciones destacadas:
 * - ActiveRouteScreen: Composable principal. Muestra la barra de progreso general y lista de paradas.
 * - RouteStopItem: Fila de cada parada que muestra orden, nombre, distancia GPS real e ícono de completado.
 */

package mx.utng.ecoguiawear.ui.screens

import android.location.Location
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguia.shared.domain.model.RemoteRouteStop
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel
import mx.utng.ecoguiawear.ui.viewmodel.RouteViewModel

/**
 * Pantalla que visualiza y sigue el recorrido de una ruta activa.
 */
@Composable
fun ActiveRouteScreen(
    onFinishRoute: () -> Unit = {},
    routeViewModel: RouteViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeRoute by routeViewModel.activeRoute
    val stops by routeViewModel.activeStops
    val currentLocation by locationViewModel.currentLocation

    // Iniciar GPS si no está activo
    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates(context)
    }

    // Evaluar progreso GPS con cada actualización de ubicación
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            routeViewModel.updateProgressWithLocation(loc)
        }
    }

    val completedCount = stops.count { routeViewModel.completedStops[it.id] == true }
    val totalStops = if (stops.isNotEmpty()) stops.size else 1
    val progressFraction = completedCount.toFloat() / totalStops.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "progress")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 20.dp)
        ) {
            Column {
                Text(
                    text = "Ruta activa",
                    color = EcoGuiaColors.Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activeRoute?.title ?: "Recorrido Histórico",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    routeViewModel.stopActiveRoute()
                    onFinishRoute()
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Finalizar ruta", tint = Color.White)
            }
        }

        // Card de Progreso
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (completedCount == totalStops && stops.isNotEmpty()) "🎉 ¡Ruta completada!" else "Progreso del recorrido",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completedCount de $totalStops paradas visitadas · ~${activeRoute?.estimatedMinutes ?: 30} min",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = EcoGuiaColors.Jade,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        // Lista de Paradas Ordenadas
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Paradas del recorrido",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (stops.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗺️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay ruta seleccionada.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Explora las rutas disponibles e inicia un recorrido.",
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(stops) { stop ->
                        val isDone = routeViewModel.completedStops[stop.id] == true
                        val distanceM = calculateDistanceM(currentLocation, stop.latitude, stop.longitude)

                        RouteStopItem(
                            stop = stop,
                            isCompleted = isDone,
                            distanceM = distanceM,
                            onToggle = { routeViewModel.toggleStopCompleted(stop.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fila de una parada individual en la ruta activa.
 */
@Composable
fun RouteStopItem(
    stop: RemoteRouteStop,
    isCompleted: Boolean,
    distanceM: Int?,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número de parada
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isCompleted) EcoGuiaColors.Jade
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = stop.stopOrder.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Nombre y distancia GPS
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = stop.siteName ?: "Parada ${stop.stopOrder}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface
                )

                val distText = when {
                    isCompleted -> "✓ Visitado"
                    distanceM != null && distanceM < 1000 -> "A $distanceM m"
                    distanceM != null -> "A ${"%.1f".format(distanceM / 1000.0)} km"
                    else -> stop.instruction ?: "Parada ${stop.stopOrder}"
                }
                Text(
                    text = distText,
                    color = if (isCompleted) EcoGuiaColors.Jade else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Indicador o botón de toggle
            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Place,
                    contentDescription = null,
                    tint = if (isCompleted) EcoGuiaColors.Jade else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Calcula distancia geodésica simple en metros. */
private fun calculateDistanceM(userLoc: Location?, targetLat: Double?, targetLng: Double?): Int? {
    if (userLoc == null || targetLat == null || targetLng == null) return null
    val results = FloatArray(1)
    Location.distanceBetween(userLoc.latitude, userLoc.longitude, targetLat, targetLng, results)
    return results[0].toInt()
}

@Preview(showBackground = true)
@Composable
fun ActiveRouteScreenPreview() {
    EcoGuiaMobileTheme {
        ActiveRouteScreen()
    }
}
