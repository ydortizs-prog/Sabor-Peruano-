package com.tecsup.saborperuano.data.mapper

import com.tecsup.saborperuano.data.local.ItemEntity
import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.domain.model.Item

fun ItemDto.aEntity(esFavorito: Boolean): ItemEntity = ItemEntity(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    esFavorito = esFavorito,
    precioCentimos = precioCentimos?.takeIf { it > 0 }
)

fun ItemEntity.aModelo(): Item = Item(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    esFavorito = esFavorito,
    precioCentimos = precioCentimos?.takeIf { it > 0 }
)

fun ItemDto.aModelo(): Item = Item(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    precioCentimos = precioCentimos?.takeIf { it > 0 }
)
