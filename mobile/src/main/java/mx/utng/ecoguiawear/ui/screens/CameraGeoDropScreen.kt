/**
 * Archivo: CameraGeoDropScreen.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Interfaz de cámara real (CameraX) con visor y retícula de Realidad Aumentada (AR Overlay).
 * Calcula dinámicamente la distancia al Geo-Drop más cercano usando la ubicación GPS actual
 * y permite capturar fotos para anclarlas a la ubicación física del usuario.
 */

package mx.utng.ecoguiawear.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguiawear.ui.components.EcoButton
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.GeoDropViewModel
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Pantalla que visualiza la cámara con retícula AR overlay reactiva a Geo-Drops cercanos.
 *
 * @param onCapture Callback cuando la foto se captura exitosamente.
 * @param geoDropViewModel ViewModel de gestión de Geo-Drops y estado.
 * @param locationViewModel ViewModel para obtener las coordenadas GPS actuales.
 */
@Composable
fun CameraGeoDropScreen(
    onCapture: (File) -> Unit,
    geoDropViewModel: GeoDropViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    val currentLocation by locationViewModel.currentLocation
    val closestGeoDrop by geoDropViewModel.closestGeoDrop
    val distanceToClosest by geoDropViewModel.distanceToClosest

    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
        locationViewModel.startLocationUpdates(context)
        geoDropViewModel.loadGeoDrops()
    }

    LaunchedEffect(currentLocation) {
        geoDropViewModel.updateProximity(currentLocation)
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
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
                Text("Cámara", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Geo-Drops & AR Target", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }

        // Camera Viewport con Retícula AR Overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Log.e("CameraGeoDrop", "Camera binding failed: ${e.message}", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Permiso de cámara requerido para AR", color = Color.White)
            }

            // AR Overlay Retícula Dinámica
            Box(
                modifier = Modifier
                    .size(260.dp, 110.dp)
                    .border(2.dp, EcoGuiaColors.Jade, RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = EcoGuiaColors.DeepBlue.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = closestGeoDrop?.title ?: "Buscando Geo-Drop...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (distanceToClosest != null) "📍 A $distanceToClosest metros" else "Escanear entorno",
                            color = EcoGuiaColors.Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Action Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Cápsula en el entorno", color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = EcoGuiaColors.Jade)
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)
                    ) {
                        Text(
                            "Alinea el encuadre con el objetivo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Presiona capturar para anclar tu foto",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = if (distanceToClosest != null) "$distanceToClosest m" else "--",
                        color = EcoGuiaColors.Jade,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            EcoButton(
                text = "Capturar Geo-Drop",
                onClick = {
                    val photoFile = File(context.cacheDir, "geodrop_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                geoDropViewModel.setCapturedPhoto(photoFile)
                                onCapture(photoFile)
                            }

                            override fun onError(exc: ImageCaptureException) {
                                Log.e("CameraGeoDrop", "Error al capturar foto: ${exc.message}", exc)
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
