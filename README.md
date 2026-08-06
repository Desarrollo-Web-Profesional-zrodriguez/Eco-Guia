<div align="center">
  <h1>🌿 Eco-Guía</h1>
  <p><strong>Plataforma Multiplataforma para Turismo Cultural, Geolocalización e Inteligencia Artificial</strong></p>
  <p><i>Dolores Hidalgo Cuna de la Independencia Nacional — Guanajuato, México</i></p>

  <br>

  <div>
    <img src="https://img.shields.io/badge/Kotlin-2.1.0-blue?style=for-the-badge&logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/Android-Phone_/_Wear_OS_/_TV-green?style=for-the-badge&logo=android" alt="Android">
    <img src="https://img.shields.io/badge/Database-PostgreSQL_/_PostGIS-orange?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
    <img src="https://img.shields.io/badge/AI-Groq_Llama_3.3_70B-purple?style=for-the-badge&logo=openai" alt="Groq AI">
    <img src="https://img.shields.io/badge/Messaging-MQTT_RealTime-red?style=for-the-badge&logo=eclipseche" alt="MQTT">
    <img src="https://img.shields.io/badge/Android_Studio-Meerkat_2025.1.1-3DDC84?style=for-the-badge&logo=androidstudio" alt="Android Studio">
  </div>
</div>

<br>
<hr>

## 👥 Información del Proyecto

<table>
  <tr>
    <td><b>Proyecto:</b></td>
    <td>Eco-Guía Multiplataforma</td>
  </tr>
  <tr>
    <td><b>Desarrolladores:</b></td>
    <td>Zahir Andrés Rodríguez Mora &amp; Cesar Enrique Garay García</td>
  </tr>
  <tr>
    <td><b>Grupo:</b></td>
    <td>GIDS6092</td>
  </tr>
  <tr>
    <td><b>Institución:</b></td>
    <td>Universidad Tecnológica del Norte de Guanajuato (UTNG)</td>
  </tr>
  <tr>
    <td><b>Repositorio:</b></td>
    <td>https://github.com/Desarrollo-Web-Profesional-zrodriguez/Eco-Guia</td>
  </tr>
</table>

<br>

---

## 🎯 Objetivo de la Aplicación

Desarrollar una solución tecnológica integral y distribuida (**Android Móvil**, **Wear OS** y **Smart TV 360°**) que fomente la exploración del patrimonio histórico de Dolores Hidalgo, Guanajuato.

La plataforma permite:
- Descubrir puntos de interés mediante un **radar GPS con brújula háptica** en el reloj inteligente.
- Capturar **cápsulas geolocalizadas (Geo-Drops)** con fotografías y texto anclados a coordenadas reales.
- Interactuar con un **bot conversacional histórico** impulsado por **Miguel Hidalgo IA (Groq Llama 3.3 70B)**.
- Interconectar pantallas en tiempo real mediante **MQTT (HiveMQ)** y persistir datos en **PostgreSQL/PostGIS (Neon DB)**.
- Exhibir el patrimonio en pantalla grande a través del módulo **Smart TV** con rotación automática 360°.

<br>

---

## 💌 Carta del Beneficiario

> **[INSTRUCCIÓN]** — Colocar aquí el texto completo de la carta emitida por el beneficiario del proyecto (institución, museo, dependencia municipal o cultural de Dolores Hidalgo) que avala el desarrollo de Eco-Guía. La carta debe incluir: nombre del beneficiario, cargo, institución, fecha de emisión y firma digitalizada.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     CARTA DEL BENEFICIARIO                              │
│                                                                         │
│  Nombre:    _________________________________________________            │
│  Cargo:     _________________________________________________            │
│  Institución: _______________________________________________            │
│  Fecha:     _________________________________________________            │
│                                                                         │
│  [Cuerpo de la carta]                                                   │
│                                                                         │
│  Firma: _____________________                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

<br>

---

## 🎬 Video del Beneficiario

> **[INSTRUCCIÓN]** — Insertar aquí el enlace al video donde el beneficiario presenta o avala el proyecto Eco-Guía. Puede ser un enlace de YouTube, Google Drive o cualquier plataforma de video pública.

