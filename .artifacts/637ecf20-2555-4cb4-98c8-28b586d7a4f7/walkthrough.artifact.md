# Walkthrough: CRUD de Geo-Drops (Cápsulas) en Neon

Se ha implementado un flujo completo de Registro y Lectura (CR) para las cápsulas de información (Geo-Drops) directamente conectadas a la base de datos Neon PostgreSQL.

## Cambios Realizados

### Capa de Dominio y Datos
- **[EcoGuiaRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/repository/EcoGuiaRepository.kt):** Se añadió el método `createGeoDrop` para permitir la inserción de nuevos registros.
- **[EcoGuiaRepositoryImpl.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/data/repository/EcoGuiaRepositoryImpl.kt):** Implementación de la lógica SQL (`INSERT INTO`) utilizando funciones geográficas de PostGIS para guardar la ubicación exacta.
- **[RemoteEntities.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/model/RemoteEntities.kt):** Ajuste de modelos para manejar campos opcionales generados por la base de datos (ID y fechas).

### Interfaz de Usuario (Mobile)
- **[MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt):**
    - Se integró un nuevo **Gestor de Cápsulas** en el panel de control.
    - **Formulario:** Permite ingresar Título y Descripción.
    - **Lista en Tiempo Real:** Muestra todas las cápsulas guardadas en Neon, ordenadas por fecha de creación (las más recientes primero).
    - **Refresco Automático:** La lista se actualiza automáticamente después de cada registro exitoso.

## Cómo Probar el Registro

1. Abre la aplicación móvil.
2. Desliza hasta la sección **"Nuevo Registro (Neon PostgreSQL)"**.
3. Escribe un título (ej: "Mural de Hidalgo") y una descripción.
4. Presiona **"Guardar en Nube"**.
5. Verás un mensaje de confirmación y el nuevo registro aparecerá instantáneamente en la lista inferior.

> [!TIP]
> Por ahora, el registro utiliza coordenadas fijas de Dolores Hidalgo para simplificar la prueba, pero la infraestructura ya está lista para recibir coordenadas dinámicas del GPS.

> [!IMPORTANT]
> Los datos se guardan en la tabla `geo_drops` de tu base de datos Neon con el estado `approved` para que sean visibles inmediatamente en esta prueba.
