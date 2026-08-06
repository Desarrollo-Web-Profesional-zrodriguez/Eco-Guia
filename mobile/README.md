# Guía Paso a Paso: Construyendo el Módulo Mobile de Eco-Guía

Esta guía documenta y desglosa paso a paso la arquitectura, configuración y construcción completa del módulo **Mobile (Android Phone)** de **Eco-Guía Dolores Hidalgo**, explicando las decisiones técnicas, patrones de diseño y bloques de código esenciales.

---

## Objetivo de Esta Guía

Al estudiar y seguir esta guía, comprenderás:

1. Cómo estructurar un proyecto Android profesional moderno con **Kotlin 2.1** y **Jetpack Compose (Material 3)**.
2. Cómo implementar la arquitectura **MVVM (Model-View-ViewModel)** con flujo unidireccional de datos (*Unidirectional Data Flow - UDF*) y estados reactivos (`StateFlow`, `State`).
3. Cómo integrar servicios de hardware avanzados: **Google Maps Compose**, **CameraX**, geolocalización en primer plano (**Foreground Services** con `FusedLocationProviderClient`) y notificaciones persistentes.
4. Cómo orquestar la comunicación multicanal: integración de base de datos remota **Neon Serverless PostgreSQL**, correos transaccionales con **Brevo API v3**, sincronización con **Wear OS Data Layer** y conectividad IoT con **Smart TVs**.

---

## FASE 1: Configuración Inicial del Entorno y Build System

### Paso 1.1: Configurar el Catálogo de Versiones (`gradle/libs.versions.toml`)

El catálogo de dependencias centraliza las versiones y librerías utilizadas en todos los submódulos del proyecto (`:shared`, `:mobile`, `:wear`, `:tv`).

```toml
[versions]
agp = "8.8.0"
kotlin = "2.1.0"
composeBom = "2025.01.00"
coreKtx = "1.15.0"
lifecycle = "2.8.7"
navigationCompose = "2.8.5"
playServicesLocation = "21.3.0"
playServicesMaps = "19.0.0"
mapsCompose = "6.4.0"
camerax = "1.4.1"
ktor = "3.0.3"
room = "2.6.1"
work = "2.10.0"
playServicesWearable = "19.0.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
maps-compose = { group = "com.google.maps.android", name = "maps-compose", version.ref = "mapsCompose" }
play-services-wearable = { group = "com.google.android.gms", name = "play-services-wearable", version.ref = "playServicesWearable" }
```

> **CONCEPTO CLAVE:** El archivo `libs.versions.toml` permite versionar dependencias de manera determinista, evitando conflictos entre módulos y asegurando que las versiones de Compose y Kotlin permanezcan alineadas mediante el BOM (*Bill of Materials*).

---

### Paso 1.2: Configurar `mobile/build.gradle.kts`

El archivo de configuración del módulo `mobile` gestiona la inyección de credenciales seguras (como la API Key de Brevo) y enlaza el módulo de lógica compartida `:shared`.

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// Lectura segura de credenciales desde local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}
val brevoApiKey = localProperties.getProperty("BREVO_API_KEY") ?: ""

android {
    namespace = "mx.utng.ecoguiawear"
    compileSdk = 35

    defaultConfig {
        applicationId = "mx.utng.ecoguiawear"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BREVO_API_KEY", "\"$brevoApiKey\"")
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // Hardware & Servicios
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)
    implementation(libs.play.services.wearable)

    // Red y Serialización
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}
```

> **CONCEPTO CLAVE:** `buildConfigField` inyecta valores en tiempo de compilación a la clase `BuildConfig`, permitiendo que clases como `EmailService` utilicen claves de API sin exponerlas en el control de versiones público.

---

### Paso 1.3: Configurar Permisos y Componentes en `AndroidManifest.xml`

Para permitir la geolocalización en segundo plano, la captura de fotografías y el soporte para Wear OS, declaramos los permisos y componentes del sistema:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permisos de Red y Conectividad -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Permisos de Geolocalización -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

    <!-- Permisos de Cámara y Notificaciones -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.EcoGuiaWear">

        <!-- Actividad Principal -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Servicio en Primer Plano de Monitoreo GPS -->
        <service
            android:name=".data.ProximityService"
            android:exported="false"
            android:foregroundServiceType="location" />

        <!-- Receptor de Reinicio del Sistema -->
        <receiver
            android:name=".data.BootReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

        <!-- Listener de Comunicación con Wear OS -->
        <service
            android:name=".data.wear.MobileWearListenerService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
                <data android:scheme="wear" android:host="*" android:pathPrefix="/wear" />
            </intent-filter>
        </service>

    </application>
</manifest>
```

