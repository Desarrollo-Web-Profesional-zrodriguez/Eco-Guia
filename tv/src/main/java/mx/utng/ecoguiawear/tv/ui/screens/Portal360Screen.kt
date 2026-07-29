@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

@Composable
fun Portal360Screen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    val repository = remember { EcoGuiaRepositoryImpl() }

    var site by remember { mutableStateOf<RemoteHistoricalSite?>(null) }
    var geoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val savedUserId = prefs.getString("saved_paired_user_id", "2603b469-aa27-4eed-a6aa-dbce7fc145f5").orEmpty()
        try {
            val fetchedSite = repository.getSiteByOwner(savedUserId)
            site = fetchedSite
            if (fetchedSite != null) {
                geoDrops = repository.getGeoDropsBySite(fetchedSite.id)
            } else {
                geoDrops = repository.getGeoDrops()
            }
        } catch (e: Exception) {
            android.util.Log.e("TVMap", "Error al cargar datos del sitio: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val siteLatLng = LatLng(site?.latitude ?: 21.1561, site?.longitude ?: -100.935)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(siteLatLng, 16f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = site?.name ?: "Mapa del Sitio Histórico",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = site?.address ?: "Dolores Hidalgo - Cuna de la Independencia",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrushedGold
                )
            }

            Button(onClick = onBack) {
                Text("Volver al Lobby")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Panel Izquierdo: Mapa interactivo con Radio de Detección y Marcadores de Geo-Drops
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        zoomControlsEnabled = false
                    )
                ) {
                    // Círculo del Radio de Detección del Sitio Histórico (ej. 50 metros)
                    Circle(
                        center = siteLatLng,
                        radius = (site?.detectionRadiusM ?: 50).toDouble(),
                        fillColor = Color(0x330F5A3E),
                        strokeColor = JadeGreen,
                        strokeWidth = 3f
                    )

                    // Marcador del Sitio Principal
                    Marker(
                        state = MarkerState(position = siteLatLng),
                        title = site?.name ?: "Sitio Histórico",
                        snippet = site?.address
                    )

                    // Marcadores de Geo-Drops dados de alta
                    geoDrops.forEach { drop ->
                        val lat = drop.latitude
                        val lng = drop.longitude
                        if (lat != null && lng != null) {
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = drop.title,
                                snippet = "Likes: ${drop.likesCount}"
                            )
                        }
                    }

                }
            }

            // Panel Derecho: Descripción del Sitio e Información de Geo-Drops
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ficha Informativa del Sitio
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info Sitio",
                                tint = BrushedGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Información General",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = site?.historicalDescription ?: site?.shortDescription ?: "Sitio histórico registrado en la base de datos de EcoGuía.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Lista de Geo-Drops Dados de Alta
                Card(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Geo-Drops en el Sitio (${geoDrops.size})",
                            color = BrushedGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (geoDrops.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Aún no hay cápsulas registradas en este sitio.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(geoDrops) { drop ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(DeepBlue, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.tv.material3.Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = "Drop",
                                            tint = JadeGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(drop.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(drop.description ?: "Sin descripción", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        }
                                        Text("❤️ ${drop.likesCount}", color = BrushedGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

