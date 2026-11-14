package com.tecsup.saborperuano.domain.model

data class Item(
    val id: String,
    val titulo: String,
    val subtitulo: String = "",
    val imagenUrl: String? = null,
    val esFavorito: Boolean = false,
    val precioCentimos: Int? = null,
    /** Solo viene en el detalle (segundo endpoint): instrucciones de preparación. */
    val descripcion: String = ""
)
