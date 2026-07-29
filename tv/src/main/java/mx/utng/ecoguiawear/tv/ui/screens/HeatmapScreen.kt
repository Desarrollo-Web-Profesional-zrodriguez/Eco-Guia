@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun HeatmapScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mapa de Calor de Dolores Hidalgo",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .width(600.dp)
                .height(350.dp)
                .background(Color.DarkGray)
        ) {
            val doloresHidalgo = LatLng(21.1561, -100.9325)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(doloresHidalgo, 15f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = false
                )
            ) {
                // Parroquia
                Circle(
                    center = LatLng(21.1575, -100.9330),
                    radius = 150.0,
                    fillColor = Color(0x66FF0000), // Rojo semi transparente
                    strokeColor = Color.Transparent
                )
                // Plaza
                Circle(
                    center = LatLng(21.1560, -100.9310),
                    radius = 100.0,
                    fillColor = Color(0x66FFA500), // Naranja semi transparente
                    strokeColor = Color.Transparent
                )
                // Museo
                Circle(
                    center = LatLng(21.1550, -100.9340),
                    radius = 80.0,
                    fillColor = Color(0x66FFFF00), // Amarillo semi transparente
                    strokeColor = Color.Transparent
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onBack) {
            Text("Volver al Inicio")
        }
    }
}
