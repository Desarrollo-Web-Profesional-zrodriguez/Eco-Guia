# Plan de Documentación KDoc — Módulo `tv`

> **Instrucciones generales:**
> - Usar KDoc estándar de Kotlin: `/** */` para clases y funciones públicas.
> - Cada archivo debe tener un bloque KDoc de cabecera antes del `package`.
> - Etiquetas obligatorias: `@author`, `@since` (fecha del archivo si existe, si no usar `2026-08-05`).
> - Etiquetas de función: `@param` por cada parámetro, `@return` si retorna algo distinto de `Unit`.
> - Autores del proyecto: `Zahir Andrés Rodríguez Mora` y `Cesar Enrique Garay García`.
> - El archivo `MainActivity.kt` del TV tiene la anotación `@file:OptIn(...)` antes del package; el bloque KDoc de cabecera va **después** de esa anotación y **antes** del `package`.
> - Las clases y funciones del módulo `shared` que se usen en `tv` deben estar documentadas en el plan de `shared`, no aquí.

---

## 📄 `tv/MainActivity.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: actividad principal del módulo Smart TV; punto de entrada que configura el tema y el host de navegación; mencionar la anotación `@OptIn` y por qué es necesaria |
| `MainActivity` | class | Qué hace; por qué hereda de `ComponentActivity` y no de `FragmentActivity` |
| `onCreate` | fun | Qué configura en `setContent`; que delega todo el contenido a `SmartTVNavHost` |

---

## 📄 `network/TvLocalServer.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: servidor HTTP local para recibir comandos del teléfono cuando MQTT no está disponible; mencionar el puerto que usa (8080) y el endpoint principal |
| `TvLocalServer` | class | Para qué sirve; cuándo se usa como alternativa a MQTT |
| `start` | fun | Cuándo llamarla; qué pone a escuchar |
| `stop` | fun | Cuándo llamarla; qué libera |
| `onCommandReceived` | fun | `@param action` tipo de comando; `@param params` parámetros adicionales; qué tipos de acción maneja; cómo notifica a la UI |

---

## 📄 `ui/navigation/SmartTVNavHost.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: host de navegación de la Smart TV; define todas las rutas y la pantalla inicial del módulo |
| `TvRoutes` | object | Para qué sirve; que contiene todas las rutas del módulo como constantes |
| Cada constante de ruta | const String | Una línea describiendo a qué pantalla corresponde |
| `SmartTVNavHost` | @Composable fun | Cuál es la pantalla inicial; cómo funciona la navegación con D-Pad; qué hace con el botón Back |

---

## 📄 `ui/screens/LobbyScreen.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: pantalla de bienvenida y hub principal de la Smart TV; punto de entrada desde donde se accede a Portal 360°, Galería y Mapa de Calor |
| `LobbyScreen` | @Composable fun | `@param navController`; qué secciones contiene; que es la pantalla que inicia la conexión MQTT |
| Sección de conexión MQTT | LaunchedEffect interno | Qué topic suscribe; cómo reacciona al recibir un comando |
| `TvLocalServer` en Lobby | DisposableEffect interno | Cuándo inicia y cuándo detiene el servidor local |
| Indicador de conexión | Composable interno | Qué estado muestra y cómo |
| Lógica de modo kiosco | Lógica interna | Cuándo activa `KioskUnlockDialog` y qué hace el modo kiosco |

---

## 📄 `ui/screens/Portal360Screen.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: pantalla de exhibición 360° del mapa de Dolores Hidalgo con rotación automática y cámara inclinada a 45°; es la pantalla principal de exhibición pública |
| `Portal360Screen` | @Composable fun | `@param navController`; `@param siteId: Int?` sitio a centrar; qué parámetros de cámara usa (tilt, zoom, bearing) |
| Rotación automática | LaunchedEffect interno | Qué valor incrementa; a qué velocidad rota; cómo se anima |
| Control D-Pad | onKeyEvent interno | Qué teclas captura y qué efecto tiene cada una |
| Recepción MQTT | Lógica de estado | Qué topic observa; qué hace al recibir `navigate_site` |
| Overlay de información | Composable interno | Qué datos muestra del sitio actual |

---

## 📄 `ui/screens/GalleryScreen.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: galería de GeoDrops aprobados para exhibición pública en pantalla grande; soporta modo grilla y modo slideshow |
| `GalleryScreen` | @Composable fun | `@param navController`; qué datos carga; de dónde los obtiene |
| Carga de datos | LaunchedEffect interno | Qué llama para obtener los GeoDrops; qué muestra mientras carga |
| Grilla principal | TvLazyVerticalGrid interno | Cuántas columnas; efecto de escala al enfocar un ítem |
| Vista de detalle | Composable interno | Cuándo se activa; qué información muestra a pantalla completa |
| Modo slideshow | Lógica interna | Cómo itera los ítems; cada cuántos segundos; cómo pausar |

