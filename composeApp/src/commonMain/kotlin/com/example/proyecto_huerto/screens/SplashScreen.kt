package com.example.proyecto_huerto.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.Res
import proyectohuerto.composeapp.generated.resources.app_name
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Yard

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        delay(2000) // Duración fija de 2 segundos
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                imageVector = Icons.Default.Yard,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp
            )
        }
    }
}
