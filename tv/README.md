<div align="center">
  <h1>📺 Módulo <code>tv</code> — Eco-Guía Smart TV</h1>
  <p><strong>Guía de Desarrollo — Archivos e Indicaciones por Paso</strong></p>
  <div>
    <img src="https://img.shields.io/badge/Kotlin-2.1.0-blue?style=for-the-badge&logo=kotlin">
    <img src="https://img.shields.io/badge/Compose_for_TV-1.x-4285F4?style=for-the-badge&logo=androidtv">
    <img src="https://img.shields.io/badge/MQTT-HiveMQ-red?style=for-the-badge">
    <img src="https://img.shields.io/badge/minSdk-21-green?style=for-the-badge&logo=android">
    <img src="https://img.shields.io/badge/compileSdk-35-green?style=for-the-badge&logo=android">
    <img src="https://img.shields.io/badge/Android_Studio-Meerkat_2025.1.1-3DDC84?style=for-the-badge&logo=androidstudio">
  </div>
</div>

<br>

> ⚠️ **Dependencia de `shared`:** El módulo `tv` consume `HiveMQManager` y `EcoGuiaRepository` de `:shared` para recibir eventos MQTT del teléfono y cargar los sitios históricos. `:shared` debe compilarse antes de trabajar en `:tv`. Consulta el [README raíz](../README.md) para la configuración del entorno global.

> ⚠️ **Dependencia de HiveMQ:** Para que la comunicación en tiempo real funcione, el broker MQTT de HiveMQ debe estar activo y accesible. Solicitar las credenciales del broker al líder del proyecto.

<br>

---

## 📋 Versiones y Configuración del Módulo

| Parámetro | Valor |
|-----------|-------|
| `namespace` | `mx.utng.ecoguiawear.tv` |
| `applicationId` | `mx.utng.ecoguiawear.tv` |
| `compileSdk` | 35 |
| `minSdk` | 21 (Android TV 5.0+) |
| `targetSdk` | 35 |
| `versionName` | 1.0.0 |
| `JVM Target` | 17 |
| `buildFeatures` | `compose = true` |

<br>

---

## 🗂️ Estructura Completa del Módulo

```
tv/
└── src/main/java/mx/utng/ecoguiawear/tv/
    ├── MainActivity.kt
    ├── network/
    │   └── TvLocalServer.kt
    └── ui/
        ├── navigation/
        │   └── SmartTVNavHost.kt
        ├── screens/
        │   ├── GalleryScreen.kt
        │   ├── HeatmapScreen.kt
        │   ├── LobbyScreen.kt
        │   ├── Portal360Screen.kt
        │   └── components/
        │       ├── KioskUnlockDialog.kt
        │       ├── LobbyComponents.kt
        │       ├── MapStyleSelectorDialog.kt
        │       ├── SiteSelectorDialog.kt
        │       └── SkeletonComponents.kt
        └── theme/
```

<br>

---

## 📄 Descripción Línea a Línea por Archivo

---

### `tv/build.gradle.kts`

- **Bloque de plugins:** Aplicar `android.application` y `kotlin.compose`; no requiere `google.services` a menos que se integre Firebase en el futuro.
- **Bloque `android`:** Declarar `namespace = "mx.utng.ecoguiawear.tv"`, `compileSdk = 35`; en `defaultConfig` usar `applicationId = "mx.utng.ecoguiawear.tv"`, `minSdk = 21`, `targetSdk = 35`.
- **`compileOptions` y `kotlin.compilerOptions`:** Configurar JVM 17 en ambos.
- **`buildFeatures`:** Activar solo `compose = true`.
- **Bloque `dependencies`:** Primera línea: `implementation(project(":shared"))`; luego agregar `androidx.tv.foundation`, `androidx.tv.material3` (Compose for TV), `play.services.maps` (Google Maps), `maps.compose`, `play.services.location`, y el cliente MQTT de HiveMQ (org.eclipse.paho o HiveMQ SDK).

---

### `tv/src/main/AndroidManifest.xml`

- **`uses-feature`:** Declarar `android.software.leanback` con `required = true` para identificar la app como app de TV; declarar `android.hardware.touchscreen` con `required = false` (las TVs no tienen touchscreen).
- **`<application>`:** Agregar `android:banner` con el recurso del banner de la app (imagen de 320x180dp requerida por Google TV/Leanback); agregar el tema `Theme.Leanback` o el tema TV personalizado.
- **Permisos requeridos:** `INTERNET`, `ACCESS_NETWORK_STATE`.
- **`MainActivity`:** Declarar como launcher de TV con intent-filter: acción `MAIN` y categoría `android.intent.category.LEANBACK_LAUNCHER`.
- **`TvLocalServer`:** No requiere declaración en manifiesto; es una clase de utilidad instanciada en `LobbyScreen`.

