@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Pantalla de exhibición 360° y visualización cartográfica 3D para Smart TV.
 *
 * Presenta una experiencia inmersiva para salas de museo y módulos turísticos:
 * - Rotación orbital automática y continua en torno al sitio histórico seleccionado (tilt 45°).
 * - Renderizado perimetral de áreas de detección (círculo del sitio activo y zonas de 30m de sitios vecinos).
 * - Marcadores geolocalizados de cápsulas culturales GeoDrops.
 * - Ficha de información histórica detallada y lista de cápsulas en panel lateral.
 * - Soporte para selección de estilos de mapa (Minimalista 3D, Modo Oscuro, Satélite) y bloqueo de modo kiosco con PIN.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.tv.ui.screens.components.KioskUnlockDialog
import mx.utng.ecoguiawear.tv.ui.screens.components.MapStyleSelectorDialog
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.MapStyles
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

/**
 * Tipos de vista y temas cartográficos disponibles en el Portal 360°.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
enum class MapViewType {
    /** Estilo minimalista claro con edificios y relieves en 3D. */
    MINIMAL_WHITE,

    /** Estilo nocturno de alto contraste con acentos dorados y jade. */
    DARK_MODE,

    /** Imagen satelital fotorrealista con nombres de calles y puntos de referencia. */
    SATELLITE_CITY
}

