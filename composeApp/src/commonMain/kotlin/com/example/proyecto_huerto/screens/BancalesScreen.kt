package com.example.proyecto_huerto.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_huerto.models.Bancal

@Composable
fun BancalesScreen(bancales: List<Bancal>) {
    LazyColumn {
        items(bancales) { bancal ->
            BancalItem(bancal = bancal)
        }
    }
}

@Composable
fun BancalItem(bancal: Bancal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = bancal.nombre, style = MaterialTheme.typography.titleLarge)
            Text(text = "Cultivos: ${bancal.cultivos.joinToString()}")
            Text(text = "Fecha de siembra: ${bancal.fechaSiembra}")
        }
    }
}

@Preview
@Composable
fun BancalesScreenPreview() {
    val bancalesDeEjemplo = listOf(
        Bancal("1", "Bancal de tomates", listOf("Tomate", "Albahaca"), "2024-05-10"),
        Bancal("2", "Bancal de lechugas", listOf("Lechuga"), "2024-03-15"),
        Bancal("3", "Bancal de hierbas", listOf("Menta", "Perejil", "Cilantro"), "2024-04-01")
    )
    BancalesScreen(bancales = bancalesDeEjemplo)
}
