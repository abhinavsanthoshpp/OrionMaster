package com.pdfmaster.app.ui

sealed class Screen(val route: String) {
    // Bottom Nav
    object Home : Screen("home")
    object Recent : Screen("recent")
    object Settings : Screen("settings")

    // Tool Screens
    object MergePDF : Screen("merge")
    object SplitPDF : Screen("split")
    object CompressPDF : Screen("compress")
    object RotatePDF : Screen("rotate")
    object RemovePages : Screen("remove_pages")
    object ExtractPages : Screen("extract_pages")
    object OrganizePDF : Screen("organize")
    object RepairPDF : Screen("repair")
    object OCRPDF : Screen("ocr")
    object JPGToPDF : Screen("jpg_to_pdf")
    object PDFToJPG : Screen("pdf_to_jpg")
    object AddWatermark : Screen("add_watermark")
    object AddPageNumbers : Screen("add_page_numbers")
    object CropPDF : Screen("crop")
    object EditPDF : Screen("edit")
    object ProtectPDF : Screen("protect")
    object UnlockPDF : Screen("unlock")
    object SignPDF : Screen("sign")
    object ComparePDF : Screen("compare")

    // Viewer
    object PDFViewer : Screen("pdf_viewer/{uri}") {
        fun createRoute(uri: String) = "pdf_viewer/$uri"
    }
}

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val iconName: String
) {
    object Home : BottomNavItem(Screen.Home, "Home", "home")
    object Recent : BottomNavItem(Screen.Recent, "Recent", "history")
    object Settings : BottomNavItem(Screen.Settings, "Settings", "settings")

    companion object {
        val items = listOf(Home, Recent, Settings)
    }
}
