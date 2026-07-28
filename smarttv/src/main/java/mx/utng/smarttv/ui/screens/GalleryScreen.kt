package mx.utng.smarttv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
fun GalleryScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Galería Comunitaria",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cuadrícula simulada de imágenes
            Column {
                Row {
                    Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.Gray))
                    Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.DarkGray))
                }
                Row {
                    Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.DarkGray))
                    Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.Gray))
                }
            }
            
            Spacer(modifier = Modifier.width(64.dp))
            
            // Simulación de código QR
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Escanea para subir tu foto:", color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for actual QR code
                    Box(modifier = Modifier.size(130.dp).background(Color.Black))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onBack) {
            Text("Volver al Inicio")
        }
    }
}
