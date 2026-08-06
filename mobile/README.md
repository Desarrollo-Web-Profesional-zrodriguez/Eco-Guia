<div align="center">
  <h1>📱 Módulo <code>mobile</code> — Eco-Guía Android Phone</h1>
  <p><strong>Guía de Desarrollo — Archivos e Indicaciones por Paso</strong></p>
  <div>
    <img src="https://img.shields.io/badge/Kotlin-2.1.0-blue?style=for-the-badge&logo=kotlin">
    <img src="https://img.shields.io/badge/Jetpack_Compose-BOM_2025-4285F4?style=for-the-badge&logo=jetpackcompose">
    <img src="https://img.shields.io/badge/minSdk-24-green?style=for-the-badge&logo=android">
    <img src="https://img.shields.io/badge/compileSdk-37-green?style=for-the-badge&logo=android">
    <img src="https://img.shields.io/badge/Android_Studio-Meerkat_2025.1.1-3DDC84?style=for-the-badge&logo=androidstudio">
  </div>
</div>

<br>

> ⚠️ **Dependencia obligatoria:** El módulo `:shared` debe estar compilado y sincronizado antes de trabajar en cualquier archivo de este módulo. Toda la lógica de acceso a datos, modelos de dominio y clientes remotos (Neon, Groq, MQTT) reside en `shared`. Consulta el [README raíz](../README.md) para la configuración del entorno global.

<br>

---

## 📋 Versiones y Configuración del Módulo

| Parámetro | Valor |
|-----------|-------|
| `namespace` | `mx.utng.ecoguiawear` |
| `applicationId` | `mx.utng.ecoguiawear` |
| `compileSdk` | 37 |
| `minSdk` | 24 |
| `targetSdk` | 37 |
| `versionName` | 1.0.0 |
| `JVM Target` | 17 |
| `buildFeatures` | `compose = true`, `buildConfig = true` |

<br>

---

## 🗂️ Estructura Completa del Módulo

```
mobile/
└── src/main/java/mx/utng/ecoguiawear/
    ├── MainActivity.kt
    ├── data/
    │   ├── BootReceiver.kt
    │   ├── ProximityNotificationHelper.kt
    │   ├── ProximityService.kt
    │   ├── remote/
    │   │   ├── EmailService.kt
    │   │   └── FirebaseStorageRepository.kt
    │   └── wear/
    │       ├── MobileWearListenerService.kt
    │       └── WearMessageClient.kt
    ├── worker/
    │   └── SyncOfflineWorker.kt
    └── ui/
        ├── theme/
        │   ├── Color.kt
        │   └── Theme.kt
        ├── navigation/
        │   └── AppNavHost.kt
        ├── components/
        │   ├── AdminNavigation.kt
        │   ├── BottomMenu.kt
        │   ├── CommonComponents.kt
        │   ├── EcoNavigation.kt
        │   └── GeoDropSavingDialog.kt
        ├── feature/
        │   ├── exploration/
        │   │   ├── ExplorationMapContent.kt
        │   │   ├── ExplorationSiteList.kt
        │   │   ├── MapMarkerUtils.kt
        │   │   └── SiteDetailSheet.kt
        │   └── collection/
        │       └── CollectionItemCard.kt
        ├── screens/
        │   ├── SplashScreen.kt
        │   ├── LoginScreen.kt
        │   ├── SignUpScreen.kt
        │   ├── RecoveryScreen.kt
        │   ├── PermissionsScreen.kt
        │   ├── ExplorationScreen.kt
        │   ├── SearchExperienceScreen.kt
        │   ├── MyCollectionScreen.kt
        │   ├── ProfileScreen.kt
        │   ├── EditProfileScreen.kt
        │   ├── SecurityScreen.kt
        │   ├── MoreOptionsScreen.kt
        │   ├── ActiveRouteScreen.kt
        │   ├── OfflineRouteScreen.kt
        │   ├── AnchorPhotoScreen.kt
        │   ├── CameraGeoDropScreen.kt
        │   ├── CreateRouteScreen.kt
        │   ├── DeviceStatusScreen.kt
        │   ├── LinkedDevicesScreen.kt
        │   ├── ManageDevicesScreen.kt
        │   ├── IAKnowledgeBaseScreen.kt
        │   ├── MiguelHidalgoChatScreen.kt
        │   ├── NoInternetScreen.kt
        │   ├── ProximityAlertsScreen.kt
        │   ├── TvCameraScreen.kt
        │   └── admin/
        │       ├── AdminSummaryScreen.kt
        │       ├── UserManagementScreen.kt
        │       ├── ModerateCommunityScreen.kt
        │       ├── ModerationListScreen.kt
        │       ├── ReportDetailScreen.kt
        │       ├── ReportDecisionScreen.kt
        │       ├── ReviewDetailScreen.kt
        │       ├── SecurityReportsScreen.kt
        │       ├── SiteRegistrationScreen.kt
        │       ├── SiteLocationScreen.kt
        │       ├── SiteContentScreen.kt
        │       ├── SiteOperationScreen.kt
        │       ├── CampaignDevicesScreen.kt
        │       ├── TVCampaignScreen.kt
        │       ├── MuseumPortal360Screen.kt
        │       ├── CapsuleGalleryScreen.kt
        │       ├── GalleryAdditionScreen.kt
        │       ├── ManualGeoDropScreen.kt
        │       └── VisitorAnalyticsScreen.kt
        └── viewmodel/
            ├── AuthViewModel.kt
            ├── ChatViewModel.kt
            ├── CollectionViewModel.kt
            ├── GeoDropViewModel.kt
            ├── LocationViewModel.kt
            ├── ModerationViewModel.kt
            ├── NotificationViewModel.kt
            ├── RouteViewModel.kt
            ├── SiteRegistrationViewModel.kt
            └── UserManagementViewModel.kt
```

