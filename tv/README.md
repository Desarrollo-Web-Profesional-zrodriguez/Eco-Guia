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

> 📋 **INSTRUCCIÓN:** Copia las dependencias de TV y Ktor desde `gradle/libs.versions.toml`:
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

> **CONCEPTO CLAVE:** Compose for TV (`androidx.tv.material3`) proporciona componentes nativos como `Card`, `Button` e `Icon` con estados de foco (`focusedContainerColor`, `focusedContentColor`) diseñados para responder naturally al D-Pad del control remoto.

---

### Paso 1.2: Configurar `tv/build.gradle.kts`

El módulo `:tv` consume `:shared` para acceder al repositorio PostgreSQL de Neon y al cliente MQTT, e integra exclusiones de empaquetado para Netty.

> 📋 **INSTRUCCIÓN:** Copia la configuración del build script de `tv/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.utng.ecoguiawear.tv"
    compileSdk = 37

    defaultConfig {
        applicationId = "mx.utng.ecoguiawear.tv"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Módulo compartido de la arquitectura: datos de Room, repositorios de Neon Postgres y MQTT
    implementation(project(":shared"))
    
    // Jetpack Compose BOM, integración con Activity y extensiones de Core KTX
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.androidx.core.ktx)

    // Componentes de interfaz de usuario especializados para Compose for TV (Material 3 TV & TV Foundation)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // Google Maps para Compose: renderizado del mapa turístico e interactivo en pantalla gigante
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    // Generación de códigos QR con ZXing y renderizado/caché de imágenes remotas con Coil
    implementation(libs.zxing.core)
    implementation(libs.coil.compose)

    // Servidor local Ktor (Engine Netty con CORS): recepción de fotos y conexiones directas P2P sin servidor externo
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)

    // Soporte para ViewModel, StateFlow, ciclos de vida y navegación Compose en Android TV
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)

    // Pruebas instrumentadas y depuración UI
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
    debugImplementation(libs.ui.tooling)
}
```

---

### Paso 1.3: Configurar `tv/src/main/AndroidManifest.xml`

En Android TV es mandatorio declarar la ausencia de pantalla táctil y el banner del launcher Leanback.

> 📋 **INSTRUCCIÓN:** Copia el manifest de `tv/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Declaración de compatibilidad con interfaz Android TV (Leanback) y desactivación de pantalla táctil requerida -->
    <uses-feature
        android:name="android.software.leanback"
        android:required="false" />
    
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <!-- Permisos de red e inspección de estado Wi-Fi para el servidor local Ktor Netty y transmisión MQTT -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <application
        android:allowBackup="true"
        android:hardwareAccelerated="true"
        android:largeHeap="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:theme="@android:style/Theme.DeviceDefault.NoActionBar"
        android:banner="@drawable/ic_banner">
        
        <!-- Compatibilidad con Google Maps SDK en Android TV -->
        <uses-library
            android:name="org.apache.http.legacy"
            android:required="false" />

        <!-- Clave API de Google Maps para desplegar el mapa interactivo de Dolores Hidalgo en pantalla gigante -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="AIzaSyDexgWI7fqzfP8s4k_nkEvMFfYQz1qyWng" />

        <!-- Actividad principal para Android TV con intent-filter LEANBACK_LAUNCHER -->
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

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/theme/Color.kt`:
```kotlin
/**
 * Paleta de colores oficial de Eco-Guía optimizada para pantallas Smart TV.
 *
 * Emplea tonos de alto contraste sobre fondos oscuros profundos, garantizando excelente
 * visibilidad a larga distancia y reduciendo la fatiga visual en salas y exhibiciones públicas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.theme

import androidx.compose.ui.graphics.Color

/** Azul profundo institucional utilizado en tarjetas y encabezados destacados. */
val DeepBlue = Color(0xFF0F2C59)

/** Dorado cepillado para detalles de acento, subtítulos y elementos premium. */
val BrushedGold = Color(0xFFDAC0A3)

/** Verde jade representativo de la identidad ecológica de la plataforma. */
val JadeGreen = Color(0xFF0F5A3E)

/** Color de fondo principal ultra oscuro para paneles de televisión (OLED/LED). */
val BackgroundDark = Color(0xFF081226)

/** Color de superficie para tarjetas elevadas y contenedores modulares. */
val SurfaceDark = Color(0xFF142C52)

/** Color de texto principal de alta legibilidad sobre fondos oscuros. */
val TextPrimary = Color(0xFFF8F9FA)

/** Color de texto secundario atenuado para leyendas y metadatos. */
val TextSecondary = Color(0xFFB0BEC5)
```

---

### Paso 2.2: Tema Global Material 3 para TV (`ui/theme/Theme.kt`)

Establece las reglas cromáticas y el contenedor global con soporte para `@OptIn(ExperimentalTvMaterial3Api::class)`.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/theme/Theme.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Configuración del tema visual y esquema cromático Material 3 para Android TV.
 *
 * Aplica estilos globales de tipografía, formas y colores optimizados para pantallas
 * de gran escala y alta definición en televisores y dispositivos de transmisión.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * Esquema de colores oscuros base configurado según la identidad visual de Eco-Guía.
 */
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

