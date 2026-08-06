# Manual de Instalación y Despliegue - Proyecto Eco-Guía

**Versión del Documento:** 1.0.0  
**Fecha de Publicación:** Agosto 2026  
**Ecosistema:** Android (Smartphone Mobile, Wear OS Smartwatch, Android TV)  

---

## 1. Introducción y Alcance del Documento

Este manual describe detalladamente los pasos técnicos necesarios para preparar el entorno de ejecución, generar los paquetes de instalación ejecutables (**archivos `.apk`**) e instalarlos en los diferentes dispositivos físicos y emulados del ecosistema **Eco-Guía**.

El documento cubre tres métodos de instalación:
1. **Instalación Directa desde Android Studio** (Recomendado para desarrolladores y pruebas rápidas).
2. **Generación e Instalación Manual de APKs** (Para distribución interna a evaluadores y usuarios sin acceso al código fuente).
3. **Generación de APKs Firmados (Release)** (Para despliegues formales y producción).

---

## 2. Requisitos Previos

### 2.1. Requisitos del Teléfono Móvil / Dispositivo Físico
*   **Smartphone Android:** Versión de Android 7.0 (API 24) o superior (Recomendado Android 11+).
*   **Sensores Requeridos:** Cámara trasera funcional, GPS / Ubicación de alta precisión y conexión a Internet (Wi-Fi o Datos Móviles).
*   **Cable USB:** Cable de datos de alta calidad para la conexión con la computadora.

### 2.2. Preparación del Dispositivo Android (Modo Desarrollador)
Antes de poder instalar aplicaciones directamente o mediante comandos ADB, se debe habilitar la depuración USB en el teléfono:

1.  Abre la aplicación de **Ajustes / Configuración** en el celular.
2.  Dirígete a **Acerca del teléfono** (o *Información del software*).
3.  Localiza la opción **Número de compilación** (en dispositivos Xiaomi: *Versión de MIUI / HyperOS*) y **presiónala 7 veces consecutivas** hasta ver el mensaje *"¡Ya eres desarrollador!"*.
4.  Regresa a Ajustes > **Sistema** (o *Ajustes adicionales*) > **Opciones de desarrollador**.
5.  Activa las siguientes casillas:
    *   ✅ **Depuración USB** (USB Debugging).
    *   ✅ **Instalar vía USB** (Install via USB - presente en Xiaomi/MIUI).
    *   ✅ (Opcional) **Depuración inalámbrica** (Wireless Debugging si deseas prescindir del cable).

---

## 3. Preparación Inicial del Código Fuente

Antes de compilar los ejecutables APK, se debe verificar que el proyecto cuente con las credenciales mínimas necesarias:

1.  **Clonar el repositorio y situarse en la rama correcta:**
    ```bash
    git clone https://github.com/Desarrollo-Web-Profesional-zrodriguez/Eco-Guia.git
    cd Eco-Guia
    git checkout main
    ```
2.  **Verificación de Firebase:**
    Asegúrate de que el archivo `google-services.json` esté colocado en las siguientes dos rutas:
    *   `Eco-Guia/mobile/google-services.json`
    *   `Eco-Guia/tv/google-services.json`
3.  **Sincronización de dependencias:**
    Abre el proyecto en Android Studio y espera a que Gradle finalice la descarga de librerías (`Gradle Sync Finished`).

---

## 4. Método 1: Instalación Directa desde Android Studio (Desarrollo)

Este es el método más rápido para probar la aplicación en tiempo real en un teléfono físico o emulador.

### Paso 1: Conectar el Dispositivo
1. Conecta el celular a la computadora mediante el cable USB.
2. En la pantalla del celular aparecerá un aviso: *"¿Permitir depuración USB de esta computadora?"*.
3. Marca la casilla **"Permitir siempre desde esta computadora"** y presiona **Aceptar**.