```
📹 Video del beneficiario:
   URL: [PENDIENTE — insertar enlace al video]
   Duración aproximada: ____ minutos
   Descripción: Entrevista/testimonio del beneficiario validando el impacto de Eco-Guía
                en la difusión del patrimonio histórico de Dolores Hidalgo.
```

<br>

---

## 🏗️ Arquitectura Global de la Aplicación

El proyecto sigue una arquitectura **modular multi-target** basada en **Clean Architecture** con separación de responsabilidades por capa. El módulo `shared` es el núcleo transversal que provee modelos, repositorio y clientes remotos a todos los demás módulos.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        EcoGuiaWear — Proyecto Raíz                          │
│                         (settings.gradle.kts)                               │
└───────────┬─────────────┬───────────────┬───────────────────────────────────┘
            │             │               │
   ┌────────▼───────┐  ┌──▼──────────┐  ┌▼──────────────┐
   │   :mobile      │  │   :wear     │  │     :tv        │
   │ Android Phone  │  │  Wear OS    │  │  Android TV    │
   │                │  │             │  │                │
   │ • Jetpack      │  │ • Horologist│  │ • Compose TV   │
   │   Compose      │  │ • Wear      │  │ • Google Maps  │
   │ • Google Maps  │  │   Compose   │  │   3D/Tilt      │
   │ • CameraX      │  │ • Hardware  │  │ • Key Trapping │
   │ • MQTT         │  │   Sensors   │  │ • MQTT         │
   │ • Firebase     │  │ • Data      │  │ • Local Server │
   │   Storage      │  │   Layer API │  │                │
   │ • Brevo Email  │  │             │  │                │
   └────────┬───────┘  └──┬──────────┘  └┬──────────────┘
            │              │              │
            └──────────────┼──────────────┘
                           │
               ┌───────────▼──────────────┐
               │         :shared           │
               │   Módulo Compartido       │
               │                          │
               │ • Room Database (local)   │
               │ • NeonClient (PostgreSQL) │
               │ • GroqClient (IA)         │
               │ • HiveMQManager (MQTT)    │
               │ • EcoGuiaRepository       │
               │ • Domain Models           │
               └───────────┬──────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼──────┐  ┌────────▼──────┐  ┌───────▼──────┐
