package com.tecsup.saborperuano.data.repository

import com.tecsup.saborperuano.data.mapper.aModelo
import com.tecsup.saborperuano.data.remote.ApiService
import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.domain.model.Item
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class ItemRepositoryImpl(private val api: ApiService) : ItemRepository {
    private val catalogo = MutableStateFlow<List<Item>>(emptyList())

    override val items = catalogo.asStateFlow()
    override val favoritos = items.map { lista -> lista.filter(Item::esFavorito) }

    override suspend fun refrescar(): Result<Int> = ejecutarPeticion {
        val recetas = api.listar().map(ItemDto::aModelo)
        catalogo.update { actuales ->
            val idsFavoritos = actuales.filter(Item::esFavorito).map(Item::id).toSet()
            recetas.map { it.copy(esFavorito = it.id in idsFavoritos) }
        }
        recetas.size
    }

    override suspend fun obtenerDetalle(id: String): Result<Item> = ejecutarPeticion {
        val detalle = api.obtenerDetalle(id).aModelo()
        val esFavorito = catalogo.value.any { it.id == id && it.esFavorito }
        detalle.copy(esFavorito = esFavorito)
    }

    private suspend fun <T> ejecutarPeticion(operacion: suspend () -> T): Result<T> = try {
        Result.success(operacion())
    } catch (error: IOException) {
        Result.failure(error)
    } catch (error: HttpException) {
        Result.failure(error)
    } catch (error: SerializationException) {
        Result.failure(error)
    }

    override suspend fun alternarFavorito(id: String) {
        catalogo.update { lista ->
            lista.map { if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it }
        }
    }

    override suspend fun cantidadGuardada(): Int = catalogo.value.size

    override suspend fun cantidadFavoritos(): Int = catalogo.value.count(Item::esFavorito)

    override suspend fun limpiarCache() {
        catalogo.value = emptyList()
    }
}
