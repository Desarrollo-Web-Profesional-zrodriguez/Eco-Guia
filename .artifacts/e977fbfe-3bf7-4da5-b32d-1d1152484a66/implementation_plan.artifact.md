# EcoGuia: Radar de Detección Automática (50km)

Este plan implementa la capacidad del reloj para buscar y apuntar automáticamente al sitio histórico más cercano dentro de un radio de 50 km, siempre que no haya un objetivo manual seleccionado o una ruta activa.

## Proposed Changes

### Lógica de Navegación (Wear OS)

#### [MODIFY] [DemoRadarRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/repository/DemoRadarRepository.kt)
- **Búsqueda Automática de Proximidad:**
    - Al recibir una actualización de ubicación GPS, si el objetivo actual es `"none"`, el repositorio disparará una consulta a la base de datos Neon.
    - Utilizará `getNearbySites(lat, lng, 50000)` para encontrar todos los sitios en un rango de 50 km.
    - Seleccionará el sitio con la menor distancia calculada como el nuevo objetivo del radar.
- **Control de Frecuencia:**
    - Se implementará un mecanismo para evitar consultas excesivas a la red (ej: buscar cada 30 segundos o solo cuando el desplazamiento sea significativo).
- **Prioridad:**
    - Las rutas enviadas desde el móvil y los sitios seleccionados manualmente ("Ver") siempre tendrán prioridad sobre el radar automático. Si llega un comando del móvil, el radar automático se desactivará para ese objetivo.

### Interfaz de Usuario (Wear OS)

#### [MODIFY] [RadarModels.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/domain/model/RadarModels.kt)
- Añadir un estado `isAutoTarget` al `RadarTarget` o `RadarUiState` para indicar visualmente que el objetivo fue detectado automáticamente por proximidad.

#### [MODIFY] [RadarScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/presentation/screens/RadarScreen.kt)
- Mostrar un subtítulo sutil como "Detección automática" cuando el radar esté en este modo.

## Verification Plan

### Manual Verification
1. Abrir **EcoGuia** en el reloj (asegurando que no se haya enviado ninguna ruta desde el móvil).
2. Esperar a que el GPS obtenga señal.
3. **Resultado:** El radar debería dejar de decir "Esperando objetivo" y cambiar automáticamente al nombre del sitio más cercano en un radio de 50 km (ej: "Parroquia de Dolores"), moviendo la flecha hacia esa dirección.
4. Enviar una ruta desde el móvil y verificar que el radar automático se detenga para seguir la ruta indicada.