### Paso 2: Ejecutar la Aplicación
1. En la barra superior de Android Studio, localiza el menú desplegable de módulos y selecciona **`mobile`** (o `wear` / `tv` según corresponda).
2. En el menú desplegable contiguo de dispositivos, selecciona tu teléfono físico conectado (o un emulador activo).
3. Presiona el botón verde de **Run (Play)** ▶️ o utiliza la combinación de teclas `Shift + F10`.
4. Gradle compilará el proyecto, enviará el APK al celular y lo abrirá automáticamente.

---

## 5. Método 2: Generación e Instalación Manual de APKs (Debug)

Si deseas compartir el archivo instalador (`.apk`) con compañeros de equipo, evaluadores o maestros sin que ellos tengan que abrir Android Studio:

### 5.1. Generación de los APKs

#### Opción A: Mediante Línea de Comandos (Terminal)
Abre una terminal en la carpeta raíz del proyecto y ejecuta:

*   **Para el módulo Móvil (Smartphone):**
    ```bash
    # En Windows (PowerShell / CMD)
    .\gradlew :mobile:assembleDebug

    # En Linux / macOS
    ./gradlew :mobile:assembleDebug
    ```
*   **Para el módulo Android TV:**
    ```bash
    .\gradlew :tv:assembleDebug
    ```
*   **Para el módulo Smartwatch (Wear OS):**
    ```bash
    .\gradlew :wear:assembleDebug
    ```

#### Opción B: Mediante la Interfaz de Android Studio
1. En la barra de menú superior de Android Studio, ve a:  
   **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. En la esquina inferior derecha aparecerá una notificación flotante cuando termine: *"Build APK(s): APK(s) generated successfully"*.
3. Haz clic en el enlace azul **`locate`** para abrir la carpeta que contiene el archivo.

---

### 5.2. Ubicación de los Archivos APK Generados

Los ejecutables generados se guardan automáticamente en las siguientes rutas dentro de tu proyecto:

| Módulo | Ruta del Archivo APK Generado |
| :--- | :--- |
| **Móvil (Smartphone)** | `Eco-Guia/mobile/build/outputs/apk/debug/mobile-debug.apk` |
| **Smart TV** | `Eco-Guia/tv/build/outputs/apk/debug/tv-debug.apk` |
| **Smartwatch (Wear)** | `Eco-Guia/wear/build/outputs/apk/debug/wear-debug.apk` |

---

### 5.3. Métodos para Instalar el APK en el Teléfono

#### Alternativa 1: Instalación Rápida mediante ADB (Por cable)
Si tienes el teléfono conectado a la computadora:
```bash
# Sintaxis: adb install -r <ruta_del_apk>
adb install -r mobile/build/outputs/apk/debug/mobile-debug.apk
```
*Si la instalación es exitosa, la terminal responderá con la palabra `Success`.*

#### Alternativa 2: Instalación Directa en el Teléfono (Sin computadora)
1. **Transferir el archivo:** Copia el archivo `mobile-debug.apk` a tu celular mediante cable USB, o envíatelo por Google Drive, WhatsApp, Telegram o correo electrónico.
2. **Descargar y abrir:** En el celular, abre la carpeta de **Descargas** o tu aplicación de **Administrador de Archivos** y toca el archivo `mobile-debug.apk`.
3. **Habilitar orígenes desconocidos:** Si el teléfono muestra la advertencia *"Por motivos de seguridad, tu teléfono no tiene permiso para instalar aplicaciones desconocidas de esta fuente"*:
   - Toca en **Ajustes / Configuración**.
   - Activa el interruptor **"Confiar en esta fuente"** (o *Permitir desde este origen*).
   - Regresa y presiona **Instalar**.
4. **Alerta de Play Protect:** Si Google Play Protect muestra una ventana diciendo *"Aplicación bloqueada por Play Protect"* (común en APKs en desarrollo que no vienen de la tienda oficial), presiona **"Más detalles"** y selecciona **"Instalar de todas formas"**.

---

## 6. Método 3: Generación de APK Firmado (Release / Producción)

