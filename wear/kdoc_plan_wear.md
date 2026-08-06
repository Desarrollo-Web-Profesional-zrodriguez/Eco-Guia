# Plan de Documentación KDoc — Módulo `wear`

> **Instrucciones generales:**
> - Usar KDoc estándar de Kotlin: `/** */` para clases y funciones públicas.
> - Cada archivo debe tener un bloque KDoc de cabecera antes del `package`.
> - Etiquetas obligatorias: `@author`, `@since` (fecha del archivo si existe, si no usar `2026-08-05`).
> - Etiquetas de función: `@param` por cada parámetro, `@return` si retorna algo distinto de `Unit`.
> - Autores del proyecto: `Zahir Andrés Rodríguez Mora` y `Cesar Enrique Garay García`.
> - Las clases y funciones del módulo `shared` que se usen en `wear` deben estar documentadas en el plan de `shared`, no aquí.

---

## 📄 `domain/model/RadarModels.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: modelos de dominio del radar de proximidad; mencionar que son los tipos usados por el ViewModel y el repositorio de radar |
| `RadarState` | data class | Para qué sirve este estado; qué representa cada campo: `isConnected`, `targetSite`, `distanceMeters`, `azimuthDegrees`, `isHapticEnabled`, `isRouteActive`, `routeProgress` |
| `SiteTarget` | data class | Para qué se usa; diferencia con `HistoricalSite` de shared (este es el modelo ligero para el radar) |
| `HapticPattern` | enum class | Qué representa cada valor: `SINGLE_PULSE` (cuándo se usa), `SLOW_TRIPLE`, `FAST_TRIPLE`, `LONG_BUZZ` |

---

## 📄 `domain/repository/RadarRepository.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: interfaz que define el contrato de acceso a datos para el módulo wear; mencionar que `RadarRepositoryImpl` la implementa |
| `RadarRepository` | interface | Propósito de la abstracción |
| `getNearestSite` | suspend fun | `@param lat`, `@param lon`; `@return SiteTarget?` el sitio más cercano o null si no hay ninguno |
| `getActiveSites` | fun | `@return Flow<List<SiteTarget>>` flujo observable de sitios activos |
| `syncRouteFromPhone` | suspend fun | `@param routeJson` JSON recibido del teléfono; qué almacena |
| `updateRadarTarget` | fun | `@param site` nuevo sitio objetivo; cuándo se llama |

---

## 📄 `data/haptics/HapticController.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: centraliza el control del motor háptico del reloj; único punto de acceso a la vibración en toda la app |
| `HapticController` | class | Constructor; qué API usa según el API level (VibratorManager vs Vibrator) |
| `vibrate` | fun | `@param pattern: HapticPattern` patrón a ejecutar; qué pasa internamente |
| `cancel` | fun | Para qué sirve; cuándo llamarla |

---

## 📄 `data/repository/RadarRepositoryImpl.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: implementación de `RadarRepository`; integra el repositorio de shared para cargar sitios y calcula distancia y azimut localmente |
| `RadarRepositoryImpl` | class | Constructor; `@param sharedRepo: EcoGuiaRepository`, `@param context: Context` |
| `getActiveSites` | fun | Qué hace con los datos de shared; cómo mapea a `SiteTarget` |
| `getNearestSite` | suspend fun | Qué algoritmo usa para encontrar el sitio más cercano |
| `calculateHaversineDistance` | private fun | `@param lat1`, `@param lon1`, `@param lat2`, `@param lon2`; `@return Float` metros |
| `calculateAzimuth` | private fun | `@param userLat`, `@param userLon`, `@param targetLat`, `@param targetLon`; `@return Float` grados 0–360 |

---

## 📄 `data/repository/extensions/RadarAlertsExt.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: extensiones de `RadarRepositoryImpl` para determinar cuándo y cómo lanzar alertas de proximidad |
| `RadarRepositoryImpl.shouldTriggerAlert` | extension fun | `@param distanceMeters`; qué umbrales usa; `@return AlertLevel` |
| `RadarRepositoryImpl.getHapticPatternForDistance` | extension fun | `@param distanceMeters`; cómo mapea la distancia al patrón; `@return HapticPattern` |

---

## 📄 `data/repository/extensions/RadarAutoSearchExt.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: extensiones para la búsqueda automática y continua del sitio más cercano en segundo plano |
| `RadarRepositoryImpl.startAutoSearch` | extension fun | `@param locationFlow: Flow<Location>`; cómo detecta cambios en el objetivo; cuándo notifica al ViewModel |
| `RadarRepositoryImpl.stopAutoSearch` | extension fun | Qué cancela; cuándo llamarla |

---

