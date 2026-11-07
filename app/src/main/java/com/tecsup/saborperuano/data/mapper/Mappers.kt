package com.tecsup.saborperuano.data.mapper

import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.domain.model.Item

fun ItemDto.aModelo(): Item = Item(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    porciones = porciones,
    ingredientes = ingredientes,
    preparacion = preparacion
)
