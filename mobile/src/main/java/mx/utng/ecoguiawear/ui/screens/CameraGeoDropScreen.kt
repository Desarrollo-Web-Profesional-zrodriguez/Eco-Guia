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
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary

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
    onSkip: (() -> Unit)? = null,
    isSiteCreationMode: Boolean = false,
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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val photoFile = File(context.cacheDir, "geodrop_${System.currentTimeMillis()}.jpg")
                    photoFile.outputStream().use { output ->
                        inputStream?.copyTo(output)
                    }
                    geoDropViewModel.setCapturedPhoto(photoFile)
                    onCapture(photoFile)
                } catch (e: Exception) {
                    Log.e("CameraGeoDrop", "Error copiando imagen de galería: ${e.message}", e)
                }
            }
        }
    )

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

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (_: Exception) {}
            cameraExecutor.shutdown()
        }
    }

        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val nearbyList by geoDropViewModel.nearbyGeoDrops
    val collectedMap = geoDropViewModel.collectedGeoDropIds
    val isExistingNearby = !isSiteCreationMode && closestGeoDrop != null && distanceToClosest != null && distanceToClosest!! <= (closestGeoDrop?.detectionRadiusM ?: 50)
    val currentDropCaptured = !isSiteCreationMode && closestGeoDrop?.id?.let { collectedMap[it] } == true

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoGuiaColors.DeepBlue)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Izquierda: Visor de Cámara con Retícula AR
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, EcoGuiaColors.Jade, RoundedCornerShape(24.dp))
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
                                        lifecycleOwner,
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
                        Text("Se requiere permiso de cámara para ver el visor AR", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Retícula AR
                if (!isSiteCreationMode && closestGeoDrop != null && distanceToClosest != null && distanceToClosest!! <= (closestGeoDrop?.detectionRadiusM ?: 50)) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.Center)
                            .border(2.dp, EcoGuiaColors.Gold, RoundedCornerShape(20.dp))
                            .background(EcoGuiaColors.Gold.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = null,
                                tint = EcoGuiaColors.Gold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = closestGeoDrop?.title ?: "Buscando...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (distanceToClosest != null) "A $distanceToClosest m" else "Escanear",
                                color = EcoGuiaColors.Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Derecha: Panel de Controles y Botones
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isSiteCreationMode) "Anclar foto al Sitio" else if (closestGeoDrop != null && distanceToClosest != null && distanceToClosest!! <= 50) "Cápsula Encontrada" else "Explorador AR",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSiteCreationMode) "Fotografía identificativa del lugar" else closestGeoDrop?.title ?: "Escanear entorno con la cámara",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                if (!isSiteCreationMode && nearbyList.size > 1) {
                    Text("Cápsulas detectadas (${nearbyList.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(nearbyList) { drop ->
                            val isSelected = drop.id == closestGeoDrop?.id
                            val isAlreadyCaptured = collectedMap[drop.id] == true
                            FilterChip(
                                selected = isSelected,
                                onClick = { geoDropViewModel.selectGeoDrop(drop, userId) },
                                label = { Text(if (isAlreadyCaptured) "${drop.title} (Capturado)" else drop.title, fontSize = 11.sp) },
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentDropCaptured) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (currentDropCaptured) EcoGuiaColors.Gold else EcoGuiaColors.Jade,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                            Text(
                                text = if (isSiteCreationMode) "Toma o sube una foto descriptiva para tu sitio" else if (currentDropCaptured) "¡Cápsula ya en tu colección!" else if (isExistingNearby) "Geo-Drop a $distanceToClosest m" else "Alinea el encuadre",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (!isSiteCreationMode && isExistingNearby && closestGeoDrop != null) {
                    EcoButton(
                        text = if (currentDropCaptured) "Geo-Drop Ya Capturado" else "Coleccionar (${closestGeoDrop?.title})",
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
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                            Text("Galería", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    EcoButton(
                        text = "Tomar Foto",
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
                        modifier = Modifier.weight(1.2f)
                    )
                }

                if (isSiteCreationMode && onSkip != null) {
                    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                        Text("Omitir por ahora (Finalizar sin anclar)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoGuiaColors.DeepBlue)
        ) {
            // Visor de Cámara de pantalla completa
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
                                    lifecycleOwner,
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

            // Overlay Gradiente Superior (Header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(top = 36.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                Column {
                    Text(
                        text = if (isSiteCreationMode) "Anclar foto al Sitio" else if (closestGeoDrop != null && distanceToClosest != null && distanceToClosest!! <= 50) "Cápsula Encontrada" else "Explorador AR",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSiteCreationMode) "Fotografía identificativa del lugar" else closestGeoDrop?.title ?: "Escanear entorno con la cámara",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            // Retícula AR en el centro
            if (!isSiteCreationMode && closestGeoDrop != null && distanceToClosest != null && distanceToClosest!! <= (closestGeoDrop?.detectionRadiusM ?: 50)) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.Center)
                        .border(2.dp, EcoGuiaColors.Gold, RoundedCornerShape(24.dp))
                        .background(EcoGuiaColors.Gold.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = null,
                            tint = EcoGuiaColors.Gold,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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

            // Action Panel Inferior Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isSiteCreationMode) {
                        if (nearbyList.size > 1) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                            containerColor = Color.Black.copy(alpha = 0.6f),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentDropCaptured) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (currentDropCaptured) EcoGuiaColors.Gold else EcoGuiaColors.Jade,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = if (isSiteCreationMode) "Toma o sube una foto descriptiva" else if (currentDropCaptured) "¡Este Geo-Drop ya está en tu colección!" else if (isExistingNearby) "Existe un Geo-Drop a $distanceToClosest metros" else "Alinea el encuadre con el objetivo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSiteCreationMode) "Las fotos ayudan a identificar el lugar." else if (currentDropCaptured) "Ya tienes guardada esta cápsula. Puedes verla en Mi Colección." else if (isExistingNearby) "Puedes agregar este Geo-Drop a tu colección" else "Presiona capturar para anclar tu foto",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (!isSiteCreationMode && isExistingNearby && closestGeoDrop != null) {
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
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Galería", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        EcoButton(
                            text = "Tomar Foto",
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
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    if (isSiteCreationMode && onSkip != null) {
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Omitir por ahora (Finalizar sin anclar)",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}