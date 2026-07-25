# Eco-Guía Dolores: Actualización de Cerebro IA (Migración a Gemini 2.0)

El error 404 indica que el modelo `gemini-1.5-flash` ha sido retirado (estamos en julio de 2026). Para que Miguel Hidalgo vuelva a responder, debemos migrar a la nueva generación de modelos.

## Proposed Changes

### Chat IA (Mobile)

#### [MODIFY] [ChatViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/ChatViewModel.kt)
- Cambiar el nombre del modelo de `gemini-1.5-flash` a `gemini-2.0-flash` (el estándar actual en 2026).
- Mejorar el manejo de excepciones para que, en caso de error de red o de API Key, el mensaje sea más claro para el usuario.

## User Review Required

> [!CAUTION]
> **API Key sospechosa:** La clave que proporcionaste (`AQ.Ab8...`) no coincide con el formato tradicional de Google AI Studio (`AIzaSy...`).
>
> Si el cambio a **Gemini 2.0 Flash** no soluciona el problema, por favor genera una nueva clave en [Google AI Studio](https://aistudio.google.com/) y asegúrate de que empiece con `AIza`.

## Verification Plan

### Manual Verification
1. Abrir el chat de Miguel Hidalgo.
2. Enviar un mensaje.
3. El Padre de la Patria debería responder ahora que estamos usando un modelo activo.
