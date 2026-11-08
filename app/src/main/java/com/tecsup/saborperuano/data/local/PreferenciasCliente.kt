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
internal data class AvisoGuardado(val tipo: TipoAvisoCliente, val plato: String? = null)

@Serializable
internal data class ActividadGuardada(
    val avisos: List<AvisoGuardado> = emptyList(),
    val sinLeer: Boolean = false
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
        val avisos = (listOf(AvisoGuardado(tipo, plato)) + _actividad.value.avisos).take(12)
        guardarActividad(ActividadGuardada(avisos, sinLeer = true))
    }

    fun marcarAvisosLeidos() {
        if (_actividad.value.sinLeer) guardarActividad(_actividad.value.copy(sinLeer = false))
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
    }
}
