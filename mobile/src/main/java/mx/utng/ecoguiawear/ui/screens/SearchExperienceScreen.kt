/**
 * Archivo: SearchExperienceScreen.kt
 * Autor: Zahir Andres
 * Fecha de última actualización: 2026-07-25
 * Descripción: Pantalla de exploración y búsqueda de experiencias. Muestra el catálogo de rutas
 * turísticas disponibles desde Neon PostgreSQL, permitiendo iniciar cualquiera de ellas.
 *
 * Funciones destacadas:
 * - SearchExperienceScreen: Muestra el catálogo dinámico de rutas y accesos rápidos a IA y mapas.
 * - RouteCatalogItem: Tarjeta interactiva con título, descripción y botón "Iniciar Recorrido".
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
import androidx.compose.runtime.getValue
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
import mx.utng.ecoguia.shared.domain.model.RemoteRoute
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.RouteViewModel

/**
 * Pantalla que presenta las rutas turísticas disponibles para el usuario.
 */
@Composable
fun SearchExperienceScreen(
    onSelectRoute: () -> Unit = {},
    routeViewModel: RouteViewModel = viewModel()
) {
    val context = LocalContext.current
    val routes by routeViewModel.routes
    val isLoading by routeViewModel.isLoading

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
            Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.align(Alignment.TopEnd))
        }

        // Info Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Elige tu aventura histórica", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Selecciona una ruta para sincronizar con tu reloj y recibir guiado por voz.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Catálogo de Rutas
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Rutas disponibles", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EcoGuiaColors.Jade)
                    }
                }
                routes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay rutas registradas aún.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(routes) { route ->
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
@Composable
fun RouteCatalogItem(
    route: RemoteRoute,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                val desc = route.description ?: "Recorrido turístico por la ciudad"
                Text(
                    text = "${route.estimatedMinutes ?: 45} min · $desc",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 2
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
    }
}

@Preview(showBackground = true)
@Composable
fun SearchExperienceScreenPreview() {
    EcoGuiaMobileTheme {
        SearchExperienceScreen()
    }
}
