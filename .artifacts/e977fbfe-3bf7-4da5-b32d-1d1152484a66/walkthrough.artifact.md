# Walkthrough: Migración de Cerebro IA a Gemini 2.0

Se ha actualizado el motor de inteligencia artificial de Miguel Hidalgo para resolver el error 404 causado por el retiro de los modelos antiguos.

## Cambios Realizados

### Chat IA (ChatViewModel)
- **Actualización de Modelo:** Se migró de `gemini-1.5-flash` a **`gemini-2.0-flash`**.
    - Dado que estamos en julio de 2026, los modelos 1.5 ya no están disponibles. El uso de la versión 2.0 asegura la continuidad del servicio.
- **Manejo de Errores Mejorado:**
    - Se añadieron mensajes específicos para errores de permisos (403) y modelo no encontrado (404), manteniendo siempre el tono histórico de Miguel Hidalgo.

## Notas Importantes
> [!CAUTION]
> Si después de esta actualización recibes un mensaje sobre la "llave de acceso", por favor verifica que tu API Key sea correcta en [Google AI Studio](https://aistudio.google.com/). Las claves válidas suelen empezar con `AIza`.

 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/ChatViewModel.kt)
