/**
 * Archivo: AnchorPhotoScreen.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Pantalla de anclaje de fotografía capturada a las coordenadas GPS del sitio histórico.
 * Muestra una vista previa de la foto real y envía el nuevo Geo-Drop a la base de datos Neon PostgreSQL.
 */

package mx.utng.ecoguiawear.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.GeoDropViewModel
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel

/**
 * Pantalla que permite titular, agregar descripción y anclar la foto a la ubicación GPS actual.
 *
 * @param onAnchorClick Callback invocado tras completar exitosamente el anclaje.
 * @param geoDropViewModel ViewModel de estado de Geo-Drops y foto capturada.
 * @param locationViewModel ViewModel para obtener las coordenadas GPS del anclaje.
 */
@Composable
fun AnchorPhotoScreen(
    onAnchorClick: () -> Unit,
    userId: String = "guest",
    geoDropViewModel: GeoDropViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {

    val context = LocalContext.current
    val capturedPhoto by geoDropViewModel.capturedPhoto
    val currentLocation by locationViewModel.currentLocation
    val isSaving by geoDropViewModel.isSaving

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates(context)
    }

    val imageBitmap = remember(capturedPhoto) {
        capturedPhoto?.let { file ->
            if (file.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@remember null
                    val exif = androidx.exifinterface.media.ExifInterface(file.absolutePath)
                    val orientation = exif.getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
                    )
                    val matrix = android.graphics.Matrix()
                    when (orientation) {
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }
                    val rotatedBitmap = android.graphics.Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                    rotatedBitmap.asImageBitmap()
                } catch (e: Exception) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                }
            } else null
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoGuiaColors.DeepBlue)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Nueva Cápsula", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Anclar fotografía al mapa", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }

        // Vista previa de la Fotografía Capturada
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF423A2B), Color(0xFF2B251B))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Foto capturada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "Foto lista para anclaje",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

            }
        }

        // Formulario de Anclaje
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Publicar en comunidad", color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la Cápsula") },
                        placeholder = { Text("Ej. Detalle del arco principal") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = EcoGuiaColors.Jade) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGuiaColors.Jade)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción u Observación") },
                        placeholder = { Text("Ej. Frente a la entrada norte") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = EcoGuiaColors.Jade) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGuiaColors.Jade)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLocation != null)
                                "GPS Fix: ${currentLocation?.latitude?.toString()?.take(7)}, ${currentLocation?.longitude?.toString()?.take(7)}"
                            else "Obteniendo coordenadas GPS...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (currentLocation != null) {
                            Text("OK", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            EcoButton(
                text = if (isSaving) "Guardando cápsula..." else "Anclar foto al sitio",
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Por favor ingresa un título para la cápsula."
                        return@EcoButton
                    }

                    errorMessage = null
                    geoDropViewModel.anchorGeoDrop(
                        title = title,
                        description = description,
                        location = currentLocation,
                        userId = userId,
                        onSuccess = onAnchorClick,
                        onError = { msg -> errorMessage = msg }
                    )

                }
            )
        }
    }
}