<br>

---

## 📄 Descripción Línea a Línea por Archivo

---

### `mobile/build.gradle.kts`

- **Línea 1-7:** Bloque de lectura de `local.properties` para extraer la variable `BREVO_API_KEY` de forma segura; no codificar la clave directamente en el código fuente.
- **Línea 8:** Declaración de la variable `brevoKey` con la API key leída; si no existe en `local.properties`, retorna cadena vacía.
- **Línea 10-14:** Bloque `plugins`: aplicar `android.application`, `kotlin.compose` y `google.services` (Firebase).
- **Línea 16-55:** Bloque `android`: declarar `namespace`, `compileSdk = 37`, `defaultConfig` con `applicationId`, `minSdk = 24`, `targetSdk = 37`, `versionCode` y `versionName`.
- **Línea 30:** `buildConfigField` para inyectar `BREVO_API_KEY` al `BuildConfig`; necesario para que `EmailService` la lea en tiempo de ejecución.
- **Línea 42-50:** `compileOptions` y `kotlin.compilerOptions` para JVM 17.
- **Línea 51-54:** `buildFeatures`: activar `compose = true` y `buildConfig = true`.
- **Línea 63:** Inicio del bloque `dependencies`; primera línea debe ser `implementation(project(":shared"))`.
- **Líneas siguientes:** Declarar dependencias de Compose BOM, Material 3, Navigation Compose, Google Maps, CameraX, Play Services Wearable, Room, WorkManager, Ktor Client, Firebase BOM + Storage, Coil Compose y Google Code Scanner.

---

### `mobile/src/main/AndroidManifest.xml`

- **Permisos requeridos:** `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `CAMERA`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`.
- **`MainActivity`:** Declarar como launcher con intent-filter `MAIN` + `LAUNCHER`; incluir la meta-data de Google Maps API Key.
- **`ProximityService`:** Declarar como `<service>` con `foregroundServiceType="location"`.
- **`BootReceiver`:** Declarar como `<receiver>` con `RECEIVE_BOOT_COMPLETED` y `enabled = true`, `exported = true`.
- **`MobileWearListenerService`:** Declarar como `<service>` heredando de `WearableListenerService`; agregar intent-filter con la acción `com.google.android.gms.wearable.MESSAGE_RECEIVED`.
- **`SyncOfflineWorker`:** No requiere declaración en manifiesto; WorkManager lo registra automáticamente.

---

### `MainActivity.kt`

- **Bloque 1 — Cabecera KDoc:** Descripción del archivo, autores (`Zahir Andrés Rodríguez Mora`, `Cesar Enrique Garay García`), fecha `2026-07-26`.
- **Bloque 2 — `MainActivity : ComponentActivity`:** Punto de entrada; llama a `enableEdgeToEdge()` para diseño de pantalla completa; en `onCreate` instancia `EcoGuiaDatabase` y `EcoGuiaRepositoryImpl` del módulo `shared`; luego llama a `setContent` con `EcoGuiaTheme` y el composable `MainAppContainer`.
- **Bloque 3 — `MainAppContainer`:** Composable orquestador de estado global; observa el estado de sesión del usuario desde `AuthViewModel`; maneja permisos de ubicación y cámara; detecta la orientación (portrait/landscape) para adaptar el `Scaffold`; incluye el `SnackbarHost` para notificaciones reactivas.
- **Bloque 4 — `Scaffold` principal:** Configura `topBar` (barra de navegación superior o vacía según pantalla), `bottomBar` (menú inferior), `snackbarHost` y el contenido central delegado a `AppNavHost`.
- **Bloque 5 — `sendMessage`:** Función que envía un mensaje al nodo Wear OS usando `Wearable.getMessageClient`; recibe el `nodeId` y el `payload`; se usa para pruebas internas del panel de control.

---

### `data/BootReceiver.kt`

- **Bloque 1 — KDoc:** Descripción: reinicia el `ProximityService` al arrancar el dispositivo; autores y fecha.
- **Bloque 2 — `BootReceiver : BroadcastReceiver`:** Hereda de `BroadcastReceiver`; sobreescribe `onReceive`; verifica que la acción del intent sea `ACTION_BOOT_COMPLETED`; si es así, lanza un `Intent` apuntando a `ProximityService` y lo inicia con `startForegroundService`.

---

### `data/ProximityNotificationHelper.kt`

