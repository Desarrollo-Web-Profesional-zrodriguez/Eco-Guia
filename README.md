<div align="center">
  <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-radar.png" width="90" height="90" alt="EcoGuia Logo">
  <h1>🌿 Eco-Guía</h1>
  <p><strong>Plataforma Multiplataforma para Turismo Cultural, Geolocalización e Inteligencia Artificial</strong></p>
  <p><i>Dolores Hidalgo Cuna de la Independencia Nacional — Guanajuato, México</i></p>

  <br>

  <div>
    <img src="https://img.shields.io/badge/Kotlin-v2.1.0-blue?style=for-the-badge&logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/Android-Phone_/_Wear_OS_/_TV-green?style=for-the-badge&logo=android" alt="Android">
    <img src="https://img.shields.io/badge/Database-PostgreSQL_/_PostGIS-orange?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
    <img src="https://img.shields.io/badge/AI-Groq_Llama_3.3_70B-purple?style=for-the-badge&logo=openai" alt="Groq AI">
    <img src="https://img.shields.io/badge/Messaging-MQTT_RealTime-red?style=for-the-badge&logo=eclipseche" alt="MQTT">
  </div>
</div>

<br>
<hr>

<h2>👥 Información del Proyecto</h2>
<table>
  <tr>
    <td><b>Proyecto:</b></td>
    <td>Eco-Guía Multiplataforma</td>
  </tr>
  <tr>
    <td><b>Estudiantes:</b></td>
    <td>Zahir Andrés Rodríguez Mora & Cesar Enrique Garay García</td>
  </tr>
  <tr>
    <td><b>Grupo:</b></td>
    <td>GIDS6092</td>
  </tr>
  <tr>
    <td><b>Institución:</b></td>
    <td>Universidad Tecnológica del Norte de Guanajuato (UTNG)</td>
  </tr>
</table>

<br>

<h2>🎯 Objetivo</h2>
<p>
Desarrollar una solución tecnológica integral y distribuida (<strong>Android Móvil</strong>, <strong>Wear OS</strong> y <strong>Smart TV 360°</strong>) que fomente la exploración del patrimonio histórico de Dolores Hidalgo, Guanajuato. La plataforma permite descubrir puntos de interés mediante un radar GPS con brújula háptica, capturar cápsulas geolocalizadas (<strong>Geo-Drops</strong>), interactuar con un bot conversacional histórico impulsado por <strong>Miguel Hidalgo IA (Groq)</strong>, e interconectar pantallas en tiempo real mediante <strong>MQTT</strong> y <strong>PostgreSQL/PostGIS (Neon DB)</strong>.
</p>

<br>

<h2>🛠️ Arquitectura y Tecnologías</h2>

