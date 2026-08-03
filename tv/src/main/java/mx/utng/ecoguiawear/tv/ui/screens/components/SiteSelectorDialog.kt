package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite

/**
 * Diálogo adaptado para Android TV que permite al usuario seleccionar
 * un sitio histórico de una lista filtrable, usando el D-Pad del control remoto.
 */
@Composable
fun SiteSelectorDialog(
    sites: List<RemoteHistoricalSite>,
    currentSiteId: String?,
    isAdmin: Boolean,
    onSiteSelected: (RemoteHistoricalSite) -> Unit,
    onDismiss: () -> Unit
) {
    var filterText by remember { mutableStateOf("") }
    val filtered = remember(filterText, sites) {
        if (filterText.isBlank()) sites
        else sites.filter {
            it.name.contains(filterText, ignoreCase = true) ||
                    it.address.orEmpty().contains(filterText, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF34D399).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Encabezado ──────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = if (isAdmin) Icons.Default.Public else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAdmin) "Todos los Sitios" else "Mis Sitios",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Text(
                            text = "${filtered.size} sitio(s) encontrado(s)",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cerrar", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Campo de Búsqueda / Filtro ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (filterText.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buscar por nombre o dirección...",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    BasicTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF34D399)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Lista de Sitios ──────────────────────────────────────────
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin resultados para \"$filterText\"",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { site ->
                            val isSelected = site.id == currentSiteId
                            SiteItem(
                                site = site,
                                isSelected = isSelected,
                                onClick = { onSiteSelected(site) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SiteItem(
    site: RemoteHistoricalSite,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.colors(
            containerColor = if (isSelected) Color(0xFF064E3B) else Color(0xFF1E293B),
            focusedContainerColor = Color(0xFF065F46)
        ),
        shape = androidx.tv.material3.ButtonDefaults.shape(
            shape = RoundedCornerShape(10.dp),
            focusedShape = RoundedCornerShape(10.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de tipo de sitio
            val iconVector = when (site.siteType.lowercase()) {
                "museo" -> Icons.Default.AccountBalance
                "hotel" -> Icons.Default.Hotel
                "parque" -> Icons.Default.Park
                "restaurant" -> Icons.Default.Restaurant
                else -> Icons.Default.Place
            }
            androidx.tv.material3.Icon(
                imageVector = iconVector,
                contentDescription = site.siteType,
                tint = if (isSelected) Color(0xFF34D399) else Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(22.dp)
                    .padding(end = 6.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.name,
                    color = if (isSelected) Color(0xFF34D399) else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!site.address.isNullOrBlank()) {
                    Text(
                        text = site.address.orEmpty(),
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    androidx.tv.material3.Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Activo",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Activo",
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