- **Bloque 1 — KDoc:** Descripción: construye y gestiona las notificaciones de proximidad; autores y fecha.
- **Bloque 2 — Constantes:** Definir el ID del canal de notificación (`PROXIMITY_CHANNEL_ID`) y el ID numérico de notificación.
- **Bloque 3 — `createNotificationChannel`:** Crea el `NotificationChannel` para Android 8+; nombre del canal: "Alertas de Proximidad Eco-Guía"; importancia `IMPORTANCE_HIGH`; llamar desde el `Service` antes de mostrar la primera notificación.
- **Bloque 4 — `buildProximityNotification`:** Recibe el nombre del sitio histórico y la distancia en metros; retorna una `Notification` de `NotificationCompat.Builder` con ícono, título ("¡Sitio cercano!"), descripción y pendingIntent al abrir la app.
- **Bloque 5 — `showNotification`:** Recibe el contexto y la notificación construida; la muestra usando `NotificationManagerCompat`.

---

### `data/ProximityService.kt`

- **Bloque 1 — KDoc:** Descripción: `ForegroundService` de monitoreo GPS continuo; autores y fecha.
- **Bloque 2 — Campos:** `FusedLocationProviderClient`, coroutine scope del servicio, lista de sitios históricos cargada desde el repositorio, umbral de distancia en metros (configurable).
- **Bloque 3 — `onCreate`:** Crear canal de notificación via `ProximityNotificationHelper`; inicializar el `FusedLocationProviderClient`; llamar a `startForeground` con una notificación persistente ("Eco-Guía monitoreando ubicación...").
- **Bloque 4 — `onStartCommand`:** Iniciar el loop de actualización de ubicación; cargar los sitios desde `EcoGuiaRepositoryImpl`; para cada actualización GPS calcular la distancia a cada sitio; si algún sitio está dentro del umbral, llamar a `ProximityNotificationHelper.showNotification`.
- **Bloque 5 — `onDestroy`:** Cancelar el scope de corrutinas; detener las actualizaciones de ubicación.

---

### `data/remote/EmailService.kt`

- **Bloque 1 — KDoc:** Descripción: cliente REST para Brevo API v3; autores y fecha.
- **Bloque 2 — Configuración Ktor:** Crear un `HttpClient` con el plugin de content negotiation y serialización JSON; la `BASE_URL` es `https://api.brevo.com/v3`.
- **Bloque 3 — `sendOtpEmail(to, otp)`:** Función suspendida; construye el cuerpo del request con el destinatario, asunto ("Tu código OTP - Eco-Guía"), contenido HTML con el código OTP; agrega el header `api-key` con `BuildConfig.BREVO_API_KEY`; realiza el POST a `/smtp/email`; retorna `true` si el código HTTP es 2xx.
- **Bloque 4 — Plantilla HTML del correo:** Cuerpo HTML institucional con el logo de Eco-Guía, el código OTP resaltado y el mensaje de expiración (15 minutos).

---

### `data/remote/FirebaseStorageRepository.kt`

- **Bloque 1 — KDoc:** Descripción: repositorio de subida/descarga de imágenes en Firebase Storage; autores y fecha.
- **Bloque 2 — `uploadGeoDropImage(imageBytes, fileName)`:** Función suspendida; referencia a `FirebaseStorage.getInstance().reference.child("geodrops/$fileName")`; sube los bytes de la imagen; retorna la URL de descarga pública como `String`.
- **Bloque 3 — `uploadSiteImage(imageBytes, siteId)`:** Similar al anterior pero en la ruta `"sites/$siteId/cover.jpg"`; retorna URL de descarga.
- **Bloque 4 — `deleteImage(url)`:** Elimina la imagen dado su URL de Firebase Storage; útil al rechazar un GeoDrop.

---

### `data/wear/MobileWearListenerService.kt`

- **Bloque 1 — KDoc:** Descripción: `WearableListenerService` que recibe mensajes del reloj; autores y fecha.
- **Bloque 2 — `onMessageReceived(messageEvent)`:** Sobreescribe el método de `WearableListenerService`; verifica el path del mensaje; si el path es `/wear-arrival`, procesa la llegada al sitio; si el path es `/wear-haptic-request`, responde con datos del sitio más cercano; delegar el procesamiento al `WearMessageClient`.

---

### `data/wear/WearMessageClient.kt`

- **Bloque 1 — KDoc:** Descripción: cliente de envío de mensajes al nodo Wear OS activo; autores y fecha.
- **Bloque 2 — `getConnectedNodes(context)`:** Función suspendida; usa `Wearable.getNodeClient(context).connectedNodes.await()` para obtener la lista de relojes conectados.
- **Bloque 3 — `sendSiteToWatch(context, site)`:** Serializa el objeto `HistoricalSite` a JSON; llama a `Wearable.getMessageClient(context).sendMessage(nodeId, "/eco-site-selected", payload)` para cada nodo conectado.
- **Bloque 4 — `sendRouteToWatch(context, route)`:** Serializa la ruta activa a JSON; la envía al reloj por el path `/eco-route-sync`.

---

### `worker/SyncOfflineWorker.kt`