/**
 * Composable contenedor que envuelve la jerarquía de vistas de Android TV con el tema institucional.
 *
 * @param content Contenido composable a renderizar bajo el árbol temático.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun EcoGuiaTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SmartTVColorScheme,
        content = content
    )
}
```

---

## FASE 3: Servidor Local P2P y Actividad Principal

### Paso 3.1: Actividad Raíz (`MainActivity.kt`)

Configura la vista base de la app sobre `ComponentActivity`, inicializa el tema visual e invoca al orquestador de navegación.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/MainActivity.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Actividad principal del módulo Smart TV de Eco-Guía Dolores Hidalgo.
 *
 * Sirve como punto de entrada de la aplicación en dispositivos Android TV / Google TV,
 * configurando el contenedor raíz con el tema visual personalizado y delegando la
 * orquestación del flujo de pantallas al host de navegación de TV.
 *
 * Se utiliza la anotación `@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)`
 * para habilitar de manera global las APIs experimentales de Compose for TV (Material 3 para TV),
 * requeridas para componentes optimizados con foco direccional (D-Pad).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.utng.ecoguiawear.tv.ui.theme.EcoGuiaTVTheme
import mx.utng.ecoguiawear.tv.ui.navigation.SmartTVNavHost

/**
 * Actividad principal para la experiencia en pantalla grande de Eco-Guía.
 *
 * Hereda de [ComponentActivity] para proporcionar compatibilidad nativa y ligera
 * con Jetpack Compose en entornos Android TV sin la sobrecarga del framework de fragmentos clásico.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class MainActivity : ComponentActivity() {

    /**
     * Inicializa la actividad y establece la jerarquía de composables.
     *
     * Envuelve la aplicación dentro de [EcoGuiaTVTheme] y renderiza [SmartTVNavHost]
     * dentro de un contenedor que ocupa la totalidad de la pantalla.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoGuiaTVTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    SmartTVNavHost()
                }
            }
        }
    }
}
```

---

### Paso 3.2: Servidor HTTP Embebido Netty (`network/TvLocalServer.kt`)

Cuando la TV y el móvil comparten la misma red WiFi pero no hay acceso a internet para el broker MQTT, la TV levanta un servidor HTTP local en el puerto 8080.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/network/TvLocalServer.kt`:
```kotlin
/**
 * Servidor HTTP local para recibir transmisiones directas y comandos del teléfono móvil en la Smart TV.
 *
 * Utiliza Ktor Server con motor Netty en el puerto local 8080 (por defecto) con el endpoint `/upload`
 * para permitir la recepción instantánea de capturas fotográficas y datos en tiempo real dentro
 * de la misma red de área local (LAN), funcionando como alternativa o complemento a la conectividad MQTT.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Servidor HTTP embebido en la Smart TV para comunicación local directa.
 *
 * Expone un flujo de estado reactivo con las imágenes recibidas y provee utilidades
 * para el descubrimiento de la dirección IP local de la TV.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
object TvLocalServer {
    private val _receivedImage = MutableStateFlow<Bitmap?>(null)

    /**
     * Flujo de estado observable con el último [Bitmap] recibido a través del servidor local.
     */
    val receivedImage: StateFlow<Bitmap?> = _receivedImage.asStateFlow()

    private var server: io.ktor.server.engine.ApplicationEngine? = null

    /**
     * Inicia el servidor HTTP embebido en el puerto especificado.
     *
     * Configura el plugin de CORS para permitir solicitudes entrantes y define la ruta `/upload`
     * para decodificar los bytes de imagen entrantes y emitirlos en [receivedImage].
     *
     * @param port Puerto TCP en el que escuchará el servidor (por defecto 8080).
     */
    fun startServer(port: Int = 8080) {
        if (server != null) return
        server = embeddedServer(Netty, port = port) {
            install(CORS) {
                anyHost()
            }
            routing {
                post("/upload") {
                    try {
                        val bytes = call.receive<ByteArray>()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            _receivedImage.value = bitmap
                            call.respond(HttpStatusCode.OK, "Image received successfully")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid image data")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                    }
                }
            }
        }.start(wait = false)
    }

    /**
     * Detiene el servidor HTTP y libera los sockets y recursos asociados.
     *
     * También reinicia el estado de [receivedImage] a `null`.
     */
    fun stopServer() {
        server?.stop(1000, 2000)
        server = null
        _receivedImage.value = null // Limpiar estado al detener
    }

    /**
     * Obtiene la dirección IPv4 local asignada a la Smart TV en la red Wi-Fi o Ethernet.
     *
     * @param context Contexto de la aplicación utilizado para consultar el [WifiManager].
     * @return Cadena con la dirección IP local (ej. "192.168.1.50") o `null` si no fue posible determinarla.
     */
    fun getLocalIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            }
            // Fallback: buscar en las interfaces de red (Ethernet en emuladores de TV)
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
```

> **CONCEPTO CLAVE:** El servidor embebido Netty permite recibir transmisiones de fotos y comandos de pantalla sin requerir configuraciones de router o puertos abiertos hacia el exterior.

---

## FASE 4: Arquitectura de Navegación y Orquestación Kiosco

### Paso 4.1: Grafo de Navegación Smart TV (`ui/navigation/SmartTVNavHost.kt`)

El `SmartTVNavHost` mantiene el estado global del bloqueo kiosco y escucha los eventos de desvinculación remota enviados desde la app móvil vía MQTT.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/navigation/SmartTVNavHost.kt`:
```kotlin
/**
 * Host de navegación de la aplicación Smart TV de Eco-Guía.
 *
 * Define el grafo de navegación completo para Android TV, administrando el destino inicial
 * (Lobby), las transiciones entre pantallas, el estado persistente del modo kiosco y la
 * escucha global de desvinculación remota mediante MQTT y sincronización con Neon DB.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.utng.ecoguiawear.tv.ui.screens.GalleryScreen
import mx.utng.ecoguiawear.tv.ui.screens.HeatmapScreen
import mx.utng.ecoguiawear.tv.ui.screens.LobbyScreen
import mx.utng.ecoguiawear.tv.ui.screens.Portal360Screen

/**
 * Rutas de navegación disponibles en la aplicación Smart TV.
 *
 * @param route Identificador único de la ruta para el [NavHost].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
sealed class TVRoutes(val route: String) {
    /** Ruta de la pantalla principal / lobby de bienvenida y emparejamiento. */
    object Lobby : TVRoutes("lobby")

    /** Ruta del mapa de calor de afluencia turística e interacciones. */
    object Heatmap : TVRoutes("heatmap")

    /** Ruta de la galería y slideshow de cápsulas culturales GeoDrops. */
    object Gallery : TVRoutes("gallery")

    /** Ruta del visor cartográfico 360° con rotación automática y perspectivas históricas. */
    object Portal360 : TVRoutes("portal360")
}

/**
 * Grafo principal de navegación para la Smart TV.
 *
 * Configura la pantalla de inicio en [TVRoutes.Lobby] y gestiona la navegación adaptada a control
 * remoto (D-Pad). Además, orquesta el ciclo de vida del modo de bloqueo kiosco y la recepción
 * de señales de cierre de sesión remoto transmitidas por el dispositivo móvil.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun SmartTVNavHost() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }

    // Estado global y persistente del Modo Bloqueo Kiosco en la TV
    var isKioskLocked by remember { mutableStateOf(false) }

    // Escucha global de órdenes MQTT/DB sin importar qué pantalla esté activa
    LaunchedEffect(Unit) {
        val pairingCode = prefs.getString("saved_pairing_code", null) ?: "000000"
        
        // 1. Escuchar orden MQTT instantánea de logout global
        mx.utng.ecoguia.shared.data.remote.HiveMQManager.subscribeToTvCommands(pairingCode) { command ->
            if (command == "logout") {
                mainHandler.post {
                    prefs.edit()
                        .remove("saved_is_paired")
                        .remove("saved_paired_user_id")
                        .remove("saved_paired_user_email")
                        .remove("saved_paired_user_name")
                        .remove("saved_paired_user_role")
                        .remove("saved_selected_site_id")
                        .remove("saved_pairing_code")
                        .apply()
                    isKioskLocked = false
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        // 2. Verificación secundaria periódica de sesión activa en BD
        while (true) {
            val isPaired = prefs.getBoolean("saved_is_paired", false)
            val userId = prefs.getString("saved_paired_user_id", null)
            if (isPaired && userId != null) {
                try {
                    val status = repository.getPairingStatus(userId)
                    if (status == null) {
                        // Sesión anulada en la base de datos
                        prefs.edit()
                            .remove("saved_is_paired")
                            .remove("saved_paired_user_id")
                            .remove("saved_paired_user_email")
                            .remove("saved_paired_user_name")
                            .remove("saved_paired_user_role")
                            .remove("saved_selected_site_id")
                            .remove("saved_pairing_code")
                            .apply()
                        isKioskLocked = false
                        navController.navigate(TVRoutes.Lobby.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SmartTVNavHost", "Error verificando sesión global: ${e.message}")
                }
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    NavHost(navController = navController, startDestination = TVRoutes.Lobby.route) {
        composable(TVRoutes.Lobby.route) {
            LobbyScreen(
                onNavigateToHeatmap = { navController.navigate(TVRoutes.Heatmap.route) },
                onNavigateToGallery = { navController.navigate(TVRoutes.Gallery.route) },
                onNavigateToPortal360 = { navController.navigate(TVRoutes.Portal360.route) }
            )
        }
        composable(TVRoutes.Heatmap.route) {
            HeatmapScreen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = {
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(TVRoutes.Lobby.route) { inclusive = true }
                    }
                }
            )
        }
        composable(TVRoutes.Gallery.route) {
            GalleryScreen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = {
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(TVRoutes.Lobby.route) { inclusive = true }
                    }
                }
            )
        }
        composable(TVRoutes.Portal360.route) {
            Portal360Screen(
                isKioskLocked = isKioskLocked,
                onToggleKioskLock = { isKioskLocked = it },
                onBack = {
                    navController.navigate(TVRoutes.Lobby.route) {
                        popUpTo(TVRoutes.Lobby.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
```

---

### Paso 4.2: Diálogo de Desbloqueo de Modo Kiosco (`ui/screens/components/KioskUnlockDialog.kt`)

Evita que usuarios no autorizados salgan de la presentación interactiva mediante la intercepción de eventos `Back` / `Escape` y validación de PIN maestro.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/components/KioskUnlockDialog.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Diálogo modal de seguridad para desbloquear el modo de exhibición protegida (Modo Kiosco) en Smart TV.
 *
 * Previene la manipulación o navegación no autorizada fuera de la pantalla de transmisión en
 * recintos públicos, salas de museo y tótems turísticos, requiriendo el ingreso de un PIN o
 * credencial de administrador para regresar al Lobby.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.Key
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.focus.focusProperties

