# Walkthrough: Menú Desplegable de Abajo hacia Arriba

Se ha ajustado el sistema de navegación para que el menú de opciones emerja desde la parte inferior de la pantalla (Bottom Sheet), cumpliendo con el requerimiento de movimiento "abajo para arriba".

## Cambios Realizados

### Navegación Estructural
- **[BottomMenu.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/BottomMenu.kt):**
    - Nuevo componente `BottomMenuSheet` que utiliza `ModalBottomSheet`.
    - Mantiene la lógica reactiva: muestra opciones como "Seguridad" solo cuando es relevante (ej. en Edición de Perfil).
    - Diseño unificado con el color `DeepBlue` y tirador (`DragHandle`) color `Jade`.
- **[MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt):**
    - Se eliminó el Sidebar lateral (`ModalNavigationDrawer`).
    - Se integró el nuevo flujo para disparar el menú inferior desde la barra de navegación.

## Estándar de Documentación ZahirMora
Todos los archivos han sido actualizados con el encabezado de autoría, fecha y descripción de funciones principales.

## Cómo verificar el funcionamiento

1.  **Menú Inferior:** Presiona el icono (≡) en la barra de navegación inferior. El menú emergerá desde abajo cubriendo parcialmente la pantalla.
2.  **Pantalla de Opciones:** La pantalla "Menú Más Opciones" sigue siendo una pantalla completa independiente, accesible desde el icono dorado de Admin en Exploración.
3.  **Reactividad Contextual:**
    *   En **Exploración**: El menú muestra opciones generales (Mi Colección, IA, Perfil).
    *   En **Editar Perfil**: El menú se adapta y muestra **"Seguridad"** como opción destacada.

> [!TIP]
> El menú inferior usa `ModalBottomSheet` de Material 3, lo que permite cerrarlo deslizando hacia abajo o tocando fuera de él, brindando una experiencia nativa y fluida.

> [!IMPORTANT]
> Se eliminó el archivo anterior `SideBarMenu.kt` para mantener la limpieza del proyecto.