---

### `tv/MainActivity.kt`

- **Bloque 1 — KDoc:** Descripción: actividad principal del módulo Smart TV; punto de entrada; configura el tema y el host de navegación; autores y fecha.
- **Bloque 2 — `MainActivity : ComponentActivity`:** Hereda de `ComponentActivity`; en `onCreate` llama a `setContent` con `EcoGuiaTVTheme`; dentro del tema, declara un `Box` con `fillMaxSize` y fondo del tema; coloca `SmartTVNavHost()` como contenido principal.
- **Bloque 3 — Anotación `@OptIn`:** El archivo debe tener `@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)` al inicio porque las APIs de Compose for TV son experimentales; esta anotación suprime las advertencias en todo el archivo.

---

### `network/TvLocalServer.kt`

- **Bloque 1 — KDoc:** Descripción: servidor HTTP local que expone un endpoint para que el teléfono pueda enviar comandos a la TV cuando MQTT no está disponible; autores y fecha.
- **Bloque 2 — Configuración del servidor:** Usa `NanoHTTPD` o `Ktor Server` en modo embebido; escucha en el puerto `8080`; el endpoint principal es `GET /command?action=X&siteId=Y`.
- **Bloque 3 — `start()`:** Inicia el servidor HTTP; llama desde `LobbyScreen` al montar la pantalla.
- **Bloque 4 — `stop()`:** Detiene el servidor; llamar al abandonar `LobbyScreen`.
- **Bloque 5 — `onCommandReceived(action, params)`:** Callback que se ejecuta cuando el servidor recibe un comando del teléfono; los comandos posibles son: `navigate_site`, `change_map_style`, `lock_kiosk`, `unlock_kiosk`; notifica a la UI a través de un `SharedFlow` o `StateFlow`.

---

### `ui/theme/`

- **`EcoGuiaTVTheme`:** Composable que aplica `MaterialTheme` de `androidx.tv.material3`; define el color scheme adecuado para pantalla grande (colores vibrantes, buen contraste a distancia); tipografía con tamaños más grandes que la app mobile (mínimo 18sp para texto de cuerpo); el tema envuelve toda la app en `MainActivity`.

---

### `ui/navigation/SmartTVNavHost.kt`

- **Bloque 1 — KDoc:** Descripción: host de navegación de la Smart TV; define todas las rutas y la pantalla inicial; autores y fecha.
- **Bloque 2 — `TvRoutes` object:** Define las rutas como constantes: `LOBBY`, `PORTAL_360`, `GALLERY`, `HEATMAP`.
- **Bloque 3 — `SmartTVNavHost` composable:** Usa `NavHost` de Navigation Compose (compatible con TV); el `startDestination` es `LOBBY`; declara un `composable` por cada ruta.
- **Bloque 4 — Manejo de teclas del control remoto:** La navegación en TV se maneja con el D-Pad y el botón Back del control remoto; `NavHost` gestiona automáticamente el botón Back para volver a la pantalla anterior; asegurarse de que cada pantalla tenga un elemento enfocable al cargar.
- **Bloque 5 — `LOBBY`:** Ruta inicial sin argumentos; instancia `LobbyScreen`.
- **Bloque 6 — `PORTAL_360`:** Ruta con argumento opcional `siteId: Int?`; instancia `Portal360Screen` pasando el sitio a centrar en el mapa.
- **Bloque 7 — `GALLERY`:** Sin argumentos; instancia `GalleryScreen`.
- **Bloque 8 — `HEATMAP`:** Sin argumentos; instancia `HeatmapScreen`.

---

### `ui/screens/LobbyScreen.kt`

