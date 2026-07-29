/**
 * Archivo: MuseumPortal360Screen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-22
 * DescripciÃ³n: Pantalla de portal inmersivo que muestra la vista 360 del museo y puntos de interÃ©s.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*



import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun MuseumPortal360Screen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoGuiaColors.DeepBlue)
            .padding(24.dp)
    ) {
        Text(
            text = "Portal 360 del Museo",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Recorrido inmersivo para lobby y casa",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            // Visor simulado 360 y Mapa AR
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(260.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                // Simulación de marcadores de GeoDrop con ícono de Ojo 👁️
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Surface(
                        color = EcoGuiaColors.Jade,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Visibility,
                                contentDescription = "GeoDrop visible",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Geo-Drop Activo en Radio",
                        color = EcoGuiaColors.Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(EcoGuiaColors.DeepBlue.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Dolores Hidalgo - Centro Histórico", color = Color.White, fontSize = 10.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Métricas laterales
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PortalStatItem("360°", "vista AR")
                PortalStatItem("12", "GeoDrops 👁️")
                PortalStatItem("IA", "guía activa")
            }
        }


    }
}

@Composable
fun PortalStatItem(value: String, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = EcoGuiaColors.Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun MuseumPortal360ScreenPreview() {
    EcoGuiaMobileTheme {
        MuseumPortal360Screen()
    }
}

