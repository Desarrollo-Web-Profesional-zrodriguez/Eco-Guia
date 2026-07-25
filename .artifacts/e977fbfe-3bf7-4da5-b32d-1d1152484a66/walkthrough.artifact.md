# EcoGuia: El Radar Real y Guía por Rutas

Se ha transformado el radar del reloj de una demostración visual a una herramienta de navegación real basada en GPS y soporte para rutas de múltiples puntos.

## Cambios Realizados

### Navegación Real (Wear OS)
- **Activación de GPS:** El reloj ahora solicita y utiliza permisos de ubicación precisa (`ACCESS_FINE_LOCATION`).
- **Cálculo de Rumbo (Bearing):** La flecha del radar ya no gira al azar. Ahora calcula matemáticamente el ángulo real entre la posición GPS actual del reloj y las coordenadas del objetivo.
- **Soporte para Rutas:**
    - El reloj puede recibir una secuencia de puntos (waypoints) desde el móvil.
    - **Progresión Automática:** Cuando el usuario llega a un punto de la ruta (radio de 5 metros), el reloj vibra y apunta automáticamente al siguiente destino.

### Sincronización Avanzada (Mobile)
- **Protocolo de Rutas:** Se implementó `/eco-guia/sync/route` para enviar rutas completas al reloj.
- **Botón de Prueba:** Se añadió un botón "Probar Ruta" en la pantalla de **Exploración** que envía una "Ruta del Centro" (Parroquia -> Museo -> Casa Hidalgo) para validar el funcionamiento.

### Identidad
- **Renombrado Final:** La aplicación ahora se identifica formalmente como **"EcoGuia"** en todos los menús y sistemas de ambos dispositivos.

## Verificación

### Prueba del Radar Real
1. Abrir **EcoGuia** en el reloj.
2. En el móvil, presionar **"Probar Ruta"**.
3. **Resultado en Reloj:**
    - La flecha apuntará al primer punto.
    - Si caminas hacia él, la distancia disminuirá en tiempo real.
    - Al llegar, verás "¡Llegaste!" y la flecha cambiará para apuntar al siguiente sitio de la ruta.

 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/repository/DemoRadarRepository.kt)
 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/wear/LocationHelper.kt)
