@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.smarttv.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import mx.utng.smarttv.network.TvLocalServer

@Composable
fun GalleryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val receivedImage by TvLocalServer.receivedImage.collectAsState()
    val ip = remember { TvLocalServer.getLocalIpAddress(context) ?: "localhost" }

    LaunchedEffect(Unit) {
        TvLocalServer.startServer()
    }

    DisposableEffect(Unit) {
        onDispose {
            TvLocalServer.stopServer()
        }
    }

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
        Text(
            text = "Servidor activo en: $ip",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Green.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cuadrícula de imágenes (Recibidas y simuladas)
            Column {
                Row {
                    if (receivedImage != null) {
                        Image(
                            bitmap = receivedImage!!.asImageBitmap(),
                            contentDescription = "Foto recibida",
                            modifier = Modifier.size(150.dp).padding(4.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.Gray)) {
                            Text("Esperando foto...", color = Color.White, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.DarkGray))
                }
                Row {
                    Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.DarkGray))
                    Box(modifier = Modifier.size(150.dp).padding(4.dp).background(Color.Gray))
                }
            }
            
            Spacer(modifier = Modifier.width(64.dp))
            
            // QR Real
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Escanea para subir tu foto:", color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
                
                val qrBitmap = rememberQrBitmap("ecoguia://tv/gallery?ip=$ip&port=8080")
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "QR para aplicación móvil",
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.White)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error QR", color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onBack) {
            Text("Volver al Inicio")
        }
    }
}

@Composable
fun rememberQrBitmap(content: String, size: Int = 512): androidx.compose.ui.graphics.ImageBitmap? {
    return remember(content) {
        try {
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
