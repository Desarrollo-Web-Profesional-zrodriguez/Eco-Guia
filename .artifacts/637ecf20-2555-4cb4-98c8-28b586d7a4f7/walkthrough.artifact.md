# Walkthrough: Perfil Dinámico, Notificaciones y Modelado de Captura

Se ha implementado el sistema de actualización de perfil, un gestor centralizado de notificaciones y se han mapeado visualmente las pantallas de alertas y captura de Geo-Drops, cumpliendo con el estándar de documentación solicitado.

## Cambios Realizados

### Gestión de Usuario y Perfil
- **[AuthViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/AuthViewModel.kt):**
    - Implementación de `updateProfile(newName)` para sincronizar cambios con Neon.
    - Implementación de `logout()` para limpiar el estado de sesión.
    - Integración con el sistema de notificaciones para alertas de éxito/error.
- **[EditProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/EditProfileScreen.kt):** Ahora permite editar el nombre y guardar los cambios directamente en la nube.

### Sistema de Notificaciones Centralizado
- **[NotificationViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/NotificationViewModel.kt):** Gestiona una cola de mensajes (Success, Error, Info) que se muestran mediante un Snackbar global en toda la aplicación.
- **[MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt):** Configuración del `SnackbarHost` en el `Scaffold` raíz para que las notificaciones sean visibles desde cualquier pantalla.

### Mapeo de Nuevas Pantallas (Modelado Visual)
Se han creado las interfaces basadas en las capturas de flujo de captura:
1.  **[ProximityAlertsScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ProximityAlertsScreen.kt):** Centro de notificaciones de proximidad con card "Geo-Drop oculto cerca".
2.  **[CameraGeoDropScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CameraGeoDropScreen.kt):** Interfaz de cámara con visor AR y botón de captura.
3.  **[AnchorPhotoScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/AnchorPhotoScreen.kt):** Formulario para publicar y anclar fotos a sitios históricos.

### Navegación y Limpieza UI
- **[BottomMenu.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/BottomMenu.kt):** La opción "Cerrar Sesión" ahora está siempre presente en el menú inferior.
- **Header:** Se ha limpiado la parte superior de la aplicación eliminando títulos automáticos del sistema.

## Estándar de Documentación ZahirMora
Todos los archivos y funciones críticas han sido actualizados con los comentarios de autoría, fecha y descripción.

## Cómo verificar los cambios

1.  **Actualizar Perfil:** Ve a Perfil > Editar, cambia tu nombre y guarda. Recibirás una notificación y el cambio se verá reflejado instantáneamente.
2.  **Notificaciones:** Al iniciar sesión o fallar en las credenciales, verás un Snackbar en la parte inferior con el mensaje correspondiente.
3.  **Cerrar Sesión:** Abre el menú inferior (≡) desde cualquier pantalla y presiona "Cerrar Sesión". Serás redirigido al Login con una notificación confirmando la salida.
4.  **Flujo de Captura:** Desde el icono de Radar en la barra inferior (simulado), puedes navegar al flujo: Alertas > Cámara > Anclar Foto.

> [!TIP]
> El sistema de notificaciones es reactivo: si disparas múltiples alertas, estas se mostrarán de forma ordenada.

> [!IMPORTANT]
> El autor `ZahirMora` ha validado la consistencia visual y técnica de este entregable.
