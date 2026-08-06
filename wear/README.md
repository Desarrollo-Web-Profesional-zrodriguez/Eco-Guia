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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Módulo compartido de la arquitectura: contiene entidades de Room, repositorios de Postgres y modelos comunes
    implementation(project(":shared"))

    // Integración de Jetpack Compose con el ciclo de vida de Actividades Android
    implementation(libs.activity.compose)

    // Pantalla de carga (Splash Screen) inicial del sistema Android
    implementation(libs.core.splashscreen)

    // Soporte para corrutinas de Kotlin en el hilo principal de Android y Play Services (await)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Gestión del ciclo de vida y estado reactivo (StateFlow / ViewModel) en Compose
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel.ktx)

    // APIs de Google Play Services: Data Layer para comunicación reloj-móvil y FusedLocationProviderClient para GPS
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.location)

    // Base de datos SQLite local mediante Room para persistencia offline de alertas de proximidad
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Bill of Materials (BOM) para sincronizar las versiones de Jetpack Compose UI
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)

    // Componentes nativos UI de Compose diseñados para pantallas circulares de Wear OS 3+
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.navigation)

    // Horologist: utilidades para optimizar layouts circulares, scroll con corona física y TimeText
    implementation(libs.horologist.compose.layout)
    implementation(libs.wear.tooling.preview)

    // Íconos extendidos de Material Design y componentes foundation avanzados
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.foundation)

    // Herramientas de depuración y vistas previas en Android Studio
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Pruebas unitarias
    testImplementation(libs.junit)
}
```

---

### Paso 1.3: Configurar el Manifiesto (`wear/src/main/AndroidManifest.xml`)

El manifiesto define los permisos de sensores, hardware de reloj y declara el servicio receptor de mensajes en segundo plano.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Declaración obligatoria para indicar que esta APK está diseñada para dispositivos Smartwatch -->
    <uses-feature
        android:name="android.hardware.type.watch"
        android:required="true" />

    <!-- Permisos de hardware: actuador vibratorio háptico y estado de encendido/mantenimiento en segundo plano -->
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!-- Permiso de red para consultas directas a la base de datos Neon PostgreSQL si aplica -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Permisos de geolocalización de alta precisión para el radar geodésico -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Permiso para notificaciones emergentes de alerta de proximidad en Wear OS 13+ -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault">

        <!-- Librerías del sistema Wear OS opcionales para retrocompatibilidad con relojes antiguos -->
        <uses-library
            android:name="com.google.android.wearable"
            android:required="false" />
        <uses-library
            android:name="wear-sdk"
            android:required="false" />

        <!-- Configuración de aplicación emparejada (standalone=false exige vinculación con el smartphone) -->
        <meta-data
            android:name="com.google.android.wearable.standalone"
            android:value="false" />
        <meta-data
            android:name="com.google.android.gms.wearable.capabilities"
            android:resource="@array/android_wear_capabilities" />

        <!-- Actividad principal del reloj que aloja la interfaz gráfica en Compose -->
        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:taskAffinity=""
            android:theme="@android:style/Theme.DeviceDefault">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Servicio de escucha en segundo plano para procesar mensajes RPC del teléfono (Wearable Data Layer) -->
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
/**
 * Modelos de dominio y estructuras de datos para el radar háptico de proximidad en Wear OS.
 *
 * Define los estados de interfaz, objetivos geográficos, configuraciones hápticas,
 * paradas de ruta y entidades de alerta consumidas por el ViewModel y los controladores del reloj.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.domain.model

/**
 * Categoría del elemento objetivo rastreado por el radar.
 */
enum class TargetType {
    /** Monumento, parroquia o sitio histórico oficial de Dolores Hidalgo. */
    HISTORIC_SITE,

    /** Cápsula cultural o hallazgo comunitario GeoDrop. */
    GEO_DROP
}

/**
 * Modos de operación y estados de navegación del radar de proximidad.
 */
enum class RadarMode {
    /** El radar está en reposo o suspendido para ahorrar batería. */
    PAUSED,

    /** El radar está escaneando activamente la posición GPS y calculando proximidad. */
    SCANNING,

    /** El usuario se encuentra siguiendo la aguja de la brújula hacia el objetivo. */
    FOLLOWING_ARROW,

    /** El usuario ha llegado al perímetro de proximidad del objetivo (<30m). */
    ARRIVED
}

/**
 * Niveles de intensidad para la retroalimentación vibratoria del motor háptico.
 */
enum class HapticStrength {
    /** Vibración sutil de bajo impacto energético. */
    LOW,

    /** Vibración estándar balanceada para caminatas al aire libre. */
    MEDIUM,

    /** Vibración enérgica y prolongada para entornos con mucho movimiento. */
    HIGH
}

/**
 * Representa un objetivo geográfico hacia el cual apunta el radar.
 *
 * @property id Identificador único del sitio o cápsula.
 * @property title Nombre principal o título del objetivo.
 * @property subtitle Descripción corta o categoría del sitio.
 * @property type Tipo de objetivo ([TargetType.HISTORIC_SITE] o [TargetType.GEO_DROP]).
 * @property distanceMeters Distancia estimada en línea recta expresada en metros.
 * @property bearingDegrees Ángulo de azimut respecto al norte magnético (0° a 360°).
 * @property latitude Coordenada de latitud geográfica opcional.
 * @property longitude Coordenada de longitud geográfica opcional.
 * @property isAutoTarget Indica si el objetivo fue seleccionado automáticamente por cercanía.
 */
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

/**
 * Resumen del progreso de una ruta turística activa transmitida desde el teléfono.
 *
 * @property title Nombre o temática de la ruta activa.
 * @property visitedStops Cantidad de sitios o paradas ya visitadas en la sesión.
 * @property totalStops Total de puntos de interés incluidos en la ruta.
 * @property nextStop Nombre del próximo punto de interés sugerido.
 * @property estimatedMinutes Tiempo estimado en minutos para completar el recorrido restante.
 * @property waypoints Lista detallada de coordenadas y puntos de paso que integran la ruta.
 */
data class RouteSummary(
    val title: String,
    val visitedStops: Int,
    val totalStops: Int,
    val nextStop: String,
    val estimatedMinutes: Int,
    val waypoints: List<Waypoint> = emptyList()
)

/**
 * Punto de parada o hito geográfico individual dentro de una ruta turística.
 *
 * @property id Identificador del punto de paso.
 * @property title Nombre del sitio histórico asociado.
 * @property latitude Coordenada de latitud del hito.
 * @property longitude Coordenada de longitud del hito.
 * @property isReached Indica si el usuario ya registró su llegada física a este punto.
 */
data class Waypoint(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val isReached: Boolean = false
)

/**
 * Ajustes de personalización para la respuesta háptica en el reloj.
 *
 * @property enabled Indica si las vibraciones de proximidad se encuentran activas.
 * @property strength Nivel de fuerza o intensidad configurado ([HapticStrength]).
 */
data class HapticSettings(
    val enabled: Boolean = true,
    val strength: HapticStrength = HapticStrength.MEDIUM
)

/**
 * Registro de una notificación o evento de proximidad emitido por el sistema.
 *
 * @property id Identificador único de la alerta.
 * @property message Contenido descriptivo del evento de proximidad.
 * @property type Categoría de la alerta (e.g., "GEODROP", "SITE", "INFO").
 * @property timestamp Marca de tiempo en milisegundos en que se generó la alerta.
 */
data class AlertEntity(
    val id: String,
    val message: String,
    val type: String,
    val timestamp: Long
)

/**
 * Estado inmutable completo de la interfaz de usuario del módulo Wear OS.
 *
 * @property isLinkedToPhone Indica si el reloj mantiene un canal de comunicación activo con la app móvil.
 * @property isStealthMode Modo discreto que apaga la pantalla y guía exclusivamente mediante pulsos hápticos.
 * @property mode Modo de navegación actual del radar ([RadarMode]).
 * @property isGpsEnabled Indica si el sensor GPS del dispositivo está encendido y accesible.
 * @property isCameraReady Indica si el hardware periférico y sensores de orientación se encuentran listos.
 * @property alerts Lista histórica de alertas de proximidad recibidas en la sesión.
 * @property currentHeading Orientación angular actual del reloj respecto al norte (grados).
 * @property target Objetivo geográfico activo hacia el cual se calculan distancia y rumbo.
 * @property routeSummary Información consolidada de la ruta turística sincronizada.
 * @property hapticSettings Preferencias activas de vibración.
 * @property lastAlert Mensaje o descripción del evento más reciente.
 * @property isRouteCompleted Indica si el usuario finalizó satisfactoriamente todos los hitos de la ruta.
 * @property nearbyAutoTargets Lista de objetivos cercanos descubiertos automáticamente por GPS.
 * @property selectedAutoIndex Índice del objetivo seleccionado dentro de la lista de auto-descubrimiento.
 */
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
/**
 * Contrato de repositorio para la gestión del radar, cálculo de proximidad y sincronización en Wear OS.
 *
 * Expone flujos observables del estado de navegación y operaciones para actualizar coordenadas,
 * rutas turísticas, alertas y retroalimentación háptica.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.domain.repository

import kotlinx.coroutines.flow.StateFlow
import mx.utng.ecoguiawear.domain.model.AlertEntity
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.domain.model.Waypoint

/**
 * Interfaz que define las operaciones del radar de proximidad y sincronización con el teléfono móvil.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
interface RadarRepository {
    /** Flujo inmutable y observable del estado global del radar. */
    val radarState: StateFlow<RadarUiState>

    /**
     * Actualiza el estado de vinculación física o por Bluetooth con el teléfono móvil.
     *
     * @param linked Verdadero si el canal con el teléfono está conectado.
     */
    fun setLinkedToPhone(linked: Boolean)

    /** Inicia el servicio de escaneo de proximidad continuo. */
    fun startRadar()

    /** Alterna entre el estado de escaneo activo y pausa del radar. */
    fun toggleRadar()

    /** Alterna la activación del modo discreto (pantalla apagada, solo háptico). */
    fun toggleStealthMode()

    /**
     * Establece explícitamente el estado del modo discreto.
     *
     * @param enabled Verdadero para activar el modo discreto.
     */
    fun setStealthMode(enabled: Boolean)

    /**
     * Asigna la lista de alertas históricas o en cola para la interfaz del reloj.
     *
     * @param alerts Colección de entidades [AlertEntity].
     */
    fun setAlerts(alerts: List<AlertEntity>)

    /**
     * Notifica el estado de disponibilidad de los sensores requeridos.
     *
     * @param gps Verdadero si el permiso y sensor GPS están disponibles.
     * @param camera Verdadero si los periféricos auxiliares están listos.
     */
    fun setPermissions(gps: Boolean, camera: Boolean)

    /**
     * Ajusta manualmente la distancia calculada hacia el objetivo activo.
     *
     * @param distance Distancia en metros.
     */
    fun setDistance(distance: Int)

    /**
     * Actualiza las estadísticas de progreso en la ruta turística sincronizada.
     *
     * @param visited Número de hitos visitados.
     * @param total Total de hitos de la ruta.
     */
    fun setRouteProgress(visited: Int, total: Int)

    /** Simula una aproximación secuencial paso a paso hacia el objetivo para pruebas. */
    fun simulateApproach()

    /** Restablece los valores del radar a su configuración de demostración inicial. */
    fun resetDemo()

    /** Registra la llegada formal al objetivo actual, emitiendo vibración háptica de éxito. */
    fun completeArrival()

    /**
     * Actualiza las preferencias de vibración del reloj.
     *
     * @param enabled Verdadero para habilitar respuesta táctil.
     * @param strength Nivel de fuerza seleccionado ([HapticStrength]).
     */
    fun updateHaptics(enabled: Boolean, strength: HapticStrength)

    /** Re-evalúa los sitios históricos y cápsulas más cercanas en base a las coordenadas actuales. */
    fun refreshNearbyTargets()

    /**
     * Sincroniza un objetivo turístico individual transmitido desde el teléfono móvil.
     *
     * @param id Identificador del sitio.
     * @param name Nombre comercial o monumental del sitio.
     * @param lat Latitud geográfica.
     * @param lng Longitud geográfica.
     */
    fun setSyncTarget(id: String, name: String, lat: Double, lng: Double)

    /**
     * Sincroniza una ruta turística guiada con múltiples puntos de paso.
     *
     * @param title Nombre de la ruta.
     * @param waypoints Lista ordenada de hitos [Waypoint].
     */
    fun setSyncRoute(title: String, waypoints: List<Waypoint>)

    /** Limpia la ruta activa del estado del reloj. */
    fun clearActiveRoute()

    /** Marca la ruta turística activa como completada en su totalidad. */
    fun markRouteCompleted()

    /** Cierra el diálogo o banner de felicitación de ruta completada. */
    fun dismissRouteCompleted()

    /** Selecciona el siguiente objetivo disponible en la lista de auto-descubrimiento. */
    fun selectNextAutoTarget()

    /**
     * Elimina una alerta específica de la lista por su identificador.
     *
     * @param id Identificador de la alerta a remover.
     */
    fun deleteAlert(id: String)

    /** Remueve todas las alertas acumuladas en la sesión. */
    fun clearAllAlerts()

    /** Selecciona el objetivo previo en la lista de auto-descubrimiento. */
    fun selectPreviousAutoTarget()

    /**
     * Actualiza las coordenadas GPS actuales del reloj y recalcula distancias hacia el objetivo.
     *
     * @param lat Latitud actual del usuario.
     * @param lng Longitud actual del usuario.
     */
    fun updateCurrentLocation(lat: Double, lng: Double)

    /**
     * Actualiza la orientación del compás magnético.
     *
     * @param heading Ángulo de orientación actual en grados (0° - 360°).
     */
    fun updateHeading(heading: Float)
}
```

