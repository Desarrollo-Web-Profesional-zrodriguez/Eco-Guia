# Plan de Implementación: Conexión del Radar Wear OS a Datos Reales (Neon)

Este plan describe cómo conectar el radar del reloj a la base de datos Neon para que muestre "cápsulas" (Geo-Drops) reales en lugar de datos de prueba.

## Análisis Técnico

Aprovecharemos el repositorio compartido (`EcoGuiaRepository`) que ya tiene la lógica para consultar Neon. El reloj actuará como un buscador de cápsulas en tiempo real.
- **Acceso a Datos:** El reloj usará `EcoGuiaRepository` para obtener los Geo-Drops más cercanos o recientes.
- **Mapeo:** Convertiremos los objetos `RemoteGeoDrop` de la base de datos al formato `RadarTarget` que entiende la interfaz del reloj.
- **Sincronización:** Cuando se registre una nueva cápsula en el celular, el reloj podrá detectarla al refrescar sus datos.

## Cambios Propuestos

### 1. Módulo Wear OS (Capa de Datos)

#### [MODIFY] [DemoRadarRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/repository/DemoRadarRepository.kt)
- Inyectar `EcoGuiaRepository` (la implementación de Neon).
- Añadir lógica para obtener cápsulas reales de la nube.
- Implementar una función que busque cápsulas cercanas (por ahora simularemos la ubicación del reloj en Dolores Hidalgo).

#### [MODIFY] [RadarRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/domain/repository/RadarRepository.kt)
- Añadir el método `refreshNearbyTargets()` a la interfaz.

### 2. Módulo Wear OS (Capa de Presentación)

#### [MODIFY] [RadarViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/presentation/RadarViewModel.kt)
- Añadir una llamada para refrescar los datos al iniciar el radar o mediante un gesto.

### 3. Configuración

#### [MODIFY] [wear/build.gradle.kts](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/build.gradle.kts)
- Asegurar que el módulo wear tenga las dependencias necesarias de Ktor (si se conecta directamente) o simplemente use el módulo compartido correctamente.

## Verificación

1. **Prueba de Flujo:** Registrar un Geo-Drop en el celular con un título específico (ej: "Cápsula Real").
2. **Refresco en Reloj:** Iniciar el radar en el reloj y verificar que el objetivo (`RadarTarget`) ahora muestra el título de la cápsula registrada en Neon.
3. **Distancia Simulada:** Verificar que el reloj calcula una distancia y dirección hacia esa nueva cápsula.

---

> [!IMPORTANT]
> El reloj necesita conexión a internet (vía WiFi o Bluetooth a través del teléfono) para consultar directamente a Neon.

> [!TIP]
> Usaremos una ubicación "base" en Dolores Hidalgo para el reloj, de modo que las cápsulas registradas en esa misma zona aparezcan como objetivos cercanos.
