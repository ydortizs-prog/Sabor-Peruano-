package com.tecsup.saborperuano.domain.model

const val MAX_UNIDADES_POR_PLATO = 99

data class LineaCarrito(
    val id: String,
    val nombre: String,
    val precioCentimos: Int,
    val cantidad: Int
) {
    val subtotalCentimos: Long get() = precioCentimos.toLong() * cantidad
}