---

## FASE 3: Capa de Datos y Sensores del Reloj (Data Layer)

### Paso 3.1: Controlador Háptico (`data/haptics/HapticController.kt`)

Gestiona el actuador vibratorio del reloj mediante `Vibrator` o `VibratorManager` (Android 12+), ofreciendo patrones de pulsación táctil según el tipo de evento y la intensidad configurada.

```kotlin
/**
 * Controlador de efectos hápticos y patrones de vibración para relojes inteligentes Wear OS.
 *
 * Centraliza la emisión de pulsos sensoriales para avisos de emparejamiento, cambios de estado,
 * proximidad a monumentos históricos y llegada a coordenadas de interés turístico.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import mx.utng.ecoguiawear.domain.model.HapticStrength

/**
 * Tipos de eventos sensoriales o pulsos hápticos reconocidos por el sistema Wear OS.
 */
enum class HapticPulse {
    /** Pulso de confirmación tras vincular exitosamente el reloj con el teléfono. */
    LINKED,

    /** Pulso breve de interacción o alternancia de interruptores/botones. */
    TOGGLE,

    /** Pulso de advertencia al ingresar al radio de proximidad intermedia (<100m). */
    NEARBY,

    /** Pulso sostenido de éxito al alcanzar físicamente el objetivo (<30m). */
    ARRIVED
}

/**
 * Administra el actuador vibratorio del smartwatch según la versión del sistema operativo.
 *
 * @param context Contexto de la aplicación para acceder a los servicios de hardware [Vibrator] o [VibratorManager].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class HapticController(context: Context) {

    private val appContext = context.applicationContext

    /**
     * Emite un pulso háptico con la duración e intensidad configuradas.
     *
     * @param type Tipo de pulso táctil a emitir ([HapticPulse]).
     * @param strength Nivel de fuerza de la vibración ([HapticStrength]).
     */
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

    /**
     * Obtiene la instancia del motor de vibración compatible con la versión de API del dispositivo.
     *
     * @return Instancia activa de [Vibrator].
     */
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
/**
 * Implementación principal del repositorio de navegación y radar de proximidad para Wear OS.
 *
 * Coordina la base de datos local SQLite/Room, la sincronización remota mediante [EcoGuiaRepositoryImpl],
 * el motor de vibración háptica [HapticController] y el cálculo de rumbos y distancias mediante
 * extensiones modulares.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.repository

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase
import mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl
import mx.utng.ecoguia.shared.domain.model.ConfigEntity
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.repository.extensions.*
import mx.utng.ecoguiawear.domain.model.*
import mx.utng.ecoguiawear.domain.repository.RadarRepository

/**
 * Repositorio de datos y lógica de radar en el reloj inteligente.
 *
 * @param context Contexto de la aplicación para inicializar Room y servicios de hardware.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
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
    internal var lastAutoSearchTime: Long = 0
    internal val AUTO_SEARCH_INTERVAL_MS = 30000L

    internal val lastAlertPerSiteTime = mutableMapOf<String, Long>()
    internal val THIRTY_MINUTES_MS = 30 * 60 * 1000L

    private var smoothedHeading: Float = 0f

    init {
        initStealthModeListener()
        initAlertsListener()
    }

    override fun setLinkedToPhone(linked: Boolean) {
        _radarState.update {
            it.copy(
                isLinkedToPhone = linked,
                lastAlert = if (linked) "Teléfono vinculado" else "Sin teléfono"
            )
        }
    }

    override fun startRadar() {
        _radarState.update { state ->
            val isRouteActive = state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted
            val nextMode = if (isRouteActive) RadarMode.FOLLOWING_ARROW else RadarMode.SCANNING
            state.copy(
                isLinkedToPhone = true,
                mode = nextMode,
                lastAlert = if (isRouteActive) "Ruta activa" else "Radar activo"
            )
        }
    }

    override fun toggleRadar() {
        _radarState.update { state ->
            val isRouteActive = state.routeSummary.waypoints.isNotEmpty() && !state.isRouteCompleted
            val activeMode = if (isRouteActive) RadarMode.FOLLOWING_ARROW else RadarMode.SCANNING
            val nextMode = when (state.mode) {
                RadarMode.PAUSED -> activeMode
                RadarMode.SCANNING -> RadarMode.PAUSED
                RadarMode.FOLLOWING_ARROW -> RadarMode.PAUSED
                RadarMode.ARRIVED -> activeMode
            }
            state.copy(
                mode = nextMode,
                lastAlert = if (nextMode == RadarMode.PAUSED) "Radar pausado" else "Radar activo"
            )
        }
    }

    override fun toggleStealthMode() {
        setStealthMode(!_radarState.value.isStealthMode)
    }

    override fun setStealthMode(enabled: Boolean) {
        scope.launch {
            dao.saveConfig(ConfigEntity("stealth_mode", if (enabled) "1" else "0"))
        }
        _radarState.update { it.copy(isStealthMode = enabled) }
    }

    override fun setAlerts(alerts: List<AlertEntity>) {
        saveAlertsExt(alerts)
    }

    override fun deleteAlert(id: String) {
        deleteAlertExt(id)
    }

    override fun clearAllAlerts() {
        clearAllAlertsExt()
    }

    override fun setPermissions(gps: Boolean, camera: Boolean) {
        _radarState.update { it.copy(isGpsEnabled = gps, isCameraReady = camera) }
    }

    override fun setDistance(distance: Int) {
        _radarState.update { state ->
            val nextTarget = nextTargetForDistance(state.target, distance)
            val nextMode = if (distance == 0) RadarMode.ARRIVED else RadarMode.SCANNING
            state.copy(mode = nextMode, target = nextTarget)
        }
    }

    override fun setRouteProgress(visited: Int, total: Int) {
        setRouteProgressExt(visited, total)
    }

    override fun simulateApproach() {
        simulateApproachExt()
    }

    override fun completeArrival() {
        completeArrivalExt()
    }

    override fun resetDemo() {
        _radarState.value = RadarUiState(isLinkedToPhone = true, mode = RadarMode.SCANNING)
    }

    override fun updateHaptics(enabled: Boolean, strength: HapticStrength) {
        _radarState.update {
            it.copy(
                hapticSettings = it.hapticSettings.copy(enabled = enabled, strength = strength),
                lastAlert = if (enabled) "Vibración activa" else "Vibración apagada"
            )
        }
    }

    override fun refreshNearbyTargets() {
        refreshNearbyTargetsExt()
    }

    override fun setSyncTarget(id: String, name: String, lat: Double, lng: Double) {
        setSyncTargetExt(id, name, lat, lng)
    }

    override fun setSyncRoute(title: String, waypoints: List<Waypoint>) {
        setSyncRouteExt(title, waypoints)
    }

    override fun clearActiveRoute() {
        clearActiveRouteExt()
    }

    override fun markRouteCompleted() {
        _radarState.update {
            it.copy(
                mode = RadarMode.SCANNING,
                isRouteCompleted = true,
                lastAlert = "Ruta Completada"
            )
        }
    }

    override fun dismissRouteCompleted() {
        _radarState.update { it.copy(isRouteCompleted = false) }
        clearActiveRoute()
    }

    override fun updateCurrentLocation(lat: Double, lng: Double) {
        updateCurrentLocationExt(lat, lng)
    }

    override fun selectNextAutoTarget() {
        selectNextAutoTargetExt()
    }

    override fun selectPreviousAutoTarget() {
        selectPreviousAutoTargetExt()
    }

    override fun updateHeading(heading: Float) {
        val diff = Math.abs(heading - smoothedHeading)
        if (diff > mx.utng.ecoguia.shared.config.EcoGuiaConfig.COMPASS_HEADING_THRESHOLD_DEGREES) {
            val factor = mx.utng.ecoguia.shared.config.EcoGuiaConfig.COMPASS_SMOOTHING_FACTOR
            smoothedHeading = smoothedHeading + factor * (heading - smoothedHeading)
            _radarState.update { it.copy(currentHeading = smoothedHeading) }
        }
    }

    internal fun nextTargetForDistance(current: RadarTarget, distance: Int): RadarTarget {
        return if (distance == 0) {
            current.copy(
                distanceMeters = 0,
                title = "Museo alcanzado",
                subtitle = "Abre el celular para ver detalles del sitio",
                type = TargetType.HISTORIC_SITE,
                bearingDegrees = 0f
            )
        } else {
            current.copy(distanceMeters = distance)
        }
    }
}
```