## 📄 `data/repository/extensions/RadarRouteSyncExt.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: extensiones para sincronizar la ruta activa recibida del teléfono y gestionar el progreso de paradas |
| `RadarRepositoryImpl.syncRouteFromPhone` | extension fun | `@param routeJson` JSON serializado de la ruta; cómo lo parsea; dónde lo almacena |
| `RadarRepositoryImpl.getNextRouteStop` | extension fun | `@param currentIndex`; `@return SiteTarget?` siguiente parada o null al terminar |
| `RadarRepositoryImpl.markStopReached` | extension fun | `@param stopIndex`; qué actualiza; qué envía al teléfono |

---

## 📄 `data/wear/LocationHelper.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: helper para obtener la ubicación GPS del reloj; usa FusedLocationProviderClient |
| `LocationHelper` | class | `@param context: Context` |
| `getCurrentLocation` | suspend fun | `@return Location?` ubicación actual; qué hace si el permiso no está otorgado |
| `getLocationFlow` | fun | `@return Flow<Location>` flujo continuo de ubicación; intervalo de actualización |

---

## 📄 `data/wear/SensorHelper.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: helper para leer el sensor de orientación del reloj y calcular el azimut |
| `SensorHelper` | class | `@param context: Context`; qué sensor registra |
| `getAzimuthFlow` | fun | `@return Flow<Float>` azimut en grados 0–360; cómo se calcula desde el sensor de rotación |
| `unregister` | fun | Cuándo llamar; qué libera |

---

## 📄 `data/wear/EcoWearMessageService.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: WearableListenerService que recibe mensajes del teléfono y actualiza el repositorio de radar |
| `EcoWearMessageService` | class | Qué hereda; cuándo lo invoca el sistema |
| `onMessageReceived` | fun | Qué paths maneja y qué acción toma cada uno; `@param messageEvent` |

---

## 📄 `data/wear/WearMessageListener.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: clase auxiliar que procesa los mensajes recibidos del teléfono; separa la lógica del Service |
| `WearMessageListener` | class | Para qué existe; cómo se relaciona con `EcoWearMessageService` |
| `processMessage` | fun | `@param path`, `@param payload`; `@return WearMessage` resultado parseado |
| `WearMessage` | sealed class | Cada subtipo: `SiteSelected`, `RouteSynced`, `Disconnect` con sus datos |

---

## 📄 `data/wear/PhoneMessageClient.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: cliente para enviar mensajes desde el reloj al teléfono conectado |
| `PhoneMessageClient` | class | Para qué sirve |
| `notifyArrival` | suspend fun | `@param context`, `@param siteId`; qué path usa; `@return Unit` |
| `requestNearestSite` | suspend fun | `@param context`, `@param lat`, `@param lon`; qué path usa |

---

## 📄 `presentation/MainActivity.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: actividad principal del módulo Wear OS; inicializa los helpers y el grafo de navegación |
| `MainActivity` | class | Hereda de `ComponentActivity`; implementa `MessageClient.OnMessageReceivedListener` |
| `onCreate` | fun | Secuencia de inicialización: splash screen, helpers, listeners, setContent |
| `onResume` / `onPause` | fun | Por qué registra/desregistra el MessageClient aquí |
| `onDestroy` | fun | Qué recursos libera |
| `onMessageReceived` | fun | `@param messageEvent`; a quién delega el procesamiento |

---

## 📄 `presentation/RadarViewModel.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: ViewModel central del módulo wear; gestiona el estado del radar, la brújula y las alertas hápticas |
| `RadarViewModel` | class | Constructor; qué dependencias recibe; qué estado expone |
| `_radarState` / `radarState` | StateFlow | Para qué sirve; que es el único `StateFlow` de estado observable del módulo |
| `startRadar` | fun | Qué corrutinas inicia; qué observa |
| `stopRadar` | fun | Qué cancela |
| `selectSite` | fun | `@param site: SiteTarget`; qué actualiza en el estado |
| `triggerHaptic` | fun | `@param pattern: HapticPattern`; a quién delega |
| `onRouteReceived` | fun | `@param routeJson`; qué procesa |

---

## 📄 `presentation/components/EcoWearScaffold.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: Scaffold base para todas las pantallas del reloj; incluye TimeText y fondo AMOLED |
| `EcoWearScaffold` | @Composable fun | `@param content` bloque de contenido; por qué todas las pantallas deben usarlo |

---

## 📄 `presentation/components/CompassArrow.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: flecha animada de brújula que rota en tiempo real hacia el sitio histórico objetivo |
| `CompassArrow` | @Composable fun | `@param azimuth: Float` ángulo en grados 0–360; cómo funciona la animación de rotación |

---

