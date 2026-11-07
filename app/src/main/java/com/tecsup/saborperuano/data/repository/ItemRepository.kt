package com.tecsup.saborperuano.data.repository

import com.tecsup.saborperuano.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    val items: Flow<List<Item>>
    val favoritos: Flow<List<Item>>

    suspend fun refrescar(): Result<Int>
    suspend fun obtenerDetalle(id: String): Result<Item>
    suspend fun alternarFavorito(id: String)
    suspend fun cantidadGuardada(): Int
    suspend fun cantidadFavoritos(): Int
    suspend fun limpiarCache()
}
