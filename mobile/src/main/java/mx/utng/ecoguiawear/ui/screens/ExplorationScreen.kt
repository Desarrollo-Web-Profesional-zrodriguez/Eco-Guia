/**
 * Archivo: ExplorationScreen.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Pantalla principal de exploración cultural. Integra un mapa de Google interactivo,
 * visualización de marcadores de sitios históricos y una lista reactiva de lugares cercanos.
 * 
 * Funciones destacadas:
 * - ExplorationScreen: Composable principal que coordina el mapa y la lista de recomendaciones.
 * - GoogleMap: Integración con la API de Google Maps para visualización geoespacial.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import androidx.compose.ui.tooling.preview.Preview
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel

/**
 * Composable que representa la pantalla de exploración.
 */
@Composable
fun ExplorationScreen(
    onAdminClick: () -> Unit,
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentLocation by locationViewModel.currentLocation
    val nearbySites by locationViewModel.nearbySites
    val closestSite by locationViewModel.closestSite

    // Iniciar actualizaciones de ubicación al entrar
    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.1561, -100.9350), 15f)
    }

    // Centrar cámara en el usuario cuando se obtiene ubicación por primera vez
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude), 17f
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoTopBar(
            title = if (closestSite != null) "¡Sitio Detectado!" else "Cerca de ti",
            subtitle = closestSite?.name ?: "Explorar",
            actionIcon = Icons.Default.AddCircle,
            onActionClick = onAdminClick
        )
        
        // Mapa Real
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Aumentamos altura para mejor visibilidad
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F5E9))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = currentLocation != null),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                nearbySites.forEach { site ->
                    val siteLat = site.latitude ?: return@forEach
                    val siteLng = site.longitude ?: return@forEach
                    Marker(
                        state = MarkerState(position = LatLng(siteLat, siteLng)),
                        title = site.name,
                        snippet = site.shortDescription
                    )
                }
            }
        }
        
        // Lista de Sitios
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                if (nearbySites.isEmpty()) "Buscando sitios históricos..." else "Sitios recomendados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(nearbySites.size) { index ->
                    val site = nearbySites[index]
                    RecommendedSiteItem(
                        title = site.name,
                        subtitle = site.siteType + " • " + (site.address ?: ""),
                        icon = Icons.Default.Place,
                        trailing = "Ver"
                    )
                }
            }
        }
    }
}

/**
 * Elemento de lista para un sitio recomendado.
 */
@Composable
fun RecommendedSiteItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = EcoGuiaColors.Jade.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = trailing,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGuiaColors.Jade
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExplorationScreenPreview() {
    EcoGuiaMobileTheme {
        ExplorationScreen({})
    }
}

