@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

@Composable
fun HeatmapScreen(onBack: () -> Unit) {
    val repository = remember { EcoGuiaRepositoryImpl() }
    var rankingGeoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            rankingGeoDrops = repository.getTopRankingGeoDrops(10)
        } catch (e: Exception) {
            android.util.Log.e("TVRanking", "Error cargando Ranking GeoDrops: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ranking Semanal & Resumen de Visitas",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Top de cápsulas y lugares más valorados de la semana",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrushedGold
                )
            }

            Button(onClick = onBack) {
                Text("Volver al Lobby")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ficha de Resumen de Visitas Estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.tv.material3.Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Visitas",
                        tint = BrushedGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Visitas Totales", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("1,248 Visitantes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Card(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.tv.material3.Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Likes",
                        tint = JadeGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Interacciones Totales", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("${rankingGeoDrops.sumOf { it.likesCount }} Me Gusta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de Carrusel del Ranking Semanal
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando Ranking Semanal...", color = Color.White)
            }
        } else if (rankingGeoDrops.isEmpty()) {
            // Pantalla cuando no hay Geo-Drops
            Card(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.tv.material3.Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Sin GeoDrops",
                        tint = BrushedGold,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Aún no hay Geo-Drops para mostrar en el Ranking!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Usa la aplicación Eco-Guía Móvil para crear y reaccionar a cápsulas en este sitio.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(rankingGeoDrops) { index, drop ->
                    Card(
                        onClick = {},
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight(),
                        colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!drop.mediaUrl.isNull_or_empty_ext()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(drop.mediaUrl),
                                        contentDescription = drop.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("Geo-Drop", color = Color.White.copy(alpha = 0.5f))
                                }

                                // Medalla / Posición del Ranking
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(
                                            when (index) {
                                                0 -> Color(0xFFF59E0B)
                                                1 -> Color(0xFF9CA3AF)
                                                2 -> Color(0xFFB45309)
                                                else -> DeepBlue
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = drop.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )

                            Text(
                                text = drop.description ?: "Sin descripción",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("❤️ ${drop.likesCount} Likes", color = BrushedGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Radio: ${drop.detectionRadiusM}m", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_empty_ext(): Boolean = this == null || this.trim().isEmpty()

