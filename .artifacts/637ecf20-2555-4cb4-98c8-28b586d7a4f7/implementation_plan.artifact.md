# Plan de Implementación: Autenticación y Documentación

Este plan detalla la implementación de la lógica de inicio de sesión y registro de usuarios en la aplicación móvil, conectándolos con la base de datos Neon PostgreSQL, e incluyendo los estándares de documentación solicitados.

## Análisis Técnico

Utilizaremos el esquema de base de datos existente que incluye la tabla `users` con soporte para `pgcrypto` (para manejo seguro de contraseñas mediante hashing en el servidor).

### Funcionalidades a implementar:
- **Registro:** Inserción de un nuevo usuario en la tabla `users`. La contraseña se guardará usando `crypt()` de PostgreSQL.
- **Inicio de Sesión:** Consulta que verifica las credenciales del usuario comparando el hash de la contraseña.
- **Documentación:** Cada archivo y función principal incluirá un bloque de comentarios con:
    - Autor: `ZahirMora`
    - Última actualización: `2026-07-20`
    - Descripción de la funcionalidad.

## Cambios Propuestos

### 1. Capa de Datos (shared)

#### [MODIFY] [RemoteEntities.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/model/RemoteEntities.kt)
- Añadir encabezado de documentación.

#### [MODIFY] [EcoGuiaRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/repository/EcoGuiaRepository.kt)
- Añadir métodos `login(email, password)` y `register(name, email, password)`.
- Añadir encabezado de documentación.

#### [MODIFY] [EcoGuiaRepositoryImpl.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/data/repository/EcoGuiaRepositoryImpl.kt)
- Implementar `login` usando `SELECT * FROM users WHERE email = ? AND password_hash = crypt(?, password_hash)`.
- Implementar `register` usando `INSERT INTO users (display_name, email, password_hash) VALUES (?, ?, crypt(?, gen_salt('bf')))`.
- Añadir encabezado de documentación y comentarios en funciones.

### 2. Capa de Presentación (mobile)

#### [NEW] [AuthViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/presentation/AuthViewModel.kt)
- Gestionar el estado de autenticación (Loading, Success, Error).
- Llamar a los métodos del repositorio.
- Incluir documentación completa.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/LoginScreen.kt)
- Integrar con `AuthViewModel`.
- Mostrar mensajes de error o indicadores de carga.
- Añadir encabezado de documentación.

#### [MODIFY] [SignUpScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/SignUpScreen.kt)
- Integrar con `AuthViewModel`.
- Añadir encabezado de documentación.

## Verificación

1. **Prueba de Registro:** Crear un usuario nuevo y verificar su existencia en la tabla `users` de Neon.
2. **Prueba de Login:** Intentar acceder con las credenciales creadas.
3. **Validación de Documentación:** Revisar que todos los archivos modificados tengan el formato de comentarios solicitado.

---

> [!IMPORTANT]
> Estamos utilizando funciones nativas de PostgreSQL (`crypt` y `gen_salt`) para la seguridad. Esto asegura que las contraseñas nunca se guarden en texto plano, cumpliendo con las mejores prácticas de seguridad.

> [!WARNING]
> La extensión `pgcrypto` debe estar habilitada en tu base de datos Neon (el script de esquema inicial ya incluye la instrucción `CREATE EXTENSION IF NOT EXISTS pgcrypto;`).
