/**
 * Archivo: MainActivity.kt
 * Autor: Zahir Rodriguez
 * Fecha de última actualización: 2026-07-24
 * Descripción: Actividad principal y punto de entrada de la aplicación. Configura el Scaffold global,
 * la navegación mediante NavHost y gestiona los permisos críticos de ubicación y cámara.
 * 
 * Funciones destacadas:
 * - MainAppContainer: Gestiona la estructura global de la UI y la inyección de ViewModels.
 * - permissionLauncher: Maneja la solicitud de permisos de GPS en tiempo de ejecución.
 */

package mx.utng.ecoguiawear

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase

import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import mx.utng.ecoguiawear.ui.screens.*
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.AuthViewModel
import mx.utng.ecoguiawear.ui.viewmodel.NotificationViewModel
import mx.utng.ecoguiawear.ui.viewmodel.NotificationType
import mx.utng.ecoguiawear.ui.viewmodel.SiteRegistrationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguiawear.ui.components.EcoBottomBar
import mx.utng.ecoguiawear.ui.components.BottomMenuSheet

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewModelScope

import mx.utng.ecoguiawear.ui.screens.admin.*

class MainActivity : ComponentActivity() {
    private val repository = EcoGuiaRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Fuerza el diseño pantalla completa e inmersivo
        setContent {
            EcoGuiaMobileTheme {
                MainAppContainer(this, repository)
            }
        }
    }
}

/**
 * Contenedor principal que gestiona el Scaffold global, la navegación y las notificaciones.
 */
