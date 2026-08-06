# Guía Paso a Paso: Construyendo el Módulo Wear OS de Eco-Guía

Esta guía documenta y desglosa paso a paso la arquitectura, configuración y construcción completa del módulo **Wear OS (Smartwatch)** de **Eco-Guía Dolores Hidalgo**, explicando las decisiones de diseño para pantallas circulares AMOLED, el cálculo de azimut y navegación con brújula háptica, la sincronización en tiempo real vía **Wearable Data Layer API** y la gestión de batería optimizada.

---

## Objetivo de Esta Guía

Al estudiar y seguir esta guía, comprenderás:

1. Cómo estructurar una aplicación moderna para **Wear OS 3+ (API 30+)** utilizando **Kotlin 2.1**, **Jetpack Compose for Wear OS (`androidx.wear.compose.material3`)** y componentes de **Horologist**.
2. Cómo implementar un **Radar Háptico de Proximidad** que calcula distancias geodésicas en tiempo real (Haversine) y guía al usuario hacia monumentos históricos mediante vibraciones sensoriales configurables.
3. Cómo construir una **Brújula Digital Suavizada** con sensores de orientación (`TYPE_ROTATION_VECTOR`) y representación visual animada (`CompassArrow`).
4. Cómo orquestar la comunicación bidireccional entre el Smartwatch y el Teléfono Móvil mediante **Google Play Services Wearable** (`MessageClient`, `WearableListenerService`, `CapabilityClient`).
5. Cómo diseñar una experiencia táctil intuitiva mediante **carruseles horizontales (`HorizontalPager`)**, soporte para corona rotatoria (*Rotary input*) y **Modo Discreto (*Stealth Mode*)** para navegación sin emisión de luz.

---

## FASE 1: Configuración Inicial del Entorno y Build System

### Paso 1.1: Configurar el Catálogo de Versiones (`gradle/libs.versions.toml`)

Wear OS requiere dependencias específicas de Compose for Wear, Horologist para layouts con corona rotatoria y Play Services Wearable para sincronización.

```toml
[versions]
agp = "8.8.0"
kotlin = "2.1.0"
composeBom = "2025.01.00"
wearCompose = "1.4.0"
wearComposeMaterial3 = "1.0.0-alpha29"
horologist = "0.7.0-alpha02"
playServicesWearable = "19.0.0"
playServicesLocation = "21.3.0"

[libraries]
wear-compose-foundation = { group = "androidx.wear.compose", name = "compose-foundation", version.ref = "wearCompose" }
wear-compose-material = { group = "androidx.wear.compose", name = "compose-material", version.ref = "wearCompose" }
wear-compose-material3 = { group = "androidx.wear.compose", name = "compose-material3", version.ref = "wearComposeMaterial3" }
wear-compose-navigation = { group = "androidx.wear.compose", name = "compose-navigation", version.ref = "wearCompose" }
horologist-compose-layout = { group = "com.google.android.horologist", name = "horologist-compose-layout", version.ref = "horologist" }
play-services-wearable = { group = "com.google.android.gms", name = "play-services-wearable", version.ref = "playServicesWearable" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
```

> **CONCEPTO CLAVE:** Horologist complementa Wear Compose proveyendo `AppScaffold` y `ScreenScaffold`, componentes que gestionan automáticamente el indicador de hora del sistema (`TimeText`) y la vinculación con la corona rotatoria (*Rotary input*).

---

### Paso 1.2: Configurar `wear/build.gradle.kts`

El módulo `:wear` se configura como aplicación ejecutable independiente (`com.android.application`) pero consume directamente el módulo de lógica compartida `:shared` para modelos y acceso a Room.

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.utng.ecoguiawear"
    compileSdk = 37

    defaultConfig {
        applicationId = "mx.utng.ecoguiawear"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.location)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.navigation)
    implementation(libs.horologist.compose.layout)
}
```

---

### Paso 1.3: Configurar el Manifiesto (`wear/src/main/AndroidManifest.xml`)

El manifiesto define los permisos de sensores, hardware de reloj y declara el servicio receptor de mensajes en segundo plano.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Declaración obligatoria de dispositivo wearable -->
    <uses-feature
        android:name="android.hardware.type.watch"
        android:required="true" />

    <!-- Permisos de hardware y sensores -->
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault">

        <meta-data
            android:name="com.google.android.wearable.standalone"
            android:value="false" />
        <meta-data
            android:name="com.google.android.gms.wearable.capabilities"
            android:resource="@array/android_wear_capabilities" />

        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.DeviceDefault">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Servicio receptor de eventos de la capa de datos Wearable -->
        <service
            android:name=".data.wear.EcoWearMessageService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.BIND_LISTENER" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

---

## FASE 2: Capa de Dominio (Domain Layer)

### Paso 2.1: Modelos de Datos del Radar (`domain/model/RadarModels.kt`)

La capa de dominio define el estado completo de la interfaz de usuario (`RadarUiState`), los objetivos de radar (`RadarTarget`), las paradas de ruta (`Waypoint`, `RouteSummary`) y los modos operativos (`RadarMode`, `HapticStrength`).

```kotlin
enum class TargetType {
    HISTORIC_SITE,
    GEO_DROP
}

