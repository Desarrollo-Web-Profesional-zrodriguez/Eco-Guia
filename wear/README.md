<div align="center">
  <h1>⌚ Módulo <code>wear</code> — Eco-Guía Wear OS</h1>
  <p><strong>Guía de Desarrollo — Archivos e Indicaciones por Paso</strong></p>
  <div>
    <img src="https://img.shields.io/badge/Kotlin-2.1.0-blue?style=for-the-badge&logo=kotlin">
    <img src="https://img.shields.io/badge/Wear_Compose-1.x-4285F4?style=for-the-badge&logo=wear-os">
    <img src="https://img.shields.io/badge/Horologist-0.x-orange?style=for-the-badge">
    <img src="https://img.shields.io/badge/minSdk-30-green?style=for-the-badge&logo=android">
    <img src="https://img.shields.io/badge/compileSdk-34-green?style=for-the-badge&logo=android">
    <img src="https://img.shields.io/badge/Android_Studio-Meerkat_2025.1.1-3DDC84?style=for-the-badge&logo=androidstudio">
  </div>
</div>

<br>

> ⚠️ **Dependencia de `shared`:** El módulo `wear` consume `EcoGuiaRepository` de `:shared` para cargar sitios históricos y sincronizar rutas. `:shared` debe compilarse antes de trabajar en `:wear`. Consulta el [README raíz](../README.md) para la configuración del entorno global.

> ⚠️ **Dependencia de `mobile`:** Para probar la comunicación Wear ↔ Phone en emuladores, el módulo `:mobile` debe estar corriendo simultáneamente en un emulador de teléfono emparejado.

<br>

---

## 📋 Versiones y Configuración del Módulo

| Parámetro | Valor |
|-----------|-------|
| `namespace` | `mx.utng.ecoguiawear` |
| `applicationId` | `mx.utng.ecoguiawear` |
| `compileSdk` | 34 |
| `minSdk` | 30 (Wear OS 3+) |
| `targetSdk` | 34 |
| `versionName` | 1.0.0 |
| `JVM Target` | 17 |
| `buildFeatures` | `compose = true` |

<br>

---

## 🗂️ Estructura Completa del Módulo

```
wear/
└── src/main/java/mx/utng/ecoguiawear/
    ├── data/
    │   ├── haptics/
    │   │   └── HapticController.kt
    │   ├── repository/
    │   │   ├── RadarRepositoryImpl.kt
    │   │   └── extensions/
    │   │       ├── RadarAlertsExt.kt
    │   │       ├── RadarAutoSearchExt.kt
    │   │       └── RadarRouteSyncExt.kt
    │   └── wear/
    │       ├── EcoWearMessageService.kt
    │       ├── LocationHelper.kt
    │       ├── PhoneMessageClient.kt
    │       ├── SensorHelper.kt
    │       └── WearMessageListener.kt
    ├── domain/
    │   ├── model/
    │   │   └── RadarModels.kt
    │   └── repository/
    │       └── RadarRepository.kt
    └── presentation/
        ├── MainActivity.kt
        ├── RadarViewModel.kt
        ├── components/
        │   ├── CircularStatus.kt
        │   ├── CompassArrow.kt
        │   └── EcoWearScaffold.kt
        ├── navigation/
        │   ├── EcoGuiaWearNavGraph.kt
        │   └── RadarPagerScreen.kt
        ├── screens/
        │   ├── AlertsScreen.kt
        │   ├── ArrivalScreen.kt
        │   ├── CompassScreen.kt
        │   ├── HapticSettingsScreen.kt
        │   ├── PairingScreen.kt
        │   ├── ProximityAlertScreen.kt
        │   ├── RadarScreen.kt
        │   ├── RouteCompletedWearScreen.kt
        │   ├── RouteSummaryScreen.kt
        │   ├── SiteNearbyScreen.kt
        │   └── StealthRadarScreen.kt
        └── theme/
```

<br>

---

## 📄 Descripción Línea a Línea por Archivo

