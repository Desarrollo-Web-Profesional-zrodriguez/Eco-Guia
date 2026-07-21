# Walkthrough: Funcionalidad de Autenticación y Documentación Estándar

Se ha implementado la lógica de negocio para el inicio de sesión y registro de usuarios, conectando las interfaces con la base de datos Neon. Además, se ha aplicado el estándar de documentación solicitado en todos los archivos y funciones principales.

## Cambios Realizados

### Capa de Datos (Módulo Shared)
- **[EcoGuiaRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/repository/EcoGuiaRepository.kt):** Se añadieron los métodos `login` y `register` con su respectiva documentación.
- **[EcoGuiaRepositoryImpl.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/data/repository/EcoGuiaRepositoryImpl.kt):**
    - Implementación de seguridad usando `pgcrypto` de PostgreSQL.
    - Las contraseñas se cifran en el servidor usando `crypt` y `gen_salt('bf')`.
    - Se corrigieron las consultas para usar placeholders (`$1`, `$2`) evitando inyecciones SQL.

### Capa de Presentación (Módulo Mobile)
- **[AuthViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/AuthViewModel.kt):** Gestiona el flujo de autenticación, manejando estados de carga, éxito y error.
- **[LoginScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/LoginScreen.kt):** Integrada con el ViewModel para validar credenciales reales.
- **[SignUpScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/SignUpScreen.kt):** Permite la creación de usuarios reales en la nube.

## Estándar de Documentación Aplicado

Todos los archivos modificados incluyen ahora un encabezado con el siguiente formato:
```kotlin
/**
 * Archivo: [Nombre]
 * Autor: ZahirMora
 * Fecha de última actualización: 2026-07-20
 * Descripción: [Detalle de funcionalidad]
 */
```
Además, las funciones principales cuentan con KDoc describiendo su propósito.

## Verificación de Funcionalidad

1.  **Registro:** Al llenar el formulario en "Crea tu cuenta" y presionar el botón, el usuario se guarda en la tabla `users` de Neon.
2.  **Login:** El sistema valida el correo y la contraseña (hace el match del hash en el servidor) y permite el acceso a la pantalla de Exploración.
3.  **Seguridad:** Las contraseñas nunca viajan ni se guardan en texto plano en la base de datos.

> [!IMPORTANT]
> El autor `ZahirMora` ha sido establecido como el responsable de estas actualizaciones siguiendo tus instrucciones.

> [!TIP]
> Puedes usar el correo `cesar@email.com` como prueba inicial después de registrarlo en la pantalla de SignUp.
