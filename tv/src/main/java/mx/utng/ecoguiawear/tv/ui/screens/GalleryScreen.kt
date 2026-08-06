@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Pantalla de galería y presentación continua (Slideshow) de cápsulas GeoDrops para Smart TV.
 *
 * Diseñada para exhibiciones públicas en pantallas de gran formato:
 * - Carga y filtra las cápsulas comunitarias aprobadas pertenecientes al sitio histórico vinculado.
 * - Ejecuta una transición cíclica automática cada 5 segundos entre las fotografías y descripciones registradas.
 * - Integra estados de carga tipo Skeleton cuando no hay sesión activa o mientras se descargan datos desde Neon DB.
 * - Soporta protección de modo kiosco con PIN y manejo del botón Back del control remoto.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import coil.compose.AsyncImage
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguiawear.tv.ui.screens.components.NoSessionOverlay
import mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonBox
import mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonImageCard
import mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

/**
 * Composable que renderiza la galería de GeoDrops y su carrusel automatizado en la Smart TV.
 *
 * @param isKioskLocked Indica si el modo de bloqueo de exhibición (kiosco) está activado.
 * @param onToggleKioskLock Callback para alternar el estado de bloqueo con PIN.
 * @param onBack Callback para regresar al Lobby principal.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun GalleryScreen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Estado reactivo que detecta si la sesión fue eliminada en cualquier momento
    var isNoSession by remember { mutableStateOf(prefs.getString("saved_paired_user_id", null) == null) }

    val repository = remember { EcoGuiaRepositoryImpl() }
    var geoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNoSession) }
    var currentIndex by remember { mutableStateOf(0) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    // Monitorear SharedPreferences en tiempo real para alternar inmediatamente a Skeleton y navegar al Lobby
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "saved_paired_user_id" || key == "saved_is_paired") {
                val userId = sharedPreferences.getString("saved_paired_user_id", null)
                isNoSession = userId == null
                if (userId == null) {
                    geoDrops = emptyList()
                    onBack()
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    BackHandler(enabled = true) {
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
                repository.getGeoDrops()
            }
            geoDrops = drops
        } catch (e: Exception) {
            android.util.Log.e("TVGallery", "Error cargando GeoDrops: ${e.message}")
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

    // Transición automática cada 5 segundos si hay Geo-Drops
    LaunchedEffect(geoDrops) {
        if (geoDrops.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                currentIndex = (currentIndex + 1) % geoDrops.size
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Galería Móvil & Geo-Drops",
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
                        text = "Presentación de fotos e información capturada por visitantes",
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
                                    imageVector = androidx.compose.material.icons.Icons.Default.Lock,
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


            if (isNoSession) {
                // ── SKELETON MODE: Sin sesión activa ─────────────────────────────
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NoSessionOverlay("Vincula tu cuenta para ver los Geo-Drops capturados")
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Imagen skeleton gris
                                SkeletonImageCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                // Líneas de texto skeleton
                                SkeletonTextLine(widthFraction = 0.75f, height = 18.dp)
                                SkeletonTextLine(widthFraction = 0.55f, height = 14.dp)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SkeletonBox(
                                        modifier = Modifier.size(28.dp),
                                        cornerRadius = 14.dp
                                    )
                                    SkeletonTextLine(widthFraction = 0.6f, height = 14.dp)
                                }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando galería de Geo-Drops...", color = Color.White.copy(alpha = 0.7f))
                }
            } else if (geoDrops.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        onClick = {},
                        modifier = Modifier.padding(24.dp),
                        colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = JadeGreen,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay Geo-Drops registrados",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Abre la aplicación móvil en tu teléfono para crear la primera cápsula histórica.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                val currentDrop = geoDrops[currentIndex]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Foto del Geo-Drop
                    Card(
                        onClick = {},
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (!currentDrop.mediaUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentDrop.mediaUrl,
                                    contentDescription = currentDrop.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(DeepBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        androidx.tv.material3.Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = JadeGreen,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Sin imagen adjunta",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Información del Geo-Drop a un lado
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
                                    text = " Geo-Drop Aprobado",
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
