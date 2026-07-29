/**
 * Archivo: TVCampaignScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: GestiÃ³n de campaÃ±as visuales para Smart TVs en hoteles y museos (SalÃ³n de la Fama).
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
import androidx.compose.runtime.*

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
    onManageDevicesClick: (String) -> Unit
) {
    var selectedProgram by remember { mutableStateOf("gallery") }

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
                Text("Campañas & Emisión TV", color = Color.White, fontSize = 14.sp)
                Text("Programación para Museos y Hoteles", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = onAnalyticsClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(Icons.Default.Star, contentDescription = "Resumen Analítico", tint = EcoGuiaColors.Gold, modifier = Modifier.size(22.dp))
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
                Text("Salón de la Fama Visual", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Selecciona 1 programación activa para transmitir en las Smart TV conectadas de tu establecimiento.", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp
                )
            }
        }

        // Programming List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Modos de Programación Disponibles", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    CampaignItem(
                        title = "Galería Móvil",
                        subtitle = "Muestra todos los Geo-Drops y fotos capturadas en este hotel, sitio o museo.",
                        icon = Icons.Default.Tv,
                        isSelected = selectedProgram == "gallery",
                        onClick = {
                            selectedProgram = "gallery"
                            onManageDevicesClick("gallery")
                        }
                    )
                }
                item {
                    CampaignItem(
                        title = "Colección Pública (Mapa AR)",
                        subtitle = "Mapa centrado en las coordenadas del sitio con marcadores e ícono de visibilidad (Ojo).",
                        icon = Icons.Default.QrCode,
                        isSelected = selectedProgram == "public",
                        onClick = {
                            selectedProgram = "public"
                            onManageDevicesClick("public")
                        }
                    )
                }

                item {
                    CampaignItem(
                        title = "Ranking Semanal",
                        subtitle = "Muestra las cápsulas e imágenes más visitadas con mayor número de likes.",
                        icon = Icons.Default.AccountTree,
                        isSelected = selectedProgram == "ranking",
                        onClick = {
                            selectedProgram = "ranking"
                            onManageDevicesClick("ranking")
                        }
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
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, EcoGuiaColors.Jade) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) EcoGuiaColors.Jade else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = EcoGuiaColors.Jade)
            )
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




