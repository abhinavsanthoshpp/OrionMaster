package com.pdfmaster.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ===================== LUXURY PALETTE =====================

// Brand Primary Gradient (Electric Crimson -> Sunset Coral)
val PrimaryRed = Color(0xFFFF334B)
val PrimaryRedDark = Color(0xFFD61834)
val PrimaryRedLight = Color(0xFFFF697C)
val PrimaryCoral = Color(0xFFFF5E3A)

val BrandGradient = Brush.horizontalGradient(listOf(PrimaryRed, PrimaryCoral))
val BrandGradientVertical = Brush.verticalGradient(listOf(PrimaryRed, PrimaryCoral))

// Accent Highlights
val AccentOrange = Color(0xFFFF9500)
val AccentGreen = Color(0xFF00C853)
val AccentEmerald = Color(0xFF10B981)
val AccentBlue = Color(0xFF007AFF)
val AccentPurple = Color(0xFFAF52DE)
val AccentTeal = Color(0xFF00C7BE)
val AccentAmber = Color(0xFFFFD60A)

// Surfaces - Dark Theme (Deep Obsidian / Starlight Glass)
val BackgroundDark = Color(0xFF090B10)
val SurfaceDark = Color(0xFF131722)
val SurfaceElevatedDark = Color(0xFF1A202E)
val SurfaceVariantDark = Color(0xFF22293A)
val BorderDark = Color(0xFF262E42)
val BorderHighlightDark = Color(0xFF38435E)

// Surfaces - Light Theme (Pure Alabaster / Crisp Porcelain)
val BackgroundLight = Color(0xFFF6F8FC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEDF2F7)
val BorderLight = Color(0xFFE2E8F0)
val BorderHighlightLight = Color(0xFFCBD5E1)

// Typography Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextTertiaryDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextTertiaryLight = Color(0xFF94A3B8)

// Category Colors
val ColorOrganize = Color(0xFFFF334B)
val ColorOptimize = Color(0xFF00C853)
val ColorConvert = Color(0xFF007AFF)
val ColorEdit = Color(0xFFAF52DE)
val ColorSecurity = Color(0xFFFF9500)

// Tool Colors (Matched to App)
val MergePDFColor = Color(0xFFFF334B)
val SplitPDFColor = Color(0xFFFF5E3A)
val CompressPDFColor = Color(0xFF00C853)
val RotatePDFColor = Color(0xFFFF6442)
val JPGtoPDFColor = Color(0xFF007AFF)
val PDFtoJPGColor = Color(0xFF00C7BE)
val WatermarkColor = Color(0xFFAF52DE)
val ProtectColor = Color(0xFFFF9500)
val UnlockColor = Color(0xFFFFD60A)
val SignColor = Color(0xFF5856D6)
