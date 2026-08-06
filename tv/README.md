# Guía Paso a Paso: Construyendo el Módulo Smart TV de Eco-Guía

Esta guía documenta y desglosa paso a paso la arquitectura, configuración y construcción completa del módulo **Smart TV (Android TV / Google TV)** de **Eco-Guía Dolores Hidalgo**, explicando las decisiones de diseño para interfaces de 10 pies (*10-foot UI*), el manejo de foco con D-Pad, la conectividad P2P e IoT (MQTT/Ktor Server), y la visualización inmersiva en mapas 3D.

---

## Objetivo de Esta Guía

Al estudiar y seguir esta guía, comprenderás:

1. Cómo estructurar una aplicación moderna para **Android TV** utilizando **Kotlin 2.1**, **Jetpack Compose for TV (`androidx.tv.material3`)** y navegación optimizada para control remoto.
2. Cómo implementar un sistema de **Modo Kiosco de Exhibición Pública** con bloqueo de navegación y liberación mediante PIN maestro.
3. Cómo configurar un servidor HTTP embebido con **Ktor/Netty (`TvLocalServer`)** en el puerto 8080 para comunicación de respaldo en red local (LAN P2P) y sincronización en la nube mediante **HiveMQ MQTT**.
4. Cómo integrar **Google Maps 3D** con perspectiva inclinada (*tilt 45°*), rotación orbital continua y estilos visuales personalizados (Maqueta Minimalista, Neón Futurista y Satelital).
5. Cómo implementar carruseles automatizados (*Slideshows*), rankings analíticos paginados y animaciones *Shimmer* / *Skeleton* para transiciones fluidas de red.

---

## FASE 1: Configuración Inicial del Entorno y Build System

### Paso 1.1: Configurar el Catálogo de Versiones (`gradle/libs.versions.toml`)

Android TV requiere dependencias específicas de Leanback y Compose for TV, además de motores ligeros para el servidor local.

```toml
[versions]
agp = "8.8.0"
kotlin = "2.1.0"
composeBom = "2025.01.00"
tvFoundation = "1.0.0-alpha12"
tvMaterial = "1.0.0"
playServicesMaps = "19.0.0"
mapsCompose = "6.4.0"
zxing = "3.5.3"
coil = "2.7.0"
ktor = "3.0.3"

[libraries]
androidx-tv-foundation = { group = "androidx.tv", name = "tv-foundation", version.ref = "tvFoundation" }
androidx-tv-material = { group = "androidx.tv", name = "tv-material", version.ref = "tvMaterial" }
play-services-maps = { group = "com.google.android.gms", name = "play-services-maps", version.ref = "playServicesMaps" }
maps-compose = { group = "com.google.maps.android", name = "maps-compose", version.ref = "mapsCompose" }
zxing-core = { group = "com.google.zxing", name = "core", version.ref = "zxing" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
ktor-server-core = { group = "io.ktor", name = "ktor-server-core-jvm", version.ref = "ktor" }
ktor-server-netty = { group = "io.ktor", name = "ktor-server-netty-jvm", version.ref = "ktor" }
ktor-server-cors = { group = "io.ktor", name = "ktor-server-cors-jvm", version.ref = "ktor" }
```

> **CONCEPTO CLAVE:** Compose for TV (`androidx.tv.material3`) proporciona componentes nativos como `Card`, `Button` e `Icon` con estados de foco (`focusedContainerColor`, `focusedContentColor`) diseñados para responder naturalmente al D-Pad del control remoto.

---

### Paso 1.2: Configurar `tv/build.gradle.kts`

