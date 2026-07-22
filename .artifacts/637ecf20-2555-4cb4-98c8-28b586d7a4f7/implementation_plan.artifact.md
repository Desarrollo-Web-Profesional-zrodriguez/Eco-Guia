# Plan de Implementación: Pantallas de Miguel Hidalgo IA (Mobile)

Este plan detalla el modelado de las interfaces para el Chatbot de Miguel Hidalgo IA y su Base de Conocimiento, siguiendo los flujos y el estilo visual de las imágenes proporcionadas.

## Análisis de Diseño

1.  **Miguel Hidalgo Chat:**
    *   Interfaz de conversación con burbujas de mensaje.
    *   Los mensajes del usuario utilizan el degradado `JadeGradient`.
    *   Incluye una tarjeta de perfil de la IA en la parte superior.
    *   Botón de acceso a la base de conocimiento en la cabecera.
2.  **Base de Conocimiento IA:**
    *   Pantalla informativa que detalla la "Base de IA".
    *   Muestra preguntas frecuentes, respuestas guiadas y el tono de la IA.
    *   Botón inferior de acción "Entrenar base local".

## Cambios Propuestos

### 1. Pantallas (Mobile Screens)

#### [NEW] [MiguelHidalgoChatScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/MiguelHidalgoChatScreen.kt)
*   Implementación de la conversación guiada.
*   Tarjeta de presentación de la IA.
*   Entrada de texto estilizada.

#### [NEW] [IAKnowledgeBaseScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/IAKnowledgeBaseScreen.kt)
*   Lista de entradas de conocimiento (Pregunta/Respuesta).
*   Sección de tono de voz de la IA.
*   Botón de entrenamiento.

### 2. Navegación y MainActivity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/MainActivity.kt)
*   Registrar las nuevas rutas: `chat_ia_hidalgo` y `ia_knowledge_base`.
*   Actualizar los enlaces desde el menú de opciones.

#### [DELETE] [ChatIAScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/ChatIAScreen.kt)
*   Eliminar el archivo anterior de placeholder.

### 3. Documentación

*   Cada archivo incluirá el estándar: **Autor: ZahirMora | Fecha: 2026-07-22**.

## Plan de Verificación

1.  **Navegación:** Probar el flujo desde Menú de Opciones -> Miguel Hidalgo IA -> Botón IA -> Base de Conocimiento.
2.  **Consistencia Visual:** Verificar que los degradados en las burbujas de chat correspondan a la identidad de marca (Jade/Oro).
3.  **Fidelidad de Texto:** Asegurar que los placeholders y textos descriptivos coincidan con las capturas.

---

> [!NOTE]
> Autor: ZahirMora | Fecha: 2026-07-22
