/**
 * Archivo: ReportDetailScreen.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Pantalla para la resolución detallada de una cápsula o reporte de moderación.
 * Permite al Administrador/Moderador aprobar o rechazar un contenido impactando la BD en tiempo real.
 */

package mx.utng.ecoguiawear.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.ModerationViewModel

@Composable
fun ReportDetailScreen(
    onResolve: () -> Unit,
    onBack: () -> Unit = {},
    moderationViewModel: ModerationViewModel = viewModel()
) {
    val selectedDrop by moderationViewModel.selectedDrop
    val isLoading by moderationViewModel.isLoading
    var selectedAction by remember { mutableStateOf<String?>("approved") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                .padding(top = 48.dp, start = 16.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Column {
                    Text("Revisión de Moderación", color = Color.White, fontSize = 14.sp)
                    Text(
                        selectedDrop?.title ?: "Detalle del elemento",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Ficha del Elemento Reportado/Revisado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Visualizador de Fotografía Capturada / Media
                val mediaUrl = selectedDrop?.mediaUrl
                var showZoomDialog by remember { mutableStateOf(false) }

                if (showZoomDialog && !mediaUrl.isNullOrBlank()) {
                    AlertDialog(
                        onDismissRequest = { showZoomDialog = false },
                        title = { Text(selectedDrop?.title ?: "Vista previa", fontWeight = FontWeight.Bold) },
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(Color.Black, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = mediaUrl,
                                    contentDescription = "Foto ampliada",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showZoomDialog = false }) {
                                Text("Cerrar", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                if (!mediaUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                            .clickable { showZoomDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        coil.compose.AsyncImage(
                            model = mediaUrl,
                            contentDescription = "Fotografía a moderar",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EcoGuiaColors.DeepBlue.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = EcoGuiaColors.Jade)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Cápsula de texto sin foto adjunta",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null, tint = EcoGuiaColors.Jade)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedDrop?.title ?: "Sin título",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedDrop?.description ?: "Sin descripción adicional.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Estado actual: ${selectedDrop?.status ?: "pending"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedDrop?.status == "approved") EcoGuiaColors.Jade else EcoGuiaColors.Gold
                    )
                    Text(
                        text = "ID: ${selectedDrop?.id?.take(8) ?: "N/A"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }



        // Sección de Toma de Decisiones
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Decisión del Moderador", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DecisionItem(
                    title = "Aprobar Cápsula",
                    subtitle = "El contenido cumple con las normas y se publicará en el mapa",
                    color = EcoGuiaColors.Jade,
                    tag = "APROBAR",
                    isSelected = selectedAction == "approved",
                    onClick = { selectedAction = "approved" }
                )

                DecisionItem(
                    title = "Rechazar / Inhabilitar",
                    subtitle = "El contenido viola las reglas comunitarias o está mal ubicado",
                    color = Color.Red,
                    tag = "RECHAZAR",
                    isSelected = selectedAction == "rejected",
                    onClick = { selectedAction = "rejected" }
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón Final de Aplicar Decisión
        Box(modifier = Modifier.padding(24.dp)) {
            EcoButton(
                text = if (isLoading) "Guardando decisión..." else "Aplicar decisión de moderación",
                onClick = {
                    val dropId = selectedDrop?.id
                    if (dropId == null || selectedAction == null) {
                        errorMessage = "No se ha seleccionado una acción válida."
                        return@EcoButton
                    }

                    errorMessage = null
                    moderationViewModel.resolveDrop(
                        dropId = dropId,
                        newStatus = selectedAction!!,
                        onSuccess = onResolve,
                        onError = { msg -> errorMessage = msg }
                    )
                }
            )
        }
    }
}

@Composable
fun DecisionItem(
    title: String,
    subtitle: String,
    color: Color,
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = color)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }

            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
