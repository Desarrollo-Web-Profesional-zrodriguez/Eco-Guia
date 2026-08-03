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
import androidx.navigation.NavGraph.Companion.findStartDestination
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
        mx.utng.ecoguia.shared.config.EcoGuiaConfig.appContext = applicationContext
        mx.utng.ecoguiawear.data.ProximityNotificationHelper.createChannels(applicationContext)
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
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val siteRegistrationViewModel: SiteRegistrationViewModel = viewModel()
    val routeViewModel: RouteViewModel = viewModel()
    val isRouteActive by routeViewModel.activeRoute.run { remember { derivedStateOf { value != null } } }

    // Procesar intent enviado desde el reloj para abrir la pantalla GeoDrop del sitio
    val intent = activity.intent
    LaunchedEffect(intent) {
        if (intent?.action == "mx.utng.ecoguiawear.OPEN_GEODROP") {
            val siteId = intent.getStringExtra("siteId").orEmpty()
            navController.navigate("camera_capture?siteId=$siteId") {
                launchSingleTop = true
            }
        }
    }

    // Cargar sesión persistente al abrir la aplicación
    LaunchedEffect(Unit) {
        authViewModel.initSessionPersistence(context)
    }


    // Solicitar permisos de GPS, Cámara y Notificaciones al iniciar
    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!locationGranted) {
            notificationViewModel.showNotification(
                "La app necesita GPS para ofrecer recomendaciones por proximidad.",
                NotificationType.INFO
            )
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.initNotifications(notificationViewModel)
        permissionLauncher.launch(permissionsToRequest)
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

    val isAdmin = authViewModel.isAdmin
    var showBottomMenu by remember { mutableStateOf(false) }


    val authRoutes = listOf("login", "signup", "recovery")
    val showNav = currentRoute !in authRoutes

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // BroadcastReceiver para alertas In-App de proximidad
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == "mx.utng.ecoguiawear.PROXIMITY_ALERT") {
                    val siteName = intent.getStringExtra("siteName") ?: "un sitio histórico"
                    val distance = intent.getIntExtra("distance", 0)
                    notificationViewModel.showNotification(
                        "¡Estás a ${distance}m de $siteName!",
                        NotificationType.SUCCESS
                    )
                }
            }
        }
        val filter = android.content.IntentFilter("mx.utng.ecoguiawear.PROXIMITY_ALERT")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Función utilitaria para verificar conectividad a Internet
    fun isOnline(context: android.content.Context): Boolean {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Comprobación de estado de red y sesión al iniciar con pantalla Splash (Spinner)
    LaunchedEffect(authViewModel.currentUser, routeViewModel.activeRoute.value) {
        kotlinx.coroutines.delay(1200) // Tiempo breve para permitir al usuario ver el spinner de verificación
        val online = isOnline(context)
        val hasActiveRoute = routeViewModel.activeRoute.value != null
        val isLoggedIn = authViewModel.currentUser != null

        if (!online) {
            if (hasActiveRoute) {
                navController.navigate("active_route") {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                navController.navigate("no_internet") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else if (!isLoggedIn) {
            val currentDestination = navController.currentDestination?.route
            if (currentDestination != "login" && currentDestination != "signup" && currentDestination != "recovery") {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            val currentDestination = navController.currentDestination?.route
            if (currentDestination == "splash") {
                navController.navigate("exploration") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // Lógica unificada de navegación entre tabs principales
    val onNavigateAction: (String) -> Unit = { route ->
        if (route == "logout") {
            authViewModel.logout()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val hasActiveRoute = routeViewModel.activeRoute.value != null
            val online = isOnline(context)
            if (!online) {
                if (hasActiveRoute) {
                    if (route == "active_route" || route == "offline") {
                        navController.navigate(route) { launchSingleTop = true }
                    } else {
                        notificationViewModel.showNotification(
                            "Estás en modo offline con una ruta activa. Solo puedes ver el progreso de la ruta.",
                            NotificationType.ERROR
                        )
                    }
                } else {
                    navController.navigate("no_internet") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                val isMainTab = route in listOf("exploration", "search_experience", "collection")
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                }
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = mx.utng.ecoguiawear.ui.theme.EcoGuiaColors.Jade,
                        contentColor = Color.White,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
                }
            },
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
                isSuperAdmin = authViewModel.isSuperAdmin,
                isModerator = authViewModel.isModerator,
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