Para generar una versión optimizada, ofuscada y firmada con llave criptográfica lista para distribución final:

1. En Android Studio, ve a **Build > Generate Signed Bundle / APK...**
2. Selecciona **APK** y haz clic en **Next**.
3. **Configuración de Llave (Key Store):**
   - Si ya tienes una llave, presiona *Choose existing...*
   - Si no tienes una, presiona *Create new...*, elige una ruta para guardar el archivo `.jks`, define contraseñas seguras y completa los datos de la organización.
4. Selecciona el tipo de compilación **`release`**.
5. Marca las casillas de firma **V1 (Jar Signature)** y **V2 (Full APK Signature)**.
6. Haz clic en **Finish**. El APK optimizado se generará en `mobile/build/outputs/apk/release/mobile-release.apk`.

---

## 7. Configuración Post-Instalación en el Dispositivo

Para garantizar que todas las funciones (Realidad Aumentada, Alertas de Proximidad en Segundo Plano y Geo-Drops) funcionen al 100%, realiza la siguiente configuración inicial al abrir la app:

### 7.1. Concesión de Permisos Iniciales
Al abrir **Eco-Guía** por primera vez o al navegar a la pantalla de **Permisos**:
1. **Ubicación:** Concede el permiso seleccionando **"Mientras la app está en uso"** o **"Permitir siempre"** (requerido para geofencing en segundo plano).
2. **Cámara:** Concede el permiso para habilitar el visor de Realidad Aumentada en los Geo-Drops.
3. **Notificaciones:** Concede el permiso para que el sistema emita alertas cuando te encuentres a menos de 50 metros de un sitio histórico.

### 7.2. Ajustes Especiales para Xiaomi / Redmi / POCO (MIUI / HyperOS)
Los dispositivos Xiaomi cuentan con optimizaciones de batería agresivas que pueden suspender los servicios en segundo plano:
1. Ve a **Ajustes > Aplicaciones > Administrar aplicaciones > EcoGuia**.
2. **Inicio Automático (Autostart):** Activa esta casilla para permitir que el servicio de proximidad funcione con la app cerrada.
3. **Ahorro de Batería:** Cambia la opción a **"Sin restricciones"** (No restrictions).
4. **Ajustes Restringidos:** Si la app fue instalada manualmente, baja hasta la sección *Ajustes avanzados* y activa **"Permitir ajustes restringidos"** para desbloquear la gestión completa de permisos.

---

## 8. Solución de Problemas Comunes de Instalación (FAQ)

### 🔴 Error: "App no instalada" o "Error al analizar el paquete"
*   **Causa:** Existe una versión previa instalada con una firma criptográfica diferente (por ejemplo, compilada desde otra computadora).
*   **Solución:** Desinstala por completo la aplicación `EcoGuia` de tu teléfono y vuelve a intentar la instalación del nuevo APK.

### 🔴 Error: Gradle no genera el APK (`google-services.json missing`)
*   **Causa:** Falta el archivo de configuración de Firebase en el proyecto.
*   **Solución:** Descarga el archivo `google-services.json` de la consola de Firebase y asegúrate de colocarlo dentro de la carpeta `mobile/`.

### 🔴 Error: La terminal no reconoce el comando `adb`
*   **Causa:** La ruta del Android SDK Platform Tools no está agregada a la variable de entorno `PATH` del sistema.
*   **Solución:** Utiliza la ruta completa:
    *   En Windows: `C:\Users\<TuUsuario>\AppData\Local\Android\Sdk\platform-tools\adb.exe install ...`
    *   O simplemente transfiere el archivo `.apk` a la memoria de tu teléfono mediante el explorador de archivos de Windows.

### 🔴 El servicio de proximidad no notifica con la pantalla apagada
*   **Causa:** El sistema operativo cerró el proceso por ahorro de energía.
*   **Solución:** Revisa la sección 7.2 de este manual y desactiva las restricciones de batería para EcoGuía.
