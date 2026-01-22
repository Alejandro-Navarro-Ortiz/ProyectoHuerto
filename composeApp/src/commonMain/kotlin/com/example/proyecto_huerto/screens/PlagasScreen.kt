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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// 1. Estructura de datos para una Plaga
data class Plaga(
    val id: String,
    val name: String,
    val scientificName: String,
    val description: String,
    val symptoms: String,
    val organicTreatment: String
)

// 2. Información sobre las plagas
val plagasList = listOf(
    Plaga(
        id = "pulgon",
        name = "Pulgón",
        scientificName = "Aphidoidea",
        description = "Pequeños insectos chupadores de savia que se agrupan en los brotes tiernos, hojas y tallos. Pueden ser de varios colores (verdes, negros, amarillos).",
        symptoms = "Hojas deformadas, amarillentas y pegajosas (por la melaza que excretan). Crecimiento debilitado. Puede atraer hormigas y favorecer la aparición del hongo negrilla.",
        organicTreatment = "Rociar con jabón potásico o aceite de Neem. Fomentar la presencia de depredadores naturales como mariquitas y crisopas. Infusiones de ajo o guindilla también son efectivas."
    ),
    Plaga(
        id = "mosca_blanca",
        name = "Mosca Blanca",
        scientificName = "Aleyrodidae",
        description = "Insectos muy pequeños de color blanco que se asientan en el envés de las hojas. Al agitar la planta, levantan el vuelo formando una nube.",
        symptoms = "Amarillamiento y debilitamiento general de la planta, ya que chupan la savia. Similar al pulgón, secretan una melaza que atrae a las hormigas y al hongo negrilla.",
        organicTreatment = "Uso de trampas cromáticas amarillas. Rociar el envés de las hojas con jabón potásico o aceite de Neem. Introducir depredadores como la avispa Encarsia formosa."
    ),
    Plaga(
        id = "arana_roja",
        name = "Araña Roja",
        scientificName = "Tetranychus urticae",
        description = "Ácaros muy pequeños, casi imperceptibles a simple vista, que tejen finas telarañas en el envés de las hojas. Prefieren ambientes secos y cálidos.",
        symptoms = "Aparición de puntos amarillos o pardos en las hojas (decoloración). En ataques fuertes, las hojas se secan y caen. Se pueden observar finas telarañas.",
        organicTreatment = "Aumentar la humedad ambiental regando la planta (no las flores). Rociar con jabón potásico y aceite de Neem. El ácaro depredador Phytoseiulus persimilis es un excelente control biológico."
    ),
    Plaga(
        id = "babosas_caracoles",
        name = "Babosas y Caracoles",
        scientificName = "Gastropoda",
        description = "Moluscos terrestres que se alimentan de noche o en días lluviosos, dejando un rastro de baba brillante a su paso.",
        symptoms = "Agujeros irregulares en hojas y tallos tiernos. En plántulas jóvenes pueden devorarlas por completo.",
        organicTreatment = "Recogida manual al atardecer. Colocar trampas de cerveza (recipientes enterrados a ras de suelo con cerveza). Barreras de ceniza, cáscaras de huevo trituradas o serrín alrededor de las plantas."
    ),
    Plaga(
        id = "orugas",
        name = "Orugas",
        scientificName = "Lepidoptera (larva)",
        description = "Larvas de mariposas y polillas. Son voraces y pueden tener una gran variedad de tamaños y colores.",
        symptoms = "Hojas mordidas, a veces hasta dejar solo los nervios. Pueden perforar frutos y tallos. Presencia de excrementos (pequeñas bolitas negras).",
        organicTreatment = "Retirada manual. Aplicación de Bacillus thuringiensis, una bacteria que afecta específicamente a las larvas de lepidópteros y es inocua para otros seres vivos."
    )
)

// 3. Pantalla principal de Plagas (la lista)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagasScreen(
    onPlagaClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guía de Plagas") },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(plagasList) { plaga ->
                PlagaListItem(plaga = plaga, onClick = { onPlagaClick(plaga.id) })
            }
        }
    }
}

// 4. Elemento de la lista de plagas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagaListItem(plaga: Plaga, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = plaga.name, style = MaterialTheme.typography.titleLarge)
                Text(text = plaga.scientificName, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// 5. Pantalla de detalle de una plaga
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagaDetailScreen(
    plaga: Plaga,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plaga.name) },
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
                Column {
                    Text(plaga.scientificName, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    
                    PlagaDetailSection("Descripción", plaga.description)
                    PlagaDetailSection("Síntomas y Daños", plaga.symptoms)
                    PlagaDetailSection("Tratamiento Ecológico", plaga.organicTreatment)
                }
            }
        }
    }
}

@Composable
fun PlagaDetailSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify
        )
    }
}
