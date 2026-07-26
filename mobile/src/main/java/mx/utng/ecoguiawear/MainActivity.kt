/**
 * Archivo: MainActivity.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Actividad principal y punto de entrada de la aplicación EcoGuía. Configura el
 * Scaffold global, la barra de navegación adaptativa (portrait/landscape), el SnackBar de
 * notificaciones reactivas y delega el grafo de navegación completo a [AppNavHost].
 *
 * Funciones destacadas:
 * - MainActivity: Actividad ComponentActivity con edge-to-edge y tema unificado.
 * - MainAppContainer: Orquestador de estado global (permisos, sesión, orientación).
 * - ControlPanel: Panel interno de pruebas y gestión de cápsulas GeoDrop.
 * - sendMessage: Envío de mensajes al nodo Wear OS conectado.
 */

package mx.utng.ecoguiawear

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import mx.utng.ecoguiawear.ui.navigation.AppNavHost
import mx.utng.ecoguiawear.ui.theme.EcoGuiaMobileTheme
import mx.utng.ecoguiawear.ui.viewmodel.AuthViewModel
import mx.utng.ecoguiawear.ui.viewmodel.NotificationViewModel
import mx.utng.ecoguiawear.ui.viewmodel.NotificationType
import mx.utng.ecoguiawear.ui.viewmodel.SiteRegistrationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.ecoguiawear.ui.components.EcoBottomBar
import mx.utng.ecoguiawear.ui.components.EcoNavigationRail
import mx.utng.ecoguiawear.ui.components.BottomMenuSheet
import androidx.activity.enableEdgeToEdge
import mx.utng.ecoguiawear.ui.viewmodel.RouteViewModel

/**
 * Actividad principal. Establece el tema y delega toda la composición a [MainAppContainer].
 */
class MainActivity : ComponentActivity() {
    private val repository = EcoGuiaRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoGuiaMobileTheme {
                MainAppContainer(this, repository)
            }
        }
    }
}

/**
 * Contenedor principal de la aplicación.
 *
 * Responsabilidades:
 * - Solicitar permisos de ubicación en tiempo de ejecución.
 * - Observar el estado de sesión y la ruta actual de navegación.
 * - Mostrar la barra de navegación adaptativa (bottom bar en portrait, rail en landscape).
 * - Mostrar notificaciones reactivas mediante [SnackbarHost].
 * - Delegar el grafo de rutas a [AppNavHost].
 *
 * @param activity Referencia a la [ComponentActivity] activa.
 * @param repository Repositorio de datos compartido entre módulos.
 */
@SuppressLint("ContextCastToActivity")
@Composable
fun MainAppContainer(activity: ComponentActivity, repository: EcoGuiaRepositoryImpl) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val siteRegistrationViewModel: SiteRegistrationViewModel = viewModel()
    val routeViewModel: RouteViewModel = viewModel()
    val isRouteActive by routeViewModel.activeRoute.run { remember { derivedStateOf { value != null } } }

    // Solicitar permisos de GPS al iniciar
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

    LaunchedEffect(Unit) {
        authViewModel.initNotifications(notificationViewModel)
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"

    val snackbarHostState = remember { SnackbarHostState() }
    val notification by notificationViewModel.currentNotification
    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it.message)
        }
    }

    var isAdmin by remember { mutableStateOf(true) }
    var showBottomMenu by remember { mutableStateOf(false) }

    val authRoutes = listOf("login", "signup", "recovery")
    val showNav = currentRoute !in authRoutes

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Lógica unificada de navegación entre tabs principales
    val onNavigateAction: (String) -> Unit = { route ->
        if (route == "logout") {
            authViewModel.logout()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val isMainTab = route in listOf("exploration", "search_experience", "collection")
            navController.navigate(route) {
                if (isMainTab) {
                    popUpTo("exploration") {
                        inclusive = (route == "exploration")
                        saveState = false
                    }
                } else {
                    popUpTo("exploration") { saveState = true }
                }
                launchSingleTop = true
                restoreState = isMainTab && route != "exploration"
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail en modo landscape
        if (showNav && isLandscape) {
            EcoNavigationRail(
                currentRoute = currentRoute,
                onNavigate = onNavigateAction,
                onOpenSidebar = { showBottomMenu = true },
                isRouteActive = isRouteActive
            )
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showNav && !isLandscape) {
                    EcoBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = onNavigateAction,
                        onOpenSidebar = { showBottomMenu = true },
                        isRouteActive = isRouteActive
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AppNavHost(
                    navController = navController,
                    authViewModel = authViewModel,
                    notificationViewModel = notificationViewModel,
                    siteRegistrationViewModel = siteRegistrationViewModel,
                    routeViewModel = routeViewModel,
                    repository = repository,
                    isAdmin = isAdmin
                )
            }
        }

        // Bottom sheet del menú de opciones extra
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
 * Panel de control interno para simulación y pruebas de administración.
 * Permite crear y listar cápsulas GeoDrop desde la base de datos Neon,
 * y simular la conexión con el reloj Wear OS.
 *
 * @param activity Contexto de la actividad principal.
 * @param repository Repositorio de datos para operaciones de GeoDrop.
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

    LaunchedEffect(Unit) { refreshGeoDrops() }

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
                    Text(
                        if (isPhoneConnected) "CONECTADO" else "DESCONECTADO",
                        color = if (isPhoneConnected) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GPS Preciso:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        if (gpsEnabled) "ACTIVO" else "INACTIVO",
                        color = if (gpsEnabled) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cámara Móvil:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        if (cameraReady) "LISTA" else "ERROR/OFF",
                        color = if (cameraReady) Color(0xFF2E7D32) else Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                        Text(
                            "Fecha: ${drop.createdAt?.take(10) ?: "---"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
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

/**
 * Envía un mensaje a todos los nodos Wear OS conectados vía Wearable Data Layer.
 *
 * @param context Contexto Android para acceder al cliente de Wearable.
 * @param path Ruta del mensaje (e.g. "/eco-guia/simulate/link").
 * @param payload Contenido del mensaje como cadena de texto.
 */
suspend fun sendMessage(context: android.content.Context, path: String, payload: String) {
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