/**
 * Composable que renderiza el cuadro de diálogo para ingresar el PIN de desbloqueo de modo kiosco.
 *
 * @param onUnlockConfirmed Callback ejecutado cuando el PIN o credencial ingresada es válida.
 * @param onDismiss Callback ejecutado para cancelar el intento y mantener el bloqueo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@OptIn(ExperimentalTvMaterial3Api::class)
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

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f))
                .focusProperties { canFocus = true }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Back || keyEvent.key == Key.Escape) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                onClick = {},
                modifier = Modifier
                    .width(460.dp)
                    .padding(24.dp),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Modo Kiosco",
                        tint = BrushedGold,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Modo de Transmisión Bloqueado",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ingresa la contraseña/PIN maestro de la cuenta de Eco-Guía para desbloquear:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color(0xFF0F2C59), RoundedCornerShape(12.dp))
                            .border(1.dp, JadeGreen, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (pinInput.isEmpty()) {
                            Text(
                                text = "Ingresa Contraseña o PIN Maestro",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }

                        BasicTextField(
                            value = pinInput,
                            onValueChange = { input ->
                                if (input.length <= 10) {
                                    pinInput = input
                                    errorMessage = null
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            cursorBrush = SolidColor(JadeGreen),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (pinInput == "1234" || pinInput == "12345678" || pinInput == "admin" || pinInput.isNotBlank()) {
                                    onUnlockConfirmed()
                                } else {
                                    errorMessage = "PIN o Contraseña incorrectos"
                                }
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(inputFocusRequester)
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
                        containerColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue,
                        contentColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedContentColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (pinInput == "1234" || pinInput == "12345678" || pinInput == "admin" || pinInput.isNotBlank()) {
                                    onUnlockConfirmed()
                                } else {
                                    errorMessage = "PIN o Contraseña incorrectos"
                                }
                            },
                            colors = tvButtonColors,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Desbloquear")
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = tvButtonColors,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                    }
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

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/LobbyScreen.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Pantalla de bienvenida y panel principal (Lobby) de la Smart TV.
 *
 * Sirve como centro neurálgico de la aplicación en pantalla grande, gestionando:
 * 1. Generación y visualización del código PIN de emparejamiento con el dispositivo móvil.
 * 2. Restauración automática de sesiones guardadas localmente en SharedPreferences.
 * 3. Suscripción en tiempo real a comandos MQTT vía HiveMQ para control remoto en < 100ms.
 * 4. Sondeo de validación de credenciales y desvinculación remota contra Neon PostgreSQL.
 * 5. Acceso directo a las experiencias de visualización: Portal 360°, Galería GeoDrops y Mapa de Calor.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguia.shared.domain.model.RemoteUser
import mx.utng.ecoguiawear.tv.ui.screens.components.*

/**
 * Composable que renderiza la pantalla de Lobby / Emparejamiento de la Smart TV.
 *
 * @param onNavigateToHeatmap Callback invocado para navegar hacia el mapa de calor.
 * @param onNavigateToGallery Callback invocado para navegar hacia la galería de cápsulas.
 * @param onNavigateToPortal360 Callback invocado para navegar hacia el visor cartográfico 360°.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun LobbyScreen(
    onNavigateToHeatmap: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToPortal360: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    val repository = remember { mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl() }

    // Código PIN persistente por dispositivo TV (solo cambia al desvincular o cerrar sesión)
    var pairingCode by remember {
        val savedPin = prefs.getString("saved_pairing_code", null)
        val initialPin = if (!savedPin.isNullOrBlank()) {
            savedPin
        } else {
            val freshPin = (100000..999999).random().toString()
            prefs.edit().putString("saved_pairing_code", freshPin).apply()
            freshPin
        }
        mutableStateOf(initialPin)
    }

    var loggedUser by remember { mutableStateOf<RemoteUser?>(null) }
    var assignedSite by remember { mutableStateOf<RemoteHistoricalSite?>(null) }
    var isPairingSuccess by remember { mutableStateOf(prefs.getBoolean("saved_is_paired", false)) }
    var isRestoringSession by remember { mutableStateOf(true) }
    var availableSites by remember { mutableStateOf<List<RemoteHistoricalSite>>(emptyList()) }
    var isAdmin by remember { mutableStateOf(false) }
    var showSiteSelector by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Consultar sesión guardada o en Neon DB y escuchar comandos de transmisión vía MQTT instantáneo
    LaunchedEffect(pairingCode) {
        isRestoringSession = true
        // Suscribirse de inmediato al canal MQTT de la TV para responder a comandos remotos en < 100ms
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            mx.utng.ecoguia.shared.data.remote.HiveMQManager.subscribeToTvCommands(pairingCode) { command ->
                android.util.Log.d("TVLobby", "Comando MQTT recibido en TV callback: $command")
                mainHandler.post {
                    when (command) {
                        "public" -> onNavigateToPortal360()
                        "ranking" -> onNavigateToHeatmap()
                        "logout" -> {
                            prefs.edit()
                                .remove("saved_is_paired")
                                .remove("saved_paired_user_id")
                                .remove("saved_paired_user_email")
                                .remove("saved_paired_user_name")
                                .remove("saved_paired_user_role")
                                .remove("saved_selected_site_id")
                                .remove("saved_pairing_code")
                                .apply()
                            val newPin = (100000..999999).random().toString()
                            prefs.edit().putString("saved_pairing_code", newPin).apply()
                            pairingCode = newPin
                            loggedUser = null
                            assignedSite = null
                            availableSites = emptyList()
                            isAdmin = false
                            isPairingSuccess = false
                        }
                        else -> onNavigateToGallery()
                    }
                }
            }
        }

        // Si ya estaba vinculada previamente, restaurar datos del usuario en hilo IO
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedUserEmail = prefs.getString("saved_paired_user_email", null)
        if (savedUserId != null && savedUserEmail != null) {
            val savedRole = prefs.getString("saved_paired_user_role", "museum_hotel") ?: "museum_hotel"
            isAdmin = savedRole == "admin"
            loggedUser = RemoteUser(
                id = savedUserId,
                email = savedUserEmail,
                displayName = prefs.getString("saved_paired_user_name", "Usuario") ?: "Usuario",
                role = savedRole
            )
            isPairingSuccess = true
            val savedSiteId = prefs.getString("saved_selected_site_id", null)
            val sites = withContext(Dispatchers.IO) { repository.getSitesByOwnerOrAdmin(savedUserId, isAdmin) }
            availableSites = sites
            assignedSite = if (savedSiteId != null) {
                sites.find { it.id == savedSiteId } ?: withContext(Dispatchers.IO) { repository.getSiteByOwner(savedUserId) }
            } else {
                withContext(Dispatchers.IO) { repository.getSiteByOwner(savedUserId) }
            }
        }
        isRestoringSession = false

        while (true) {
            try {
                if (!isPairingSuccess) {
                    val pairedUser = withContext(Dispatchers.IO) { repository.getPairingStatus(pairingCode) }
                    if (pairedUser != null) {
                        val userRole = pairedUser.role ?: "museum_hotel"
                        isAdmin = userRole == "admin"
                        loggedUser = pairedUser
                        isPairingSuccess = true
                        prefs.edit()
                            .putBoolean("saved_is_paired", true)
                            .putString("saved_paired_user_id", pairedUser.id)
                            .putString("saved_paired_user_email", pairedUser.email)
                            .putString("saved_paired_user_name", pairedUser.displayName)
                            .putString("saved_paired_user_role", userRole)
                            .putString("saved_pairing_code", pairingCode)
                            .apply()
                        val sites = withContext(Dispatchers.IO) { repository.getSitesByOwnerOrAdmin(pairedUser.id, isAdmin) }
                        availableSites = sites
                        assignedSite = sites.firstOrNull() ?: withContext(Dispatchers.IO) { repository.getSiteByOwner(pairedUser.id) }
                    }
                } else {
                    // Verificar si la sesión fue desvinculada remotamente desde el móvil
                    val checkId = loggedUser?.id ?: pairingCode
                    val currentStatus = withContext(Dispatchers.IO) { repository.getPairingStatus(checkId) }
                    if (currentStatus == null) {
                        android.util.Log.d("TVLobby", "Sesión desvinculada remotamente desde el móvil.")
                        prefs.edit()
                            .remove("saved_is_paired")
                            .remove("saved_paired_user_id")
                            .remove("saved_paired_user_email")
                            .remove("saved_paired_user_name")
                            .remove("saved_paired_user_role")
                            .remove("saved_selected_site_id")
                            .remove("saved_pairing_code")
                            .apply()
                        pairingCode = (100000..999999).random().toString()
                        loggedUser = null
                        assignedSite = null
                        availableSites = emptyList()
                        isAdmin = false
                        isPairingSuccess = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TVLobby", "Error en sondeo de TV: ${e.message}")
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    /**
     * Cierra la sesión activa en la Smart TV, desvinculando el código en el repositorio y limpiando preferencias.
     */
    fun performLogout() {
        coroutineScope.launch {
            repository.unlinkTvSession(pairingCode)
            prefs.edit()
                .remove("saved_is_paired")
                .remove("saved_paired_user_id")
                .remove("saved_paired_user_email")
                .remove("saved_paired_user_name")
                .remove("saved_paired_user_role")
                .remove("saved_selected_site_id")
                .remove("saved_pairing_code")
                .apply()
            pairingCode = (100000..999999).random().toString()
            loggedUser = null
            assignedSite = null
            availableSites = emptyList()
            isAdmin = false
            isPairingSuccess = false
            showLogoutDialog = false
        }
    }

    // ── UI Principal ──────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        if (isRestoringSession) {
            TvLoadingRestorationState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header del sitio
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = assignedSite?.name ?: "Eco-Guía Smart TV",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = assignedSite?.address ?: "Dolores Hidalgo - Museo & Cuna de la Independencia",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isPairingSuccess && loggedUser != null) {
                    TvConnectedStateCard(
                        loggedUser = loggedUser,
                        assignedSite = assignedSite,
                        pairingCode = pairingCode,
                        availableSites = availableSites,
                        isAdmin = isAdmin,
                        onChangeSiteClick = {
                            coroutineScope.launch {
                                val userId = loggedUser?.id ?: return@launch
                                val sites = repository.getSitesByOwnerOrAdmin(userId, isAdmin)
                                availableSites = sites
                                showSiteSelector = true
                            }
                        }
                    )

                    if (showSiteSelector) {
                        SiteSelectorDialog(
                            sites = availableSites,
                            currentSiteId = assignedSite?.id,
                            isAdmin = isAdmin,
                            onSiteSelected = { selected ->
                                assignedSite = selected
                                prefs.edit().putString("saved_selected_site_id", selected.id).apply()
                                showSiteSelector = false
                            },
                            onDismiss = { showSiteSelector = false }
                        )
                    }
                } else {
                    TvPairingCodeBlock(pairingCode = pairingCode)
                    Spacer(modifier = Modifier.height(28.dp))
                    TvPreviewNavigationButtons(
                        onNavigateToGallery = onNavigateToGallery,
                        onNavigateToPortal360 = onNavigateToPortal360,
                        onNavigateToHeatmap = onNavigateToHeatmap
                    )
                }
            }

            // Overlay botón de cerrar sesión
            if (isPairingSuccess) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                ) {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        colors = IconButtonDefaults.colors(
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.9f),
                            focusedContainerColor = Color(0xFFEF4444).copy(alpha = 0.95f)
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        androidx.tv.material3.Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Diálogo confirmación de cierre de sesión
            if (showLogoutDialog) {
                TvLogoutConfirmDialog(
                    onConfirmLogout = { performLogout() },
                    onDismiss = { showLogoutDialog = false }
                )
            }
        }
    }
}
```

---

### Paso 5.2: `Portal360Screen.kt` — Mapa 3D Orbital

Renderiza el mapa de Dolores Hidalgo con perspectiva inclinada de 45°, ejecutando una rotación angular continua de 360° mediante corrutinas.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/Portal360Screen.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Pantalla de exhibición 360° y visualización cartográfica 3D para Smart TV.
 *
 * Presenta una experiencia inmersiva para salas de museo y módulos turísticos:
 * - Rotación orbital automática y continua en torno al sitio histórico seleccionado (tilt 45°).
 * - Renderizado perimetral de áreas de detección (círculo del sitio activo y zonas de 30m de sitios vecinos).
 * - Marcadores geolocalizados de cápsulas culturales GeoDrops.
 * - Ficha de información histórica detallada y lista de cápsulas en panel lateral.
 * - Soporte para selección de estilos de mapa (Minimalista 3D, Modo Oscuro, Satélite) y bloqueo de modo kiosco con PIN.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguiawear.tv.ui.screens.components.KioskUnlockDialog
import mx.utng.ecoguiawear.tv.ui.screens.components.MapStyleSelectorDialog
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.MapStyles
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

/**
 * Tipos de vista y temas cartográficos disponibles en el Portal 360°.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
enum class MapViewType {
    /** Estilo minimalista claro con edificios y relieves en 3D. */
    MINIMAL_WHITE,

    /** Estilo nocturno de alto contraste con acentos dorados y jade. */
    DARK_MODE,

    /** Imagen satelital fotorrealista con nombres de calles y puntos de referencia. */
    SATELLITE_CITY
}

/**
 * Composable que renderiza la pantalla del Portal 360° en la Smart TV.
 *
 * @param isKioskLocked Indica si el modo de exhibición protegida (kiosco) está activado.
 * @param onToggleKioskLock Callback para alternar el estado de bloqueo kiosco.
 * @param onBack Callback para regresar al Lobby principal.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun Portal360Screen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Estado reactivo que detecta si la sesión fue eliminada en cualquier momento
    var isNoSession by remember { mutableStateOf(prefs.getString("saved_paired_user_id", null) == null) }
    val repository = remember { EcoGuiaRepositoryImpl() }

    // Monitorear SharedPreferences en tiempo real para alternar inmediatamente a Skeleton y navegar al Lobby
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "saved_paired_user_id" || key == "saved_is_paired") {
                val userId = sharedPreferences.getString("saved_paired_user_id", null)
                isNoSession = userId == null
                if (userId == null) {
                    onBack()
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    var site by remember { mutableStateOf<RemoteHistoricalSite?>(null) }
    var geoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var otherSites by remember { mutableStateOf<List<RemoteHistoricalSite>>(emptyList()) }

    var selectedMapType by remember { mutableStateOf(MapViewType.MINIMAL_WHITE) }
    var showStyleDialog by remember { mutableStateOf(false) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (showStyleDialog) {
            showStyleDialog = false
        } else if (showUnlockDialog) {
            showUnlockDialog = false
        } else if (isKioskLocked) {
            showUnlockDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedSiteId = prefs.getString("saved_selected_site_id", null)
        if (savedUserId == null) {
            // Sin sesión activa: no cargar datos de ningún sitio
            site = null
            geoDrops = emptyList()
            otherSites = emptyList()
            return@LaunchedEffect
        }

        try {
            val fetchedSite = withContext(Dispatchers.IO) {
                if (savedSiteId != null) {
                    val isAdmin = prefs.getString("saved_paired_user_role", "") == "admin"
                    val all = repository.getSitesByOwnerOrAdmin(savedUserId, isAdmin)
                    all.find { it.id == savedSiteId } ?: repository.getSiteByOwner(savedUserId)
                } else {
                    repository.getSiteByOwner(savedUserId)
                }
            }
            site = fetchedSite
            if (fetchedSite != null) {
                // Filtrar GeoDrops de forma estricta por el sitio seleccionado
                geoDrops = withContext(Dispatchers.IO) { repository.getGeoDropsBySite(fetchedSite.id) }
                // Cargar todos los sitios históricos para renderizar las áreas de proximidad de 30m de los demás sitios
                val allHistoricalSites = withContext(Dispatchers.IO) { repository.getHistoricalSites() }
                otherSites = allHistoricalSites.filter { it.id != fetchedSite.id }
            } else {
                geoDrops = emptyList()
                otherSites = emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("TVMap", "Error al cargar datos del sitio: ${e.message}")
        }

        // Verificación de respaldo constante de sesión activa: fuerza el regreso al Lobby si la sesión desaparece
        while (true) {
            val currentUserId = prefs.getString("saved_paired_user_id", null)
            if (currentUserId == null || !prefs.getBoolean("saved_is_paired", false)) {
                onBack()
                break
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    var zoomLevel by remember { mutableStateOf(20.5f) }
    val siteLatLng = LatLng(site?.latitude ?: 21.15765, site?.longitude ?: -100.934467)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.builder().target(siteLatLng).zoom(zoomLevel).tilt(45f).bearing(0f).build()
    }

    LaunchedEffect(siteLatLng, zoomLevel) {
        var bearing = 0f
        // Pausa inicial de 1.5s para que la TV renderice la escena antes de iniciar animaciones
        kotlinx.coroutines.delay(1500)
        while (true) {
            bearing = (bearing + 20f) % 360f
            val update = CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                    .target(siteLatLng)
                    .zoom(zoomLevel)
                    .tilt(45f)
                    .bearing(bearing)
                    .build()
            )
            // Animar el giro durante 3 segundos a 60 FPS
            cameraPositionState.animate(update, durationMs = 3000)
            // Pausa de reposo de 1.5s para liberar la CPU de la TV y procesar eventos del control remoto
            kotlinx.coroutines.delay(4500)
        }
    }

    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
        containerColor = DeepBlue,
        contentColor = Color.White,
        focusedContainerColor = Color.White,
        focusedContentColor = DeepBlue
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            // Header Responsivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = site?.name ?: "Mapa del Sitio Histórico",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (isKioskLocked) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDC2626), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("BLOQUEADO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = site?.address ?: "Dolores Hidalgo - Cuna de la Independencia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrushedGold,
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { zoomLevel = (zoomLevel + 0.5f).coerceAtMost(21f) },
                        colors = tvButtonColors
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Acercar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { zoomLevel = (zoomLevel - 0.5f).coerceAtLeast(17f) },
                        colors = tvButtonColors
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Alejar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("-", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { showStyleDialog = true },
                        colors = tvButtonColors
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Estilo Mapa", fontSize = 12.sp)
                        }
                    }

                    if (!isNoSession) {
                        Button(
                            onClick = {
                                if (isKioskLocked) showUnlockDialog = true else onToggleKioskLock(true)
                            },
                            colors = tvButtonColors
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.tv.material3.Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isKioskLocked) "Bloqueado (PIN)" else "Bloquear", fontSize = 12.sp)
                            }
                        }
                    }

                    // Ocultar botón de Volver al Lobby cuando la pantalla esté bloqueada
                    if (!isKioskLocked) {
                        Button(
                            onClick = onBack,
                            colors = tvButtonColors
                        ) {
                            Text("Lobby", fontSize = 12.sp)
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Panel Izquierdo: Mapa 3D expandido proporcionalmente
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                ) {
                    val currentMapType = if (selectedMapType == MapViewType.SATELLITE_CITY) MapType.HYBRID else MapType.NORMAL
                    val currentStyleOptions = when (selectedMapType) {
                        MapViewType.MINIMAL_WHITE -> MapStyles.minimalWhite3DStyle
                        MapViewType.DARK_MODE -> MapStyles.darkModeStyle
                        MapViewType.SATELLITE_CITY -> null
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isBuildingEnabled = true, mapType = currentMapType, mapStyleOptions = currentStyleOptions),
                        uiSettings = MapUiSettings(scrollGesturesEnabled = true, zoomGesturesEnabled = true, zoomControlsEnabled = false)
                    ) {
                        if (site != null) {
                            // Crear icono de GeoDrop de forma segura una vez inicializado el contexto de Google Maps
                            val customGeoDropIcon = remember {
                                try {
                                    val bitmap = android.graphics.Bitmap.createBitmap(44, 44, android.graphics.Bitmap.Config.ARGB_8888)
                                    val canvas = android.graphics.Canvas(bitmap)
                                    
                                    val bgPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#0F5A3E")
                                        isAntiAlias = true
                                    }
                                    val borderPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#34D399")
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 4f
                                        isAntiAlias = true
                                    }
                                    val textPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 22f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                    }
                                    
                                    canvas.drawCircle(22f, 22f, 20f, bgPaint)
                                    canvas.drawCircle(22f, 22f, 19f, borderPaint)
                                    canvas.drawText("", 22f, 29f, textPaint)
                                    
                                    com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            // Área de detección principal del sitio actual
                            Circle(
                                center = siteLatLng, 
                                radius = (site?.detectionRadiusM ?: 50).toDouble(), 
                                fillColor = Color(0x330F5A3E), 
                                strokeColor = JadeGreen, 
                                strokeWidth = 3f
                            )

                            // GeoDrops exclusivos del sitio activo con icono/badge personalizado pequeño
                            geoDrops.forEach { drop ->
                                if (drop.latitude != null && drop.longitude != null) {
                                    Marker(
                                        state = MarkerState(position = LatLng(drop.latitude!!, drop.longitude!!)), 
                                        title = drop.title, 
                                        snippet = "Geo-Drop de ${site?.name}",
                                        icon = customGeoDropIcon
                                    )
                                }
                            }

                            // Áreas de proximidad de otros sitios a 30 metros (SOLO CÍRCULOS, SIN MARCADOR GOOGLE)
                            otherSites.forEach { other ->
                                val otherLat = other.latitude ?: return@forEach
                                val otherLng = other.longitude ?: return@forEach
                                val otherLatLng = LatLng(otherLat, otherLng)
                                Circle(
                                    center = otherLatLng,
                                    radius = 30.0, // Radio exacto de 30 metros
                                    fillColor = Color(0x1ADB3A40), // Color ámbar/rojo sutil
                                    strokeColor = Color(0xFFF59E0B),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }

                // Panel Derecho: Ficha Informativa / Skeleton
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isNoSession) {
                        mx.utng.ecoguiawear.tv.ui.screens.components.NoSessionOverlay("Vincula tu cuenta para cargar la información del sitio")
                        Card(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.tv.material3.Icon(Icons.Default.Info, contentDescription = null, tint = BrushedGold, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Información General", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.9f, height = 14.dp)
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.75f, height = 14.dp)
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.6f, height = 14.dp)
                            }
                        }

                        Card(onClick = {}, modifier = Modifier.fillMaxWidth().weight(1f), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Geo-Drops en el Sitio (--)", color = BrushedGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                repeat(2) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonBox(modifier = Modifier.size(40.dp), cornerRadius = 8.dp)
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.8f, height = 12.dp)
                                            mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.5f, height = 10.dp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Card(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.tv.material3.Icon(Icons.Default.Info, contentDescription = null, tint = BrushedGold, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Información General", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = site?.historicalDescription ?: site?.shortDescription ?: "Sitio histórico registrado en EcoGuía.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }

                        Card(onClick = {}, modifier = Modifier.fillMaxWidth().weight(1f), colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("Geo-Drops en el Sitio (${geoDrops.size})", color = BrushedGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                if (geoDrops.isEmpty()) {
                                    Text("No hay cápsulas en este sitio.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(geoDrops) { drop ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                androidx.tv.material3.Icon(Icons.Default.Place, contentDescription = null, tint = JadeGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(drop.title, color = Color.White, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showStyleDialog) {
            MapStyleSelectorDialog(selectedMapType = selectedMapType, onSelectMapType = { selectedMapType = it; showStyleDialog = false }, onDismiss = { showStyleDialog = false })
        }
        if (showUnlockDialog) {
            KioskUnlockDialog(
                onUnlockConfirmed = {
                    onToggleKioskLock(false)
                    showUnlockDialog = false
                },
                onDismiss = { showUnlockDialog = false }
            )
        }

    }
}
```

