import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val groqKey: String = localProperties.getProperty("GROQ_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "mx.utng.ecoguia.shared"
    compileSdk = 37

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqKey\"")
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
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Persistencia local offline con Room (SQLite) y soporte de corrutinas Flow/suspend
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Despachadores de corrutinas para subprocesos en segundo plano
    implementation(libs.kotlinx.coroutines.android)

    // Ktor Client HTTP & Kotlinx Serialization: comunicación remota REST con base de datos PostgreSQL Neon
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    // Cliente MQTT Paho: transmisión en tiempo real de telemetría y geodrops hacia la app Smart TV
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    // Pruebas unitarias para repositorios y entidades compartidas
    testImplementation(libs.junit)
}