<table>
  <thead>
    <tr>
      <th>Capa</th>
      <th>Tecnología / Librería</th>
      <th>Descripción</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>Móvil (Android Phone)</b></td>
      <td>Jetpack Compose, Material 3, Google Maps, CameraX</td>
      <td>Panel interactivo de exploración, registro de sitios, cámara GeoDrop, Mi Colección y moderación.</td>
    </tr>
    <tr>
      <td><b>Wearable (Wear OS)</b></td>
      <td>Horologist, Wear Compose Foundation, Hardware Sensors</td>
      <td>Radar de proximidad en metros, brújula dinámica con rotación de azimut y vibración háptica.</td>
    </tr>
    <tr>
      <td><b>Smart TV (Android TV)</b></td>
      <td>Compose for TV, Google Maps 3D Tilt, Key Trapping</td>
      <td>Lobby de exhibición, transmisión en vivo de mapa 360° con rotación automática y selector de maquetas.</td>
    </tr>
    <tr>
      <td><b>Shared Module</b></td>
      <td>Kotlin Multiplatform Layout, Room, Ktor Client</td>
      <td>Modelo de datos unificado, repositorio espacial y cliente de conexión a Neon PostgreSQL.</td>
    </tr>
    <tr>
      <td><b>Base de Datos Remota</b></td>
      <td>Neon PostgreSQL + Extensiones PostGIS, Pgcrypto, Citext</td>
      <td>Persistencia relacional y espacial (`ST_DWithin`, `ST_MakePoint`, `ST_AsText`).</td>
    </tr>
    <tr>
      <td><b>Inteligencia Artificial</b></td>
      <td>Groq Cloud API (`llama-3.3-70b-versatile`)</td>
      <td>Simulación conversacional en primera persona de Miguel Hidalgo y Costilla con contexto histórico.</td>
    </tr>
    <tr>
      <td><b>Sincronización Real-Time</b></td>
      <td>Protocolo MQTT (`Eclipse Paho Broker`)</td>
      <td>Emisión y recepción de eventos instantáneos entre el teléfono móvil y la transmisión Smart TV.</td>
    </tr>
    <tr>
      <td><b>Correos Transaccionales</b></td>
      <td>Brevo REST API v3 (Sendinblue)</td>
      <td>Envío de códigos OTP de recuperación de contraseña con plantilla institucional Eco-Guía.</td>
    </tr>
  </tbody>
</table>

<br>

<h2>✨ Funcionalidades Principales por Módulo</h2>

<h3>📱 1. Aplicación Móvil (Android)</h3>
<ul>
  <li><b>Exploración & Mapa Interactivo:</b> Visualización de sitios históricos de Dolores Hidalgo con marcadores de radar y distancia dinámica.</li>
  <li><b>Captura de Geo-Drops:</b> Creación de cápsulas fotográficas o de texto geolocalizadas con cálculo automático de coordenadas GPS.</li>
  <li><b>Mi Colección & Nivel de Explorador:</b> Guardado personal de sitios y rutas con conteo de autoría de cápsulas y asignación dinámica de rangos (<i>Turista Reciente</i> ➔ <i>Guardián del Patrimonio</i>).</li>
  <li><b>Flujo de Alta de Sitios Históricos:</b> Proceso guiado en 4 pasos con conservación inteligente de estado en inputs y persistencia en Neon PostgreSQL.</li>
  <li><b>Miguel Hidalgo IA:</b> Chat interactivo con respuestas históricas contextuadas y base de conocimiento.</li>
  <li><b>Moderación de Comunidad (2 Columnas en Landscape):</b> Panel de revisión para administradores y moderadores con aprobación/rechazo en tiempo real.</li>
  <li><b>Recuperación de Cuenta por OTP:</b> Autenticación y recuperación de clave mediante códigos de 6 dígitos enviados por correo electrónico con Brevo.</li>
</ul>

<h3>⌚ 2. Aplicación Wear OS (Reloj Inteligente)</h3>
<ul>
  <li><b>Brújula Dinámica de Azimut:</b> Indicador visual que rota en tiempo real hacia la ubicación del sitio histórico seleccionado.</li>
  <li><b>Radar de Proximidad Háptico:</b> Distancia calculada en metros con pulsaciones hápticas al aproximarse a un punto de interés.</li>
  <li><b>Vinculación Rápida:</b> Sincronización transparente con el teléfono usando la Data Layer API de Google Play Services Wearable.</li>
</ul>

<h3>📺 3. Aplicación Smart TV (Android TV)</h3>
<ul>
  <li><b>Portal 360° de Exhibición:</b> Transmisión interactiva en pantalla grande centrada en el Jardín Principal de Dolores Hidalgo.</li>
  <li><b>Cámara Giratoria 3D:</b> Mapa inclinado a 45° que rota automáticamente a 360° alrededor del punto de interés.</li>
  <li><b>Selector de Estilos de Mapa:</b> Diálogo modal aislado con soporte de control remoto (Maqueta Blanca, Neón Oscuro y Satelital).</li>
  <li><b>Modo Kiosco de Transmisión:</b> Bloqueo por PIN de seguridad para evitar interrupciones no autorizadas en exhibiciones públicas.</li>
