package com.tecsup.saborperuano.data.remote.dto

import kotlinx.serialization.Serializable

/** TheMealDB envuelve la lista y el detalle en "meals" (puede venir null si no hay resultados). */
@Serializable
data class RespuestaMeals(val meals: List<ItemDto>? = null)

/**
 * Plato tal como lo responde TheMealDB.
 * La lista (filter.php) solo trae id, nombre y foto; el detalle (lookup.php) agrega categoría, origen e instrucciones.
 */
@Serializable
data class ItemDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String? = null,
    val strCategory: String? = null,
    val strArea: String? = null,
    val strInstructions: String? = null
)
