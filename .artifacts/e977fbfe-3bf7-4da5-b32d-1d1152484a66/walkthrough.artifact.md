# Walkthrough: Radar con Brújula (Heading Support)

Se ha implementado el soporte para la orientación física del reloj (Heading), permitiendo que la flecha del radar rote dinámicamente cuando el usuario gira sobre su propio eje.

## Cambios Realizados

### Inteligencia Sensorial (Wear OS)
- **SensorHelper:** Nuevo componente que utiliza el **Magnetómetro** y el **Acelerómetro** del reloj para calcular el *Azimuth* (hacia dónde apunta físicamente el reloj respecto al Norte).
- **Cálculo de Orientación Relativa:**
    - Antes: La flecha solo mostraba la dirección absoluta hacia el objetivo.
    - Ahora: Se calcula la diferencia entre el rumbo al objetivo (GPS) y la orientación actual del reloj (Sensores).
    - **Resultado:** Si el objetivo está al Norte y tú giras hacia el Sur, la flecha girará 180 grados en la pantalla para seguir apuntando físicamente al lugar correcto.

### Interfaz de Usuario
- **Actualización en Tiempo Real:** El `RadarViewModel` ahora recibe actualizaciones constantes de los sensores, haciendo que el movimiento de la flecha sea fluido y reactivo a tus movimientos físicos.

## Verificación

### Prueba de Giro (180 Grados)
1. Abrir **EcoGuia** en el reloj y activar el radar hacia un objetivo.
2. Caminar hacia el frente (la flecha apunta hacia arriba).
3. Girar físicamente 180 grados sin moverse de lugar.
4. **Resultado:** La flecha ahora apuntará hacia abajo (atrás), indicando correctamente dónde quedó el sitio histórico tras tu giro.

 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/data/wear/SensorHelper.kt)
 render_diffs(file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/wear/src/main/java/mx/utng/ecoguiawear/presentation/screens/RadarScreen.kt)