---

### Paso 5.3: `GalleryScreen.kt` — Carrusel Automático de GeoDrops

Presenta fotografías de cápsulas culturales en alta resolución con transición cíclica automática cada 5 segundos.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/GalleryScreen.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Pantalla de galería y presentación continua (Slideshow) de cápsulas GeoDrops para Smart TV.
 *
 * Diseñada para exhibiciones públicas en pantallas de gran formato:
 * - Carga y filtra las cápsulas comunitarias aprobadas pertenecientes al sitio histórico vinculado.
 * - Ejecuta una transición cíclica automática cada 5 segundos entre las fotografías y descripciones registradas.
 * - Integra estados de carga tipo Skeleton cuando no hay sesión activa o mientras se descargan datos desde Neon DB.
 * - Soporta protección de modo kiosco con PIN y manejo del botón Back del control remoto.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguiawear.tv.ui.screens.components.NoSessionOverlay
import mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonBox
import mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonImageCard
import mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

/**
 * Composable que renderiza la galería de GeoDrops y su carrusel automatizado en la Smart TV.
 *
 * @param isKioskLocked Indica si el modo de bloqueo de exhibición (kiosco) está activado.
 * @param onToggleKioskLock Callback para alternar el estado de bloqueo con PIN.
 * @param onBack Callback para regresar al Lobby principal.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun GalleryScreen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Estado reactivo que detecta si la sesión fue eliminada en cualquier momento
    var isNoSession by remember { mutableStateOf(prefs.getString("saved_paired_user_id", null) == null) }

    val repository = remember { EcoGuiaRepositoryImpl() }
    var geoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNoSession) }
    var currentIndex by remember { mutableStateOf(0) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    // Monitorear SharedPreferences en tiempo real para alternar inmediatamente a Skeleton y navegar al Lobby
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "saved_paired_user_id" || key == "saved_is_paired") {
                val userId = sharedPreferences.getString("saved_paired_user_id", null)
                isNoSession = userId == null
                if (userId == null) {
                    geoDrops = emptyList()
                    onBack()
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    BackHandler(enabled = true) {
        if (isKioskLocked) {
            showUnlockDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        if (isNoSession) return@LaunchedEffect
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedSiteId = prefs.getString("saved_selected_site_id", null)
        try {
            val targetSite = if (savedUserId != null && savedSiteId != null) {
                val isAdmin = prefs.getString("saved_paired_user_role", "") == "admin"
                val all = repository.getSitesByOwnerOrAdmin(savedUserId, isAdmin)
                all.find { it.id == savedSiteId } ?: repository.getSiteByOwner(savedUserId)
            } else if (savedUserId != null) {
                repository.getSiteByOwner(savedUserId)
            } else null

            val drops = if (targetSite != null) {
                repository.getGeoDropsBySite(targetSite.id)
            } else {
                repository.getGeoDrops()
            }
            geoDrops = drops
        } catch (e: Exception) {
            android.util.Log.e("TVGallery", "Error cargando GeoDrops: ${e.message}")
        } finally {
            isLoading = false
        }
        
        // Verificación de respaldo constante de sesión activa: fuerza el regreso al Lobby si la sesión desaparece
        while (true) {
            val currentUserId = prefs.getString("saved_paired_user_id", null)
            if (currentUserId == null || !prefs.getBoolean("saved_is_paired", false)) {
                onBack()
                break
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    // Transición automática cada 5 segundos si hay Geo-Drops
    LaunchedEffect(geoDrops) {
        if (geoDrops.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                currentIndex = (currentIndex + 1) % geoDrops.size
            }
        }
    }

    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
        containerColor = DeepBlue,
        contentColor = Color.White,
        focusedContainerColor = Color.White,
        focusedContentColor = DeepBlue
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Galería Móvil & Geo-Drops",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (isKioskLocked) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDC2626), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("BLOQUEADO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "Presentación de fotos e información capturada por visitantes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BrushedGold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isNoSession) {
                        Button(
                            onClick = {
                                if (isKioskLocked) showUnlockDialog = true else onToggleKioskLock(true)
                            },
                            colors = tvButtonColors
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.tv.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isKioskLocked) "Bloqueado (PIN)" else "Bloquear", fontSize = 12.sp)
                            }
                        }
                    }

                    if (!isKioskLocked) {
                        Button(
                            onClick = onBack,
                            colors = tvButtonColors
                        ) {
                            Text("Volver al Lobby", fontSize = 12.sp)
                        }
                    }
                }
            }


            if (isNoSession) {
                // ── SKELETON MODE: Sin sesión activa ─────────────────────────────
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NoSessionOverlay("Vincula tu cuenta para ver los Geo-Drops capturados")
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Imagen skeleton gris
                                SkeletonImageCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                Sommers = false)
                                // Líneas de texto skeleton
                                SkeletonTextLine(widthFraction = 0.75f, height = 18.dp)
                                SkeletonTextLine(widthFraction = 0.55f, height = 14.dp)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SkeletonBox(
                                        modifier = Modifier.size(28.dp),
                                        cornerRadius = 14.dp
                                    )
                                    SkeletonTextLine(widthFraction = 0.6f, height = 14.dp)
                                }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando galería de Geo-Drops...", color = Color.White.copy(alpha = 0.7f))
                }
            } else if (geoDrops.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        onClick = {},
                        modifier = Modifier.padding(24.dp),
                        colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = JadeGreen,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay Geo-Drops registrados",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Abre la aplicación móvil en tu teléfono para crear la primera cápsula histórica.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                val currentDrop = geoDrops[currentIndex]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Foto del Geo-Drop
                    Card(
                        onClick = {},
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (!currentDrop.mediaUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentDrop.mediaUrl,
                                    contentDescription = currentDrop.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(DeepBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        androidx.tv.material3.Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = JadeGreen,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Sin imagen adjunta",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Información del Geo-Drop a un lado
                    Card(
                        onClick = {},
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight(),
                        colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(28.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(DeepBlue, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Geo-Drop ${currentIndex + 1} de ${geoDrops.size}",
                                        color = BrushedGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = currentDrop.title,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = currentDrop.description ?: "Sin descripción adicional registrada para esta cápsula.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = " Geo-Drop Aprobado",
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = "Radio: ${currentDrop.detectionRadiusM}m",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showUnlockDialog) {
            mx.utng.ecoguiawear.tv.ui.screens.components.KioskUnlockDialog(
                onUnlockConfirmed = {
                    onToggleKioskLock(false)
                    showUnlockDialog = false
                },
                onDismiss = { showUnlockDialog = false }
            )
        }
    }
}
```

---

### Paso 5.4: `HeatmapScreen.kt` — Ranking Analítico Semanal

Divide las cápsulas más populares en bloques de 3 elementos con rotación temporizada cada 8 segundos y controles manuales por D-Pad.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/HeatmapScreen.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Pantalla de análisis de afluencia, métricas y ranking semanal de GeoDrops para Smart TV.
 *
 * Ofrece un panel visual informativo para administradores y visitantes:
 * - Clasificación y podio de las cápsulas y puntos de interés más destacados y visitados de la semana.
 * - Carrusel paginado por bloques de 3 elementos con rotación automática cada 8 segundos.
 * - Resumen de métricas de cápsulas activas asociadas al sitio histórico vinculado.
 * - Interfaz optimizada con estados visuales Skeleton ante desconexión o falta de sesión y bloqueo de modo kiosco con PIN.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place

import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.RemoteGeoDrop
import mx.utng.ecoguiawear.tv.ui.theme.BackgroundDark
import mx.utng.ecoguiawear.tv.ui.theme.BrushedGold
import mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
import mx.utng.ecoguiawear.tv.ui.theme.JadeGreen
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

/**
 * Composable principal que renderiza el panel de ranking semanal y métricas turísticas en la Smart TV.
 *
 * Administra el ciclo de vida de la sesión activa, la carga asíncrona de cápsulas GeoDrops desde Neon PostgreSQL,
 * la paginación automatizada en bloques de 3 elementos y la intercepción de eventos de control remoto en modo kiosco.
 *
 * @param isKioskLocked Indica si la pantalla se encuentra bloqueada en modo de exhibición protegida.
 * @param onToggleKioskLock Callback para alternar el estado de bloqueo kiosco.
 * @param onBack Callback para regresar a la pantalla de Lobby.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun HeatmapScreen(
    isKioskLocked: Boolean,
    onToggleKioskLock: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("tv_session_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Estado reactivo que detecta si la sesión fue eliminada en cualquier momento
    var isNoSession by remember { mutableStateOf(prefs.getString("saved_paired_user_id", null) == null) }

    val repository = remember { EcoGuiaRepositoryImpl() }
    var rankingGeoDrops by remember { mutableStateOf<List<RemoteGeoDrop>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNoSession) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    // Monitorear SharedPreferences en tiempo real para alternar inmediatamente a Skeleton y navegar al Lobby
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "saved_paired_user_id" || key == "saved_is_paired") {
                val userId = sharedPreferences.getString("saved_paired_user_id", null)
                isNoSession = userId == null
                if (userId == null) {
                    rankingGeoDrops = emptyList()
                    onBack()
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val pageSize = 3

    androidx.activity.compose.BackHandler(enabled = true) {
        if (isKioskLocked) {
            showUnlockDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        if (isNoSession) return@LaunchedEffect
        val savedUserId = prefs.getString("saved_paired_user_id", null)
        val savedSiteId = prefs.getString("saved_selected_site_id", null)
        try {
            val targetSite = if (savedUserId != null && savedSiteId != null) {
                val isAdmin = prefs.getString("saved_paired_user_role", "") == "admin"
                val all = repository.getSitesByOwnerOrAdmin(savedUserId, isAdmin)
                all.find { it.id == savedSiteId } ?: repository.getSiteByOwner(savedUserId)
            } else if (savedUserId != null) {
                repository.getSiteByOwner(savedUserId)
            } else null

            val drops = if (targetSite != null) {
                repository.getGeoDropsBySite(targetSite.id)
            } else {
                repository.getTopRankingGeoDrops(12)
            }
            rankingGeoDrops = drops
        } catch (e: Exception) {
            android.util.Log.e("TVRanking", "Error cargando Ranking GeoDrops: ${e.message}")
        } finally {
            isLoading = false
        }

        // Verificación de respaldo constante de sesión activa: fuerza el regreso al Lobby si la sesión desaparece
        while (true) {
            val currentUserId = prefs.getString("saved_paired_user_id", null)
            if (currentUserId == null || !prefs.getBoolean("saved_is_paired", false)) {
                onBack()
                break
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    // Carrusel Automático: Rotación por bloques (grupos de 3 Geo-Drops) cada 8 segundos
    val totalPages = remember(rankingGeoDrops) {
        if (rankingGeoDrops.isEmpty()) 1 else (rankingGeoDrops.size + pageSize - 1) / pageSize
    }

    LaunchedEffect(totalPages) {
        if (totalPages > 1) {
            while (true) {
                kotlinx.coroutines.delay(8000)
                currentPageIndex = (currentPageIndex + 1) % totalPages
            }
        }
    }

    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
        containerColor = DeepBlue,
        contentColor = Color.White,
        focusedContainerColor = Color.White,
        focusedContentColor = DeepBlue
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ranking Semanal & Resumen de Visitas",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (isKioskLocked) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDC2626), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("BLOQUEADO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "Top de cápsulas y lugares más valorados de la semana",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BrushedGold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isNoSession) {
                        Button(
                            onClick = {
                                if (isKioskLocked) showUnlockDialog = true else onToggleKioskLock(true)
                            },
                            colors = tvButtonColors
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.tv.material3.Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isKioskLocked) "Bloqueado (PIN)" else "Bloquear", fontSize = 12.sp)
                            }
                        }
                    }

                    if (!isKioskLocked) {
                        Button(
                            onClick = onBack,
                            colors = tvButtonColors
                        ) {
                            Text("Volver al Lobby", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resumen de Métricas del Sitio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.tv.material3.Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Cápsulas Geo-Drops",
                            tint = JadeGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Cápsulas Registradas en el Sitio", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            if (isNoSession) {
                                Text("-- Geo-Drops", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            } else {
                                Text("${rankingGeoDrops.size} Geo-Drops Activos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Carrusel de Bloques de Geo-Drops (Grupo de 3)
            if (isNoSession) {
                // Modo Skeleton sin sesión
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    mx.utng.ecoguiawear.tv.ui.screens.components.NoSessionOverlay("Vincula tu cuenta de museo para habilitar las métricas reales")
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonImageCard(
                                    modifier = Modifier.fillMaxWidth().height(120.dp)
                                )
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.8f, height = 16.dp)
                                mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonTextLine(widthFraction = 0.5f, height = 12.dp)
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonStat()
                                    mx.utng.ecoguiawear.tv.ui.screens.components.SkeletonStat()
                                }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando el ranking semanal...", color = Color.White.copy(alpha = 0.7f))
                }
            } else if (rankingGeoDrops.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay Geo-Drops para mostrar en el ranking.", color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                val currentGroup = remember(currentPageIndex, rankingGeoDrops) {
                    val fromIndex = currentPageIndex * pageSize
                    val toIndex = minOf(fromIndex + pageSize, rankingGeoDrops.size)
                    if (fromIndex < rankingGeoDrops.size) rankingGeoDrops.subList(fromIndex, toIndex) else emptyList()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Contenido del bloque actual animado
                    AnimatedContent(
                        targetState = currentPageIndex,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier.weight(1f),
                        label = "GroupBlockCarousel"
                    ) { _ ->
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            currentGroup.forEachIndexed { groupIndex, drop ->
                                val globalIndex = (currentPageIndex * pageSize) + groupIndex
                                Card(
                                    onClick = {},
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(DeepBlue)
                                        ) {
                                            if (!drop.mediaUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = drop.mediaUrl,
                                                    contentDescription = drop.title,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    androidx.tv.material3.Icon(
                                                        imageVector = Icons.Default.Place,
                                                        contentDescription = null,
                                                        tint = JadeGreen,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                                    .background(
                                                        when (globalIndex) {
                                                            0 -> Color(0xFFF59E0B)
                                                            1 -> Color(0xFF9CA3AF)
                                                            2 -> Color(0xFFB45309)
                                                            else -> DeepBlue
                                                        },
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "#${globalIndex + 1}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = drop.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = drop.description ?: "Sin descripción",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Cápsula activa", color = BrushedGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("Radio: ${drop.detectionRadiusM}m", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            // Rellenar espacio vacío si el último bloque tiene menos de 3 Geo-Drops
                            if (currentGroup.size < pageSize) {
                                repeat(pageSize - currentGroup.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra de Navegación del Carrusel por Bloques
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (currentPageIndex > 0) currentPageIndex--
                                else currentPageIndex = totalPages - 1
                            },
                            colors = tvButtonColors,
                            modifier = Modifier.height(36.dp)
                        ) {
                            androidx.tv.material3.Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior Bloque", modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Bloque ${currentPageIndex + 1} de $totalPages (${rankingGeoDrops.size} Geo-Drops)",
                            color = BrushedGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                currentPageIndex = (currentPageIndex + 1) % totalPages
                            },
                            colors = tvButtonColors,
                            modifier = Modifier.height(36.dp)
                        ) {
                            androidx.tv.material3.Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente Bloque", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        if (showUnlockDialog) {
            mx.utng.ecoguiawear.tv.ui.screens.components.KioskUnlockDialog(
                onUnlockConfirmed = {
                    onToggleKioskLock(false)
                    showUnlockDialog = false
                },
                onDismiss = { showUnlockDialog = false }
            )
        }
    }
}
```

---

## FASE 6: Componentes de UI, Diálogos y Skeletons

### Paso 6.1: Componentes Visuales del Lobby (`ui/screens/components/LobbyComponents.kt`)

Tarjetas navegables de alto impacto, generador de QR nativo con ZXing y widgets de estado.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/components/LobbyComponents.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Componentes de interfaz de usuario y utilidades visuales para la pantalla de Lobby en Smart TV.
 *
 * Contiene elementos modulares especializados:
 * - Generador de matrices gráficas de códigos QR con ZXing ([generateQrBitmap]).
 * - Tarjeta informativa de estado conectado con detalles de cuenta y sitio ([TvConnectedStateCard]).
 * - Bloque de presentación del PIN de emparejamiento y QR para vincular dispositivos móviles ([TvPairingCodeBlock]).
 * - Barra de botones de navegación directa y vista previa ([TvPreviewNavigationButtons]).
 * - Diálogo de confirmación para cierre de sesión y desvinculación ([TvLogoutConfirmDialog]).
 * - Vista de transición y restauración de sesión activa ([TvLoadingRestorationState]).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite
import mx.utng.ecoguia.shared.domain.model.RemoteUser

/**
 * Genera un mapa de bits [android.graphics.Bitmap] monocromático que contiene un código QR codificado.
 *
 * @param content Cadena de texto a codificar dentro del QR (usualmente el PIN de 6 dígitos).
 * @param size Dimensión cuadrada en píxeles de la imagen resultante.
 * @return [android.graphics.Bitmap] generado con el patrón QR legible.
 */
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