> **CONCEPTO CLAVE:** En Android 14+ (API 34+), todo `ForegroundService` debe declarar explícitamente su tipo (`foregroundServiceType="location"`), de lo contrario el sistema arrojará una `SecurityException` en tiempo de ejecución.

---

## FASE 2: Capa de Datos y Servicios de Red

```
data/
├── BootReceiver.kt
├── ProximityNotificationHelper.kt
├── ProximityService.kt
├── remote/
│   ├── EmailService.kt
│   └── FirebaseStorageRepository.kt
└── wear/
    ├── MobileWearListenerService.kt
    └── WearMessageClient.kt
```

### Paso 2.1: Implementar el Servicio de Correo Transaccional (`EmailService.kt`)

`EmailService` se comunica directamente con la API REST v3 de Brevo para enviar códigos OTP de verificación de cuenta y recuperación de contraseñas.

```kotlin
package mx.utng.ecoguiawear.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mx.utng.ecoguiawear.BuildConfig

@Serializable
data class SendSmtpEmail(
    val sender: EmailSender,
    val to: List<EmailRecipient>,
    val subject: String,
    val htmlContent: String
)

@Serializable
data class EmailSender(val name: String, val email: String)

@Serializable
data class EmailRecipient(val email: String, val name: String? = null)

class EmailService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun sendVerificationEmail(recipientEmail: String, recipientName: String, otp: String): Boolean {
        val apiKey = BuildConfig.BREVO_API_KEY
        if (apiKey.isBlank()) return false

        val html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: #2E7D32;">Eco-Guía Dolores Hidalgo</h2>
                <p>Hola <strong>$recipientName</strong>,</p>
                <p>Tu código de verificación para completar el registro es:</p>
                <div style="font-size: 28px; font-weight: bold; color: #1B5E20; letter-spacing: 5px; text-align: center; padding: 15px; background: #E8F5E9; border-radius: 6px;">
                    $otp
                </div>
                <p style="color: #757575; font-size: 12px; margin-top: 20px;">Este código expira en 15 minutos.</p>
            </div>
        """.trimIndent()

        val payload = SendSmtpEmail(
            sender = EmailSender(name = "Eco-Guía Dolores", email = "no-reply@ecoguia.mx"),
            to = listOf(EmailRecipient(email = recipientEmail, name = recipientName)),
            subject = "Código de Verificación - Eco-Guía",
            htmlContent = html
        )

        return try {
            val response = client.post("https://api.brevo.com/v3/smtp/email") {
                header("api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}
```

> **CONCEPTO CLAVE:** El uso de Ktor Client con serialización JSON nativa de Kotlin (`kotlinx.serialization`) asegura una huella de memoria ligera y un rendimiento óptimo sin depender de librerías pesadas.

---

### Paso 2.2: Implementar el Servicio en Primer Plano de Monitoreo GPS (`ProximityService.kt`)

Este servicio evalúa constantemente la proximidad del usuario con respecto a los sitios históricos registrados y dispara alertas hápicas y notificaciones cuando entra a su radio de cobertura.

