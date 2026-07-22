# Plan de Implementación: Nuevas Pantallas de Ruta y Ajustes de Perfil

Este plan detalla la construcción de las pantallas de Ruta Activa, Búsqueda de Experiencia, Permisos, Creación de Ruta y Modo Offline. Además, se incluyen correcciones críticas en la gestión del nombre del perfil y ajustes visuales de cabecera.

## Análisis de Requerimientos

1.  **Perfil Flexible:** Consolidar el nombre del usuario en un solo campo de "Nombre Completo" para soportar múltiples nombres y apellidos sin errores de guardado.
2.  **Limpieza Visual:** Eliminar el encabezado del sistema ("Eco-Guía Control") para lograr una interfaz de pantalla completa inmersiva.
3.  **Funcionalidad de Cámara:** Refinar la captura de Geo-Drops con CameraX para que el botón de captura sea funcional.
4.  **Mapeo de Pantallas (Flujo de Ruta):**
    *   **ActiveRouteScreen:** Seguimiento de la ruta actual y paradas.
    *   **SearchExperienceScreen:** Filtros rápidos de exploración.
    *   **PermissionsScreen:** Gestión de requerimientos técnicos (GPS, Cámara).
    *   **CreateRouteScreen:** Interfaz de diseño de rutas personalizadas.
    *   **OfflineRouteScreen:** Acceso a contenido local descargado.
5.  **Documentación ZahirMora:** Mantener el estándar de comentarios en todos los componentes.

## Cambios Propuestos

### 1. Ajustes Estructurales y de Perfil (mobile)

#### [MODIFY] [EditProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/EditProfileScreen.kt)
- Asegurar que solo exista un campo `fullName`.
- Validar que al guardar se pase la cadena completa al ViewModel.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Forzar el diseño "Edge-to-Edge" si es necesario para ocultar barras persistentes.
- Registrar las nuevas rutas de navegación.

### 2. Modelado de Nuevas Pantallas (mobile)

#### [NEW] [ActiveRouteScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ActiveRouteScreen.kt)
- Basado en Imagen 1: Lista de paradas numeradas con check de completado.

#### [NEW] [SearchExperienceScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/SearchExperienceScreen.kt)
- Basado en Imagen 2: Tarjetas de filtros rápidos (Museos, Geo-Drops, etc.).

#### [NEW] [PermissionsScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/PermissionsScreen.kt)
- Basado en Imagen 3: Interruptores visuales para Ubicación, Cámara y Notificaciones.

#### [NEW] [CreateRouteScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CreateRouteScreen.kt)
- Basado en Imagen 4: Visor de mapa con formulario de 3 pasos (Sitios, Orden, Recompensa).

#### [NEW] [OfflineRouteScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/OfflineRouteScreen.kt)
- Basado en Imagen 5: Lista de contenido local disponible sin conexión.

### 3. Funcionalidad de Cámara (mobile)

#### [MODIFY] [CameraGeoDropScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CameraGeoDropScreen.kt)
- Implementar la función de disparo (`ImageCapture`) para guardar temporalmente la imagen y proceder al anclaje.

## Plan de Verificación

1.  **Prueba de Nombre:** Registrar un usuario con 4 nombres/apellidos, editarlo y verificar que se guarda íntegro en Neon.
2.  **Validación Visual:** Comprobar que no existe ninguna barra de título con "Eco-Guía Control".
3.  **Flujo de Ruta:** Navegar desde Exploración -> Buscar -> Permisos -> Crear Ruta para validar la continuidad visual.
4.  **Cámara:** Verificar que al presionar "Capturar" se toma la foto y se navega correctamente a la pantalla de anclaje.

---

> [!IMPORTANT]
> Se priorizará la flexibilidad del campo de nombre para evitar problemas con apellidos compuestos.

> [!NOTE]
> Autor: ZahirMora | Fecha: 2026-07-21
