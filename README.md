# Eco-Guia Wear

Proyecto Wear OS inicial para **Eco-Guia Dolores: Ruta & Capsulas**.

Este MVP implementa la parte de reloj como radar cultural:

- Vinculacion simulada con telefono.
- Radar activo/pausado.
- Brujula hacia Geo-Drop o sitio cercano.
- Alerta de proximidad a menos de 20 metros.
- Pantalla de llegada.
- Resumen rapido de ruta.
- Ajustes hapticos.
- Cliente de mensajes preparado para sincronizacion con la app movil.

Arquitectura base:

- `data`: fuentes de datos, demo repository y comunicacion con telefono.
- `domain`: modelos y contratos de negocio.
- `presentation`: ViewModel, navegacion, pantallas y componentes Compose para Wear.

