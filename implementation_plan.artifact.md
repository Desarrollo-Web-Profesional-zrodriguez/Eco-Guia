# Plan de Implementación: Refinamiento de UX y Catálogos (Alta de Sitio)

Este plan tiene como objetivo profesionalizar el flujo de registro mediante el uso de catálogos dinámicos, sugerencias de direcciones y una interfaz más intuitiva con Chips y placeholders.

## User Review Required

> [!IMPORTANT]
> - Se requiere ejecutar el script SQL proporcionado para crear la tabla de **Categorías** en Neon PostgreSQL.
> - Se integrará la SDK de **Google Places** para las sugerencias de direcciones (requiere que la API Key tenga habilitado "Places API").

## Proposed Changes

### 1. Base de Datos y Repositorio [MODIFY]
Crear el catálogo de categorías y permitir su consulta.

#### [NEW] [site_categories.sql](file:///C:/Users/Lenovo/AppData/Local/Google/AndroidStudio2026.1.2/projects/ecoguiawear.c57ae389/.artifacts/e977fbfe-3bf7-4da5-b32d-1d1152484a66/scratch/site_categories.sql)
- Definición de la tabla `site_categories` e inserciones iniciales (Museo, Monumento, Templo, etc.).

#### [MODIFY] [EcoGuiaRepository.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/domain/repository/EcoGuiaRepository.kt)
- Añadir función `getSiteCategories()`.

---

### 2. Componentes de UI [MODIFY]
Mejorar los componentes base para soportar placeholders y chips.

#### [MODIFY] [CommonComponents.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/CommonComponents.kt)
- Actualizar `EcoTextField` para incluir un parámetro `placeholder`.
- Crear componente `EcoChipGroup` para selección múltiple de accesibilidad.

#### [NEW] [AddressAutocomplete.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/AddressAutocomplete.kt)
- Componente que utiliza la API de Google Places para sugerir direcciones conforme el usuario escribe.

---

### 3. Pantallas de Registro [MODIFY]
Integrar los nuevos controles en el flujo de 4 pasos.

#### [MODIFY] [SiteRegistrationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteRegistrationScreen.kt)
- Cambiar el input de "Categoría" por un selector desplegable (Dropdown) que cargue datos del repositorio.
- Integrar el buscador de direcciones en el campo "Dirección".
- Añadir placeholders de ejemplo (ej. "Nombre: Parroquia de Dolores").

#### [MODIFY] [SiteOperationScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SiteOperationScreen.kt)
- Reemplazar el input de "Accesibilidad" por los **Chips de Selección** (Rampas, Elevador, Braille, etc.).

## SQL para la Consola (Neon)
```sql
CREATE TABLE site_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(30),
    is_active BOOLEAN DEFAULT TRUE
);

INSERT INTO site_categories (name, icon) VALUES
('Museo', 'museum'),
('Monumento', 'account_balance'),
('Plaza', 'location_city'),
('Templo', 'church'),
('Restaurante Histórico', 'restaurant'),
('Parque', 'park'),
('Galería', 'image');
```

## Verification Plan

### Manual Verification
1. **Categorías:** Verificar que al entrar al Paso 1, las categorías se carguen desde la base de datos y aparezcan en una lista.
2. **Dirección:** Escribir una calle conocida en Dolores Hidalgo y confirmar que Google sugiera la dirección completa.
3. **Accesibilidad:** Seleccionar varios Chips (ej. Rampas y Braille) y confirmar que se guarden como texto separado por comas.
4. **Placeholders:** Confirmar que los campos vacíos muestren textos de ayuda en gris claro.