- **Bloque 1 — KDoc:** Descripción: `CoroutineWorker` de WorkManager para sincronizar GeoDrops offline con Neon DB; autores y fecha.
- **Bloque 2 — `doWork()`:** Sobreescribe el método principal; instancia `EcoGuiaDatabase` y `EcoGuiaRepositoryImpl`; consulta en Room los GeoDrops con estado `pendiente_sync`; para cada uno llama al repositorio para subirlos a Neon DB; si todos se sincronizan correctamente retorna `Result.success()`; si falla con reintento posible, retorna `Result.retry()`.
- **Bloque 3 — Companion object:** Define la constante con el nombre único del worker (`SYNC_WORKER_TAG`) usada al encolar con `WorkManager`.

---

### `ui/theme/Color.kt`

- **Bloque único:** Define las constantes de color como `val` de tipo `Color`; incluir colores primarios (verdes Eco-Guía), colores de fondo (oscuro y claro), colores de acento (café histórico, dorado colonial), colores de error y colores de texto; todos en formato hexadecimal.

---

### `ui/theme/Theme.kt`

- **Bloque 1 — `EcoGuiaColorScheme` (dark):** Define el `darkColorScheme` usando los colores de `Color.kt` para modo oscuro.
- **Bloque 2 — `EcoGuiaColorScheme` (light):** Define el `lightColorScheme` para modo claro.
- **Bloque 3 — `EcoGuiaTypography`:** Tipografía personalizada con fuente importada de Google Fonts; definir `titleLarge`, `bodyMedium`, `labelSmall` con los pesos y tamaños del diseño.
- **Bloque 4 — `EcoGuiaTheme`:** Composable público que envuelve `MaterialTheme`; detecta si el sistema está en modo oscuro con `isSystemInDarkTheme()`; aplica el color scheme y tipografía correspondiente; es el único punto de entrada del tema en toda la app.

---

### `ui/navigation/AppNavHost.kt`

- **Bloque 1 — KDoc:** Descripción: define el grafo de navegación completo de la app; autores y fecha.
- **Bloque 2 — Rutas de navegación (`Routes` object):** Declara todas las rutas como constantes `String`; incluir rutas con argumentos (ej. `"site_detail/{siteId}"`); agrupar por sección: Auth, Main, Admin, Devices.
- **Bloque 3 — `AppNavHost` composable:** Recibe el `NavController` y el `EcoGuiaRepository`; configura `NavHost` con `startDestination = Routes.SPLASH`; declara cada `composable("ruta") { }` con los argumentos de navegación necesarios.
- **Bloque 4 — Lógica de redirección:** Si no hay sesión activa → navegar a `Routes.LOGIN`; si hay sesión → navegar a `Routes.EXPLORATION`; si faltan permisos → navegar a `Routes.PERMISSIONS`.
- **Bloque 5 — Rutas protegidas:** Las rutas del grupo Admin solo son accesibles si el `userRole` es `admin` o `moderator`; de lo contrario redirigir a `Routes.EXPLORATION`.

---

### `ui/components/CommonComponents.kt`

- **Bloque 1 — KDoc:** Descripción: biblioteca de componentes UI reutilizables; autores y fecha.
- **Bloque 2 — `EcoButton`:** Composable de botón primario con el estilo de Eco-Guía; parámetros: `text`, `onClick`, `enabled`, `isLoading`; muestra un `CircularProgressIndicator` cuando `isLoading = true`.
- **Bloque 3 — `EcoTextField`:** Campo de texto estilizado; parámetros: `value`, `onValueChange`, `label`, `isError`, `errorMessage`, soporte para campo de contraseña con toggle de visibilidad.
- **Bloque 4 — `EcoCard`:** Card base con esquinas redondeadas, sombra y fondo adaptativo; usada como contenedor en listas y detalles.
- **Bloque 5 — `EcoBadge`:** Indicador de nivel de explorador (ej. "Guardián del Patrimonio"); recibe el nivel como `String` y asigna el color del badge automáticamente.
- **Bloque 6 — `EcoLoadingScreen`:** Pantalla de carga con animación del logo de Eco-Guía; usada mientras se esperan respuestas del servidor.
- **Bloque 7 — `EcoErrorMessage`:** Composable de mensaje de error con ícono y texto descriptivo; incluye botón de reintentar opcional.

---

### `ui/components/BottomMenu.kt`

- **Bloque 1 — KDoc:** Descripción: barra de navegación inferior para usuarios regulares; autores y fecha.
- **Bloque 2 — `BottomMenuItems`:** Objeto sellado o lista que define los ítems del menú: Exploración (ícono mapa), Mi Colección (ícono estrella), IA Chat (ícono chat), Perfil (ícono persona).
- **Bloque 3 — `BottomMenu` composable:** `NavigationBar` de Material 3; itera sobre los ítems; resalta el ítem activo comparando la ruta actual del `NavController`; al pulsar un ítem navega a la ruta correspondiente con `popUpTo` para limpiar el back stack.

---

### `ui/components/AdminNavigation.kt`

- **Bloque 1 — KDoc:** Descripción: navegación de administrador/moderador; autores y fecha.
- **Bloque 2 — Ítems de admin:** Lista de rutas admin: Dashboard, Usuarios, Registro de Sitios, Moderación, Reportes, Campañas, Analíticas.
- **Bloque 3 — `AdminNavigation` composable:** Barra lateral o superior según orientación; ítem activo resaltado; al seleccionar navega a la ruta admin correspondiente.

