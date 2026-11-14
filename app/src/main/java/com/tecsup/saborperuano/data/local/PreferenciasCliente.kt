package com.tecsup.saborperuano.data.local

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal enum class TipoAvisoCliente { FAVORITO, CARRITO, WHATSAPP, PEDIDO_GUARDADO, PEDIDO_WHATSAPP }

@Serializable
internal data class AvisoGuardado(
    val tipo: TipoAvisoCliente,
    val plato: String? = null,
    val fecha: Long = 0L,
    // Oculto solo en la campana; el historial de pedidos del perfil lo sigue mostrando.
    val oculto: Boolean = false
)

@Serializable
internal data class ActividadGuardada(
    val avisos: List<AvisoGuardado> = emptyList(),
    val sinLeer: Boolean = false,
    val noLeidos: Int = 0
)

internal class PreferenciasCliente(private val preferencias: SharedPreferences) {
    private val _temaOscuro = MutableStateFlow(
        preferencias.all["tema_oscuro"] as? Boolean
    )
    val temaOscuro = _temaOscuro.asStateFlow()

    private val _valoraciones = MutableStateFlow(
        preferencias.all.mapNotNull { (clave, valor) ->
            val estrellas = valor as? Int
            if (clave.startsWith(PREFIJO) && estrellas != null && estrellas in 1..5) {
                clave.removePrefix(PREFIJO) to estrellas
            } else null
        }.toMap()
    )
    val valoraciones = _valoraciones.asStateFlow()

    private val _actividad = MutableStateFlow(leerActividad())
    val actividad = _actividad.asStateFlow()

    fun agregarAviso(tipo: TipoAvisoCliente, plato: String? = null) {
        val actual = _actividad.value
        val nuevo = AvisoGuardado(tipo, plato, System.currentTimeMillis())
        val ultimo = actual.avisos.firstOrNull()
        // Si se repite el mismo aviso seguido (p. ej. agregar el mismo plato), se actualiza en vez de duplicarse.
        val repetido = ultimo != null && !ultimo.oculto && ultimo.tipo == tipo && ultimo.plato == plato &&
            actual.noLeidos > 0
        val previos = if (repetido) actual.avisos.drop(1) else actual.avisos
        val noLeidos = if (repetido) actual.noLeidos else actual.noLeidos + 1
        guardarActividad(
            ActividadGuardada(
                avisos = (listOf(nuevo) + previos).take(MAX_AVISOS),
                sinLeer = true,
                noLeidos = noLeidos.coerceAtMost(MAX_AVISOS)
            )
        )
    }

    fun marcarAvisosLeidos() {
        val actual = _actividad.value
        if (actual.sinLeer || actual.noLeidos > 0) {
            guardarActividad(actual.copy(sinLeer = false, noLeidos = 0))
        }
    }

    fun ocultarAviso(indice: Int) {
        val actual = _actividad.value
        if (indice !in actual.avisos.indices) return
        val avisos = actual.avisos.mapIndexed { i, aviso -> if (i == indice) aviso.copy(oculto = true) else aviso }
        guardarActividad(actual.copy(avisos = avisos))
    }

    fun ocultarTodosLosAvisos() {
        val actual = _actividad.value
        guardarActividad(
            ActividadGuardada(
                avisos = actual.avisos.map { it.copy(oculto = true) },
                sinLeer = false,
                noLeidos = 0
            )
        )
    }

    private fun leerActividad(): ActividadGuardada {
        val guardado = preferencias.all[CLAVE_ACTIVIDAD] as? String ?: return ActividadGuardada()
        return try {
            Json.decodeFromString<ActividadGuardada>(guardado)
        } catch (_: SerializationException) {
            ActividadGuardada()
        }
    }

    private fun guardarActividad(actividad: ActividadGuardada) {
        preferencias.edit().putString(CLAVE_ACTIVIDAD, Json.encodeToString(actividad)).apply()
        _actividad.value = actividad
    }

    fun cambiarTema(oscuro: Boolean) {
        preferencias.edit().putBoolean("tema_oscuro", oscuro).apply()
        _temaOscuro.value = oscuro
    }

    fun calificar(id: String, estrellas: Int?) {
        if (estrellas == null) {
            preferencias.edit().remove(PREFIJO + id).apply()
            _valoraciones.update { it - id }
        } else {
            require(estrellas in 1..5)
            preferencias.edit().putInt(PREFIJO + id, estrellas).apply()
            _valoraciones.update { it + (id to estrellas) }
        }
    }

    private companion object {
        const val PREFIJO = "valoracion_"
        const val CLAVE_ACTIVIDAD = "actividad_cliente"
        const val MAX_AVISOS = 12
    }
}
