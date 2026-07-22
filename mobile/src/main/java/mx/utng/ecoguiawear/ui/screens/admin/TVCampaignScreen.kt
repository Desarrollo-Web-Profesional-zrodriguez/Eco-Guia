/**
 * Archivo: TVCampaignScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Gestión de campañas visuales para Smart TVs en hoteles y museos (Salón de la Fama).
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun TVCampaignScreen(
    onAnalyticsClick: () -> Unit,
    onManageDevicesClick: () -> Unit
) {
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
                Text("Campañas", color = Color.White, fontSize = 14.sp)
                Text("Hoteles y museos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = onAnalyticsClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(Icons.Default.Star, null, tint = EcoGuiaColors.Gold, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Salón de la Fama", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Programa qué cápsulas aparecerán en Smart TV durante el día.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Programming List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Programación", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    CampaignItem(
                        title = "Galería lobby",
                        subtitle = "Activo hasta 20:00",
                        icon = Icons.Default.Tv,
                        onClick = onManageDevicesClick
                    )
                }
                item {
                    CampaignItem(
                        title = "Colección pública",
                        subtitle = "Descargas habilitadas",
                        icon = Icons.Default.QrCode,
                        onClick = onManageDevicesClick
                    )
                }
                item {
                    CampaignItem(
                        title = "Ranking semanal",
                        subtitle = "Fotos más visitadas",
                        icon = Icons.Default.AccountTree,
                        onClick = onManageDevicesClick
                    )
                }
            }
        }
    }
}

@Composable
fun CampaignItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }
            
            RadioButton(selected = false, onClick = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TVCampaignScreenPreview() {
    EcoGuiaMobileTheme {
        TVCampaignScreen({}, {})
    }
}