```kotlin
package mx.utng.ecoguiawear.data

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.HistoricalSite
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository

class ProximityService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl()

    private var sitesList: List<HistoricalSite> = emptyList()
    private val notifiedSiteIds = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        ProximityNotificationHelper.createNotificationChannel(this)
        startForeground(
            ProximityNotificationHelper.NOTIFICATION_ID,
            ProximityNotificationHelper.buildForegroundNotification(this)
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        cargarSitiosHistoricos()
        iniciarRastreoUbicacion()
    }

    private fun cargarSitiosHistoricos() {
        serviceScope.launch {
            try {
                sitesList = repository.getHistoricalSites()
            } catch (e: Exception) {
                // Manejo de error de red
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun iniciarRastreoUbicacion() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    verificarProximidad(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun verificarProximidad(userLocation: Location) {
        for (site in sitesList) {
            val siteLoc = Location("").apply {
                latitude = site.latitude
                longitude = site.longitude
            }
            val distanciaMetros = userLocation.distanceTo(siteLoc)

            if (distanciaMetros <= site.radiusMeters && !notifiedSiteIds.contains(site.id)) {
                notifiedSiteIds.add(site.id)
                ProximityNotificationHelper.mostrarAlertaSitio(this, site, distanciaMetros.toInt())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```

> **CONCEPTO CLAVE:** `SupervisorJob` previene que la falla de una corrutina individual (como una petición de red lenta al obtener sitios) cancele el scope completo del servicio en primer plano.

---

## FASE 3: Capa de Estado y ViewModels (MVVM)

```
ui/viewmodel/
├── AuthViewModel.kt
├── LocationViewModel.kt
├── CollectionViewModel.kt
├── RouteViewModel.kt
├── ChatViewModel.kt
├── SiteRegistrationViewModel.kt
├── ModerationViewModel.kt
├── NotificationViewModel.kt
├── UserManagementViewModel.kt
└── GeoDropViewModel.kt
```

### Paso 3.1: ViewModel de Autenticación y Flujo OTP (`AuthViewModel.kt`)

Controla el ciclo de vida del usuario, inicio de sesión contra Neon DB, flujo de registro con verificación de 6 dígitos y recuperación de cuenta.

```kotlin
package mx.utng.ecoguiawear.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguia.shared.domain.repository.EcoGuiaRepository
import mx.utng.ecoguiawear.data.remote.EmailService

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: RemoteUser) : AuthState()
    data class Error(val message: String) : AuthState()
    data class AwaitingVerification(val name: String, val email: String, val passwordHash: String, val expectedOtp: String) : AuthState()
    data class AwaitingPasswordReset(val email: String, val expectedOtp: String) : AuthState()
    object Registered : AuthState()
    object PasswordResetSuccess : AuthState()
}

class AuthViewModel(
    private val repository: EcoGuiaRepository = EcoGuiaRepositoryImpl(),
    private val emailService: EmailService = EmailService()
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    var currentUser: RemoteUser? = null
        private set

    fun initiateRegistration(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val otp = (100000..999999).random().toString()
            val emailSent = emailService.sendVerificationEmail(email, name, otp)
            if (emailSent) {
                _authState.value = AuthState.AwaitingVerification(name, email, pass, otp)
            } else {
                _authState.value = AuthState.Error("No se pudo enviar el correo de verificación.")
            }
        }
    }

    fun verifyAndCompleteRegistration(enteredOtp: String) {
        val state = _authState.value as? AuthState.AwaitingVerification ?: return
        if (enteredOtp == state.expectedOtp) {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                val user = repository.registerUser(state.name, state.email, state.passwordHash)
                if (user != null) {
                    currentUser = user
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Error al registrar en base de datos.")
                }
            }
        } else {
            _authState.value = AuthState.Error("Código OTP incorrecto.")
        }
    }
}
```

> **CONCEPTO CLAVE:** Los estados sellados (`sealed class`) garantizan exhaustividad en el compilador al representarse en la UI mediante sentencias `when (authState) { ... }`.

---

## FASE 4: Componentes Reutilizables y Sistema de Diseño