@Composable
fun MainAppContainer(activity: ComponentActivity, repository: EcoGuiaRepositoryImpl) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val siteRegistrationViewModel: SiteRegistrationViewModel = viewModel()
    val routeViewModel: mx.utng.ecoguiawear.ui.viewmodel.RouteViewModel = viewModel()
    val isRouteActive by routeViewModel.activeRoute.run { remember { derivedStateOf { value != null } } }

    // Gestión de Permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            notificationViewModel.showNotification(
                "La app necesita GPS para funcionar correctamente.", 
                NotificationType.INFO
            )
        }
    }

    // Inicializar vinculación de ViewModels para notificaciones automáticas y pedir permisos
    LaunchedEffect(Unit) {
        authViewModel.initNotifications(notificationViewModel)
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Observar notificaciones reactivas
    val notification by notificationViewModel.currentNotification
    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it.message)
        }
    }
    
    // Estado de Usuario (Simulado Admin)
    var isAdmin by remember { mutableStateOf(true) }
    var showBottomMenu by remember { mutableStateOf(false) }

    // Pantallas de Auth que ocultan la navegación
    val authRoutes = listOf("login", "signup", "recovery")
    val showNav = currentRoute !in authRoutes

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showNav) {
                EcoBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == "logout") {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo("exploration") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onOpenSidebar = {
                        showBottomMenu = true
                    },
                    isRouteActive = isRouteActive
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { 
                            navController.navigate("exploration") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onSignUpClick = { navController.navigate("signup") },
                        onRecoverClick = { navController.navigate("recovery") }
                    )
                }
                composable("signup") {
                    SignUpScreen(
                        viewModel = authViewModel,
                        onSignUpSuccess = { },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }
                composable("recovery") {
                    RecoveryScreen(
                        onSendClick = { navController.popBackStack() },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }
                
                // Rutas de Aplicación Principal
                composable("exploration") {
                    ExplorationScreen(
                        onAdminClick = { navController.navigate("more_options") },
                        onOpenRoutes = { navController.navigate("search_experience") },
                        userId = authViewModel.currentUser?.id ?: "guest"
                    )
                }
                composable("collection") {
                    MyCollectionScreen(userId = authViewModel.currentUser?.id ?: "guest")
                }
                composable("profile") {
                    ProfileScreen(
                        user = authViewModel.currentUser,
                        onEditClick = { navController.navigate("edit_profile") }
                    )
                }
                composable("edit_profile") {
                    EditProfileScreen(
                        user = authViewModel.currentUser,
                        onSaveClick = { newName: String ->
                            authViewModel.updateProfile(newName)
                            navController.popBackStack()
                        }
                    )
                }
                composable("security") {
                    SecurityScreen(onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    })
                }
                composable("chat_ia") {
                    MiguelHidalgoChatScreen(
                        onKnowledgeBaseClick = { navController.navigate("ia_knowledge_base") }
                    )
                }
                composable("ia_knowledge_base") {
                    IAKnowledgeBaseScreen()
                }
                composable("more_options") {
                    MoreOptionsScreen(
                        isAdmin = isAdmin,
                        onOptionClick = { route ->
                            if (route == "logout") {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                navController.navigate(route) { launchSingleTop = true }
                            }
                        }
                    )
                }
                
                // Nuevas Pantallas de Flujo
                composable("proximity_alerts") {
                    ProximityAlertsScreen()
                }
                composable("camera_capture") {
                    CameraGeoDropScreen(
                        onCapture = { file -> 
                            // Aquí se podría pasar la ruta del archivo a la siguiente pantalla
                            navController.navigate("anchor_photo") 
                        }
                    )
                }
                composable("anchor_photo") {
                    AnchorPhotoScreen(
                        onAnchorClick = { 
                            notificationViewModel.showNotification("¡Foto anclada con éxito!", NotificationType.SUCCESS)
                            navController.navigate("exploration") {
                                popUpTo("exploration") { inclusive = true }
                            }
                        }
                    )
                }
                composable("active_route") {
                    ActiveRouteScreen(
                        onFinishRoute = {
                            val route = routeViewModel.activeRoute.value
                            val userId = authViewModel.currentUser?.id ?: "guest"
                            if (route != null) {
                                routeViewModel.viewModelScope.launch {
                                    val ok = EcoGuiaRepositoryImpl().saveRouteToCollection(userId, route.id)
                                    android.util.Log.d("MainActivity", "Guardado de ruta en DB completado: $ok para routeId=${route.id}")
                                    routeViewModel.completeActiveRoute()
                                    notificationViewModel.showNotification("🎉 ¡Ruta completada y guardada en Mi Colección!", NotificationType.SUCCESS)
                                    navController.navigate("collection") {
                                        popUpTo("exploration") { inclusive = false }
                                    }
                                }
                            } else {
                                routeViewModel.stopActiveRoute()
                                navController.navigate("collection") {
                                    popUpTo("exploration") { inclusive = false }
                                }
                            }
                        },
                        routeViewModel = routeViewModel
                    )
                }
                composable("search_experience") {
                    SearchExperienceScreen(
                        onSelectRoute = { navController.navigate("active_route") },
                        routeViewModel = routeViewModel
                    )
                }
                composable("permissions") {
                    PermissionsScreen()
                }
                composable("create_route") {
                    CreateRouteScreen(
                        onRouteCreated = { navController.popBackStack() },
                        routeViewModel = routeViewModel
                    )
                }
                composable("offline") {
                    OfflineRouteScreen()
                }
                
                // Device Management Module
                composable("linked_devices") {
                    LinkedDevicesScreen(
                        onTVCampaignClick = { navController.navigate("tv_campaign") },
                        onManageClick = { navController.navigate("manage_devices") },
                        onStatusClick = { navController.navigate("device_status") }
                    )
                }
                composable("manage_devices") {
                    ManageDevicesScreen(
                        onConfirmChanges = { 
                            notificationViewModel.showNotification("Cambios guardados.", NotificationType.SUCCESS)
                            navController.popBackStack() 
                        }
                    )
                }
                composable("device_status") {
                    DeviceStatusScreen(onBack = { navController.popBackStack() })
                }
                
                // TV & Analytics Sub-module
                composable("tv_campaign") {
                    TVCampaignScreen(
                        onAnalyticsClick = { navController.navigate("visitor_analytics") },
                        onManageDevicesClick = { navController.navigate("campaign_devices") }
                    )
                }
                composable("visitor_analytics") {
                    VisitorAnalyticsScreen()
                }
                composable("campaign_devices") {
                    CampaignDevicesScreen(
                        onManageContentClick = { navController.navigate("portal_360") }
                    )
                }
                composable("portal_360") {
                    MuseumPortal360Screen()
                }

                composable("admin") {
                    ControlPanel(activity, repository)
                }

                // Admin & Moderation Module
                composable("site_registration") {
                    SiteRegistrationScreen(
                        viewModel = siteRegistrationViewModel,
                        onNext = { navController.navigate("site_content") }
                    )
                }
                composable("site_content") {
                    SiteContentScreen(
                        viewModel = siteRegistrationViewModel,
                        onNext = { navController.navigate("site_location") }
                    )
                }
                composable("site_location") {
                    SiteLocationScreen(
                        viewModel = siteRegistrationViewModel,
                        onNext = { navController.navigate("site_operation") }
                    )
                }
                composable("site_operation") {
                    SiteOperationScreen(
                        viewModel = siteRegistrationViewModel,
                        onFinish = { 
                            siteRegistrationViewModel.registerSite(
                                onSuccess = {
                                    notificationViewModel.showNotification("Sitio publicado con éxito.", NotificationType.SUCCESS)
                                    navController.navigate("exploration") { popUpTo("exploration") { inclusive = true } }
                                },
                                onError = { msg ->
                                    notificationViewModel.showNotification(msg, NotificationType.ERROR)
                                }
                            )
                        }
                    )
                }
                composable("gallery_addition") {
                    GalleryAdditionScreen(
                        onAddClick = { navController.popBackStack() },
                        onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } }
                    )
                }
                composable("moderation_list") {
                    ModerationListScreen(onResolveClick = { navController.navigate("report_detail") })
                }
                composable("report_detail") {
                    ReportDetailScreen(onResolve = { 
                        notificationViewModel.showNotification("Reporte resuelto.", NotificationType.SUCCESS)
                        navController.popBackStack() 
                    })
                }
                composable("manual_geo_drop") {
                    ManualGeoDropScreen(onAnchorClick = { navController.popBackStack() })
                }
                
                // Mapeo de botones de barra inferior
                composable("radar") { 
                    navController.navigate("camera_capture") { launchSingleTop = true }
                }
                composable("favorites") { 
                    navController.navigate("collection") { launchSingleTop = true }
                }
            }
        }

        if (showBottomMenu) {
            BottomMenuSheet(
                currentRoute = currentRoute,
                isAdmin = isAdmin,
                onDismiss = { showBottomMenu = false },
                onNavigate = { route ->
                    if (route == "logout") {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                }
            )
        }
    }
}

