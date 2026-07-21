# Plan de Implementación: Navegación por Sidebar y Pantalla de Opciones (Mobile)

Este plan detalla la corrección del sistema de navegación para incluir un menú lateral (Sidebar) reactivo y transformar el menú de opciones en una pantalla completa, siguiendo el flujo y diseño proporcionado en las imágenes.

## Análisis de Requerimientos

1.  **Menú Más Opciones:** Deja de ser un BottomSheet para convertirse en una **pantalla completa** (`MoreOptionsScreen`) con la cuadrícula de accesos.
2.  **Menú Desplegable (Sidebar):** Se implementará un Sidebar lateral con el mismo color `DeepBlue` de la barra inferior. Este menú será **reactivo**: mostrará opciones relevantes según la pantalla actual (ej. en Edición de Perfil mostrará "Seguridad").
3.  **Restricciones de Rol:** El usuario normal solo verá habilitado el acceso a "Mi colección", mientras que el administrador tendrá acceso total.
4.  **Documentación ZahirMora:** Todos los archivos nuevos y modificados mantendrán el estándar de comentarios con autor y descripción.

## Cambios Propuestos

### 1. Componentes de Navegación

#### [NEW] [SideBarMenu.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/SideBarMenu.kt)
- Implementación del contenido del `ModalNavigationDrawer`.
- Lógica reactiva para mostrar títulos dinámicos según el contexto de la aplicación.
- Estilo visual unificado con `DeepBlue` y acentos `Jade`.

#### [MODIFY] [EcoNavigation.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/EcoNavigation.kt)
- Eliminar `MoreOptionsSheet`.
- Ajustar `EcoBottomBar` para que el cuarto icono dispare la apertura del Sidebar.

### 2. Pantallas (Screens)

#### [NEW] [MoreOptionsScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/MoreOptionsScreen.kt)
- Pantalla completa basada en la imagen 3.
- Cuadrícula de tarjetas para: Mi colección, Miguel Hidalgo IA, Mi perfil, Modo offline, Ajustes y Panel.

### 3. Actividad Principal y Navegación

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Integrar `ModalNavigationDrawer` como contenedor raíz de la aplicación principal.
- Configurar el estado del Drawer (`DrawerState`) para que sea controlado por la barra inferior.
- Añadir la ruta `more_options` al `NavHost`.
- Pasar el estado del rol de usuario (Admin/Normal) a todos los componentes de navegación.

## Plan de Verificación

1.  **Sidebar Reactivo:** Verificar que en la pantalla de "Edición de Perfil" el Sidebar muestre la opción "Seguridad" (Imagen 6).
2.  **Navegación de Pantalla:** Validar que se puede navegar a la pantalla de "Más Opciones" y desde allí a "Mi Colección".
3.  **Fidelidad Visual:** Asegurar que el Sidebar use el color `DeepBlue` exacto de la barra inferior.
4.  **Validación de Roles:** Confirmar que un usuario normal vea las opciones bloqueadas excepto "Mi Colección".

---

> [!IMPORTANT]
> El Sidebar debe sentirse como una extensión natural de la interfaz, manteniendo la normalización de colores en toda la aplicación.

> [!NOTE]
> Autor: ZahirMora | Fecha: 2026-07-21