```
ui/
├── theme/
│   ├── Color.kt
│   └── Theme.kt
└── components/
    ├── CommonComponents.kt
    ├── BottomMenu.kt
    ├── EcoNavigation.kt
    ├── AdminNavigation.kt
    └── GeoDropSavingDialog.kt
```

### Paso 4.1: Definición de Paleta y Tema Visual (`Theme.kt` & `Color.kt`)

Eco-Guía utiliza tonos orgánicos y coloniales inspirados en Dolores Hidalgo (Cuna de la Independencia):

```kotlin
package mx.utng.ecoguiawear.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object EcoGuiaColors {
    val GreenPrimary = Color(0xFF2E7D32)
    val GreenDark = Color(0xFF1B5E20)
    val GreenAccent = Color(0xFF81C784)
    val GoldColonial = Color(0xFFC5A059)
    val BackgroundDark = Color(0xFF121212)
    val SurfaceDark = Color(0xFF1E1E1E)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0BEC5)
    val ErrorRed = Color(0xFFCF6679)
}

private val DarkColorScheme = darkColorScheme(
    primary = EcoGuiaColors.GreenPrimary,
    secondary = EcoGuiaColors.GoldColonial,
    background = EcoGuiaColors.BackgroundDark,
    surface = EcoGuiaColors.SurfaceDark,
    error = EcoGuiaColors.ErrorRed
)

@Composable
fun EcoGuiaMobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
```

---

### Paso 4.2: Componentes Reutilizables (`CommonComponents.kt`)

Componentes estándar como botones con estado de carga y campos de texto estilizados aseguran coherencia en más de 40 pantallas:

```kotlin
package mx.utng.ecoguiawear.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.utng.ecoguiawear.ui.theme.EcoGuiaColors

@Composable
fun EcoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = EcoGuiaColors.GreenPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
```

---

## FASE 5: Grafo de Navegación y Pantallas Principales

```
ui/
├── navigation/
│   └── AppNavHost.kt
└── screens/
    ├── SplashScreen.kt
    ├── LoginScreen.kt
    ├── SignUpScreen.kt
    ├── RecoveryScreen.kt
    ├── PermissionsScreen.kt
    ├── ExplorationScreen.kt
    ├── MyCollectionScreen.kt
    ├── MiguelHidalgoChatScreen.kt
    ├── CameraGeoDropScreen.kt
    └── ActiveRouteScreen.kt
```

### Paso 5.1: Orquestación del Grafo Central (`AppNavHost.kt`)

`AppNavHost` administra la navegación protegida por roles (Usuario regular, Administrador, Moderador, Museo):

```kotlin
package mx.utng.ecoguiawear.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.ecoguiawear.ui.screens.*
import mx.utng.ecoguiawear.ui.screens.admin.*
import mx.utng.ecoguiawear.ui.viewmodel.*

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val RECOVERY = "recovery"
    const val PERMISSIONS = "permissions"
    const val EXPLORATION = "exploration"
    const val COLLECTION = "collection"
    const val CHAT_HIDALGO = "chat_hidalgo"
    const val CAMERA_GEODROP = "camera_geodrop"
    const val ADMIN_SUMMARY = "admin_summary"
    const val USER_MANAGEMENT = "user_management"
    const val SITE_REGISTRATION = "site_registration"
    const val MODERATION_LIST = "moderation_list"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    collectionViewModel: CollectionViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onNavigateToHome = { navController.navigate(Routes.EXPLORATION) { popUpTo(Routes.SPLASH) { inclusive = true } } }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Routes.EXPLORATION) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                onRecoverClick = { navController.navigate(Routes.RECOVERY) }
            )
        }

        composable(Routes.EXPLORATION) {
            ExplorationScreen(
                locationViewModel = locationViewModel,
                onOpenSite = { siteId -> /* Abrir detalle */ },
                onNavigateToCamera = { navController.navigate(Routes.CAMERA_GEODROP) }
            )
        }

        composable(Routes.CHAT_HIDALGO) {
            MiguelHidalgoChatScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_SUMMARY) {
            AdminSummaryScreen(
                onNavigateToUsers = { navController.navigate(Routes.USER_MANAGEMENT) },
                onNavigateToModeration = { navController.navigate(Routes.MODERATION_LIST) },
                onNavigateToSiteReg = { navController.navigate(Routes.SITE_REGISTRATION) }
            )
        }
    }
}
```

