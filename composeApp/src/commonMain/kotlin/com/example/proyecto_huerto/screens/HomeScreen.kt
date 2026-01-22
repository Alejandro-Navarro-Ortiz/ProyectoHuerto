package com.example.proyecto_huerto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class HomeOption(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(recentActivities: List<String>, onNavigate: (String) -> Unit) {
    val navOptions = listOf(
        HomeOption("Bancales", Icons.Filled.Yard, "gestion_bancales"),
        HomeOption("Diario", Icons.Filled.Book, "diario_cultivo"),
        HomeOption("Plagas", Icons.Filled.BugReport, "plagas"),
        HomeOption("Guía", Icons.Filled.LocalFlorist, "guia_hortalizas")
    )

    val cardOptions = listOf(
        HomeOption("Mis Bancales", Icons.Filled.Yard, "gestion_bancales"),
        HomeOption("Diario de Cultivo", Icons.Filled.Book, "diario_cultivo"),
        HomeOption("Plagas", Icons.Filled.BugReport, "plagas"),
        HomeOption("Consejos", Icons.Filled.Lightbulb, "consejos"),
        HomeOption("Guía de Hortalizas", Icons.Filled.LocalFlorist, "guia_hortalizas")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proyecto Huerto") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { onNavigate("profile") }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Mi Perfil",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navOptions.forEach { option ->
                    NavigationBarItem(
                        icon = { Icon(option.icon, contentDescription = option.title) },
                        label = { Text(option.title) },
                        selected = false,
                        onClick = { onNavigate(option.route) }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filas de 2 botones
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HomeOptionCard(
                        modifier = Modifier.weight(1f).height(120.dp),
                        option = cardOptions[0],
                        onClick = { onNavigate(cardOptions[0].route) }
                    )
                    HomeOptionCard(
                        modifier = Modifier.weight(1f).height(120.dp),
                        option = cardOptions[1],
                        onClick = { onNavigate(cardOptions[1].route) }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HomeOptionCard(
                        modifier = Modifier.weight(1f).height(120.dp),
                        option = cardOptions[2],
                        onClick = { onNavigate(cardOptions[2].route) },
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    HomeOptionCard(
                        modifier = Modifier.weight(1f).height(120.dp),
                        option = cardOptions[3],
                        onClick = { onNavigate(cardOptions[3].route) },
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
            // 5º Botón: Guía de Hortalizas (Ocupa toda la fila)
            item {
                HomeOptionCard(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    option = cardOptions[4],
                    onClick = { onNavigate(cardOptions[4].route) },
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            // Actividad reciente
            if (recentActivities.isNotEmpty()) {
                item {
                    Text(
                        text = "Actividad Reciente",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(recentActivities) { activity ->
                    RecentActivityCard(activityText = activity)
                }
            }
        }
    }
}

@Composable
fun HomeOptionCard(
    option: HomeOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentActivityCard(activityText: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = activityText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}