# Walkthrough: Módulo Extendido de Dispositivos (TV, Analítica y Portal 360)

Se ha completado la expansión del sistema de gestión de dispositivos, integrando las capacidades de control de campañas para Smart TV, el dashboard de analítica de visitantes y el portal inmersivo de museos, cumpliendo con el estándar de documentación ZahirMora.

## Cambios Realizados

### Nuevas Pantallas de Gestión (Paquete Admin)
1.  **[TVCampaignScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/TVCampaignScreen.kt):**
    *   Centro de programación "Salón de la Fama" para hoteles y museos.
    *   Gestión de galerías en lobby, colecciones públicas y rankings semanales.
2.  **[VisitorAnalyticsScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/VisitorAnalyticsScreen.kt):**
    *   Dashboard con mapa de calor de exploración.
    *   Métricas en tiempo real: Visitantes hoy (+15%), Cápsulas vistas y Reportes.
3.  **[CampaignDevicesScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/CampaignDevicesScreen.kt):**
    *   Selector de dispositivos activos para una campaña específica.
    *   Control visual del estado (ON/OFF) por dispositivo.
4.  **[MuseumPortal360Screen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/MuseumPortal360Screen.kt):**
    *   Interfaz inmersiva apaisada para el recorrido 360.
    *   Visualización de puntos de interés y soporte de IA guía.

### Integración de Flujo
- **Acceso Directo:** Se vinculó el botón **"TV"** de la pantalla de Dispositivos Vinculados para entrar directamente al flujo de campañas.
- **Navegación Secuencial:**
    *   Campaña -> Icono Estrella -> Analítica.
    *   Campaña -> Item de Lista -> Dispositivos Seleccionados.
    *   Dispositivos Seleccionados -> Botón "Gestionar Contenido" -> Portal 360.

## Estándar de Documentación ZahirMora
Todos los archivos han sido actualizados con el encabezado de autoría y descripción técnica: **Autor: ZahirMora | Fecha: 2026-07-22**.

## Cómo verificar el ecosistema extendido

1.  **Entrada:** Ve a Menú > Dispositivos.
2.  **Campañas:** Presiona el botón circular **"TV"** en la parte superior derecha. Entrarás a la gestión de Smart TVs.
3.  **Analítica:** Dentro de la pantalla de Campañas, toca el icono dorado de la estrella para ver las métricas y el mapa de calor.
4.  **Selección:** Toca cualquier elemento de la programación (ej. Galería lobby) para ver qué dispositivos están proyectando ese contenido.
5.  **Portal 360:** Desde la pantalla de dispositivos seleccionados, presiona el botón verde inferior para abrir el portal inmersivo del museo.

> [!TIP]
> La pantalla del Portal 360 está diseñada para visualizarse mejor en orientación horizontal o tablets, manteniendo la coherencia de datos del sistema.

> [!IMPORTANT]
> Se han normalizado los iconos (Tv, Star, Group, Phonelink) y los colores para asegurar que el administrador tenga una experiencia de control fluida.
