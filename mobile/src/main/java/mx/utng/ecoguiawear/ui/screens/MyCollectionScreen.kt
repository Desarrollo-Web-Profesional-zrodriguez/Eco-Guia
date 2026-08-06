/**
 * Archivo: MyCollectionScreen.kt
 *
 * Pantalla de colección cultural personal del usuario. Gestiona el filtrado por categorías,
 * búsqueda por palabras clave y visualización de sitios y cápsulas guardadas.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguia.shared.domain.model.RemoteCollectionItem
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.feature.collection.CollectionItemRow
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.CollectionViewModel

/**
 * Pantalla composable de colección cultural del usuario.
 *
 * @param userId Identificador único del usuario autenticado.
 * @param viewModel ViewModel encargado de la carga y eliminación de elementos guardados.
 */
@Composable
fun MyCollectionScreen(
    userId: String,
    viewModel: CollectionViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todos", "Sitios", "Fotos", "Rutas")
    val tabFilters = listOf(null, "site", "photo", "route")

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    val items by viewModel.items
    val isLoading by viewModel.isLoading
    val saveError by viewModel.saveError

    LaunchedEffect(Unit) {
        viewModel.loadCollection(userId)
    }

    // Filtro combinado: tipo de tab + texto de búsqueda
    val filteredItems = remember(items, selectedTab, searchQuery) {
        val typeFilter = tabFilters[selectedTab]
        val query = searchQuery.trim().lowercase()
        items
            .filter { item -> typeFilter == null || item.type == typeFilter }
            .filter { item ->
                if (query.isEmpty()) true
                else item.title.lowercase().contains(query) ||
                     item.subtitle.lowercase().contains(query)
            }
    }

    if (saveError != null) {
        LaunchedEffect(saveError) { viewModel.clearSaveError() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoTopBar(
            title = "Mi Colección",
            subtitle = if (showSearch) "" else "Guardados",
            actionIcon = if (showSearch) Icons.Default.Close else Icons.Default.Search,
            onActionClick = {
                showSearch = !showSearch
                if (!showSearch) searchQuery = ""
            }
        )

        // Campo de búsqueda expandible con animación
        AnimatedVisibility(visible = showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar en mi colección...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = EcoGuiaColors.Jade)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoGuiaColors.Jade,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        }

        // Card de contador y tabs de filtro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 2.dp, bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                val countText = when {
                    searchQuery.isNotEmpty() -> "${filteredItems.size} resultado(s) para \"$searchQuery\""
                    else -> "${items.size} guardados en tu colección"
                }
                Text(countText, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Sitios históricos y cápsulas que has guardado.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs de filtro por tipo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedTab == index) EcoGuiaColors.Jade
                                    else Color.Transparent
                                )
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title,
                                color = if (selectedTab == index) Color.White
                                        else Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Sección de lista de elementos
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = when {
                    searchQuery.isNotEmpty() -> "Resultados (${filteredItems.size})"
                    selectedTab == 0 -> "Recientes"
                    else -> "${tabs[selectedTab]} (${filteredItems.size})"
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )


            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EcoGuiaColors.Jade)
                    }
                }
                filteredItems.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (searchQuery.isNotEmpty()) Color.Gray else EcoGuiaColors.Jade,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "Sin resultados para \"$searchQuery\""
                                else if (selectedTab == 0) "No tienes elementos guardados."
                                else "Sin ${tabs[selectedTab].lowercase()} en tu colección.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Explora el mapa y presiona \"Guardar\".",
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                else -> {
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val isLandscape =
                        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                    if (isLandscape) {
                        // Grilla de 2 columnas en modo horizontal
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                count = filteredItems.size,
                                key = { index -> filteredItems[index].id }
                            ) { index ->
                                val item = filteredItems[index]
                                CollectionItemRow(
                                    item = item,
                                    currentUserId = userId,
                                    searchQuery = searchQuery,
                                    onRemove = { viewModel.removeItem(userId, item) }
                                )
                            }
                        }
                    } else {
                        // Lista vertical en modo portrait
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(
                                items = filteredItems,
                                key = { _, item -> item.id }
                            ) { _, item ->
                                CollectionItemRow(
                                    item = item,
                                    currentUserId = userId,
                                    searchQuery = searchQuery,
                                    onRemove = { viewModel.removeItem(userId, item) }
                                )
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
fun MyCollectionScreenPreview() {
    EcoGuiaMobileTheme {
        MyCollectionScreen("dummy_user")
    }
}
