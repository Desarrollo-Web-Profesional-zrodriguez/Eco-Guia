# Eco-Guía Dolores: Migración a Groq IA y RAG Contextual

Debido a los problemas de cuota persistentes con Gemini, migraremos el cerebro de Miguel Hidalgo a **Groq IA**. Además, implementaremos una técnica de **RAG (Generación Aumentada por Recuperación)** para que la IA conozca todos los sitios históricos registrados en tiempo real.

## Proposed Changes

### Infraestructura de IA (Shared & Mobile)

#### [NEW] [GroqModels.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/data/remote/GroqModels.kt)
- Definir las clases de datos para la API de Groq (OpenAI compatible): `ChatRequest`, `Message`, `ChatResponse`, etc.

#### [NEW] [GroqClient.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/shared/src/main/java/mx/utng/ecoguia/shared/data/remote/GroqClient.kt)
- Implementar el cliente HTTP usando Ktor para realizar peticiones a `api.groq.com`.
- Usar el modelo `llama-3.3-70b-versatile` por su alta capacidad de razonamiento histórico.

#### [MODIFY] [ChatViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/viewmodel/ChatViewModel.kt)
- Reemplazar el SDK de Gemini por `GroqClient`.
- **Implementar Contexto RAG:** Al inicializar, el ViewModel descargará todos los sitios históricos desde Neon.
- **System Prompt Dinámico:** Generar un JSON/Texto con la descripción de los sitios y pasarlo como contexto para que Miguel Hidalgo pueda responder preguntas específicas sobre los lugares que el usuario ha registrado.

### Estabilidad de Navegación

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
- Asegurar que el `ChatViewModel` se mantenga vivo durante la navegación para no perder el contexto descargado de los sitios.

## User Review Required

> [!NOTE]
> **API Key de Groq:** Se utilizará la clave proporcionada: `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`.
>
> Groq es notablemente más rápido y no tiene las restricciones de "créditos prepagados" que nos están bloqueando en Google.

## Verification Plan

### Manual Verification
1. Abrir el chat.
2. Miguel Hidalgo debería decir: "Cargando memorias de la patria..." (mientras descarga los sitios de la DB).
3. Preguntar sobre un sitio específico que acabes de registrar (ej. "¿Qué puedes decirme de la Parroquia?").
4. Verificar que la respuesta sea instantánea y precisa gracias a Groq y al contexto inyectado.
