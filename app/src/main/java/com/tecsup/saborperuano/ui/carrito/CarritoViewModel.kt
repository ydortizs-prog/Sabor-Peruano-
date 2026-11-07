package com.tecsup.saborperuano.ui.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.data.repository.CarritoRepository
import com.tecsup.saborperuano.domain.model.LineaCarrito
import com.tecsup.saborperuano.ui.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ResumenCarrito(val lineas: List<LineaCarrito>) {
    val totalCentimos: Long = lineas.sumOf { it.subtotalCentimos }
}

class CarritoViewModel(private val repositorio: CarritoRepository) : ViewModel() {
    val estado: StateFlow<UiState<ResumenCarrito>> = repositorio.lineas
        .map { UiState.Exito(ResumenCarrito(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    fun cambiarCantidad(id: String, cantidad: Int) = repositorio.cambiarCantidad(id, cantidad)
    fun quitar(id: String) = repositorio.quitar(id)

    class Factory(private val repositorio: CarritoRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == CarritoViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return CarritoViewModel(repositorio) as T
        }
    }
}