/**
 * Composable que renderiza la pantalla del Portal 360° en la Smart TV.
 *
 * @param isKioskLocked Indica si el modo de exhibición protegida (kiosco) está activado.
 * @param onToggleKioskLock Callback para alternar el estado de bloqueo kiosco.
 * @param onBack Callback para regresar al Lobby principal.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun Portal360Screen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Estado reactivo que detecta si la sesión fue eliminada en cualquier momento
    var isNoSession by remember { mutableStateOf(prefs.getString("saved_paired_user_id", null) == null) }
    val repository = remember { EcoGuiaRepositoryImpl() }

    // Monitorear SharedPreferences en tiempo real para alternar inmediatamente a Skeleton y navegar al Lobby
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "saved_paired_user_id" || key == "saved_is_paired") {
                val userId = sharedPreferences.getString("saved_paired_user_id", null)
                isNoSession = userId == null
                if (userId == null) {
                    onBack()
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    var site by remember { mutableStateOf<RemoteHistoricalSite?>(null) }
    var geoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var otherSites by remember { mutableStateOf<List<RemoteHistoricalSite>>(emptyList()) }

    var selectedMapType by remember { mutableStateOf(MapViewType.MINIMAL_WHITE) }
    var showStyleDialog by remember { mutableStateOf(false) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (showStyleDialog) {
            showStyleDialog = false
        } else if (showUnlockDialog) {
            showUnlockDialog = false
        } else if (isKioskLocked) {
            showUnlockDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedSiteId = prefs.getString("saved_selected_site_id", null)
        if (savedUserId == null) {
            // Sin sesión activa: no cargar datos de ningún sitio
            site = null
            geoDrops = emptyList()
            otherSites = emptyList()
            return@LaunchedEffect
        }

        try {
            val fetchedSite = withContext(Dispatchers.IO) {
                if (savedSiteId != null) {
                    val isAdmin = prefs.getString("saved_paired_user_role", "") == "admin"
                    val all = repository.getSitesByOwnerOrAdmin(savedUserId, isAdmin)
                    all.find { it.id == savedSiteId } ?: repository.getSiteByOwner(savedUserId)
                } else {
                    repository.getSiteByOwner(savedUserId)
                }
            }
            site = fetchedSite
            if (fetchedSite != null) {
                // Filtrar GeoDrops de forma estricta por el sitio seleccionado
                geoDrops = withContext(Dispatchers.IO) { repository.getGeoDropsBySite(fetchedSite.id) }
                // Cargar todos los sitios históricos para renderizar las áreas de proximidad de 30m de los demás sitios
                val allHistoricalSites = withContext(Dispatchers.IO) { repository.getHistoricalSites() }
                otherSites = allHistoricalSites.filter { it.id != fetchedSite.id }
            } else {
                geoDrops = emptyList()
                otherSites = emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("TVMap", "Error al cargar datos del sitio: ${e.message}")
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

    var zoomLevel by remember { mutableStateOf(20.5f) }
    val siteLatLng = LatLng(site?.latitude ?: 21.15765, site?.longitude ?: -100.934467)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.builder().target(siteLatLng).zoom(zoomLevel).tilt(45f).bearing(0f).build()
    }

    LaunchedEffect(siteLatLng, zoomLevel) {
        var bearing = 0f
        // Pausa inicial de 1.5s para que la TV renderice la escena antes de iniciar animaciones
        kotlinx.coroutines.delay(1500)
        while (true) {
            bearing = (bearing + 20f) % 360f
            val update = CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                    .target(siteLatLng)
                    .zoom(zoomLevel)
                    .tilt(45f)
                    .bearing(bearing)
                    .build()
            )
            // Animar el giro durante 3 segundos a 60 FPS
            cameraPositionState.animate(update, durationMs = 3000)
            // Pausa de reposo de 1.5s para liberar la CPU de la TV y procesar eventos del control remoto
            kotlinx.coroutines.delay(4500)
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
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            // Header Responsivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = site?.name ?: "Mapa del Sitio Histórico",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
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
                        text = site?.address ?: "Dolores Hidalgo - Cuna de la Independencia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrushedGold,
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { zoomLevel = (zoomLevel + 0.5f).coerceAtMost(21f) },
                        colors = tvButtonColors
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Acercar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { zoomLevel = (zoomLevel - 0.5f).coerceAtLeast(17f) },
                        colors = tvButtonColors
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Alejar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("-", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { showStyleDialog = true },
                        colors = tvButtonColors
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Estilo Mapa", fontSize = 12.sp)
                        }
                    }

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

                    // Ocultar botón de Volver al Lobby cuando la pantalla esté bloqueada
                    if (!isKioskLocked) {
                        Button(
                            onClick = onBack,
                            colors = tvButtonColors
                        ) {
                            Text("Lobby", fontSize = 12.sp)
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Panel Izquierdo: Mapa 3D expandido proporcionalmente
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                ) {
                    val currentMapType = if (selectedMapType == MapViewType.SATELLITE_CITY) MapType.HYBRID else MapType.NORMAL
                    val currentStyleOptions = when (selectedMapType) {
                        MapViewType.MINIMAL_WHITE -> MapStyles.minimalWhite3DStyle
                        MapViewType.DARK_MODE -> MapStyles.darkModeStyle
                        MapViewType.SATELLITE_CITY -> null
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isBuildingEnabled = true, mapType = currentMapType, mapStyleOptions = currentStyleOptions),
                        uiSettings = MapUiSettings(scrollGesturesEnabled = true, zoomGesturesEnabled = true, zoomControlsEnabled = false)
                    ) {
                        if (site != null) {
                            // Crear icono de GeoDrop de forma segura una vez inicializado el contexto de Google Maps
                            val customGeoDropIcon = remember {
                                try {
                                    val bitmap = android.graphics.Bitmap.createBitmap(44, 44, android.graphics.Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(bitmap)
                                    
                                    val bgPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#0F5A3E")
                                        isAntiAlias = true
                                    }
                                    val borderPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#34D399")
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 4f
                                        isAntiAlias = true
                                    }
                                    val textPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 22f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                    }
                                    
                                    canvas.drawCircle(22f, 22f, 20f, bgPaint)
                                    canvas.drawCircle(22f, 22f, 19f, borderPaint)
                                    canvas.drawText("", 22f, 29f, textPaint)
                                    
                                    com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            // Área de detección principal del sitio actual
                            Circle(
                                center = siteLatLng, 
                                radius = (site?.detectionRadiusM ?: 50).toDouble(), 
                                fillColor = Color(0x330F5A3E), 
                                strokeColor = JadeGreen, 
                                strokeWidth = 3f
                            )

                            // GeoDrops exclusivos del sitio activo con icono/badge personalizado pequeño
                            geoDrops.forEach { drop ->
                                if (drop.latitude != null && drop.longitude != null) {
                                    Marker(
                                        state = MarkerState(position = LatLng(drop.latitude!!, drop.longitude!!)), 
                                        title = drop.title, 
                                        snippet = "Geo-Drop de ${site?.name}",
                                        icon = customGeoDropIcon
                                    )
                                }
                            }

                            // Áreas de proximidad de otros sitios a 30 metros (SOLO CÍRCULOS, SIN MARCADOR GOOGLE)
                            otherSites.forEach { other ->
                                val otherLat = other.latitude ?: return@forEach
                                val otherLng = other.longitude ?: return@forEach
                                val otherLatLng = LatLng(otherLat, otherLng)
                                Circle(
                                    center = otherLatLng,
                                    radius = 30.0, // Radio exacto de 30 metros
                                    fillColor = Color(0x1ADB3A40), // Color ámbar/rojo sutil
                                    strokeColor = Color(0xFFF59E0B),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }

                // Panel Derecho: Ficha Informativa / Skeleton
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isNoSession) {
                        mx.utng.ecoguiawear.tv.ui.screens.components.NoSessionOverlay("Vincula tu cuenta para cargar la información del sitio")
                        Card(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.tv.material3.Icon(Icons.Default.Info, contentDescription = null, tint = BrushedGold, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Información General", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.9f, height = 14.dp)
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.75f, height = 14.dp)
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.6f, height = 14.dp)
                            }
                        }

                        Card(onClick = {}, modifier = Modifier.fillMaxWidth().weight(1f), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Geo-Drops en el Sitio (--)", color = BrushedGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                repeat(2) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonBox(modifier = Modifier.size(40.dp), cornerRadius = 8.dp)
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.8f, height = 12.dp)
                                            mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.5f, height = 10.dp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Card(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.tv.material3.Icon(Icons.Default.Info, contentDescription = null, tint = BrushedGold, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Información General", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = site?.historicalDescription ?: site?.shortDescription ?: "Sitio histórico registrado en EcoGuía.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }

                        Card(onClick = {}, modifier = Modifier.fillMaxWidth().weight(1f), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Geo-Drops en el Sitio (${geoDrops.size})", color = BrushedGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                if (geoDrops.isEmpty()) {
                                    Text("No hay cápsulas en este sitio.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(geoDrops) { drop ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                androidx.tv.material3.Icon(Icons.Default.Place, contentDescription = null, tint = JadeGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(drop.title, color = Color.White, fontSize = 12.sp)
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

        if (showStyleDialog) {
            MapStyleSelectorDialog(selectedMapType = selectedMapType, onSelectMapType = { selectedMapType = it; showStyleDialog = false }, onDismiss = { showStyleDialog = false })
        }
        if (showUnlockDialog) {
            KioskUnlockDialog(
                onUnlockConfirmed = {
                    onToggleKioskLock(false)
                    showUnlockDialog = false
                },
                onDismiss = { showUnlockDialog = false }
            )
        }

    }
}
