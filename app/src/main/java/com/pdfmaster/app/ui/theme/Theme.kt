package com.pdfmaster.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A1519),
    onPrimaryContainer = Color(0xFFFFB3B8),
    secondary = AccentOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1B0F),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = AccentBlue,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = SurfaceElevatedDark,
    outline = BorderDark,
    outlineVariant = BorderHighlightDark,
    error = Color(0xFFFF5252)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEAEC),
    onPrimaryContainer = Color(0xFF8F0018),
    secondary = AccentOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0E6),
    onSecondaryContainer = Color(0xFF7A3600),
    tertiary = AccentBlue,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = SurfaceElevatedLight,
    outline = BorderLight,
    outlineVariant = BorderHighlightLight,
    error = Color(0xFFD32F2F)
)

@Composable
fun PDFMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