---

### `wear/build.gradle.kts`

- **Bloque de plugins:** Aplicar `android.application` y `kotlin.compose`; no requiere `google.services` a menos que se agregue Firebase en el futuro.
- **Bloque `android`:** Declarar `namespace = "mx.utng.ecoguiawear"`, `compileSdk = 34`, `defaultConfig` con `applicationId`, `minSdk = 30`, `targetSdk = 34`, `versionCode` y `versionName`.
- **`compileOptions` y `kotlin.compilerOptions`:** Configurar JVM 17 en ambos.
- **`buildFeatures`:** Activar solo `compose = true`.
- **Bloque `dependencies`:** Primera línea: `implementation(project(":shared"))`; luego agregar `horologist.compose.layout`, `wear.compose.foundation`, `wear.compose.material`, `wear.compose.navigation`, `androidx.core.splashscreen`, `play.services.wearable`, `play.services.location`.

---

### `wear/src/main/AndroidManifest.xml`

- **`uses-feature`:** Declarar `android.hardware.type.watch` para identificar el módulo como app de reloj.
- **Permisos requeridos:** `BODY_SENSORS`, `ACCESS_FINE_LOCATION`, `VIBRATE`, `WAKE_LOCK`, `INTERNET`.
- **`MainActivity`:** Declarar como launcher con category `android.intent.category.DEFAULT` y `com.google.intent.category.BROWSABLE` para Wear OS; incluir el meta-data de standalone app (`com.google.android.wearable.standalone = true`).
- **`EcoWearMessageService`:** Declarar como `<service>` exportado; agregar intent-filter con la acción `com.google.android.gms.wearable.MESSAGE_RECEIVED` y el data path `/eco-site-selected`.

---

### `domain/model/RadarModels.kt`

- **Bloque 1 — KDoc:** Descripción: modelos de dominio del radar de proximidad; autores y fecha.
- **Bloque 2 — `RadarState` (data class):** Estado completo del radar; campos: `isConnected: Boolean` (estado de conexión con el teléfono), `targetSite: SiteTarget?` (sitio objetivo actual), `distanceMeters: Float` (distancia calculada al objetivo), `azimuthDegrees: Float` (ángulo hacia el objetivo respecto al norte), `isHapticEnabled: Boolean` (si la vibración está activa), `isRouteActive: Boolean` (si hay una ruta en progreso), `routeProgress: Int` (índice de la parada actual).
- **Bloque 3 — `SiteTarget` (data class):** Datos mínimos del sitio para el radar; campos: `id: Int`, `name: String`, `latitude: Double`, `longitude: Double`, `categoryName: String`.
- **Bloque 4 — `HapticPattern` (enum class):** Patrones de vibración disponibles: `SINGLE_PULSE` (llegada), `SLOW_TRIPLE` (proximidad lejana), `FAST_TRIPLE` (proximidad cercana), `LONG_BUZZ` (alerta crítica).

---

### `domain/repository/RadarRepository.kt`

- **Bloque 1 — KDoc:** Descripción: interfaz del repositorio de radar; define el contrato de acceso a datos para el módulo wear; autores y fecha.
- **Bloque 2 — `getNearestSite(lat, lon)`:** Función suspendida; recibe coordenadas del usuario; retorna el `SiteTarget?` más cercano calculado por la capa de datos.
- **Bloque 3 — `getActiveSites()`:** Retorna un `Flow<List<SiteTarget>>` con todos los sitios históricos activos; se observa desde el `RadarViewModel`.
- **Bloque 4 — `syncRouteFromPhone(routeJson)`:** Función suspendida; recibe el JSON de la ruta enviada por el teléfono; parsea y almacena las paradas en memoria o Room; retorna `Unit`.
- **Bloque 5 — `updateRadarTarget(site)`:** Actualiza el sitio objetivo actual del radar; se llama cuando el usuario selecciona un sitio desde el teléfono.

---

### `data/haptics/HapticController.kt`

