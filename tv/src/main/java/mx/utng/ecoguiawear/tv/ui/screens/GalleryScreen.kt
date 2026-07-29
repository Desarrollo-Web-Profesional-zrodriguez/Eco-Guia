@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Place
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
fun GalleryScreen(onBack: () -> Unit) {
    val repository = remember { EcoGuiaRepositoryImpl() }
    var geoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val drops = repository.getGeoDrops()
            geoDrops = drops
        } catch (e: Exception) {
            android.util.Log.e("TVGallery", "Error cargando GeoDrops: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Transición automática cada 5 segundos si hay Geo-Drops
    LaunchedEffect(geoDrops) {
        if (geoDrops.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                currentIndex = (currentIndex + 1) % geoDrops.size
            }
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
                    text = "Galería Móvil & Geo-Drops",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Presentación de fotos e información capturada por visitantes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrushedGold
                )
            }

            Button(onClick = onBack) {
                Text("Volver al Lobby")
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando cápsulas de información...", color = Color.White)
            }
        } else if (geoDrops.isEmpty()) {
            // Pantalla de No Hay Geo-Drops -> Crear Geo-Drops
            Card(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(24.dp),
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
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Aún no hay Geo-Drops creados en este sitio!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Escanea y usa tu aplicación Eco-Guía Móvil para capturar y publicar la primera foto o cápsula histórica aquí.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            val currentDrop = geoDrops[currentIndex]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Panel Izquierdo: Foto Principal
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentDrop.mediaUrl.isNull_or_empty_ext()) {
                        Image(
                            painter = rememberAsyncImagePainter(currentDrop.mediaUrl),
                            contentDescription = currentDrop.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "GeoDrop",
                                tint = JadeGreen,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Geo-Drop Sin Imagen Directa", color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }

                // Panel Derecho: Información del Geo-Drop
                Card(
                    onClick = {},
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .background(DeepBlue, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Geo-Drop ${currentIndex + 1} de ${geoDrops.size}",
                                    color = BrushedGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = currentDrop.title,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentDrop.description ?: "Sin descripción adicional registrada para esta cápsula.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "❤️ ${currentDrop.likesCount} Likes de Visitantes",
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Radio: ${currentDrop.detectionRadiusM}m",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_empty_ext(): Boolean = this == null || this.trim().isEmpty()

