package com.tecsup.saborperuano.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.UiState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class ListaViewModel(private val repositorio: ItemRepository) : ViewModel() {
    private val resultadoRefresco = MutableStateFlow<Result<Int>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val estado: StateFlow<UiState<List<Item>>> = resultadoRefresco
        .flatMapLatest { resultado ->
            val error = resultado?.exceptionOrNull()
            when {
                resultado == null -> flowOf(UiState.Cargando)
                error != null && error !is IOException -> flowOf(UiState.Error(mensajeDe(error)))
                else -> repositorio.items.map { recetas ->
                    if (recetas.isEmpty() && error != null) {
                        UiState.Error(mensajeDe(error))
                    } else {
                        UiState.Exito(recetas)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Cargando)

    val sinConexion: StateFlow<Boolean> = resultadoRefresco
        .map { it?.exceptionOrNull() is IOException }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var carga: Job? = null

    init {
        refrescar()
    }

    fun refrescar() {
        if (carga?.isActive == true) return

        carga = viewModelScope.launch {
            resultadoRefresco.value = null
            resultadoRefresco.value = repositorio.refrescar()
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No se pudo conectar con el catálogo. Revisa tu conexión."
        is HttpException -> "El servidor respondió con el error HTTP ${error.code()}."
        is SerializationException -> "El catálogo llegó en un formato inesperado."
        else -> "No se pudo cargar el catálogo."
    }

    class Factory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == ListaViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return ListaViewModel(appContainer.itemRepository) as T
        }
    }
}
