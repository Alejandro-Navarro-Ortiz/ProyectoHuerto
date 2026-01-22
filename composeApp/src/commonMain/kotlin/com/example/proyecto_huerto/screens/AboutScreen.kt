package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de la Aplicación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                GuideSection(
                    title = "¡Bienvenido a Proyecto Huerto!",
                    content = "Esta aplicación está diseñada para ser tu compañera ideal en la gestión de tu huerto ecológico y sostenible. Aquí encontrarás todo lo que necesitas para planificar, registrar y aprender."
                )
            }
            item {
                GuideSection(
                    title = "Mis Bancales",
                    content = "Es el corazón de tu huerto. Aquí puedes crear y personalizar tus bancales (las zonas de cultivo). Define sus dimensiones y añade los cultivos que vas a plantar en cada uno. Visualiza tu huerto de forma gráfica y organizada."
                )
            }
            item {
                GuideSection(
                    title = "Diario de Cultivo",
                    content = "Tu bitácora personal. Cada vez que realices una acción importante como regar, sembrar o cosechar en uno de tus bancales, el evento quedará registrado automáticamente en esta sección, con la fecha y hora exactas. ¡Así no perderás detalle del progreso de tus cultivos!"
                )
            }
            item {
                GuideSection(
                    title = "Guía de Plagas",
                    content = "Una sección informativa para ayudarte a identificar y combatir las plagas más comunes de forma ecológica. Aprende a reconocer los síntomas y a aplicar tratamientos orgánicos para mantener tu huerto sano."
                )
            }
            item {
                GuideSection(
                    title = "Consejos",
                    content = "Aquí encontrarás recomendaciones y buenas prácticas para que saques el máximo partido a tu huerto. Desde técnicas de siembra hasta cómo mejorar la calidad de tu suelo."
                )
            }
        }
    }
}

@Composable
private fun GuideSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify
        )
    }
}