El módulo `:tv` consume `:shared` para acceder al repositorio PostgreSQL de Neon y al cliente MQTT, e integra exclusiones de empaquetado para Netty.

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.utng.ecoguiawear.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "mx.utng.ecoguiawear.tv"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=androidx.tv.material3.ExperimentalTvMaterial3Api")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // Cartografía y Mapas
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    // Renderizado QR e Imágenes
    implementation(libs.zxing.core)
    implementation(libs.coil.compose)

    // Servidor Local Embebido (P2P LAN)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)

    implementation(libs.navigation.compose)
}
```

---

### Paso 1.3: Configurar `tv/src/main/AndroidManifest.xml`

En Android TV es mandatorio declarar la ausencia de pantalla táctil y el banner del launcher Leanback.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Declaración de compatibilidad con Android TV -->
    <uses-feature
        android:name="android.software.leanback"
        android:required="false" />
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <application
        android:allowBackup="true"
        android:hardwareAccelerated="true"
        android:largeHeap="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:banner="@drawable/ic_banner"
        android:usesCleartextTraffic="true"
        android:theme="@android:style/Theme.DeviceDefault.NoActionBar">

        <!-- Clave API de Google Maps -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="AIzaSyDexgWI7fqzfP8s4k_nkEvMFfYQz1qyWng" />

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.DeviceDefault.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

> **CONCEPTO CLAVE:** `android.intent.category.LEANBACK_LAUNCHER` registra la app en el carrusel principal de Google TV / Android TV. La propiedad `android:banner` (320x180 dp) sirve como portada gráfica en el menú del televisor.

---

## FASE 2: Sistema de Diseño Visual y Paleta Cromática (10-Foot UI)

### Paso 2.1: Paleta de Colores de Alto Contraste (`ui/theme/Color.kt`)

Las pantallas de Smart TV exigen contrastes intensos para garantizar legibilidad a una distancia de 3 a 5 metros.

```kotlin
package mx.utng.ecoguiawear.tv.ui.theme

import androidx.compose.ui.graphics.Color

val DeepBlue = Color(0xFF0F2C59)
val BrushedGold = Color(0xFFDAC0A3)
val JadeGreen = Color(0xFF0F5A3E)

val BackgroundDark = Color(0xFF081226)
val SurfaceDark = Color(0xFF142C52)
val TextPrimary = Color(0xFFF8F9FA)
val TextSecondary = Color(0xFFB0BEC5)
```

---

### Paso 2.2: Tema Global Material 3 para TV (`ui/theme/Theme.kt`)

```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package mx.utng.ecoguiawear.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val SmartTVColorScheme = darkColorScheme(
    primary = DeepBlue,
    onPrimary = TextPrimary,
    secondary = BrushedGold,
    onSecondary = BackgroundDark,
    tertiary = JadeGreen,
    onTertiary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary
)

@Composable
fun EcoGuiaTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SmartTVColorScheme,
        content = content
    )
}
```

---

## FASE 3: Servidor Local P2P y Generación de Códigos QR

### Paso 3.1: Servidor HTTP Embebido Netty (`network/TvLocalServer.kt`)

Cuando la TV y el móvil comparten la misma red WiFi pero no hay acceso a internet para el broker MQTT, la TV levanta un servidor HTTP local en el puerto 8080.

```kotlin
package mx.utng.ecoguiawear.tv.network

import android.util.Log
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.net.NetworkInterface
import java.util.Collections

class TvLocalServer(
    private val onPhotoReceived: (ByteArray) -> Unit = {}
) {
    private var server: NettyApplicationEngine? = null

    fun startServer(port: Int = 8080) {
        if (server != null) return
        try {
            server = embeddedServer(Netty, port = port) {
                install(CORS) {
                    anyHost()
                    allowHeader(HttpHeaders.ContentType)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Get)
                }
                routing {
                    get("/status") {
                        call.respondText("OK: Smart TV Eco-Guia en línea")
                    }
                    post("/upload-photo") {
                        val bytes = call.receive<ByteArray>()
                        onPhotoReceived(bytes)
                        call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                    }
                }
            }.start(wait = false)
        } catch (e: Exception) {
            Log.e("TvLocalServer", "Error al iniciar servidor: ${e.message}")
        }
    }

    fun stopServer() {
        server?.stop(1000, 2000)
        server = null
    }

    fun getLocalIpAddress(): String? {
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            val addrs = Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                    return addr.hostAddress
                }
            }
        }
        return null
    }
}
```

> **CONCEPTO CLAVE:** El servidor embebido Netty permite recibir transmisiones de fotos y comandos de pantalla sin requerir configuraciones de router o puertos abiertos hacia el exterior.

---

### Paso 3.2: Generador de Matriz QR con ZXing (`ui/screens/components/LobbyComponents.kt`)

Genera en tiempo de ejecución un `Bitmap` con el PIN de vinculación para escaneo directo desde la cámara móvil.

```kotlin
private fun generateQrBitmap(content: String, size: Int = 350): android.graphics.Bitmap {
    return try {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
    }
}
```

---

## FASE 4: Arquitectura de Navegación y Orquestación Kiosco

### Paso 4.1: Grafo de Navegación Smart TV (`ui/navigation/SmartTVNavHost.kt`)

El `SmartTVNavHost` mantiene el estado global del bloqueo kiosco y escucha los eventos de desvinculación remota enviados desde la app móvil vía MQTT.

```kotlin
package mx.utng.ecoguiawear.tv.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.utng.ecoguiawear.tv.ui.screens.GalleryScreen
import mx.utng.ecoguiawear.tv.ui.screens.HeatmapScreen
import mx.utng.ecoguiawear.tv.ui.screens.LobbyScreen
import mx.utng.ecoguiawear.tv.ui.screens.Portal360Screen

