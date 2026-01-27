package com.example.proyecto_huerto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_huerto.weather.WeatherState
import org.jetbrains.compose.resources.stringResource
import proyectohuerto.composeapp.generated.resources.*

/**
 * Representa una opción de navegación en el menú principal.
 */
data class HomeOption(
    val titleKey: org.jetbrains.compose.resources.StringResource,
    val icon: ImageVector,
    val route: String,
    val descriptionKey: org.jetbrains.compose.resources.StringResource
)

/**
 * Pantalla principal de la aplicación.
 * Ofrece acceso rápido a los servicios del huerto y muestra la actividad reciente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    weatherState: WeatherState,
    recentActivities: List<String>,
    onNavigate: (String) -> Unit,
    onRefreshWeather: () -> Unit
) {
    // Definición de las opciones del menú principal
    val navOptions = listOf(
        HomeOption(Res.string.home_bancales_title, Icons.Filled.Yard, "gestion_bancales", Res.string.home_bancales_desc),
        HomeOption(Res.string.home_diario_title, Icons.Filled.Book, "diario_cultivo", Res.string.home_diario_desc),
        HomeOption(Res.string.home_guia_title, Icons.Filled.LocalFlorist, "guia_hortalizas", Res.string.home_guia_desc),
        HomeOption(Res.string.guia_plagas_title, Icons.Filled.BugReport, "plagas", Res.string.guia_plagas_desc)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = stringResource(Res.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.home_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate("profile") },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = stringResource(Res.string.profile_title),
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget del Clima
            item {
                WeatherWidget(state = weatherState, onRefresh = onRefreshWeather)
            }

            item {
                Text(
                    stringResource(Res.string.home_services),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Cuadrícula de servicios (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HomeServiceCard(navOptions[0], Modifier.weight(1f)) { onNavigate(navOptions[0].route) }
                        HomeServiceCard(navOptions[1], Modifier.weight(1f)) { onNavigate(navOptions[1].route) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HomeServiceCard(navOptions[2], Modifier.weight(1f)) { onNavigate(navOptions[2].route) }
                        HomeServiceCard(navOptions[3], Modifier.weight(1f)) { onNavigate(navOptions[3].route) }
                    }
                }
            }

            // Tarjeta destacada: Consejo del día
            item {
                Card(
                    onClick = { onNavigate("consejos") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(stringResource(Res.string.home_consejos_title), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text(stringResource(Res.string.home_consejos_desc), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Sección de Actividad Reciente
            if (recentActivities.isNotEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.home_recent_activity),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(recentActivities.take(5)) { activity ->
                    TimelineActivityItem(activity)
                }
            }
        }
    }
}

@Composable
fun WeatherWidget(state: WeatherState, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (state) {
                is WeatherState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                        strokeWidth = 2.dp
                    )
                }
                is WeatherState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getWeatherIcon(state.weatherCode),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "${state.temperature}°C",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(getWeatherDescription(state.weatherCode)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(Res.string.weather_refresh),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                is WeatherState.Error -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.weather_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(Res.string.weather_retry),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getWeatherIcon(code: Int): ImageVector {
    return when (code) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.CloudQueue
        45, 48 -> Icons.Default.Cloud
        51, 53, 55, 61, 63, 65 -> Icons.Default.Umbrella
        71, 73, 75 -> Icons.Default.AcUnit
        80, 81, 82 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }
}

fun getWeatherDescription(code: Int): org.jetbrains.compose.resources.StringResource {
    return when (code) {
        0 -> Res.string.weather_sunny
        1, 2, 3 -> Res.string.weather_cloudy_part
        45, 48 -> Res.string.weather_foggy
        51, 53, 55 -> Res.string.weather_drizzle
        61, 63, 65 -> Res.string.weather_rain
        71, 73, 75 -> Res.string.weather_snow
        80, 81, 82 -> Res.string.weather_showers
        95, 96, 99 -> Res.string.weather_storm
        else -> Res.string.weather_cloudy
    }
}

/**
 * Tarjeta individual para cada servicio principal.
 */
@Composable
fun HomeServiceCard(option: HomeOption, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(stringResource(option.titleKey), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(stringResource(option.descriptionKey), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Elemento de la línea de tiempo para la actividad reciente.
 */
@Composable
fun TimelineActivityItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Spacer(Modifier.width(16.dp))
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
