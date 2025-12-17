package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal

// Es necesario añadir esta anotación porque TopAppBar se considera experimental en Material 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionBancalesScreen(bancales: List<Bancal>, onAddBancal: () -> Unit, onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Bancales") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                // Opcional: para darle color a la barra superior
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBancal) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Bancal")
            }
        },
        bottomBar = {
            // Se reemplaza BottomNavigation por NavigationBar de Material 3
            NavigationBar {
                // Se reemplaza BottomNavigationItem por NavigationBarItem
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { onNavigate("Inicio") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Bancales") },
                    label = { Text("Bancales") },
                    selected = true,
                    onClick = { onNavigate("Bancales") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { onNavigate("Perfil") }
                )
            }
        }
    ) { paddingValues -> // El padding ahora es PaddingValues
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues) // Usar los paddingValues del Scaffold
                .fillMaxSize()
        ) {
            items(bancales) { bancal ->
                BancalGridItem(bancal = bancal)
            }
        }
    }
}

@Composable
fun BancalGridItem(bancal: Bancal) {
    // Usamos el Card de Material 3
    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Nueva forma de definir la elevación
    ) {
        // Usamos el Text de Material 3 y estilos de tipografía actualizados
        Text(
            text = bancal.nombre,
            style = MaterialTheme.typography.titleMedium, // Estilo actualizado de Material 3
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface // Usar colores del tema es una buena práctica
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GestionBancalesScreenPreview() {
    val bancalesDeEjemplo = listOf(
        Bancal("1", "Bancal Principal", emptyList(), ""),
        Bancal("2", "Jardineras", emptyList(), ""),
        Bancal("3", "Huerto Urbano", emptyList(), ""),
        Bancal("4", "Semilleros", emptyList(), ""),
        Bancal("5", "Aromáticas", emptyList(), ""),
        Bancal("6", "Tomateras", emptyList(), ""),
    )
    // Para que la preview funcione, necesitas un Theme de Material 3 envolviéndola.
    // Asumiendo que tienes uno llamado AppTheme en tu proyecto:
    // TuProyectoTheme {
    GestionBancalesScreen(bancales = bancalesDeEjemplo, onAddBancal = {}, onNavigate = {})
    // }
}
