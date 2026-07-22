# Plan de Implementación: Módulo de Dispositivos y Control Wear (Mobile)

Este plan detalla la construcción del módulo de gestión de dispositivos ("Dispositivos"), permitiendo vincular, desvincular y monitorear el estado de los periféricos (Relojes Wear OS y Smart TVs) desde el teléfono.

## Análisis de Diseño

1.  **Pantalla "Dispositivos Vinculados":**
    *   Título principal y cabecera "Ecosistema activo".
    *   Lista de dispositivos detectados (TV, Wearables, Sesiones QR).
    *   Iconografía circular para el estado de conexión.
2.  **Pantalla "Gestión de Dispositivos" (Desvincular):**
    *   Interfaz para remover dispositivos vinculados.
    *   Botones de acción "Quitar" y pie de página "Confirmar cambios".
3.  **Pantalla "Estado de Conexión":**
    *   Vista detallada que muestra el estado "CONECTADO" de los servicios críticos: Eco-Guía móvil, GPS preciso y Cámara.

## Cambios Propuestos

### 1. Componentes de Navegación

#### [MODIFY] [MoreOptionsScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/MoreOptionsScreen.kt)
- Añadir la opción **"Dispositivos"** en la cuadrícula de usuario.

#### [MODIFY] [BottomMenu.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/BottomMenu.kt)
- Incluir **"Dispositivos"** en el menú desplegable inferior.

### 2. Nuevas Pantallas (Mobile Screens)

#### [NEW] [LinkedDevicesScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/LinkedDevicesScreen.kt)
- Interfaz del listado principal de dispositivos vinculados.

#### [NEW] [ManageDevicesScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ManageDevicesScreen.kt)
- Interfaz de edición y desvinculación.

#### [NEW] [DeviceStatusScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/DeviceStatusScreen.kt)
- Vista de diagnóstico de conexión inmersiva.

### 3. Navegación y MainActivity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Registrar las rutas: `linked_devices`, `manage_devices`, `device_status`.

## Estándar de Documentación
Cada archivo incluirá: **Autor: ZahirMora | Fecha: 2026-07-22**.

---

> [!NOTE]
> Autor: ZahirMora | Fecha: 2026-07-22
