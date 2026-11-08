package com.tecsup.saborperuano.ui.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.data.repository.CarritoRepository
import com.tecsup.saborperuano.data.repository.LineaSolicitud
import com.tecsup.saborperuano.data.repository.PedidoGuardado
import com.tecsup.saborperuano.data.repository.PedidoRepository
import com.tecsup.saborperuano.data.repository.SolicitudPedido
import com.tecsup.saborperuano.domain.model.LineaCarrito
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal enum class Entrega { RECOJO, DELIVERY }

internal enum class MetodoPago(val etiqueta: Int, val nombre: String) {
    YAPE(R.string.pedido_pago_yape, "Yape"),
    EFECTIVO(R.string.pedido_pago_efectivo, "Efectivo")
}

internal data class DatosPedido(
    val nombre: String = "",
    val telefono: String = "",
    val entrega: Entrega = Entrega.RECOJO,
    val direccion: String = "",
    val observaciones: String = "",
    val metodoPago: MetodoPago? = null
) {
    val nombreValido: Boolean get() = nombre.isNotBlank()
    val telefonoValido: Boolean get() =
        telefono.trim().matches(Regex("""\+?[0-9 ()-]+""")) &&
            telefono.count { it in '0'..'9' } in 7..15
    val direccionValida: Boolean get() = entrega == Entrega.RECOJO || direccion.isNotBlank()
    val valido: Boolean get() = nombreValido && telefonoValido && direccionValida
}

internal sealed interface EnvioPedido {
    data object SinEnviar : EnvioPedido
    data object Guardando : EnvioPedido
    data class Guardado(
        val pedido: PedidoGuardado,
        val solicitud: SolicitudPedido,
        val lineas: List<LineaCarrito>
    ) : EnvioPedido
    data class Error(val mensaje: Int, val reintentar: Boolean) : EnvioPedido
}

internal class ConfirmarPedidoViewModel(
    private val carrito: CarritoRepository,
    private val pedidos: PedidoRepository,
    private val uid: String
) : ViewModel() {
    val envioHabilitado = pedidos.habilitado
    private val _envio = MutableStateFlow<EnvioPedido>(EnvioPedido.SinEnviar)
    val envio = _envio.asStateFlow()
    private val _datos = MutableStateFlow(DatosPedido())
    val datos = _datos.asStateFlow()
    val resumen = carrito.lineas
        .map(::ResumenCarrito)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenCarrito(carrito.lineas.value))

    fun nombre(valor: String) = _datos.update { it.copy(nombre = valor) }
    fun telefono(valor: String) = _datos.update { it.copy(telefono = valor) }
    fun entrega(valor: Entrega) = _datos.update { it.copy(entrega = valor) }
    fun direccion(valor: String) = _datos.update { it.copy(direccion = valor) }
    fun observaciones(valor: String) = _datos.update { it.copy(observaciones = valor) }
    fun metodoPago(valor: MetodoPago) = _datos.update { it.copy(metodoPago = valor) }

    fun guardar() {
        val estado = _envio.value
        if (!envioHabilitado || estado is EnvioPedido.Guardando || estado is EnvioPedido.Guardado ||
            (estado is EnvioPedido.Error && !estado.reintentar)
        ) return

        val contacto = datos.value
        val seleccion = ResumenCarrito(carrito.lineas.value)
        if (!contacto.valido || contacto.entrega != Entrega.RECOJO || seleccion.lineas.isEmpty()) return

        val solicitud = carrito.prepararSolicitud(SolicitudPedido(
            intento = "",
            nombre = contacto.nombre.trim(),
            telefono = contacto.telefono.trim(),
            entrega = "recojo",
            direccion = null,
            observaciones = contacto.observaciones.trim(),
            lineas = seleccion.lineas.map { LineaSolicitud(it.id, it.cantidad) },
            subtotalCentimos = seleccion.totalCentimos
        ))

        _envio.value = EnvioPedido.Guardando
        viewModelScope.launch {
            try {
                val pedido = pedidos.guardar(uid, solicitud)
                _envio.value = EnvioPedido.Guardado(pedido, solicitud, seleccion.lineas)
                carrito.vaciar()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val http = (error as? HttpException)?.code()
                _envio.value = if (http == 400) {
                    EnvioPedido.Error(R.string.pedido_error_cotizacion, reintentar = false)
                } else {
                    EnvioPedido.Error(R.string.pedido_error_guardar, reintentar = http !in listOf(401, 403, 409))
                }
            }
        }
    }

    class Factory(
        private val carrito: CarritoRepository,
        private val pedidos: PedidoRepository,
        private val uid: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == ConfirmarPedidoViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return ConfirmarPedidoViewModel(carrito, pedidos, uid) as T
        }
    }
}