/**
 * Panel de control para simulación y pruebas de administración.
 */
@Composable
fun ControlPanel(activity: ComponentActivity, repository: EcoGuiaRepositoryImpl) {
    val scope = rememberCoroutineScope()
    var lastMessage by remember { mutableStateOf("Esperando interacción...") }
    
    var geoDropTitle by remember { mutableStateOf("") }
    var geoDropDesc by remember { mutableStateOf("") }
    var geoDrops by remember { mutableStateOf(emptyList<RemoteGeoDrop>()) }
    var isRefreshing by remember { mutableStateOf(false) }

    val refreshGeoDrops = {
        scope.launch {
            isRefreshing = true
            try {
                geoDrops = repository.getGeoDrops()
                lastMessage = "Cápsulas actualizadas: ${geoDrops.size}"
            } catch (e: Exception) {
                lastMessage = "Error al cargar: ${e.message}"
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshGeoDrops()
    }

    var isPhoneConnected by remember { mutableStateOf(false) }
    var gpsEnabled by remember { mutableStateOf(true) }
    var cameraReady by remember { mutableStateOf(true) }

    val db = remember { EcoGuiaDatabase.getDatabase(activity) }
    val stealthModeState = db.dao().getConfigFlow("stealth_mode").collectAsState(initial = null)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Eco-Guía: Panel Admin", style = MaterialTheme.typography.headlineMedium)
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Estados en tiempo real:", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Conexión Reloj:", style = MaterialTheme.typography.bodySmall)
                    Text(if (isPhoneConnected) "CONECTADO" else "DESCONECTADO", 
                        color = if (isPhoneConnected) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GPS Preciso:", style = MaterialTheme.typography.bodySmall)
                    Text(if (gpsEnabled) "ACTIVO" else "INACTIVO", 
                        color = if (gpsEnabled) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cámara Móvil:", style = MaterialTheme.typography.bodySmall)
                    Text(if (cameraReady) "LISTA" else "ERROR/OFF", 
                        color = if (cameraReady) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Stealth Mode (DB): ${if (stealthModeState.value?.value == "1") "ON" else "OFF"}",
                    color = if (stealthModeState.value?.value == "1") Color(0xFFC5A059) else Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Log: $lastMessage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            item {
                Text("Nuevo Registro (Neon PostgreSQL)", style = MaterialTheme.typography.titleMedium, color = Color(0xFF00E676))
                OutlinedTextField(
                    value = geoDropTitle,
                    onValueChange = { geoDropTitle = it },
                    label = { Text("Título de la Cápsula") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = geoDropDesc,
                    onValueChange = { geoDropDesc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (geoDropTitle.isNotBlank()) {
                            scope.launch {
                                lastMessage = "Registrando en Neon..."
                                val success = repository.createGeoDrop(
                                    title = geoDropTitle,
                                    description = geoDropDesc,
                                    lat = 21.1561,
                                    lng = -100.9350
                                )
                                if (success) {
                                    lastMessage = "¡Registrado con éxito!"
                                    geoDropTitle = ""
                                    geoDropDesc = ""
                                    refreshGeoDrops()
                                } else {
                                    lastMessage = "Error al registrar."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Guardar en Nube")
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cápsulas en Nube:", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { refreshGeoDrops() }) {
                        Text(if (isRefreshing) "Cargando..." else "Refrescar")
                    }
                }
            }

            items(geoDrops.size) { index ->
                val drop = geoDrops[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(drop.title, style = MaterialTheme.typography.titleSmall)
                        Text(drop.description ?: "", style = MaterialTheme.typography.bodySmall)
                        Text("Fecha: ${drop.createdAt?.take(10) ?: "---"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                Text("Gestión de Conexión", style = MaterialTheme.typography.titleSmall)
                Button(
                    onClick = { 
                        isPhoneConnected = !isPhoneConnected
                        scope.launch { 
                            lastMessage = "Conexión: $isPhoneConnected"
                            sendMessage(activity, "/eco-guia/simulate/link", isPhoneConnected.toString()) 
                        } 
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPhoneConnected) Color.Red else Color(0xFF2E7D32)
                    )
                ) { 
                    Text(if (isPhoneConnected) "Desconectar Reloj" else "Vincular con Reloj") 
                }
            }
        }
    }
}

private suspend fun sendMessage(context: android.content.Context, path: String, payload: String) {
    try {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.forEach { node ->
            Wearable.getMessageClient(context)
                .sendMessage(node.id, path, payload.toByteArray())
                .await()
        }
    } catch (e: Exception) {
        Log.e("EcoGuiaMobile", "Error al enviar mensaje: ${e.message}")
    }
}
