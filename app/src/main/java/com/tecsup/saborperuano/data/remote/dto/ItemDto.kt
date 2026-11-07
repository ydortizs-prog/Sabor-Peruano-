package com.tecsup.saborperuano.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val imagenUrl: String? = null,
    val porciones: Int? = null,
    val ingredientes: List<String> = emptyList(),
    val preparacion: List<String> = emptyList()
)
