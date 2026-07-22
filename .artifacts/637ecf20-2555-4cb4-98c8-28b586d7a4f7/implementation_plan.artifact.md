# Plan de Implementación: Módulo de Administración y Moderación (Mobile)

Este plan detalla la construcción del paquete de administración (`admin`) que incluye las interfaces para el registro de sitios históricos, geolocalización, gestión de contenido y moderación de reportes, siguiendo fielmente los diseños proporcionados.

## Análisis de Pantallas (Módulo Admin)

1.  **Registro de Sitio (Alta):**
    *   `SiteRegistrationScreen`: Datos básicos (Nombre, Categoría, Dirección).
    *   `SiteContentScreen`: Texto turístico curado y descripción histórica.
    *   `SiteLocationScreen`: Mapa interactivo, coordenadas y radio de detección (20-50m).
    *   `SiteOperationScreen`: Horarios, costos y accesibilidad.
2.  **Gestión de Medios:**
    *   `GalleryAdditionScreen`: Formulario para agregar fotos a la galería con texto alternativo.
3.  **Moderación y Reportes:**
    *   `ModerationListScreen`: Listado de contenidos reportados y pendientes de revisión.
    *   `ReportDetailScreen`: Interfaz para resolver reportes (Aprobar, Rechazar, Editar).
4.  **Cápsulas Manuales:**
    *   `ManualGeoDropScreen`: Creación manual de cápsulas con título y ubicación forzada.

## Cambios Propuestos

### 1. Estructura de Paquetes
Crearemos el paquete `mx.utng.ecoguiawear.ui.screens.admin` para organizar estas interfaces de gestión separadas de la experiencia del usuario final.

### 2. Implementación de Pantallas

#### [NEW] [SiteRegistrationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteRegistrationScreen.kt)
- Formulario de alta con campos de texto estilizados.

#### [NEW] [SiteContentScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteContentScreen.kt)
- Gestión de textos históricos.

#### [NEW] [SiteLocationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteLocationScreen.kt)
- Visualización de mapa y configuración de radio de llegada.

#### [NEW] [SiteOperationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteOperationScreen.kt)
- Formulario de datos prácticos.

#### [NEW] [ModerationListScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ModerationListScreen.kt)
- Lista de pendientes con prioridad.

#### [NEW] [ReportDetailScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ReportDetailScreen.kt)
- Acciones de resolución de conflictos de contenido.

### 3. Navegación y MainActivity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Registrar las nuevas rutas de administración.
- Implementar el flujo secuencial de "Alta de Sitio" (Paso 1 al 4).

## Estándar de Documentación
Cada archivo incluirá: **Autor: ZahirMora | Fecha: 2026-07-22**.

## Plan de Verificación
1.  **Navegación Admin:** Probar que el flujo de "Alta de Sitio" navegue correctamente entre los 4 pasos.
2.  **Consistencia de Diseño:** Asegurar que los botones verdes de acción ("Guardar sitio", "Publicar datos", etc.) mantengan el estilo visual unificado.
3.  **Visualización:** Verificar que las tarjetas de reportes en la lista de moderación coincidan con la Imagen 3.

---

> [!IMPORTANT]
> El acceso a este módulo estará restringido por el rol de Administrador en etapas posteriores.

> [!NOTE]
> Autor: ZahirMora | Fecha: 2026-07-22
