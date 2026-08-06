/**
 * Archivo: SearchExperienceScreen.kt
 *
 * Pantalla de exploración y búsqueda de experiencias turísticas. Muestra el catálogo de rutas
 * disponibles desde Neon PostgreSQL, permitiendo iniciar cualquiera de ellas o filtrarlas.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteRoute
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.RouteViewModel

/**
 * Pantalla que presenta el catálogo de rutas turísticas disponibles para el usuario.
 *
 * @param onSelectRoute Callback ejecutado al seleccionar e iniciar una ruta.
 * @param onCreateRoute Callback para navegar al formulario de creación de rutas (moderadores/admins).
 * @param isModerator Indica si el usuario actual posee permisos de moderación o administración.
 * @param routeViewModel ViewModel para la consulta y activación de rutas.
 * @param locationViewModel ViewModel para calcular la distancia a las rutas respecto a la ubicación actual.
 */
@Composable
fun SearchExperienceScreen(
    onSelectRoute: () -> Unit = {},
    onCreateRoute: () -> Unit = {},
    isModerator: Boolean = false,
    routeViewModel: RouteViewModel = viewModel(),
    locationViewModel: mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val routes by routeViewModel.routes
    val isLoading by routeViewModel.isLoading
    val currentLocation by locationViewModel.currentLocation

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        routeViewModel.loadRoutes()
    }

    val filteredRoutes = remember(routes, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) routes
        else routes.filter { r ->
            r.title.lowercase().contains(q) || (r.description?.lowercase()?.contains(q) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 20.dp)
        ) {
            Column {
                Text("Explorar", color = EcoGuiaColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Rutas Turísticas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            if (isModerator) {
                IconButton(
                    onClick = onCreateRoute,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Crear Ruta", tint = EcoGuiaColors.Gold)
                }
            } else {
                Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.align(Alignment.TopEnd))
            }
        }


        // Campo de búsqueda por categoría / palabras clave
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por categoría o nombre (ej: Museos, Independencia)...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EcoGuiaColors.Jade) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.Gray)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoGuiaColors.Jade,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            )
        )

        // Info Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(" Rutas cercanas (50 km)", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${filteredRoutes.size} encontrada(s)", color = EcoGuiaColors.Gold, fontSize = 11.sp)
                }
                Text(
                    "Selecciona una ruta para sincronizar con tu reloj y comenzar el recorrido.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Catálogo de Rutas
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EcoGuiaColors.Jade)
                    }
                }
                filteredRoutes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = EcoGuiaColors.Jade,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isNotEmpty()) "Sin resultados para \"$searchQuery\""
                                else "No hay rutas cercanas en 50 km.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredRoutes) { route ->
                            RouteCatalogItem(
                                route = route,
                                onStartClick = {
                                    routeViewModel.startRoute(context, route)
                                    onSelectRoute()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fila de una ruta en el catálogo de búsqueda.
 */
/**
 * Fila interactiva de una ruta en el catálogo de búsqueda. Al hacer clic se despliega la descripción completa y los destinos.
 */
@Composable
fun RouteCatalogItem(
    route: RemoteRoute,
    onStartClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var stops by remember { mutableStateOf<List<mx.utng.ecoguia.shared.domain.model.RemoteRouteStop>>(emptyList()) }
    var isLoadingStops by remember { mutableStateOf(false) }

    LaunchedEffect(isExpanded) {
        if (isExpanded && stops.isEmpty()) {
            isLoadingStops = true
            try {
                stops = EcoGuiaRepositoryImpl().getRouteStops(route.id)
            } catch (e: Exception) {
                android.util.Log.e("RouteItem", "Error al cargar destinos: ${e.message}")
            } finally {
                isLoadingStops = false
            }
        }
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(EcoGuiaColors.Jade.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = EcoGuiaColors.Jade, modifier = Modifier.size(22.dp))
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .weight(1f)
                ) {
                    Text(route.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    val desc = route.description ?: "Recorrido turístico cultural"
                    Text(
                        text = "${route.estimatedMinutes ?: 45} min · ${route.distanceM ?: 1200} m",
                        color = EcoGuiaColors.Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onStartClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoGuiaColors.Jade,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Iniciar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Sección Expandible con Descripción Detallada y Destinos
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Descripción del recorrido:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EcoGuiaColors.Gold
                )
                Text(
                    text = route.description ?: "Esta ruta te guiará por los puntos históricos más representativos.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Destinos y Paradas Incluidas:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EcoGuiaColors.Jade
                )

                if (isLoadingStops) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = EcoGuiaColors.Jade,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cargando destinos...", fontSize = 11.sp, color = Color.Gray)
                    }
                } else if (stops.isEmpty()) {
                    Text(
                        text = "• Recorrido general por zona centro e hitos culturales.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                } else {
                    stops.forEachIndexed { index, stop ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text("${index + 1}. ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EcoGuiaColors.Gold)
                            Text(stop.siteName ?: "Sitio histórico", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            if (!stop.instruction.isNullOrEmpty()) {
                                Text(" — ${stop.instruction}", fontSize = 10.sp, color = Color.Gray)
                            }


                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SearchExperienceScreenPreview() {
    EcoGuiaMobileTheme {
        SearchExperienceScreen()
    }
}
