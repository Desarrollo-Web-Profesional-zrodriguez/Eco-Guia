/**
 * Archivo: CollectionItemCard.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Tarjeta swipeable para un elemento de la colección personal del usuario.
 * Soporta swipe izquierdo para eliminar con confirmación modal, e ilumina el texto
 * que coincide con el término de búsqueda activo.
 *
 * Funciones destacadas:
 * - CollectionItemRow: Tarjeta con SwipeToDismissBox, confirmación por AlertDialog y
 *   resaltado de búsqueda en el título.
 */

package mx.utng.ecoguiawear.ui.feature.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguia.shared.domain.model.RemoteCollectionItem
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Fila swipeable para un elemento de la colección del usuario.
 *
 * Comportamientos:
 * - Swipe de derecha a izquierda dispara un [AlertDialog] de confirmación.
 * - El título del elemento resalta el fragmento de texto que coincide con [searchQuery].
 * - El ícono y color del fondo varían según el tipo de elemento (sitio, foto, ruta).
 *
 * @param item Elemento de la colección a mostrar.
 * @param searchQuery Texto de búsqueda actual para resaltar coincidencias en el título.
 * @param onRemove Callback invocado cuando el usuario confirma la eliminación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionItemRow(
    item: RemoteCollectionItem,
    currentUserId: String = "",
    searchQuery: String = "",
    onRemove: () -> Unit = {}
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirmDialog = true
            }
            false // Mantener tarjeta en posición visual hasta confirmar en modal
        }
    )

    // Diálogo de confirmación antes de eliminar
    if (showConfirmDialog) {
        val authorId = item.authorId
        val isAuthor = !authorId.isNullOrBlank() && 
            !currentUserId.isNullOrBlank() &&
            authorId.trim().equals(currentUserId.trim(), ignoreCase = true)
        
        android.util.Log.d("CollectionItemCard", "Evaluando autoría -> item.title='${item.title}', item.authorId='$authorId', currentUserId='$currentUserId', isAuthor=$isAuthor")

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(if (isAuthor) "Eliminación Definitiva" else "Quitar de Mi Colección", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isAuthor) 
                        "Tú creaste este contenido. Al eliminarlo se borrará DEFINITIVAMENTE del mapa y de las colecciones de todos los usuarios." 
                    else 
                        "¿Deseas quitar \"${item.title}\" de tu colección personal?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onRemove()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isAuthor) "Eliminar Definitivamente" else "Quitar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Construcción del título con fragmento resaltado según la búsqueda
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
                withStyle(
                    SpanStyle(
                        background = EcoGuiaColors.Jade.copy(alpha = 0.25f),
                        color = EcoGuiaColors.Jade,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(title.substring(idx, idx + query.length))
                }
                start = idx + query.length
            }
        }
    }

    var showDetailDialog by remember { mutableStateOf(false) }

    if (showDetailDialog) {
        val dialogScrollState = androidx.compose.foundation.rememberScrollState()

        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = { Text(item.title, fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(dialogScrollState)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!item.mediaUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color.Black, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = item.mediaUrl,
                                    contentDescription = item.title,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val typeText = when(item.type) {
                            "site" -> "Sitio Histórico" + (item.siteType?.let { " · ${it.uppercase()}" } ?: "")
                            "photo" -> "Cápsula GeoDrop"
                            else -> "Ruta Turística"
                        }
                        Text(
                            text = typeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EcoGuiaColors.Jade
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Para GeoDrops: Mostrar Sitio Perteneciente
                        if (!item.siteName.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = EcoGuiaColors.Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pertenece a: ${item.siteName}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = EcoGuiaColors.Gold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = item.subtitle.ifBlank { "Sin descripción detallada." },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Detalles de Sitio (Dirección, Horarios, Costo, Accesibilidad, Historia)
                        if (item.type == "site") {
                            if (!item.address.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = EcoGuiaColors.Jade,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dirección: ${item.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            val cleanHours = item.openingHours.orEmpty()
                                .replace(Regex("[{}\"\\s]"), " ")
                                .replace("schedule:", "")
                                .trim()
                            if (cleanHours.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = EcoGuiaColors.Jade,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Horarios: $cleanHours", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            if (!item.costInfo.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = EcoGuiaColors.Jade,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Costo: ${item.costInfo}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            val cleanAccess = item.accessibility.orEmpty()
                                .replace(Regex("[{}\"\\s\\[\\]]"), " ")
                                .replace("features:", "")
                                .trim()
                            if (cleanAccess.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Accessible,
                                        contentDescription = null,
                                        tint = EcoGuiaColors.Jade,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Accesibilidad: $cleanAccess", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            val histDesc = item.historicalDescription
                            if (!histDesc.isNullOrBlank() && histDesc != item.subtitle) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = EcoGuiaColors.Gold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Historia & Contexto:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EcoGuiaColors.Gold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(histDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Detalles de Ruta (Tiempo Estimado, Distancia)
                        if (item.type == "route") {
                            val mins = item.estimatedMinutes
                            val distM = item.distanceM
                            if (mins != null || distM != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    mins?.let {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = EcoGuiaColors.Jade,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("~$it min", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EcoGuiaColors.Jade)
                                    }
                                    if (mins != null && distM != null) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    distM?.let {
                                        Icon(
                                            imageVector = Icons.Default.Straighten,
                                            contentDescription = null,
                                            tint = EcoGuiaColors.Jade,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${"%.1f".format(it / 1000.0)} km", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EcoGuiaColors.Jade)
                                    }
                                }
                            }
                        }

                        val createdDate = item.createdAt
                        if (createdDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Guardado el: ${createdDate.take(10)}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        if (item.type == "route") {
                            var stops by remember { mutableStateOf<List<mx.utng.ecoguia.shared.domain.model.RemoteRouteStop>>(emptyList()) }
                            var isLoadingStops by remember { mutableStateOf(true) }

                            LaunchedEffect(item.rawId, item.id) {
                                val targetId = item.rawId ?: item.id.removePrefix("fav_").removePrefix("saved_")
                                try {
                                    stops = EcoGuiaRepositoryImpl().getRouteStops(targetId)
                                } catch (e: Exception) {
                                    android.util.Log.e("CollectionDialog", "Error al cargar paradas: ${e.message}")
                                } finally {
                                    isLoadingStops = false
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Sitios que componen la ruta:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = EcoGuiaColors.Gold
                            )

                            if (isLoadingStops) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = EcoGuiaColors.Jade,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cargando sitios...", fontSize = 11.sp, color = Color.Gray)
                                }
                            } else if (stops.isEmpty()) {
                                Text(
                                    text = "• Recorrido turístico por zonas de interés histórico.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            } else {
                                Column(modifier = Modifier.padding(top = 4.dp)) {
                                    stops.forEachIndexed { index, stop ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Text("${index + 1}. ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EcoGuiaColors.Gold)
                                            Text(
                                                text = stop.siteName ?: "Sitio histórico",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (!stop.instruction.isNullOrBlank()) {
                                                Text(" — ${stop.instruction}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE53935), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Deslizar para eliminar",
                    tint = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDetailDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono de tipo (sitio, foto, ruta) o Thumbnail si hay mediaUrl
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                    if (!item.mediaUrl.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = item.mediaUrl,
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray, RoundedCornerShape(14.dp))
                        )
                    } else {
                        Icon(
                            imageVector = when (item.type) {
                                "site" -> Icons.Default.AccountBalance
                                "photo" -> Icons.Default.CameraAlt
                                else -> Icons.Default.Map
                            },
                            contentDescription = null,
                            tint = when (item.type) {
                                "site" -> EcoGuiaColors.Jade
                                "photo" -> EcoGuiaColors.Gold
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = highlightedTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (item.type == "photo") {
                            Spacer(modifier = Modifier.width(6.dp))
                            val (statusText, statusBg, statusColor) = when (item.status) {
                                "approved", "active" -> Triple("Aprobada", EcoGuiaColors.Jade.copy(alpha = 0.2f), EcoGuiaColors.Jade)
                                "rejected" -> Triple("Rechazada", MaterialTheme.colorScheme.error.copy(alpha = 0.2f), MaterialTheme.colorScheme.error)
                                else -> Triple("Pendiente", EcoGuiaColors.Gold.copy(alpha = 0.2f), EcoGuiaColors.Gold)
                            }
                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${item.subtitle} · ${item.createdAt?.take(10) ?: ""}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

