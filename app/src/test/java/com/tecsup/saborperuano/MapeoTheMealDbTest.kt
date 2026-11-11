package com.tecsup.saborperuano

import com.tecsup.saborperuano.data.mapper.aEntity
import com.tecsup.saborperuano.data.mapper.aModelo
import com.tecsup.saborperuano.data.mapper.precioDePlato
import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.data.remote.dto.RespuestaMeals
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapeoTheMealDbTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun listaDeTheMealDbSeConvierteEnEntidadConPrecio() {
        val respuesta = json.decodeFromString<RespuestaMeals>(
            """{"meals":[{"strMeal":"Baked salmon with fennel & tomatoes",
               "strMealThumb":"https://www.themealdb.com/images/media/meals/1548772327.jpg","idMeal":"52959"}]}"""
        )
        val entidad = respuesta.meals!!.single().aEntity(esFavorito = true)

        assertEquals("52959", entidad.id)
        assertEquals("Baked salmon with fennel & tomatoes", entidad.titulo)
        assertEquals("Pescados y mariscos", entidad.subtitulo)
        assertTrue(entidad.esFavorito)
        assertTrue(entidad.precioCentimos!! in 1500..2500)
    }

    @Test
    fun detalleTraeOrigenCategoriaEInstrucciones() {
        val respuesta = json.decodeFromString<RespuestaMeals>(
            """{"meals":[{"idMeal":"52959","strMeal":"Baked salmon","strCategory":"Seafood",
               "strArea":"British","strInstructions":"Heat oven.","strTags":null,"strIngredient1":"Fennel"}]}"""
        )
        val plato = respuesta.meals!!.single().aModelo()

        assertEquals("Británica · Pescados y mariscos", plato.subtitulo)
        assertEquals("Heat oven.", plato.descripcion)
    }

    @Test
    fun sinResultadosMealsVieneNullYElPrecioEsEstable() {
        assertNull(json.decodeFromString<RespuestaMeals>("""{"meals":null}""").meals)
        assertEquals(precioDePlato("52959"), precioDePlato("52959"))
        assertEquals(precioDePlato("52959"), ItemDto("52959", "x").aModelo().precioCentimos)
    }
}
