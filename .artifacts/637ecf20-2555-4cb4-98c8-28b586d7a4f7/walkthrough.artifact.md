# Walkthrough: Perfil Flexible, Cámara Real y Notificaciones Centralizadas

Se han implementado mejoras críticas en la gestión de usuario, se activó la funcionalidad de cámara real con CameraX y se centralizó el sistema de notificaciones reactivas, todo bajo el estándar de documentación solicitado.

## Cambios Realizados

### Gestión de Usuario (Nombre Flexible)
- **[EditProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/EditProfileScreen.kt):** Se reemplazaron los campos separados por un único campo de **"Nombre Completo"**. Esto permite registrar cualquier número de nombres y apellidos sin restricciones técnicas, guardándolos directamente en la base de datos Neon.
- **Reactividad Inmediata:** Al guardar los cambios, el `AuthViewModel` actualiza el estado local al instante, permitiendo que el nuevo nombre se vea reflejado en todas las pantallas sin necesidad de reiniciar la app.

### Cámara y Captura (CameraX)
- **[CameraGeoDropScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CameraGeoDropScreen.kt):** Se implementó la lógica real de cámara utilizando la librería **CameraX**.
    - Incluye gestión automática de permisos.
    - Muestra un visor en vivo con la interfaz de Realidad Aumentada (AR) superpuesta.
- **[AnchorPhotoScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/AnchorPhotoScreen.kt):** Se completó la lógica del formulario de publicación con notificaciones de éxito al anclar contenido.

### Sistema de Notificaciones Centralizado
- **[NotificationViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/NotificationViewModel.kt):** Centro de mensajes reactivo que dispara Snackbars globales.
- **Eventos Notificados:**
    *   **Login:** "¡Bienvenido, [Nombre]!" o "Credenciales no válidas".
    *   **Perfil:** "Perfil actualizado con éxito".
    *   **Sesión:** "Sesión cerrada".

### Mejoras en Navegación y UI
- **Cerrar Sesión Global:** Se añadió un icono de salida (ExitToApp) permanentemente visible en la barra inferior (color rojo suave) y en el menú desplegable. Al presionarlo, redirige inmediatamente a la pantalla de Login.
- **Limpieza de Cabecera:** Se actualizó el tema del sistema para eliminar el nombre de la app ("Eco-Guía Control") de la barra superior, dejando una interfaz limpia y moderna.

## Estándar de Documentación ZahirMora
Se ha verificado que todos los archivos (Repository, ViewModels, Components, Screens) cuentan con el encabezado de autoría: **Autor: ZahirMora | Fecha: 2026-07-21**.

## Cómo verificar los cambios

1.  **Header:** Al abrir la app, nota que ya no aparece el título gris en la parte superior.
2.  **Nombre Completo:** Ve a Perfil > Editar y cambia tu nombre completo incluyendo apellidos. Guarda y verifica la notificación de éxito y el cambio en la tarjeta de perfil.
3.  **Cerrar Sesión:** Presiona el nuevo icono rojo en la barra inferior o la opción en el menú inferior. Serás llevado al inicio de sesión con un mensaje informativo.
4.  **Cámara:** Desde la barra inferior, presiona el icono de Radar. Acepta el permiso de cámara y verás el visor real de tu dispositivo.

> [!TIP]
> El sistema de cámara ahora detecta si tienes permisos y te los solicita de forma elegante mediante Jetpack Compose.

> [!IMPORTANT]
> Se han normalizado los colores de alerta: Verde para éxitos, Rojo para errores/salida y Azul para información.
