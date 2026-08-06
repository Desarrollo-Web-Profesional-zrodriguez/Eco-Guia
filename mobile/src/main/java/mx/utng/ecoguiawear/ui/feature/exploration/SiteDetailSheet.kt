/**
 * Archivo: SiteDetailSheet.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Componente BottomSheet de detalle de un sitio histórico. Muestra nombre, tipo,
 * dirección y descripción corta. Permite al usuario guardar el sitio en su colección personal
 * o navegar hacia él sincronizando con el reloj Wear OS.
 *
 * Funciones destacadas:
 * - SiteDetailSheet: ModalBottomSheet animado con acciones Guardar / Navegar.
 */

package mx.utng.ecoguiawear.ui.feature.exploration

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * Contenido interno del BottomSheet de detalle de un sitio histórico.
 *
 * Muestra el nombre, tipo, dirección, descripción del sitio y ofrece dos acciones:
 * guardar en colección (toggle animado) y navegar hacia el sitio.
 *
 * @param site Sitio histórico a mostrar.
 * @param userId ID del usuario autenticado. Si es "guest" los botones estarán deshabilitados para guardar.
 * @param collectionViewModel ViewModel que gestiona el estado de guardado.
 * @param onNavigate Callback invocado cuando el usuario pulsa "Navegar".
 * @param onDismiss Callback invocado cuando el usuario cierra el sheet.
 */
@Composable
fun SiteDetailSheet(
    site: RemoteHistoricalSite,
    userId: String,
    collectionViewModel: CollectionViewModel,
    isWithinRange: Boolean = false,
    isUserAdmin: Boolean = false,
    onNavigate: () -> Unit,
    onGeoDropClick: () -> Unit = {},
    onDismiss: () -> Unit
) {


    val isSaved = collectionViewModel.savedSiteIds[site.id] == true

    val saveButtonColor by animateColorAsState(
        targetValue = if (isSaved) EcoGuiaColors.Jade else EcoGuiaColors.Surface,
        animationSpec = tween(300),
        label = "save_color"
    )
    val saveButtonTextColor by animateColorAsState(
        targetValue = if (isSaved) Color.White else EcoGuiaColors.Jade,
        animationSpec = tween(300),
        label = "save_text_color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Handle visual
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Ícono + nombre del sitio
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(EcoGuiaColors.Jade.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = EcoGuiaColors.Jade,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = site.siteType,
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoGuiaColors.Jade
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dirección
        val address = site.address.orEmpty()
        if (address.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = EcoGuiaColors.Jade,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Descripción corta
        val shortDesc = site.shortDescription.orEmpty()
        if (shortDesc.isNotBlank()) {
            Text(
                text = shortDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Se habilita si el usuario está en rango O si es dueño/administrador del sitio
            val canAddGeoDrop = isWithinRange || site.createdBy == userId || isUserAdmin
            if (canAddGeoDrop) {
                Button(
                    onClick = {
                        onDismiss()
                        onGeoDropClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoGuiaColors.Jade,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isUserAdmin -> "Agregar Geo-Drop (Cámara / Galería)"
                            isWithinRange -> "Capturar Geo-Drop (AR / Galería)"
                            else -> "Publicar Geo-Drop en mi Sitio"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Guardar / Guardado (toggle animado)
                Button(
                    onClick = {
                        if (userId != "guest") {
                            collectionViewModel.toggleSave(userId, site.id)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = saveButtonColor,
                        contentColor = saveButtonTextColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (!isSaved) ButtonDefaults.outlinedButtonBorder else null
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSaved) "Guardado" else "Guardar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Botón Navegar — sincroniza destino con el reloj Wear OS
                Button(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoGuiaColors.Gold,
                        contentColor = EcoGuiaColors.DeepBlue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Navegar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }


        // Aviso si el usuario no está autenticado
        if (userId == "guest") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Inicia sesión para guardar en tu colección",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
