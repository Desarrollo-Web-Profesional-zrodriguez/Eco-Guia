/**
 * Archivo: CameraGeoDropScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Interfaz de cámara para la detección y captura de Geo-Drops mediante realidad aumentada.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun CameraGeoDropScreen(
    onCapture: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoGuiaColors.DeepBlue)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Cámara", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Geo-Drops", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Camera Viewport Simulation
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF423A2B), Color(0xFF2B251B))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // AR Target circle
            Box(
                modifier = Modifier
                    .size(240.dp, 100.dp)
                    .border(2.dp, EcoGuiaColors.Jade, RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = EcoGuiaColors.DeepBlue.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Geo-Drop a 8 m", 
                        color = Color.White, 
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Cápsula detectada", color = Color.White, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = EcoGuiaColors.Jade)
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("Alinea el encuadre", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Sigue la flecha hasta el punto exacto", color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("8 m", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            EcoButton(
                text = "Capturar Geo-Drop",
                onClick = onCapture
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraGeoDropScreenPreview() {
    EcoGuiaMobileTheme {
        CameraGeoDropScreen({})
    }
}
