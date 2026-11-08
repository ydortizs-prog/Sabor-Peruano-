package com.tecsup.saborperuano.data.repository

import androidx.room.withTransaction
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.local.ItemEntity
import java.util.UUID

internal const val MaxPrecioLocalCentimos = 1_000_000

internal data class DatosPlatilloLocal(
    val nombre: String,
    val descripcion: String,
    val precioCentimos: Int
)

internal class MenuLocalRepository(private val database: AppDatabase) {
    private val dao = database.itemDao()

    suspend fun guardar(id: String?, datos: DatosPlatilloLocal) = database.withTransaction {
        require(datos.nombre.isNotBlank() && datos.nombre.length <= 80)
        require(datos.descripcion.length <= 240)
        require(datos.precioCentimos in 1..MaxPrecioLocalCentimos)

        val anterior = id?.let { checkNotNull(dao.buscar(it)) }
        check(anterior?.retiradoLocal != true)
        val plato = anterior ?: ItemEntity(
            id = "local-${UUID.randomUUID()}",
            titulo = datos.nombre,
            subtitulo = datos.descripcion,
            imagenUrl = null
        )
        dao.guardarTodos(listOf(plato.copy(
            titulo = datos.nombre,
            subtitulo = datos.descripcion,
            precioCentimos = datos.precioCentimos,
            editadoLocal = true
        )))
    }

    suspend fun retirar(id: String) {
        check(dao.retirarLocalmente(id) == 1)
    }
}
