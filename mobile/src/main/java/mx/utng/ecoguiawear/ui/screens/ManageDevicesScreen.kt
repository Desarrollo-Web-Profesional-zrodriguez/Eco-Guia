/**
 * Archivo: ManageDevicesScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla para gestionar y desvincular dispositivos del ecosistema.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun ManageDevicesScreen(
    onConfirmChanges: () -> Unit
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
                Text("Desvincular", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Warning Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gestión de dispositivos", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Esta acción cortará la conexión en tiempo real con el dispositivo seleccionado del proyecto.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Lista de Dispositivos Editables
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Sesiones y Dispositivos Conectados", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    ManageDeviceItem(
                        title = "Reloj Wear OS (Galaxy Watch)",
                        subtitle = "Sesión: mus@ecoguia.com",
                        icon = Icons.Default.Watch,
                        actionText = "Cerrar sesión en Reloj"
                    )
                }
                item {
                    ManageDeviceItem(
                        title = "Smart TV - Lobby Hotel Hidalgo",
                        subtitle = "Sesión: mus@ecoguia.com",
                        icon = Icons.Default.Tv,
                        actionText = "Desvincular TV"
                    )
                }
                item {
                    ManageDeviceItem(
                        title = "Smart TV - Sala 2 Museo",
                        subtitle = "Sesión activa en red local",
                        icon = Icons.Default.Tv,
                        actionText = "Desvincular TV"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Guardar y confirmar cambios",
                onClick = onConfirmChanges
            )
        }
    }
}

@Composable
fun ManageDeviceItem(
    title: String,
    subtitle: String = "Sesión activa",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String = "Quitar"
) {
    var isRemoved by remember { mutableStateOf(false) }

    if (isRemoved) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(18.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = androidx.compose.ui.Modifier.clickable { isRemoved = true }
            ) {
                Text(
                    text = actionText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ManageDevicesScreenPreview() {
    EcoGuiaMobileTheme {
        ManageDevicesScreen({})
    }
}




