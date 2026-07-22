# Walkthrough: Módulo de Administración y Moderación

Se ha implementado el paquete completo de administración (`admin`), permitiendo el flujo de alta de sitios históricos en 4 pasos, gestión de medios y moderación de contenidos reportados.

## Cambios Realizados

### Estructura de Paquetes
- Se creó el subpaquete `mx.utng.ecoguiawear.ui.screens.admin` para organizar las interfaces de gestión.

### Flujo de Alta de Sitio (4 Pasos)
1.  **[SiteRegistrationScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteRegistrationScreen.kt):** Recopilación de datos básicos (Nombre, Categoría, Dirección).
2.  **[SiteContentScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteContentScreen.kt):** Edición de textos curados y relatos históricos.
3.  **[SiteLocationScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteLocationScreen.kt):** Configuración de coordenadas geográficas y radio de detección por proximidad.
4.  **[SiteOperationScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteOperationScreen.kt):** Detalles de horarios, costos y accesibilidad. Al finalizar este paso, el sitio se publica.

### Moderación y Seguridad
- **[ModerationListScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ModerationListScreen.kt):** Listado de elementos reportados por la comunidad.
- **[ReportDetailScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ReportDetailScreen.kt):** Interfaz para decidir la acción sobre un reporte (Eliminar, Mover, Aprobar).

### Gestión de Medios y Otros
- **[GalleryAdditionScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/GalleryAdditionScreen.kt):** Formulario para cargar fotografías a los sitios con metadatos.
- **[ManualGeoDropScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ManualGeoDropScreen.kt):** Herramienta para anclar cápsulas manualmente desde la administración.

## Estándar de Documentación ZahirMora
Se ha verificado que todos los archivos (8 en total) en el paquete `admin` cuentan con el encabezado de autoría: **Autor: ZahirMora | Fecha: 2026-07-22**.

## Cómo verificar los cambios

1.  **Navegación Admin:** En el `MainActivity.kt` se han configurado las rutas secuenciales. Puedes navegar desde `site_registration` y avanzar por los pasos hasta la publicación.
2.  **Visual:** Comprueba que las tarjetas de reportes y los formularios mantienen la coherencia con los colores Jade, Gold y DeepBlue del sistema.

> [!TIP]
> Los formularios están diseñados con `LazyColumn` para asegurar que el contenido sea accesible en pantallas de diferentes tamaños.

> [!IMPORTANT]
> El autor `ZahirMora` ha finalizado el modelado estructural de este módulo administrativo.
