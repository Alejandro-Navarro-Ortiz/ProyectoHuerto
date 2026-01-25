package com.example.proyecto_huerto.data

import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.Plaga
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// --- DICCIONARIOS DE TRADUCCIÓN ---
private val hortalizaNameTranslations = mapOf(
    "ajo" to "Garlic", "berenjena" to "Eggplant", "brocoli" to "Broccoli",
    "calabacin" to "Zucchini", "cebolla" to "Onion", "espinaca" to "Spinach",
    "judia" to "Bean", "lechuga" to "Lettuce", "patata" to "Potato",
    "pepino" to "Cucumber", "pimiento" to "Pepper", "rabano" to "Radish",
    "tomate" to "Tomato", "zanahoria" to "Carrot", "remolacha" to "Beetroot",
    "guisante" to "Pea", "hinojo" to "Fennel", "albahaca" to "Basil"
)

private val plagaNameTranslations = mapOf(
    "arana_roja" to "Spider Mite", "babosas_caracoles" to "Slugs & Snails",
    "mildiu" to "Mildew", "minadores" to "Leaf Miners", "mosca_blanca" to "Whitefly",
    "nematodos" to "Nematodes", "oidio" to "Powdery Mildew", "orugas" to "Caterpillars",
    "pulgon" to "Aphid"
)

// --- MODELOS ANTIGUOS ---
@Serializable private data class OldHortaliza(val nombre: String = "", val icono: String = "", val descripcion: String = "", val consejos: String = "", val compatibles: List<String> = emptyList(), val incompatibles: List<String> = emptyList())
@Serializable private data class OldPlaga(val id: String = "", val name: String = "", val scientificName: String = "", val description: String = "", val symptoms: String = "", val organicTreatment: String = "")


object Migration {
    private val firestore = Firebase.firestore

    fun migrateData() {
        println("--- INICIANDO PROCESO DE MIGRACIÓN FINAL CON TRADUCCIONES ---")
        GlobalScope.launch {
            migrateHortalizas()
            migratePlagas()
        }
    }

    private suspend fun migrateHortalizas() {
        println("\n--> Migrando HORTALIZAS con traducciones...")
        try {
            val collection = firestore.collection("hortalizas")
            val snapshot = collection.get()
            for (doc in snapshot.documents) {
                try {
                    val oldHortaliza = doc.data<OldHortaliza>()
                    val hortalizaId = doc.id

                    // Si ya tiene un nombre en inglés, asumimos que ya está migrado.
                    val currentData = doc.data<Hortaliza>()
                    if (currentData.nombreMostrado["en"] != null && !currentData.nombreMostrado["en"]!!.startsWith("[")) {
                         println("--> INFO: Hortaliza '${doc.id}' parece ya traducida. Saltando.")
                        continue
                    }

                    println("--> PROCESANDO: Hortaliza '${hortalizaId}'...")

                    val nombreEn = hortalizaNameTranslations[hortalizaId] ?: oldHortaliza.nombre

                    val newHortaliza = Hortaliza(
                        nombre = oldHortaliza.nombre,
                        nombreMostrado = mapOf("es" to oldHortaliza.nombre, "en" to nombreEn),
                        icono = oldHortaliza.icono,
                        descripcion = mapOf("es" to oldHortaliza.descripcion, "en" to "[English description for $nombreEn]"),
                        consejos = mapOf("es" to oldHortaliza.consejos, "en" to "[English tips for $nombreEn]"),
                        compatibles = oldHortaliza.compatibles,
                        incompatibles = oldHortaliza.incompatibles
                    )

                    collection.document(doc.id).set(newHortaliza, merge = false)
                    println("--> ÉXITO: Hortaliza '${hortalizaId}' migrada con nombre y placeholders.")
                } catch (e: Exception) {
                     println("### ERROR al migrar la hortaliza '${doc.id}': ${e.message}")
                }
            }
            println("--- MIGRACIÓN DE HORTALIZAS FINALIZADA. ---")
        } catch (e: Exception) {
            println("### ERROR CRÍTICO en la colección 'hortalizas': ${e.message}")
        }
    }

    private suspend fun migratePlagas() {
        println("\n--> Migrando PLAGAS con traducciones...")
        try {
            val collection = firestore.collection("plagas")
            val snapshot = collection.get()
            for (doc in snapshot.documents) {
                try {
                    val oldPlaga = doc.data<OldPlaga>()
                    val plagaId = doc.id
                    
                    val currentData = doc.data<Plaga>()
                    if (currentData.name["en"] != null && !currentData.name["en"]!!.startsWith("[")) {
                         println("--> INFO: Plaga '${doc.id}' parece ya traducida. Saltando.")
                        continue
                    }

                    println("--> PROCESANDO: Plaga '${plagaId}'...")
                    
                    val nombreEn = plagaNameTranslations[plagaId] ?: oldPlaga.name

                    val newPlaga = Plaga(
                        id = oldPlaga.id,
                        name = mapOf("es" to oldPlaga.name, "en" to nombreEn),
                        scientificName = mapOf("es" to oldPlaga.scientificName, "en" to oldPlaga.scientificName), // El nombre científico no se suele traducir
                        description = mapOf("es" to oldPlaga.description, "en" to "[English description for $nombreEn]"),
                        symptoms = mapOf("es" to oldPlaga.symptoms, "en" to "[English symptoms for $nombreEn]"),
                        organicTreatment = mapOf("es" to oldPlaga.organicTreatment, "en" to "[English organic treatment for $nombreEn]")
                    )

                    collection.document(doc.id).set(newPlaga, merge = false)
                    println("--> ÉXITO: Plaga '${plagaId}' migrada con nombre y placeholders.")
                } catch (e: Exception) {
                    println("### ERROR al migrar la plaga '${doc.id}': ${e.message}")
                }
            }
            println("--- MIGRACIÓN DE PLAGAS FINALIZADA. ---")
        } catch (e: Exception) {
            println("### ERROR CRÍTICO en la colección 'plagas': ${e.message}")
        }
    }
}