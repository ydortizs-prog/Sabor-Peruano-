package com.tecsup.saborperuano.data.repository

import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.domain.model.LineaCarrito
import com.tecsup.saborperuano.domain.model.MAX_UNIDADES_POR_PLATO
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CarritoRepository {
    private val _lineas = MutableStateFlow<List<LineaCarrito>>(emptyList())
    val lineas: StateFlow<List<LineaCarrito>> = _lineas.asStateFlow()

    fun agregar(plato: Item, cantidad: Int): Boolean {
        val precio = plato.precioCentimos?.takeIf { it > 0 } ?: return false
        if (cantidad !in 1..MAX_UNIDADES_POR_PLATO) return false

        _lineas.update { actuales ->
            val anterior = actuales.find { it.id == plato.id }
            val unidades = ((anterior?.cantidad ?: 0) + cantidad).coerceAtMost(MAX_UNIDADES_POR_PLATO)
            val actualizada = LineaCarrito(plato.id, plato.titulo, precio, unidades)
            if (anterior == null) actuales + actualizada
            else actuales.map { if (it.id == plato.id) actualizada else it }
        }
        return true
    }

    fun cambiarCantidad(id: String, cantidad: Int) {
        if (cantidad !in 1..MAX_UNIDADES_POR_PLATO) return
        _lineas.update { actuales ->
            actuales.map { if (it.id == id) it.copy(cantidad = cantidad) else it }
        }
    }

    private var ultimaSolicitud: SolicitudPedido? = null

    internal fun prepararSolicitud(datos: SolicitudPedido): SolicitudPedido {
        val anterior = ultimaSolicitud
        if (anterior?.copy(intento = "") == datos) return checkNotNull(anterior)
        return datos.copy(intento = UUID.randomUUID().toString()).also { ultimaSolicitud = it }
    }

    fun vaciar() {
        ultimaSolicitud = null
        _lineas.value = emptyList()
    }

    fun quitar(id: String) {
        _lineas.update { actuales -> actuales.filterNot { it.id == id } }
    }
}