- **Bloque 1 — KDoc:** Descripción: controlador del motor háptico del reloj; centraliza todos los patrones de vibración de la app; autores y fecha.
- **Bloque 2 — Constructor:** Recibe `context: Context`; obtiene el `Vibrator` o `VibratorManager` según el API level del dispositivo (usar `VibratorManager` en API 31+, `Vibrator` en API 30).
- **Bloque 3 — `vibrate(pattern)`:** Recibe un `HapticPattern`; ejecuta el patrón de vibración correspondiente usando `VibrationEffect.createWaveform` o `VibrationEffect.createOneShot` según el patrón; no bloquea el hilo principal.
- **Bloque 4 — `cancel()`:** Detiene cualquier vibración en curso; llamar desde el `RadarViewModel` al salir de la pantalla.

---

### `data/repository/RadarRepositoryImpl.kt`

- **Bloque 1 — KDoc:** Descripción: implementación del `RadarRepository`; carga sitios de `EcoGuiaRepository` (shared), calcula distancias y azimut; autores y fecha.
- **Bloque 2 — Constructor:** Recibe `sharedRepo: EcoGuiaRepository` (del módulo shared) y `context: Context`.
- **Bloque 3 — `getActiveSites()`:** Llama a `sharedRepo.getHistoricalSites()`; mapea los resultados al modelo `SiteTarget` del módulo wear; retorna como `Flow`.
- **Bloque 4 — `getNearestSite(lat, lon)`:** Obtiene la lista de sitios; para cada uno calcula la distancia haversine entre la posición del usuario y el sitio; retorna el sitio con la distancia mínima.
- **Bloque 5 — `calculateHaversineDistance(lat1, lon1, lat2, lon2)`:** Función privada de cálculo de distancia; fórmula haversine estándar; retorna la distancia en metros como `Float`.
- **Bloque 6 — `calculateAzimuth(userLat, userLon, targetLat, targetLon)`:** Calcula el ángulo en grados (0–360°) desde la posición del usuario hacia el sitio objetivo usando `atan2`; retorna `Float`.

---

### `data/repository/extensions/RadarAlertsExt.kt`

- **Bloque 1 — KDoc:** Descripción: extensiones de `RadarRepositoryImpl` para la lógica de alertas de proximidad; autores y fecha.
- **Bloque 2 — `RadarRepositoryImpl.shouldTriggerAlert(distanceMeters)`:** Función de extensión; evalúa si la distancia actual amerita una alerta; umbrales definidos: <500m = alerta leve, <100m = alerta media, <20m = alerta crítica; retorna un `AlertLevel` enum.
- **Bloque 3 — `RadarRepositoryImpl.getHapticPatternForDistance(distanceMeters)`:** Mapea la distancia al patrón háptico correspondiente usando los `AlertLevel` y `HapticPattern` definidos en `RadarModels.kt`; retorna `HapticPattern`.

---

### `data/repository/extensions/RadarAutoSearchExt.kt`

- **Bloque 1 — KDoc:** Descripción: extensiones para la búsqueda automática del sitio más cercano en segundo plano; autores y fecha.
- **Bloque 2 — `RadarRepositoryImpl.startAutoSearch(locationFlow)`:** Función de extensión que recibe un `Flow<Location>`; para cada nueva ubicación del usuario llama a `getNearestSite()`; si el resultado cambia respecto al objetivo anterior, notifica al `RadarViewModel` vía `SharedFlow`.
- **Bloque 3 — `RadarRepositoryImpl.stopAutoSearch()`:** Cancela el job de búsqueda automática en curso.

---

### `data/repository/extensions/RadarRouteSyncExt.kt`

