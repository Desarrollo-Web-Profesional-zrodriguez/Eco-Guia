/**
 * Archivo: CapsuleGalleryScreen.kt
 *
 * Pantalla para gestionar la galería y catálogo de cápsulas informativas y fotográficas.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.components.AdminBottomBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme

/**
 * Pantalla composable para administrar la galería de cápsulas multimedia.
 *
 * @param onNavigate Callback para navegar entre secciones de administración.
 */
@Composable
fun CapsuleGalleryScreen(
    onNavigate: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            AdminBottomBar(currentRoute = "capsule_gallery", onNavigate = onNavigate)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new capsule */ },
                containerColor = EcoGuiaColors.Jade,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar cápsula")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EcoGuiaColors.DeepBlue)
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Column {
                    Text("Contenido", color = EcoGuiaColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Galería de Cápsulas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "12 cápsulas encontradas",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, null, tint = EcoGuiaColors.Jade)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(12) { index ->
                        CapsuleCard(
                            title = "Cápsula #${index + 1}",
                            location = "Parque Hidalgo",
                            onClick = { /* View details */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CapsuleCard(
    title: String,
    location: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                
                // Badge for status
                Surface(
                    color = EcoGuiaColors.Jade,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        "Activa",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = location,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CapsuleGalleryScreenPreview() {
    EcoGuiaMobileTheme {
        CapsuleGalleryScreen({})
    }
}
