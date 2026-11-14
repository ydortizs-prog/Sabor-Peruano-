package com.tecsup.saborperuano.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE retiradoLocal = 0 ORDER BY titulo")
    fun todos(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE esFavorito = 1 AND retiradoLocal = 0 ORDER BY titulo")
    fun favoritos(): Flow<List<ItemEntity>>

    @Query("SELECT id FROM items WHERE editadoLocal = 1")
    suspend fun idsEditadosLocalmente(): List<String>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun buscar(id: String): ItemEntity?

    @Query("SELECT id FROM items WHERE esFavorito = 1")
    suspend fun idsFavoritos(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM items WHERE id = :id AND esFavorito = 1)")
    suspend fun esFavorito(id: String): Boolean

    @Query("SELECT precioCentimos FROM items WHERE id = :id")
    suspend fun precioCentimos(id: String): Int?

    @Query("SELECT COUNT(*) FROM items WHERE retiradoLocal = 0")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM items WHERE esFavorito = 1")
    suspend fun contarFavoritos(): Int

    @Upsert
    suspend fun guardarTodos(items: List<ItemEntity>)

    @Query("UPDATE items SET esFavorito = NOT esFavorito WHERE id = :id AND retiradoLocal = 0")
    suspend fun alternarFavorito(id: String)

    @Query(
        "UPDATE items SET editadoLocal = 1, retiradoLocal = 1, esFavorito = 0 " +
            "WHERE id = :id AND retiradoLocal = 0"
    )
    suspend fun retirarLocalmente(id: String): Int

    @Query("DELETE FROM items WHERE editadoLocal = 0")
    suspend fun borrarCacheRemota()

    /** Quita los platos descargados antes que ya no vienen en la API (no toca ediciones locales). */
    @Query("DELETE FROM items WHERE editadoLocal = 0 AND id NOT IN (:ids)")
    suspend fun borrarAusentes(ids: List<String>)
}