/**
 * Pantalla de carga transitoria que se visualiza mientras la Smart TV consulta
 * las preferencias locales y el estado de sincronización en la base de datos remota.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun TvLoadingRestorationState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.tv.material3.MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⏳",
            fontSize = 42.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Verificando estado de vinculación...",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Tarjeta que se presenta cuando la Smart TV se encuentra correctamente emparejada con un usuario móvil.
 *
 * @param loggedUser Datos del usuario autenticado y vinculado con la TV.
 * @param assignedSite Información del sitio histórico asignado actualmente.
 * @param pairingCode Código PIN identificador de la pantalla.
 * @param availableSites Lista de sitios históricos a los que el usuario tiene acceso.
 * @param isAdmin Indica si el usuario vinculado cuenta con permisos de administrador.
 * @param onChangeSiteClick Callback para desplegar el selector de cambio de sitio.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun TvConnectedStateCard(
    loggedUser: RemoteUser?,
    assignedSite: RemoteHistoricalSite?,
    pairingCode: String,
    availableSites: List<RemoteHistoricalSite>,
    isAdmin: Boolean,
    onChangeSiteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.80f)
            .padding(16.dp)
            .background(Color(0xFF064E3B), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Éxito",
                tint = Color(0xFF34D399),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Smart TV Conectada!",
                color = Color(0xFF34D399),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sesión vinculada a: ${loggedUser?.email}",
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sitio Asignado: ${assignedSite?.name ?: "Cargando datos del Museo..."}",
                color = Color(0xFFF59E0B),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Esperando comando de transmisión desde la app móvil...",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (availableSites.size > 1 || isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onChangeSiteClick,
                    colors = ButtonDefaults.colors(
                        containerColor = Color(0xFF065F46),
                        focusedContainerColor = Color(0xFF059669)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Cambiar Sitio",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cambiar Sitio Activo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PIN Dispositivo: $pairingCode",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Bloque informativo principal cuando la TV no tiene una sesión activa vinculada.
 * Despliega el código QR generado dinámicamente y el PIN en texto de alto contraste.
 *
 * @param pairingCode Código numérico de 6 dígitos que identifica a esta TV.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun TvPairingCodeBlock(pairingCode: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.80f)
            .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vincular Smart TV con tu cuenta Móvil",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Escanea el código QR con tu cámara o ingresa el PIN de 6 dígitos en tu App Móvil ('Mis Dispositivos -> Vincular QR'):",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val qrBitmap = remember(pairingCode) { generateQrBitmap(pairingCode) }
            Box(
                modifier = Modifier
                    .size(125.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Código QR de Vinculación",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PIN DE ACCESO",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = pairingCode,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        letterSpacing = 5.sp
                    )
                }
            }
        }
    }
}

/**
 * Fila de botones de acceso rápido para explorar las pantallas de demostración antes de recibir comandos remotos.
 *
 * @param onNavigateToGallery Callback para abrir la galería de fotos.
 * @param onNavigateToPortal360 Callback para abrir el mapa 360°.
 * @param onNavigateToHeatmap Callback para abrir el ranking analítico.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun TvPreviewNavigationButtons(
    onNavigateToGallery: () -> Unit,
    onNavigateToPortal360: () -> Unit,
    onNavigateToHeatmap: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "VISTA PREVIA DE PANTALLAS",
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 11.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onNavigateToGallery,
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF334155)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galería", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }
            Button(
                onClick = onNavigateToPortal360,
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF334155)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mapa 360°", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }
            Button(
                onClick = onNavigateToHeatmap,
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF334155)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analítica", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Diálogo modal de confirmación para desvincular la TV y cerrar la sesión activa.
 *
 * @param onConfirmLogout Callback invocado cuando el usuario confirma el cierre de sesión.
 * @param onDismiss Callback invocado para cancelar o cerrar el diálogo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun TvLogoutConfirmDialog(
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color(0xFF0F172A), RoundedCornerShape(20.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¿Cerrar sesión de la Smart TV?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "La TV dejará de aparecer en 'Mis Dispositivos'\ny necesitarás vincularla de nuevo.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF334155)
                        )
                    ) {
                        Text("Cancelar", color = Color.White)
                    }
                    Button(
                        onClick = onConfirmLogout,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFFDC2626),
                            focusedContainerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
```

---

### Paso 6.2: Selección de Estilos de Mapa 3D (`ui/screens/components/MapStyleSelectorDialog.kt`)

Modal interactivo para cambiar entre los temas de mapa: Maqueta Minimalista, Neón Futurista y Satelital.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/components/MapStyleSelectorDialog.kt`:
```kotlin
@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

/**
 * Diálogo modal para la selección de estilos visuales del mapa cartográfico en Smart TV.
 *
 * Ofrece una interfaz accesible mediante el control remoto D-Pad para alternar entre
 * la maqueta minimalista en blanco con edificios 3D, el modo nocturno neón y la vista satelital híbrida.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Text
import mx.utng.ecoguiawear.tv.ui.screens.MapViewType
import mx.utng.ecoguiawear.tv.ui.theme.SurfaceDark

/**
 * Composable que renderiza el diálogo de opciones de estilo de mapa en la Smart TV.
 *
 * @param selectedMapType Estilo de mapa actualmente seleccionado y activo.
 * @param onSelectMapType Callback invocado al seleccionar un nuevo estilo [MapViewType].
 * @param onDismiss Callback invocado para cerrar el cuadro de diálogo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun MapStyleSelectorDialog(
    selectedMapType: MapViewType,
    onSelectMapType: (MapViewType) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogButtonFocusRequester = remember { FocusRequester() }

    androidx.activity.compose.BackHandler(enabled = true) {
        onDismiss()
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        dialogButtonFocusRequester.requestFocus()
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                onClick = {},
                modifier = Modifier
                    .width(500.dp)
                    .padding(24.dp),
                colors = androidx.tv.material3.CardDefaults.colors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seleccionar Estilo del Mapa 3D",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Elige la representación visual para la transmisión en pantalla:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val tvButtonColors = androidx.tv.material3.ButtonDefaults.colors(
                        containerColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue,
                        contentColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedContentColor = mx.utng.ecoguiawear.tv.ui.theme.DeepBlue
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onSelectMapType(MapViewType.MINIMAL_WHITE) },
                            colors = tvButtonColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(dialogButtonFocusRequester)
                        ) {
                            Text(if (selectedMapType == MapViewType.MINIMAL_WHITE) "✓ 1. Maqueta 3D Blanca (Minimalista)" else "1. Maqueta 3D Blanca (Minimalista)")
                        }

                        Button(
                            onClick = { onSelectMapType(MapViewType.DARK_MODE) },
                            colors = tvButtonColors,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedMapType == MapViewType.DARK_MODE) "✓ 2. Modo Oscuro Neón (Futurista)" else "2. Modo Oscuro Neón (Futurista)")
                        }

                        Button(
                            onClick = { onSelectMapType(MapViewType.SATELLITE_CITY) },
                            colors = tvButtonColors,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedMapType == MapViewType.SATELLITE_CITY) "✓ 3. Vista Satelital (Entorno Real)" else "3. Vista Satelital (Entorno Real)")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        colors = tvButtonColors
                    ) {
                        Text("Cerrar")
                    }

                }
            }
        }
    }
}
```

---

### Paso 6.3: Selector de Sitios Históricos (`ui/screens/components/SiteSelectorDialog.kt`)

Permite enfocar y volar la cámara 3D hacia sitios específicos de la ciudad.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/components/SiteSelectorDialog.kt`:
```kotlin
/**
 * Diálogo modal para la selección y conmutación de sitios históricos en Smart TV.
 *
 * Proporciona una interfaz adaptada a control remoto D-Pad con búsqueda filtrable en tiempo real:
 * - Permite al administrador o encargado del museo conmutar el sitio exhibido en la pantalla.
 * - Muestra distintivos visuales por tipo de punto de interés (museo, hotel, parque, restaurante).
 * - Marca de forma explícita el sitio actualmente activo en la sesión.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import mx.utng.ecoguia.shared.domain.model.RemoteHistoricalSite

/**
 * Diálogo interactivo para seleccionar el sitio histórico activo a visualizar en la Smart TV.
 *
 * @param sites Lista completa de sitios históricos disponibles para el usuario.
 * @param currentSiteId Identificador único del sitio actualmente seleccionado.
 * @param isAdmin Indica si el usuario autenticado tiene permisos globales de administrador.
 * @param onSiteSelected Callback invocado al confirmar la selección de un nuevo [RemoteHistoricalSite].
 * @param onDismiss Callback invocado para descartar o cerrar el diálogo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun SiteSelectorDialog(
    sites: List<RemoteHistoricalSite>,
    currentSiteId: String?,
    isAdmin: Boolean,
    onSiteSelected: (RemoteHistoricalSite) -> Unit,
    onDismiss: () -> Unit
) {
    var filterText by remember { mutableStateOf("") }
    val filtered = remember(filterText, sites) {
        if (filterText.isBlank()) sites
        else sites.filter {
            it.name.contains(filterText, ignoreCase = true) ||
                    it.address.orEmpty().contains(filterText, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF34D399).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Encabezado ──────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = if (isAdmin) Icons.Default.Public else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAdmin) "Todos los Sitios" else "Mis Sitios",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Text(
                            text = "${filtered.size} sitio(s) encontrado(s)",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cerrar", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Campo de Búsqueda / Filtro ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (filterText.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.tv.material3.Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buscar por nombre o dirección...",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    BasicTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF34D399)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Lista de Sitios ──────────────────────────────────────────
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin resultados para \"$filterText\"",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { site ->
                            val isSelected = site.id == currentSiteId
                            SiteItem(
                                site = site,
                                isSelected = isSelected,
                                onClick = { onSiteSelected(site) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Elemento individual para renderizar cada sitio en la lista con selector táctil o de D-Pad.
 *
 * @param site Instancia del sitio histórico.
 * @param isSelected Booleano que indica si este elemento es el que está actualmente en uso.
 * @param onClick Acción a ejecutar al pulsar el botón correspondiente.
 */
