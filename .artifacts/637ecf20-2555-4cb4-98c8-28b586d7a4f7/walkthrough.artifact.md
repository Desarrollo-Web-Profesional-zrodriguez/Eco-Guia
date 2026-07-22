# Walkthrough: Perfil Flexible, Cámara Funcional y Flujo de Rutas

Se han implementado mejoras críticas en la gestión de perfiles, se ha activado la funcionalidad de captura real con CameraX y se han mapeado todas las pantallas del flujo de rutas y búsqueda de experiencias.

## Cambios Realizados

### Perfil y Registro Flexible
- **[SignUpScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/SignUpScreen.kt):** Ahora utiliza un campo de "Nombre Completo" único para evitar problemas con apellidos compuestos.
- **[EditProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/EditProfileScreen.kt):** Simplificada a un solo campo de nombre para total flexibilidad al guardar en Neon.

### Cámara y Captura Real (CameraX)
- **[CameraGeoDropScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CameraGeoDropScreen.kt):**
    - Implementada la lógica de captura real mediante `imageCapture.takePicture`.
    - Al presionar "Capturar", se genera un archivo temporal y se navega automáticamente a la pantalla de anclaje de foto.

### Modelado de Pantallas (Flujo de Rutas)
Se han creado 5 nuevas interfaces basadas en tus diseños:
1.  **[ActiveRouteScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ActiveRouteScreen.kt):** Visualización del progreso de la ruta y paradas completadas.
2.  **[SearchExperienceScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/SearchExperienceScreen.kt):** Filtros rápidos para exploración.
3.  **[PermissionsScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/PermissionsScreen.kt):** Centro de gestión de requerimientos técnicos.
4.  **[CreateRouteScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CreateRouteScreen.kt):** Interfaz para el diseño de nuevas rutas.
5.  **[OfflineRouteScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/OfflineRouteScreen.kt):** Acceso a contenido descargado sin conexión.

### Ajustes Visuales
- **[MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt):** Se añadió `enableEdgeToEdge()` para forzar el diseño de pantalla completa y eliminar el encabezado gris del sistema ("Eco-Guía Control").

## Estándar de Documentación ZahirMora
Se ha verificado que todos los archivos nuevos y modificados cuentan con el encabezado de autoría: **Autor: ZahirMora | Fecha: 2026-07-21**.

## Cómo verificar los cambios

1.  **Captura Real:** Ve al Radar > Acepta permisos > Presiona "Capturar Geo-Drop". Verás que la cámara toma la foto y te lleva a la pantalla de publicación.
2.  **Nombre Completo:** Edita tu perfil poniendo múltiples apellidos. Verifica que se guardan correctamente en Neon.
3.  **Nuevas Pantallas:** Desde la pantalla de Exploración o el menú de Opciones, ahora puedes navegar a las pantallas de Buscar, Ruta Activa y Modo Offline.

> [!TIP]
> Al presionar el icono dorado (+) en Exploración, ahora navegarás a la pantalla de **Búsqueda de Experiencias**.

> [!IMPORTANT]
> El sistema de cámara solicita permisos dinámicamente: asegúrate de otorgarlos en tu dispositivo para ver el visor en vivo.