---

---

### Paso 3.3: Extensiones Modulares de Lógica (`data/repository/extensions/`)

Para mantener una arquitectura limpia y desacoplada, la lógica especializada se organiza en tres funciones de extensión de `RadarRepositoryImpl`:

#### 1. `data/repository/extensions/RadarAlertsExt.kt`
Administra el almacenamiento persistente de alertas en Room, la purga automática de notificaciones de más de 3 horas y la emisión de avisos de proximidad con el historial de notificaciones.

```kotlin
/**
 * Extensiones del repositorio para la administración, persistencia y depuración de alertas en Room DB.
 *
 * Mantiene la reactividad del modo discreto y asegura que el historial de notificaciones
 * de proximidad en el smartwatch se mantenga optimizado y libre de registros obsoletos (>3 horas).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.repository.extensions

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.domain.model.AlertEntity

internal fun RadarRepositoryImpl.initStealthModeListener() {
    scope.launch {
        dao.getConfigFlow("stealth_mode").collect { config ->
            val isStealth = config?.value == "1"
            _radarState.update { it.copy(isStealthMode = isStealth) }
        }
    }
}

internal fun RadarRepositoryImpl.initAlertsListener() {
    scope.launch {
        val threeHoursAgo = System.currentTimeMillis() - (3 * 3600 * 1000L)
        try { dao.deleteOldAlerts(threeHoursAgo) } catch (_: Exception) {}

        dao.getAllAlerts().collect { alerts ->
            val now = System.currentTimeMillis()
            val validAlerts = alerts
                .filter { now - it.timestamp <= 3 * 3600 * 1000L }
                .map { AlertEntity(it.id, it.message, it.type, it.timestamp) }
            _radarState.update { it.copy(alerts = validAlerts) }
        }
    }
}

internal fun RadarRepositoryImpl.saveAlertsExt(alerts: List<AlertEntity>) {
    scope.launch {
        alerts.forEach { 
            dao.insertAlert(mx.utng.ecoguia.shared.domain.model.AlertEntity(it.id, it.message, it.type, it.timestamp))
        }
    }
}
```

#### 2. `data/repository/extensions/RadarAutoSearchExt.kt`
Consulta los sitios turísticos en SQLite o Neon PostgreSQL y selecciona automáticamente el objetivo más cercano calculando la distancia Haversine y el ángulo de rumbo geodésico (*bearing*).

```kotlin
/**
 * Extensiones del repositorio para el auto-descubrimiento de sitios turísticos y monumentos en Neon DB.
 *
 * Realiza búsquedas georreferenciadas en un radio de hasta 50 km alrededor de las coordenadas GPS actuales,
 * seleccionando el top 3 de sitios más cercanos y administrando la alternancia rápida de objetivos.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.repository.extensions

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.domain.model.RadarTarget
import mx.utng.ecoguiawear.domain.model.TargetType

internal fun RadarRepositoryImpl.refreshNearbyTargetsExt() {
    scope.launch {
        try {
            _radarState.update { it.copy(lastAlert = "Buscando sitios históricos...") }
            val radius = mx.utng.ecoguia.shared.config.EcoGuiaConfig.SEARCH_RADIUS_METERS
            val nearbySites = remoteRepository.getNearbySites(currentLat, currentLng, radius)
            if (nearbySites.isNotEmpty()) {
                val top3 = nearbySites.mapNotNull { site ->
                    val siteLat = site.getComputedLatitude() ?: return@mapNotNull null
                    val siteLng = site.getComputedLongitude() ?: return@mapNotNull null
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(currentLat, currentLng, siteLat, siteLng, results)
                    val dist = results[0].toInt()
                    RadarTarget(
                        id = site.id,
                        title = site.name,
                        subtitle = "Sitio histórico (50km)",
                        type = TargetType.HISTORIC_SITE,
                        distanceMeters = dist,
                        bearingDegrees = 0f,
                        latitude = siteLat,
                        longitude = siteLng,
                        isAutoTarget = true
                    ) to dist
                }.sortedBy { it.second }.take(3).map { it.first }

                val first = top3.firstOrNull() ?: return@launch
                _radarState.update { state ->
                    state.copy(
                        nearbyAutoTargets = top3,
                        selectedAutoIndex = 0,
                        target = first,
                        lastAlert = "Sitios cercanos: ${top3.size}"
                    )
                }
            }
        } catch (e: Exception) {
            _radarState.update { it.copy(lastAlert = "Error Neon: ${e.message}") }
        }
    }
}
```

#### 3. `data/repository/extensions/RadarRouteSyncExt.kt`
Sincroniza rutas guiadas con múltiples waypoints, actualiza el progreso paso a paso y emite vibraciones hápticas y notificaciones de llegada (`ARRIVED`) al acercarse a menos de 30 metros del hito activo.

```kotlin
/**
 * Extensiones del repositorio para la sincronización de rutas activas, waypoints y cálculo geodésico en Wear OS.
 *
 * Administra el avance punto por punto de rutas turísticas transmitidas desde el teléfono,
 * calculando la distancia Haversine, el rumbo angular hacia el hito activo y disparando
 * los eventos de llegada con pulsos hápticos [HapticPulse.ARRIVED].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.repository.extensions

import kotlinx.coroutines.flow.update
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.domain.model.*

internal fun RadarRepositoryImpl.setSyncTargetExt(id: String, name: String, lat: Double, lng: Double) {
    _radarState.update {
        it.copy(
            mode = RadarMode.FOLLOWING_ARROW,
            target = RadarTarget(
                id = id,
                title = name,
                subtitle = "Sincronizado desde móvil",
                type = TargetType.HISTORIC_SITE,
                distanceMeters = 50,
                bearingDegrees = 0f,
                latitude = lat,
                longitude = lng,
                isAutoTarget = false
            ),
            lastAlert = "Nuevo objetivo: $name"
        )
    }
}

internal fun RadarRepositoryImpl.setSyncRouteExt(title: String, waypoints: List<Waypoint>) {
    _radarState.update {
        it.copy(
            mode = RadarMode.FOLLOWING_ARROW,
            routeSummary = RouteSummary(
                title = title,
                visitedStops = waypoints.count { wp -> wp.isReached },
                totalStops = waypoints.size,
                nextStop = waypoints.firstOrNull { wp -> !wp.isReached }?.title ?: "Fin de ruta",
                estimatedMinutes = waypoints.count { wp -> !wp.isReached } * 5,
                waypoints = waypoints
            ),
            lastAlert = "Ruta activa: $title"
        )
    }
}
```

---

### Paso 3.4: Manejo de Sensores y FusedLocation (`data/wear/`)

El reloj inteligente utiliza `LocationHelper` para consultar periódicamente las coordenadas del GPS mediante `FusedLocationProviderClient`, mientras que `SensorHelper` procesa el acelerómetro y magnetómetro para calcular la brújula orientada al norte magnético.

