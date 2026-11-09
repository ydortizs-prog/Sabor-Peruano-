package com.tecsup.saborperuano.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.local.PreferenciasCliente
import com.tecsup.saborperuano.data.repository.CarritoRepository

internal class SesionNavegacionViewModel(
    val carrito: CarritoRepository,
    val preferencias: PreferenciasCliente
) : ViewModel() {
    private var ultimoPedido: String? = null

    fun registrarPedido(id: String): Boolean {
        if (ultimoPedido == id) return false
        ultimoPedido = id
        return true
    }

    fun limpiarSesion() {
        carrito.vaciar()
        ultimoPedido = null
    }

    class Factory(private val container: AppContainer, private val uid: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == SesionNavegacionViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return SesionNavegacionViewModel(
                container.crearCarritoDeSesion(), container.preferenciasCliente(uid)
            ) as T
        }
    }
}
