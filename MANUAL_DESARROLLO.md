# Manual de Desarrollo - Proyecto Eco-Guía

**Versión del Documento:** 1.0.0  
**Fecha de Actualización:** Agosto 2026  
**Ecosistema:** Android Multimodal (Mobile, Wear OS, Android TV)  

---

## 1. Introducción y Visión General del Proyecto

**Eco-Guía** es una plataforma turística y cultural interactiva orientada a la exploración de sitios históricos (con enfoque inicial en Dolores Hidalgo, Cuna de la Independencia Nacional). 

La plataforma integra una experiencia **multidispositivo** conectada:
*   **Smartphone (Mobile):** Guía interactiva principal, mapas en tiempo real, alertas de proximidad en segundo plano, portal 360°, captura de *Geo-Drops* con Realidad Aumentada (AR) y gestión de perfiles.
*   **Smartwatch (Wear OS):** Brújula direccional háptica, radar de proximidad en la muñeca y navegación asistida por vibración en tiempo real.
*   **Smart TV / Kiosco (Android TV):** Galería interactiva comunitaria de alta definición, visualización de rutas turísticas y sincronización remota con el smartphone mediante códigos QR y mensajes MQTT en tiempo real.
*   **Módulo Compartido (Shared):** Núcleo de lógica de negocio, clientes de bases de datos serverless (PostgreSQL/Neon), mensajería MQTT (HiveMQ), inteligencia artificial (Groq) y modelos de dominio compartidos.

---

## 2. Requisitos del Entorno de Desarrollo

Para clonar, compilar y ejecutar el proyecto satisfactoriamente, el equipo de desarrollo debe contar con las siguientes herramientas instaladas y configuradas:

