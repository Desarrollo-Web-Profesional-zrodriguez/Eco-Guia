# Plan de Documentación KDoc — Módulo `mobile`

> **Instrucciones generales:**
> - Usar KDoc estándar de Kotlin: `/** */` para clases y funciones públicas.
> - Cada archivo debe tener un bloque KDoc de cabecera antes del `package`.
> - Etiquetas obligatorias: `@author`, `@since` (fecha del archivo si existe, si no usar `2026-08-05`).
> - Etiquetas de función: `@param` por cada parámetro, `@return` si retorna algo distinto de `Unit`.
> - Autores del proyecto: `Zahir Andrés Rodríguez Mora` y `Cesar Enrique Garay García`.
> - Si el archivo ya tiene un bloque KDoc de cabecera, **actualizarlo** con el formato estándar sin borrar el contenido existente.
> - Las funciones privadas (`private fun`) solo necesitan documentación si su lógica no es evidente.

---

## 📄 `MainActivity.kt`
**Fecha existente en el archivo:** `2026-07-26`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción general de la actividad; mencionar que es el punto de entrada, que configura el tema, la sesión y el Scaffold global |
| `MainActivity` | class | Qué hace esta Activity; que hereda de `ComponentActivity`; que configura edge-to-edge |
| `onCreate` | fun | Describe la secuencia de inicialización: Room DB, repositorio shared, setContent |
| `MainAppContainer` | @Composable fun | Qué orquesta: estado de sesión, permisos, orientación, Scaffold global |
| `sendMessage` | fun | Para qué sirve; `@param nodeId` destino del mensaje; `@param message` payload enviado |

---

## 📄 `data/BootReceiver.kt`
**Fecha:** `2026-08-05` (usar si no hay fecha en el archivo)

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: receptor de arranque del SO que reinicia el ProximityService |
| `BootReceiver` | class | Qué hace; cuándo es invocado por el sistema operativo |
| `onReceive` | fun | Qué verifica en el intent; qué servicio lanza; `@param context`, `@param intent` |

---

## 📄 `data/ProximityNotificationHelper.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: helper que construye y muestra notificaciones de proximidad a sitios históricos |
| `PROXIMITY_CHANNEL_ID` | const | Para qué se usa este ID |
| `createNotificationChannel` | fun | Cuándo llamar; qué canal crea; `@param context` |
| `buildProximityNotification` | fun | Qué construye; `@param siteName`, `@param distanceMeters`; `@return Notification` |
| `showNotification` | fun | Qué hace; `@param context`, `@param notification` |

---

## 📄 `data/ProximityService.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: Foreground Service que monitorea continuamente la ubicación GPS y dispara alertas de proximidad |
| `ProximityService` | class | Qué tipo de servicio es; por qué necesita `foregroundServiceType=location` |
| `onCreate` | fun | Qué inicializa; qué notificación persistente muestra |
| `onStartCommand` | fun | Qué loop inicia; cómo evalúa la distancia a los sitios |
| `onDestroy` | fun | Qué recursos libera |

---

## 📄 `data/remote/EmailService.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: cliente REST para la API de Brevo v3 que envía correos OTP |
| `EmailService` | class | Qué hace; de dónde lee la API key |
| `sendOtpEmail` | suspend fun | Qué envía; `@param to` destinatario; `@param otp` código de 6 dígitos; `@return Boolean` éxito o fallo |

---

## 📄 `data/remote/FirebaseStorageRepository.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: repositorio para subida y descarga de imágenes en Firebase Storage |
| `FirebaseStorageRepository` | class | Qué gestiona; dónde guarda las imágenes en el storage |
| `uploadGeoDropImage` | suspend fun | `@param imageBytes`, `@param fileName`; `@return String` URL pública |
| `uploadSiteImage` | suspend fun | `@param imageBytes`, `@param siteId`; `@return String` URL pública |
| `deleteImage` | suspend fun | `@param url` URL de Firebase; para qué se usa (al rechazar un GeoDrop) |

---

## 📄 `data/wear/MobileWearListenerService.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: WearableListenerService que recibe mensajes del reloj Wear OS |
| `MobileWearListenerService` | class | Qué hereda; qué mensajes escucha |
| `onMessageReceived` | fun | Qué paths maneja; qué acción toma según el path; `@param messageEvent` |

---

