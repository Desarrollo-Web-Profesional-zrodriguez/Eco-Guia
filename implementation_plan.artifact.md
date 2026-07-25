# Plan de Implementación: Registro Dinámico de Sitios (Modo Creador)

Este plan permitirá que cualquier usuario (o administrador) pueda registrar nuevos sitios históricos directamente desde la app, utilizando su ubicación GPS actual para "anclar" el sitio en el mapa. Esto facilita las pruebas fuera de Dolores Hidalgo.

## User Review Required

> [!IMPORTANT]
> - El registro de sitios requiere que el usuario esté físicamente en el lugar que desea registrar para obtener las coordenadas más precisas.
> - Se integrará el flujo de 4 pasos ya existente en la UI con la base de datos real.

## Proposed Changes

### 1. Repositorio de Datos [MODIFY]
Añadir la capacidad de guardar nuevos sitios en la base de datos.

#### [MODIFY] [EcoGuiaRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/repository/EcoGuiaRepository.kt)
- Añadir función `createHistoricalSite`.

#### [MODIFY] [EcoGuiaRepositoryImpl.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/data/repository/EcoGuiaRepositoryImpl.kt)
- Implementar la inserción SQL usando PostGIS para convertir latitud/longitud en un tipo `GEOGRAPHY`.

---

### 2. Lógica de Negocio (ViewModel) [NEW]
Crear un ViewModel que gestione el estado temporal del nuevo sitio durante los 4 pasos del registro.

#### [NEW] [SiteRegistrationViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/SiteRegistrationViewModel.kt)
- Almacenar los datos de los 4 pasos:
    - Paso 1: Nombre, categoría, dirección.
    - Paso 2: Historia y descripciones.
    - Paso 3: Coordenadas (con botón para "Capturar ubicación actual").
    - Paso 4: Horarios, costos y accesibilidad.
- Función `registerSite` para persistir todo al finalizar.

---

### 3. Interfaz de Usuario (Admin) [MODIFY]
Conectar las pantallas de "Alta de sitio" con el nuevo ViewModel.

#### [MODIFY] [SiteRegistrationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteRegistrationScreen.kt)
- Vincular campos con el ViewModel.

#### [MODIFY] [SiteLocationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteLocationScreen.kt)
- Añadir botón "Obtener mi ubicación" que consuma el GPS actual.

#### [MODIFY] [SiteOperationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteOperationScreen.kt)
- Ejecutar la acción final de guardado.

---

### 4. Navegación [MODIFY]
Asegurar que el flujo de 4 pasos pase los datos correctamente.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Inyectar el `SiteRegistrationViewModel` compartido entre las pantallas de registro.

## Verification Plan

### Manual Verification
1. **Registro:** Ir al menú de Admin > Alta de sitio.
2. **Paso 3:** Tocar "Obtener mi ubicación" y verificar que las coordenadas cambien a las actuales.
3. **Publicación:** Al terminar el Paso 4, verificar que aparezca la notificación de "Sitio publicado".
4. **Verificación en Mapa:** Regresar a la pantalla de exploración y confirmar que el nuevo sitio aparece marcado en el mapa en tu ubicación actual.