sealed class TVRoutes(val route: String) {
    object Lobby : TVRoutes("lobby")
    object Heatmap : TVRoutes("heatmap")
    object Portal360 : TVRoutes("portal_360")
    object Gallery : TVRoutes("gallery")
}

@Composable
fun SmartTVNavHost() {
    val navController = rememberNavController()
    var isKioskLocked by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = TVRoutes.Lobby.route
    ) {
        composable(TVRoutes.Lobby.route) {
            LobbyScreen(
                onNavigateToHeatmap = { navController.navigate(TVRoutes.Heatmap.route) },
                onNavigateToPortal360 = { navController.navigate(TVRoutes.Portal360.route) },
                onNavigateToGallery = { navController.navigate(TVRoutes.Gallery.route) }
            )
        }
        composable(TVRoutes.Portal360.route) {
            Portal360Screen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = { navController.popBackStack() }
            )
        }
        composable(TVRoutes.Gallery.route) {
            GalleryScreen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = { navController.popBackStack() }
            )
        }
        composable(TVRoutes.Heatmap.route) {
            HeatmapScreen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

### Paso 4.2: Diálogo de Desbloqueo de Modo Kiosco (`ui/screens/components/KioskUnlockDialog.kt`)

Evita que usuarios no autorizados salgan de la presentación interactiva mediante la intercepción de eventos `Back` / `Escape` y validación de PIN maestro.

```kotlin
@Composable
fun KioskUnlockDialog(
    onUnlockConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val inputFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        inputFocusRequester.requestFocus()
    }

    Popup(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                onClick = {},
                modifier = Modifier.width(460.dp).padding(24.dp),
                colors = CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Modo de Transmisión Bloqueado", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    BasicTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (pinInput in listOf("1234", "12345678", "admin")) {
                                onUnlockConfirmed()
                            } else {
                                errorMessage = "PIN incorrecto"
                            }
                        }),
                        modifier = Modifier.focusRequester(inputFocusRequester)
                    )
                }
            }
        }
    }
}
```

---

## FASE 5: Pantallas Principales de Exhibición

### Paso 5.1: `LobbyScreen.kt` — Hub de Emparejamiento y Sesión

Administra la persistencia de sesión local (`tv_session_prefs`), la sincronización en segundo plano con Neon DB y la escucha MQTT de comandos remotos como `/stream-start`.

```kotlin
@Composable
fun LobbyScreen(
    onNavigateToHeatmap: () -> Unit,
    onNavigateToPortal360: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    // 1. Obtener o generar PIN de 6 dígitos persistente
    val pairingCode = remember { getOrCreatePairingCode(prefs) }

    // 2. Escucha reactiva MQTT para comandos de transmisión del teléfono
    LaunchedEffect(pairingCode) {
        HiveMQManager.subscribe("/eco-guia/tv/$pairingCode/command") { payload ->
            when (payload) {
                "PORTAL_360" -> onNavigateToPortal360()
                "GALLERY" -> onNavigateToGallery()
                "HEATMAP" -> onNavigateToHeatmap()
            }
        }
    }
}
```

---

### Paso 5.2: `Portal360Screen.kt` — Mapa 3D Orbital

Renderiza el mapa de Dolores Hidalgo con perspectiva inclinada de 45°, ejecutando una rotación angular continua de 360° mediante corrutinas.

```kotlin
@Composable
fun Portal360Screen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(LatLng(21.1561, -100.9325)) // Jardín Principal de Dolores Hidalgo
            .zoom(17f)
            .tilt(45f) // Perspectiva 3D
            .bearing(0f)
            .build()
    }

    // Rotación automática continua de 360 grados
    LaunchedEffect(Unit) {
        while (true) {
            val currentBearing = cameraPositionState.position.bearing
            val newBearing = (currentBearing + 0.5f) % 360f
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder(cameraPositionState.position)
                        .bearing(newBearing)
                        .build()
                ),
                durationMs = 100
            )
            delay(100)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            rotationGesturesEnabled = false
        )
    )
}
```

---

### Paso 5.3: `GalleryScreen.kt` — Carrusel Automático de GeoDrops

Presenta fotografías de cápsulas culturales en alta resolución con transición cíclica automática cada 5 segundos.

```kotlin
@Composable
fun GalleryScreen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var geoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }

    // Transición automática cada 5 segundos
    LaunchedEffect(geoDrops) {
        if (geoDrops.isNotEmpty()) {
            while (true) {
                delay(5000)
                currentIndex = (currentIndex + 1) % geoDrops.size
            }
        }
    }
}
```

---

### Paso 5.4: `HeatmapScreen.kt` — Ranking Analítico Semanal

Divide las cápsulas más populares en bloques de 3 elementos con rotación temporizada cada 8 segundos y controles manuales por D-Pad.

```kotlin
@Composable
fun HeatmapScreen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val pageSize = 3
    var currentPageIndex by remember { mutableStateOf(0) }
    val totalPages = (rankingGeoDrops.size + pageSize - 1) / pageSize

    LaunchedEffect(totalPages) {
        if (totalPages > 1) {
            while (true) {
                delay(8000)
                currentPageIndex = (currentPageIndex + 1) % totalPages
            }
        }
    }
}
```

---

## FASE 6: Componentes Skeleton y Efecto Shimmer

### Paso 6.1: `SkeletonComponents.kt`

Proporciona retroalimentación visual inmediata con degradados lineales animados mientras se descargan datos desde Neon PostgreSQL o cuando la TV está en espera de vinculación.

```kotlin
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val shimmerColors = listOf(
        Color(0xFF1E293B),
        Color(0xFF334155),
        Color(0xFF475569),
        Color(0xFF334155),
        Color(0xFF1E293B)
    )

    val transition = rememberInfiniteTransition(label = "skeleton")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 400f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}
```

---

## FASE 7: Verificación, Pruebas y Despliegue

### 1. Configurar un Emulador Android TV
1. En Android Studio, abre **Device Manager** > **Create Virtual Device**.
2. Selecciona la categoría **TV** > **Android TV (1080p)**.
3. Elige la imagen del sistema **Android 14 (API 34)** o superior con Google APIs.

### 2. Atajos de Navegación D-Pad en Emulador
- **Flechas de dirección:** Desplazamiento de foco (D-Pad Up/Down/Left/Right).
- **Enter / Return:** Selección / Clic (D-Pad Center).
- **Escape / Backspace:** Retroceso (`BackHandler`).

### 3. Prueba de Emparejamiento Móvil
1. Inicia la app TV: visualiza el código QR y el PIN de 6 dígitos.
2. Abre la app móvil `:mobile`, ve a **Mis Dispositivos > Vincular Pantalla Smart TV**.
3. Ingresa el PIN o escanea el QR.
4. Pulsa **Transmitir Mapa 3D** en el teléfono: la TV cambiará automáticamente a `Portal360Screen`.

---

## Desarrolladores

| Nombre | Rol |
| :--- | :--- |
| **Zahir Andrés Rodríguez Mora** | Desarrollador Principal |
| **Cesar Enrique Garay García** | Desarrollador |

**Institución:** Universidad Tecnológica del Norte de Guanajuato (UTNG)
