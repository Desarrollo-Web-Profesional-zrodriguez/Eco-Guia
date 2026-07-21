/**
 * Archivo: ExplorationScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: Pantalla principal de exploración. Muestra el mapa y sitios recomendados.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import androidx.compose.ui.tooling.preview.Preview
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla de exploración.
 */
@Composable
fun ExplorationScreen(
    onAdminClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F4F1))
    ) {
        // Header Azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Explorar", color = EcoGuiaColors.Text, fontSize = 18.sp)
                Text("Cerca de ti", color = EcoGuiaColors.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = onAdminClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Admin",
                    tint = EcoGuiaColors.Gold
                )
            }
        }
        
        // Simulación de Mapa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F5E9)) // Color del mapa
        ) {
            // Aquí iría el mapa real, por ahora un placeholder visual
            Text("Mapa de Dolores Hidalgo", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
        }
        
        // Lista de Sitios
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Sitios recomendados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    RecommendedSiteItem(
                        title = "Museo de la Independencia",
                        subtitle = "Geo-Drops: 3 • A 20 m",
                        icon = Icons.Default.Place,
                        trailing = "Ir"
                    )
                }
                item {
                    RecommendedSiteItem(
                        title = "Parroquia de Dolores",
                        subtitle = "Ruta activa • A 350 m",
                        icon = Icons.Default.Star,
                        trailing = "1.2 km"
                    )
                }
            }
        }
    }
}

/**
 * Elemento de lista para un sitio recomendado.
 */
@Composable
fun RecommendedSiteItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0F2F1), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            
            Surface(
                color = Color(0xFFE0F2F1),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = trailing,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGuiaColors.Jade
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExplorationScreenPreview() {
    EcoGuiaMobileTheme {
        ExplorationScreen({})
    }
}