</ul>

<br>

<h2>👥 Roles del Sistema</h2>

<table>
  <thead>
    <tr>
      <th>Rol</th>
      <th>Descripción y Permisos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>👑 Super Admin</b></td>
      <td>Acceso total al sistema, gestión de usuarios/roles, control de Smart TV, alta de sitios y panel de moderación.</td>
    </tr>
    <tr>
      <td><b>🛡️ Moderador</b></td>
      <td>Alta de Sitios Históricos, creación de rutas turísticas y revisión/aprobación de cápsulas GeoDrops en el Panel de Moderación.</td>
    </tr>
    <tr>
      <td><b>🏨 Museo / Hotel</b></td>
      <td>Gestión y vinculación de sitios pertenecientes a su establecimiento cultural o turístico.</td>
    </tr>
    <tr>
      <td><b>🌿 Visitante / Turista</b></td>
      <td>Exploración de mapa, captura de GeoDrops, brújula en reloj, consulta de rutas y chat con Miguel Hidalgo IA.</td>
    </tr>
  </tbody>
</table>

<br>

<h2>🗄️ Esquema Oficial de Base de Datos (Neon PostgreSQL)</h2>
<p>La base de datos de producción está estructurada en <strong>10 componentes activos</strong>:</p>
<ol>
  <li><code>users</code>: Registro de usuarios, credenciales hashes, roles y perfil.</li>
  <li><code>historical_sites</code>: Puntos históricos con coordenadas PostGIS (`GEOGRAPHY(Point, 4326)`).</li>
  <li><code>geo_drops</code>: Cápsulas comunitarias con fotos, texto y coordenadas espaciales.</li>
  <li><code>user_saved_items</code>: Relación de ítems y sitios guardados en Mi Colección.</li>
  <li><code>routes</code>: Rutas turísticas creadas por gestores culturales.</li>
  <li><code>route_stops</code>: Secuencia y paradas ordenadas pertenecientes a cada ruta.</li>
  <li><code>site_categories</code>: Diccionario de categorías de sitios (Museo, Monumento, Plaza, etc.).</li>
  <li><code>devices</code>: Registro de teléfonos, relojes y TVs conectados.</li>
  <li><code>device_pairings</code>: Códigos de vinculación activa entre dispositivos.</li>
  <li><code>approved_geo_drops</code> (Vista): Vista optimizada de cápsulas aprobadas para mapas y TV.</li>
</ol>

<br>

<h2>🚀 Instrucciones de Compilación y Ejecución</h2>

<ol>
  <li>
    <b>Clonar el Repositorio:</b>
    <pre><code>git clone https://github.com/Desarrollo-Web-Profesional-zrodriguez/Eco-Guia.git
cd Eco-Guia</code></pre>
  </li>
  <li>
    <b>Configurar Variables de Entorno (<code>.env</code> o <code>local.properties</code>):</b>
    <pre><code>GROQ_API_KEY=tu_api_key_de_groq
BREVO_API_KEY=tu_api_key_de_brevo
NEON_DATABASE_URL=postgresql://tu_usuario:tu_password@tu_host.neon.tech/neondb?sslmode=require</code></pre>
  </li>
  <li>
    <b>Ejecutar los Módulos en Gradle:</b>
    <ul>
      <li>Móvil: <code>./gradlew :mobile:assembleDebug</code></li>
      <li>Smart TV: <code>./gradlew :tv:assembleDebug</code></li>
      <li>Wear OS: <code>./gradlew :wear:assembleDebug</code></li>
    </ul>
  </li>
</ol>

<br>
<hr>

<div align="center">
  <p><b>Eco-Guía</b> — <i>Impulsando el Turismo Cultural con Tecnología de Vanguardia.</i></p>
</div>