- **Bloque 1 — KDoc:** Descripción: pantalla de bienvenida y hub principal de la Smart TV; punto desde donde se accede a todas las funcionalidades; autores y fecha.
- **Bloque 2 — `LobbyScreen` composable:** Pantalla de pantalla completa con fondo oscuro; recibe `navController`.
- **Bloque 3 — Sección superior:** Logo de Eco-Guía centrado y título "Portal de Exhibición Dolores Hidalgo"; subtítulo con la fecha y hora actuales actualizándose con `LaunchedEffect`.
- **Bloque 4 — Grilla de opciones:** Usa `TvLazyVerticalGrid` de Compose for TV con 2 columnas; cada ítem es un `LobbyCard` (definido en `LobbyComponents.kt`) con ícono y etiqueta; opciones: "Portal 360°", "Galería de Cápsulas", "Mapa de Calor", "Estado en Vivo".
- **Bloque 5 — Conexión MQTT:** Al montar la pantalla (`LaunchedEffect(Unit)`), inicializa la conexión con HiveMQ vía `HiveMQManager` de `shared`; suscribirse al topic `/eco-tv-command`; al recibir un comando, navegar automáticamente a la pantalla correspondiente.
- **Bloque 6 — Servidor local:** Inicia `TvLocalServer` al montar; detenerlo en `DisposableEffect` al desmontar.
- **Bloque 7 — Indicador de conexión:** En la esquina superior derecha, muestra un badge de estado MQTT (Verde = conectado, Rojo = sin conexión).
- **Bloque 8 — Bloqueo de kiosco:** Botón flotante en la esquina inferior derecha "Bloquear Kiosco"; al pulsarlo abre `KioskUnlockDialog` para activar el modo kiosco; en modo kiosco este botón cambia a "Desbloquear" y requiere el PIN.

---

### `ui/screens/Portal360Screen.kt`

- **Bloque 1 — KDoc:** Descripción: pantalla de exhibición 360° del mapa de Dolores Hidalgo con rotación automática y cámara inclinada; pantalla principal de exhibición pública; autores y fecha.
- **Bloque 2 — `Portal360Screen` composable:** Recibe `navController` y `siteId: Int?` (sitio a centrar; si es null centra en el Jardín Principal de Dolores Hidalgo).
- **Bloque 3 — Mapa en pantalla completa:** Usa `GoogleMap` de Maps Compose a pantalla completa; configura la cámara con `tilt = 45f` (vista inclinada 3D) y `zoom = 17f` (nivel de detalle de calle); deshabilita todos los controles nativos del mapa (botones de zoom, My Location, etc.) ya que se controla con el D-Pad.
- **Bloque 4 — Rotación automática:** `LaunchedEffect(Unit)` lanza una corrutina que incrementa el `bearing` (ángulo de rotación horizontal) en 0.5° cada 100ms; usa `cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(...))` para la rotación fluida; completa 360° en aproximadamente 2 minutos.
- **Bloque 5 — Overlay de información:** Panel semitransparente en la esquina inferior izquierda con el nombre del sitio siendo exhibido, la categoría y un ícono; se actualiza según el sitio recibido por MQTT o por el argumento `siteId`.
- **Bloque 6 — Controles con D-Pad:** Captura los eventos del control remoto usando `Modifier.onKeyEvent`; D-Pad arriba/abajo cambia el zoom; D-Pad izquierda/derecha cambia el ángulo de rotación manualmente; botón OK abre `SiteSelectorDialog`.
- **Bloque 7 — Selector de estilo:** Botón flotante en la esquina superior derecha "Estilo"; al presionar OK sobre él abre `MapStyleSelectorDialog` con los tres estilos disponibles.
- **Bloque 8 — Recepción MQTT:** Observa los comandos MQTT del `HiveMQManager`; si recibe `navigate_site`, centra el mapa en las coordenadas del nuevo sitio con animación suave.

---

### `ui/screens/GalleryScreen.kt`

- **Bloque 1 — KDoc:** Descripción: galería de GeoDrops aprobados en pantalla grande; pensada para exhibición pública en modo slideshow o selección manual; autores y fecha.
- **Bloque 2 — `GalleryScreen` composable:** Pantalla de fondo oscuro; recibe `navController`.
- **Bloque 3 — Carga de datos:** Al montar, llama a `EcoGuiaRepository.getApprovedGeoDrop()` de `shared` para obtener los GeoDrops aprobados; muestra `SkeletonComponents.GalleryItemSkeleton` mientras carga.
- **Bloque 4 — Grilla de GeoDrops:** `TvLazyVerticalGrid` con 3 columnas; cada ítem muestra la foto del GeoDrop (cargada con Coil), el título y la distancia al sitio relacionado; el ítem enfocado se escala al 110% con animación.
- **Bloque 5 — Vista de detalle:** Al seleccionar un ítem con OK en el D-Pad, expande el GeoDrop a pantalla completa: foto a tamaño completo, título, descripción, autor, fecha y coordenadas GPS.
- **Bloque 6 — Modo slideshow:** Botón "Reproducir Slideshow"; al activarlo, itera los GeoDrops automáticamente cada 5 segundos con transición fade; botón OK pausa/reanuda el slideshow.

