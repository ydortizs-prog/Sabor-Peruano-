package com.tecsup.saborperuano.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ListaViewModel(private val repositorio: ItemRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = _estado.asStateFlow()

    init {
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            repositorio.refrescar().fold(
                onSuccess = { _estado.value = UiState.Exito(repositorio.items.first()) },
                onFailure = { _estado.value = UiState.Error("No se pudo cargar el catálogo.") }
            )
        }
    }

    class Factory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == ListaViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return ListaViewModel(appContainer.itemRepository) as T
        }
    }
}
