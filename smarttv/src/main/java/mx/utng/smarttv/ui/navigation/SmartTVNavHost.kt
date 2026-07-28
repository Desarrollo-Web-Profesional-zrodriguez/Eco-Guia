package mx.utng.smarttv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.utng.smarttv.ui.screens.GalleryScreen
import mx.utng.smarttv.ui.screens.HeatmapScreen
import mx.utng.smarttv.ui.screens.LobbyScreen
import mx.utng.smarttv.ui.screens.Portal360Screen

sealed class TVRoutes(val route: String) {
    object Lobby : TVRoutes("lobby")
    object Heatmap : TVRoutes("heatmap")
    object Gallery : TVRoutes("gallery")
    object Portal360 : TVRoutes("portal360")
}

@Composable
fun SmartTVNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = TVRoutes.Lobby.route) {
        composable(TVRoutes.Lobby.route) {
            LobbyScreen(
                onNavigateToHeatmap = { navController.navigate(TVRoutes.Heatmap.route) },
                onNavigateToGallery = { navController.navigate(TVRoutes.Gallery.route) },
                onNavigateToPortal360 = { navController.navigate(TVRoutes.Portal360.route) }
            )
        }
        composable(TVRoutes.Heatmap.route) {
            HeatmapScreen(onBack = { navController.popBackStack() })
        }
        composable(TVRoutes.Gallery.route) {
            GalleryScreen(onBack = { navController.popBackStack() })
        }
        composable(TVRoutes.Portal360.route) {
            Portal360Screen(onBack = { navController.popBackStack() })
        }
    }
}
