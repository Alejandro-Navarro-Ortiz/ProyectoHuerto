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

// 1. Define tus paletas de colores para el modo claro y oscuro.
// Puedes personalizar estos colores usando un generador online como: https://m3.material.io/theme-builder

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),      // Verde principal
    secondary = Color(0xFF8BC34A),    // Verde más claro
    tertiary = Color(0xFFCDDC39),     // Lima
    background = Color(0xFFF5F5F5),   // Fondo gris claro
    surface = Color(0xFFFFFFFF),      // Superficie blanca (para Cards, etc.)
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BC34A),      // Verde claro para modo oscuro
    secondary = Color(0xFFCDDC39),    // Lima
    tertiary = Color(0xFFAED581),     // Verde pálido
    background = Color(0xFF121212),   // Fondo oscuro estándar
    surface = Color(0xFF1E1E1E),      // Superficie ligeramente más clara
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

// 2. Define la tipografía (opcional, puedes usar la de por defecto)
private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Puedes definir más estilos aquí: titleLarge, labelSmall, etc. */
)

// 3. Define las formas de los componentes (opcional)
private val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// 4. Esta es la función principal que usarás en tu aplicación
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
