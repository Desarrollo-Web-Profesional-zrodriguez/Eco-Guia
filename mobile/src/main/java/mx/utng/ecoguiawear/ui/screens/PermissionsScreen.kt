/**
 * Archivo: PermissionsScreen.kt
 *
 * Pantalla de gestión de permisos necesarios para la experiencia completa de Eco-Guía.
 * Muestra el estado real de cada permiso del sistema y permite activar o desactivar
 * el servicio de Alertas de Proximidad en segundo plano (ProximityService).
 *
 * @since 2026-08-05
 */

package mx.utng.ecoguiawear.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel

/**
 * Pantalla composable para visualizar y autorizar los permisos del sistema (cámara, ubicación, notificaciones).
 *
 * @param locationViewModel ViewModel para sincronizar el estado y controlar el servicio de geocercas.
 */
@Composable
fun PermissionsScreen(
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Leer estado real desde SharedPreferences al componer
    LaunchedEffect(Unit) {
        locationViewModel.syncProximityState(context)
    }
    
    val isProximityActive by locationViewModel.isProximityServiceActive

    // Launcher para ACCESS_BACKGROUND_LOCATION
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationViewModel.startProximityService(context)
        }
    }

    // Launcher para POST_NOTIFICATIONS (API 33+) → al concederse, pide background location
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoGuiaColors.DeepBlue)
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Column {
                Text("Permisos", color = Color.White, fontSize = 14.sp)
                Text("Experiencia", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = { },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = EcoGuiaColors.Gold)
            }
        }

        // Feature Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcoGuiaColors.Surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Activa la experiencia completa", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Ubicación, cámara y notificaciones permiten descubrir historia de forma inmersiva.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Lista de permisos
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Requeridos", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    PermissionToggleItem(
                        title = "Ubicación precisa",
                        subtitle = "Geo-rutas y llegadas a sitios históricos",
                        icon = Icons.Default.Place,
                        permission = Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
                item {
                    PermissionToggleItem(
                        title = "Cámara",
                        subtitle = "Para ver Geo-Drops en realidad aumentada",
                        icon = Icons.Default.CameraAlt,
                        permission = Manifest.permission.CAMERA
                    )
                }
                item {
                    // Toggle de alertas de proximidad con flujo de permisos real
                    ProximityAlertToggleItem(
                        isActive = isProximityActive,
                        onActivate = {
                            // Flujo: API 33+ pide POST_NOTIFICATIONS primero, luego background location
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            }
                        },
                        onDeactivate = {
                            locationViewModel.stopProximityService(context)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Toggle de Alertas de Proximidad.
 * Muestra el estado activo/inactivo del [ProximityService] y gestiona la activación/desactivación
 * con el flujo de permisos correcto para Android 11+ y Android 13+.
 *
 * @param isActive Si el servicio está actualmente activo.
 * @param onActivate Callback para iniciar el flujo de solicitud de permisos y servicio.
 * @param onDeactivate Callback para detener el servicio.
 */
@Composable
fun ProximityAlertToggleItem(
    isActive: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isActive) EcoGuiaColors.Jade.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (isActive) EcoGuiaColors.Jade else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(
                    "Alertas en segundo plano",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (isActive) "Activo · Monitoreando sitios históricos"
                    else "Recibe alertas aunque la app esté cerrada",
                    color = if (isActive) EcoGuiaColors.Jade else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }

            Switch(
                checked = isActive,
                onCheckedChange = { enabled ->
                    if (enabled) onActivate() else onDeactivate()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EcoGuiaColors.Jade,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}

/**
 * Fila genérica de permiso con switch local de estado.
 * Para permisos sin lógica de servicio asociada (ubicación precisa, cámara).
 */
@Composable
fun PermissionToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    permission: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Verifica estado real en el sistema
    val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
        context, permission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
    var isChecked by remember(isGranted) { mutableStateOf(isGranted) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isChecked = granted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EcoGuiaColors.Jade,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, lineHeight = 12.sp)
            }

            Switch(
                checked = isChecked,
                onCheckedChange = { newVal -> 
                    if (newVal) {
                        permissionLauncher.launch(permission)
                    } else {
                        isChecked = false
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EcoGuiaColors.Jade,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionsScreenPreview() {
    EcoGuiaMobileTheme {
        PermissionsScreen()
    }
}