- **Bloque 1 — KDoc:** Descripción: extensiones para sincronizar la ruta activa recibida del teléfono; autores y fecha.
- **Bloque 2 — `RadarRepositoryImpl.syncRouteFromPhone(routeJson)`:** Implementación del método de la interfaz; parsea el JSON usando Kotlinx Serialization; almacena la lista de `SiteTarget` de la ruta en memoria; emite las paradas como `Flow` para que el `RadarViewModel` las consuma.
- **Bloque 3 — `RadarRepositoryImpl.getNextRouteStop(currentIndex)`:** Retorna el siguiente `SiteTarget` en la lista de paradas de la ruta activa; retorna `null` si no hay más paradas.
- **Bloque 4 — `RadarRepositoryImpl.markStopReached(stopIndex)`:** Actualiza el índice actual de la ruta; envía una notificación al teléfono via `PhoneMessageClient` informando el progreso.

---

### `data/wear/LocationHelper.kt`

- **Bloque 1 — KDoc:** Descripción: helper para obtener la ubicación GPS del reloj; autores y fecha.
- **Bloque 2 — Constructor:** Recibe `context: Context`; inicializa `FusedLocationProviderClient`.
- **Bloque 3 — `getCurrentLocation()`:** Función suspendida; verifica el permiso `ACCESS_FINE_LOCATION`; llama a `fusedLocationClient.getCurrentLocation(Priority.HIGH_ACCURACY, null).await()`; retorna `Location?`.
- **Bloque 4 — `getLocationFlow()`:** Retorna un `Flow<Location>` que emite la ubicación actualizada cada 3 segundos usando `locationFlow` del Play Services; usado por `RadarAutoSearchExt`.

---

### `data/wear/SensorHelper.kt`

- **Bloque 1 — KDoc:** Descripción: helper para leer los sensores de orientación del reloj; autores y fecha.
- **Bloque 2 — Constructor:** Recibe `context: Context`; obtiene el `SensorManager`; referencia al sensor `TYPE_ROTATION_VECTOR`.
- **Bloque 3 — `getAzimuthFlow()`:** Retorna un `Flow<Float>` que emite el azimut calculado (0–360°); internamente registra un `SensorEventListener` y usa `SensorManager.getRotationMatrixFromVector` + `SensorManager.getOrientation` para extraer el azimut; convierte radianes a grados.
- **Bloque 4 — `unregister()`:** Desregistra el `SensorEventListener`; llamar en `onDestroy` de `MainActivity`.

---

### `data/wear/EcoWearMessageService.kt`

- **Bloque 1 — KDoc:** Descripción: `WearableListenerService` que recibe mensajes del teléfono móvil; autores y fecha.
- **Bloque 2 — `onMessageReceived(messageEvent)`:** Sobreescribe el método de `WearableListenerService`; usa `when(messageEvent.path)` para manejar cada tipo de mensaje:
  - Path `/eco-site-selected`: parsea el JSON del sitio y llama a `radarRepo.updateRadarTarget()`.
  - Path `/eco-route-sync`: parsea el JSON de la ruta y llama a `radarRepo.syncRouteFromPhone()`.
  - Path `/eco-disconnect`: limpia el estado del radar al desconectarse el teléfono.
- **Bloque 3 — Instanciación del repositorio:** Obtener `EcoGuiaDatabase` y `EcoGuiaRepositoryImpl` de `shared`; construir `RadarRepositoryImpl`; dado que el servicio no tiene un ViewModel, acceder directamente al repositorio.

---

### `data/wear/WearMessageListener.kt`

- **Bloque 1 — KDoc:** Descripción: clase auxiliar que procesa los mensajes recibidos del teléfono; separa la lógica del `EcoWearMessageService`; autores y fecha.
- **Bloque 2 — `processMessage(path, payload)`:** Recibe el path y los bytes del mensaje; los parsea como JSON; retorna un `WearMessage` (sealed class) con los tipos `SiteSelected`, `RouteSynced`, `Disconnect`.
- **Bloque 3 — `WearMessage` (sealed class):** Define los tipos de mensajes posibles con sus datos parseados; ubicado en el mismo archivo o en `RadarModels.kt`.

---

### `data/wear/PhoneMessageClient.kt`

