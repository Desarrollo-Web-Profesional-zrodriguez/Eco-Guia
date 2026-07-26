/**
 * Archivo: ExplorationScreen.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-25
 * Descripción: Pantalla principal de exploración cultural. Integra un mapa de Google interactivo,
 * visualización de marcadores de sitios históricos y una lista reactiva de lugares cercanos.
 * Incluye un BottomSheet de detalle de sitio con botón "Guardar en Colección".
 *
 * Funciones destacadas:
 * - ExplorationScreen: Composable principal que coordina el mapa, la lista y el guardado.
 * - SiteDetailSheet: BottomSheet con info del sitio y acciones (guardar / navegar).
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import androidx.compose.ui.tooling.preview.Preview
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.CollectionViewModel
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel

/**
 * Composable que representa la pantalla de exploración.
 * @param userId ID del usuario autenticado. Requerido para guardar sitios en la colección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationScreen(
    onAdminClick: () -> Unit,
    onOpenRoutes: () -> Unit = {},
    userId: String = "guest",
    locationViewModel: LocationViewModel = viewModel(),
    collectionViewModel: CollectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentLocation by locationViewModel.currentLocation
    val nearbySites by locationViewModel.nearbySites
    val closestSite by locationViewModel.closestSite
    val scope = rememberCoroutineScope()

    var isFollowingUser by remember { mutableStateOf(true) }

    // BottomSheet state — sitio seleccionado para ver detalle
    var selectedSite by remember { mutableStateOf<RemoteHistoricalSite?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Iniciar actualizaciones de ubicación al entrar
    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.1561, -100.9350), 15f)
    }

    // Centrar cámara en el usuario SOLO si isFollowingUser es true
    LaunchedEffect(currentLocation) {
        if (isFollowingUser) {
            currentLocation?.let {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17f)
                )
            }
        }
    }

    // Detectar si el usuario mueve el mapa manualmente para desactivar el seguimiento
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            isFollowingUser = false
        }
    }

    // Precargar estado de guardado cuando se carga la pantalla con userId real
    LaunchedEffect(userId, nearbySites) {
        if (userId != "guest") {
            nearbySites.forEach { site ->
                collectionViewModel.checkIfSaved(userId, site.id)
            }
        }
    }

    var displayLimit by remember { mutableIntStateOf(5) }

    // Ordenamiento: Más cercanos primero, divididos en 2 grupos (Favoritos primero, luego Normales)
    val sortedSites = remember(nearbySites, collectionViewModel.savedSiteIds, currentLocation) {
        nearbySites.sortedWith(
            compareByDescending<RemoteHistoricalSite> { site ->
                collectionViewModel.savedSiteIds[site.id] == true
            }.thenBy { site ->
                if (currentLocation != null && site.latitude != null && site.longitude != null) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        currentLocation!!.latitude, currentLocation!!.longitude,
                        site.latitude!!, site.longitude!!, results
                    )
                    results[0]
                } else Float.MAX_VALUE
            }
        )
    }

    val visibleSites = sortedSites.take(displayLimit)

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoTopBar(
            title = if (closestSite != null) "¡Sitio Detectado!" else "Cerca de ti",
            subtitle = closestSite?.name ?: "Explorar",
            actionIcon = Icons.Default.AddCircle,
            onActionClick = onAdminClick
        )

        val mapContent: @Composable (Modifier) -> Unit = { modifier ->
            Box(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8F5E9))
            ) {
                val isSystemInDark = isSystemInDarkTheme()
                val mapStyleOptions = remember(isSystemInDark) {
                    if (isSystemInDark) {
                        com.google.android.gms.maps.model.MapStyleOptions(
                            """[{"elementType":"geometry","stylers":[{"color":"#242f3e"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},{"elementType":"labels.text.stroke","stylers":[{"color":"#242f3e"}]},{"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#263c3f"}]},{"featureType":"poi.park","elementType":"labels.text.fill","stylers":[{"color":"#6b9a76"}]},{"featureType":"road","elementType":"geometry","stylers":[{"color":"#38414e"}]},{"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212a37"}]},{"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#9ca5b3"}]},{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#746855"}]},{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1f2835"}]},{"featureType":"road.highway","elementType":"labels.text.fill","stylers":[{"color":"#f3d19c"}]},{"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2f3948"}]},{"featureType":"transit.station","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#17263c"}]},{"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]},{"featureType":"water","elementType":"labels.text.stroke","stylers":[{"color":"#17263c"}]}]"""
                        )
                    } else null
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = currentLocation != null,
                        mapStyleOptions = mapStyleOptions
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false
                    )
                ) {
                    nearbySites.forEach { site ->
                        val siteLat = site.latitude ?: return@forEach
                        val siteLng = site.longitude ?: return@forEach
                        val sitePos = LatLng(siteLat, siteLng)
                        val customIcon = remember(site.siteType) {
                            getCustomCategoryMarkerIcon(context, site.siteType)
                        }

                        Circle(
                            center = sitePos,
                            radius = site.detectionRadiusM.toDouble(),
                            strokeColor = EcoGuiaColors.Jade,
                            strokeWidth = 3f,
                            fillColor = EcoGuiaColors.Jade.copy(alpha = 0.15f)
                        )

                        Marker(
                            state = MarkerState(position = sitePos),
                            title = site.name,
                            snippet = site.shortDescription,
                            icon = customIcon
                        )
                    }
                }

                // Botón de Mira (Ubicación) - Superior Derecha
                IconButton(
                    onClick = {
                        isFollowingUser = true
                        currentLocation?.let { loc ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17f)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(if (isFollowingUser) EcoGuiaColors.Jade else Color.White, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = if (isFollowingUser) Color.White else EcoGuiaColors.DeepBlue
                    )
                }

                // Controles de Zoom Personalizados - Inferior Derecha
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                        },
                        modifier = Modifier
                            .background(EcoGuiaColors.Jade, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, "Zoom In", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                        },
                        modifier = Modifier
                            .background(EcoGuiaColors.Jade, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Zoom Out", tint = Color.White)
                    }
                }
            }
        }

        val listContent: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier = modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (nearbySites.isEmpty()) "Buscando sitios..." else "Sitios recomendados (${visibleSites.size}/${sortedSites.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = {
                        onOpenRoutes()
                    }) {
                        Text("Ver Rutas", color = EcoGuiaColors.Gold, fontWeight = FontWeight.Bold)
                    }
                }

                val isLoading by locationViewModel.isLoading

                if (isLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Gray.copy(alpha = 0.3f))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.6f)
                                                .height(14.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Gray.copy(alpha = 0.3f))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.4f)
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Gray.copy(alpha = 0.2f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            count = visibleSites.size,
                            key = { index -> visibleSites[index].id }
                        ) { index ->
                            val site = visibleSites[index]
                            val isFavorite = collectionViewModel.savedSiteIds[site.id] == true

                            val dismissState = key(site.id, isFavorite) {
                                rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.EndToStart && userId != "guest") {
                                            if (isFavorite) {
                                                collectionViewModel.removeSite(userId, site.id)
                                            } else {
                                                collectionViewModel.saveSite(userId, site.id)
                                            }
                                        }
                                        false
                                    }
                                )
                            }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                val backgroundColor = if (isFavorite) Color(0xFFE53935) else EcoGuiaColors.Jade
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(backgroundColor, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isFavorite) "Quitar de Favoritos" else "Agregar a Favoritos",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                                            contentDescription = if (isFavorite) "Quitar" else "Agregar",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            RecommendedSiteItem(
                                title = site.name,
                                subtitle = site.siteType + " • " + (site.address ?: ""),
                                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.Place,
                                trailing = "Ver",
                                onVerClick = {
                                    isFollowingUser = false
                                    selectedSite = site
                                    if (userId != "guest") {
                                        collectionViewModel.checkIfSaved(userId, site.id)
                                    }
                                }
                            )
                        }
                    }

                    if (sortedSites.size > displayLimit) {
                        item {
                            TextButton(
                                onClick = { displayLimit += 5 },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text("Cargar más sitios (+5)", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                mapContent(Modifier.weight(1f).fillMaxHeight())
                listContent(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            mapContent(Modifier.fillMaxWidth().height(260.dp))
            listContent(Modifier.weight(1f))
        }
    }

    // BottomSheet de detalle del sitio seleccionado
    selectedSite?.let { site ->
        ModalBottomSheet(
            onDismissRequest = { selectedSite = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            SiteDetailSheet(
                site = site,
                userId = userId,
                collectionViewModel = collectionViewModel,
                onNavigate = {
                    // Sincronizar con el reloj y centrar mapa
                    locationViewModel.syncTargetWithWatch(site)
                    val siteLat = site.latitude
                    val siteLng = site.longitude
                    if (siteLat != null && siteLng != null) {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(siteLat, siteLng), 17f)
                            )
                        }
                    }
                    selectedSite = null
                },
                onDismiss = { selectedSite = null }
            )
        }
    }
}

/**
 * Contenido del BottomSheet de detalle de un sitio histórico.
 * Muestra información del sitio y permite guardarlo en la colección o navegar hacia él.
 */