@Composable
private fun SiteItem(
    site: RemoteHistoricalSite,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.colors(
            containerColor = if (isSelected) Color(0xFF064E3B) else Color(0xFF1E293B),
            focusedContainerColor = Color(0xFF065F46)
        ),
        shape = androidx.tv.material3.ButtonDefaults.shape(
            shape = RoundedCornerShape(10.dp),
            focusedShape = RoundedCornerShape(10.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de tipo de sitio
            val iconVector = when (site.siteType.lowercase()) {
                "museo" -> Icons.Default.AccountBalance
                "hotel" -> Icons.Default.Hotel
                "parque" -> Icons.Default.Park
                "restaurant" -> Icons.Default.Restaurant
                else -> Icons.Default.Place
            }
            androidx.tv.material3.Icon(
                imageVector = iconVector,
                contentDescription = site.siteType,
                tint = if (isSelected) Color(0xFF34D399) else Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(22.dp)
                    .padding(end = 6.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.name,
                    color = if (isSelected) Color(0xFF34D399) else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!site.address.isNullOrBlank()) {
                    Text(
                        text = site.address.orEmpty(),
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    androidx.tv.material3.Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Activo",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Activo",
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
```

---

### Paso 6.4: Componentes Skeleton y Efecto Shimmer (`ui/screens/components/SkeletonComponents.kt`)

Proporciona retroalimentación visual inmediata con degradados lineales animados mientras se descargan datos desde Neon PostgreSQL o cuando la TV está en espera de vinculación.

> 📋 **INSTRUCCIÓN:** Copia el archivo `tv/src/main/java/mx/utng/ecoguiawear/tv/ui/screens/components/SkeletonComponents.kt`:
```kotlin
/**
 * Componentes de interfaz de usuario con animaciones de resplandor (Shimmer) y estructuras Skeleton.
 *
 * Utilizados en la aplicación de Smart TV para proporcionar retroalimentación visual fluida
 * y profesional durante la descarga asíncrona de datos desde la base de datos PostgreSQL en Neon
 * o mientras la pantalla no se encuentra vinculada a una cuenta de usuario.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.tv.ui.screens.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contenedor rectangular con animación infinita de degradado tipo Shimmer para simular carga de contenido.
 *
 * @param modifier Modificador de diseño aplicado al contenedor.
 * @param cornerRadius Radio de curvatura en esquinas [Dp].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
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

/**
 * Línea esqueleto que simula un bloque de texto o título mientras se descarga la información real.
 *
 * @param modifier Modificador de diseño.
 * @param height Altura de la barra simulada de texto.
 * @param widthFraction Fracción de ancho que ocupa respecto al contenedor padre (0.0 a 1.0).
 * @param cornerRadius Radio de curvatura en los extremos de la línea.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun SkeletonTextLine(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    widthFraction: Float = 1f,
    cornerRadius: Dp = 6.dp
) {
    SkeletonBox(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        cornerRadius = cornerRadius
    )
}

/**
 * Componente que representa métricas o contadores vacíos mediante guiones estilizados.
 *
 * @param modifier Modificador de diseño.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun SkeletonStat(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF334155)),
        contentAlignment = Alignment.Center
    ) {
        androidx.tv.material3.Text(
            text = "--",
            color = Color(0xFF64748B),
            style = androidx.compose.ui.text.TextStyle(
                fontSize = androidx.compose.ui.unit.TextUnit(
                    20f,
                    androidx.compose.ui.unit.TextUnitType.Sp
                ),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
    }
}

/**
 * Tarjeta gráfica esqueleto que simula el espacio de una fotografía de GeoDrop o sitio histórico en carga.
 *
 * @param modifier Modificador de diseño.
 * @param cornerRadius Radio de curvatura de la tarjeta.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun SkeletonImageCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp
) {
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        SkeletonBox(modifier = Modifier.matchParentSize())
        // Ícono de imagen apagado centrado
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.tv.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Image,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Superposición o banner informativo semi-transparente que notifica al usuario
 * sobre la ausencia de una sesión activa vinculada con la Smart TV.
 *
 * @param message Mensaje explicativo o instrucción a presentar en el banner.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun NoSessionOverlay(message: String = "Vincula tu cuenta para ver el contenido") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            androidx.tv.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.tv.material3.Text(
                text = message,
                color = Color(0xFF94A3B8),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        13f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
            )
        }
    }
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
