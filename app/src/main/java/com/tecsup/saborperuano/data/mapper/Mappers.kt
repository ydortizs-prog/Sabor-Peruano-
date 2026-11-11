package com.tecsup.saborperuano.data.mapper

import com.tecsup.saborperuano.data.local.ItemEntity
import com.tecsup.saborperuano.data.remote.CATEGORIA_TEMA
import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.domain.model.Item

/**
 * TheMealDB no trae precios, pero el carrito los necesita.
 * Se asigna un precio fijo por plato, siempre el mismo para el mismo id: de S/ 15.00 a S/ 25.00.
 */
internal fun precioDePlato(id: String): Int = 1500 + id.hashCode().mod(11) * 100

/** Categorías y orígenes de TheMealDB en español (son pocos y fijos). */
private val enEspanol = mapOf(
    "Seafood" to "Pescados y mariscos", "Dessert" to "Postres", "Chicken" to "Pollo",
    "American" to "Estadounidense", "Argentinian" to "Argentina", "Australian" to "Australiana",
    "Brazilian" to "Brasileña", "British" to "Británica", "Canadian" to "Canadiense", "Chinese" to "China",
    "Croatian" to "Croata", "Dutch" to "Neerlandesa", "Egyptian" to "Egipcia", "Filipino" to "Filipina",
    "French" to "Francesa", "Greek" to "Griega", "Indian" to "India", "Irish" to "Irlandesa",
    "Italian" to "Italiana", "Jamaican" to "Jamaiquina", "Japanese" to "Japonesa", "Kenyan" to "Keniana",
    "Malaysian" to "Malaya", "Mexican" to "Mexicana", "Moroccan" to "Marroquí", "Norwegian" to "Noruega",
    "Polish" to "Polaca", "Portuguese" to "Portuguesa", "Russian" to "Rusa", "Saudi Arabian" to "Saudí",
    "Slovakian" to "Eslovaca", "Spanish" to "Española", "Syrian" to "Siria", "Thai" to "Tailandesa",
    "Tunisian" to "Tunecina", "Turkish" to "Turca", "Ukrainian" to "Ucraniana", "Uruguayan" to "Uruguaya",
    "Venezulan" to "Venezolana", "Venezuelan" to "Venezolana", "Vietnamese" to "Vietnamita"
)

private fun String.alEspanol(): String = enEspanol[this] ?: this

private fun ItemDto.subtituloTema(): String =
    listOfNotNull(
        strArea?.takeIf { it.isNotBlank() && it != "Unknown" }?.alEspanol(),
        (strCategory?.takeIf { it.isNotBlank() } ?: CATEGORIA_TEMA).alEspanol()
    ).joinToString(" · ")

fun ItemDto.aEntity(esFavorito: Boolean): ItemEntity = ItemEntity(
    id = idMeal,
    titulo = strMeal,
    subtitulo = subtituloTema(),
    imagenUrl = strMealThumb,
    esFavorito = esFavorito,
    precioCentimos = precioDePlato(idMeal)
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
    id = idMeal,
    titulo = strMeal,
    subtitulo = subtituloTema(),
    imagenUrl = strMealThumb,
    precioCentimos = precioDePlato(idMeal),
    descripcion = strInstructions.orEmpty().trim()
)
