import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val brevoKey: String = localProperties.getProperty("BREVO_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {

    namespace = "mx.utng.ecoguiawear"
    compileSdk = 37

    defaultConfig {
        applicationId = "mx.utng.ecoguiawear"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BREVO_API_KEY", "\"$brevoKey\"")
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
        buildConfig = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Módulo compartido de la arquitectura: base de datos Room, repositorios y modelos de datos comunes
    implementation(project(":shared"))

    // Compose Foundation, UI y Material Design 3
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.compose.material.icons.extended)

    // APIs de Google Play Services: Data Layer para sincronización con Smartwatch Wear OS
    implementation(libs.play.services.wearable)

    // Google Maps & Fused Location: mapas interactivos, rutas y geolocalización de alta precisión
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)
    implementation(libs.google.places)

    // Corrutinas Kotlin y extensión para integración con tareas asíncronas de Play Services
    implementation(libs.kotlinx.coroutines.play.services)

    // Componentes de arquitectura Android: Lifecycle, ViewModel Compose y Room offline
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.navigation.compose)

    // WorkManager para programación de tareas y sincronizaciones en segundo plano
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // CameraX: captura de fotografías para la recolección de Geo-Drops y portales AR
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Integración de Inteligencia Artificial (Groq/Ktor Client HTTP)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    
    // Almacenamiento multimedia en Firebase Storage y carga asíncrona de imágenes con Coil
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.storage)
    implementation(libs.coil.compose)
    
    // Escáner de código QR nativo de Google Play Services
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // Herramientas de pruebas y depuración UI
    testImplementation(libs.junit)
    debugImplementation(libs.ui.tooling)
}

