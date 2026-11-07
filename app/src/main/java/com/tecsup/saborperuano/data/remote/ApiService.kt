package com.tecsup.saborperuano.data.remote

import com.tecsup.saborperuano.data.remote.dto.ItemDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("recetas.json")
    suspend fun listar(): List<ItemDto>

    @GET("recetas/{id}.json")
    suspend fun obtenerDetalle(@Path("id") id: String): ItemDto
}
