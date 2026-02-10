package com.example.finvovo.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryViolet,
    secondary = Teal200,
    tertiary = LightViolet,
    background = DarkText,
    surface = DarkText,
    onPrimary = WhiteSurface,
    onSecondary = DarkText,
    onTertiary = DarkText,
    onBackground = WhiteSurface,
    onSurface = WhiteSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryViolet,
    secondary = Teal200,
    tertiary = DarkViolet,
    background = OffWhiteBackground,
    surface = WhiteSurface,
    onPrimary = WhiteSurface,
    onSecondary = DarkText,
    onTertiary = WhiteSurface,
    onBackground = DarkText,
    onSurface = DarkText,
)

@Composable
fun FinvovoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to enforce our branding
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Icons are always white on Primary background
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