---

## 📄 `ui/screens/HeatmapScreen.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: visualización del mapa de calor de visitas y GeoDrops en Dolores Hidalgo; muestra las zonas de mayor actividad turística |
| `HeatmapScreen` | @Composable fun | `@param navController`; qué tipo de mapa base usa; qué capa de datos aplica |
| Capa de heatmap | Lógica interna | Qué datos carga; cómo construye el `HeatmapTileProvider` |
| Panel de leyenda | Composable interno | Qué gradiente muestra; qué etiquetas usa |
| Filtros de datos | Composable interno | Qué opciones de filtro ofrece; cómo afectan el heatmap |
| Estadísticas | Composable interno | Qué métricas muestra; cada cuánto se actualiza |

---

## 📄 `ui/screens/components/LobbyComponents.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: componentes visuales del lobby de la Smart TV; incluye tarjetas navegables, indicadores de conexión, banner de eventos y reloj |
| `LobbyCard` | @Composable fun | `@param icon`, `@param title`, `@param subtitle`, `@param onClick`; cómo se comporta al estar enfocado con D-Pad |
| `ConnectionStatusBadge` | @Composable fun | `@param isConnected: Boolean`; qué color y texto muestra en cada estado |
| `LiveEventBanner` | @Composable fun | `@param message`; cuánto tiempo se muestra; qué animación usa |
| `ClockWidget` | @Composable fun | Qué muestra; con qué frecuencia se actualiza |

---

## 📄 `ui/screens/components/MapStyleSelectorDialog.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: diálogo modal para seleccionar el estilo visual del mapa en Portal360Screen; adaptado para navegación con D-Pad |
| `MapStyle` | enum class | Descripción de cada valor: `WHITE_MOCKUP`, `DARK_NEON`, `SATELLITE`; qué apariencia visual genera cada uno |
| `MapStyleSelectorDialog` | @Composable fun | `@param onStyleSelected: (MapStyle) → Unit`; `@param onDismiss`; cómo se navega con D-Pad entre las opciones |

---

## 📄 `ui/screens/components/SiteSelectorDialog.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: diálogo modal para seleccionar el sitio histórico a exhibir en Portal360Screen; adaptado para TV |
| `SiteSelectorDialog` | @Composable fun | `@param sites: List<HistoricalSite>`; `@param onSiteSelected`; `@param onDismiss`; cómo filtra por nombre con el campo de búsqueda |

---

## 📄 `ui/screens/components/KioskUnlockDialog.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: diálogo de bloqueo/desbloqueo del modo kiosco; evita interrupciones no autorizadas en exhibiciones públicas |
| `KioskUnlockDialog` | @Composable fun | `@param onUnlock`, `@param onDismiss`; cómo se muestra el PIN ingresado; cómo valida el PIN |
| Panel de PIN | Composable interno | Cómo funciona la cuadrícula de dígitos; navegación con D-Pad |
| Lógica de validación | Lógica interna | Cuántos dígitos; qué muestra si el PIN es incorrecto; qué limpia al fallar |
| `BackHandler` | Uso interno | Por qué se deshabilita el botón Back del control remoto en modo kiosco |

---

## 📄 `ui/screens/components/SkeletonComponents.kt`
**Fecha:** `2026-08-05`

| Elemento | Tipo | Qué documentar |
|----------|------|----------------|
| Cabecera del archivo | KDoc bloque | Descripción: componentes skeleton (pantallas de carga esqueleto) para mientras se obtienen datos del servidor |
| `GalleryItemSkeleton` | @Composable fun | Qué forma simula; cómo implementa el efecto shimmer |
| `HeatmapSkeleton` | @Composable fun | Qué muestra mientras carga el mapa |
| `SiteCardSkeleton` | @Composable fun | Qué elementos de la tarjeta de sitio simula |

---

## ✅ Checklist de archivos a documentar

- [ ] `tv/MainActivity.kt`
- [ ] `network/TvLocalServer.kt`
- [ ] `ui/navigation/SmartTVNavHost.kt`
- [ ] `ui/screens/LobbyScreen.kt`
- [ ] `ui/screens/Portal360Screen.kt`
- [ ] `ui/screens/GalleryScreen.kt`
- [ ] `ui/screens/HeatmapScreen.kt`
- [ ] `ui/screens/components/KioskUnlockDialog.kt`
- [ ] `ui/screens/components/LobbyComponents.kt`
- [ ] `ui/screens/components/MapStyleSelectorDialog.kt`
- [ ] `ui/screens/components/SiteSelectorDialog.kt`
- [ ] `ui/screens/components/SkeletonComponents.kt`