#### `data/wear/LocationHelper.kt`
```kotlin
/**
 * Utilidad de geolocalización GPS y cálculo de rumbos/distancias geodésicas en Wear OS.
 *
 * Utiliza [com.google.android.gms.location.FusedLocationProviderClient] para actualizaciones
 * periódicas de alta precisión y métodos estáticos para cálculos esféricos entre coordenadas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

/**
 * Gestor de ubicación en tiempo real para el smartwatch.
 *
 * @param context Contexto de la aplicación para inicializar el cliente de localización.
 * @param onLocationUpdate Función lambda invocada al registrar una nueva posición GPS válida.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class LocationHelper(
    private val context: Context,
    private val onLocationUpdate: (Location) -> Unit
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { onLocationUpdate(it) }
        }
    }

    /**
     * Inicia la suscripción a actualizaciones de ubicación continua cada 2 segundos.
     */
    @SuppressLint("MissingPermission")
    fun startUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()
        
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error al iniciar GPS: ${e.message}")
        }
    }

    /**
     * Cancela la recepción de actualizaciones GPS para ahorrar batería.
     */
    fun stopUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        /**
         * Calcula el ángulo de rumbo (bearing) en grados desde un punto inicial hacia un destino.
         *
         * @param startLat Latitud del punto de partida.
         * @param startLng Longitud del punto de partida.
         * @param endLat Latitud del punto de llegada.
         * @param endLng Longitud del punto de llegada.
         * @return Ángulo de rumbo en grados respecto al norte.
         */
        fun calculateBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
            val startLocation = Location("").apply {
                latitude = startLat
                longitude = startLng
            }
            val endLocation = Location("").apply {
                latitude = endLat
                longitude = endLng
            }
            return startLocation.bearingTo(endLocation)
        }

        /**
         * Calcula la distancia geodésica en línea recta expresada en metros entre dos coordenadas.
         *
         * @param startLat Latitud de origen.
         * @param startLng Longitud de origen.
         * @param endLat Latitud de destino.
         * @param endLng Longitud de destino.
         * @return Distancia en metros.
         */
        fun calculateDistance(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
            val results = FloatArray(1)
            Location.distanceBetween(startLat, startLng, endLat, endLng, results)
            return results[0]
        }
    }
}
```

#### `data/wear/SensorHelper.kt`
```kotlin
/**
 * Lector de orientación espacial y compás magnético para relojes Wear OS.
 *
 * Combina las lecturas del acelerómetro ([android.hardware.Sensor.TYPE_ACCELEROMETER]) y del
 * magnetómetro ([android.hardware.Sensor.TYPE_MAGNETIC_FIELD]) mediante matrices de rotación
 * para calcular el azimut absoluto normalizado (0° - 360°).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Gestor de sensores de movimiento y orientación del reloj.
 *
 * @param context Contexto de la aplicación para obtener el [SensorManager].
 * @param onHeadingUpdate Callback invocado con el ángulo de orientación actualizado (en grados).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class SensorHelper(
    context: Context,
    private val onHeadingUpdate: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    /**
     * Registra los observadores de sensores con tasa de muestreo de interfaz de usuario ([SensorManager.SENSOR_DELAY_UI]).
     */
    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    /**
     * Desregistra los listeners de sensores para liberar hardware y ahorrar energía.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Callback de cambio de valores en sensores de aceleración o campo magnético.
     *
     * @param event Datos del evento del sensor físico.
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values
        }

        if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                
                val azimuthRadians = orientation[0]
                val azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
                val normalizedHeading = (azimuthDegrees + 360) % 360
                onHeadingUpdate(normalizedHeading.toFloat())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
```

---

### Paso 3.5: Comunicación Wearable Data Layer (`data/wear/`)

Para coordinar el smartwatch con la app móvil se utilizan tres componentes: `PhoneMessageClient` para transmitir acciones del reloj hacia el smartphone, `WearMessageListener` para interpretar payloads RPC entrantes, y `EcoWearMessageService` para recibir eventos en segundo plano.

#### `data/wear/PhoneMessageClient.kt`
```kotlin
/**
 * Emisor de mensajes y comandos desde el reloj inteligente hacia el teléfono móvil emparejado.
 *
 * Utiliza la API Wearable [com.google.android.gms.wearable.MessageClient] y [com.google.android.gms.wearable.CapabilityClient]
 * para enviar eventos de llegada, cambios de estado del radar o solicitudes de apertura de cámara.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Cliente de mensajería Wearable para comunicación bidireccional Reloj -> Teléfono.
 *
 * @param context Contexto de la aplicación para instanciar las APIs de Google Play Services Wearable.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class PhoneMessageClient(private val context: Context) {

    /**
     * Envía asíncronamente un mensaje a todos los nodos de teléfono conectados con la capacidad registrada.
     *
     * @param path Ruta o endpoint del mensaje Wearable.
     * @param payload Contenido en texto plano del mensaje a transmitir.
     */
    suspend fun sendRadarEvent(path: String, payload: String) {
        try {
            val capabilityInfo = Wearable.getCapabilityClient(context)
                .getCapability(PHONE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
            val nodes = capabilityInfo.nodes.ifEmpty {
                Wearable.getNodeClient(context).connectedNodes.await()
            }

            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, path, payload.toByteArray())
                    .await()
            }
        } catch (error: Exception) {
            Log.w("EcoGuiaWear", "No se pudo enviar evento al telefono", error)
        }
    }

    companion object {
        const val PHONE_CAPABILITY = "eco_guia_phone_receiver"
        const val PATH_LINKED = "/eco-guia/wear/linked"
        const val PATH_RADAR_STATE = "/eco-guia/wear/radar-state"
        const val PATH_OPEN_CAMERA = "/eco-guia/phone/open-camera"
    }
}
```

#### `data/wear/WearMessageListener.kt`
```kotlin
/**
 * Despachador y decodificador de mensajes de la capa Wearable para el reloj inteligente.
 *
 * Parsea los payloads transmitidos desde el teléfono (rutas, waypoints, objetivos, estados de simulación
 * y alertas) y delega las operaciones correspondientes al [RadarRepository].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import com.google.android.gms.wearable.MessageEvent
import mx.utng.ecoguiawear.domain.repository.RadarRepository

/**
 * Escucha y procesador de eventos de mensajería Wearable.
 *
 * @param repository Repositorio [RadarRepository] donde se aplicarán los cambios de estado solicitados.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class WearMessageListener(
    private val repository: RadarRepository
) {
    fun onMessageReceived(event: MessageEvent) {
        val payload = String(event.data)
        when (event.path) {
            "/eco-guia/simulate/link" -> repository.setLinkedToPhone(payload.toBoolean())
            "/eco-guia/simulate/proximity" -> {
                if (payload == "step") repository.simulateApproach()
                else payload.toIntOrNull()?.let { repository.setDistance(it) }
            }
            "/eco-guia/sync/target" -> {
                val parts = payload.split("|")
                if (parts.size == 4) {
                    repository.setSyncTarget(parts[0], parts[1], parts[2].toDouble(), parts[3].toDouble())
                }
            }
        }
    }
}
```

#### `data/wear/EcoWearMessageService.kt`
```kotlin
/**
 * Servicio en segundo plano para la recepción de mensajes del teléfono en Wear OS.
 *
 * Escucha eventos del [com.google.android.gms.wearable.WearableListenerService], procesa comandos
 * de rutas turísticas, paradas y sitios históricos, y genera notificaciones del sistema en el reloj.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.data.wear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl

/**
 * Servicio del sistema Wear OS que escucha los mensajes enviados desde la app del teléfono móvil.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class EcoWearMessageService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        val path = messageEvent.path
        val payload = String(messageEvent.data, Charsets.UTF_8)

        val repository = RadarRepositoryImpl(applicationContext)
        val listener = WearMessageListener(repository)
        listener.onMessageReceived(messageEvent)

        if (path.startsWith("/eco-guia/")) {
            showSystemNotification(path, payload)
        }
    }

    private fun showSystemNotification(path: String, payload: String) {
        val channelId = "wear_eco_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas EcoGuía",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

---

## FASE 4: Capa de Presentación (Presentation Layer & UI)

### Paso 4.1: Sistema de Diseño y Tokens Wear (`presentation/theme/EcoGuiaWearTheme.kt`)

La paleta cromática utiliza negro profundo (`#050B10`) para optimizar el consumo en displays AMOLED, con acentos en Jade colonial (`#26A69A`) y Oro histórico (`#C5A059`).