---

### `ui/screens/HeatmapScreen.kt`

- **Bloque 1 — KDoc:** Descripción: pantalla del mapa de calor de visitas y GeoDrops en Dolores Hidalgo; visualiza las zonas de mayor actividad turística; autores y fecha.
- **Bloque 2 — `HeatmapScreen` composable:** Mapa de pantalla completa; recibe `navController`.
- **Bloque 3 — Mapa base:** `GoogleMap` centrado en Dolores Hidalgo con `zoom = 14f` para ver toda la ciudad; modo satélite por defecto para mejor contraste del heatmap.
- **Bloque 4 — Capa de heatmap:** Usa la API de `HeatmapTileProvider` de Google Maps; carga las coordenadas de GeoDrops aprobados y visitas registradas desde `EcoGuiaRepository`; aplica el heatmap como capa overlay sobre el mapa.
- **Bloque 5 — Panel de leyenda:** Columna derecha con la leyenda del mapa de calor: gradiente de color (azul frío → rojo caliente) con etiquetas "Pocas visitas" y "Muchas visitas".
- **Bloque 6 — Filtros de datos:** Barra superior con chips para filtrar entre: "Todos", "GeoDrops", "Visitas a sitios", "Rutas completadas"; al cambiar el filtro, recalcula el heatmap.
- **Bloque 7 — Estadísticas en tiempo real:** Panel inferior con: total de GeoDrops en pantalla, total de visitas registradas, sitio más visitado del día; se actualiza cada 30 segundos.

---

### `ui/screens/components/LobbyComponents.kt`

- **Bloque 1 — KDoc:** Descripción: componentes visuales del lobby de la Smart TV; autores y fecha.
- **Bloque 2 — `LobbyCard` composable:** Tarjeta navegable con D-Pad; recibe `icon`, `title`, `subtitle`, `onClick`; muestra el ícono grande centrado y el título debajo; cuando está enfocada (D-Pad), aplica efecto de escala y borde iluminado; al presionar OK ejecuta `onClick`.
- **Bloque 3 — `ConnectionStatusBadge` composable:** Badge de estado de conexión MQTT; muestra un círculo de color con el texto "En Línea" o "Sin Conexión".
- **Bloque 4 — `LiveEventBanner` composable:** Banner animado que aparece en la parte superior cuando el teléfono del moderador envía un evento en tiempo real (nuevo GeoDrop aprobado, nuevo sitio registrado); se muestra 5 segundos y desaparece con animación slide.
- **Bloque 5 — `ClockWidget` composable:** Reloj digital grande para el lobby; muestra hora (HH:MM:SS) y fecha completa; usa `LaunchedEffect` con un `ticker` de 1 segundo para actualizar.

---

### `ui/screens/components/MapStyleSelectorDialog.kt`

- **Bloque 1 — KDoc:** Descripción: diálogo modal para seleccionar el estilo visual del mapa en `Portal360Screen`; autores y fecha.
- **Bloque 2 — `MapStyleSelectorDialog` composable:** `AlertDialog` adaptado a TV con soporte de D-Pad; recibe `onStyleSelected: (MapStyle) → Unit` y `onDismiss: () → Unit`.
- **Bloque 3 — `MapStyle` enum:** Define los estilos disponibles: `WHITE_MOCKUP` (maqueta blanca, estilo minimalista), `DARK_NEON` (modo oscuro con colores neón), `SATELLITE` (vista satelital real).
- **Bloque 4 — Lista de estilos:** `LazyColumn` de 3 ítems, cada uno con nombre del estilo, descripción corta y vista previa en miniatura; el ítem enfocado se resalta; al presionar OK selecciona el estilo y cierra el diálogo.

---

### `ui/screens/components/SiteSelectorDialog.kt`

