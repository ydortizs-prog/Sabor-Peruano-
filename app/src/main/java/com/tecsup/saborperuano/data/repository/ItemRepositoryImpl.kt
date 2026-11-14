package com.tecsup.saborperuano.data.repository

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.mapper.aEntity
import com.tecsup.saborperuano.data.mapper.aModelo
import com.tecsup.saborperuano.data.remote.ApiService
import com.tecsup.saborperuano.data.remote.CATEGORIA_TEMA
import com.tecsup.saborperuano.data.traduccion.SinTraduccion
import com.tecsup.saborperuano.data.traduccion.Traductor
import com.tecsup.saborperuano.domain.model.Item
import java.io.IOException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class ItemRepositoryImpl(
    private val api: ApiService,
    private val database: AppDatabase,
    private val preciosDePrueba: Map<String, Int> = emptyMap(),
    private val traductor: Traductor = SinTraduccion
) : ItemRepository {
    private val dao = database.itemDao()
    private val cambiosCatalogo = Mutex()

    override val items = dao.todos()
        .map { lista -> lista.map { it.aModelo().conPrecioDePrueba() } }
        .distinctUntilChanged()

    override val favoritos = dao.favoritos()
        .map { lista -> lista.map { it.aModelo().conPrecioDePrueba() } }
        .distinctUntilChanged()

    override suspend fun refrescar(): Result<Int> = ejecutarPeticion {
        cambiosCatalogo.withLock {
            // Los nombres se guardan en Room ya traducidos: así se ven y se buscan en español, incluso sin internet.
            val recetas = api.listar(CATEGORIA_TEMA).meals.orEmpty()
                .map { it.copy(strMeal = traductor.aEspanol(it.strMeal)) }
            database.withTransaction {
                val idsFavoritos = dao.idsFavoritos().toSet()
                val idsEditados = dao.idsEditadosLocalmente().toSet()
                if (recetas.isNotEmpty()) dao.borrarAusentes(recetas.map { it.idMeal })
                val actualizables = recetas.filterNot { it.idMeal in idsEditados }
                dao.guardarTodos(actualizables.map { it.aEntity(esFavorito = it.idMeal in idsFavoritos) })
                actualizables.size
            }
        }
    }

    override suspend fun obtenerDetalle(id: String): Result<Item> = ejecutarPeticion {
        val local = dao.buscar(id)
        if (local?.retiradoLocal == true) throw NoSuchElementException()
        if (local?.editadoLocal == true) return@ejecutarPeticion local.aModelo()
        val remoto = api.obtenerDetalle(id).meals?.firstOrNull() ?: throw NoSuchElementException()
        val detalle = remoto.copy(
            strMeal = traductor.aEspanol(remoto.strMeal),
            strInstructions = remoto.strInstructions?.let { traductor.aEspanol(it) }
        ).aModelo()
        detalle.copy(
            esFavorito = dao.esFavorito(id),
            precioCentimos = detalle.precioCentimos ?: dao.precioCentimos(id)
        ).conPrecioDePrueba()
    }

    override suspend fun alternarFavorito(id: String): Boolean = database.withTransaction {
        dao.alternarFavorito(id)
        dao.esFavorito(id)
    }

    override suspend fun cantidadGuardada(): Int = dao.contar()

    override suspend fun cantidadFavoritos(): Int = dao.contarFavoritos()

    override suspend fun limpiarCache() = cambiosCatalogo.withLock { dao.borrarCacheRemota() }

    private fun Item.conPrecioDePrueba(): Item =
        if (precioCentimos == null) copy(precioCentimos = preciosDePrueba[id]) else this

    private suspend fun <T> ejecutarPeticion(operacion: suspend () -> T): Result<T> = try {
        Result.success(operacion())
    } catch (error: IOException) {
        Result.failure(error)
    } catch (error: NoSuchElementException) {
        Result.failure(error)
    } catch (error: HttpException) {
        Result.failure(error)
    } catch (error: SerializationException) {
        Result.failure(error)
    } catch (error: SQLiteException) {
        Result.failure(error)
    }
}