---

### `ui/components/EcoNavigation.kt`

- **Bloque 1 — KDoc:** Descripción: componente de navegación contextual; autores y fecha.
- **Bloque 2 — `EcoNavigation` composable:** Barra superior con botón de retroceso, título de la pantalla actual y acciones opcionales (ej. filtro, búsqueda); se adapta según la ruta activa del `NavController`.

---

### `ui/components/GeoDropSavingDialog.kt`

- **Bloque 1 — KDoc:** Descripción: diálogo de confirmación al guardar un GeoDrop; autores y fecha.
- **Bloque 2 — `GeoDropSavingDialog` composable:** `AlertDialog` de Material 3; muestra la miniatura de la foto capturada, la ubicación GPS y los campos de título y descripción; botón "Guardar" llama al `GeoDropViewModel.saveGeoDrop()`; botón "Cancelar" descarta el diálogo.

---

### `ui/feature/exploration/MapMarkerUtils.kt`

- **Bloque 1 — KDoc:** Descripción: funciones puras de cálculo geoespacial para el mapa; autores y fecha.
- **Bloque 2 — `calculateDistance(lat1, lon1, lat2, lon2)`:** Implementación de la fórmula haversine; retorna la distancia en metros entre dos coordenadas GPS.
- **Bloque 3 — `formatDistance(meters)`:** Formatea la distancia: si es menor a 1000m retorna "Xm", si es mayor retorna "X.Xkm".
- **Bloque 4 — `getBitmapDescriptor(context, vectorResId)`:** Convierte un vector drawable en `BitmapDescriptor` para usarlo como marcador personalizado en Google Maps.
- **Bloque 5 — `getMarkerColor(category)`:** Retorna el color del marcador según la categoría del sitio (Museo → azul, Monumento → dorado, Plaza → verde, etc.).

---

### `ui/feature/exploration/ExplorationMapContent.kt`

- **Bloque 1 — KDoc:** Descripción: composable del mapa principal de exploración; autores y fecha.
- **Bloque 2 — `ExplorationMapContent` composable:** Recibe la lista de sitios históricos, la ubicación actual del usuario y callbacks; usa `GoogleMap` de Maps Compose; centra el mapa en la posición del usuario al iniciar.
- **Bloque 3 — Marcadores de sitios:** Itera la lista de sitios y agrega un `Marker` por cada uno usando el descriptor de `MapMarkerUtils.getBitmapDescriptor`; al pulsar un marcador llama al callback `onSiteSelected(site)`.
- **Bloque 4 — Marcador del usuario:** Marcador especial (diferente icono) en la posición GPS actual; se actualiza en tiempo real con `LocationViewModel`.
- **Bloque 5 — Controles del mapa:** Botón flotante de "centrar en mi ubicación"; indicador de nivel de zoom; se ocultan cuando el `SiteDetailSheet` está visible.

---

### `ui/feature/exploration/ExplorationSiteList.kt`

- **Bloque 1 — KDoc:** Descripción: lista de sitios históricos con filtros; autores y fecha.
- **Bloque 2 — `ExplorationSiteList` composable:** `LazyColumn` de sitios; recibe la lista filtrada y el callback `onSiteSelected`.
- **Bloque 3 — Filtros de categoría:** Fila horizontal de chips de categoría (Todos, Museo, Monumento, Plaza, Templo); al seleccionar un chip, filtra la lista.
- **Bloque 4 — `SiteListItem`:** Composable interno; muestra la foto miniatura del sitio, nombre, categoría, distancia al usuario y un botón de guardado rápido en colección.
- **Bloque 5 — Estado vacío:** Cuando la lista filtrada está vacía, muestra el composable `EcoErrorMessage` con el mensaje "No se encontraron sitios en esta categoría".

---

### `ui/feature/exploration/SiteDetailSheet.kt`

- **Bloque 1 — KDoc:** Descripción: bottom sheet de detalle de un sitio histórico; autores y fecha.
- **Bloque 2 — `SiteDetailSheet` composable:** `ModalBottomSheet` de Material 3; se muestra al seleccionar un marcador en el mapa o un ítem de la lista.
- **Bloque 3 — Contenido del sheet:** Imagen de portada del sitio a pantalla completa con gradiente; nombre del sitio (título H1); categoría con chip; descripción completa con scroll; coordenadas GPS.
- **Bloque 4 — Acciones:** Botón "Guardar en colección" → llama a `CollectionViewModel.saveItem()`; botón "Enviar al reloj" → llama a `WearMessageClient.sendSiteToWatch()`; botón "Navegar ruta" si el sitio pertenece a una ruta activa.
- **Bloque 5 — Distancia dinámica:** Muestra la distancia calculada por `MapMarkerUtils.calculateDistance` entre la posición actual del usuario y el sitio; se actualiza cada vez que cambia `LocationViewModel.currentLocation`.

---

### `ui/feature/collection/CollectionItemCard.kt`

