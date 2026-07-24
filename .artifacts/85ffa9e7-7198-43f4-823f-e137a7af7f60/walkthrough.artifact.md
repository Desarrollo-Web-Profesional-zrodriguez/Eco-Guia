# Admin Screens Mockup

I have implemented the complete set of admin screens as requested, following the visual style from the provided images.

## Key Components

### [AdminNavigation.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/components/AdminNavigation.kt)
A specialized `AdminBottomBar` component that provides quick access to:
- **Resumen**: Dashboard overview.
- **Moderar**: Community content queue.
- **Galería**: Capsule management.
- **Seguridad**: Security reports.

## Screens Implemented

### [AdminSummaryScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/AdminSummaryScreen.kt)
The central hub for administrators.
- **Header**: Clean deep blue header with profile access.
- **Activity Card**: A prominent Jade gradient card showing today's stats (Users, Geo-Drops, Reports).
- **Quick Actions**: Easy access to other admin modules with status indicators.

### [ModerateCommunityScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ModerateCommunityScreen.kt)
A list of community submissions waiting for review. Each item shows the author and content type with an "Aprobar" or "Ver" action.

### [CapsuleGalleryScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/CapsuleGalleryScreen.kt)
A grid-based view to browse and manage existing capsules. Includes status badges and filtering options.

### [ReviewDetailScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ReviewDetailScreen.kt)
A detailed moderation view for individual submissions, including checks for author verification, image quality, and location accuracy.

### [SecurityReportsScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/SecurityReportsScreen.kt)
Focuses on community-reported issues, categorized by severity and type (e.g., "Contenido reportado", "Geo-Drop mal ubicado").

### [ReportDecisionScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/ReportDecisionScreen.kt)
The final step in the security flow where admins can see reporter details and decide on the appropriate action (Delete, Move, Approve).

### [GalleryAdditionScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/EcoGuiaWear/mobile/src/main/java/mx/utng/ecoguiawear/ui/screens/admin/GalleryAdditionScreen.kt)
Updated the existing gallery upload screen to match the new design system, including professional fields like "TEXTO ALTERNATIVO" and "ETIQUETAS".

## Verification
- All screens use `EcoGuiaColors` and `EcoGuiaMobileTheme`.
- Consistent card design with `RoundedCornerShape(24.dp)`.
- Integration with the new `AdminBottomBar`.
- Code compiles successfully.