---

### Paso 5.2: Pantalla de Exploración Cartográfica (`ExplorationScreen.kt`)

Integra **Google Maps Compose**, marcadores personalizados por categoría (Museo, Monumento, Parroquia) y el menú inferior reactivo.

```kotlin
package mx.utng.ecoguiawear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.utng.ecoguiawear.ui.components.BottomMenu
import mx.utng.ecoguiawear.ui.viewmodel.LocationViewModel

@Composable
fun ExplorationScreen(
    locationViewModel: LocationViewModel,
    onOpenSite: (String) -> Unit,
    onNavigateToCamera: () -> Unit
) {
    val doloresHidalgoCenter = LatLng(21.1561, -100.9325)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(doloresHidalgoCenter, 16f)
    }

    Scaffold(
        bottomBar = {
            BottomMenu(currentRoute = "exploration", onNavigate = { /* Navegación */ })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCamera,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+ GeoDrop")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                // Marcador Parroquia de Nuestra Señora de los Dolores
                Marker(
                    state = MarkerState(position = LatLng(21.1575, -100.9308)),
                    title = "Parroquia de Dolores",
                    snippet = "Cuna de la Independencia Nacional",
                    onClick = {
                        onOpenSite("parroquia_dolores")
                        true
                    }
                )
            }
        }
    }
}
```

---

## FASE 6: Módulo de Administración y Moderación Comunitaria

El módulo administrativo (`ui/screens/admin/`) permite a usuarios con roles `superadmin`, `moderator` o `museum_admin` realizar tareas de gobernanza cultural:

1. **`AdminSummaryScreen.kt`:** Panel de analíticas en tiempo real (visitas, GeoDrops registrados, estado de sincronización).
2. **`SiteRegistrationScreen.kt` (Asistente 4 pasos):**
   - **Paso 1:** Metadatos generales (Nombre, categoría, horario, costos).
   - **Paso 2:** Localización georreferenciada y radio de proximidad.
   - **Paso 3:** Contenido histórico y multimedia.
   - **Paso 4:** Confirmación y publicación en Neon DB.
3. **`ModerationListScreen.kt` / `ReportDecisionScreen.kt`:** Flujo de moderación con aprobación y rechazo fundado de cápsulas comunitarias.
4. **`TVCampaignScreen.kt`:** Difusión de contenido multimedia y difusión de cápsulas hacia Smart TVs mediante MQTT.

---

## Resumen de la Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    Capa de Presentación                     │
│   Jetpack Compose Screens  ──▶  Componentes Reutilizables   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observa State / StateFlow
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      Capa de ViewModel                      │
│   AuthViewModel, LocationViewModel, CollectionViewModel...  │
└──────────────────────────────┬──────────────────────────────┘
                               │ Invoca casos de uso / repos
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Módulo :shared (Dominio)                 │
│      EcoGuiaRepository ──▶ Neon PostgreSQL / Room Local     │
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌──────────────────────────────┐    ┌──────────────────────────────┐
│   Hardware & Sensores        │    │    Servicios Externos        │
│ • FusedLocation (GPS)        │    │ • Brevo API v3 (Emails OTP)  │
│ • CameraX (Fotografía)       │    │ • Groq AI (Miguel Hidalgo)   │
│ • Wearable Data Layer Client │    │ • HiveMQ (MQTT para TVs)     │
└──────────────────────────────┘    └──────────────────────────────┘
```

---

## Desarrolladores

- **Zahir Andrés Rodríguez Mora** — Arquitectura y Desarrollo Principal
- **Cesar Enrique Garay García** — Desarrollo e Integración Móvil
- **Institución:** Universidad Tecnológica del Norte de Guanajuato (UTNG)
