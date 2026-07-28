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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
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
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Eliminar de Mi Colección", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas quitar \"${item.title}\" de tu colección personal?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onRemove()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono de tipo (sitio, foto, ruta)
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
                        fontSize = 12.sp
                    )
                }
            }
        }

    }
}
