package com.tecsup.saborperuano.data.remote

import com.tecsup.saborperuano.data.remote.dto.RespuestaMeals
import retrofit2.http.GET
import retrofit2.http.Query

/** Categoría de TheMealDB asignada al tema Comidas (variantes: Seafood · Dessert · Chicken). */
const val CATEGORIA_TEMA = "Seafood"

interface ApiService {
    /** Lista: /filter.php?c={categoría} */
    @GET("filter.php")
    suspend fun listar(@Query("c") categoria: String): RespuestaMeals

    /** Detalle: /lookup.php?i={id} */
    @GET("lookup.php")
    suspend fun obtenerDetalle(@Query("i") id: String): RespuestaMeals
}