```kotlin
/**
 * Sistema de diseño, paleta cromática y tema visual para Wear OS.
 *
 * Configura los colores de alto contraste con fondo negro profundo ([EcoGuiaColors.Background])
 * optimizados para displays circulares AMOLED y bajo consumo de energía en smartwatches.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/**
 * Tokens de color para la interfaz de usuario en Wear OS.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
object EcoGuiaColors {
    /** Color de fondo negro AMOLED para máxima eficiencia energética. */
    val Background = Color(0xFF050B10)

    /** Color de superficie para tarjetas y chips interactivos. */
    val Surface = Color(0xFF0E2A3F)

    /** Tono azul petróleo profundo para bordes y fondos de radar. */
    val DeepBlue = Color(0xFF05111A)

    /** Color dorado colonial representativo de patrimonio histórico. */
    val Gold = Color(0xFFC5A059)

    /** Tono jade ecológico para acentos de navegación y progreso. */
    val Jade = Color(0xFF26A69A)

    /** Color principal de texto en alto contraste. */
    val Text = Color(0xFFF7FAFC)

    /** Color secundario atenuado para etiquetas y distancias. */
    val Muted = Color(0xFFB8C6D1)

    /** Tono ámbar/dorado para notificaciones y alertas. */
    val Alert = Color(0xFFE4B84A)
}

/**
 * Tema principal de Wear Compose basado en Material 3 Wear.
 *
 * @param content Contenido composable envuelto en el tema de la aplicación.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
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

Los componentes de interfaz están optimizados para pantallas circulares Wear OS y consumo de energía en displays AMOLED:

#### 1. `presentation/components/EcoWearScaffold.kt`
Envoltorio base para pantallas de reloj que integra `ScreenScaffold`, `ScalingLazyColumn` y soporte directo para bisel/corona física (*Rotary Input*).

```kotlin
/**
 * Estructura visual base (Scaffold) y soporte para bisel rotatorio (Rotary Input) en Wear OS.
 *
 * Envuelve las pantallas dentro de un [androidx.wear.compose.material3.ScreenScaffold] con soporte
 * para [androidx.wear.compose.foundation.lazy.ScalingLazyColumn] y fondo negro AMOLED de alta eficiencia energética.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.ScreenScaffold
import kotlinx.coroutines.android.awaitFrame
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun EcoWearScaffold(
    modifier: Modifier = Modifier,
    requestFocus: Boolean = true,
    content: ScalingLazyListScope.() -> Unit
) {
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            awaitFrame()
            focusRequester.requestFocus()
        }
    }

    ScreenScaffold(
        scrollState = listState,
        modifier = modifier
            .fillMaxSize()
            .background(EcoGuiaColors.Background)
    ) {
        ScalingLazyColumn(
            state = listState,
            autoCentering = null,
            modifier = Modifier
                .fillMaxSize()
                .rotaryScrollable(
                    behavior = RotaryScrollableDefaults.behavior(listState),
                    focusRequester = focusRequester
                )
                .focusRequester(focusRequester),
            contentPadding = PaddingValues(start = 10.dp, top = 24.dp, end = 10.dp, bottom = 24.dp),
            content = content
        )
    }
}
```

#### 2. `presentation/components/CompassArrow.kt`
Renderiza en Canvas la aguja de navegación orientada dinámicamente según el azimut del sitio histórico objetivo y la retícula de mira telescópica.

```kotlin
/**
 * Renderizador en Canvas de la rosa de los vientos y aguja de navegación en Wear OS.
 *
 * Dibuja los círculos concéntricos de mira telescópica, retícula en cruz y aguja estilizada
 * en tono dorado que rota dinámicamente según el rumbo objetivo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun CompassArrow(
    bearingDegrees: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 4.dp.toPx()

        drawCircle(color = EcoGuiaColors.DeepBlue.copy(alpha = 0.5f), center = center, radius = radius)
        drawCircle(color = EcoGuiaColors.Gold.copy(alpha = 0.3f), center = center, radius = radius, style = Stroke(width = 1.dp.toPx()))

        val crosshairColor = EcoGuiaColors.Gold.copy(alpha = 0.2f)
        drawLine(color = crosshairColor, start = Offset(center.x, center.y - radius), end = Offset(center.x, center.y + radius), strokeWidth = 0.5.dp.toPx())
        drawLine(color = crosshairColor, start = Offset(center.x - radius, center.y), end = Offset(center.x + radius, center.y), strokeWidth = 0.5.dp.toPx())

        rotate(degrees = bearingDegrees, pivot = center) {
            val arrowPath = Path().apply {
                moveTo(center.x, center.y - radius + 10.dp.toPx())
                lineTo(center.x - 14.dp.toPx(), center.y + 14.dp.toPx())
                lineTo(center.x, center.y + 4.dp.toPx())
                lineTo(center.x + 14.dp.toPx(), center.y + 14.dp.toPx())
                close()
            }
            drawPath(path = arrowPath, color = EcoGuiaColors.Gold)
        }

        drawCircle(color = EcoGuiaColors.Gold, center = center, radius = 2.dp.toPx())
    }
}
```

#### 3. `presentation/components/CircularStatus.kt`
Indicador circular de progreso que ilustra el porcentaje de avance de la ruta turística o la distancia remanente hacia el destino.

```kotlin
/**
 * Indicador visual circular para la representación de porcentajes y avances en Wear OS.
 *
 * Renderiza una barra perimetral curva con anillo de fondo y texto centrado, adaptado
 * a pantallas circulares AMOLED.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

@Composable
fun CircularStatus(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier,
    progressColor: Color = EcoGuiaColors.Jade,
    trackColor: Color = EcoGuiaColors.DeepBlue
) {
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(120.dp),
            startAngle = 270f,
            indicatorColor = progressColor,
            trackColor = trackColor,
            strokeWidth = 12.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.title1,
            color = EcoGuiaColors.Text
        )
    }
}
```

---

### Paso 4.3: State Management con `RadarViewModel.kt`

El `RadarViewModel` expone el estado inmutable `radarState` y conecta la interacción de los composables con las operaciones del `RadarRepository` y la emisión de señales táctiles mediante `HapticController`.

#### `presentation/RadarViewModel.kt`
```kotlin
/**
 * ViewModel central de la experiencia táctil, cartográfica y háptica en Wear OS.
 *
 * Mantiene el flujo observable [state], procesa eventos de usuario en pantalla, dispara pulsos hápticos
 * y sincroniza estados hacia el teléfono móvil emparejado mediante [PhoneMessageClient].
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.haptics.HapticPulse
import mx.utng.ecoguiawear.data.wear.PhoneMessageClient
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.repository.RadarRepository

/**
 * ViewModel principal del smartwatch.
 *
 * @param repository Repositorio de acceso a datos y estado del radar.
 * @param hapticController Controlador del motor de vibración háptica.
 * @param phoneMessageClient Cliente de mensajería Wearable hacia el teléfono.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class RadarViewModel(
    private val repository: RadarRepository,
    private val hapticController: HapticController,
    private val phoneMessageClient: PhoneMessageClient
) : ViewModel() {

    /** Flujo de estado inmutable observado por los composables de la interfaz. */
    val state = repository.radarState

    /** Confirma el emparejamiento manual con el teléfono móvil. */
    fun pairWithPhone() {
        repository.setLinkedToPhone(true)
        pulse(HapticPulse.LINKED)
        sendPhoneEvent(PhoneMessageClient.PATH_LINKED, "linked")
    }

    /** Inicia el radar en modo demostración y actualiza sitios históricos desde Neon DB. */
    fun startDemo() {
        repository.startRadar()
        repository.refreshNearbyTargets()
        pulse(HapticPulse.LINKED)
        sendPhoneEvent(PhoneMessageClient.PATH_RADAR_STATE, "demo-started")
    }

    /** Solicita una recarga explícita de sitios cercanos desde el backend en la nube. */
    fun refreshFromCloud() {
        repository.refreshNearbyTargets()
    }

    /** Alterna el estado activo/pausa del radar emitiendo un pulso táctil. */
    fun toggleRadar() {
        repository.toggleRadar()
        pulse(HapticPulse.TOGGLE)
        sendPhoneEvent(PhoneMessageClient.PATH_RADAR_STATE, state.value.mode.name)
    }

    /** Selecciona el siguiente objetivo descubierto automáticamente por GPS. */
    fun selectNextAutoTarget() {
        repository.selectNextAutoTarget()
        pulse(HapticPulse.TOGGLE)
    }

    /** Selecciona el objetivo anterior en la lista de auto-descubrimiento. */
    fun selectPreviousAutoTarget() {
        repository.selectPreviousAutoTarget()
        pulse(HapticPulse.TOGGLE)
    }

    /** Alterna el modo discreto (apagado de pantalla para guiado exclusivo por vibración). */
    fun toggleStealthMode() {
        repository.toggleStealthMode()
        pulse(HapticPulse.TOGGLE)
    }

    /** Simula una aproximación secuencial hacia el objetivo para pruebas. */
    fun simulateApproach() {
        val previousDistance = state.value.target.distanceMeters
        repository.simulateApproach()
        val nextDistance = state.value.target.distanceMeters

        when {
            nextDistance == 0 -> pulse(HapticPulse.ARRIVED)
            previousDistance > 20 && nextDistance <= 20 -> pulse(HapticPulse.NEARBY)
        }
    }

    /** Restablece los datos de demostración a su estado inicial. */
    fun resetDemo() {
        repository.resetDemo()
        pulse(HapticPulse.TOGGLE)
    }

    /** Confirma la llegada al sitio actual. */
    fun completeArrival() {
        repository.completeArrival()
        pulse(HapticPulse.ARRIVED)
        sendPhoneEvent(PhoneMessageClient.PATH_RADAR_STATE, "arrived")
    }

    /** Actualiza la intensidad de respuesta háptica. */
    fun updateHaptics(enabled: Boolean, strength: HapticStrength = HapticStrength.MEDIUM) {
        repository.updateHaptics(enabled, strength)
        if (enabled) pulse(HapticPulse.TOGGLE)
    }

    /** Notifica al teléfono para abrir el visor AR de cámara. */
    fun openPhoneCamera() {
        pulse(HapticPulse.TOGGLE)
        sendPhoneEvent(PhoneMessageClient.PATH_OPEN_CAMERA, "open")
    }

    /** Borra una alerta por su ID. */
    fun deleteAlert(id: String) = repository.deleteAlert(id)

    /** Limpia todo el historial de alertas. */
    fun clearAllAlerts() = repository.clearAllAlerts()

    /** Cierra el diálogo de ruta completada. */
    fun dismissRouteCompleted() = repository.dismissRouteCompleted()

    private fun pulse(pulse: HapticPulse) {
        val settings = state.value.hapticSettings
        if (settings.enabled) {
            hapticController.pulse(pulse, settings.strength)
        }
    }

    private fun sendPhoneEvent(path: String, payload: String) {
        viewModelScope.launch {
            phoneMessageClient.sendRadarEvent(path, payload)
        }
    }
}
```

---

### Paso 4.4: Navegación y Paginador Horizontal (`presentation/navigation/`)

La aplicación implementa navegación por gestos mediante `HorizontalPager` de 4 páginas laterales y soporte para `HorizontalPageIndicator`:

#### `presentation/navigation/RadarPagerScreen.kt`
```kotlin
/**
 * Contenedor de páginas horizontales (HorizontalPager) para la navegación táctil en Wear OS.
 *
 * Permite alternar mediante deslizamiento lateral entre el Modo Discreto, el Radar Principal,
 * la Brújula con aguja animada y el Resumen de la Ruta Turística activa.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import kotlinx.coroutines.launch
import mx.utng.ecoguiawear.presentation.RadarViewModel
import mx.utng.ecoguiawear.presentation.screens.CompassScreen
import mx.utng.ecoguiawear.presentation.screens.RadarScreen
import mx.utng.ecoguiawear.presentation.screens.RouteSummaryScreen
import mx.utng.ecoguiawear.presentation.screens.StealthRadarScreen

/**
 * Pantalla contenedora de paginación horizontal en el smartwatch.
 *
 * @param viewModel ViewModel del radar.
 * @param onNavigateToPairing Callback para volver a la pantalla de emparejamiento.
 * @param onNavigateToAlerts Callback para navegar al historial de alertas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
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
            val isActive = pagerState.currentPage == page
            
            when (page) {
                0 -> StealthRadarScreen(
                    state = state,
                    onToggleStealth = viewModel::toggleStealthMode,
                    onNavigateNext = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onNavigateBack = onNavigateToPairing,
                    requestFocus = isActive
                )
                1 -> RadarScreen(
                    state = state,
                    onToggleRadar = viewModel::toggleRadar,
                    onApproachDemo = viewModel::simulateApproach,
                    onOpenCompass = { scope.launch { pagerState.animateScrollToPage(2) } },
                    onOpenAlert = onNavigateToAlerts,
                    onOpenArrival = { },
                    onOpenSummary = { if (isRouteActive) scope.launch { pagerState.animateScrollToPage(3) } },
                    onOpenSettings = { },
                    onSelectNextAutoTarget = viewModel::selectNextAutoTarget,
                    onSelectPreviousAutoTarget = viewModel::selectPreviousAutoTarget,
                    onRefresh = viewModel::refreshFromCloud,
                    onNavigateBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                    requestFocus = isActive
                )
                2 -> CompassScreen(
                    state = state,
                    onNavigateBack = { scope.launch { pagerState.animateScrollToPage(1) } },
                    requestFocus = isActive
                )
                3 -> if (isRouteActive) {
                    RouteSummaryScreen(
                        state = state,
                        onNavigateBack = { scope.launch { pagerState.animateScrollToPage(1) } },
                        requestFocus = isActive
                    )
                }
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

A continuación se detalla la implementación en Jetpack Compose para las 11 pantallas del reloj inteligente:

#### 1. `presentation/screens/PairingScreen.kt`
Diagnóstico de conexión inicial: muestra el estado del enlace Bluetooth con el smartphone, disponibilidad de GPS y sensor de la cámara.

```kotlin
/**
 * Pantalla inicial de verificación de estado y emparejamiento con el teléfono móvil en Wear OS.
 *
 * Muestra el estado de vinculación Bluetooth/Wearable, disponibilidad de permisos GPS y estado
 * de la cámara/módulo AR del móvil.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla de conexión y estado inicial con el smartphone.
 *
 * @param state Estado global reactivo del radar.
 * @param onPairWithPhone Callback para forzar el emparejamiento.
 * @param onStartDemo Callback para iniciar el modo demostración.
 * @param onViewAlerts Callback opcional para navegar a las alertas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun PairingScreen(
    state: RadarUiState,
    onPairWithPhone: () -> Unit,
    onStartDemo: () -> Unit,
    onViewAlerts: () -> Unit = {}
) {
    EcoWearScaffold(
        modifier = Modifier.clickable { 
            if (state.isLinkedToPhone) onStartDemo() else onPairWithPhone() 
        }
    ) {
        item {
            Text(
                text = "CONECTADO",
                style = MaterialTheme.typography.labelSmall,
                color = EcoGuiaColors.Muted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )
        }

        item {
            StatusCard(
                icon = Icons.Default.Smartphone,
                text = "Eco-Guía móvil",
                isActive = state.isLinkedToPhone
            )
        }

        item {
            StatusCard(
                icon = Icons.Default.LocationOn,
                text = "GPS preciso",
                isActive = state.isGpsEnabled
            )
        }

        item {
            StatusCard(
                icon = Icons.Default.CameraAlt,
                text = "Cámara lista",
                isActive = state.isCameraReady
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toca para continuar",
                style = MaterialTheme.typography.bodySmall,
                color = EcoGuiaColors.Jade,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tarjeta individual de indicador de conectividad de servicios periféricos.
 *
 * @param icon Icono del servicio.
 * @param text Etiqueta descriptiva del servicio.
 * @param isActive Indicador de estado activo / vinculado.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun StatusCard(icon: ImageVector, text: String, isActive: Boolean) {
    val backgroundColor = if (isActive) {
        EcoGuiaColors.Surface.copy(alpha = 0.6f)
    } else {
        EcoGuiaColors.Surface.copy(alpha = 0.2f)
    }
    
    val iconGradient = if (isActive) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFE0E5A5), Color(0xFF98D8B1))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(EcoGuiaColors.Muted, EcoGuiaColors.Muted)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EcoGuiaColors.Background,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isActive) EcoGuiaColors.Text else EcoGuiaColors.Muted
        )
    }
}

/** Previsualización de la pantalla de emparejamiento. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PairingScreenPreview() {
    EcoGuiaWearTheme {
        PairingScreen(
            state = RadarUiState(isLinkedToPhone = true, isGpsEnabled = true, isCameraReady = false),
            onPairWithPhone = {},
            onStartDemo = {}
        )
    }
}
```

#### 2. `presentation/screens/RadarScreen.kt`
Vista central del radar geodésico con aguja de rumbo animada, distancia en metros y selector de sitios turísticos descubiertos.

```kotlin
/**
 * Pantalla principal del radar y navegación háptica en Wear OS.
 *
 * Muestra la aguja de compás en tiempo real, el título del objetivo, la distancia geodésica restante
 * y la botonera de navegación hacia la brújula ampliada, alertas y ajustes.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla central de visualización de radar geodésico.
 *
 * @param state Estado global reactivo del radar.
 * @param onToggleRadar Callback para pausar o reactivar el radar.
 * @param onApproachDemo Callback para simular aproximación al objetivo.
 * @param onOpenCompass Callback para abrir la vista de brújula.
 * @param onOpenAlert Callback para abrir la pantalla de alertas.
 * @param onOpenArrival Callback para abrir la pantalla de llegada.
 * @param onOpenSummary Callback para ver el resumen de ruta turística.
 * @param onOpenSettings Callback para acceder a ajustes.
 * @param onSelectNextAutoTarget Callback para ciclar al siguiente sitio descubierto.
 * @param onSelectPreviousAutoTarget Callback para ciclar al sitio anterior descubierto.
 * @param onRefresh Callback para recargar sitios desde Neon PostgreSQL.
 * @param onOpenStealth Callback para alternar al modo discreto.
 * @param onNavigateBack Callback para retornar a la pantalla previa.
 * @param requestFocus Indica si debe solicitar el foco para entrada rotatoria.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun RadarScreen(
    state: RadarUiState,
    onToggleRadar: () -> Unit,
    onApproachDemo: () -> Unit,
    onOpenCompass: () -> Unit,
    onOpenAlert: () -> Unit,
    onOpenArrival: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectNextAutoTarget: () -> Unit = {},
    onSelectPreviousAutoTarget: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenStealth: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    requestFocus: Boolean = true
) {
    val target = state.target

    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            ScreenHeader(
                title = "Sitio histórico",
                subtitle = state.lastAlert
            )
        }

        item {
            CompassArrow(
                bearingDegrees = target.bearingDegrees - state.currentHeading,
                modifier = Modifier.size(108.dp)
            )
        }
        item {
            Text(
                text = target.title,
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        // Subtítulo del objetivo con flechas para alternar entre los 3 sitios más cercanos
        if (target.subtitle.isNotBlank()) {
            item {
                if (target.isAutoTarget && state.nearbyAutoTargets.isNotEmpty()) {
                    val count = state.nearbyAutoTargets.size
                    val currentIndex = state.selectedAutoIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onSelectPreviousAutoTarget,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EcoGuiaColors.Surface,
                                contentColor = EcoGuiaColors.Jade
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text("<", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }

                        Text(
                            text = "${currentIndex + 1}/$count · ${target.title.take(12)}",
                            color = EcoGuiaColors.Jade,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onSelectNextAutoTarget,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EcoGuiaColors.Surface,
                                contentColor = EcoGuiaColors.Jade
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Text(">", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = target.subtitle,
                        color = EcoGuiaColors.Muted,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    )
                }
            }
        }
        item {
            Text(
                text = "${target.distanceMeters} m",
                color = EcoGuiaColors.Gold,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                label = { 
                Text(
                    text = "Ver Brújula / Dirección",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                ) 
            },
            onClick = {
                onOpenCompass()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoGuiaColors.Jade,
                contentColor = EcoGuiaColors.Background
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }

    item {
        Button(
            label = { 
                Text(
                    text = "Ver Alertas",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                ) 
            },
            onClick = onOpenAlert,
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
                contentColor = EcoGuiaColors.Text
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
    item {
        Button(
            label = { 
                Text(
                    text = "Ajustes",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                ) 
            },
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
                contentColor = EcoGuiaColors.Text
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
}

/** Previsualización de la pantalla de radar. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RadarScreenPreview() {
    EcoGuiaWearTheme {
        RadarScreen(
            state = RadarUiState(),
            onToggleRadar = {},
            onApproachDemo = {},
            onOpenCompass = {},
            onOpenAlert = {},
            onOpenArrival = {},
            onOpenSummary = {},
            onOpenSettings = {}
        )
    }
}
```

#### 3. `presentation/screens/CompassScreen.kt`
Brújula animada ampliada que utiliza el sensor de rotación en tiempo real para guiado durante caminatas al aire libre.

```kotlin
/**
 * Pantalla de brújula digital interactiva para Wear OS.
 *
 * Presenta la aguja de navegación orientada dinámicamente según la diferencia angular entre
 * el azimut del reloj y el rumbo hacia el sitio turístico objetivo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CompassArrow
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla de brújula y orientación visual.
 *
 * @param state Estado global reactivo del radar.
 * @param onNext Callback para avanzar de página.
 * @param onBack Callback para retroceder de página.
 * @param requestFocus Solicita foco rotatorio para el bisel físico si está en primer plano.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun CompassScreen(
    state: RadarUiState,
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    requestFocus: Boolean = true
) {
    val title = if (state.target.distanceMeters <= 1000) "SITIO CERCA" else "SIGUE LA FLECHA"
    
    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            ScreenHeader(
                title = title,
                subtitle = null
            )
        }
        
        item {
            CompassArrow(
                bearingDegrees = state.target.bearingDegrees - state.currentHeading,
                modifier = Modifier.size(120.dp)
            )
        }
        
        item {
            Text(
                text = "${state.target.distanceMeters} m restantes",
                style = MaterialTheme.typography.titleMedium,
                color = EcoGuiaColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

/** Previsualización en el diseñador de layouts Wear OS. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun CompassScreenPreview() {
    EcoGuiaWearTheme {
        CompassScreen(state = RadarUiState())
    }
}
```

#### 4. `presentation/screens/StealthRadarScreen.kt`
Modo discreto con pantalla oscurecida para ahorro extremo de batería y orientación táctil guiada exclusivamente por pulsos hápticos.

```kotlin
/**
 * Pantalla de control del Modo Discreto (Stealth Mode) en Wear OS.
 *
 * Permite alternar la navegación sin emitir luz en pantalla, confiando únicamente en las alertas
 * y patrones de pulsación háptica suave del smartwatch.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla de configuración del modo silencioso y discreto.
 *
 * @param state Estado global reactivo del radar.
 * @param onToggleStealth Callback para alternar el estado del modo discreto.
 * @param onNavigateNext Callback para desplazarse a la siguiente página (Radar).
 * @param onNavigateBack Callback para regresar al emparejamiento.
 * @param requestFocus Solicita foco rotatorio si la página está activa.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun StealthRadarScreen(
    state: RadarUiState,
    onToggleStealth: () -> Unit,
    onNavigateNext: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    requestFocus: Boolean = true
) {
    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "RADAR DISCRETO",
                    style = MaterialTheme.typography.titleSmall,
                    color = EcoGuiaColors.Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                CircularStatus(
                    progress = 0.75f,
                    text = if (state.isStealthMode) "ON" else "OFF",
                    progressColor = if (state.isStealthMode) EcoGuiaColors.Jade else EcoGuiaColors.Muted
                )
                
                Text(
                    text = if (state.isStealthMode) "Vibración baja \n solo proximidad" else "Radar visible \n vibración normal",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoGuiaColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Button(
                    label = { 
                        Text(
                            text = if (state.isStealthMode) "Desactivar" else "Activar",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        ) 
                    },
                    onClick = onToggleStealth,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isStealthMode) EcoGuiaColors.Surface else EcoGuiaColors.Jade,
                        contentColor = if (state.isStealthMode) EcoGuiaColors.Text else EcoGuiaColors.Background
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Button(
                    label = { 
                        Text(
                            text = "Ir al Radar",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        ) 
                    },
                    onClick = onNavigateNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoGuiaColors.Surface.copy(alpha = 0.5f),
                        contentColor = EcoGuiaColors.Text
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

/** Previsualización de la pantalla de modo discreto. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun StealthRadarScreenPreview() {
    EcoGuiaWearTheme {
        StealthRadarScreen(state = RadarUiState(), onToggleStealth = {})
    }
}
```

#### 5. `presentation/screens/AlertsScreen.kt`
Historial interactivo de notificaciones de proximidad recibidas en la sesión con soporte de purga y eliminación individual.

```kotlin
/**
 * Pantalla de historial y administración de alertas de proximidad en Wear OS.
 *
 * Muestra las notificaciones recientes de sitios turísticos y geodrops mediante un carrusel horizontal
 * con soporte para descarte individual o purga masiva de registros.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla carrusel de alertas de proximidad.
 *
 * @param state Estado global reactivo del radar.
 * @param onBack Callback para cerrar o retroceder de pantalla.
 * @param onDeleteAlert Callback para eliminar una alerta específica por su ID.
 * @param onClearAll Callback para vaciar todo el historial de alertas.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun AlertsScreen(
    state: RadarUiState,
    onBack: () -> Unit,
    onDeleteAlert: (String) -> Unit = {},
    onClearAll: () -> Unit = {}
) {
    if (state.alerts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EcoGuiaColors.Background)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        if (dragAmount.y < -50) { // Swipe up
                            onBack()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay alertas",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcoGuiaColors.Text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Se purgan tras 3h",
                    style = MaterialTheme.typography.labelSmall,
                    color = EcoGuiaColors.Muted,
                    fontSize = 11.sp
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { state.alerts.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoGuiaColors.Background)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    if (dragAmount.y < -50) { // Swipe up
                        onBack()
                    }
                }
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page >= state.alerts.size) return@HorizontalPager
            val alert = state.alerts[page]
            val icon = when (alert.type) {
                "GEODROP" -> Icons.Default.LocationOn
                "SITE" -> Icons.Default.Adjust
                else -> Icons.Default.Star
            }
            val color = if (alert.type == "SITE") EcoGuiaColors.Gold else EcoGuiaColors.Jade

            val ageMins = ((System.currentTimeMillis() - alert.timestamp) / 60000L).coerceAtLeast(0)
            val timeText = if (ageMins < 1) "Ahora" else "Hace ${ageMins}m"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "ALERTA ${page + 1}/${state.alerts.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EcoGuiaColors.Gold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = EcoGuiaColors.Muted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EcoGuiaColors.Background,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoGuiaColors.Text,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón para borrar alerta individual
                    Chip(
                        label = { Text("Borrar", fontSize = 10.sp) },
                        onClick = { onDeleteAlert(alert.id) },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        colors = ChipDefaults.secondaryChipColors(backgroundColor = EcoGuiaColors.Surface),
                        modifier = Modifier.weight(1f).height(32.dp)
                    )

                    // Botón para borrar todas las alertas
                    Chip(
                        label = { Text("Todas", fontSize = 10.sp) },
                        onClick = onClearAll,
                        icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Gold),
                        modifier = Modifier.weight(1f).height(32.dp)
                    )
                }
            }
        }

        HorizontalPageIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
            selectedColor = EcoGuiaColors.Jade,
            unselectedColor = EcoGuiaColors.Muted
        )
    }
}

/**
 * Componente auxiliar para listar alertas en formato de renglón compacto.
 *
 * @param icon Vector del icono descriptivo.
 * @param text Mensaje de la alerta.
 * @param iconBackground Color de fondo circular del icono.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun AlertItem(icon: ImageVector, text: String, iconBackground: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(EcoGuiaColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EcoGuiaColors.Background,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = EcoGuiaColors.Text
        )
    }
}

/** Previsualización en herramientas de diseño Compose. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun AlertsScreenPreview() {
    EcoGuiaWearTheme {
        AlertsScreen(state = RadarUiState(), onBack = {})
    }
}
```

#### 6. `presentation/screens/ArrivalScreen.kt`
Diálogo de confirmación al ingresar al radio de llegada (0 metros) con botón rápido para solicitar al teléfono la apertura de la cámara AR.

```kotlin
/**
 * Pantalla de confirmación de llegada a un hito o monumento histórico en Wear OS.
 *
 * Muestra el indicador visual de 0 metros, mensaje de éxito y opciones para abrir el visor AR
 * en el teléfono o continuar con la siguiente parada de la ruta.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla modal o de destino para eventos de arribo a destino.
 *
 * @param state Estado global reactivo del radar.
 * @param onOpenPhone Callback para solicitar la apertura de la cámara en el teléfono.
 * @param onContinue Callback para registrar la parada y avanzar de waypoint.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun ArrivalScreen(
    state: RadarUiState,
    onOpenPhone: () -> Unit,
    onContinue: () -> Unit
) {
    EcoWearScaffold {
        item {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EcoGuiaColors.Jade,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        item {
            Text(
                text = "¡LLEGASTE!",
                style = MaterialTheme.typography.titleMedium,
                color = EcoGuiaColors.Gold,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CircularStatus(
                progress = 1f,
                text = "0 m",
                progressColor = EcoGuiaColors.Jade,
                modifier = Modifier.height(60.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Usa el móvil para ver los detalles AR",
                style = MaterialTheme.typography.bodySmall,
                color = EcoGuiaColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = "Volver al radar",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Previsualización en el entorno de desarrollo Compose. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun ArrivalScreenPreview() {
    EcoGuiaWearTheme {
        ArrivalScreen(state = RadarUiState(), onOpenPhone = {}, onContinue = {})
    }
}
```

#### 7. `presentation/screens/RouteSummaryScreen.kt`
Vista resumen de la ruta turística sincronizada, mostrando el indicador circular con el avance de paradas completadas respecto al total.

```kotlin
/**
 * Pantalla de resumen del recorrido turístico activo en Wear OS.
 *
 * Presenta el progreso de paradas visitadas respecto al total (ej. 3/8) mediante un indicador
 * circular de estado y acceso para volver al radar en vivo.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.CircularStatus
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla de resumen estadístico de la ruta turística sincronizada.
 *
 * @param state Estado reactivo del radar.
 * @param onBackToRadar Callback para volver al radar principal.
 * @param requestFocus Solicita foco de corona rotatoria si está activa la página.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun RouteSummaryScreen(
    state: RadarUiState,
    onBackToRadar: () -> Unit,
    requestFocus: Boolean = true
) {
    val summary = state.routeSummary

    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            ScreenHeader(
                title = "RUTA ACTIVA",
                subtitle = "Siguiente parada a ${state.target.distanceMeters} m"
            )
        }
        item {
            CircularStatus(
                progress = summary.visitedStops.toFloat() / summary.totalStops.toFloat(),
                text = "${summary.visitedStops}/${summary.totalStops}",
                progressColor = EcoGuiaColors.Jade
            )
        }
        item {
            Text(
                text = summary.title,
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
        item {
            Button(
                label = { 
                    Text(
                        text = "Volver al radar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                onClick = onBackToRadar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Jade,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
    }
}

/** Previsualización en el editor de Compose. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RouteSummaryScreenPreview() {
    EcoGuiaWearTheme {
        RouteSummaryScreen(state = RadarUiState(), onBackToRadar = {})
    }
}
```

#### 8. `presentation/screens/RouteCompletedWearScreen.kt`
Pantalla modal de felicitación al culminar la totalidad de las paradas turísticas de una ruta guiada.

```kotlin
/**
 * Pantalla de felicitación y fin de ruta turística completada en Wear OS.
 *
 * Muestra el trofeo de logro turístico, el nombre del recorrido completado y un botón
 * para descartar el diálogo modal y volver al modo de auto-escaneo libre.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Pantalla de confirmación de ruta turística completada.
 *
 * @param state Estado global reactivo del radar.
 * @param onDismiss Callback para cerrar el diálogo modal de felicitación.
 * @param requestFocus Solicita foco rotatorio si está en primer plano.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun RouteCompletedWearScreen(
    state: RadarUiState,
    onDismiss: () -> Unit,
    requestFocus: Boolean = true
) {
    EcoWearScaffold(requestFocus = requestFocus) {
        item {
            Text(
                text = "🏆",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            Text(
                text = "¡RUTA COMPLETADA!",
                color = EcoGuiaColors.Gold,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
        item {
            Text(
                text = if (state.routeSummary.title.isNotBlank()) state.routeSummary.title else "Recorrido Turístico",
                color = EcoGuiaColors.Text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
            )
        }
        item {
            Text(
                text = "Se guardó el logro en tu colección del teléfono.",
                color = EcoGuiaColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
        }
        item {
            Button(
                label = {
                    Text(
                        text = "Aceptar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGuiaColors.Gold,
                    contentColor = EcoGuiaColors.Background
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
    }
}

/** Previsualización de la pantalla de ruta completada. */
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RouteCompletedWearScreenPreview() {
    EcoGuiaWearTheme {
        RouteCompletedWearScreen(state = RadarUiState(), onDismiss = {})
    }
}
```

#### 9. `presentation/screens/HapticSettingsScreen.kt`
Selector de preferencias de vibración para ajustar los niveles de intensidad (Suave, Media o Alta).

```kotlin
/**
 * Pantalla de configuración del motor de retroalimentación háptica en Wear OS.
 *
 * Permite alternar el estado general de las vibraciones táctiles y seleccionar entre tres niveles
 * de intensidad predefinidos ([HapticStrength.LOW], [HapticStrength.MEDIUM], [HapticStrength.HIGH]).
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.HapticStrength
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

/**
 * Pantalla de ajustes hápticos en el smartwatch.
 *
 * @param state Estado reactivo del radar.
 * @param onToggleHaptics Callback para activar o desactivar la respuesta sensorial.
 * @param onSelectStrength Callback para establecer la intensidad deseada.
 * @param onBackToRadar Callback para retornar a la vista de radar.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun HapticSettingsScreen(
    state: RadarUiState,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectStrength: (HapticStrength) -> Unit,
    onBackToRadar: () -> Unit
) {
    val settings = state.hapticSettings

    EcoWearScaffold {
        item {
            ScreenHeader(
                title = "Hapticos",
                subtitle = if (settings.enabled) "Vibracion activa" else "Vibracion apagada"
            )
        }
        item {
            Chip(
                label = { Text(if (settings.enabled) "Apagar vibracion" else "Activar vibracion") },
                onClick = { onToggleHaptics(!settings.enabled) },
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = if (settings.enabled) EcoGuiaColors.Surface else EcoGuiaColors.Jade
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Suave") },
                onClick = { onSelectStrength(HapticStrength.LOW) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Media") },
                onClick = { onSelectStrength(HapticStrength.MEDIUM) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CompactChip(
                label = { Text("Alta") },
                onClick = { onSelectStrength(HapticStrength.HIGH) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Volver al radar") },
                onClick = onBackToRadar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

#### 10. `presentation/screens/SiteNearbyScreen.kt`
Alerta emergente compacta que notifica al usuario el ingreso al perímetro de un nuevo hito cultural.

```kotlin
/**
 * Pantalla informativa de proximidad a un punto patrimonial o sitio turístico en Wear OS.
 *
 * Ofrece detalles breves sobre el patrimonio cultural detectado e instruye al usuario a consultar
 * la app móvil para visualizar el contenido multimedia e IA generativa.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

/**
 * Pantalla de aviso de sitio patrimonial próximo.
 *
 * @param state Estado global reactivo del radar.
 * @param onBackToRadar Callback para volver al radar.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun SiteNearbyScreen(
    state: RadarUiState,
    onBackToRadar: () -> Unit
) {
    EcoWearScaffold(
        modifier = Modifier.clickable { onBackToRadar() }
    ) {
        item {
            ScreenHeader(
                title = "Sitio cercano",
                subtitle = state.target.subtitle
            )
        }
        item {
            Text(
                text = "Hay contenido cultural cercano. El reloj guia; el celular muestra detalles, IA y camara AR.",
                color = EcoGuiaColors.Text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = "${state.target.distanceMeters} m restantes",
                color = EcoGuiaColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Volver al radar") },
                onClick = onBackToRadar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

#### 11. `presentation/screens/ProximityAlertScreen.kt`
Notificación interactiva con accesos directos para inspeccionar o iniciar el seguimiento de un punto de interés cercano.

```kotlin
/**
 * Pantalla informativa de proximidad a un sitio turístico detectado en Wear OS.
 *
 * Ofrece accesos rápidos para abrir el visor AR en el teléfono móvil o marcar la llegada de forma manual.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import mx.utng.ecoguiawear.domain.model.RadarUiState
import mx.utng.ecoguiawear.presentation.components.EcoWearScaffold
import mx.utng.ecoguiawear.presentation.components.ScreenHeader
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaColors

/**
 * Pantalla de aviso de proximidad a un punto de interés.
 *
 * @param state Estado global reactivo del radar.
 * @param onOpenPhone Callback para abrir la cámara de Geo-Drops en el smartphone.
 * @param onArrived Callback para marcar el punto como alcanzado.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
@Composable
fun ProximityAlertScreen(
    state: RadarUiState,
    onOpenPhone: () -> Unit,
    onArrived: () -> Unit
) {
    EcoWearScaffold {
        item {
            ScreenHeader(
                title = "Sitio histórico",
                subtitle = "${state.target.distanceMeters} m restantes"
            )
        }
        item {
            Text(
                text = "Dentro del rango del sitio. Abre la cámara Geo-Drop en tu móvil.",
                color = EcoGuiaColors.Text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Ver Geo-Drops del sitio") },
                onClick = onOpenPhone,
                colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Jade),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Ya llegue") },
                onClick = onArrived,
                colors = ChipDefaults.primaryChipColors(backgroundColor = EcoGuiaColors.Gold),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

### Paso 4.6: Actividad Principal (`presentation/MainActivity.kt`)

`MainActivity` coordina el ciclo de vida del reloj, solicita permisos de GPS en tiempo de ejecución, inicializa los listeners de sensores y Play Services Wearable, e inicializa el grafo de navegación `EcoGuiaWearNavGraph`.

```kotlin
/**
 * Actividad principal y punto de entrada para la aplicación en Wear OS.
 *
 * Configura la pantalla de bienvenida (Splash Screen), solicita permisos de ubicación en tiempo de ejecución,
 * inicializa los sensores de orientación y GPS, y establece el grafo de navegación Wear Compose.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
package mx.utng.ecoguiawear.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import mx.utng.ecoguiawear.data.haptics.HapticController
import mx.utng.ecoguiawear.data.repository.RadarRepositoryImpl
import mx.utng.ecoguiawear.data.wear.LocationHelper
import mx.utng.ecoguiawear.data.wear.PhoneMessageClient
import mx.utng.ecoguiawear.data.wear.SensorHelper
import mx.utng.ecoguiawear.data.wear.WearMessageListener
import mx.utng.ecoguiawear.presentation.navigation.EcoGuiaWearNavGraph
import mx.utng.ecoguiawear.presentation.theme.EcoGuiaWearTheme

/**
 * Actividad única de Wear OS que implementa la escucha de mensajes Wearable en primer plano.
 *
 * @author Zahir Andrés Rodríguez Mora
 * @author Cesar Enrique Garay García
 * @since 2026-08-05
 */
class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var messageListener: WearMessageListener
    private lateinit var locationHelper: LocationHelper
    private lateinit var sensorHelper: SensorHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val repository = RadarRepositoryImpl(applicationContext)
        messageListener = WearMessageListener(repository)
        
        locationHelper = LocationHelper(applicationContext) { location ->
            repository.updateCurrentLocation(location.latitude, location.longitude)
        }

        sensorHelper = SensorHelper(applicationContext) { heading ->
            repository.updateHeading(heading)
        }
        sensorHelper.start()

        Wearable.getMessageClient(this).addListener(this)

        val factory = RadarViewModelFactory(
            repository = repository,
            hapticController = HapticController(applicationContext),
            phoneMessageClient = PhoneMessageClient(applicationContext)
        )

        setContent {
            val radarViewModel: RadarViewModel = viewModel(factory = factory)
            
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    locationHelper.startUpdates()
                }
            }

            LaunchedEffect(Unit) {
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }

            EcoGuiaWearTheme {
                EcoGuiaWearNavGraph(viewModel = radarViewModel)
            }
        }
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
