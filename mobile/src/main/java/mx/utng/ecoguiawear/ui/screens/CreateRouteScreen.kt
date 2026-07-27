/**
 * Archivo: CreateRouteScreen.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Pantalla de administración para la creación y publicación de nuevas rutas turísticas.
 * Utiliza el diseño unificado del sistema: cabecera en DeepBlue con la card flotante en Surface,
 * campos EcoTextField y botones de acción en la paleta oficial (EcoGuiaColors).
 *
 * Funciones destacadas:
 * - CreateRouteScreen: Formulario unificado con validaciones y selector de sitios.
 * - SiteSelectionRow: Componente de selección estilo tarjeta unificada con checkbox en Jade.
 */

package mx.utng.ecoguiawear.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check

import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel
import mx.utng.ecoguiawear.ui.viewmodel.RouteViewModel

/**
 * Pantalla de creación de rutas estilizada acorde al formulario de alta de sitios.
 */
@Composable
fun CreateRouteScreen(
    onRouteCreated: () -> Unit = {},
    onBack: () -> Unit = {},
    routeViewModel: RouteViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    notificationViewModel: mx.utng.ecoguiawear.ui.viewmodel.NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var estimatedMinutesStr by remember { mutableStateOf("45") }

    val availableSites by locationViewModel.nearbySites
    val selectedSiteIds = remember { mutableStateListOf<String>() }

    val isLoading by routeViewModel.isLoading
    val createSuccess by routeViewModel.createSuccess

    var siteSearchQuery by remember { mutableStateOf("") }
    val currentLocation by locationViewModel.currentLocation

    val dbCategories by locationViewModel.dbCategories
    var selectedCategory by remember { mutableStateOf("Todas") }

    val categories = remember(dbCategories, availableSites) {
        if (dbCategories.isNotEmpty()) {
            listOf("Todas") + dbCategories.map { it.name }.distinct()
        } else {
            listOf("Todas") + availableSites.map { it.siteType }.distinct().filter { it.isNotBlank() }
        }
    }

    val filteredSites = remember(availableSites, selectedCategory, siteSearchQuery, currentLocation) {
        availableSites.filter { site ->
            val categoryMatch = selectedCategory == "Todas" || site.siteType.equals(selectedCategory, ignoreCase = true)
            val queryMatch = siteSearchQuery.isBlank() ||
                site.name.contains(siteSearchQuery, ignoreCase = true) ||
                site.siteType.contains(siteSearchQuery, ignoreCase = true)

            val distanceMatch = if (currentLocation != null && site.latitude != null && site.longitude != null) {
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    currentLocation!!.latitude, currentLocation!!.longitude,
                    site.latitude!!, site.longitude!!, results
                )
                results[0] <= mx.utng.ecoguia.shared.config.EcoGuiaConfig.SEARCH_RADIUS_METERS
            } else true

            categoryMatch && queryMatch && distanceMatch
        }
    }

    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates(context)
        locationViewModel.loadAllHistoricalSites()
    }

    LaunchedEffect(createSuccess) {
        when (createSuccess) {
            true -> {
                notificationViewModel.showNotification(
                    "¡Ruta publicada con éxito!",
                    mx.utng.ecoguiawear.ui.viewmodel.NotificationType.SUCCESS
                )
                routeViewModel.resetCreateState()
                onRouteCreated()
            }
            false -> {
                notificationViewModel.showNotification(
                    "Error al publicar la ruta en Neon.",
                    mx.utng.ecoguiawear.ui.viewmodel.NotificationType.ERROR
                )
                routeViewModel.resetCreateState()
            }
            null -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Encabezado estándar del flujo con botón Volver
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 40.dp, start = 16.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Column {
                    Text("Alta de Ruta", color = Color.White, fontSize = 14.sp)
                    Text("Diseñar Recorrido", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = EcoGuiaColors.Gold)
            }
        }


        // Card superior descriptiva en Surface (Mismo diseño de alta de sitio)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (title.isEmpty()) "Nueva Ruta Turística" else title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configura el recorrido y las paradas ordenadas para los visitantes.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Sección del Formulario usando componentes estandarizados EcoTextField
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Detalles del recorrido",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    EcoTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "TÍTULO DE LA RUTA",
                        placeholder = "Ej. Ruta de los Conspiradores"
                    )
                }

                item {
                    EcoTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "DESCRIPCIÓN DE LA RUTA",
                        placeholder = "Breve reseña del recorrido turístico...",
                        singleLine = false
                    )
                }

                item {
                    EcoTextField(
                        value = estimatedMinutesStr,
                        onValueChange = { estimatedMinutesStr = it },
                        label = "TIEMPO ESTIMADO (MINUTOS)",
                        placeholder = "Ej. 45"
                    )
                }

                item {
                    Text(
                        text = "Seleccionar paradas (${selectedSiteIds.size} seleccionadas)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = siteSearchQuery,
                        onValueChange = { siteSearchQuery = it },
                        placeholder = { Text("Buscar sitio por nombre o tipo...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGuiaColors.Jade)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EcoGuiaColors.Jade,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (filteredSites.isEmpty()) {
                    item {
                        Text(
                            text = if (siteSearchQuery.isNotEmpty()) "No hay sitios que coincidan con la búsqueda." else "Cargando sitios a menos de 50km...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    items(filteredSites) { site ->
                        val isSelected = selectedSiteIds.contains(site.id)
                        val orderIndex = selectedSiteIds.indexOf(site.id)

                        SiteSelectionRow(
                            site = site,
                            isSelected = isSelected,
                            orderIndex = if (orderIndex != -1) orderIndex + 1 else null,
                            canMoveUp = orderIndex > 0,
                            canMoveDown = orderIndex != -1 && orderIndex < selectedSiteIds.size - 1,
                            onToggle = {
                                if (isSelected) selectedSiteIds.remove(site.id)
                                else selectedSiteIds.add(site.id)
                            },
                            onMoveUp = {
                                if (orderIndex > 0) {
                                    val temp = selectedSiteIds[orderIndex]
                                    selectedSiteIds[orderIndex] = selectedSiteIds[orderIndex - 1]
                                    selectedSiteIds[orderIndex - 1] = temp
                                }
                            },
                            onMoveDown = {
                                if (orderIndex != -1 && orderIndex < selectedSiteIds.size - 1) {
                                    val temp = selectedSiteIds[orderIndex]
                                    selectedSiteIds[orderIndex] = selectedSiteIds[orderIndex + 1]
                                    selectedSiteIds[orderIndex + 1] = temp
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = EcoGuiaColors.Jade)
                        }
                    } else {
                        EcoButton(
                            text = "Publicar Ruta Turística",
                            onClick = {
                                if (title.isBlank()) {
                                    notificationViewModel.showNotification(
                                        "Ingresa un título para la ruta",
                                        mx.utng.ecoguiawear.ui.viewmodel.NotificationType.WARNING
                                    )
                                    return@EcoButton
                                }
                                if (selectedSiteIds.isEmpty()) {
                                    notificationViewModel.showNotification(
                                        "Selecciona al menos una parada para la ruta",
                                        mx.utng.ecoguiawear.ui.viewmodel.NotificationType.WARNING
                                    )
                                    return@EcoButton
                                }
                                val minutes = estimatedMinutesStr.toIntOrNull() ?: 45
                                routeViewModel.createRoute(title, description, minutes, selectedSiteIds.toList())
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Fila de un sitio seleccionable con diseño armónico.
 */
@Composable
fun SiteSelectionRow(
    site: RemoteHistoricalSite,
    isSelected: Boolean,
    orderIndex: Int? = null,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EcoGuiaColors.Jade.copy(alpha = 0.12f)
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) EcoGuiaColors.Jade else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected && orderIndex != null) {
                    Text(
                        text = "#$orderIndex",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(Icons.Default.Place, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(20.dp))
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(site.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(site.siteType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                        Text("▲", fontSize = 12.sp, color = if (canMoveUp) EcoGuiaColors.Jade else Color.Gray.copy(alpha = 0.3f))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                        Text("▼", fontSize = 12.sp, color = if (canMoveDown) EcoGuiaColors.Jade else Color.Gray.copy(alpha = 0.3f))
                    }
                }
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = EcoGuiaColors.Jade)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRouteScreenPreview() {
    EcoGuiaMobileTheme {
        CreateRouteScreen()
    }
}
