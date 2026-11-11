package com.tecsup.saborperuano

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.remote.ApiService
import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.data.remote.dto.RespuestaMeals
import com.tecsup.saborperuano.data.repository.DatosPlatilloLocal
import com.tecsup.saborperuano.data.repository.ItemRepositoryImpl
import com.tecsup.saborperuano.data.repository.MenuLocalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuLocalTest {
    @Test
    fun sincronizarConservaEdicionesFavoritosYPlatosRetirados() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val original = ItemDto("52959", "Baked salmon with fennel & tomatoes")
            val api = object : ApiService {
                override suspend fun listar(categoria: String) = RespuestaMeals(listOf(original))
                override suspend fun obtenerDetalle(id: String) = RespuestaMeals(listOf(original))
            }
            val catalogo = ItemRepositoryImpl(api, db)
            val menu = MenuLocalRepository(db)
            catalogo.refrescar().getOrThrow()
            assertTrue(catalogo.alternarFavorito(original.idMeal))

            val foto = "foto-local:content://prueba/plato"
            menu.guardar(original.idMeal, DatosPlatilloLocal("Ají de la casa", "Edición local", 1950, foto))
            menu.guardar(null, DatosPlatilloLocal("Nuevo plato", "Solo en este dispositivo", 1350))
            catalogo.limpiarCache()
            catalogo.refrescar().getOrThrow()

            val platos = catalogo.items.first()
            val nuevo = platos.single { it.titulo == "Nuevo plato" }
            assertEquals(1350, catalogo.obtenerDetalle(nuevo.id).getOrThrow().precioCentimos)
            val editado = catalogo.obtenerDetalle(original.idMeal).getOrThrow()
            assertEquals(1950, editado.precioCentimos)
            assertEquals(foto, editado.imagenUrl)
            assertEquals("Ají de la casa", catalogo.favoritos.first().single().titulo)

            menu.retirar(original.idMeal)
            catalogo.refrescar().getOrThrow()
            assertFalse(catalogo.items.first().any { it.id == original.idMeal })
            assertTrue(catalogo.favoritos.first().isEmpty())
            assertTrue(catalogo.obtenerDetalle(original.idMeal).isFailure)
        } finally {
            db.close()
        }
    }
}
