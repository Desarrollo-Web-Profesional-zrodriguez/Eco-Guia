/**
 * Archivo: SiteLocationScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Configuración de geolocalización y radio de detección para un sitio (Paso 3).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun SiteLocationScreen(
    onNext: () -> Unit
) {
    var coordinates by remember { mutableStateOf("21.1561, -100.9350") }
    var radius by remember { mutableStateOf("20 metros") }
    var visibility by remember { mutableStateOf("Si, con moderación") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F4F1))
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
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Map Section Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F5E9))
        ) {
            Text("Visor de Mapa Satelital", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
        }

        // Location Form
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text("Ubicación y radio", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EcoTextField(value = coordinates, onValueChange = { coordinates = it }, label = "COORDENADAS")
                EcoTextField(value = radius, onValueChange = { radius = it }, label = "RADIO DE LLEGADA")
                EcoTextField(value = visibility, onValueChange = { visibility = it }, label = "VISIBILIDAD EN TV/SMART")
            }
        }

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Guardar ubicación",
                onClick = onNext
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiteLocationScreenPreview() {
    EcoGuiaMobileTheme {
        SiteLocationScreen({})
    }
}
