package mx.utng.smarttv.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.Text

@Composable
fun LobbyScreen(
    onNavigateToHeatmap: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToPortal360: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Lobby Screen (TODO)")
    }
}
