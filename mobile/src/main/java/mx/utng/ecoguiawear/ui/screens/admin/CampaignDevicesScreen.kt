/**
 * Archivo: CampaignDevicesScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla de selecciÃ³n de dispositivos para una campaÃ±a visual.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    programType: String = "gallery",
    onManageContentClick: () -> Unit
) {
    var selectedTVIndex by remember { mutableStateOf(0) }

    val programTitle = when (programType) {
        "public" -> "Colección Pública (Mapa AR)"
        "ranking" -> "Ranking Semanal (Top Likes)"
        else -> "Galería Móvil (Hotel / Museo)"
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
                Text("Pantallas Conectadas", color = Color.White, fontSize = 14.sp)
                Text(programTitle, color = EcoGuiaColors.Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Tv, null, tint = Color.White)
            }
        }

        // Card explicativa
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Smart TVs con Sesión Iniciada", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona la pantalla donde deseas transmitir la programación '$programTitle'. Solo se permite una transmisión activa por TV.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // TV Sessions List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Pantallas en Red Local (mus@ecoguia.com)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    DeviceItemToggle(
                        title = "Smart TV - Lobby Hotel Hidalgo",
                        subtitle = "Sesión activa · Resolución 4K",
                        isSelected = selectedTVIndex == 0,
                        onSelect = { selectedTVIndex = 0 }
                    )
                }
                item {
                    DeviceItemToggle(
                        title = "Smart TV - Sala 2 Museo Cuna",
                        subtitle = "Sesión activa · Pantalla Táctil",
                        isSelected = selectedTVIndex == 1,
                        onSelect = { selectedTVIndex = 1 }
                    )
                }
                item {
                    DeviceItemToggle(
                        title = "Totem TV - Recepción Principal",
                        subtitle = "Sesión activa · Modo Standby",
                        isSelected = selectedTVIndex == 2,
                        onSelect = { selectedTVIndex = 2 }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Button
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = "Iniciar Transmisión en TV",
                onClick = onManageContentClick
            )
        }
    }
}

@Composable
fun DeviceItemToggle(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, EcoGuiaColors.Jade) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tv, null, tint = if (isSelected) EcoGuiaColors.Jade else Color.Gray, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = EcoGuiaColors.Jade)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CampaignDevicesScreenPreview() {
    EcoGuiaMobileTheme {
        CampaignDevicesScreen(programType = "gallery", onManageContentClick = {})
    }
}





