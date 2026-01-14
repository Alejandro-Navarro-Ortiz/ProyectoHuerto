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

private val VerdePrincipal = Color(0xFF2C5E2C) // Un verde más oscuro y sereno
private val VerdeSecundario = Color(0xFF5A8E5A)
private val MarronTierra = Color(0xFF8B4513)
private val AmarilloSol = Color(0xFFFFFACD)
private val NegroTransparente = Color(0x99000000) // Negro con transparencia para superposiciones

private val LightColorScheme = lightColorScheme(
    primary = VerdeSecundario,
    onPrimary = Color.White,
    primaryContainer = VerdePrincipal,
    onPrimaryContainer = Color.White,
    secondary = MarronTierra,
    onSecondary = Color.White,
    tertiary = Color(0xFFC2B280),
    background = VerdePrincipal, // Fondo verde oscuro
    onBackground = Color.White,
    surface = AmarilloSol, // Superficies como tarjetas en un tono claro
    onSurface = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeSecundario,
    onPrimary = Color.Black,
    primaryContainer = VerdePrincipal,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFC2B280), 
    tertiary = Color(0xFF4682B4), 
    background = Color(0xFF121212), // Fondo negro para el modo oscuro
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF2E2E2E),
    onSurface = Color(0xFFE6E1E5),
)

private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
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
