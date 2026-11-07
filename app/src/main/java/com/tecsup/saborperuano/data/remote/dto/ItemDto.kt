package com.tecsup.saborperuano.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val imagenUrl: String? = null,
    val precioCentimos: Int? = null
)