│  Neon DB     │  │  Groq Cloud   │  │  HiveMQ      │
│  PostgreSQL  │  │  Llama 3.3    │  │  MQTT Broker │
│  + PostGIS   │  │  70B          │  │  (Real-time) │
└──────────────┘  └───────────────┘  └──────────────┘
```

### Capas por módulo

| Módulo | Capa Data | Capa Domain | Capa Presentation/UI |
|--------|-----------|-------------|----------------------|
| `shared` | `EcoGuiaDatabase`, `NeonClient`, `HiveMQManager`, `GroqClient` | `EcoGuiaRepository`, Models | — |
| `mobile` | `ProximityService`, `EmailService`, `WearMessageClient`, `FirebaseStorageRepository` | Vía `shared` | `AppNavHost`, 44 Screens, 10 ViewModels |
| `wear` | `HapticController`, `RadarRepositoryImpl`, `SensorHelper`, `LocationHelper` | `RadarRepository`, `RadarModels` | `EcoGuiaWearNavGraph`, 11 Screens, `RadarViewModel` |
| `tv` | `TvLocalServer` | Vía `shared` | `SmartTVNavHost`, 4 Screens + 5 Components |

<br>

---

## 📦 Versiones por Módulo

| Módulo | `compileSdk` | `minSdk` | `targetSdk` | `versionName` | Kotlin | Android Studio |
|--------|-------------|----------|-------------|---------------|--------|----------------|
| `mobile` | 37 | 24 | 37 | 1.0.0 | 2.1.0 | Meerkat 2025.1.1+ |
| `wear` | 34 | 30 | 34 | 1.0.0 | 2.1.0 | Meerkat 2025.1.1+ |
| `tv` | 35 | 21 | 35 | 1.0.0 | 2.1.0 | Meerkat 2025.1.1+ |
| `shared` | 35 | 21 | 35 | — (librería) | 2.1.0 | Meerkat 2025.1.1+ |

> Las arquitecturas se desarrollaron íntegramente en **Android Studio Meerkat 2025.1.1** con **Kotlin 2.1.0**, **Jetpack Compose BOM 2025.x** y **Gradle 8.x** con Kotlin DSL (`.kts`).

<br>

---

## 💻 Preparación del Entorno de Desarrollo

Sigue esta guía **paso a paso** para configurar el entorno desde cero antes de comenzar a desarrollar.

### Paso 1 — Instalar Android Studio

1. Descarga **Android Studio Meerkat 2025.1.1** (o la versión más reciente) desde:  
   👉 https://developer.android.com/studio
2. Instala con las opciones predeterminadas (incluye el SDK de Android base).
3. Al iniciar por primera vez, completa el **Android Studio Setup Wizard**.

### Paso 2 — Configurar el SDK Manager

Dentro de Android Studio, ve a `Tools → SDK Manager` y asegúrate de tener instalados:

| Componente | Versión requerida |
|---|---|
| Android SDK Platform 35 (API 35) | ✅ Instalar |
| Android SDK Platform 34 (API 34 — Wear OS) | ✅ Instalar |
| Android SDK Platform 37 (API 37 — Mobile) | ✅ Instalar |
| Android SDK Build-Tools 35.x | ✅ Instalar |
| Android Emulator | ✅ Instalar |
| Google Play Services | ✅ Instalar |
| Intel x86 Emulator Accelerator (HAXM) | ✅ Recomendado |

En la pestaña **SDK Tools**, instala también:
- Google USB Driver
- Android Emulator Hypervisor Driver

### Paso 3 — Crear los Emuladores (AVD Manager)

Ve a `Tools → Device Manager → Create Device`:

| Dispositivo | Perfil recomendado | API | Uso |
|---|---|---|---|
| **Android Phone** | Pixel 7 Pro | API 34 / 35 | Módulo `mobile` |
| **Wear OS** | Wear OS Large Round | API 34 | Módulo `wear` |
| **Android TV** | Television 1080p | API 35 | Módulo `tv` |

> **Nota sobre Wear OS:** Para probar la comunicación entre `wear` y `mobile`, empareja el emulador Wear OS con el emulador de teléfono usando `adb` y el asistente de vinculación de Android Studio.

### Paso 4 — Clonar el Repositorio

```bash
git clone https://github.com/Desarrollo-Web-Profesional-zrodriguez/Eco-Guia.git
cd EcoGuiaWear
```

### Paso 5 — Configurar Variables de Entorno (`local.properties`)

En la raíz del proyecto, edita o crea el archivo `local.properties` y agrega:

```properties
# SDK path (se genera automáticamente con Android Studio)
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

# API Keys del proyecto
GROQ_API_KEY=tu_api_key_de_groq
BREVO_API_KEY=tu_api_key_de_brevo
NEON_DATABASE_URL=postgresql://usuario:password@host.neon.tech/neondb?sslmode=require
GOOGLE_MAPS_API_KEY=tu_api_key_de_google_maps
```

> ⚠️ El archivo `.env` de la raíz también contiene variables de entorno. Solicita las claves al líder del proyecto. **Nunca subas `local.properties` ni `.env` al repositorio.**

### Paso 6 — Configurar Firebase

1. Descarga el archivo `google-services.json` desde **Firebase Console → Proyecto → Configuración**.
2. Colócalo en:
   - `mobile/google-services.json` (módulo `mobile`)
3. Verifica que el `applicationId` en `mobile/build.gradle.kts` coincida con el registrado en Firebase.

### Paso 7 — Abrir el Proyecto en Android Studio

1. Abre Android Studio.
2. `File → Open` → Selecciona la carpeta raíz `EcoGuiaWear/`.
3. Espera a que Gradle sincronice el proyecto (puede tomar varios minutos la primera vez).
4. Si Gradle presenta errores de sincronización, ve a `File → Invalidate Caches → Invalidate and Restart`.

### Paso 8 — Verificar la estructura de carpetas global

Antes de comenzar a desarrollar, familiarízate con la estructura clave:

```
EcoGuiaWear/
├── build.gradle.kts         ← Plugins raíz del proyecto
├── settings.gradle.kts      ← Declaración de módulos (:mobile, :wear, :tv, :shared)
├── gradle.properties        ← Propiedades globales de Gradle
├── local.properties         ← Variables locales y rutas de SDK (NO subir a Git)
├── .env                     ← Variables de entorno de APIs (NO subir a Git)
│
├── shared/                  ← ⭐ LEER PRIMERO — módulo base de todos los demás
│   └── src/main/java/mx/utng/ecoguia/shared/
│       ├── config/          ← EcoGuiaConfig (URLs, constantes)
│       ├── data/            ← Room DB, clientes remotos, repositorio impl
│       └── domain/          ← Modelos de dominio, interfaces de repositorio
│
├── mobile/                  ← App Android Phone
├── wear/                    ← App Wear OS
└── tv/                      ← App Android TV
```

<br>

---

## 🔨 Construcción de los Módulos Paso a Paso

### Compilar todos los módulos (debug)

```bash
# Desde la raíz del proyecto
./gradlew assembleDebug
```

### Compilar módulo individual

```bash
# Solo el módulo mobile
./gradlew :mobile:assembleDebug

