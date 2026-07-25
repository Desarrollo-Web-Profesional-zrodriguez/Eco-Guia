/**
 * Archivo: MyCollectionScreen.kt
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-25
 * Descripción: Pantalla de colección personal del usuario. Muestra elementos guardados desde
 * user_saved_items en Neon. Soporta filtros por tipo y eliminación individual con swipe o botón.
 */

package mx.utng.ecoguiawear.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguia.shared.domain.model.RemoteCollectionItem
import mx.utng.ecoguiawear.ui.components.EcoTopBar
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.CollectionViewModel

/**
 * Composable que representa la pantalla "Mi Colección".
 * @param userId ID del usuario autenticado para cargar y eliminar sus elementos.
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

    // Filtro combinado: tab + búsqueda de texto
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

        // Campo de búsqueda expandible
        AnimatedVisibility(visible = showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar en mi colección...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = EcoGuiaColors.Jade
                    )
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

        // Info Card con contador y tabs de filtro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Contador adaptado a búsqueda activa
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

                // Tabs de filtro
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

        // Lista de elementos
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = when {
                    searchQuery.isNotEmpty() -> "Resultados (${filteredItems.size})"
                    selectedTab == 0 -> "Recientes"
                    else -> "${tabs[selectedTab]} (${filteredItems.size})"
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EcoGuiaColors.Jade)
                    }
                }
                filteredItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "🔍" else "🏛️",
                                fontSize = 40.sp
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
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(
                            items = filteredItems,
                            key = { _, item -> item.id }
                        ) { _, item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut()
                            ) {
                                CollectionItemRow(
                                    item = item,
                                    searchQuery = searchQuery,
                                    onRemove = { viewModel.removeSite(userId, item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fila de un elemento guardado en la colección.
 * Resalta el texto que coincide con la búsqueda activa.
 */
@Composable
fun CollectionItemRow(
    item: RemoteCollectionItem,
    searchQuery: String = "",
    onRemove: () -> Unit = {}
) {
    var showConfirm by remember { mutableStateOf(false) }

    // Construye el título con highlight del texto buscado
    val highlightedTitle = buildAnnotatedString {
        val query = searchQuery.trim().lowercase()
        val title = item.title
        if (query.isEmpty()) {
            append(title)
        } else {
            var start = 0
            val lower = title.lowercase()
            while (start < title.length) {
                val idx = lower.indexOf(query, start)
                if (idx == -1) {
                    append(title.substring(start))
                    break
                }
                append(title.substring(start, idx))
                withStyle(SpanStyle(background = EcoGuiaColors.Jade.copy(alpha = 0.25f), color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold)) {
                    append(title.substring(idx, idx + query.length))
                }
                start = idx + query.length
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        when (item.type) {
                            "site" -> EcoGuiaColors.Jade.copy(alpha = 0.12f)
                            "photo" -> EcoGuiaColors.Gold.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.type) {
                        "site" -> "🏛️"
                        "photo" -> "📸"
                        else -> "🗺️"
                    },
                    fontSize = 20.sp
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                // Título con highlight de búsqueda
                Text(
                    text = highlightedTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.subtitle} · ${item.createdAt?.take(10) ?: ""}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Botón eliminar con confirmación en dos pasos
            if (showConfirm) {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onRemove()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                IconButton(
                    onClick = { showConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar de colección",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
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
