package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Radiant Gold and Silver light/dark representations to ensure 100% black/gold/silver consistency
private val LightColorScheme = lightColorScheme(
    primary = GeoAccentPurple,
    secondary = GeoSelectedBg,
    tertiary = GeoHeroBg,
    background = GeoBg,
    surface = GeoCardBg,
    onPrimary = RealDark,
    onSecondary = GeoHeroText,
    onTertiary = GeoHeroText,
    onBackground = GeoTextPrimary,
    onSurface = GeoTextPrimary,
    outline = GeoBorder,
    error = ErrorRed
)

// Define local helper for ColorStateDark
private val ColorStateDark = androidx.compose.ui.graphics.Color(0xFF0F0F0F)

// A sleek dark mode alternative using golden accents and silver labels on absolute black background
private val DarkColorScheme = darkColorScheme(
    primary = GeoSelectedBg,
    secondary = GeoAccentPurple,
    tertiary = GeoHeroBg,
    background = RealDark,
    surface = ColorStateDark,
    onPrimary = GeoHeroText,
    onSecondary = PureWhite,
    onTertiary = GeoHeroText,
    onBackground = PureWhite,
    onSurface = PureWhite,
    outline = GeoBorder,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to TRUE to showcase Gold, Silver, and Absolute Black theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