- **Bloque 1 — KDoc:** Descripción: cliente para enviar mensajes desde el reloj al teléfono; autores y fecha.
- **Bloque 2 — `notifyArrival(context, siteId)`:** Función suspendida; obtiene los nodos conectados con `Wearable.getNodeClient(context).connectedNodes.await()`; para cada nodo envía el mensaje al path `/wear-arrival` con el `siteId` como payload.
- **Bloque 3 — `requestNearestSite(context, lat, lon)`:** Envía al teléfono la ubicación actual del reloj para que el teléfono calcule y responda con el sitio más cercano; path `/wear-location-update`.

---

### `presentation/MainActivity.kt`

- **Bloque 1 — KDoc:** Descripción: actividad principal del módulo Wear OS; punto de entrada de la app; inicializa los helpers y el grafo de navegación; autores y fecha.
- **Bloque 2 — `MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener`:** Hereda de `ComponentActivity` e implementa `OnMessageReceivedListener` para recibir mensajes del teléfono directamente en la actividad.
- **Bloque 3 — Campos de la actividad:** `messageListener: WearMessageListener`, `locationHelper: LocationHelper`, `sensorHelper: SensorHelper`; declarados como `lateinit var`.
- **Bloque 4 — `onCreate`:** Llamar a `installSplashScreen()` antes de `super.onCreate`; instanciar los helpers; registrar `Wearable.getMessageClient(this).addListener(this)`; llamar a `setContent` con `EcoGuiaWearTheme` conteniendo `EcoGuiaWearNavGraph`.
- **Bloque 5 — `onResume` / `onPause`:** Registrar y desregistrar el `MessageClient.OnMessageReceivedListener` para evitar fugas de memoria.
- **Bloque 6 — `onDestroy`:** Llamar a `sensorHelper.unregister()`; cancelar cualquier corrutina activa.
- **Bloque 7 — `onMessageReceived(messageEvent)`:** Implementación de la interfaz; delega a `messageListener.processMessage(messageEvent.path, messageEvent.data)`; actualiza el `RadarViewModel` con el resultado.

---

### `presentation/RadarViewModel.kt`

- **Bloque 1 — KDoc:** Descripción: ViewModel central del módulo wear; gestiona el estado del radar, la brújula y las alertas hápticas; autores y fecha.
- **Bloque 2 — Constructor:** Recibe `RadarRepository`, `HapticController`, `LocationHelper`, `SensorHelper`.
- **Bloque 3 — Estado:** `_radarState: MutableStateFlow<RadarState>` expuesto como `StateFlow<RadarState>`; representa el estado completo del radar observable desde la UI.
- **Bloque 4 — `startRadar()`:** Lanza corrutinas en `viewModelScope` para: (a) observar `LocationHelper.getLocationFlow()` y actualizar distancia y azimut; (b) observar `SensorHelper.getAzimuthFlow()` y actualizar el azimut en `_radarState`; (c) verificar alertas hápticas con `radarRepo.shouldTriggerAlert(distance)` en cada actualización de ubicación.
- **Bloque 5 — `stopRadar()`:** Cancela el job activo del radar; llama a `hapticController.cancel()`.
- **Bloque 6 — `selectSite(site)`:** Actualiza `_radarState` con el nuevo `targetSite`; inicia el cálculo de distancia y azimut hacia ese sitio.
- **Bloque 7 — `triggerHaptic(pattern)`:** Llama a `hapticController.vibrate(pattern)` en el dispatcher apropiado.
- **Bloque 8 — `onRouteReceived(routeJson)`:** Llama a `radarRepo.syncRouteFromPhone(routeJson)`; actualiza el estado para mostrar el resumen de ruta.

---

### `presentation/components/EcoWearScaffold.kt`