### 2.1. Software Obligatorio
*   **Sistema Operativo:** Windows 10/11 (64-bit), macOS (Monterey o superior) o Linux (Ubuntu 22.04 LTS o equivalente).
*   **IDE:** [Android Studio](https://developer.android.com/studio) (Versión recomendada: **Ladybug | 2024.2.1** o superior).
*   **Java Development Kit (JDK):** **JDK 17** (Gestionado automáticamente por Gradle Foojay Toolchains o instalado vía OpenJDK / Azul Zulu 17).
*   **Android SDK Platforms:** 
    *   `compileSdk`: **API 37**
    *   `targetSdk`: **API 37**
    *   `minSdk`: **API 24** (Android 7.0 Nougat para Mobile y TV) / **API 30** (Wear OS 3.0 para Smartwatches).
*   **Git:** Versión 2.40+ para control de versiones.

### 2.2. Hardware Recomendado
*   **Procesador:** CPU de 64 bits con al menos 4 núcleos (soporte de virtualización habilitado para emuladores).
*   **Memoria RAM:** 16 GB o superior (mínimo 8 GB con memoria swap activa).
*   **Espacio en Disco:** Al menos 15 GB libres para SDKs, emuladores y caché de compilación de Gradle.
*   **Dispositivos de Prueba:**
    *   Smartphone Android físico con cámara y GPS (o Emulador con Google Play Services).
    *   (Opcional) Reloj inteligente Wear OS físico o Emulador Wear OS (Square o Round API 30+).
    *   (Opcional) Dispositivo Android TV físico o Emulador Android TV (1080p).

---

## 3. Arquitectura del Proyecto (Multi-Módulo)

El proyecto está estructurado como un proyecto multi-módulo de Gradle bajo el principio de separación de responsabilidades:

```text
Eco-Guia/
├── gradle/
│   └── libs.versions.toml      # Catálogo centralizado de versiones y dependencias
├── shared/                     # Módulo de lógica y datos compartidos (Android Library)
│   ├── src/main/java/mx/utng/ecoguia/shared/
│   │   ├── config/             # Constantes y umbrales globales (EcoGuiaConfig.kt)
│   │   ├── data/
│   │   │   ├── remote/         # Clientes Neon (PostgreSQL), HiveMQ (MQTT), Groq (IA)
│   │   │   └── repository/     # Implementación del repositorio EcoGuiaRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/          # Modelos de datos (Sitios, Categorías, Usuarios, GeoDrops)
│   │   │   └── repository/     # Interfaces de repositorios
│   │   └── ecoguia_postgres_schema.sql # Esquema DDL de la base de datos PostgreSQL
│   └── build.gradle.kts
├── mobile/                     # Módulo de la aplicación para teléfonos móviles (Android App)
│   ├── google-services.json    # Configuración de Firebase (Ignorado en Git por seguridad)
│   ├── src/main/
│   │   ├── AndroidManifest.xml # Permisos de ubicación, cámara, notificaciones y servicios
│   │   └── java/mx/utng/ecoguiawear/
│   │       ├── data/           # ProximityService, NotificationHelper, EmailService (Brevo)
│   │       └── ui/
│   │           ├── navigation/ # AppNavHost.kt (Enrutamiento Jetpack Compose)
│   │           ├── screens/    # Pantallas (Exploración, AR GeoDrop, Perfil, Permisos, etc.)
│   │           ├── theme/      # Paleta de colores, tipografía e iconografía
│   │           └── viewmodel/  # ViewModels de autenticación, ubicación, GeoDrops, etc.
│   └── build.gradle.kts
├── wear/                       # Módulo de la aplicación para relojes Wear OS (Wear App)
│   ├── src/main/java/mx/utng/ecoguiawear/
│   │   ├── data/               # Receptor de mensajes del smartphone (Data Layer API)
│   │   ├── presentation/       # Pantallas circulares Compose (Brújula, Radar, Notificaciones)
│   │   └── theme/              # Estilos optimizados para pantallas OLED y batería baja
│   └── build.gradle.kts
└── tv/                         # Módulo de la aplicación para Smart TV (Android TV App)
    ├── google-services.json    # Configuración de Firebase para TV
    ├── src/main/java/mx/utng/ecoguiawear/tv/
    │   ├── data/               # Sincronización MQTT para control remoto y emparejamiento
    │   └── ui/screens/         # Galería comunitaria interactiva y visor de pantallas grandes
    └── build.gradle.kts
```

---

## 4. Stack Tecnológico y Dependencias Principales

Todas las dependencias están gestionadas en el archivo `gradle/libs.versions.toml` para evitar discrepancias entre módulos:

| Categoría | Tecnología / Librería | Propósito |
| :--- | :--- | :--- |
| **Lenguaje Base** | Kotlin 2.4.x + Coroutines + Flow | Programación reactiva, asíncrona y estructurada. |
| **Interfaz de Usuario** | Jetpack Compose (BOM 2024.09.00) + Material 3 | Construcción de interfaces declarativas en Mobile, Wear y TV. |
| **Wearable UI** | Wear Compose + Horologist (0.6.22) | Componentes adaptados a pantallas circulares y baja latencia. |
| **TV UI** | Android TV Foundation & Material | Soporte de navegación mediante D-Pad / Control Remoto. |
| **Base de Datos Principal** | Neon Serverless PostgreSQL | Almacenamiento relacional de usuarios, sitios, categorías y reseñas. |
| **Cliente de Red & SQL** | Ktor Client 2.3.x (OkHttp engine) + Kotlinx Serialization | Comunicación HTTP y ejecución de consultas SQL serverless. |
| **Almacenamiento Multimedia** | Firebase Storage SDK (BOM 33.7.0) | Almacenamiento y descarga de fotos de Geo-Drops y galerías. |
| **Cámara y Realidad Aumentada**| CameraX (1.4.0) | Captura de imagen, renderizado de visor y retículas AR. |
| **Mapas y Geolocalización** | Google Maps Compose + Play Services Location | Visualización de mapas vectoriales y geofencing en tiempo real. |
| **Mensajería en Tiempo Real** | HiveMQ MQTT Client | Comunicación bidireccional de baja latencia entre el Celular y la TV. |
| **Sincronización Reloj** | Google Play Services Wearable (Data Layer API) | Envío de coordenadas y alertas táctiles entre Smartphone y Reloj. |
| **Correos Transaccionales** | Brevo REST API (v3) | Envío de códigos OTP de 6 dígitos para validación de registro. |
| **Inteligencia Artificial** | Groq Cloud API (Llama 3) | Generación inteligente de datos históricos y descripciones. |

---

## 5. Configuración de Credenciales y Servicios Externos

Para que un nuevo desarrollador configure el proyecto localmente, debe asegurarse de contar con las siguientes configuraciones:

### 5.1. Firebase (`google-services.json`)
*   **Ubicación requerida:** Colocar una copia del archivo `google-services.json` en las carpetas `mobile/` y `tv/`.
*   **Servicio utilizado:** Firebase Cloud Storage (bucket de subida de imágenes para la carpeta `geo_drops/`).
*   *Nota de seguridad:* Este archivo está en el `.gitignore` por seguridad y no debe subirse a repositorios públicos.

### 5.2. Google Maps Platform API Key
*   Configurada en los archivos `AndroidManifest.xml` de los módulos `mobile` y `tv` dentro de la etiqueta `<meta-data android:name="com.google.android.geo.API_KEY" .../>`.
*   **APIs requeridas en Google Cloud Console:**
    *   Maps SDK for Android
    *   Places API (New)
    *   Geocoding API

### 5.3. Base de Datos Neon (PostgreSQL)
*   La conexión se realiza mediante el cliente HTTP de Ktor en `NeonClient.kt`.
*   El esquema relacional completo (tablas `users`, `historical_sites`, `categories`, `geo_drops`, `site_reviews`, `tv_devices`, etc.) se encuentra documentado en `shared/src/main/java/mx/utng/ecoguia/shared/ecoguia_postgres_schema.sql`.

### 5.4. Servicio de Correos (Brevo API)
*   Ubicado en `EmailService.kt` (`mobile`).
*   Utiliza la API Key de Brevo para disparar correos transaccionales con plantilla HTML y código de verificación OTP de 6 dígitos.

### 5.5. Broker MQTT (HiveMQ)
*   Gestionado en `HiveMQManager.kt` (`shared`).
*   Permite emparejar dispositivos TV mediante un código de 6 caracteres y transmitir el contenido interactivo a la pantalla grande.

---

## 6. Flujo de Trabajo y Ciclo de Vida de las Funcionalidades

### 6.1. Flujo de Autenticación y Verificación OTP
1. El usuario ingresa Nombre, Correo y Contraseña en `SignUpScreen.kt`.
2. `AuthViewModel` invoca `EmailService.sendOtpEmail()`, generando un código de 6 dígitos y enviándolo vía Brevo.
3. La aplicación navega a `OtpVerificationScreen.kt`.
4. Al validar el código, se ejecuta la inserción del usuario en la base de datos de Neon mediante `EcoGuiaRepositoryImpl.registerUser()` con la contraseña encriptada.

### 6.2. Flujo de Geo-Drops y Realidad Aumentada
1. El usuario abre el modo Cámara/Radar desde la barra de navegación o detalle de un sitio.
2. `CameraGeoDropScreen.kt` inicializa el visor de CameraX con el overlay de la retícula AR.
3. Al presionar "Capturar", la imagen se convierte a formato JPEG.
4. `FirebaseStorageRepository.uploadImage()` sube el archivo a Firebase Storage y obtiene la URL pública de descarga.
5. Se registra el Geo-Drop con sus coordenadas GPS en la tabla `geo_drops` de Neon DB.

### 6.3. Flujo de Geofencing y Alertas en Segundo Plano
1. El usuario enciende el switch de **Alertas en segundo plano** en `PermissionsScreen.kt`.
2. El estado se persiste en `SharedPreferences` y `LocationViewModel` inicia `ProximityService` como un **ForegroundService** persistente.
3. El servicio consulta periódicamente la ubicación GPS del usuario y compara la distancia con los sitios históricos cercanos (`checkProximity`).
4. Si la distancia es menor o igual al `detection_radius_m` del sitio y no se ha notificado previamente en la sesión, `ProximityNotificationHelper` dispara una notificación del sistema y una alerta háptica.

### 6.4. Flujo de Sincronización Móvil <-> Smartwatch
1. En `ExplorationScreen.kt`, al seleccionar un sitio histórico, el usuario puede pulsar "Navegar con mi Smartwatch".
2. `WearMessageClient.kt` envía un payload binario con `siteId`, `name`, `latitude` y `longitude` a través de la API `Wearable.getMessageClient()`.
3. El módulo `wear` recibe el mensaje, calcula el azimut y muestra la aguja magnética en `PresentationScreen.kt`.

### 6.5. Flujo de Vinculación con Android TV
1. La aplicación Android TV genera un código de vinculación único de 6 caracteres y se suscribe a su tópico MQTT en HiveMQ.
2. El usuario escanea el código QR desde el móvil o ingresa el código manual.
3. El móvil envía una señal MQTT con el contenido a proyectar (galería de fotos, sitio 360°, etc.), actualizando la pantalla de la TV de forma instantánea.

---

## 7. Instrucciones de Compilación y Ejecución

### 7.1. Compilación por Línea de Comandos (Gradle Wrapper)

*   **Compilar todo el proyecto (Debug):**
    ```bash
    ./gradlew assembleDebug
    ```
*   **Compilar únicamente el módulo Móvil:**
    ```bash
    ./gradlew :mobile:assembleDebug
    ```
*   **Compilar únicamente el módulo Wear OS:**
    ```bash
    ./gradlew :wear:assembleDebug
    ```
*   **Compilar únicamente el módulo Android TV:**
    ```bash
    ./gradlew :tv:assembleDebug
    ```
*   **Limpiar caché y artefactos generados:**
    ```bash
    ./gradlew clean
    ```

### 7.2. Ejecución desde Android Studio
1. Abre el proyecto en Android Studio seleccionando la carpeta raíz `Eco-Guia`.
2. Espera a que la sincronización de Gradle finalice exitosamente.
3. En la barra superior de herramientas, selecciona la configuración que deseas ejecutar (`mobile`, `wear` o `tv`).
4. Selecciona el dispositivo físico o emulador correspondiente en el menú desplegable.
5. Presiona el botón verde de **Run (Play)** ▶️ o utiliza el atajo `Shift + F10`.

---

## 8. Buenas Prácticas y Solución de Problemas (Troubleshooting)

### 8.1. Buenas Prácticas de Desarrollo
*   **Seguridad de Credenciales:** Nunca hagas commit forzado de archivos `google-services.json`, claves privadas `.jks`, o archivos `.env`.
*   **Gestión de Permisos en Android 13+ / 14+:** Siempre verifica los permisos en tiempo de ejecución antes de iniciar servicios en primer plano (`FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`).
*   **Eficiencia Energética:** Asegúrate de detener las actualizaciones de ubicación GPS en los métodos `onCleared()` de los ViewModels y `onDestroy()` de los servicios.

### 8.2. Problemas Frecuentes y Soluciones

*   **Error:** *`File google-services.json is missing` al compilar.*
    *   *Solución:* Descarga el archivo desde Firebase Console y colócalo en `mobile/google-services.json` y `tv/google-services.json`.
*   **Error:** *Incompatibilidad de versión de Java / Gradle.*
    *   *Solución:* Ve a **Settings > Build, Execution, Deployment > Build Tools > Gradle** y confirma que la opción **Gradle JDK** esté apuntando a **JDK 17**.
*   **Comportamiento:** *En dispositivos Xiaomi / MIUI / HyperOS los permisos no aparecen en "App Info".*
    *   *Solución:* En dispositivos con capas restrictivas, activa la opción *"Allow restricted settings"* al fondo de la pantalla de ajustes de la app, o gestiona los permisos desde **Ajustes del Sistema > Aplicaciones > Permisos**.
