/**
 * Archivo: SearchExperienceScreen.kt
 * Autor: ZahirMora
 * Fecha de Ãºltima actualizaciÃ³n: 2026-07-21
 * DescripciÃ³n: Pantalla de bÃºsqueda de experiencias con filtros rÃ¡pidos para museos, cÃ¡psulas y chat IA.
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

@Composable
fun SearchExperienceScreen() {
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
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Buscar", color = Color.White, fontSize = 14.sp)
                Text("Experiencias", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Search, null, tint = Color.White)
            }
        }

        // Search Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Â¿QuÃ© quieres explorar?", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Encuentra museos, crea cÃ¡psulas o haz preguntas a la IA.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }

        // Quick Filters
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Filtros rÃ¡pidos", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    FilterItem(
                        title = "Museos",
                        subtitle = "12 sitios histÃ³ricos cerca",
                        icon = Icons.Default.Place
                    )
                }
                item {
                    FilterItem(
                        title = "Geo-Drops",
                        subtitle = "24 cÃ¡psulas activas",
                        icon = Icons.Default.AddCircle
                    )
                }
                item {
                    FilterItem(
                        title = "Preguntar a Hidalgo IA",
                        subtitle = "Respuesta en segundos",
                        icon = Icons.Default.AutoAwesome
                    )
                }
            }
        }
    }
}

@Composable
fun FilterItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchExperienceScreenPreview() {
    EcoGuiaMobileTheme {
        SearchExperienceScreen()
    }
}




