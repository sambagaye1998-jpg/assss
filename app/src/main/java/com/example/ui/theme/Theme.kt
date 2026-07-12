package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EcomLightColorScheme = lightColorScheme(
    primary = EcomPrimary,
    secondary = EcomAccent,
    tertiary = StatSuccess,
    background = EcomDarkBg,
    surface = EcomSlateCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = EcomTextPrimary,
    onSurface = EcomTextPrimary,
    outline = EcomBorder
)

// Force EcomPilotTheme default for EcomPilot to keep that professional Clean light visual style
@Composable
fun EcomPilotTheme(
    darkTheme: Boolean = false, // Force false to keep light theme matching the Clean Minimal theme
    content: @Composable () -> Unit
) {
    val colorScheme = EcomLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep a backward compatibility wrapper name to avoid compilation breaking
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    EcomPilotTheme(content = content)
}
