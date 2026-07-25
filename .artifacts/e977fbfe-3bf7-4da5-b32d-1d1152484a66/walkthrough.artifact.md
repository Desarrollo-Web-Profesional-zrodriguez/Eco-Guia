# Walkthrough: Sincronización de Radar y Nuevo Nombre "EcoGuia"

Se ha implementado la comunicación en tiempo real entre el móvil y el reloj, y se ha actualizado la identidad de la aplicación.

## Cambios Realizados

### Identidad de la Marca
- **Nuevo Nombre:** La aplicación ha sido renombrada de "Eco-Guía Control" a **"EcoGuia"** tanto en el módulo móvil como en el de Wear OS.
- **Manifiesto y Recursos:** Se actualizaron los labels en `AndroidManifest.xml` y `strings.xml`.

### Sincronización Wear OS (Radar Real)
- **WearMessageClient (Mobile):** Nuevo componente encargado de enviar paquetes de datos de sitios históricos al reloj inteligente.
- **Integración en Exploración:**
    - Al presionar el botón "Ver" en la pantalla de **Exploración** del móvil, se envía automáticamente el ID, nombre y coordenadas del sitio al reloj.
- **Recepción en Wear OS:**
    - El `WearMessageListener` en el reloj ahora escucha la ruta `/eco-guia/sync/target`.
    - El radar del reloj se actualiza instantáneamente con el nuevo objetivo, cambiando su título y reiniciando el modo de escaneo.

## Verificación

### Prueba de Sincronización
1. Abrir **EcoGuia** en el teléfono y en el reloj.
2. En el teléfono, seleccionar un sitio (ej: "Parroquia de Dolores") en la lista de recomendados.
3. **Resultado:** El reloj muestra inmediatamente "Nuevo objetivo: Parroquia de Dolores" y la flecha del radar comienza a apuntar hacia la dirección del sitio.

 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/AndroidManifest.xml)
 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/LocationViewModel.kt)
 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/repository/DemoRadarRepository.kt)