- **Bloque 1 — KDoc:** Descripción: tarjeta expandible para ítems de Mi Colección; autores y fecha.
- **Bloque 2 — `CollectionItemCard` composable:** Card expandible; en estado colapsado muestra foto miniatura, nombre y fecha de guardado; en estado expandido muestra descripción completa, coordenadas, categoría y acciones.
- **Bloque 3 — Acciones:** Botón de eliminar de la colección; botón de compartir; botón de ver en mapa (navega a `ExplorationScreen` centrando el mapa en ese sitio).
- **Bloque 4 — Indicador de nivel de explorador:** Al pie de la tarjeta muestra el nivel actual del usuario calculado desde el total de ítems en colección: 1-5 ítems = "Turista Reciente", 6-15 = "Explorador Cultural", 16-30 = "Cronista Local", 31+ = "Guardián del Patrimonio".

---

### `ui/viewmodel/AuthViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel de autenticación y sesión; autores y fecha `2026-07-26`.
- **Bloque 2 — Estado:** `AuthUiState` data class con campos: `isLoading`, `isLoggedIn`, `currentUser`, `errorMessage`, `otpSent`, `otpVerified`.
- **Bloque 3 — `login(email, password)`:** Función suspendida en `viewModelScope`; llama a `repository.loginUser(email, password)`; actualiza el estado según el resultado.
- **Bloque 4 — `register(name, email, password)`:** Valida los campos; llama a `repository.registerUser()`; navega al flujo de permisos al completar.
- **Bloque 5 — `sendOtp(email)`:** Genera un código OTP de 6 dígitos; lo almacena en el estado; llama a `EmailService.sendOtpEmail()`.
- **Bloque 6 — `verifyOtp(inputCode)`:** Compara el código ingresado con el almacenado; si coincide, marca `otpVerified = true` y permite restablecer contraseña.
- **Bloque 7 — `resetPassword(email, newPassword)`:** Llama a `repository.updatePassword()`; al completar navega a `LoginScreen`.
- **Bloque 8 — `logout()`:** Limpia el estado de sesión; navega a `LoginScreen`.

---

### `ui/viewmodel/ChatViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel del chat con Miguel Hidalgo IA (Groq); autores y fecha.
- **Bloque 2 — Estado:** Lista mutable de mensajes `List<ChatMessage>` donde cada mensaje tiene `role` (user/assistant) y `content`.
- **Bloque 3 — `sendMessage(userText)`:** Agrega el mensaje del usuario a la lista; llama a `GroqClient.sendChatMessage()` con el historial completo y el system prompt de Miguel Hidalgo; agrega la respuesta del modelo a la lista.
- **Bloque 4 — System prompt:** Constante con el prompt de rol: "Eres Miguel Hidalgo y Costilla, cura de Dolores Hidalgo. Hablas en primera persona con el conocimiento histórico de la época de la Independencia de México. Responde siempre en español y con tono solemne pero accesible."

---

### `ui/viewmodel/CollectionViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel de Mi Colección; autores y fecha.
- **Bloque 2 — Estado:** Lista de `UserSavedItem` como `StateFlow`; contador total de ítems.
- **Bloque 3 — `loadCollection(userId)`:** Llama a `repository.getUserSavedItems(userId)`; actualiza la lista del estado.
- **Bloque 4 — `saveItem(siteId, type)`:** Llama a `repository.saveItemToCollection()`; recarga la colección.
- **Bloque 5 — `removeItem(itemId)`:** Llama a `repository.removeFromCollection()`; actualiza la lista localmente.
- **Bloque 6 — `getExplorerLevel()`:** Función derivada del total de ítems; retorna el nivel de explorador como `String`.

---

### `ui/viewmodel/GeoDropViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel de creación y gestión de GeoDrops; autores y fecha.
- **Bloque 2 — Estado:** `GeoDropUiState` con: `currentPhoto`, `currentLocation`, `pendingGeoDrops`, `isSaving`, `errorMessage`.
- **Bloque 3 — `capturePhoto(imageBytes)`:** Almacena los bytes de la imagen capturada por CameraX; obtiene la ubicación GPS actual desde `LocationViewModel`.
- **Bloque 4 — `saveGeoDrop(title, description)`:** Sube la imagen a Firebase Storage vía `FirebaseStorageRepository`; luego llama a `repository.createGeoDrop()` con URL, título, descripción y coordenadas GPS; si no hay internet, guarda en Room para sincronización posterior.
- **Bloque 5 — `loadNearbyGeoDrops(lat, lon)`:** Carga GeoDrops aprobados dentro de un radio de 500m desde Neon DB.

---

### `ui/viewmodel/LocationViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel de ubicación GPS en tiempo real; autores y fecha.
- **Bloque 2 — Estado:** `currentLocation` como `StateFlow<Location?>`; `hasLocationPermission` como `StateFlow<Boolean>`.
- **Bloque 3 — `startLocationUpdates(context)`:** Configura `FusedLocationProviderClient` con intervalo de 5 segundos; actualiza `currentLocation` con cada nueva posición.
- **Bloque 4 — `stopLocationUpdates()`:** Remueve el listener de ubicación; cancela las corrutinas.
- **Bloque 5 — `requestPermissions(launcher)`:** Lanza el launcher de permisos de ubicación; actualiza `hasLocationPermission` según el resultado.