- **Bloque 1 — KDoc:** Descripción: Scaffold base para todas las pantallas del reloj; autores y fecha.
- **Bloque 2 — `EcoWearScaffold` composable:** Recibe `content: @Composable () → Unit`; usa `Scaffold` de Wear Compose; incluye `TimeText` en la parte superior (muestra la hora del reloj); aplica fondo oscuro (#000000) adecuado para pantallas AMOLED circulares; envuelve el contenido con `ScalingLazyColumnDefaults` si aplica.

---

### `presentation/components/CompassArrow.kt`

- **Bloque 1 — KDoc:** Descripción: flecha animada de brújula que apunta hacia el sitio objetivo; autores y fecha.
- **Bloque 2 — `CompassArrow` composable:** Recibe `azimuth: Float` (ángulo en grados respecto al norte, 0–360°); usa `animateFloatAsState` con `tween` para suavizar la rotación; dibuja la flecha usando `Canvas` + `DrawScope.rotate(azimuth)` o con un `Image` + `Modifier.rotate(azimuth)`; la flecha apunta hacia el norte (0°) en su estado base y rota según el azimut recibido.

---

### `presentation/components/CircularStatus.kt`

- **Bloque 1 — KDoc:** Descripción: indicador circular de estado de conexión y GPS; autores y fecha.
- **Bloque 2 — `CircularStatus` composable:** Recibe `isConnected: Boolean` y `hasGps: Boolean`; muestra un círculo pequeño en la esquina de la pantalla; verde si ambos están activos, amarillo si solo uno, rojo si ninguno; incluye un tooltip al pulsar con el detalle del estado.

---

### `presentation/navigation/EcoGuiaWearNavGraph.kt`

- **Bloque 1 — KDoc:** Descripción: grafo de navegación completo del módulo Wear OS usando `SwipeDismissableNavHost`; autores y fecha.
- **Bloque 2 — `WearRoutes` object:** Define las rutas de navegación como constantes `String`: `PAIRING`, `RADAR`, `COMPASS`, `ALERTS`, `HAPTIC_SETTINGS`, `ROUTE_SUMMARY`, `ROUTE_COMPLETED`, `ARRIVAL`, `SITE_NEARBY`, `PROXIMITY_ALERT`, `STEALTH`.
- **Bloque 3 — `EcoGuiaWearNavGraph` composable:** Recibe el `RadarViewModel`; usa `rememberSwipeDismissableNavController()`; el `startDestination` es `PAIRING` si no hay conexión con el teléfono, o `RADAR` si ya está vinculado; el deslizamiento hacia la derecha activa el `SwipeDismissableNavHost` built-in para retroceder.
- **Bloque 4 — Rutas declaradas:** Un `composable(WearRoutes.RADAR)` por cada pantalla; las pantallas de alerta (`PROXIMITY_ALERT`, `SITE_NEARBY`, `ARRIVAL`) se navegan programáticamente desde `RadarViewModel` al detectar la proximidad.

---

### `presentation/navigation/RadarPagerScreen.kt`

- **Bloque 1 — KDoc:** Descripción: pantalla paginadora que permite deslizarse horizontalmente entre `RadarScreen` y `CompassScreen`; autores y fecha.
- **Bloque 2 — `RadarPagerScreen` composable:** Recibe el `RadarViewModel`; usa `HorizontalPager` de Wear Compose con 2 páginas: índice 0 = `RadarScreen`, índice 1 = `CompassScreen`; incluye `HorizontalPageIndicator` en la parte inferior para que el usuario sepa en qué página está.

---

### Pantallas (`presentation/screens/`)

Cada pantalla del reloj sigue la misma estructura de archivo:

| Sección | Indicación |
|---------|-----------|
| **KDoc del archivo** | Nombre, descripción de la pantalla, autores, fecha |
| **`@Composable fun NombreScreen`** | Recibe `viewModel: RadarViewModel` y `navController` |
| **Estado observable** | `val radarState by viewModel.radarState.collectAsState()` |
| **`EcoWearScaffold`** | Envuelve todo el contenido de la pantalla |
| **Contenido** | Layout circular adaptado a pantalla redonda usando `ScalingLazyColumn` o `Box` centrado |
| **Navegación** | `LaunchedEffect` con condiciones del `radarState` para navegar automáticamente |

Pantallas y su función principal:

| Archivo | Función principal de la pantalla |
|---------|----------------------------------|
| `RadarScreen.kt` | Pantalla principal: muestra la distancia en metros al sitio objetivo, el nombre del sitio y un indicador visual circular de proximidad; el círculo se contrae conforme el usuario se acerca |
| `CompassScreen.kt` | Muestra el componente `CompassArrow` rotando en tiempo real según el azimut del `RadarViewModel`; incluye el nombre del sitio objetivo en la parte inferior |
| `AlertsScreen.kt` | Lista de alertas de proximidad activas con `ScalingLazyColumn`; cada ítem muestra el nombre del sitio, la distancia y la hora de la alerta; permite desactivar alertas individuales |
| `ArrivalScreen.kt` | Pantalla de confirmación visual al llegar al sitio (distancia < 20m); animación de celebración; botón "Siguiente parada" si hay ruta activa |
| `HapticSettingsScreen.kt` | Controles de intensidad háptica (Baja/Media/Alta) con sliders circulares de Wear Compose; toggle para activar/desactivar completamente la vibración |
| `PairingScreen.kt` | Pantalla de espera mientras se establece la conexión con el teléfono; muestra animación de búsqueda; al conectar navega automáticamente a `RadarScreen` |
| `ProximityAlertScreen.kt` | Alerta inmediata al cruzar el umbral de 100m; vibra con `SLOW_TRIPLE`; muestra el nombre del sitio y la distancia; botón "Ver en radar" navega a `RadarScreen` |
| `RouteCompletedWearScreen.kt` | Pantalla de celebración al completar todas las paradas de la ruta; muestra el tiempo total, los sitios visitados y un mensaje de felicitación |
| `RouteSummaryScreen.kt` | Resumen de la ruta activa: nombre, total de paradas, parada actual (índice), siguiente sitio con su distancia |
| `SiteNearbyScreen.kt` | Notificación pequeña de sitio muy cercano (<30m); vibra con `FAST_TRIPLE`; diseño compacto para no interrumpir demasiado al usuario |
| `StealthRadarScreen.kt` | Radar en modo discreto: fondo completamente negro, sin texto visible; solo la vibración háptica indica la proximidad; activo cuando el usuario quiere usar el radar sin distracciones visuales |

<br>

---

## 🔗 Integración con `shared` y `mobile`

| Componente `wear` | Componente relacionado | Propósito |
|-------------------|----------------------|-----------|
| `RadarRepositoryImpl` | `EcoGuiaRepository` (shared) | Cargar sitios históricos desde Neon DB |
| `EcoWearMessageService` | `WearMessageClient` (mobile) | Recibir el sitio seleccionado desde el teléfono |
| `PhoneMessageClient` | `MobileWearListenerService` (mobile) | Enviar notificaciones de llegada al teléfono |
| `RadarRouteSyncExt` | `RouteViewModel` (mobile) | Sincronizar la ruta activa enviada por el teléfono |

**Path de mensajes Wear ↔ Mobile:**

| Path | Dirección | Contenido |
|------|-----------|-----------|
| `/eco-site-selected` | Mobile → Wear | JSON del `HistoricalSite` seleccionado en el teléfono |
| `/eco-route-sync` | Mobile → Wear | JSON de la ruta activa con sus paradas |
| `/eco-disconnect` | Mobile → Wear | Señal de desconexión; limpiar estado del radar |
| `/wear-arrival` | Wear → Mobile | `siteId` del sitio al que llegó el usuario |
| `/wear-location-update` | Wear → Mobile | Coordenadas GPS actuales del reloj |

<br>

---

## 🧑‍💻 Desarrolladores

| Nombre | Rol |
|--------|-----|
| Zahir Andrés Rodríguez Mora | Desarrollador Principal |
| Cesar Enrique Garay García | Desarrollador |

**Institución:** Universidad Tecnológica del Norte de Guanajuato (UTNG) — Grupo GIDS6092
