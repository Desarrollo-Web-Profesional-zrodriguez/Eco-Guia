# Plan de Implementación: Módulo Extendido de Dispositivos (TV & Analítica)

Este plan detalla la construcción de las interfaces avanzadas para el control de Smart TVs, analítica de visitantes y el portal inmersivo 360, integrándolos en el flujo del módulo "Dispositivos".

## Análisis de Flujo

Basado en las nuevas imágenes, el flujo de administración de dispositivos se expande:
1.  **LinkedDevicesScreen:** Punto de entrada.
2.  **TVCampaignScreen:** Gestión de campañas "Salón de la Fama" para Hoteles y Museos.
3.  **VisitorAnalyticsScreen:** Visualización de métricas y mapa de calor de visitantes.
4.  **CampaignDevicesScreen:** Selección y estado de dispositivos para una campaña específica.
5.  **MuseumPortal360Screen:** Configuración del recorrido inmersivo 360 para lobby y casa.

## Cambios Propuestos

### 1. Pantallas de Administración (admin package)

#### [NEW] [TVCampaignScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/TVCampaignScreen.kt)
- Interfaz de programación para galerías en Smart TVs.
- Secciones: Galería lobby, Colección pública, Ranking semanal.

#### [NEW] [VisitorAnalyticsScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/VisitorAnalyticsScreen.kt)
- Dashboard de métricas: Visitantes hoy, Cápsulas vistas, Reportes.
- Card de "Mapa de calor".

#### [NEW] [CampaignDevicesScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/CampaignDevicesScreen.kt)
- Lista de dispositivos seleccionados con interruptores de estado.
- Botón inferior "Gestionar contenido".

#### [NEW] [MuseumPortal360Screen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/MuseumPortal360Screen.kt)
- Vista inmersiva apaisada (simulada) con indicadores de puntos e IA.

### 2. Navegación y MainActivity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Registrar las nuevas rutas: `tv_campaign`, `visitor_analytics`, `campaign_devices`, `portal_360`.

#### [MODIFY] [LinkedDevicesScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/LinkedDevicesScreen.kt)
- Vincular el botón "TV" (en la cabecera) para navegar a `tv_campaign`.

## Estándar de Documentación
Cada archivo incluirá: **Autor: ZahirMora | Fecha: 2026-07-22**.

---

> [!IMPORTANT]
> El diseño del Portal 360 se maquetará respetando la jerarquía visual de la imagen 4, enfocándose en la presentación de métricas inmersivas.

> [!NOTE]
> Autor: ZahirMora | Fecha: 2026-07-22
