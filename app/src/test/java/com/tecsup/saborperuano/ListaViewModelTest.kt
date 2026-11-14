package com.tecsup.saborperuano

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.notification.Notificador
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.lista.CatalogoVisible
import com.tecsup.saborperuano.ui.lista.ListaViewModel
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListaViewModelTest {
    private val aji = Item("aji-de-gallina", "Ají de gallina", precioCentimos = 1800)
    private val avisos = mutableListOf<String>()
    private lateinit var viewModel: ListaViewModel

    @Before
    fun configurarDispatcher() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun cerrarViewModel() {
        if (::viewModel.isInitialized) viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun cargaInicialEsperaElRefrescoYMuestraLosPlatosGuardados() = runTest {
        val respuesta = CompletableDeferred<Result<Int>>()
        val repositorio = RepositorioFalso(emptyList(), respuesta)
        crearViewModel(repositorio)
        runCurrent()

        assertEquals(UiState.Cargando, viewModel.catalogo.value)
        assertTrue(avisos.isEmpty())

        repositorio.items.value = listOf(aji)
        respuesta.complete(Result.success(1))
        runCurrent()

        assertEquals(UiState.Exito(CatalogoVisible(listOf(aji), false)), viewModel.catalogo.value)
        assertFalse(viewModel.sinConexion.value)
        assertEquals(listOf("Se guardaron 1 elementos."), avisos)
    }

    @Test
    fun sinConexionConCacheMuestraLosPlatosYActivaElAviso() = runTest {
        crearViewModel(RepositorioFalso(listOf(aji), CompletableDeferred(Result.failure(IOException()))))
        runCurrent()

        assertEquals(UiState.Exito(CatalogoVisible(listOf(aji), false)), viewModel.catalogo.value)
        assertTrue(viewModel.sinConexion.value)
        assertTrue(avisos.isEmpty())
    }

    @Test
    fun sinConexionSinCacheMuestraErrorYPermiteReintentar() = runTest {
        val repositorio = RepositorioFalso(emptyList(), CompletableDeferred(Result.failure(IOException())))
        crearViewModel(repositorio)
        runCurrent()

        assertTrue(viewModel.catalogo.value is UiState.Error)
        assertTrue(viewModel.sinConexion.value)

        repositorio.items.value = listOf(aji)
        repositorio.respuesta = CompletableDeferred(Result.success(1))
        viewModel.refrescar()
        runCurrent()

        assertEquals(UiState.Exito(CatalogoVisible(listOf(aji), false)), viewModel.catalogo.value)
        assertFalse(viewModel.sinConexion.value)
    }

    private fun crearViewModel(repositorio: ItemRepository) {
        val notificador = object : Notificador {
            override fun mostrar(titulo: String, texto: String) {
                avisos.add(texto)
            }
        }
        viewModel = ListaViewModel(repositorio, notificador, SavedStateHandle())
    }

    private class RepositorioFalso(
        cache: List<Item>,
        var respuesta: CompletableDeferred<Result<Int>>
    ) : ItemRepository {
        override val items = MutableStateFlow(cache)
        override val favoritos = flowOf(emptyList<Item>())

        override suspend fun refrescar(): Result<Int> = respuesta.await()
        override suspend fun obtenerDetalle(id: String): Result<Item> = error("Fuera del flujo de lista")
        override suspend fun alternarFavorito(id: String): Boolean = error("Fuera de estas pruebas")
        override suspend fun cantidadGuardada(): Int = items.value.size
        override suspend fun cantidadFavoritos(): Int = 0
        override suspend fun limpiarCache() {
            items.value = emptyList()
        }
    }
}