enum class RadarMode {
    PAUSED,
    SCANNING,
    FOLLOWING_ARROW,
    ARRIVED
}

enum class HapticStrength {
    LOW,
    MEDIUM,
    HIGH
}

data class RadarTarget(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: TargetType,
    val distanceMeters: Int,
    val bearingDegrees: Float,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isAutoTarget: Boolean = false
)

data class RouteSummary(
    val title: String,
    val visitedStops: Int,
    val totalStops: Int,
    val nextStop: String,
    val estimatedMinutes: Int,
    val waypoints: List<Waypoint> = emptyList()
)

data class Waypoint(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val isReached: Boolean = false
)

data class HapticSettings(
    val enabled: Boolean = true,
    val strength: HapticStrength = HapticStrength.MEDIUM
)

data class AlertEntity(
    val id: String,
    val message: String,
    val type: String,
    val timestamp: Long
)

data class RadarUiState(
    val isLinkedToPhone: Boolean = false,
    val isStealthMode: Boolean = false,
    val mode: RadarMode = RadarMode.PAUSED,
    val isGpsEnabled: Boolean = true,
    val isCameraReady: Boolean = true,
    val alerts: List<AlertEntity> = emptyList(),
    val currentHeading: Float = 0f,
    val target: RadarTarget = RadarTarget(
        id = "none",
        title = "Esperando objetivo",
        subtitle = "Selecciona un sitio en el móvil",
        type = TargetType.HISTORIC_SITE,
        distanceMeters = 0,
        bearingDegrees = 0f
    ),
    val routeSummary: RouteSummary = RouteSummary(
        title = "Sin ruta activa",
        visitedStops = 0,
        totalStops = 0,
        nextStop = "Esperando ruta desde móvil",
        estimatedMinutes = 0,
        waypoints = emptyList()
    ),
    val hapticSettings: HapticSettings = HapticSettings(),
    val lastAlert: String = "Radar listo",
    val isRouteCompleted: Boolean = false,
    val nearbyAutoTargets: List<RadarTarget> = emptyList(),
    val selectedAutoIndex: Int = 0
)
```

---

### Paso 2.2: Contrato del Repositorio (`domain/repository/RadarRepository.kt`)

Define las operaciones del radar, flujos reactivos de estado y métodos de sincronización con el teléfono móvil.

```kotlin
interface RadarRepository {
    val radarState: StateFlow<RadarUiState>

    fun setLinkedToPhone(linked: Boolean)
    fun startRadar()
    fun toggleRadar()
    fun toggleStealthMode()
    fun setStealthMode(enabled: Boolean)
    fun setAlerts(alerts: List<AlertEntity>)
    fun setPermissions(gps: Boolean, camera: Boolean)
    fun setDistance(distance: Int)
    fun setRouteProgress(visited: Int, total: Int)
    fun simulateApproach()
    fun resetDemo()
    fun completeArrival()
    fun updateHaptics(enabled: Boolean, strength: HapticStrength)
    fun refreshNearbyTargets()
    fun setSyncTarget(id: String, name: String, lat: Double, lng: Double)
    fun setSyncRoute(title: String, waypoints: List<Waypoint>)
    fun clearActiveRoute()
    fun markRouteCompleted()
    fun dismissRouteCompleted()
    fun selectNextAutoTarget()
    fun selectPreviousAutoTarget()
    fun deleteAlert(id: String)
    fun clearAllAlerts()
    fun updateCurrentLocation(lat: Double, lng: Double)
    fun updateHeading(heading: Float)
}
```

---

## FASE 3: Capa de Datos y Sensores del Reloj (Data Layer)

### Paso 3.1: Controlador Háptico (`data/haptics/HapticController.kt`)

Gestiona el actuador vibratorio del reloj mediante `Vibrator` o `VibratorManager` (Android 12+), ofreciendo patrones de pulsación táctil según el tipo de evento y la intensidad configurada.

```kotlin
enum class HapticPulse {
    LINKED,
    TOGGLE,
    NEARBY,
    ARRIVED
}