- **Bloque 1 — KDoc:** Descripción: diálogo modal para seleccionar manualmente el sitio a exhibir en `Portal360Screen`; autores y fecha.
- **Bloque 2 — `SiteSelectorDialog` composable:** `AlertDialog` de TV; recibe `sites: List<HistoricalSite>`, `onSiteSelected: (HistoricalSite) → Unit` y `onDismiss: () → Unit`.
- **Bloque 3 — Lista de sitios:** `LazyColumn` con cada sitio histórico disponible; muestra nombre, categoría y distancia al Jardín Principal; el ítem enfocado se resalta; al presionar OK selecciona el sitio, cierra el diálogo y actualiza el mapa de `Portal360Screen`.
- **Bloque 4 — Campo de búsqueda:** `TextField` en la parte superior del diálogo para filtrar sitios por nombre; compatible con teclado virtual de TV.

---

### `ui/screens/components/KioskUnlockDialog.kt`

- **Bloque 1 — KDoc:** Descripción: diálogo de bloqueo/desbloqueo del modo kiosco; evita que usuarios no autorizados interrumpan la exhibición pública; autores y fecha.
- **Bloque 2 — `KioskUnlockDialog` composable:** Diálogo modal a pantalla completa oscura; recibe `onUnlock: () → Unit` y `onDismiss: () → Unit`.
- **Bloque 3 — Panel de PIN:** Cuadrícula 3x4 de dígitos navegables con D-Pad (como un teclado PIN); el PIN correcto está hardcodeado como constante o leído desde `EcoGuiaConfig` de `shared`; muestra `●` por cada dígito ingresado para ocultar el PIN.
- **Bloque 4 — Lógica de validación:** Acumula los dígitos presionados en una cadena interna; al completar 4 dígitos verifica contra el PIN almacenado; si coincide, llama `onUnlock()`; si no coincide, muestra un mensaje de error ("PIN incorrecto") y limpia los dígitos ingresados.
- **Bloque 5 — Modo kiosco activo:** Cuando el kiosco está bloqueado, se deshabilita el botón Back del control remoto usando `BackHandler`; solo el diálogo de PIN puede desbloquear la app.

---

### `ui/screens/components/SkeletonComponents.kt`

- **Bloque 1 — KDoc:** Descripción: componentes skeleton (pantallas de carga esqueleto) para las pantallas de la TV; autores y fecha.
- **Bloque 2 — `GalleryItemSkeleton` composable:** Placeholder animado de una tarjeta de galería; usa `shimmer` effect (animación de brillo deslizante de izquierda a derecha) con `animateFloat` y un `LinearGradient`; muestra la forma de la tarjeta con colores gris oscuro.
- **Bloque 3 — `HeatmapSkeleton` composable:** Placeholder del mapa de calor mientras carga; muestra un rectángulo gris oscuro del tamaño del mapa con el texto "Cargando mapa..." centrado.
- **Bloque 4 — `SiteCardSkeleton` composable:** Placeholder de una tarjeta de sitio en la lista del selector; muestra barras grises representando el ícono, título y subtítulo.

<br>

---

## 🔗 Integración con el Módulo `shared`

| Componente `tv` | Componente relacionado | Propósito |
|-----------------|----------------------|-----------|
| `LobbyScreen` | `HiveMQManager` (shared) | Recibir comandos MQTT del teléfono en tiempo real |
| `Portal360Screen` | `EcoGuiaRepository` (shared) | Cargar sitios históricos para el selector de sitios |
| `GalleryScreen` | `EcoGuiaRepository` (shared) | Cargar GeoDrops aprobados para la galería |
| `HeatmapScreen` | `EcoGuiaRepository` (shared) | Cargar coordenadas de visitas y GeoDrops para el heatmap |
| `TvLocalServer` | `EcoGuiaConfig` (shared) | Leer el PIN del kiosco y la configuración del servidor |

**Topics MQTT suscritos por la TV:**

| Topic | Contenido | Acción en la TV |
|-------|-----------|----------------|
| `/eco-tv-command/navigate` | `siteId` del sitio a mostrar | Navegar a `Portal360Screen` centrando en ese sitio |
| `/eco-tv-command/style` | Nombre del estilo de mapa | Cambiar el estilo del mapa en `Portal360Screen` |
| `/eco-tv-command/gallery` | Signal de nuevo GeoDrop aprobado | Recargar la galería y mostrar el `LiveEventBanner` |
| `/eco-tv-command/lock` | PIN o signal de bloqueo | Activar el modo kiosco |

<br>

---

## 🧑‍💻 Desarrolladores

| Nombre | Rol |
|--------|-----|
| Zahir Andrés Rodríguez Mora | Desarrollador Principal |
| Cesar Enrique Garay García | Desarrollador |

**Institución:** Universidad Tecnológica del Norte de Guanajuato (UTNG) — Grupo GIDS6092
