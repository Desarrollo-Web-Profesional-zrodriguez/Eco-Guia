package mx.utng.ecoguiawear.tv.ui.theme

import com.google.android.gms.maps.model.MapStyleOptions

object MapStyles {
    val minimalWhite3DStyle: MapStyleOptions by lazy {
        MapStyleOptions("""
            [
              { "elementType": "geometry", "stylers": [ { "color": "#f5f5f5" } ] },
              { "elementType": "labels.icon", "stylers": [ { "visibility": "on" } ] },
              { "elementType": "labels.text.fill", "stylers": [ { "color": "#616161" } ] },
              { "elementType": "labels.text.stroke", "stylers": [ { "color": "#ffffff" } ] },
              { "featureType": "landscape.man_made", "elementType": "geometry.fill", "stylers": [ { "color": "#ffffff" } ] },
              { "featureType": "landscape.man_made", "elementType": "geometry.stroke", "stylers": [ { "color": "#e0e0e0" }, { "weight": 1 } ] },
              { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#ffffff" } ] },
              { "featureType": "road", "elementType": "geometry.stroke", "stylers": [ { "color": "#d6d6d6" } ] }
            ]
        """.trimIndent())
    }

    val darkModeStyle: MapStyleOptions by lazy {
        MapStyleOptions("""
            [
              { "elementType": "geometry", "stylers": [ { "color": "#182232" } ] },
              { "elementType": "labels.icon", "stylers": [ { "visibility": "on" } ] },
              { "elementType": "labels.text.fill", "stylers": [ { "color": "#6a9d8a" } ] },
              { "elementType": "labels.text.stroke", "stylers": [ { "color": "#1a3644" } ] },
              { "featureType": "landscape.man_made", "elementType": "geometry.fill", "stylers": [ { "color": "#0d1b2a" } ] },
              { "featureType": "landscape.man_made", "elementType": "geometry.stroke", "stylers": [ { "color": "#0f5a3e" } ] },
              { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#1f3a5f" } ] },
              { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#0e1626" } ] }
            ]
        """.trimIndent())
    }
}
