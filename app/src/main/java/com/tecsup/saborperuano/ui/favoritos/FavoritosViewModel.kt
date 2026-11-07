package com.tecsup.saborperuano.ui.favoritos

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritosViewModel(private val repositorio: ItemRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = _estado.asStateFlow()
    private var observacion: Job? = null

    init {
        observarFavoritos()
    }

    fun observarFavoritos() {
        if (observacion?.isActive == true) return

        observacion = viewModelScope.launch {
            _estado.value = UiState.Cargando
            try {
                repositorio.favoritos.collect { _estado.value = UiState.Exito(it) }
            } catch (_: SQLiteException) {
                _estado.value = UiState.Error("No se pudieron leer las recetas favoritas.")
            }
        }
    }

    class Factory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == FavoritosViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return FavoritosViewModel(appContainer.itemRepository) as T
        }
    }
}
