/**
 * Archivo: SiteLocationScreen.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Configuración de geolocalización y radio de detección para un sitio (Paso 3).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Geolocalización", color = Color.White, fontSize = 14.sp)
                Text("Radio de llegada", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { viewModel.captureCurrentLocation(context) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.MyLocation, "Capturar ubicación actual", tint = EcoGuiaColors.Gold)
            }
        }

        // Map Section (Preview)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F5E9))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    lat = latLng.latitude
                    lng = latLng.longitude
                },
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
        }

        // Location Form
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text("Ubicación y radio", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                EcoTextField(value = visibility, onValueChange = { visibility = it }, label = "VISIBILIDAD EN TV/SMART")

            }
        }

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Confirmar ubicación",
                onClick = onNext
            )
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