## 📄 `data/wear/WearMessageClient.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: cliente para enviar mensajes y datos al reloj Wear OS conectado |
| `WearMessageClient` | class | Qué hace; qué API de Wearable usa |
| `getConnectedNodes` | suspend fun | `@param context`; `@return List<Node>` nodos conectados |
| `sendSiteToWatch` | suspend fun | `@param context`, `@param site`; qué path usa; `@return Unit` |
| `sendRouteToWatch` | suspend fun | `@param context`, `@param route`; qué path usa; `@return Unit` |

---

## 📄 `worker/SyncOfflineWorker.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: CoroutineWorker de WorkManager para sincronizar GeoDrops pendientes con Neon DB al recuperar conectividad |
| `SyncOfflineWorker` | class | Qué hereda; cuándo es encolado |
| `doWork` | suspend fun | Qué hace; cuándo retorna `success`, `retry` o `failure`; `@return Result` |
| `SYNC_WORKER_TAG` | const | Para qué se usa este tag al encolar el worker |

---

## 📄 `ui/theme/Color.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: paleta de colores oficial de Eco-Guía |
| Cada constante de color | val | Una línea describiendo su uso (ej. `/** Color primario verde Eco-Guía, usado en botones y acentos */`) |

---

## 📄 `ui/theme/Theme.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: configuración del MaterialTheme de la app |
| `EcoGuiaTheme` | @Composable fun | Qué envuelve; cómo detecta modo oscuro/claro; `@param content` bloque de contenido |

---

## 📄 `ui/navigation/AppNavHost.kt`
**Fecha:** `2026-07-26` (verificar en el archivo)

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: grafo de navegación completo de la aplicación móvil |
| `Routes` | object | Para qué sirve; que contiene todas las rutas como constantes |
| Cada ruta en `Routes` | const String | Una línea describiendo a qué pantalla corresponde |
| `AppNavHost` | @Composable fun | Qué define; `@param navController`, `@param repository` |

---

## 📄 `ui/components/CommonComponents.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: biblioteca de componentes UI reutilizables de Eco-Guía |
| `EcoButton` | @Composable fun | Para qué se usa; `@param text`, `@param onClick`, `@param enabled`, `@param isLoading` |
| `EcoTextField` | @Composable fun | Para qué se usa; `@param value`, `@param onValueChange`, `@param label`, `@param isError`, `@param errorMessage` |
| `EcoCard` | @Composable fun | Para qué se usa; `@param content` |
| `EcoBadge` | @Composable fun | Para qué se usa; `@param level` nivel de explorador |
| `EcoLoadingScreen` | @Composable fun | Cuándo se muestra |
| `EcoErrorMessage` | @Composable fun | `@param message`, `@param onRetry` callback opcional |

---

## 📄 `ui/components/BottomMenu.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: barra de navegación inferior para usuarios regulares |
| `BottomMenuItems` | object/sealed class | Qué ítems define; para qué se usa |
| `BottomMenu` | @Composable fun | `@param navController`, `@param currentRoute` ruta activa |

---

## 📄 `ui/components/AdminNavigation.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: componente de navegación para roles administrador y moderador |
| `AdminNavigation` | @Composable fun | `@param navController`, `@param currentRoute`, `@param userRole` |

---

## 📄 `ui/components/EcoNavigation.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: barra de navegación contextual con título y botón de retroceso |
| `EcoNavigation` | @Composable fun | `@param title`, `@param navController`, `@param actions` acciones opcionales en la barra |

---

## 📄 `ui/components/GeoDropSavingDialog.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: diálogo modal para confirmar y guardar un GeoDrop capturado |
| `GeoDropSavingDialog` | @Composable fun | `@param photoBytes` miniatura de la foto; `@param location` coordenadas; `@param onSave`, `@param onDismiss` |

---

## 📄 `ui/feature/exploration/MapMarkerUtils.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: funciones utilitarias de cálculo geoespacial para el mapa de exploración |
| `calculateDistance` | fun | Fórmula haversine; `@param lat1`, `@param lon1`, `@param lat2`, `@param lon2`; `@return Float` metros |
| `formatDistance` | fun | `@param meters`; `@return String` formateado ("Xm" o "X.Xkm") |
| `getBitmapDescriptor` | fun | `@param context`, `@param vectorResId`; `@return BitmapDescriptor` |
| `getMarkerColor` | fun | `@param category`; `@return Color` del marcador según categoría |

