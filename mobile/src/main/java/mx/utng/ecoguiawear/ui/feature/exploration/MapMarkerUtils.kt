/**
 * Archivo: MapMarkerUtils.kt
 * Autores: ZahirAndres, CesarEnrique
 * Fecha de última actualización: 2026-07-26
 * Descripción: Funciones puras de utilidad para generar BitmapDescriptors personalizados
 * en Google Maps según la categoría del sitio histórico. Separado de ExplorationScreen
 * para facilitar su reutilización en otras pantallas (ActiveRouteScreen, SearchExperience, etc.).
 *
 * Funciones destacadas:
 * - getCustomCategoryMarkerIcon: Genera un marcador circular con emoji y color por categoría.
 * - getCategoryHue: Devuelve el tono estándar de Google Maps para cada categoría.
 */

package mx.utng.ecoguiawear.ui.feature.exploration

/**
 * Devuelve un [BitmapDescriptor] personalizado para Google Maps generando un marcador circular
 * con color distintivo y emoji según la categoría del sitio.
 *
 * @param context Contexto Android necesario para acceder a la densidad de pantalla.
 * @param siteType Cadena de texto con el tipo/categoría del sitio (e.g. "Museo", "Parque").
 * @return [BitmapDescriptor] listo para asignarse a un [Marker] de Google Maps.
 */
fun getCustomCategoryMarkerIcon(
    context: android.content.Context,
    siteType: String
): com.google.android.gms.maps.model.BitmapDescriptor {
    val type = siteType.lowercase()
    val (colorInt, emoji) = when {
        type.contains("museo") -> android.graphics.Color.parseColor("#0288D1") to "🏛️"
        type.contains("monumento") || type.contains("estatua") -> android.graphics.Color.parseColor("#F57C00") to "🗿"
        type.contains("parque") || type.contains("plaza") -> android.graphics.Color.parseColor("#388E3C") to "🌳"
        type.contains("iglesia") || type.contains("templo") || type.contains("religioso") -> android.graphics.Color.parseColor("#7B1FA2") to "⛪"
        type.contains("galería") || type.contains("galeria") -> android.graphics.Color.parseColor("#C2185B") to "🎨"
        type.contains("restaurante") -> android.graphics.Color.parseColor("#D32F2F") to "🍽️"
        type.contains("historico") || type.contains("histórico") -> android.graphics.Color.parseColor("#FBC02D") to "📜"
        else -> android.graphics.Color.parseColor("#00B4D8") to "📍"
    }

    val sizePx = (48 * context.resources.displayMetrics.density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val paint = android.graphics.Paint().apply { isAntiAlias = true }

    // Borde exterior blanco
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

    // Círculo interior de categoría
    paint.color = colorInt
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, (sizePx / 2f) - (3 * context.resources.displayMetrics.density), paint)

    // Dibujar Emoji centrado
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 22 * context.resources.displayMetrics.density
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(emoji, canvas.width / 2f, yPos, textPaint)

    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Devuelve un tono de color (Hue) estándar de Google Maps según la categoría del sitio.
 *
 * @param siteType Tipo de sitio (e.g. "Museo", "Parque").
 * @return Float representando el hue de [BitmapDescriptorFactory].
 */
fun getCategoryHue(siteType: String): Float {
    val type = siteType.lowercase()
    return when {
        type.contains("museo") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
        type.contains("monumento") || type.contains("estatua") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
        type.contains("parque") || type.contains("plaza") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
        type.contains("iglesia") || type.contains("templo") || type.contains("religioso") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET
        type.contains("historico") || type.contains("histórico") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW
        else -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
    }
}
