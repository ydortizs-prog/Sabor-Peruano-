package com.tecsup.saborperuano.ui.detalle

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.UiState
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DetalleViewModel(
    private val repositorio: ItemRepository,
    private val id: String
) : ViewModel() {
    private val estadoDetalle = MutableStateFlow<UiState<Item>>(UiState.Cargando)
    val estado: StateFlow<UiState<Item>> = combine(estadoDetalle, repositorio.favoritos) { detalle, favoritos ->
        when (detalle) {
            is UiState.Exito -> UiState.Exito(
                detalle.datos.copy(esFavorito = favoritos.any { it.id == id })
            )
            else -> detalle
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Cargando)

    private val _errorFavorito = MutableStateFlow<String?>(null)
    val errorFavorito: StateFlow<String?> = _errorFavorito.asStateFlow()
    private var carga: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        if (carga?.isActive == true) return

        carga = viewModelScope.launch {
            estadoDetalle.value = UiState.Cargando
            estadoDetalle.value = repositorio.obtenerDetalle(id).fold(
                onSuccess = { UiState.Exito(it) },
                onFailure = { UiState.Error(mensajeDe(it)) }
            )
        }
    }

    fun alternarFavorito() {
        if (estado.value !is UiState.Exito) return

        viewModelScope.launch {
            _errorFavorito.value = null
            try {
                repositorio.alternarFavorito(id)
            } catch (_: SQLiteException) {
                _errorFavorito.value = "No se pudo actualizar el favorito. Inténtalo de nuevo."
            }
        }
    }

    private fun mensajeDe(error: Throwable): String = when {
        error is HttpException && error.code() == 404 -> "No encontramos esta receta."
        error is IOException -> "No se pudo conectar. Revisa tu conexión."
        else -> "No se pudo cargar la receta."
    }

    class Factory(
        private val appContainer: AppContainer,
        private val id: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == DetalleViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return DetalleViewModel(appContainer.itemRepository, id) as T
        }
    }
}
