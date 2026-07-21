# Plan de Implementación: Integración de Firebase Storage para Imágenes

Este plan detalla los pasos para integrar Firebase en el proyecto Eco-Guía, comenzando con el almacenamiento de imágenes (Geo-Drops) y preparando el terreno para futuras funcionalidades como 2FA y envío de correos.

## Pasos Previos (Acción del Usuario)

Antes de modificar el código, es necesario configurar el proyecto en la consola de Firebase:

1.  **Crear Proyecto:** Ve a [Firebase Console](https://console.firebase.google.com/) y crea un nuevo proyecto llamado `EcoGuiaWear`.
2.  **Registrar App:** Añade una aplicación Android con el package name: `mx.utng.ecoguiawear`.
3.  **Descargar Configuración:** Descarga el archivo `google-services.json`.
4.  **Colocar Archivo:** Mueve el archivo `google-services.json` a la carpeta `mobile/` de tu proyecto.
5.  **Habilitar Storage:** En la consola de Firebase, ve a "Build" > "Storage" y presiona "Get Started" (usa las reglas por defecto en modo prueba por ahora).

## Cambios Propuestos en el SDK

### 1. Configuración de Dependencias

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/gradle/libs.versions.toml)
- Añadir versión para el plugin de Google Services (`4.4.2` o superior).
- Añadir Firebase BoM (Bill of Materials) para gestionar versiones automáticamente.
- Añadir librerías de Firebase Storage y Firebase Auth.

#### [MODIFY] [build.gradle.kts (Root)](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/build.gradle.kts)
- Añadir el plugin de `google-services` en el bloque de `plugins`.

#### [MODIFY] [mobile/build.gradle.kts](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/build.gradle.kts)
- Aplicar el plugin de Google Services.
- Añadir las dependencias de Firebase usando el BoM.

### 2. Capa de Datos (Networking & Storage)

#### [NEW] [FirebaseStorageClient.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/data/FirebaseStorageClient.kt)
- Implementación de un cliente ligero para subir imágenes.
- Función `uploadGeoDropImage(uri): String` que retorne la URL pública de Firebase.

#### [MODIFY] [AuthViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/AuthViewModel.kt)
- Preparar la integración con Firebase Auth para el futuro 2FA.

### 3. Integración en el Flujo de Captura

#### [MODIFY] [AnchorPhotoScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/AnchorPhotoScreen.kt)
- Al presionar "Anclar foto", se disparará la subida a Firebase.
- La URL obtenida se guardará en el registro de Neon PostgreSQL.

## Plan de Verificación

1.  **Sincronización:** Ejecutar `gradle sync` para validar que el SDK se integró correctamente.
2.  **Prueba de Subida:** Capturar una foto, intentar guardarla y verificar que el archivo aparece en el bucket de Firebase Console.
3.  **Link Neon:** Confirmar que la URL de Firebase se guarda correctamente en la columna `media_url` de la tabla `geo_drops` en Neon.

---

> [!IMPORTANT]
> El archivo `google-services.json` es **obligatorio** para que la app inicie. Sin él, la aplicación se cerrará al arrancar.

> [!NOTE]
> Autor: ZahirMora | Fecha: 2026-07-21
