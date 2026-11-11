package com.tecsup.saborperuano.data.traduccion

/** Traduce los textos de TheMealDB (inglés) al español. Si no puede, devuelve el texto original. */
interface Traductor {
    suspend fun aEspanol(texto: String): String
}

/** Para pruebas o si no se quiere traducir: deja el texto igual. */
object SinTraduccion : Traductor {
    override suspend fun aEspanol(texto: String): String = texto
}
