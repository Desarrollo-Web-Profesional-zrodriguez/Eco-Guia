/**
 * Archivo: ActiveRouteScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla que muestra el progreso de la ruta turística activa y las próximas paradas.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
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
fun ActiveRouteScreen() {
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
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Column {
                Text("Ruta", color = Color.White, fontSize = 14.sp)
                Text("Independencia", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White)
            }
        }

        // Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Progreso de ruta", color = Color.White, fontWeight = FontWeight.Bold)
                Text("2 de 3 sitios visitados • 15 minutos restantes", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LinearProgressIndicator(
                    progress = { 0.66f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = EcoGuiaColors.Jade,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        // Stop List
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Próximas paradas", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    RouteStopItem(
                        number = 1,
                        title = "Museo de la Independencia",
                        subtitle = "Completado",
                        isCompleted = true
                    )
                }
                item {
                    RouteStopItem(
                        number = 2,
                        title = "Parroquia de Dolores",
                        subtitle = "A 350 m",
                        isCompleted = true
                    )
                }
                item {
                    RouteStopItem(
                        number = 3,
                        title = "Casa de Visitas",
                        subtitle = "A 820 m",
                        isCompleted = false
                    )
                }
            }
        }
    }
}

@Composable
fun RouteStopItem(
    number: Int,
    title: String,
    subtitle: String,
    isCompleted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isCompleted) EcoGuiaColors.Jade.copy(alpha = 0.1f) else Color(0xFFF1F4F1),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = if (isCompleted) EcoGuiaColors.Jade else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            if (isCompleted) {
                Icon(Icons.Default.Check, null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(1.dp, Color.LightGray, CircleShape)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActiveRouteScreenPreview() {
    EcoGuiaMobileTheme {
        ActiveRouteScreen()
    }
}
