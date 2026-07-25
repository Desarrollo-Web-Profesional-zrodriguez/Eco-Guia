# EcoGuia: Sincronización Real con Wear OS y Cambio de Nombre

Este plan implementa la sincronización real entre el dispositivo móvil y el reloj inteligente, permitiendo que el radar del reloj apunte a un sitio histórico seleccionado en el móvil. Además, se renombrará la aplicación a "EcoGuia".

## Proposed Changes

### Renombrado de la Aplicación

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/AndroidManifest.xml)
- Cambiar `android:label="Eco-Guía Control"` a `android:label="EcoGuia"`.

#### [MODIFY] [strings.xml](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/res/values/strings.xml)
- Cambiar `<string name="app_name">Eco-Guia Wear</string>` a `<string name="app_name">EcoGuia</string>`.

### Sincronización Wear OS (Real-Time Radar)

#### [NEW] [WearMessageClient.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/data/wear/WearMessageClient.kt)
- Implementar un cliente en el módulo móvil para enviar mensajes al reloj usando `Wearable.getMessageClient`.
- Definir la ruta `/eco-guia/sync/target` para enviar datos de sitios.

#### [MODIFY] [LocationViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/LocationViewModel.kt)
- Añadir método `syncTargetWithWatch(site: RemoteHistoricalSite)` que use el `WearMessageClient`.
- Este método enviará el ID, nombre y coordenadas del sitio al reloj.

#### [MODIFY] [ExplorationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ExplorationScreen.kt)
- Llamar a `locationViewModel.syncTargetWithWatch(site)` cuando el usuario presione "Ver" en un sitio recomendado.

#### [MODIFY] [RadarRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/domain/repository/RadarRepository.kt)
- Añadir `setSyncTarget(id: String, name: String, lat: Double, lng: Double)` a la interfaz.

#### [MODIFY] [DemoRadarRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/repository/DemoRadarRepository.kt)
- Implementar `setSyncTarget` para actualizar el `RadarTarget` en el `RadarUiState`.
- Cambiar el modo a `SCANNING` automáticamente al recibir un nuevo objetivo.

#### [MODIFY] [WearMessageListener.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/wear/WearMessageListener.kt)
- Manejar la ruta `/eco-guia/sync/target` y llamar a `repository.setSyncTarget`.

## Verification Plan

### Manual Verification
1. Abrir la app en el móvil y en el reloj (simulador o físico).
2. Verificar que el nombre en el lanzador de apps sea "EcoGuia".
3. En el móvil, ir a **Exploración** y presionar "Ver" en cualquier sitio.
4. El reloj debería actualizar su pantalla de Radar inmediatamente mostrando el nombre del sitio seleccionado y apuntando hacia él (la flecha se moverá según la ubicación).
