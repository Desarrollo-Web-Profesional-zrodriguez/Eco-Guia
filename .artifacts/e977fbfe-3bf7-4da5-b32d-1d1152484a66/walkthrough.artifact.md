# Resumen de Estabilización y Mejoras: Sprint Dolores Hidalgo

Se han resuelto los problemas de compilación y se ha completado el flujo de registro de sitios con integraciones avanzadas.

## Cambios Realizados

### Estabilización del Proyecto
- **Kotlin & Compose:** Se alinearon las versiones en `libs.versions.toml` a Kotlin `2.4.10` y Compose BOM `2024.09.00`, eliminando los errores de incompatibilidad de metadatos.
- **UI Components:** Se reemplazaron componentes inestables (`FlowRow`, `ExposedDropdownMenuBox`) por alternativas más robustas (`LazyRow`, `DropdownMenu` estándar) para evitar cierres inesperados en dispositivos físicos.

### Registro de Sitios (4 Pasos)
- **Categorías Dinámicas:** Ahora se cargan desde Neon PostgreSQL con un estado de "Cargando" visual.
- **Google Places API:** Se integró la búsqueda de direcciones en tiempo real para facilitar el alta de sitios.
- **Mapa Interactivo:** El Paso 3 permite seleccionar la ubicación tocando el mapa o capturando el GPS actual, con vista previa del radio de detección.
- **Selector de Horarios:** Integrado un `TimePicker` nativo para definir apertura y cierre.

### Pantallas de Usuario
- **Mi Colección:** Implementada con carga de datos desde el repositorio (con fallback simulado). Muestra sitios, fotos y rutas guardadas.
- **Mi Perfil:** Vinculado al `AuthViewModel` para mostrar datos reales del usuario autenticado.

## Verificación
- **Compilación:** Exitosa mediante `gradle assembleDebug`.
- **Despliegue:** Realizado en el dispositivo `ZY22MKBD44`.
- **Backend:** Conexión con Neon verificada y funcional para categorías y creación de sitios.

## Próximos Pasos
- [ ] Validar el funcionamiento de la API Key de Google Maps en el dispositivo del usuario.
- [ ] Implementar la persistencia real para la tabla `user_collections` en Neon.
- [ ] Refinar las animaciones de transición entre los 4 pasos del registro.