class HapticController(context: Context) {
    private val appContext = context.applicationContext

    fun pulse(type: HapticPulse, strength: HapticStrength) {
        val vibrator = getVibrator()
        if (!vibrator.hasVibrator()) return

        val duration = when (type) {
            HapticPulse.LINKED -> 80L
            HapticPulse.TOGGLE -> 50L
            HapticPulse.NEARBY -> 120L
            HapticPulse.ARRIVED -> 200L
        }
        val amplitude = when (strength) {
            HapticStrength.LOW -> 80
            HapticStrength.MEDIUM -> 170
            HapticStrength.HIGH -> 255
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, amplitude)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
```

---

### Paso 3.2: Implementación del Repositorio (`data/repository/RadarRepositoryImpl.kt`)

Coordina la persistencia en Room (`EcoGuiaDatabase`), el repositorio remoto (`EcoGuiaRepositoryImpl`), el controlador háptico y el filtro pasabajas para suavizar la rotación del compás.

```kotlin
class RadarRepositoryImpl(context: Context) : RadarRepository {
    internal val db = EcoGuiaDatabase.getDatabase(context)
    internal val dao = db.dao()
    internal val remoteRepository = EcoGuiaRepositoryImpl()
    internal val hapticController = HapticController(context)
    internal val scope = CoroutineScope(Dispatchers.IO)

    internal val _radarState = MutableStateFlow(RadarUiState())
    override val radarState: StateFlow<RadarUiState> = _radarState.asStateFlow()

    internal var currentLat: Double = 0.0
    internal var currentLng: Double = 0.0
    private var smoothedHeading: Float = 0f

    init {
        initStealthModeListener()
        initAlertsListener()
    }

    override fun updateHeading(heading: Float) {
        val diff = Math.abs(heading - smoothedHeading)
        if (diff > EcoGuiaConfig.COMPASS_HEADING_THRESHOLD_DEGREES) {
            val factor = EcoGuiaConfig.COMPASS_SMOOTHING_FACTOR
            smoothedHeading += factor * (heading - smoothedHeading)
            _radarState.update { it.copy(currentHeading = smoothedHeading) }
        }
    }
}
```

---

### Paso 3.3: Extensiones Modulares de Lógica (`data/repository/extensions/`)

Para mantener una arquitectura limpia y desacoplada, la lógica especializada se organiza en tres funciones de extensión:

1. **`RadarAlertsExt.kt`:** Administra el almacenamiento persistente de alertas en Room, la purga automática de notificaciones de más de 3 horas y la emisión de avisos de proximidad con throttle de 30 minutos por sitio.
2. **`RadarAutoSearchExt.kt`:** Consulta los sitios turísticos en SQLite o Neon PostgreSQL y selecciona automáticamente el objetivo más cercano calculando la distancia Haversine y el ángulo de rumbo geodésico (*bearing*).
3. **`RadarRouteSyncExt.kt`:** Sincroniza rutas guiadas con múltiples waypoints, actualiza el progreso paso a paso y emite vibraciones hápticas y notificaciones de llegada (`ARRIVED`) al acercarse a menos de 30 metros del hito activo.

---

### Paso 3.4: Manejo de Sensores y FusedLocation (`data/wear/`)

* **`LocationHelper.kt`:** Inicializa `FusedLocationProviderClient` solicitando ubicaciones de alta precisión (`Priority.PRIORITY_HIGH_ACCURACY`) con balance de consumo de batería.
* **`SensorHelper.kt`:** Registra el sensor `TYPE_ROTATION_VECTOR` y calcula la matriz de orientación del dispositivo respecto al campo magnético terrestre para generar el azimut continuo.

---

### Paso 3.5: Comunicación Wearable Data Layer (`data/wear/`)

* **`PhoneMessageClient.kt`:** Envía mensajes RPC al teléfono emparejado mediante `Wearable.getMessageClient()` (e.g. `/wear-arrival`, `/wear-location-update`).
* **`WearMessageListener.kt`:** Procesa los mensajes recibidos desde el teléfono decodificando payloads JSON (rutas completas `/eco-route-sync`, objetivo seleccionado `/eco-site-selected`, o alerta de proximidad `/eco-proximity-alert`).
* **`EcoWearMessageService.kt`:** Hereda de `WearableListenerService` permitiendo despertar la app de reloj y registrar datos recibidos aun cuando la pantalla se encuentre suspendida.

---

## FASE 4: Capa de Presentación (Presentation Layer & UI)

### Paso 4.1: Sistema de Diseño y Tokens Wear (`presentation/theme/EcoGuiaWearTheme.kt`)

La paleta cromática utiliza negro profundo (`#050B10`) para optimizar el consumo en displays AMOLED, con acentos en Jade colonial (`#26A69A`) y Oro histórico (`#C5A059`).

```kotlin
object EcoGuiaColors {
    val Background = Color(0xFF050B10)
    val Surface = Color(0xFF0E2A3F)
    val DeepBlue = Color(0xFF05111A)
    val Gold = Color(0xFFC5A059)
    val Jade = Color(0xFF26A69A)
    val Text = Color(0xFFF7FAFC)
    val Muted = Color(0xFFB8C6D1)
    val Alert = Color(0xFFE4B84A)
}

@Composable
fun EcoGuiaWearTheme(content: @Composable () -> Unit) {
    val colorScheme = ColorScheme(
        primary = EcoGuiaColors.Jade,
        onPrimary = EcoGuiaColors.Background,
        secondary = EcoGuiaColors.Gold,
        onSecondary = EcoGuiaColors.Background,
        background = EcoGuiaColors.Background,
        onBackground = EcoGuiaColors.Text,
        surfaceContainer = EcoGuiaColors.Surface,
        onSurface = EcoGuiaColors.Text,
        error = EcoGuiaColors.Alert,
        onError = EcoGuiaColors.Background
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

---

### Paso 4.2: Componentes Reutilizables de Reloj (`presentation/components/`)

1. **`EcoWearScaffold.kt`:** Envoltorio base para pantallas de reloj que integra `AppScaffold`, `TimeText` y `ScalingLazyColumn` con soporte de scroll rotatorio para la corona física.
2. **`CompassArrow.kt`:** Renderiza la aguja de navegación apuntando dinámicamente según la diferencia angular entre la orientación del reloj y el azimut del objetivo histórico.
3. **`CircularStatus.kt`:** Indicador circular de progreso que ilustra el porcentaje de avance de la ruta o la distancia remanente hacia el destino.

---

### Paso 4.3: State Management con `RadarViewModel.kt`

El `RadarViewModel` expone el estado inmutable `radarState` y conecta la interacción de los composables con las operaciones del `RadarRepository`.

```kotlin
class RadarViewModel(
    private val repository: RadarRepository,
    private val phoneMessageClient: PhoneMessageClient
) : ViewModel() {

    val state: StateFlow<RadarUiState> = repository.radarState

    fun toggleRadar() = repository.toggleRadar()
    fun toggleStealthMode() = repository.toggleStealthMode()
    fun updateHaptics(enabled: Boolean, strength: HapticStrength = HapticStrength.MEDIUM) =
        repository.updateHaptics(enabled, strength)
    fun selectNextAutoTarget() = repository.selectNextAutoTarget()
    fun selectPreviousAutoTarget() = repository.selectPreviousAutoTarget()
    fun completeArrival() = repository.completeArrival()
    fun dismissRouteCompleted() = repository.dismissRouteCompleted()
    fun deleteAlert(id: String) = repository.deleteAlert(id)
    fun clearAllAlerts() = repository.clearAllAlerts()

    fun openPhoneCamera() {
        phoneMessageClient.notifyOpenPhoneCamera()
    }
}
```

---

### Paso 4.4: Navegación y Paginador Horizontal (`presentation/navigation/`)

La aplicación implementa navegación por gestos con `SwipeDismissableNavHost` y un carrusel táctil de 4 páginas (`HorizontalPager`):

```kotlin
@Composable
fun RadarPagerScreen(
    viewModel: RadarViewModel,
    onNavigateToPairing: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isRouteActive = state.routeSummary.waypoints.isNotEmpty()
    val pageCount = if (isRouteActive) 4 else 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> StealthRadarScreen(state = state, onToggleStealth = viewModel::toggleStealthMode, ...)
                1 -> RadarScreen(state = state, onToggleRadar = viewModel::toggleRadar, ...)
                2 -> CompassScreen(state = state, ...)
                3 -> if (isRouteActive) RouteSummaryScreen(state = state, ...)
            }
        }

