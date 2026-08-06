/**
 * Archivo: AnchorPhotoScreen.kt
 *
 * Pantalla de anclaje de fotografía capturada a las coordenadas GPS del sitio histórico.
 * Muestra una vista previa de la foto real y envía el nuevo Geo-Drop a la base de datos Neon PostgreSQL.
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import mx.utng.ecoguiawear.ui.components.EcoTextField
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.viewmodel.GeoDropViewModel
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel

import mx.utng.ecoguiawear.ui.components.GeoDropSavingDialog

/**
 * Pantalla que permite titular, agregar descripción y anclar la foto a la ubicación GPS actual.
 *
 * @param onAnchorClick Callback invocado tras completar exitosamente el anclaje.
 * @param userId Identificador del usuario autenticado.
 * @param geoDropViewModel ViewModel de estado de Geo-Drops y foto capturada.
 * @param locationViewModel ViewModel para obtener las coordenadas GPS del anclaje.
 */
@Composable
fun AnchorPhotoScreen(
    onAnchorClick: () -> Unit,
    userId: String = "guest",
    userRole: String = "visitor",
    geoDropViewModel: GeoDropViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {

    val context = LocalContext.current
    val capturedPhoto by geoDropViewModel.capturedPhoto
    val currentLocation by locationViewModel.currentLocation
    val isSaving by geoDropViewModel.isSaving
    val savingStep by geoDropViewModel.savingStep

    if (isSaving) {
        GeoDropSavingDialog(currentStep = savingStep)
    }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

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

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val allSites by geoDropViewModel.allSites
    val targetSiteId by geoDropViewModel.targetSiteId
    val targetSiteName by geoDropViewModel.targetSiteName
    var selectedSiteId by rememberSaveable(targetSiteId) { mutableStateOf(targetSiteId.orEmpty()) }
    var isSiteDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentLocation, userId, userRole) {
        geoDropViewModel.loadSites(currentLocation, userId = userId, userRole = userRole)
    }

    val isCreationMode by geoDropViewModel.isSiteCreationMode

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoGuiaColors.DeepBlue)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Columna Izquierda: Vista previa de foto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nueva Cápsula", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Anclar fotografía al mapa", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
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
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Columna Derecha: Formulario de Anclaje
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Publicar en comunidad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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

                        if (!isCreationMode) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                val selectedSite = allSites.firstOrNull { it.id == selectedSiteId }
                                val currentSiteName = selectedSite?.name
                                    ?: targetSiteName
                                    ?: if (allSites.isNotEmpty()) allSites.first().name else "Fuera del rango de un sitio histórico"

                                val isAdminRole = userRole == "admin" || userRole == "superadmin"
                                val isMuseumRole = userRole == "museum_hotel"
                                val isDropdownEnabled = when {
                                    isAdminRole -> allSites.size > 1
                                    isMuseumRole -> allSites.size > 1
                                    else -> false // Para visitor y moderator queda completamente deshabilitado
                                }

                                EcoTextField(
                                    value = currentSiteName,
                                    onValueChange = {},
                                    label = "SITIO HISTÓRICO PERTENECIENTE *",
                                    placeholder = "Sitio detectado automáticamente",
                                    readOnly = true,
                                    trailingIcon = if (isDropdownEnabled) {
                                        { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EcoGuiaColors.Jade) }
                                    } else null
                                )

                                if (isDropdownEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { isSiteDropdownExpanded = true }
                                    )

                                    DropdownMenu(
                                        expanded = isSiteDropdownExpanded,
                                        onDismissRequest = { isSiteDropdownExpanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .background(EcoGuiaColors.Surface)
                                    ) {
                                        allSites.forEach { site ->
                                            DropdownMenuItem(
                                                text = { Text(site.name, color = Color.White, fontSize = 14.sp) },
                                                onClick = {
                                                    selectedSiteId = site.id
                                                    geoDropViewModel.setTargetSite(site.id, site.name, isCreationMode = false)
                                                    isSiteDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLocation != null)
                                    "GPS Fix: ${currentLocation?.latitude?.toString()?.take(7)}, ${currentLocation?.longitude?.toString()?.take(7)}"
                                else "Obteniendo GPS...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            if (currentLocation != null) {
                                Text("OK", color = EcoGuiaColors.Jade, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                }

                EcoButton(
                    text = if (isSaving) "Guardando..." else "Anclar foto al sitio",
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Por favor ingresa un título para la cápsula."
                            return@EcoButton
                        }
                        if (selectedSiteId.isBlank()) {
                            errorMessage = "Por favor selecciona el sitio histórico al que pertenece la cápsula."
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
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoGuiaColors.DeepBlue)
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                Column {
                    Text("Nueva Cápsula", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Anclar fotografía al mapa", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }

            // Vista previa de la Fotografía Capturada
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
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
                    .padding(20.dp)
            ) {
                Text("Publicar en comunidad", color = Color.White, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))

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

                        if (!isCreationMode) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                val selectedSite = allSites.firstOrNull { it.id == selectedSiteId }
                                val currentSiteName = selectedSite?.name
                                    ?: targetSiteName
                                    ?: if (allSites.isNotEmpty()) allSites.first().name else "Fuera del rango de un sitio histórico"

                                val isAdminRole = userRole == "admin" || userRole == "superadmin"
                                val isMuseumRole = userRole == "museum_hotel"
                                val isDropdownEnabled = when {
                                    isAdminRole -> allSites.size > 1
                                    isMuseumRole -> allSites.size > 1
                                    else -> false // Para visitor y moderator queda completamente deshabilitado
                                }

                                EcoTextField(
                                    value = currentSiteName,
                                    onValueChange = {},
                                    label = "SITIO HISTÓRICO PERTENECIENTE *",
                                    placeholder = "Sitio detectado automáticamente",
                                    readOnly = true,
                                    trailingIcon = if (isDropdownEnabled) {
                                        { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EcoGuiaColors.Jade) }
                                    } else null
                                )

                                if (isDropdownEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { isSiteDropdownExpanded = true }
                                    )

                                    DropdownMenu(
                                        expanded = isSiteDropdownExpanded,
                                        onDismissRequest = { isSiteDropdownExpanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .background(EcoGuiaColors.Surface)
                                    ) {
                                        allSites.forEach { site ->
                                            DropdownMenuItem(
                                                text = { Text(site.name, color = Color.White, fontSize = 14.sp) },
                                                onClick = {
                                                    selectedSiteId = site.id
                                                    geoDropViewModel.setTargetSite(site.id, site.name, isCreationMode = false)
                                                    isSiteDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

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

                Spacer(modifier = Modifier.height(20.dp))

                EcoButton(
                    text = if (isSaving) "Guardando cápsula..." else "Anclar foto al sitio",
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Por favor ingresa un título para la cápsula."
                            return@EcoButton
                        }
                        if (selectedSiteId.isBlank()) {
                            errorMessage = "Por favor selecciona el sitio histórico al que pertenece la cápsula."
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
}
