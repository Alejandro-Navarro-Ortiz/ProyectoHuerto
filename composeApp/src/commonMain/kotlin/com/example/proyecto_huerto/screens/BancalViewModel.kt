package com.example.proyecto_huerto.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Cultivo
import com.example.proyecto_huerto.models.hortalizasDisponibles
import com.example.proyecto_huerto.models.Actividad
import com.example.proyecto_huerto.models.TipoActividad
import com.example.proyecto_huerto.util.getCurrentInstant
import com.example.proyecto_huerto.util.getCurrentEpochMillis
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BancalViewModel : ViewModel() {
    private val _bancales = MutableStateFlow<List<Bancal>>(emptyList())
    val bancales = _bancales.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenToBancales()
    }

    private fun listenToBancales() {
        viewModelScope.launch {
            auth.authStateChanged.flatMapLatest { user ->
                if (user != null) {
                    db.collection("usuarios").document(user.uid).collection("bancales").snapshots
                } else {
                    flowOf(null)
                }
            }.catch { e ->
                println("ERROR GENERAL FIRESTORE: ${e.message}")
            }.collect { snapshot ->
                if (snapshot != null) {
                    _bancales.value = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.data<Bancal>().copy(id = doc.id)
                        } catch (e: Exception) {
                            println("ERROR AL LEER BANCAL (${doc.id}): ${e.message}")
                            e.printStackTrace()
                            null
                        }
                    }
                } else {
                    _bancales.value = emptyList()
                }
            }
        }
    }

    private fun registrarActividad(tipo: TipoActividad, nombreBancal: String, detalle: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val actividad = Actividad(
                    tipo = tipo,
                    fecha = getCurrentEpochMillis(),
                    nombreBancal = nombreBancal,
                    detalle = detalle,
                    usuarioId = user.uid
                )
                db.collection("usuarios").document(user.uid).collection("actividades").add(actividad)
            } catch (e: Exception) {
                println("Error al registrar actividad: ${e.message}")
            }
        }
    }

    fun addBancal(nombre: String, ancho: Int, largo: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val nuevo = Bancal(nombre = nombre, ancho = ancho, largo = largo)
                db.collection("usuarios").document(user.uid).collection("bancales").add(nuevo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCultivos(bancal: Bancal, posiciones: List<String>, hortalizaNombre: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val hortaliza = hortalizasDisponibles.find { it.nombre == hortalizaNombre } ?: return@launch
                val nuevosCultivos = bancal.cultivos.toMutableMap()
                val nuevoCultivo = Cultivo(
                    nombreHortaliza = hortaliza.nombre,
                    frecuenciaRiegoDias = 2,
                    ultimaVezRegado = getCurrentInstant()
                )

                posiciones.forEach { nuevosCultivos[it] = nuevoCultivo }
                val bancalActualizado = bancal.copy(cultivos = nuevosCultivos)

                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancal.id)
                    .set(bancalActualizado)

                // Registro automático de siembra
                registrarActividad(
                    tipo = TipoActividad.SIEMBRA,
                    nombreBancal = bancal.nombre,
                    detalle = "Sembrado: ${hortaliza.nombre} (${posiciones.size} celdas)"
                )

            } catch (e: Exception) {
                println("ERROR AL ACTUALIZAR EL CULTIVO: ${e.message}")
            }
        }
    }

    fun regarCultivos(bancal: Bancal, posiciones: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val cultivosActualizados = bancal.cultivos.toMutableMap()
                val ahora = getCurrentInstant()

                posiciones.forEach { pos ->
                    cultivosActualizados[pos]?.let {
                        cultivosActualizados[pos] = it.copy(ultimaVezRegado = ahora)
                    }
                }

                val bancalActualizado = bancal.copy(cultivos = cultivosActualizados)

                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancal.id)
                    .set(bancalActualizado)

                // Registro automático de riego
                registrarActividad(
                    tipo = TipoActividad.RIEGO,
                    nombreBancal = bancal.nombre,
                    detalle = "Regadas ${posiciones.size} secciones"
                )

            } catch (e: Exception) {
                println("ERROR AL REGAR LOS CULTIVOS: ${e.message}")
            }
        }
    }

    fun deleteBancal(bancalId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancalId)
                    .delete()
            } catch (e: Exception) {
                println("ERROR AL ELIMINAR BANCAL: ${e.message}")
            }
        }
    }

    fun getBancalById(id: String): Bancal? = _bancales.value.find { it.id == id }
}