/**
 * Archivo: CampaignDevicesScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla de selecciÃ³n de dispositivos para una campaÃ±a visual.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
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
import mx.utng.ecoguiawear.ui.screens.DeviceItem
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun CampaignDevicesScreen(
    onManageContentClick: () -> Unit
) {
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
                Text("Dispositivos", color = Color.White, fontSize = 14.sp)
                Text("Seleccionados", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Tv, null, tint = Color.White)
            }
        }

        // Map/Graphic Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F5E9))
        ) {
            Text("Mapa de disponibilidad", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Selected List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Seleccionados para campaña", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    DeviceItemToggle("Lobby Hotel Hidalgo", "Galería de la Independencia", true)
                }
                item {
                    DeviceItemToggle("Wearables demo", "4 conectados", true)
                }
                item {
                    DeviceItemToggle("Pasillo 2", "TV de bienvenida hoy", true)
                }
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Gestionar contenido",
                onClick = onManageContentClick
            )
        }
    }
}

@Composable
fun DeviceItemToggle(
    title: String,
    subtitle: String,
    initialValue: Boolean
) {
    var isChecked by remember { mutableStateOf(initialValue) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)))
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            
            Text(
                text = "ON", 
                color = EcoGuiaColors.Jade, 
                fontWeight = FontWeight.Bold, 
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampaignDevicesScreenPreview() {
    EcoGuiaMobileTheme {
        CampaignDevicesScreen({})
    }
}




