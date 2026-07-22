/**
 * Archivo: LinkedDevicesScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-22
 * Descripción: Pantalla principal de gestión de dispositivos vinculados (Ecosistema activo).
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.QrCode
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
fun LinkedDevicesScreen(
    onManageClick: () -> Unit,
    onStatusClick: () -> Unit
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
                Text("Dispositivos", color = Color.White, fontSize = 14.sp)
                Text("Vinculados", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text("TV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Active Ecosystem Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ecosistema activo", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Administra Smart TVs de hoteles, relojes demo y pantallas de museos.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Device List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Estado", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
                TextButton(onClick = onManageClick) {
                    Text("Gestionar", color = EcoGuiaColors.Jade)
                }
            }
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    DeviceItem(
                        title = "Lobby Hotel Hidalgo",
                        subtitle = "Online - Salón de la fama",
                        icon = Icons.Default.Tv,
                        statusColor = EcoGuiaColors.Jade,
                        onClick = onStatusClick
                    )
                }
                item {
                    DeviceItem(
                        title = "Wearables demo",
                        subtitle = "4 vinculados",
                        icon = Icons.Default.Watch,
                        statusColor = EcoGuiaColors.Jade,
                        onClick = onStatusClick
                    )
                }
                item {
                    DeviceItem(
                        title = "Sesiones QR",
                        subtitle = "2 activas hoy",
                        icon = Icons.Default.QrCode,
                        statusColor = Color.White,
                        onClick = onStatusClick
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LinkedDevicesScreenPreview() {
    EcoGuiaMobileTheme {
        LinkedDevicesScreen({}, {})
    }
}