---

### `ui/viewmodel/RouteViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel de rutas turísticas; autores y fecha.
- **Bloque 2 — Estado:** Ruta activa, lista de paradas, índice de parada actual, progreso de la ruta.
- **Bloque 3 — `loadRoutes()`:** Obtiene todas las rutas disponibles desde `repository.getRoutes()`.
- **Bloque 4 — `startRoute(routeId)`:** Carga la ruta y sus paradas; envía la ruta al reloj via `WearMessageClient.sendRouteToWatch()`; inicia el monitoreo de proximidad hacia la primera parada.
- **Bloque 5 — `markStopReached(stopIndex)`:** Avanza al siguiente punto de la ruta; si es la última parada, marca la ruta como completada.
- **Bloque 6 — `createRoute(name, stops)`:** Crea una nueva ruta con sus paradas; llama a `repository.createRoute()` y `repository.createRouteStops()`.

---

### `ui/viewmodel/SiteRegistrationViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel del flujo de 4 pasos para registrar un sitio histórico; autores y fecha.
- **Bloque 2 — Estado por paso:** `Step1State` (datos básicos: nombre, categoría, descripción), `Step2State` (ubicación GPS en mapa), `Step3State` (fotos y contenido multimedia), `Step4State` (revisión y publicación).
- **Bloque 3 — `nextStep()` / `previousStep()`:** Navegan entre los pasos del flujo; validan que los campos requeridos del paso actual estén completos antes de avanzar.
- **Bloque 4 — `submitSite()`:** Consolida los datos de los 4 pasos; llama a `repository.createHistoricalSite()`; sube las imágenes a Firebase Storage previamente.

---

### `ui/viewmodel/ModerationViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel del panel de moderación de GeoDrops; autores y fecha.
- **Bloque 2 — Estado:** Lista de GeoDrops pendientes de revisión como `StateFlow`.
- **Bloque 3 — `loadPendingGeoDrops()`:** Carga los GeoDrops con estado `pending` desde el repositorio.
- **Bloque 4 — `approveGeoDrop(id)` / `rejectGeoDrop(id, reason)`:** Llaman al repositorio para actualizar el estado del GeoDrop en Neon DB; remueven el ítem de la lista local.

---

### `ui/viewmodel/UserManagementViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel de gestión de usuarios (solo super admin); autores y fecha.
- **Bloque 2 — `loadUsers()`:** Carga la lista de usuarios desde el repositorio.
- **Bloque 3 — `updateUserRole(userId, newRole)`:** Actualiza el rol del usuario en Neon DB; solo puede ser ejecutado por un super admin.
- **Bloque 4 — `deleteUser(userId)`:** Elimina la cuenta del usuario del sistema.

---

### Pantallas de usuario (`ui/screens/`)

Cada pantalla sigue la misma estructura de archivo:

| Sección | Indicación |
|---------|-----------|
| **KDoc del archivo** | Nombre del archivo, descripción de la pantalla, autores, fecha |
| **`@Composable fun NombreScreen`** | Recibe `navController` y los ViewModels necesarios como parámetros |
| **Estado observable** | `val uiState by viewModel.uiState.collectAsState()` |
| **Scaffold de la pantalla** | `Scaffold` con topBar (`EcoNavigation`), content y FAB si aplica |
| **Contenido principal** | Layout específico de la pantalla con los componentes de `CommonComponents.kt` |
| **Navegación** | Llamadas a `navController.navigate(Routes.RUTA)` en los callbacks de acción |
| **Efectos secundarios** | `LaunchedEffect` para cargar datos iniciales al montar la pantalla |

Pantallas y su función principal:

| Archivo | Función principal de la pantalla |
|---------|----------------------------------|
| `SplashScreen.kt` | Logo animado + verificación de sesión activa; redirige a Login o Exploration |
| `LoginScreen.kt` | Formulario email/password; botones de "Crear cuenta" y "¿Olvidaste tu contraseña?" |
| `SignUpScreen.kt` | Formulario de registro con validación de campos y confirmación de contraseña |
| `RecoveryScreen.kt` | Flujo de 3 pasos: ingresar email → ingresar OTP → nueva contraseña |
| `PermissionsScreen.kt` | Solicita permisos de ubicación, cámara y notificaciones con explicación de uso |
| `ExplorationScreen.kt` | Pantalla principal: integra `ExplorationMapContent` y `ExplorationSiteList` en tabs |
| `SearchExperienceScreen.kt` | Búsqueda de sitios y rutas con autocompletado y filtros avanzados |
| `MyCollectionScreen.kt` | Lista de ítems guardados con `CollectionItemCard`; muestra el nivel de explorador |
| `ProfileScreen.kt` | Foto de perfil, nombre, correo, nivel de explorador, estadísticas de exploración |
| `EditProfileScreen.kt` | Formulario de edición de nombre, foto y preferencias de notificaciones |
| `SecurityScreen.kt` | Cambio de contraseña con validación de contraseña actual |
| `MoreOptionsScreen.kt` | Ajustes de la app: tema, idioma, notificaciones, acerca de Eco-Guía |
| `ActiveRouteScreen.kt` | Mapa de la ruta activa con progreso, parada actual y siguiente parada |
| `OfflineRouteScreen.kt` | Visualización de ruta sin internet usando datos en caché de Room |
| `AnchorPhotoScreen.kt` | Preview de la foto capturada con las coordenadas GPS ancladas; confirmar o retomar |
| `CameraGeoDropScreen.kt` | Vista de cámara CameraX para capturar foto de un GeoDrop |
| `CreateRouteScreen.kt` | Mapa interactivo para agregar paradas y definir el orden de una nueva ruta |
| `DeviceStatusScreen.kt` | Estado del reloj Wear OS vinculado: conexión, batería, última sincronización |
| `LinkedDevicesScreen.kt` | Lista de todos los dispositivos vinculados: teléfonos, relojes, TVs |
| `ManageDevicesScreen.kt` | Vinculación/desvinculación de dispositivos con código QR o código manual |
| `IAKnowledgeBaseScreen.kt` | Base de conocimiento del chat IA: temas históricos, sugerencias de preguntas |
| `MiguelHidalgoChatScreen.kt` | Interfaz de chat con burbujas de mensaje; input de texto y botón de envío |
| `NoInternetScreen.kt` | Pantalla de error con animación y botón de reintentar conexión |
| `ProximityAlertsScreen.kt` | Lista de alertas de proximidad activas; permite desactivarlas individualmente |
| `TvCameraScreen.kt` | Vista de cámara para transmitir la posición actual a la Smart TV vía MQTT |

---

### Pantallas de administrador (`ui/screens/admin/`)

| Archivo | Función principal de la pantalla |
|---------|----------------------------------|
| `AdminSummaryScreen.kt` | Dashboard con KPIs: total usuarios, GeoDrops pendientes, sitios activos, dispositivos conectados |
| `UserManagementScreen.kt` | Tabla de usuarios con roles; botones de cambio de rol y eliminación |
| `SiteRegistrationScreen.kt` | **Paso 1/4:** Formulario de datos básicos del sitio (nombre, categoría, descripción corta, descripción larga) |
| `SiteLocationScreen.kt` | **Paso 2/4:** Mapa interactivo para posicionar el sitio; input de coordenadas manual; vista previa del marcador |
| `SiteContentScreen.kt` | **Paso 3/4:** Carga de imagen de portada (Firebase Storage), imágenes adicionales, datos históricos |
| `SiteOperationScreen.kt` | **Paso 4/4:** Resumen del sitio antes de publicar; botón de publicar o guardar como borrador |
| `ModerateCommunityScreen.kt` | Panel general de moderación; tabs de pendientes, aprobados y rechazados |
| `ModerationListScreen.kt` | Lista de GeoDrops pendientes con miniatura, autor, fecha y distancia al sitio relacionado |
| `ReportDetailScreen.kt` | Detalle completo de un GeoDrop reportado: foto, texto, autor, historial de moderación |
| `ReportDecisionScreen.kt` | Formulario de decisión: aprobar o rechazar con campo de motivo de rechazo |
| `ReviewDetailScreen.kt` | Detalle de una revisión de contenido con línea de tiempo de cambios |
| `SecurityReportsScreen.kt` | Reportes de seguridad: intentos de acceso fallidos, usuarios bloqueados |
| `CampaignDevicesScreen.kt` | Lista de TVs registradas; estado de conexión MQTT; botón de transmitir a un dispositivo específico |
| `TVCampaignScreen.kt` | Configuración de la campaña: sitio a proyectar, duración, modo de exhibición |
| `MuseumPortal360Screen.kt` | Vista del portal 360° gestionable por museos; configuración del punto de exhibición |
| `CapsuleGalleryScreen.kt` | Galería de GeoDrops aprobados para mostrar en la TV; ordenados por fecha/popularidad |
| `GalleryAdditionScreen.kt` | Agregar un GeoDrop aprobado a la galería activa de la TV |
| `ManualGeoDropScreen.kt` | Creación manual de un GeoDrop por el admin sin necesidad de foto (solo texto y coordenadas) |
| `VisitorAnalyticsScreen.kt` | Métricas de visitantes: mapa de calor de visitas, GeoDrops por zona, tendencias temporales |

<br>

---

## 🔗 Integración con el Módulo `shared`

Todos los ViewModels reciben `EcoGuiaRepository` (interfaz definida en `shared`) como parámetro de construcción. La instancia concreta `EcoGuiaRepositoryImpl` se crea en `MainActivity` y se pasa a los ViewModels mediante un `ViewModelFactory` o inyección manual. Ningún ViewModel importa directamente clases de la capa de datos; siempre usan la interfaz del dominio de `shared`.

<br>

---

## 🧑‍💻 Desarrolladores

| Nombre | Rol |
|--------|-----|
| Zahir Andrés Rodríguez Mora | Desarrollador Principal |
| Cesar Enrique Garay García | Desarrollador |

**Institución:** Universidad Tecnológica del Norte de Guanajuato (UTNG) — Grupo GIDS6092