        HorizontalPageIndicator(
            pageIndicatorState = object : PageIndicatorState {
                override val pageCount: Int = pageCount
                override val pageOffset: Float = pagerState.currentPageOffsetFraction
                override val selectedPage: Int = pagerState.currentPage
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

---

### Paso 4.5: Catálogo de Pantallas Wear (`presentation/screens/`)

| Pantalla | Archivo | Propósito y Función |
|---|---|---|
| **Pairing** | `PairingScreen.kt` | Diagnóstico de conexión inicial: estado de enlace con el smartphone, GPS y cámara. |
| **Radar Principal** | `RadarScreen.kt` | Vista central del radar con aguja de rumbo, distancia en metros y selector de sitios cercanos. |
| **Brújula** | `CompassScreen.kt` | Aguja ampliada con rotación en tiempo real para orientación direccional en caminatas. |
| **Modo Discreto** | `StealthRadarScreen.kt` | Navegación a ciegas con pantalla atenuada y guía exclusiva por pulsos hápticos. |
| **Historial de Alertas** | `AlertsScreen.kt` | Carrusel horizontal de notificaciones de sitios y geodrops con soporte de borrado. |
| **Llegada** | `ArrivalScreen.kt` | Diálogo de llegada a un hito (0 m) con botón para activar visor AR en el teléfono. |
| **Resumen de Ruta** | `RouteSummaryScreen.kt` | Indicador circular con progreso de paradas visitadas respecto al total (ej. 3/8). |
| **Ruta Completada** | `RouteCompletedWearScreen.kt` | Diálogo modal de felicitación con trofeo al culminar todas las paradas del recorrido. |
| **Ajustes Hápticos** | `HapticSettingsScreen.kt` | Selector de niveles de intensidad de vibración (Suave, Media, Alta). |
| **Sitio Cercano** | `SiteNearbyScreen.kt` | Alerta compacta al ingresar al perímetro de un nuevo sitio patrimonial. |
| **Alerta Proximidad** | `ProximityAlertScreen.kt` | Notificación interactiva con accesos rápidos para inspeccionar el punto de interés. |

---

### Paso 4.6: Actividad Principal (`presentation/MainActivity.kt`)

`MainActivity` coordina el ciclo de vida del reloj, registra los listeners de sensores y Play Services Wearable, e inicializa el grafo de navegación `EcoGuiaWearNavGraph`.

```kotlin
class MainActivity : ComponentActivity() {

    private lateinit var sensorHelper: SensorHelper
    private lateinit var locationHelper: LocationHelper
    private lateinit var phoneMessageClient: PhoneMessageClient
    private lateinit var radarRepository: RadarRepository
    private lateinit var radarViewModel: RadarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sensorHelper = SensorHelper(this)
        locationHelper = LocationHelper(this)
        phoneMessageClient = PhoneMessageClient(this)
        radarRepository = RadarRepositoryImpl(this)
        radarViewModel = RadarViewModel(radarRepository, phoneMessageClient)

        setContent {
            EcoGuiaWearTheme {
                EcoGuiaWearNavGraph(viewModel = radarViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorHelper.startListening { heading ->
            radarRepository.updateHeading(heading)
        }
        locationHelper.startLocationUpdates { lat, lng ->
            radarRepository.updateCurrentLocation(lat, lng)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorHelper.stopListening()
        locationHelper.stopLocationUpdates()
    }
}
```

---

## FASE 5: Integración y Comunicación Multidispositivo

### Canales de Comunicación Wearable Data Layer

| Path de Mensaje | Origen ➔ Destino | Payload | Acción Ejecutada |
|---|---|---|---|
| `/eco-site-selected` | Mobile ➔ Wear | JSON con `id`, `nombre`, `lat`, `lng` | Establece el nuevo objetivo activo en el radar. |
| `/eco-route-sync` | Mobile ➔ Wear | JSON con `title` y `waypoints[]` | Sincroniza la ruta turística guiada en el reloj. |
| `/eco-proximity-alert` | Mobile ➔ Wear | String con mensaje de proximidad | Dispara alerta visual y vibración táctil en el reloj. |
| `/wear-arrival` | Wear ➔ Mobile | `siteId` (String) | Notifica al teléfono la llegada física del usuario. |
| `/wear-open-camera` | Wear ➔ Mobile | Empty / Ping | Solicita al teléfono iniciar la cámara AR de Geo-Drops. |
| `/wear-location-update` | Wear ➔ Mobile | Latitud y Longitud | Comparte coordenadas precisas del reloj al teléfono. |

---

## Desarrolladores y Créditos

* **Zahir Andrés Rodríguez Mora** — *Desarrollador Principal & Arquitectura Android*
* **Cesar Enrique Garay García** — *Desarrollador & Integración Wear OS*

**Institución:** Universidad Tecnológica del Norte de Guanajuato (UTNG) — *Ingeniería en Desarrollo y Gestión de Software*
