/**
 * Archivo: SiteLocationScreen.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Configuración de geolocalización y radio de detección para un sitio (Paso 3).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.SiteRegistrationViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun SiteLocationScreen(
    viewModel: SiteRegistrationViewModel,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var lat by viewModel.latitude
    var lng by viewModel.longitude
    var radius by viewModel.radiusM
    var visibility by remember { mutableStateOf("Si, con moderación") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(lat.takeIf { it != 0.0 } ?: 21.1561, lng.takeIf { it != 0.0 } ?: -100.9350), 15f)
    }

    // Actualizar cámara cuando cambie la ubicación capturada
    LaunchedEffect(lat, lng) {
        if (lat != 0.0 && lng != 0.0) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 17f)
        }
    }

    var isSatelliteMap by remember { mutableStateOf(false) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

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
                .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            Column {
                Text("Geolocalización", color = Color.White, fontSize = 12.sp)
                Text("Radio de llegada", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { viewModel.captureCurrentLocation(context) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.MyLocation, "Capturar ubicación actual", tint = EcoGuiaColors.Gold)
            }
        }

        val isSystemInDark = isSystemInDarkTheme()
        val mapStyleOptions = remember(isSystemInDark) {
            if (isSystemInDark) {
                com.google.android.gms.maps.model.MapStyleOptions(
                    """[{"elementType":"geometry","stylers":[{"color":"#242f3e"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},{"elementType":"labels.text.stroke","stylers":[{"color":"#242f3e"}]},{"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#263c3f"}]},{"featureType":"poi.park","elementType":"labels.text.fill","stylers":[{"color":"#6b9a76"}]},{"featureType":"road","elementType":"geometry","stylers":[{"color":"#38414e"}]},{"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212a37"}]},{"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#9ca5b3"}]},{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#746855"}]},{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1f2835"}]},{"featureType":"road.highway","elementType":"labels.text.fill","stylers":[{"color":"#f3d19c"}]},{"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2f3948"}]},{"featureType":"transit.station","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#17263c"}]},{"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]},{"featureType":"water","elementType":"labels.text.stroke","stylers":[{"color":"#17263c"}]}]"""
                )
            } else null
        }

        val currentMapProperties = remember(isSatelliteMap, mapStyleOptions) {
            MapProperties(
                mapType = if (isSatelliteMap) MapType.HYBRID else MapType.NORMAL,
                isBuildingEnabled = true,
                mapStyleOptions = if (isSatelliteMap) null else mapStyleOptions
            )
        }

        if (isLandscape) {
            // Diseño en 2 columnas para modo Horizontal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mapa a la izquierda
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            lat = latLng.latitude
                            lng = latLng.longitude
                        },
                        properties = currentMapProperties,
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
                    ) {
                        if (lat != 0.0 && lng != 0.0) {
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = "Ubicación seleccionada"
                            )
                            Circle(
                                center = LatLng(lat, lng),
                                radius = radius.toDouble(),
                                fillColor = EcoGuiaColors.Jade.copy(alpha = 0.2f),
                                strokeColor = EcoGuiaColors.Jade,
                                strokeWidth = 2f
                            )
                        }
                    }

                    // Botón para alternar Capas (Mapa / Satélite)
                    IconButton(
                        onClick = { isSatelliteMap = !isSatelliteMap },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(if (isSatelliteMap) EcoGuiaColors.Gold else Color.White, androidx.compose.foundation.shape.CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Cambiar Capa Mapa",
                            tint = if (isSatelliteMap) EcoGuiaColors.DeepBlue else EcoGuiaColors.DeepBlue
                        )
                    }
                }

                val rightScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rightScrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Ubicación y radio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    EcoTextField(
                        value = if (lat == 0.0) "Toca el mapa o el icono superior" else "$lat, $lng", 
                        onValueChange = { }, 
                        label = "COORDENADAS"
                    )
                    var radiusText by remember { mutableStateOf(radius.toString()) }
                    EcoTextField(
                        value = radiusText,
                        onValueChange = { input ->
                            radiusText = input
                            val parsed = input.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                radius = parsed
                            }
                        },
                        label = "RADIO DE LLEGADA (Metros)"
                    )
                    EcoTextField(
                        value = visibility, 
                        onValueChange = { visibility = it }, 
                        label = "VISIBILIDAD EN TV/SMART"
                    )

                    EcoButton(
                        text = "Confirmar ubicación",
                        onClick = onNext
                    )
                }
            }
        } else {
            // Diseño vertical: El Mapa ocupa altura fija y el formulario inferior usa scroll
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            lat = latLng.latitude
                            lng = latLng.longitude
                        },
                        properties = currentMapProperties,
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
                    ) {
                        if (lat != 0.0 && lng != 0.0) {
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = "Ubicación seleccionada"
                            )
                            Circle(
                                center = LatLng(lat, lng),
                                radius = radius.toDouble(),
                                fillColor = EcoGuiaColors.Jade.copy(alpha = 0.2f),
                                strokeColor = EcoGuiaColors.Jade,
                                strokeWidth = 2f
                            )
                        }
                    }

                    // Botón para alternar Capas (Mapa / Satélite)
                    IconButton(
                        onClick = { isSatelliteMap = !isSatelliteMap },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(if (isSatelliteMap) EcoGuiaColors.Gold else Color.White, androidx.compose.foundation.shape.CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Cambiar Capa Mapa",
                            tint = EcoGuiaColors.DeepBlue
                        )
                    }
                }

                val bottomScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(bottomScrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Ubicación y radio", fontWeight = FontWeight.Bold)
                    EcoTextField(
                        value = if (lat == 0.0) "Toca el mapa o el icono superior" else "$lat, $lng", 
                        onValueChange = { }, 
                        label = "COORDENADAS"
                    )
                    var radiusText by remember { mutableStateOf(radius.toString()) }
                    EcoTextField(
                        value = radiusText,
                        onValueChange = { input ->
                            radiusText = input
                            val parsed = input.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                radius = parsed
                            }
                        },
                        label = "RADIO DE LLEGADA (Metros)"
                    )
                    EcoTextField(
                        value = visibility, 
                        onValueChange = { visibility = it }, 
                        label = "VISIBILIDAD EN TV/SMART"
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    EcoButton(
                        text = "Confirmar ubicación",
                        onClick = onNext
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiteLocationScreenPreview() {
    EcoGuiaMobileTheme {
        SiteLocationScreen(SiteRegistrationViewModel(), {})
    }
}


