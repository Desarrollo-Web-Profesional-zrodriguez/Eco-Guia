# Walkthrough: Pantallas de Usuario y Exploración (Mobile)

Se ha implementado el sistema visual y las pantallas principales de la aplicación móvil siguiendo fielmente el diseño proporcionado. Se ha normalizado la paleta de colores para asegurar consistencia en todos los módulos del proyecto.

## Cambios Realizados

### Sistema de Diseño Unificado
- **[Color.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/theme/Color.kt):** Se definió el objeto `EcoGuiaColors` con los colores oficiales:
    - **Background:** `#050B10` (Azul Profundo)
    - **Jade:** `#26A69A` (Verde Principal)
    - **Gold:** `#C5A059` (Dorado de acento)
    - **Surface:** `#0E2A3F` (Azul para tarjetas)
- **[Theme.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/theme/Theme.kt):** Se implementó `EcoGuiaMobileTheme` para aplicar estos colores globalmente en la app móvil.

### Componentes Reutilizables
- **`EcoTextField`**: Campos con bordes muy redondeados y colores integrados al tema.
- **`EcoButton`**: Botones con degradado Jade/Oro.
- **`EcoBackground`**: Contenedor con degradado vertical para todas las pantallas de acceso.

### Pantallas Implementadas
1.  **[LoginScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/LoginScreen.kt):** Acceso principal con Logo y campos de credenciales.
2.  **[SignUpScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/SignUpScreen.kt):** Registro de nuevos usuarios.
3.  **[RecoveryScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/RecoveryScreen.kt):** Restablecimiento de contraseña.
4.  **[ExplorationScreen](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ExplorationScreen.kt):** Interfaz principal con cabecera oscura, placeholder de mapa y lista de sitios recomendados.

### Navegación
- Se configuró un `NavHost` en **[MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)** que conecta todas las pantallas.
- El antiguo panel de control ahora es accesible como el **Panel Admin** desde la pantalla de exploración.

## Verificación Visual

Se han añadido funciones de `@Preview` en cada archivo de pantalla para validar el diseño sin necesidad de desplegar en un dispositivo físico.

---

> [!TIP]
> Los colores utilizados coinciden exactamente con los del módulo Wear OS para cumplir con el requisito de normalización visual.

> [!NOTE]
> Por ahora, las pantallas son visuales. La integración con el repositorio de Neon para el login real se realizará en el siguiente paso.