---

## 📄 `ui/feature/exploration/ExplorationMapContent.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: composable del mapa principal de exploración con marcadores de sitios históricos |
| `ExplorationMapContent` | @Composable fun | `@param sites` lista de sitios; `@param userLocation` ubicación actual; `@param onSiteSelected` callback al pulsar marcador |

---

## 📄 `ui/feature/exploration/ExplorationSiteList.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: lista filtrable de sitios históricos por categoría y distancia |
| `ExplorationSiteList` | @Composable fun | `@param sites`, `@param userLocation`, `@param onSiteSelected` |
| `SiteListItem` | @Composable fun (interno) | `@param site`, `@param distance`, `@param onSave` |

---

## 📄 `ui/feature/exploration/SiteDetailSheet.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: ModalBottomSheet de detalle completo de un sitio histórico |
| `SiteDetailSheet` | @Composable fun | `@param site` sitio seleccionado; `@param userLocation`; `@param onSaveToCollection`; `@param onSendToWatch`; `@param onDismiss` |

---

## 📄 `ui/feature/collection/CollectionItemCard.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: tarjeta expandible para ítems guardados en Mi Colección |
| `CollectionItemCard` | @Composable fun | `@param item` ítem guardado; `@param onRemove`; `@param onViewOnMap` |

---

## 📄 ViewModels (`ui/viewmodel/*.kt`)

Para **cada** ViewModel aplicar el siguiente patrón:

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción funcional del ViewModel; qué dominio gestiona |
| La clase ViewModel | class | Constructor, qué repositorio recibe, qué estado expone |
| `UiState` data class | data class | Cada campo: para qué se usa |
| Cada función pública | fun/suspend fun | Qué acción desencadena; `@param` por cada parámetro relevante; `@return` si aplica |

| ViewModel | Fecha a usar |
|-----------|-------------|
| `AuthViewModel.kt` | `2026-07-26` (verificar en archivo) |
| `ChatViewModel.kt` | `2026-08-05` |
| `CollectionViewModel.kt` | `2026-08-05` |
| `GeoDropViewModel.kt` | `2026-08-05` |
| `LocationViewModel.kt` | `2026-08-05` |
| `ModerationViewModel.kt` | `2026-08-05` |
| `NotificationViewModel.kt` | `2026-08-05` |
| `RouteViewModel.kt` | `2026-08-05` |
| `SiteRegistrationViewModel.kt` | `2026-08-05` |
| `UserManagementViewModel.kt` | `2026-08-05` |

---

## 📄 Pantallas de usuario (`ui/screens/*.kt`) — 25 archivos

Para **cada** pantalla aplicar el siguiente patrón mínimo:

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Nombre de la pantalla y descripción de su función dentro del flujo de usuario |
| Función principal `@Composable` | fun | `@param navController`; `@param viewModel` (especificar cuál); propósito de la pantalla |
| Sub-composables internos si son complejos | fun | Breve descripción de su rol dentro de la pantalla |

---

## 📄 Pantallas de administrador (`ui/screens/admin/*.kt`) — 19 archivos

Mismo patrón que las pantallas de usuario. Agregar además:

| Elemento adicional | Indicación |
|-------------------|-----------|
| Restricciones de rol | En el KDoc de la pantalla mencionar cuál rol puede acceder: `@note Solo accesible para roles admin o moderador` |

---

## ✅ Checklist de archivos a documentar

