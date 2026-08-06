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