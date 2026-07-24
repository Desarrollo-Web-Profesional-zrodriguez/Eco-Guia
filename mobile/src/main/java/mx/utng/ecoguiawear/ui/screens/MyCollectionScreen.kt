/**
 * Archivo: MyCollectionScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-21
 * Descripción: Pantalla de colección personal del usuario. Muestra elementos guardados como fotos, rutas y audios.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Composable que representa la pantalla "Mi Colección".
 */
@Composable
fun MyCollectionScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Fotos", "Rutas", "Audio")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoTopBar(
            title = "Mi Colección",
            subtitle = "Guardados",
            actionIcon = Icons.Default.Search,
            onActionClick = { }
        )

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("18 guardados en tu colección", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Fotos, rutas y cápsulas que has guardado durante tus viajes.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == index) EcoGuiaColors.Jade else Color.Transparent)
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title,
                                color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Lista de Recientes
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Recientes", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(3) { index ->
                    CollectionItemPlaceholder(index)
                }
            }
        }
    }
}

@Composable
fun CollectionItemPlaceholder(index: Int) {
    val titles = listOf("Detalle del arco principal", "Mural comunitario", "Ruta Independencia")
    val subtitles = listOf("Museo - Guardado hoy", "Foto - Hace 2 días", "Ruta - 3 items guardados")
    
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
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Icon placeholder
            }

            Column(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
            ) {
                Text(
                    text = titles[index % 3],
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitles[index % 3],
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Text("Ver", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyCollectionScreenPreview() {
    EcoGuiaMobileTheme {
        MyCollectionScreen()
    }
}
