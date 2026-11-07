package com.tecsup.saborperuano.ui.lista

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.notification.Notificador
import com.tecsup.saborperuano.ui.UiState
import java.io.IOException
import java.text.Normalizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

internal enum class Categoria(val etiqueta: Int, val ids: Set<String>) {
    TODOS(R.string.catalogo_todos, emptySet()),
    ENTRADAS(R.string.catalogo_entradas, setOf("causa-limena")),
    POLLO(R.string.catalogo_pollo, setOf("aji-de-gallina", "arroz-con-pollo")),
    CARNE(R.string.catalogo_carne, setOf("lomo-saltado"))
}

private val marcasDiacriticas = Regex("\\p{M}+")
private fun String.paraBusqueda(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(marcasDiacriticas, "")

internal data class CatalogoVisible(val platos: List<Item>, val catalogoVacio: Boolean)

class ListaViewModel(
    private val repositorio: ItemRepository,
    private val notificador: Notificador,
    private val estadoGuardado: SavedStateHandle
) : ViewModel() {
    private val resultadoRefresco = MutableStateFlow<Result<Int>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val estado: StateFlow<UiState<List<Item>>> = resultadoRefresco
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

    val consulta = estadoGuardado.getStateFlow("consulta", "")
    internal val categoria = estadoGuardado.getStateFlow("categoria", Categoria.TODOS)
    internal val catalogo: StateFlow<UiState<CatalogoVisible>> =
        combine(estado, consulta, categoria) { actual, texto, seleccion ->
            when (actual) {
                UiState.Cargando -> UiState.Cargando
                is UiState.Error -> actual
                is UiState.Exito -> {
                    val termino = texto.trim().paraBusqueda()
                    val platos = actual.datos.filter { plato ->
                        (seleccion == Categoria.TODOS || plato.id in seleccion.ids) &&
                            (plato.titulo.paraBusqueda().contains(termino, ignoreCase = true) ||
                                plato.subtitulo.paraBusqueda().contains(termino, ignoreCase = true))
                    }
                    UiState.Exito(CatalogoVisible(platos, actual.datos.isEmpty()))
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Cargando)

    fun buscar(texto: String) {
        estadoGuardado["consulta"] = texto
    }

    internal fun seleccionarCategoria(categoria: Categoria) {
        estadoGuardado["categoria"] = categoria
    }

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
            val resultado = repositorio.refrescar()
            resultadoRefresco.value = resultado
            resultado.onSuccess { cantidad ->
                notificador.mostrar("Catálogo actualizado", "Se guardaron $cantidad elementos.")
            }
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No se pudo conectar con el catálogo. Revisa tu conexión."
        is HttpException -> "El servidor respondió con el error HTTP ${error.code()}."
        is SerializationException -> "El catálogo llegó en un formato inesperado."
        else -> "No se pudo cargar el catálogo."
    }

    class Factory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass == ListaViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return ListaViewModel(
                appContainer.itemRepository,
                appContainer.notificador,
                extras.createSavedStateHandle()
            ) as T
        }
    }
}
