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
