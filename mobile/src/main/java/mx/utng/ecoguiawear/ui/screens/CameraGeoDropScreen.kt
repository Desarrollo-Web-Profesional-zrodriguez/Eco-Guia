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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
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
    userId: String = "",
    onSavedToCollection: () -> Unit = {},
    geoDropViewModel: GeoDropViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    val currentLocation by locationViewModel.currentLocation
    val closestGeoDrop by geoDropViewModel.closestGeoDrop
    val distanceToClosest by geoDropViewModel.distanceToClosest
    val isSaving by geoDropViewModel.isSaving

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

    LaunchedEffect(currentLocation, userId) {
        geoDropViewModel.updateProximity(currentLocation, userId)
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
                Text(
                    text = if (closestGeoDrop != null && distanceToClosest != null && distanceToClosest!! <= 50) "Cápsula Encontrada" else "Explorador AR",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = closestGeoDrop?.title ?: "Escanear entorno con la cámara",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // Visor de Cámara con Retícula AR
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(2.dp, EcoGuiaColors.Jade, RoundedCornerShape(32.dp))
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
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    context as androidx.lifecycle.LifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (exc: Exception) {
                                Log.e("CameraGeoDrop", "Error al vincular cámara: ${exc.message}", exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Se requiere permiso de cámara para ver el visor AR", color = Color.White)
                }
            }

            // Retícula AR overlay
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .align(Alignment.Center)
                    .border(2.dp, EcoGuiaColors.Gold, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                        tint = EcoGuiaColors.Gold,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = closestGeoDrop?.title ?: "Buscando Cápsula...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (distanceToClosest != null) "A $distanceToClosest metros" else "Escanear entorno",
                        color = EcoGuiaColors.Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Action Panel
        val nearbyList by geoDropViewModel.nearbyGeoDrops
        val collectedMap = geoDropViewModel.collectedGeoDropIds

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val detectionLimit = closestGeoDrop?.detectionRadiusM ?: 50
            val isExistingNearby = closestGeoDrop != null && distanceToClosest != null && distanceToClosest!! <= detectionLimit

            Text(
                text = if (nearbyList.size > 1) "Cápsulas detectadas (${nearbyList.size})" else if (isExistingNearby) "Cápsula detectada en el área" else "Anclar nueva cápsula",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selector dinámico si hay múltiples GeoDrops cercanos
            if (nearbyList.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    items(nearbyList) { drop ->
                        val isSelected = drop.id == closestGeoDrop?.id
                        val isAlreadyCaptured = collectedMap[drop.id] == true
                        FilterChip(
                            selected = isSelected,
                            onClick = { geoDropViewModel.selectGeoDrop(drop, userId) },
                            label = { 
                                Text(
                                    if (isAlreadyCaptured) "${drop.title} (Capturado)" else drop.title,
                                    fontSize = 12.sp
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EcoGuiaColors.Gold,
                                selectedLabelColor = EcoGuiaColors.DeepBlue,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            val currentDropCaptured = closestGeoDrop?.id?.let { collectedMap[it] } == true

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentDropCaptured) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (currentDropCaptured) EcoGuiaColors.Gold else EcoGuiaColors.Jade
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = if (currentDropCaptured) "¡Este Geo-Drop ya está capturado en tu colección!" else if (isExistingNearby) "Existe un Geo-Drop a $distanceToClosest metros" else "Alinea el encuadre con el objetivo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentDropCaptured) "Ya tienes guardada esta cápsula. Puedes volver a verla en Mi Colección." else if (isExistingNearby) "Puedes agregar este Geo-Drop a tu colección o crear una foto nueva" else "Presiona capturar para anclar tu foto",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isExistingNearby && closestGeoDrop != null) {
                EcoButton(
                    text = if (currentDropCaptured) "Geo-Drop Ya Capturado" else "Coleccionar Geo-Drop (${closestGeoDrop?.title})",
                    onClick = {
                        if (currentDropCaptured) return@EcoButton
                        val drop = closestGeoDrop ?: return@EcoButton
                        geoDropViewModel.saveExistingGeoDropToCollection(
                            userId = userId,
                            geoDrop = drop,
                            onSuccess = { onSavedToCollection() },
                            onError = { _ -> }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !currentDropCaptured
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            EcoButton(
                text = if (isExistingNearby) "Anclar una foto nueva propia" else "Capturar Geo-Drop",
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
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
