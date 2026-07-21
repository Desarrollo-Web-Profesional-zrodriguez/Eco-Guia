# Walkthrough: Datos de Perfil y Navegación de Seguridad Inteligente

Se han integrado los datos reales del usuario autenticado en las pantallas de perfil y se ha optimizado la navegación de seguridad, moviéndola exclusivamente al menú desplegable contextual.

## Cambios Realizados

### Integración de Datos Reales
- **[AuthViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/AuthViewModel.kt):** Se añadió la propiedad `currentUser` para exponer la información del usuario tras un inicio de sesión exitoso.
- **[ProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ProfileScreen.kt):** La pantalla ahora recibe el objeto `user` y muestra dinámicamente el Nombre, Correo e inicial del usuario.
- **[EditProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/EditProfileScreen.kt):** Los campos de edición se inicializan con los datos reales del usuario (Nombre, Apellido, Usuario generado).

### Navegación Contextual (Seguridad)
- **[BottomMenu.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/BottomMenu.kt):**
    - Se implementó lógica para detectar la pantalla actual.
    - El acceso a **"Seguridad"** ahora aparece únicamente en el menú desplegable cuando el usuario está en las pantallas de Perfil o Edición.
    - Se añadió también la opción de **"Cerrar Sesión"** en este menú contextual.
- **[EditProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/EditProfileScreen.kt):** Se eliminó el botón estático de "Seguridad" para limpiar la interfaz, delegando esa función al menú desplegable.

## Estándar de Documentación ZahirMora
Todos los archivos y funciones principales han sido documentados con el encabezado de autoría, fecha y descripción.

## Cómo verificar los cambios

1.  **Login:** Inicia sesión con cualquier usuario registrado (o usa el registro).
2.  **Perfil:** Ve a "Mi Perfil". Verás tu nombre y correo reales de la base de datos Neon.
3.  **Seguridad Dinámica:**
    *   Abre el menú inferior desde **Exploración**: Verás las opciones generales.
    *   Navega a **Mi Perfil**: Abre el menú inferior; verás que ahora aparece la opción **"Seguridad"** y **"Editar Datos"**.
4.  **Edición:** Entra a editar tu perfil. Nota que ya no hay un botón de seguridad estorbando, pero puedes acceder a él desde el menú inferior en cualquier momento mientras estés en esa sección.

> [!TIP]
> La inicial del icono de perfil se genera automáticamente a partir de tu nombre registrado.

> [!IMPORTANT]
> El botón "Cerrar Sesión" en el menú contextual te devolverá a la pantalla de Login y limpiará el estado de navegación.
