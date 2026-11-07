package com.tecsup.saborperuano.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val subtitulo: String,
    val imagenUrl: String?,
    val esFavorito: Boolean = false
)
