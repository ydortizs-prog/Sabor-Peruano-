package com.tecsup.saborperuano.data.mapper

import com.tecsup.saborperuano.data.local.ItemEntity
import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.domain.model.Item

fun ItemDto.aEntity(esFavorito: Boolean): ItemEntity = ItemEntity(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    esFavorito = esFavorito
)

fun ItemEntity.aModelo(): Item = Item(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    esFavorito = esFavorito
)

fun ItemDto.aModelo(): Item = Item(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    porciones = porciones,
    ingredientes = ingredientes,
    preparacion = preparacion
)
