package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionBancalesScreen(
    bancales: List<Bancal>,
    onAddBancal: (String, Int, Int) -> Unit,
    onDeleteBancal: (String) -> Unit, // Nueva función para eliminar
    onNavigate: (String) -> Unit,
    onBancalClick: (String) -> Unit
) {
    var nombreBancal by remember { mutableStateOf("") }
    var ancho by remember { mutableStateOf("") }
    var largo by remember { mutableStateOf("") }
    var errorDimension by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Bancales") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onNavigate("Inicio") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = nombreBancal,
                    onValueChange = { nombreBancal = it },
                    label = { Text("Nombre del Bancal") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    TextField(
                        value = ancho,
                        onValueChange = { ancho = it },
                        label = { Text("Ancho (máx. 10)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorDimension != null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = largo,
                        onValueChange = { largo = it },
                        label = { Text("Largo (máx. 10)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorDimension != null
                    )
                }
                if (errorDimension != null) {
                    Text(
                        text = errorDimension!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val anchoInt = ancho.toIntOrNull()
                        val largoInt = largo.toIntOrNull()

                        if (nombreBancal.isNotBlank() && anchoInt != null && largoInt != null) {
                            if (anchoInt > 10 || largoInt > 10) {
                                errorDimension = "El ancho y el largo no pueden ser mayores a 10."
                            } else {
                                errorDimension = null
                                onAddBancal(nombreBancal, anchoInt, largoInt)
                                nombreBancal = ""
                                ancho = ""
                                largo = ""
                            }
                        } else {
                            errorDimension = "Todos los campos son obligatorios."
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Añadir Bancal")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(bancales) { bancal ->
                    Card(
                        onClick = { onBancalClick(bancal.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bancal.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteBancal(bancal.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar Bancal",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
