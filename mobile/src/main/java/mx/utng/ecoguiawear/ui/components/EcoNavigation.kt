/**
 * Archivo: EcoNavigation.kt
 *
 * Componentes de navegación global de la aplicación: barra de navegación inferior ([NavigationBar]),
 * riel de navegación lateral ([NavigationRail]) para formato apaisado y tarjetas de acceso a opciones.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

/**
 * Barra de navegación inferior principal de la aplicación móvil.
 *
 * @param currentRoute Ruta activa actualmente seleccionada.
 * @param onNavigate Callback invocado al seleccionar un destino de navegación.
 * @param onOpenSidebar Callback invocado al presionar el botón de menú lateral/contextual.
 * @param isRouteActive Indica si hay una ruta turística activa en navegación GPS.
 */
@Composable
fun EcoBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit,
    isRouteActive: Boolean = false
) {
    NavigationBar(
        containerColor = EcoGuiaColors.DeepBlue,
        contentColor = EcoGuiaColors.Text,
        modifier = Modifier
            .height(70.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = currentRoute == "exploration",
            onClick = { onNavigate("exploration") },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Sitios recomendados") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "search_experience" || currentRoute == "active_route",
            onClick = { onNavigate(if (isRouteActive) "active_route" else "search_experience") },
            icon = {
                Icon(
                    imageVector = if (isRouteActive) Icons.Default.Navigation else Icons.Default.Map,
                    contentDescription = "Las rutas"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = if (isRouteActive) EcoGuiaColors.Gold else EcoGuiaColors.Jade,
                unselectedIconColor = if (isRouteActive) EcoGuiaColors.Gold else EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "more_options",
            onClick = { onNavigate("more_options") },
            icon = { Icon(Icons.Default.GridView, contentDescription = "Centro de navegación") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Gold,
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentRoute == "collection",
            onClick = { onNavigate("collection") },
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Mi Colección") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EcoGuiaColors.Jade,
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onOpenSidebar,
            icon = { Icon(Icons.Default.Menu, contentDescription = "Menú") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = EcoGuiaColors.Muted,
                indicatorColor = Color.Transparent
            )
        )
    }

}

/**
 * Barra de navegación lateral ([NavigationRail]) diseñada para orientaciones apaisadas (Landscape) o pantallas grandes.
 *
 * @param currentRoute Ruta activa actualmente seleccionada.
 * @param onNavigate Callback invocado al seleccionar un destino de navegación.
 * @param onOpenSidebar Callback invocado al presionar el botón de menú.
 * @param isRouteActive Indica si hay una ruta turística activa en navegación GPS.
 */
@Composable
fun EcoNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOpenSidebar: () -> Unit,
    isRouteActive: Boolean = false
) {
    NavigationRail(
        containerColor = EcoGuiaColors.DeepBlue,
        contentColor = EcoGuiaColors.Text,
        modifier = Modifier.width(76.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationRailItem(
                selected = currentRoute == "exploration",
                onClick = { onNavigate("exploration") },
                icon = { Icon(Icons.Default.LocationOn, contentDescription = "Sitios recomendados") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = EcoGuiaColors.Jade,
                    unselectedIconColor = EcoGuiaColors.Muted,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationRailItem(
                selected = currentRoute == "search_experience" || currentRoute == "active_route",
                onClick = { onNavigate(if (isRouteActive) "active_route" else "search_experience") },
                icon = {
                    Icon(
                        imageVector = if (isRouteActive) Icons.Default.Navigation else Icons.Default.Map,
                        contentDescription = "Las rutas"
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = if (isRouteActive) EcoGuiaColors.Gold else EcoGuiaColors.Jade,
                    unselectedIconColor = if (isRouteActive) EcoGuiaColors.Gold else EcoGuiaColors.Muted,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationRailItem(
                selected = currentRoute == "more_options",
                onClick = { onNavigate("more_options") },
                icon = { Icon(Icons.Default.GridView, contentDescription = "Centro de navegación") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = EcoGuiaColors.Gold,
                    unselectedIconColor = EcoGuiaColors.Muted,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationRailItem(
                selected = currentRoute == "collection",
                onClick = { onNavigate("collection") },
                icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Mi Colección") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = EcoGuiaColors.Jade,
                    unselectedIconColor = EcoGuiaColors.Muted,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationRailItem(
                selected = false,
                onClick = onOpenSidebar,
                icon = { Icon(Icons.Default.Menu, contentDescription = "Menú") },
                colors = NavigationRailItemDefaults.colors(
                    unselectedIconColor = EcoGuiaColors.Muted,
                    indicatorColor = Color.Transparent
                )
            )

        }
    }

}

/**
 * Tarjeta de opción para menús de navegación en cuadrícula o lista (por ejemplo en MoreOptionsScreen).
 *
 * @param title Título principal de la opción.
 * @param subtitle Descripción breve de la funcionalidad.
 * @param icon Ícono vectorial representativo.
 * @param enabled Indica si la tarjeta está habilitada para interacción.
 * @param onClick Callback invocado al pulsar sobre la tarjeta.
 */
@Composable
fun MenuOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.White else Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) EcoGuiaColors.Jade else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp, 
                color = if (enabled) Color.Black else Color.Gray
            )
            Text(
                text = subtitle, 
                fontSize = 10.sp, 
                color = Color.Gray, 
                lineHeight = 12.sp
            )
        }
    }
}
