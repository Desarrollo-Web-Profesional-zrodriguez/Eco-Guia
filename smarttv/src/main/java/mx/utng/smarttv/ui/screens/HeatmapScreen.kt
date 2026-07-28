@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.smarttv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun HeatmapScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mapa de Calor de Dolores Hidalgo",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // Simulación visual de mapa de calor usando un Box grande
        Box(
            modifier = Modifier
                .size(600.dp, 300.dp)
                .background(Color(0xFF142C52)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cargando datos del mapa en tiempo real...",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            
            // Puntos simulados
            Box(modifier = Modifier.size(20.dp).background(Color.Red).align(Alignment.TopStart))
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFFF9800)).align(Alignment.Center))
            Box(modifier = Modifier.size(15.dp).background(Color(0xFFFFC107)).align(Alignment.BottomEnd))
        }

        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onBack) {
            Text("Volver al Inicio")
        }
    }
}
