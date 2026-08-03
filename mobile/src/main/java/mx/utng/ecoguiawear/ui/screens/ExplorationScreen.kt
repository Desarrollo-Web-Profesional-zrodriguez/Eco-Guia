/**
 * Archivo: ExplorationScreen.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Pantalla principal de exploración cultural. Actúa como orquestador ligero
 * que coordina el estado compartido entre el mapa, la lista de sitios y el BottomSheet de detalle.
 * Los componentes visuales se han separado en archivos dedicados dentro de
 * ui/feature/exploration/ para mantener la legibilidad y el principio de responsabilidad única.
 *
 * Archivos relacionados en ui/feature/exploration/:
 * - ExplorationMapContent.kt  → GoogleMap con marcadores y controles de zoom
 * - ExplorationSiteList.kt    → Lista de sitios con skeleton, swipe y paginación
 * - SiteDetailSheet.kt        → BottomSheet animado de detalle del sitio
 * - MapMarkerUtils.kt         → Funciones puras para íconos de marcadores
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.feature.exploration.ExplorationMapContent
import mx.utng.ecoguiawear.ui.feature.exploration.ExplorationSiteList
import mx.utng.ecoguiawear.ui.feature.exploration.SiteDetailSheet
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.CollectionViewModel
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel

/**
 * Pantalla principal de exploración cultural.
 *
 * Orquesta el estado compartido entre:
 * - El mapa interactivo ([ExplorationMapContent]).
 * - La lista de sitios recomendados ([ExplorationSiteList]).
 * - El detalle flotante de un sitio seleccionado ([SiteDetailSheet]).
 *
 * Soporta modo portrait (mapa sobre lista) y landscape (mapa + lista en columnas).
 *
 * @param onAdminClick Callback para navegar al panel de opciones (botón +).
 * @param onOpenRoutes Callback para navegar a la pantalla de rutas.
 * @param userId ID del usuario autenticado. Usar "guest" para modo sin sesión.
 * @param locationViewModel ViewModel que provee ubicación y sitios cercanos.
 * @param collectionViewModel ViewModel que gestiona el estado de favoritos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationScreen(
    onAdminClick: () -> Unit,
    onOpenRoutes: () -> Unit = {},
    onOpenGeoDropWithSite: (String) -> Unit = {},
    userId: String = "",
    userRole: String = "",
    locationViewModel: LocationViewModel = viewModel(),
    collectionViewModel: CollectionViewModel = viewModel()
) {


    val context = LocalContext.current
    val currentLocation by locationViewModel.currentLocation
    val nearbySites by locationViewModel.nearbySites
    val closestSite by locationViewModel.closestSite
    val isLoading by locationViewModel.isLoading
    val scope = rememberCoroutineScope()

    var isFollowingUser by remember { mutableStateOf(true) }
    var selectedSite by remember { mutableStateOf<RemoteHistoricalSite?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Iniciar actualizaciones de ubicación al entrar a la pantalla
    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(21.1561, -100.9350), 15f)
    }

    // Centrar cámara solo cuando isFollowingUser es verdadero
    LaunchedEffect(currentLocation) {
        if (isFollowingUser) {
            currentLocation?.let {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17f)
                )
            }
        }
    }

    // Desactivar seguimiento automático si el usuario mueve el mapa manualmente
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving &&
            cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            isFollowingUser = false
        }
    }

    // Precargar estado de guardado para todos los sitios visibles
    LaunchedEffect(userId, nearbySites) {
        if (userId != "guest") {
            nearbySites.forEach { site ->
                collectionViewModel.checkIfSaved(userId, site.id)
            }
        }
    }

    var displayLimit by remember { mutableIntStateOf(5) }

    // Ordenar: favoritos primero, luego por distancia al usuario
    val sortedSites = remember(nearbySites, collectionViewModel.savedSiteIds, currentLocation) {
        nearbySites.sortedWith(
            compareByDescending<RemoteHistoricalSite> { site ->
                collectionViewModel.savedSiteIds[site.id] == true
            }.thenBy { site ->
                val siteLat = site.getComputedLatitude()
                val siteLng = site.getComputedLongitude()
                if (currentLocation != null && siteLat != null && siteLng != null) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        currentLocation!!.latitude, currentLocation!!.longitude,
                        siteLat, siteLng, results
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

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                ExplorationMapContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    nearbySites = nearbySites,
                    currentLocation = currentLocation,
                    cameraPositionState = cameraPositionState,
                    isFollowingUser = isFollowingUser,
                    onFollowUser = {
                        isFollowingUser = true
                        currentLocation?.let { loc ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17f)
                                )
                            }
                        }
                    },
                    scope = scope
                )
                ExplorationSiteList(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    sortedSites = sortedSites,
                    visibleSites = visibleSites,
                    displayLimit = displayLimit,
                    isLoading = isLoading,
                    userId = userId,
                    collectionViewModel = collectionViewModel,
                    onSiteClick = { site ->
                        isFollowingUser = false
                        selectedSite = site
                        if (userId != "guest") {
                            collectionViewModel.checkIfSaved(userId, site.id)
                        }
                    },
                    onLoadMore = { displayLimit += 5 },
                    onOpenRoutes = onOpenRoutes
                )
            }
        } else {
            ExplorationMapContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                nearbySites = nearbySites,

                currentLocation = currentLocation,
                cameraPositionState = cameraPositionState,
                isFollowingUser = isFollowingUser,
                onFollowUser = {
                    isFollowingUser = true
                    currentLocation?.let { loc ->
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17f)
                            )
                        }
                    }
                },
                scope = scope
            )
            ExplorationSiteList(
                modifier = Modifier.weight(1f),
                sortedSites = sortedSites,
                visibleSites = visibleSites,
                displayLimit = displayLimit,
                isLoading = isLoading,
                userId = userId,
                collectionViewModel = collectionViewModel,
                onSiteClick = { site ->
                    isFollowingUser = false
                    selectedSite = site
                    if (userId != "guest") {
                        collectionViewModel.checkIfSaved(userId, site.id)
                    }
                },
                onLoadMore = { displayLimit += 5 },
                onOpenRoutes = onOpenRoutes
            )
        }
    }

    // BottomSheet de detalle del sitio seleccionado
    selectedSite?.let { site ->
        val loc = currentLocation
        val siteLat = site.getComputedLatitude()
        val siteLng = site.getComputedLongitude()
        val isWithinRange = remember(loc, siteLat, siteLng) {
            if (loc != null && siteLat != null && siteLng != null) {
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    siteLat, siteLng, results
                )
                results[0] <= site.detectionRadiusM
            } else true // Permite agregar si la ubicación no ha cargado o si es simulada
        }

        val isUserAdmin = remember(userRole) {
            userRole.lowercase() in listOf("admin", "super_admin", "administrator")
        }

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
                isWithinRange = isWithinRange,
                isUserAdmin = isUserAdmin,
                onNavigate = {
                    locationViewModel.syncTargetWithWatch(site)
                    if (siteLat != null && siteLng != null) {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(siteLat, siteLng), 17f)
                            )
                        }
                    }
                    selectedSite = null
                },
                onGeoDropClick = {
                    val siteId = site.id
                    selectedSite = null
                    onOpenGeoDropWithSite(siteId)
                },
                onDismiss = { selectedSite = null }
            )
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
