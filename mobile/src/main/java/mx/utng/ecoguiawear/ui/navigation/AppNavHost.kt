/**
 * Archivo: AppNavHost.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Definición centralizada del grafo de navegación de la aplicación mediante
 * Jetpack Compose Navigation. Agrupa todas las rutas en módulos lógicos comentados:
 * Auth, Principal, Perfil, Rutas, Admin y Dispositivos.
 *
 * Separado de MainActivity.kt para reducir su tamaño y facilitar la adición de
 * nuevas rutas sin tocar la actividad principal.
 *
 * Funciones destacadas:
 * - AppNavHost: NavHost completo con todas las rutas de la aplicación.
 */

package mx.utng.ecoguiawear.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguiawear.ui.screens.*
import mx.utng.ecoguiawear.ui.screens.admin.*
import mx.utng.ecoguiawear.ui.viewmodel.AuthViewModel
import mx.utng.ecoguiawear.ui.viewmodel.NotificationViewModel
import mx.utng.ecoguiawear.ui.viewmodel.NotificationType
import mx.utng.ecoguiawear.ui.viewmodel.RouteViewModel
import mx.utng.ecoguiawear.ui.viewmodel.SiteRegistrationViewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase

/**
 * Grafo de navegación completo de la aplicación EcoGuía.
 *
 * Contiene todas las rutas organizadas por módulo funcional:
 * - **Auth**: login, signup, recovery
 * - **Principal**: exploration, collection, profile, edit_profile, security
 * - **Chat/IA**: chat_ia, ia_knowledge_base
 * - **Rutas**: search_experience, active_route, create_route, offline
 * - **Admin**: admin, site_registration, site_content, site_location, site_operation,
 *              gallery_addition, moderation_list, report_detail, manual_geo_drop
 * - **Dispositivos**: linked_devices, manage_devices, device_status
 * - **TV y Analítica**: tv_campaign, visitor_analytics, campaign_devices, portal_360
 *
 * @param navController Controlador de navegación provisto por [MainAppContainer].
 * @param authViewModel ViewModel de autenticación compartido.
 * @param notificationViewModel ViewModel de notificaciones reactivas.
 * @param siteRegistrationViewModel ViewModel para el flujo de alta de sitios.
 * @param routeViewModel ViewModel para rutas turísticas activas.
 * @param repository Repositorio de datos compartido.
 * @param isAdmin Indica si el usuario actual tiene permisos de administrador.
 * @param modifier Modifier externo para ajustar el contenedor del NavHost.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    siteRegistrationViewModel: SiteRegistrationViewModel,
    routeViewModel: RouteViewModel,
    repository: EcoGuiaRepositoryImpl,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {

        // ── Auth ──────────────────────────────────────────────────────────────
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

        // ── Principal ─────────────────────────────────────────────────────────
        composable("exploration") {
            ExplorationScreen(
                onAdminClick = { navController.navigate("more_options") },
                onOpenRoutes = { navController.navigate("search_experience") },
                onOpenGeoDropWithSite = { siteId -> navController.navigate("camera_capture/$siteId") },
                userId = authViewModel.currentUser?.id.orEmpty()
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
        composable("more_options") {
            MoreOptionsScreen(
                isSuperAdmin = authViewModel.isSuperAdmin,
                isModerator = authViewModel.isModerator,
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


        // ── Chat / IA ─────────────────────────────────────────────────────────
        composable("chat_ia") {
            MiguelHidalgoChatScreen(
                onKnowledgeBaseClick = { navController.navigate("ia_knowledge_base") },
                isSuperAdmin = authViewModel.isSuperAdmin
            )
        }

        composable("ia_knowledge_base") {
            IAKnowledgeBaseScreen(
                userId = authViewModel.currentUser?.id.orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }



        // ── Cámara y GeoDrop ─────────────────────────────────────────────────
        composable("proximity_alerts") {
            ProximityAlertsScreen()
        }
        composable("camera_capture/{siteId}") { backStackEntry ->
            val siteId = backStackEntry.arguments?.getString("siteId").orEmpty()
            val geoDropViewModel: mx.utng.ecoguiawear.ui.viewmodel.GeoDropViewModel = viewModel(backStackEntry)
            if (siteId.isNotBlank()) {
                geoDropViewModel.setTargetSite(siteId, "")
            }
            CameraGeoDropScreen(
                onCapture = { _ -> 
                    navController.navigate("anchor_photo") 
                },
                userId = authViewModel.currentUser?.id.orEmpty(),
                onSavedToCollection = {
                    notificationViewModel.showNotification("¡Cápsula agregada a tu colección!", NotificationType.SUCCESS)
                    navController.navigate("collection") {
                        popUpTo("exploration")
                    }
                },
                geoDropViewModel = geoDropViewModel
            )
        }

        composable("camera_capture") { backStackEntry ->
            val geoDropViewModel: mx.utng.ecoguiawear.ui.viewmodel.GeoDropViewModel = viewModel(backStackEntry)
            CameraGeoDropScreen(
                onCapture = { _ -> navController.navigate("anchor_photo") },
                userId = authViewModel.currentUser?.id.orEmpty(),
                onSavedToCollection = {
                    notificationViewModel.showNotification("¡Cápsula agregada a tu colección!", NotificationType.SUCCESS)
                    navController.navigate("collection") {
                        popUpTo("exploration")
                    }
                },
                geoDropViewModel = geoDropViewModel
            )
        }

        composable("anchor_photo") { backStackEntry ->
            val prevEntry = remember(backStackEntry) { navController.previousBackStackEntry }
            val geoDropViewModel: mx.utng.ecoguiawear.ui.viewmodel.GeoDropViewModel = 
                if (prevEntry != null) viewModel(prevEntry) else viewModel(backStackEntry)
                
            AnchorPhotoScreen(
                onAnchorClick = {
                    notificationViewModel.showNotification("Foto anclada al sitio con éxito", NotificationType.SUCCESS)
                    navController.navigate("exploration") {
                        popUpTo("exploration") { inclusive = true }
                    }
                },
                userId = authViewModel.currentUser?.id.orEmpty(),
                geoDropViewModel = geoDropViewModel
            )
        }






        // ── Rutas turísticas ──────────────────────────────────────────────────
        composable("search_experience") {
            SearchExperienceScreen(
                onSelectRoute = { navController.navigate("active_route") },
                onCreateRoute = { navController.navigate("create_route") },
                isModerator = authViewModel.isModerator,
                routeViewModel = routeViewModel
            )
        }

        composable("active_route") {
            ActiveRouteScreen(
                onFinishRoute = {
                    val route = routeViewModel.activeRoute.value
                    val userId = authViewModel.currentUser?.id.orEmpty()
                    if (route != null && userId.isNotBlank()) {
                        routeViewModel.viewModelScope.launch {
                            val ok = EcoGuiaRepositoryImpl().saveRouteToCollection(userId, route.id)
                            android.util.Log.d("AppNavHost", "Guardado de ruta: $ok para routeId=${route.id}")
                            routeViewModel.completeActiveRoute()
                            notificationViewModel.showNotification(
                                "Ruta completada y guardada en Mi Colección",
                                NotificationType.SUCCESS
                            )
                            navController.navigate("exploration") {
                                popUpTo("exploration") { inclusive = true }
                            }
                        }
                    } else {
                        routeViewModel.stopActiveRoute()
                        navController.navigate("exploration") {
                            popUpTo("exploration") { inclusive = true }
                        }
                    }
                },


                routeViewModel = routeViewModel
            )
        }
        composable("create_route") {
            CreateRouteScreen(
                onRouteCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                routeViewModel = routeViewModel
            )
        }

        composable("offline") {
            OfflineRouteScreen()
        }
        composable("permissions") {
            PermissionsScreen()
        }

        // ── Admin y Moderación ────────────────────────────────────────────────
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
                        onSuccess = { createdSiteId ->
                            notificationViewModel.showNotification("Sitio publicado con éxito.", NotificationType.SUCCESS)
                            if (createdSiteId != "SUCCESS" && createdSiteId.isNotBlank()) {
                                navController.navigate("camera_capture/$createdSiteId") {
                                    popUpTo("exploration")
                                }
                            } else {
                                navController.navigate("exploration") {
                                    popUpTo("exploration") { inclusive = true }
                                }
                            }
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
            val parentEntry = remember(it) { navController.getBackStackEntry("moderation_list") }
            val moderationViewModel: mx.utng.ecoguiawear.ui.viewmodel.ModerationViewModel = viewModel(parentEntry)
            ModerationListScreen(
                onResolveClick = { _ -> navController.navigate("report_detail") },
                moderationViewModel = moderationViewModel
            )
        }
        composable("report_detail") {
            val parentEntry = remember(it) {
                try {
                    navController.getBackStackEntry("moderation_list")
                } catch (e: Exception) {
                    it
                }
            }
            val moderationViewModel: mx.utng.ecoguiawear.ui.viewmodel.ModerationViewModel = viewModel(parentEntry)
            ReportDetailScreen(
                onResolve = {
                    notificationViewModel.showNotification("🎉 Decisión de moderación guardada en tiempo real", NotificationType.SUCCESS)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                moderationViewModel = moderationViewModel
            )
        }

        composable("user_management") {
            UserManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("manual_geo_drop") {
            ManualGeoDropScreen(onAnchorClick = { navController.popBackStack() })
        }


        // ── Dispositivos ──────────────────────────────────────────────────────
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

        // ── TV y Analítica ────────────────────────────────────────────────────
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

        // ── Alias de barra de navegación ──────────────────────────────────────
        composable("radar") {
            navController.navigate("camera_capture") { launchSingleTop = true }
        }
        composable("favorites") {
            navController.navigate("collection") { launchSingleTop = true }
        }
    }
}
