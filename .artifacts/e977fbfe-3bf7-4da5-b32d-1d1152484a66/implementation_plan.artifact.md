# Eco-Guía Dolores: Sprint de Estabilización y Nuevas Funcionalidades

Este plan aborda la resolución de fallos de compilación, la integración de Google Places API para el registro de sitios y la finalización de las pantallas de usuario.

## User Review Required

> [!IMPORTANT]
> Se requiere una API Key de Google Maps con **Places API** habilitada para que las sugerencias de dirección funcionen. Por ahora se usará un placeholder o se asumirá que la clave ya está configurada en el `Manifest`.

## Proposed Changes

### Build & Infrastructure

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/gradle/libs.versions.toml)
- Revertir Kotlin a `2.4.10` y BOM a `2024.09.00` para asegurar compatibilidad con el entorno actual.

### Registro de Sitios (Admin)

#### [MODIFY] [SiteRegistrationViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/SiteRegistrationViewModel.kt)
- Añadir lógica para buscar direcciones usando `PlacesClient`.
- Manejar estado de carga de categorías.

#### [MODIFY] [SiteRegistrationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteRegistrationScreen.kt)
- Implementar lista de sugerencias de Google Places bajo el campo de dirección.
- Mejorar el feedback visual cuando las categorías están cargando.

### Pantallas de Usuario

#### [NEW] [CollectionScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/CollectionScreen.kt)
- Implementar la vista "Mi Colección" para mostrar sitios visitados o guardados.

#### [NEW] [ProfileScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ProfileScreen.kt)
- Implementar la vista "Mi Perfil" con datos del usuario y estadísticas.

## Verification Plan

### Automated Tests
- Ejecutar `gradlew :mobile:assembleDebug` para verificar la compilación.

### Manual Verification
1. Desplegar en dispositivo `ZY22MKBD44`.
2. Abrir "Alta de Sitio" y verificar que las categorías se carguen desde Neon.
3. Escribir en "Dirección" y verificar que aparezcan sugerencias (si la API Key es válida).
4. Completar el flujo de 4 pasos y verificar la creación del sitio en el logcat.
