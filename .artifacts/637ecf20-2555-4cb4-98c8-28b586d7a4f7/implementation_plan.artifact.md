# Plan de Implementación: Pantallas de Usuario e Inicio de Sesión (Mobile)

Este plan detalla la construcción de las interfaces de usuario para el inicio de sesión, registro, recuperación de contraseña y exploración en la aplicación móvil, siguiendo el diseño proporcionado en las imágenes.

## Análisis de Diseño

Basado en las imágenes, el sistema visual de la aplicación móvil se caracteriza por:
- **Paleta de Colores:** Fondo azul marino profundo (`#050B10`), acentos en Jade (`#26A69A`) y Oro (`#C5A059`).
- **Componentes:** Campos de texto con bordes muy redondeados y botones con degradados verdes.
- **Iconografía:** Uso de un icono central de "casa" estilizado.
- **Pantalla de Exploración:** Cabecera con un mapa estilizado y lista de sitios con tarjetas redondeadas.

## Cambios Propuestos

### 1. Configuración de Dependencias
Añadiremos la librería de navegación para Jetpack Compose en el módulo móvil.

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/gradle/libs.versions.toml)
- Añadir `navigationCompose = "2.8.5"` y su librería correspondiente.

#### [MODIFY] [mobile/build.gradle.kts](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/build.gradle.kts)
- Implementar la dependencia de navegación.

### 2. Sistema de Diseño (Mobile Theme)

#### [NEW] [Theme.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/theme/Theme.kt)
- Definición de `EcoGuiaMobileTheme` y esquema de colores.

#### [NEW] [Color.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/theme/Color.kt)
- Definición de los colores específicos detectados en las imágenes.

### 3. Componentes Reutilizables

#### [NEW] [CommonComponents.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/CommonComponents.kt)
- `EcoTextField`: Campo de texto personalizado con bordes redondeados.
- `EcoButton`: Botón con degradado Jade.
- `EcoBackground`: Contenedor con el degradado de fondo oficial.

### 4. Pantallas (Screens)

#### [NEW] [LoginScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/LoginScreen.kt)
- Pantalla principal de acceso con campos de correo y contraseña.

#### [NEW] [SignUpScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/SignUpScreen.kt)
- Formulario de registro con Nombre Completo, Correo y Contraseña.

#### [NEW] [RecoveryScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/RecoveryScreen.kt)
- Pantalla para restablecer acceso mediante correo electrónico.

#### [NEW] [ExplorationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ExplorationScreen.kt)
- Pantalla principal de la app con mapa y lista de sitios recomendados.

### 5. Navegación y MainActivity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Reemplazar el `ControlPanel` actual por un `NavHost` que gestione las nuevas pantallas.
- Mantener la funcionalidad de `EcoGuiaRepositoryImpl` para uso futuro.

## Plan de Verificación

1. **Renders de Preview:** Generar previsualizaciones de Compose para cada pantalla para asegurar fidelidad visual.
2. **Pruebas de Flujo:** Navegar entre las 4 pantallas (Login -> SignUp -> Recovery -> Exploration) para validar las transiciones.
3. **Consistencia Visual:** Comparar los resultados con las imágenes de referencia enviadas por el usuario.

---

> [!NOTE]
> Por ahora, las pantallas serán puramente visuales y de navegación. Las funcionalidades de autenticación real se implementarán en una fase posterior.
