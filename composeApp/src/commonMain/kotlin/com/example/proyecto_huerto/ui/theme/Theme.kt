package com.example.proyecto_huerto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Paleta de colores inspirada en la naturaleza y huertos (Verdes)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),      // Verde bosque profundo
    secondary = Color(0xFF4CAF50),    // Verde medio natural
    tertiary = Color(0xFF8BC34A),     // Verde lima suave
    background = Color(0xFFF1F8E9),   // Fondo crema verdoso muy suave
    surface = Color(0xFFFFFFFF),      // Superficie blanca limpia
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1B5E20), // Texto oscuro verdoso
    onSurface = Color(0xFF1B5E20),
    outline = Color(0xFF81C784)       // Bordes verdes suaves
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),      // Verde pálido para lectura en oscuro
    secondary = Color(0xFFAED581),    // Verde oliva claro
    tertiary = Color(0xFFC5E1A5),     // Verde savia
    background = Color(0xFF1B2E1C),   // Fondo verde muy oscuro
    surface = Color(0xFF243B25),      // Superficie verde oscuro
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFDCEDC8),
    onSurface = Color(0xFFDCEDC8),
    outline = Color(0xFF4CAF50)
)

private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    )
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(24.dp) // Bordes muy redondeados para estilo amigable
)

@Composable
fun ProyectoHuertoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
