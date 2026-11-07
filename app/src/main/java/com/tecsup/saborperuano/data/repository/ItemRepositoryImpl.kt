package com.tecsup.saborperuano.data.repository

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.local.ItemEntity
import com.tecsup.saborperuano.data.mapper.aEntity
import com.tecsup.saborperuano.data.mapper.aModelo
import com.tecsup.saborperuano.data.remote.ApiService
import com.tecsup.saborperuano.domain.model.Item
import java.io.IOException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class ItemRepositoryImpl(
    private val api: ApiService,
    private val database: AppDatabase
) : ItemRepository {
    private val dao = database.itemDao()

    override val items = dao.todos()
        .map { lista -> lista.map(ItemEntity::aModelo) }
        .distinctUntilChanged()

    override val favoritos = dao.favoritos()
        .map { lista -> lista.map(ItemEntity::aModelo) }
        .distinctUntilChanged()

    override suspend fun refrescar(): Result<Int> = ejecutarPeticion {
        val recetas = api.listar()
        database.withTransaction {
            val idsFavoritos = dao.idsFavoritos().toSet()
            dao.guardarTodos(recetas.map { it.aEntity(esFavorito = it.id in idsFavoritos) })
        }
        recetas.size
    }

    override suspend fun obtenerDetalle(id: String): Result<Item> = ejecutarPeticion {
        val detalle = api.obtenerDetalle(id).aModelo()
        detalle.copy(esFavorito = dao.esFavorito(id))
    }

    override suspend fun alternarFavorito(id: String) = dao.alternarFavorito(id)

    override suspend fun cantidadGuardada(): Int = dao.contar()

    override suspend fun cantidadFavoritos(): Int = dao.contarFavoritos()

    override suspend fun limpiarCache() = dao.borrarTodo()

    private suspend fun <T> ejecutarPeticion(operacion: suspend () -> T): Result<T> = try {
        Result.success(operacion())
    } catch (error: IOException) {
        Result.failure(error)
    } catch (error: HttpException) {
        Result.failure(error)
    } catch (error: SerializationException) {
        Result.failure(error)
    } catch (error: SQLiteException) {
        Result.failure(error)
    }
}