- [ ] `MainActivity.kt`
- [ ] `data/BootReceiver.kt`
- [ ] `data/ProximityNotificationHelper.kt`
- [ ] `data/ProximityService.kt`
- [ ] `data/remote/EmailService.kt`
- [ ] `data/remote/FirebaseStorageRepository.kt`
- [ ] `data/wear/MobileWearListenerService.kt`
- [ ] `data/wear/WearMessageClient.kt`
- [ ] `worker/SyncOfflineWorker.kt`
- [ ] `ui/theme/Color.kt`
- [ ] `ui/theme/Theme.kt`
- [ ] `ui/navigation/AppNavHost.kt`
- [ ] `ui/components/AdminNavigation.kt`
- [ ] `ui/components/BottomMenu.kt`
- [ ] `ui/components/CommonComponents.kt`
- [ ] `ui/components/EcoNavigation.kt`
- [ ] `ui/components/GeoDropSavingDialog.kt`
- [ ] `ui/feature/exploration/ExplorationMapContent.kt`
- [ ] `ui/feature/exploration/ExplorationSiteList.kt`
- [ ] `ui/feature/exploration/MapMarkerUtils.kt`
- [ ] `ui/feature/exploration/SiteDetailSheet.kt`
- [ ] `ui/feature/collection/CollectionItemCard.kt`
- [ ] `ui/viewmodel/AuthViewModel.kt`
- [ ] `ui/viewmodel/ChatViewModel.kt`
- [ ] `ui/viewmodel/CollectionViewModel.kt`
- [ ] `ui/viewmodel/GeoDropViewModel.kt`
- [ ] `ui/viewmodel/LocationViewModel.kt`
- [ ] `ui/viewmodel/ModerationViewModel.kt`
- [ ] `ui/viewmodel/NotificationViewModel.kt`
- [ ] `ui/viewmodel/RouteViewModel.kt`
- [ ] `ui/viewmodel/SiteRegistrationViewModel.kt`
- [ ] `ui/viewmodel/UserManagementViewModel.kt`
- [ ] `ui/screens/SplashScreen.kt`
- [ ] `ui/screens/LoginScreen.kt`
- [ ] `ui/screens/SignUpScreen.kt`
- [ ] `ui/screens/RecoveryScreen.kt`
- [ ] `ui/screens/PermissionsScreen.kt`
- [ ] `ui/screens/ExplorationScreen.kt`
- [ ] `ui/screens/SearchExperienceScreen.kt`
- [ ] `ui/screens/MyCollectionScreen.kt`
- [ ] `ui/screens/ProfileScreen.kt`
- [ ] `ui/screens/EditProfileScreen.kt`
- [ ] `ui/screens/SecurityScreen.kt`
- [ ] `ui/screens/MoreOptionsScreen.kt`
- [ ] `ui/screens/ActiveRouteScreen.kt`
- [ ] `ui/screens/OfflineRouteScreen.kt`
- [ ] `ui/screens/AnchorPhotoScreen.kt`
- [ ] `ui/screens/CameraGeoDropScreen.kt`
- [ ] `ui/screens/CreateRouteScreen.kt`
- [ ] `ui/screens/DeviceStatusScreen.kt`
- [ ] `ui/screens/LinkedDevicesScreen.kt`
- [ ] `ui/screens/ManageDevicesScreen.kt`
- [ ] `ui/screens/IAKnowledgeBaseScreen.kt`
- [ ] `ui/screens/MiguelHidalgoChatScreen.kt`
- [ ] `ui/screens/NoInternetScreen.kt`
- [ ] `ui/screens/ProximityAlertsScreen.kt`
- [ ] `ui/screens/TvCameraScreen.kt`
- [ ] `ui/screens/admin/AdminSummaryScreen.kt`
- [ ] `ui/screens/admin/CampaignDevicesScreen.kt`
- [ ] `ui/screens/admin/CapsuleGalleryScreen.kt`
- [ ] `ui/screens/admin/GalleryAdditionScreen.kt`
- [ ] `ui/screens/admin/ManualGeoDropScreen.kt`
- [ ] `ui/screens/admin/ModerateCommunityScreen.kt`
- [ ] `ui/screens/admin/ModerationListScreen.kt`
- [ ] `ui/screens/admin/MuseumPortal360Screen.kt`
- [ ] `ui/screens/admin/ReportDecisionScreen.kt`
- [ ] `ui/screens/admin/ReportDetailScreen.kt`
- [ ] `ui/screens/admin/ReviewDetailScreen.kt`
- [ ] `ui/screens/admin/SecurityReportsScreen.kt`
- [ ] `ui/screens/admin/SiteContentScreen.kt`
- [ ] `ui/screens/admin/SiteLocationScreen.kt`
- [ ] `ui/screens/admin/SiteOperationScreen.kt`
- [ ] `ui/screens/admin/SiteRegistrationScreen.kt`
- [ ] `ui/screens/admin/TVCampaignScreen.kt`
- [ ] `ui/screens/admin/UserManagementScreen.kt`
- [ ] `ui/screens/admin/VisitorAnalyticsScreen.kt`
