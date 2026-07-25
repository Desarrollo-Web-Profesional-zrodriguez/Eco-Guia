# Eco-Guía Dolores: Mapa Inteligente con Zoom y Radio Dinámico

Este plan implementa la interactividad avanzada en el mapa de exploración, permitiendo que la búsqueda de sitios se adapte al nivel de zoom y proporcionando controles táctiles mejorados.

## Proposed Changes

### Exploración y Mapa

#### [MODIFY] [LocationViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/LocationViewModel.kt)
- Añadir un método `fetchSitesInRegion(lat, lng, zoom)` que calcule el radio óptimo de búsqueda basado en el nivel de zoom del mapa.
- Mantener el radio de 50km como límite superior para no sobrecargar la consulta.

#### [MODIFY] [ExplorationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ExplorationScreen.kt)
- **Controles de Zoom:** Habilitar los botones nativos de zoom en `MapUiSettings`.
- **Búsqueda Reactiva:** Implementar un `LaunchedEffect` que observe el estado de la cámara. Cuando el mapa deje de moverse (`cameraPositionState.isMoving == false`), se disparará una nueva búsqueda de sitios para la región visible.
- **Botón de Recalibrar:** Añadir un botón flotante sobre el mapa para centrar la vista en el usuario con un zoom de precisión (17f).

## Verification Plan

### Automated Tests
- Verificar la compilación con `gradle assembleDebug`.

### Manual Verification
1. Abrir **Exploración**.
2. Alejar el mapa (Zoom out) y verificar que aparezcan más sitios en un área mayor.
3. Acercar el mapa (Zoom in) y verificar que la lista se filtre a los sitios más inmediatos.
4. Usar los nuevos botones de zoom (+/-) para validar la interactividad.
