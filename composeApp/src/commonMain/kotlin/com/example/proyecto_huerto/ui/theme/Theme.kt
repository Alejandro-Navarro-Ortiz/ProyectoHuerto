package com.example.proyecto_huerto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GreenPrimary = Color(0xFF2D6A4F)
private val GreenSecondary = Color(0xFF52B788)
private val BackgroundLight = Color(0xFFF8FAF8)
private val DarkBackground = Color(0xFF1B1E1B) // Verde muy oscuro, no negro

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    primaryContainer = Color(0xFFD8F3DC),
    onPrimaryContainer = GreenPrimary,
    background = BackgroundLight,
    surface = Color.White,
    secondaryContainer = Color(0xFFB7E4C7)
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenSecondary,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = Color.White,
    background = DarkBackground,
    surface = Color(0xFF242824),
    secondaryContainer = Color(0xFF2D6A4F)
)

@Composable
fun ProyectoHuertoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes(medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp)),
        content = content
    )
}