# Solo el módulo wear
./gradlew :wear:assembleDebug

# Solo el módulo tv (Android TV)
./gradlew :tv:assembleDebug

# Solo el módulo shared (librería)
./gradlew :shared:assembleDebug
```

### Instalar en dispositivo/emulador conectado

```bash
./gradlew :mobile:installDebug
./gradlew :wear:installDebug
./gradlew :tv:installDebug
```

### Orden recomendado de construcción

```
1. :shared   → Se compila primero (dependencia de todos)
2. :mobile   → Depende de :shared
3. :wear     → Depende de :shared
4. :tv       → Depende de :shared
```

> Para instrucciones detalladas de instalación de los APKs en dispositivos físicos, consulta la  
> 📄 **[Guía de Instalación — Ver PDF generado del proyecto]**  
> (Solicitar al equipo de desarrollo el documento PDF oficial de instalación)

<br>

---

## 🛠️ Tecnologías y Librerías Principales

| Capa | Tecnología | Descripción |
|------|-----------|-------------|
| **Móvil (Android Phone)** | Jetpack Compose, Material 3, Google Maps, CameraX | Panel de exploración, registro de sitios, cámara GeoDrop, Mi Colección y moderación |
| **Wearable (Wear OS)** | Horologist, Wear Compose Foundation, Hardware Sensors | Radar de proximidad, brújula con rotación de azimut y vibración háptica |
| **Smart TV (Android TV)** | Compose for TV, Google Maps 3D Tilt, Key Trapping | Portal 360°, transmisión en vivo, selector de maquetas y modo kiosco |
| **Shared Module** | Room, Ktor Client, Kotlinx Serialization | Modelo de datos unificado, repositorio espacial, cliente Neon PostgreSQL |
| **Base de Datos Remota** | Neon PostgreSQL + PostGIS, Pgcrypto, Citext | Persistencia relacional y espacial |
| **Inteligencia Artificial** | Groq Cloud API (llama-3.3-70b-versatile) | Chat histórico en primera persona de Miguel Hidalgo |
| **Sincronización Real-Time** | Protocolo MQTT (Eclipse Paho / HiveMQ) | Eventos instantáneos entre teléfono y Smart TV |
| **Correos Transaccionales** | Brevo REST API v3 (Sendinblue) | Códigos OTP de recuperación de contraseña |
| **Almacenamiento de Imágenes** | Firebase Storage + Coil Compose | Fotos de GeoDrops y sitios históricos |

<br>

---

## ✨ Funcionalidades Principales por Módulo

### 📱 1. Aplicación Móvil (Android Phone)
- **Exploración & Mapa Interactivo:** Visualización de sitios históricos con marcadores de radar y distancia dinámica.
- **Captura de Geo-Drops:** Cápsulas fotográficas o de texto geolocalizadas con coordenadas GPS automáticas.
- **Mi Colección & Nivel de Explorador:** Guardado de sitios y rutas; rangos dinámicos (*Turista Reciente* → *Guardián del Patrimonio*).
- **Flujo de Alta de Sitios Históricos:** Proceso guiado en 4 pasos con persistencia en Neon PostgreSQL.
- **Miguel Hidalgo IA:** Chat interactivo con respuestas históricas contextuadas y base de conocimiento.
- **Moderación de Comunidad:** Panel de revisión en landscape con aprobación/rechazo en tiempo real.
- **Recuperación de Cuenta por OTP:** Código de 6 dígitos por correo con Brevo.

### ⌚ 2. Aplicación Wear OS (Reloj Inteligente)
- **Brújula Dinámica de Azimut:** Rotación en tiempo real hacia el sitio histórico seleccionado.
- **Radar de Proximidad Háptico:** Distancia en metros con pulsaciones hápticas al aproximarse.
- **Vinculación Rápida:** Sincronización transparente con el teléfono via Data Layer API.

### 📺 3. Aplicación Smart TV (Android TV)
- **Portal 360° de Exhibición:** Transmisión en pantalla grande centrada en el Jardín Principal.
- **Cámara Giratoria 3D:** Mapa inclinado a 45° con rotación automática a 360°.
- **Selector de Estilos de Mapa:** Maqueta Blanca, Neón Oscuro y Satelital con soporte de control remoto.
- **Modo Kiosco:** Bloqueo por PIN de seguridad para exhibiciones públicas.

<br>

---

## 👥 Roles del Sistema

| Rol | Descripción |
|-----|------------|
| 👑 **Super Admin** | Acceso total: gestión de usuarios/roles, control de Smart TV, alta de sitios y moderación |
| 🛡️ **Moderador** | Alta de sitios históricos, rutas turísticas y revisión/aprobación de GeoDrops |
| 🏨 **Museo / Hotel** | Gestión y vinculación de sitios de su establecimiento |
| 🌿 **Visitante / Turista** | Exploración, GeoDrops, brújula Wear OS, rutas y chat con Miguel Hidalgo IA |

<br>

---

## 🗄️ Esquema de Base de Datos (Neon PostgreSQL + PostGIS)

La base de datos de producción tiene **10 componentes activos**:

1. `users` — Registro de usuarios, credenciales hash, roles y perfil
2. `historical_sites` — Puntos históricos con coordenadas PostGIS (`GEOGRAPHY(Point, 4326)`)
3. `geo_drops` — Cápsulas comunitarias con fotos, texto y coordenadas espaciales
4. `user_saved_items` — Ítems y sitios guardados en Mi Colección
5. `routes` — Rutas turísticas creadas por gestores culturales
6. `route_stops` — Secuencia y paradas ordenadas por ruta
7. `site_categories` — Diccionario de categorías (Museo, Monumento, Plaza, etc.)
8. `devices` — Registro de teléfonos, relojes y TVs conectados
9. `device_pairings` — Códigos de vinculación activa entre dispositivos
10. `approved_geo_drops` *(Vista)* — Cápsulas aprobadas para mapas y TV

El esquema SQL completo se encuentra en:  
📄 `shared/src/main/java/mx/utng/ecoguia/shared/ecoguia_postgres_schema.sql`

<br>

---

## 📚 READMEs por Módulo

Para el desarrollo detallado de cada módulo, consulta su README específico:

| Módulo | README | Descripción |
|--------|--------|-------------|
| `mobile` | [mobile/README.md](./mobile/README.md) | Paso a paso para replicar el módulo Android Phone |
| `wear` | [wear/README.md](./wear/README.md) | Paso a paso para replicar el módulo Wear OS |
| `tv` | [tv/README.md](./tv/README.md) | Paso a paso para replicar el módulo Android TV |

<br>

---

<div align="center">
  <p><b>Eco-Guía</b> — <i>Impulsando el Turismo Cultural con Tecnología de Vanguardia.</i></p>
  <p><i>Zahir Andrés Rodríguez Mora &amp; Cesar Enrique Garay García — UTNG GIDS6092</i></p>
</div>