## 📄 `presentation/components/CircularStatus.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: indicador circular de estado de conexión con el teléfono y estado del GPS |
| `CircularStatus` | @Composable fun | `@param isConnected`, `@param hasGps`; qué color muestra en cada combinación de estado |

---

## 📄 `presentation/navigation/EcoGuiaWearNavGraph.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: grafo de navegación completo del módulo Wear OS usando SwipeDismissableNavHost |
| `WearRoutes` | object | Qué contiene; que agrupa todas las rutas del reloj |
| Cada constante de ruta | const String | Una línea describiendo a qué pantalla corresponde |
| `EcoGuiaWearNavGraph` | @Composable fun | `@param viewModel: RadarViewModel`; cuál es el `startDestination` y cuándo cambia |

---

## 📄 `presentation/navigation/RadarPagerScreen.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: pager horizontal que permite deslizarse entre RadarScreen y CompassScreen |
| `RadarPagerScreen` | @Composable fun | `@param viewModel: RadarViewModel`; qué páginas contiene; qué indicador muestra |

---

## 📄 Pantallas (`presentation/screens/*.kt`) — 11 archivos

Para **cada** pantalla aplicar el siguiente patrón mínimo:

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Nombre de la pantalla, descripción de su función, cuándo se navega a ella |
| Función principal `@Composable` | fun | `@param viewModel: RadarViewModel`; `@param navController`; qué muestra en pantalla |
| Sub-composables internos si existen | fun | Breve descripción de su función específica |

| Archivo | Lo que debe documentar en el KDoc de la pantalla |
|---------|--------------------------------------------------|
| `RadarScreen.kt` | Pantalla principal del radar; muestra distancia, azimut y nombre del sitio objetivo |
| `CompassScreen.kt` | Brújula visual animada; usa `CompassArrow` con el azimut del ViewModel |
| `AlertsScreen.kt` | Lista de alertas de proximidad activas; permite desactivarlas individualmente |
| `ArrivalScreen.kt` | Confirmación visual de llegada al sitio; cuándo se navega automáticamente a ella |
| `HapticSettingsScreen.kt` | Configuración de intensidad y activación de la vibración háptica |
| `PairingScreen.kt` | Pantalla de espera de conexión con el teléfono; cuándo navega a RadarScreen |
| `ProximityAlertScreen.kt` | Alerta inmediata al cruzar umbral de 100m; qué patrón háptico activa |
| `RouteCompletedWearScreen.kt` | Pantalla de celebración al completar la ruta; qué estadísticas muestra |
| `RouteSummaryScreen.kt` | Resumen de la ruta activa: nombre, parada actual, siguiente sitio |
| `SiteNearbyScreen.kt` | Notificación de sitio muy cercano (<30m); diseño compacto |
| `StealthRadarScreen.kt` | Radar en modo discreto; pantalla oscura; solo vibración indica proximidad |

---

## ✅ Checklist de archivos a documentar

- [ ] `domain/model/RadarModels.kt`
- [ ] `domain/repository/RadarRepository.kt`
- [ ] `data/haptics/HapticController.kt`
- [ ] `data/repository/RadarRepositoryImpl.kt`
- [ ] `data/repository/extensions/RadarAlertsExt.kt`
- [ ] `data/repository/extensions/RadarAutoSearchExt.kt`
- [ ] `data/repository/extensions/RadarRouteSyncExt.kt`
- [ ] `data/wear/EcoWearMessageService.kt`
- [ ] `data/wear/LocationHelper.kt`
- [ ] `data/wear/PhoneMessageClient.kt`
- [ ] `data/wear/SensorHelper.kt`
- [ ] `data/wear/WearMessageListener.kt`
- [ ] `presentation/MainActivity.kt`
- [ ] `presentation/RadarViewModel.kt`
- [ ] `presentation/components/CircularStatus.kt`
- [ ] `presentation/components/CompassArrow.kt`
- [ ] `presentation/components/EcoWearScaffold.kt`
- [ ] `presentation/navigation/EcoGuiaWearNavGraph.kt`
- [ ] `presentation/navigation/RadarPagerScreen.kt`
- [ ] `presentation/screens/AlertsScreen.kt`
- [ ] `presentation/screens/ArrivalScreen.kt`
- [ ] `presentation/screens/CompassScreen.kt`
- [ ] `presentation/screens/HapticSettingsScreen.kt`
- [ ] `presentation/screens/PairingScreen.kt`
- [ ] `presentation/screens/ProximityAlertScreen.kt`
- [ ] `presentation/screens/RadarScreen.kt`
- [ ] `presentation/screens/RouteCompletedWearScreen.kt`
- [ ] `presentation/screens/RouteSummaryScreen.kt`
- [ ] `presentation/screens/SiteNearbyScreen.kt`
- [ ] `presentation/screens/StealthRadarScreen.kt`
