/**
 * Archivo: ExplorationMapContent.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Componente de mapa interactivo para la pantalla de Exploración. Renderiza
 * el GoogleMap con estilo dinámico (día/noche), los marcadores categorizados de sitios
 * históricos, sus radios de detección y los controles personalizados de zoom y ubicación.
 *
 * Funciones destacadas:
 * - ExplorationMapContent: Box con GoogleMap, botón de mira y controles de zoom.
 * - RecommendedSiteItem: Tarjeta de lista para un sitio con icono, título y botón "Ver".
 */

package mx.utng.ecoguiawear.ui.feature.exploration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Contenido principal del mapa en la pantalla de Exploración.
 *
 * Incluye:
 * - GoogleMap con estilo día/noche automático.
 * - Marcadores personalizados por categoría para cada sitio cercano.
 * - Círculos de detección alrededor de cada sitio.
 * - Botón de mira que reactiva el seguimiento de la posición del usuario.
 * - Botones de zoom personalizados (+/-).
 *
 * @param modifier Modifier externo (controla tamaño según modo portrait/landscape).
 * @param nearbySites Lista de sitios históricos cargados desde el ViewModel.
 * @param currentLocation Última ubicación conocida del usuario (puede ser null).
 * @param cameraPositionState Estado de la cámara del mapa.
 * @param isFollowingUser Indica si el mapa sigue automáticamente al usuario.
 * @param onFollowUser Callback para reactiva el seguimiento de ubicación.
 * @param scope CoroutineScope para animaciones de cámara.
 */
@Composable
fun ExplorationMapContent(
    modifier: Modifier = Modifier,
    nearbySites: List<RemoteHistoricalSite>,
    currentLocation: android.location.Location?,
    cameraPositionState: CameraPositionState,
    isFollowingUser: Boolean,
    onFollowUser: () -> Unit,
    scope: CoroutineScope
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE8F5E9))
    ) {
        val isSystemInDark = isSystemInDarkTheme()
        val mapStyleOptions = remember(isSystemInDark) {
            if (isSystemInDark) {
                com.google.android.gms.maps.model.MapStyleOptions(
                    """[{"elementType":"geometry","stylers":[{"color":"#242f3e"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},{"elementType":"labels.text.stroke","stylers":[{"color":"#242f3e"}]},{"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#263c3f"}]},{"featureType":"poi.park","elementType":"labels.text.fill","stylers":[{"color":"#6b9a76"}]},{"featureType":"road","elementType":"geometry","stylers":[{"color":"#38414e"}]},{"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212a37"}]},{"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#9ca5b3"}]},{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#746855"}]},{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1f2835"}]},{"featureType":"road.highway","elementType":"labels.text.fill","stylers":[{"color":"#f3d19c"}]},{"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2f3948"}]},{"featureType":"transit.station","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#17263c"}]},{"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]},{"featureType":"water","elementType":"labels.text.stroke","stylers":[{"color":"#17263c"}]}]"""
                )
            } else null
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = currentLocation != null,
                mapStyleOptions = mapStyleOptions
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false
            )
        ) {
            nearbySites.forEach { site ->
                val siteLat = site.latitude ?: return@forEach
                val siteLng = site.longitude ?: return@forEach
                val sitePos = LatLng(siteLat, siteLng)
                val customIcon = remember(site.siteType) {
                    getCustomCategoryMarkerIcon(context, site.siteType)
                }

                Circle(
                    center = sitePos,
                    radius = site.detectionRadiusM.toDouble(),
                    strokeColor = EcoGuiaColors.Jade,
                    strokeWidth = 3f,
                    fillColor = EcoGuiaColors.Jade.copy(alpha = 0.15f)
                )

                Marker(
                    state = MarkerState(position = sitePos),
                    title = site.name,
                    snippet = site.shortDescription,
                    icon = customIcon
                )
            }
        }

        // Botón de Mira (Ubicación) — esquina Superior Derecha
        IconButton(
            onClick = onFollowUser,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(
                    if (isFollowingUser) EcoGuiaColors.Jade else Color.White,
                    CircleShape
                )
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Mi ubicación",
                tint = if (isFollowingUser) Color.White else EcoGuiaColors.DeepBlue
            )
        }

        // Controles de Zoom Personalizados — esquina Inferior Derecha
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = {
                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                },
                modifier = Modifier
                    .background(EcoGuiaColors.Jade, CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Add, "Zoom In", tint = Color.White)
            }
            IconButton(
                onClick = {
                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                },
                modifier = Modifier
                    .background(EcoGuiaColors.Jade, CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Remove, "Zoom Out", tint = Color.White)
            }
        }
    }
}

/**
 * Tarjeta de lista para un sitio recomendado. Muestra ícono, título, subtítulo
 * y un botón "Ver" en color Jade.
 *
 * @param title Nombre del sitio.
 * @param subtitle Tipo y dirección del sitio concatenados.
 * @param icon Vector icon de estado (favorito o marcador).
 * @param trailing Texto del botón de acción (usualmente "Ver").
 * @param onVerClick Callback invocado al pulsar la tarjeta o el botón.
 */
@Composable
fun RecommendedSiteItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: String,
    onVerClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onVerClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EcoGuiaColors.Jade,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = EcoGuiaColors.Jade.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                onClick = onVerClick
            ) {
                Text(
                    text = trailing,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGuiaColors.Jade
                )
            }
        }
    }
}