@Composable
fun SiteDetailSheet(
    site: RemoteHistoricalSite,
    userId: String,
    collectionViewModel: CollectionViewModel,
    onNavigate: () -> Unit,
    onDismiss: () -> Unit
) {
    val isSaved = collectionViewModel.savedSiteIds[site.id] == true

    val saveButtonColor by animateColorAsState(
        targetValue = if (isSaved) EcoGuiaColors.Jade else EcoGuiaColors.Surface,
        animationSpec = tween(300),
        label = "save_color"
    )
    val saveButtonTextColor by animateColorAsState(
        targetValue = if (isSaved) Color.White else EcoGuiaColors.Jade,
        animationSpec = tween(300),
        label = "save_text_color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Handle visual
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Ícono + nombre del sitio
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(EcoGuiaColors.Jade.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏛️", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = site.siteType,
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoGuiaColors.Jade
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dirección
        val address = site.address.orEmpty()
        if (address.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = EcoGuiaColors.Jade,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Descripción corta
        val shortDesc = site.shortDescription.orEmpty()
        if (shortDesc.isNotBlank()) {
            Text(
                text = shortDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Guardar / Guardado (toggle)
            Button(
                onClick = {
                    if (userId != "guest") {
                        collectionViewModel.toggleSave(userId, site.id)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = saveButtonColor,
                    contentColor = saveButtonTextColor
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (!isSaved) ButtonDefaults.outlinedButtonBorder else null
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSaved) "Guardado" else "Guardar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Botón Navegar (sincroniza con el reloj)
            Button(
                onClick = onNavigate,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Gold,
                    contentColor = EcoGuiaColors.DeepBlue
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Navegar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Aviso si no está autenticado
        if (userId == "guest") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inicia sesión para guardar en tu colección",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
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
    trailing: String,
    onVerClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onVerClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = EcoGuiaColors.Jade.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                onClick = onVerClick
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

/**
 * Devuelve un BitmapDescriptor personalizado para Google Maps generando un marcador circular 
 * con color distintivo según la categoría del sitio.
 */
fun getCustomCategoryMarkerIcon(context: android.content.Context, siteType: String): com.google.android.gms.maps.model.BitmapDescriptor {
    val type = siteType.lowercase()
    val (colorInt, emoji) = when {
        type.contains("museo") -> android.graphics.Color.parseColor("#0288D1") to "🏛️"
        type.contains("monumento") || type.contains("estatua") -> android.graphics.Color.parseColor("#F57C00") to "🗿"
        type.contains("parque") || type.contains("plaza") -> android.graphics.Color.parseColor("#388E3C") to "🌳"
        type.contains("iglesia") || type.contains("templo") || type.contains("religioso") -> android.graphics.Color.parseColor("#7B1FA2") to "⛪"
        type.contains("galería") || type.contains("galeria") -> android.graphics.Color.parseColor("#C2185B") to "🎨"
        type.contains("restaurante") -> android.graphics.Color.parseColor("#D32F2F") to "🍽️"
        type.contains("historico") || type.contains("histórico") -> android.graphics.Color.parseColor("#FBC02D") to "📜"
        else -> android.graphics.Color.parseColor("#00B4D8") to "📍"
    }

    val sizePx = (48 * context.resources.displayMetrics.density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
    }

    // Borde exterior blanco
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

    // Círculo interior de categoría
    paint.color = colorInt
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, (sizePx / 2f) - (3 * context.resources.displayMetrics.density), paint)

    // Dibujar Emoji centrado en el medio de la marca
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 22 * context.resources.displayMetrics.density
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(emoji, canvas.width / 2f, yPos, textPaint)

    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Devuelve un tono de color (Hue) único de Marker de Google Maps según la categoría del sitio.
 */
fun getCategoryHue(siteType: String): Float {
    val type = siteType.lowercase()
    return when {
        type.contains("museo") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
        type.contains("monumento") || type.contains("estatua") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
        type.contains("parque") || type.contains("plaza") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
        type.contains("iglesia") || type.contains("templo") || type.contains("religioso") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET
        type.contains("historico") || type.contains("histórico") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW
        else -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
    }
}
