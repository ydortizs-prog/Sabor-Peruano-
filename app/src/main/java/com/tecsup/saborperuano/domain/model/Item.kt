package com.tecsup.saborperuano.domain.model

data class Item(
    val id: String,
    val titulo: String,
    val subtitulo: String = "",
    val imagenUrl: String? = null,
    val esFavorito: Boolean = false,
    val precioCentimos: Int? = null
)
