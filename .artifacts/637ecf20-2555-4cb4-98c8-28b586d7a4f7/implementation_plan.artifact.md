# Plan de Implementación: Perfil, Notificaciones y Modelado de Pantallas (Mobile)

Este plan detalla la implementación de la actualización de perfil, un sistema de notificaciones centralizado, ajustes en la navegación (cerrar sesión siempre visible) y el modelado de nuevas pantallas de proximidad y captura de Geo-Drops.

## Análisis de Requerimientos

1.  **Actualización de Perfil:** Permitir que el usuario guarde cambios en su nombre desde la pantalla de edición, reflejándose inmediatamente en toda la app.
2.  **Cerrar Sesión Global:** La opción de cerrar sesión debe estar siempre presente en el menú desplegable inferior y redirigir al Login.
3.  **Notificaciones Centralizadas:** Crear un sistema de mensajes (Toasts/Snackbar) reactivo y normalizado para eventos clave (Login exitoso/fallido, perfil editado, sesión cerrada).
4.  **Ajuste Visual:** Eliminar el encabezado con el nombre de la app ("Eco-Guía Control") de la parte superior.
5.  **Modelado de Nuevas Pantallas:**
    *   Alertas de Proximidad.
    *   Cámara Geo-Drops (Captura).
    *   Nueva Cápsula / Anclar Foto.

## Cambios Propuestos

### 1. Capa de Datos (shared)

#### [MODIFY] [EcoGuiaRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/repository/EcoGuiaRepository.kt)
- Añadir `suspend fun updateUser(id: String, displayName: String): Boolean`.

#### [MODIFY] [EcoGuiaRepositoryImpl.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/data/repository/EcoGuiaRepositoryImpl.kt)
- Implementar `updateUser` con una consulta `UPDATE users SET display_name = $1 WHERE id = $2`.

### 2. Sistema de Notificaciones (mobile)

#### [NEW] [NotificationViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/NotificationViewModel.kt)
- Clase central para gestionar una cola de mensajes o un estado de "último mensaje" que la UI mostrará automáticamente.

### 3. Lógica de Usuario y Auth

#### [MODIFY] [AuthViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/AuthViewModel.kt)
- Implementar `updateProfile(newName)` que actualice la base de datos y el estado local `Success(user)`.
- Implementar `logout()` que limpie el estado del usuario.
- Integrar llamadas al `NotificationViewModel` para disparar alertas reactivas.

### 4. Componentes y Navegación

#### [MODIFY] [BottomMenu.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/BottomMenu.kt)
- Asegurar que la opción **"Cerrar Sesión"** sea inyectada en la lista base de `getContextItems`, haciéndola siempre visible.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Eliminar cualquier referencia a `TopAppBar` o títulos de sistema para limpiar el encabezado.
- Integrar un `SnackbarHost` o componente personalizado de notificaciones en el `Scaffold` global vinculado al `NotificationViewModel`.

### 5. Modelado de Pantallas (Screens)

#### [NEW] [ProximityAlertsScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ProximityAlertsScreen.kt)
- Basado en Imagen 2: Título "Alertas Proximidad", card informativa "Geo-Drop oculto cerca" y lista de notificaciones.

#### [NEW] [CameraGeoDropScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CameraGeoDropScreen.kt)
- Basado en Imagen 4: Interfaz de cámara con visor circular, título "Cámara Geo-Drops" y botón "Capturar".

#### [NEW] [AnchorPhotoScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/AnchorPhotoScreen.kt)
- Basado en Imagen 5: Formulario "Nueva cápsula", visor de foto capturada y botón "Anclar foto al sitio".

## Plan de Verificación

1.  **Edición:** Editar el nombre, guardar y verificar que el cambio se ve reflejado inmediatamente en la pantalla de Perfil y se muestra una notificación de éxito.
2.  **Notificaciones:** Validar que aparezca un mensaje visual al hacer login (Éxito o Error) y al cerrar sesión.
3.  **Cierre de Sesión:** Confirmar que al presionar "Cerrar Sesión" se redirige al Login y se muestra la notificación.
4.  **Visual:** Comprobar que el área superior de la app está limpia de títulos automáticos.
5.  **Mapeo:** Navegar a las nuevas pantallas para validar que el diseño estructural coincide con las capturas.

---

> [!IMPORTANT]
> Todas las implementaciones seguirán el estándar de documentación: **Autor: ZahirMora | Fecha: 2026-07-21**.
