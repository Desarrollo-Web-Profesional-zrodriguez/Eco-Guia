/**
 * Archivo: ExplorationSiteList.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Componente de lista de sitios recomendados para la pantalla de Exploración.
 * Incluye esqueleto de carga, lista con swipe-to-favorite y paginación "Cargar más".
 * Separado de ExplorationScreen para facilitar la re-composición independiente.
 *
 * Funciones destacadas:
 * - ExplorationSiteList: Lista reactiva de sitios con skeleton, swipe y paginación.
 */

package mx.utng.ecoguiawear.ui.feature.exploration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.CollectionViewModel

/**
 * Lista de sitios recomendados para la pantalla de Exploración.
 *
 * Comportamientos:
 * - Muestra un esqueleto de carga mientras [isLoading] es verdadero.
 * - Cuando los datos están disponibles, renderiza una [LazyColumn] con swipe izquierdo
 *   para agregar/quitar de favoritos.
 * - Incluye botón de paginación "Cargar más (+5)" cuando hay más sitios disponibles.
 *
 * @param modifier Modifier externo (controla el tamaño según portrait/landscape).
 * @param sortedSites Lista completa de sitios ya ordenados (favoritos primero, luego por distancia).
 * @param visibleSites Subconjunto de [sortedSites] visible actualmente según [displayLimit].
 * @param displayLimit Cantidad actual de sitios visibles.
 * @param isLoading Indica si los sitios están cargando.
 * @param userId ID del usuario autenticado. "guest" deshabilita el guardado.
 * @param collectionViewModel ViewModel que gestiona el estado de favoritos.
 * @param onSiteClick Callback invocado cuando el usuario pulsa "Ver" en un sitio.
 * @param onLoadMore Callback para incrementar el límite de paginación.
 * @param onOpenRoutes Callback para navegar a la pantalla de rutas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationSiteList(
    modifier: Modifier = Modifier,
    sortedSites: List<RemoteHistoricalSite>,
    visibleSites: List<RemoteHistoricalSite>,
    displayLimit: Int,
    isLoading: Boolean,
    userId: String,
    collectionViewModel: CollectionViewModel,
    onSiteClick: (RemoteHistoricalSite) -> Unit,
    onLoadMore: () -> Unit,
    onOpenRoutes: () -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {

        // Encabezado: contador de sitios
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = if (sortedSites.isEmpty()) "Buscando sitios..."
                       else "Sitios recomendados (${visibleSites.size}/${sortedSites.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }


        if (isLoading) {
            // Esqueleto de carga — 3 tarjetas grises animadas
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
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
            // Lista principal con swipe-to-favorite
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
                            onVerClick = { onSiteClick(site) }
                        )
                    }
                }

                // Botón de paginación — aparece solo si hay más sitios
                if (sortedSites.size > displayLimit) {
                    item {
                        TextButton(
                            onClick = onLoadMore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                "Cargar más sitios (+5)",
                                color = EcoGuiaColors.Jade,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
