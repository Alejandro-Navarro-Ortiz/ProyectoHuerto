package com.example.proyecto_huerto.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_huerto.models.Actividad
import com.example.proyecto_huerto.models.Bancal
import com.example.proyecto_huerto.models.Cultivo
import com.example.proyecto_huerto.models.Hortaliza
import com.example.proyecto_huerto.models.TipoActividad
import com.example.proyecto_huerto.notifications.NotificationScheduler
import com.example.proyecto_huerto.util.getCurrentEpochMillis
import com.example.proyecto_huerto.util.getCurrentInstant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.messaging.messaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BancalStats(
    val plantasActuales: Int = 0,
    val riegos: Int = 0,
    val abonados: Int = 0,
    val cosechas: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class BancalViewModel(
    private val notificationScheduler: NotificationScheduler? = null
) : ViewModel() {
    private val _bancales = MutableStateFlow<List<Bancal>>(emptyList())
    val bancales = _bancales.asStateFlow()

    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        listenToBancales()
        listenToActividades()
        saveNotificationToken()
    }

    fun getStatsForBancal(bancal: Bancal): BancalStats {
        val bancalActividades = _actividades.value.filter { it.nombreBancal == bancal.nombre }

        // CORREGIDO: Suma las cantidades en lugar de solo contar las actividades
        val riegos = bancalActividades.filter { it.tipo == TipoActividad.RIEGO }.sumOf { it.cantidad }
        val abonados = bancalActividades.filter { it.tipo == TipoActividad.ABONADO }.sumOf { it.cantidad }
        val cosechas = bancalActividades.filter { it.tipo == TipoActividad.COSECHA }.sumOf { it.cantidad }

        val plantasActuales = bancal.cultivos.size

        return BancalStats(plantasActuales, riegos, abonados, cosechas)
    }

    private fun saveNotificationToken() {
        viewModelScope.launch {
            auth.authStateChanged.collectLatest { user ->
                if (user != null) {
                    try {
                        val token = Firebase.messaging.getToken()
                        db.collection("usuarios").document(user.uid)
                            .set(mapOf("fcmToken" to token), merge = true)
                    } catch (e: Exception) {
                        println("ERROR FCM: ${e.message}")
                    }
                }
            }
        }
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
                println("ERROR FIRESTORE BANCALES: ${e.message}")
            }.collect { snapshot ->
                if (snapshot != null) {
                    _bancales.value = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.data<Bancal>().copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                } else {
                    _bancales.value = emptyList()
                }
            }
        }
    }

    private fun listenToActividades() {
        viewModelScope.launch {
            auth.authStateChanged.flatMapLatest { user ->
                if (user != null) {
                    db.collection("usuarios").document(user.uid).collection("actividades").snapshots
                } else {
                    flowOf(null)
                }
            }.catch { e ->
                println("ERROR FIRESTORE ACTIVIDADES: ${e.message}")
            }.collect { snapshot ->
                if (snapshot != null) {
                    _actividades.value = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.data<Actividad>().copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                } else {
                    _actividades.value = emptyList()
                }
            }
        }
    }

    // CORREGIDO: Ahora acepta una cantidad
    private fun registrarActividad(tipo: TipoActividad, nombreBancal: String, detalle: String, cantidad: Int = 1) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val actividad = Actividad(
                    tipo = tipo,
                    fecha = getCurrentEpochMillis(),
                    nombreBancal = nombreBancal,
                    detalle = detalle,
                    usuarioId = user.uid,
                    cantidad = cantidad
                )
                db.collection("usuarios").document(user.uid).collection("actividades").add(actividad)
            } catch (e: Exception) {
                println("Error actividad: ${e.message}")
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

    fun updateCultivos(bancal: Bancal, posiciones: List<String>, hortaliza: Hortaliza) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val nuevosCultivos = bancal.cultivos.toMutableMap()
                val ahora = getCurrentInstant()

                val nuevoCultivo = Cultivo(
                    hortalizaId = hortaliza.nombre,
                    nombreHortaliza = hortaliza.nombreMostrado,
                    frecuenciaRiegoDias = 2,
                    fechaPlantado = ahora,
                    ultimoRiego = null
                )

                posiciones.forEach { nuevosCultivos[it] = nuevoCultivo }
                val bancalActualizado = bancal.copy(cultivos = nuevosCultivos)
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancal.id)
                    .set(bancalActualizado)

                val nombreHortalizaStr = hortaliza.nombreMostrado["es"] ?: hortaliza.nombre
                registrarActividad(
                    tipo = TipoActividad.SIEMBRA,
                    nombreBancal = bancal.nombre,
                    detalle = "Sembrado: $nombreHortalizaStr",
                    cantidad = posiciones.size // CORREGIDO
                )
            } catch (e: Exception) {
                println("ERROR SIEMBRA: ${e.message}")
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
                    cultivosActualizados[pos]?.let { cultivo ->
                        cultivosActualizados[pos] = cultivo.copy(ultimoRiego = ahora)
                        val nombrePlanta = cultivo.nombreHortaliza["es"] ?: ""
                        notificationScheduler?.scheduleRiegoNotification(
                            plantName = nombrePlanta,
                            daysDelay = cultivo.frecuenciaRiegoDias
                        )
                    }
                }

                val bancalActualizado = bancal.copy(cultivos = cultivosActualizados)
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancal.id)
                    .set(bancalActualizado)

                registrarActividad(
                    tipo = TipoActividad.RIEGO,
                    nombreBancal = bancal.nombre,
                    detalle = "Regadas ${posiciones.size} secciones",
                    cantidad = posiciones.size // CORREGIDO
                )
            } catch (e: Exception) {
                println("ERROR RIEGO: ${e.message}")
            }
        }
    }

    fun abonarCultivos(bancal: Bancal, posiciones: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                registrarActividad(
                    tipo = TipoActividad.ABONADO,
                    nombreBancal = bancal.nombre,
                    detalle = "Abonadas ${posiciones.size} secciones",
                    cantidad = posiciones.size // CORREGIDO
                )
            } catch (e: Exception) {
                println("ERROR ABONO: ${e.message}")
            }
        }
    }

    fun cosecharCultivos(bancal: Bancal, posiciones: List<String>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val cultivosActualizados = bancal.cultivos.toMutableMap()
                val nombresCosechados = posiciones.mapNotNull {
                    cultivosActualizados[it]?.nombreHortaliza?.get("es")
                }.distinct().joinToString(", ")

                posiciones.forEach { cultivosActualizados.remove(it) }

                val bancalActualizado = bancal.copy(cultivos = cultivosActualizados)
                db.collection("usuarios").document(user.uid).collection("bancales")
                    .document(bancal.id)
                    .set(bancalActualizado)

                registrarActividad(
                    tipo = TipoActividad.COSECHA,
                    nombreBancal = bancal.nombre,
                    detalle = "Cosechado: $nombresCosechados",
                    cantidad = posiciones.size // CORREGIDO
                )
            } catch (e: Exception) {
                println("ERROR COSECHA: ${e.message}")
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
                println("ERROR ELIMINAR: ${e.message}")
            }
        }
    }

    fun getBancalById(id: String): Bancal? = _bancales.value.find { it.id == id }
}
