@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Pantalla de análisis de afluencia, métricas y ranking semanal de GeoDrops para Smart TV.
 *
 * Ofrece un panel visual informativo para administradores y visitantes:
 * - Clasificación y podio de las cápsulas y puntos de interés más destacados y visitados de la semana.
 * - Carrusel paginado por bloques de 3 elementos con rotación automática cada 8 segundos.
 * - Resumen de métricas de cápsulas activas asociadas al sitio histórico vinculado.
 * - Interfaz optimizada con estados visuales Skeleton ante desconexión o falta de sesión y bloqueo de modo kiosco con PIN.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place

import androidx.compose.material.icons.filled.Star
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
import coil.compose.AsyncImage
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

/**
 * Composable principal que renderiza el panel de ranking semanal y métricas turísticas en la Smart TV.
 *
 * Administra el ciclo de vida de la sesión activa, la carga asíncrona de cápsulas GeoDrops desde Neon PostgreSQL,
 * la paginación automatizada en bloques de 3 elementos y la intercepción de eventos de control remoto en modo kiosco.
 *
 * @param isKioskLocked Indica si la pantalla se encuentra bloqueada en modo de exhibición protegida.
 * @param onToggleKioskLock Callback para alternar el estado de bloqueo kiosco.
 * @param onBack Callback para regresar a la pantalla de Lobby.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun HeatmapScreen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Estado reactivo que detecta si la sesión fue eliminada en cualquier momento
    var isNoSession by remember { mutableStateOf(prefs.getString("saved_paired_user_id", null) == null) }

    val repository = remember { EcoGuiaRepositoryImpl() }
    var rankingGeoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNoSession) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    // Monitorear SharedPreferences en tiempo real para alternar inmediatamente a Skeleton y navegar al Lobby
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "saved_paired_user_id" || key == "saved_is_paired") {
                val userId = sharedPreferences.getString("saved_paired_user_id", null)
                isNoSession = userId == null
                if (userId == null) {
                    rankingGeoDrops = emptyList()
                    onBack()
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val pageSize = 3

    androidx.activity.compose.BackHandler(enabled = true) {
        if (isKioskLocked) {
            showUnlockDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        if (isNoSession) return@LaunchedEffect
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedSiteId = prefs.getString("saved_selected_site_id", null)
        try {
            val targetSite = if (savedUserId != null && savedSiteId != null) {
                val isAdmin = prefs.getString("saved_paired_user_role", "") == "admin"
                val all = repository.getSitesByOwnerOrAdmin(savedUserId, isAdmin)
                all.find { it.id == savedSiteId } ?: repository.getSiteByOwner(savedUserId)
            } else if (savedUserId != null) {
                repository.getSiteByOwner(savedUserId)
            } else null

            val drops = if (targetSite != null) {
                repository.getGeoDropsBySite(targetSite.id)
            } else {
                repository.getTopRankingGeoDrops(12)
            }
            rankingGeoDrops = drops
        } catch (e: Exception) {
            android.util.Log.e("TVRanking", "Error cargando Ranking GeoDrops: ${e.message}")
        } finally {
            isLoading = false
        }

        // Verificación de respaldo constante de sesión activa: fuerza el regreso al Lobby si la sesión desaparece
        while (true) {
            val currentUserId = prefs.getString("saved_paired_user_id", null)
            if (currentUserId == null || !prefs.getBoolean("saved_is_paired", false)) {
                onBack()
                break
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    // Carrusel Automático: Rotación por bloques (grupos de 3 Geo-Drops) cada 8 segundos
    val totalPages = remember(rankingGeoDrops) {
        if (rankingGeoDrops.isEmpty()) 1 else (rankingGeoDrops.size + pageSize - 1) / pageSize
    }

    LaunchedEffect(totalPages) {
        if (totalPages > 1) {
            while (true) {
                kotlinx.coroutines.delay(8000)
                currentPageIndex = (currentPageIndex + 1) % totalPages
            }
        }
    }

    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
        containerColor = DeepBlue,
        contentColor = Color.White,
        focusedContainerColor = Color.White,
        focusedContentColor = DeepBlue
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(28.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ranking Semanal & Resumen de Visitas",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (isKioskLocked) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDC2626), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("BLOQUEADO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "Top de cápsulas y lugares más valorados de la semana",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BrushedGold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isNoSession) {
                        Button(
                            onClick = {
                                if (isKioskLocked) showUnlockDialog = true else onToggleKioskLock(true)
                            },
                            colors = tvButtonColors
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.tv.material3.Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isKioskLocked) "Bloqueado (PIN)" else "Bloquear", fontSize = 12.sp)
                            }
                        }
                    }

                    if (!isKioskLocked) {
                        Button(
                            onClick = onBack,
                            colors = tvButtonColors
                        ) {
                            Text("Volver al Lobby", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resumen de Métricas del Sitio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.tv.material3.Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Cápsulas Geo-Drops",
                            tint = JadeGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Cápsulas Registradas en el Sitio", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            if (isNoSession) {
                                Text("-- Geo-Drops", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            } else {
                                Text("${rankingGeoDrops.size} Geo-Drops Activos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Carrusel de Bloques de Geo-Drops (Grupo de 3)
            if (isNoSession) {
                // Modo Skeleton sin sesión
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    mx.utng.ecoguiawear.tv.ui.screens.components.NoSessionOverlay("Vincula tu cuenta de museo para habilitar las métricas reales")
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonImageCard(
                                    modifier = Modifier.fillMaxWidth().height(120.dp)
                                )
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.8f, height = 16.dp)
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.5f, height = 12.dp)
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonStat()
                                    mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonStat()
                                }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando el ranking semanal...", color = Color.White.copy(alpha = 0.7f))
                }
            } else if (rankingGeoDrops.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay Geo-Drops para mostrar en el ranking.", color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                val currentGroup = remember(currentPageIndex, rankingGeoDrops) {
                    val fromIndex = currentPageIndex * pageSize
                    val toIndex = minOf(fromIndex + pageSize, rankingGeoDrops.size)
                    if (fromIndex < rankingGeoDrops.size) rankingGeoDrops.subList(fromIndex, toIndex) else emptyList()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Contenido del bloque actual animado
                    AnimatedContent(
                        targetState = currentPageIndex,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier.weight(1f),
                        label = "GroupBlockCarousel"
                    ) { _ ->
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            currentGroup.forEachIndexed { groupIndex, drop ->
                                val globalIndex = (currentPageIndex * pageSize) + groupIndex
                                Card(
                                    onClick = {},
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(DeepBlue)
                                        ) {
                                            if (!drop.mediaUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = drop.mediaUrl,
                                                    contentDescription = drop.title,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    androidx.tv.material3.Icon(
                                                        imageVector = Icons.Default.Place,
                                                        contentDescription = null,
                                                        tint = JadeGreen,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                                    .background(
                                                        when (globalIndex) {
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
                                                    text = "#${globalIndex + 1}",
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
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = drop.description ?: "Sin descripción",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Cápsula activa", color = BrushedGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("Radio: ${drop.detectionRadiusM}m", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            // Rellenar espacio vacío si el último bloque tiene menos de 3 Geo-Drops
                            if (currentGroup.size < pageSize) {
                                repeat(pageSize - currentGroup.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra de Navegación del Carrusel por Bloques
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (currentPageIndex > 0) currentPageIndex--
                                else currentPageIndex = totalPages - 1
                            },
                            colors = tvButtonColors,
                            modifier = Modifier.height(36.dp)
                        ) {
                            androidx.tv.material3.Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior Bloque", modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Bloque ${currentPageIndex + 1} de $totalPages (${rankingGeoDrops.size} Geo-Drops)",
                            color = BrushedGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                currentPageIndex = (currentPageIndex + 1) % totalPages
                            },
                            colors = tvButtonColors,
                            modifier = Modifier.height(36.dp)
                        ) {
                            androidx.tv.material3.Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente Bloque", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        if (showUnlockDialog) {
            mx.utng.ecoguiawear.tv.ui.screens.components.KioskUnlockDialog(
                onUnlockConfirmed = {
                    onToggleKioskLock(false)
                    showUnlockDialog = false
                },
                onDismiss = { showUnlockDialog = false }
            )
        }
    }
}
