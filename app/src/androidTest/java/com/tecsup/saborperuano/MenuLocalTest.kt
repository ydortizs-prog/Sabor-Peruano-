package com.tecsup.saborperuano

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.remote.ApiService
import com.tecsup.saborperuano.data.remote.dto.ItemDto
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
            val original = ItemDto("aji-de-gallina", "Ají de gallina", "", precioCentimos = 1800)
            val api = object : ApiService {
                override suspend fun listar() = listOf(original)
                override suspend fun obtenerDetalle(id: String) = original
            }
            val catalogo = ItemRepositoryImpl(api, db)
            val menu = MenuLocalRepository(db)
            catalogo.refrescar().getOrThrow()
            assertTrue(catalogo.alternarFavorito(original.id))

            val foto = "foto-local:content://prueba/plato"
            menu.guardar(original.id, DatosPlatilloLocal("Ají de la casa", "Edición local", 1950, foto))
            menu.guardar(null, DatosPlatilloLocal("Nuevo plato", "Solo en este dispositivo", 1350))
            catalogo.limpiarCache()
            catalogo.refrescar().getOrThrow()

            val platos = catalogo.items.first()
            val nuevo = platos.single { it.titulo == "Nuevo plato" }
            assertEquals(1350, catalogo.obtenerDetalle(nuevo.id).getOrThrow().precioCentimos)
            val editado = catalogo.obtenerDetalle(original.id).getOrThrow()
            assertEquals(1950, editado.precioCentimos)
            assertEquals(foto, editado.imagenUrl)
            assertEquals("Ají de la casa", catalogo.favoritos.first().single().titulo)

            menu.retirar(original.id)
            catalogo.refrescar().getOrThrow()
            assertFalse(catalogo.items.first().any { it.id == original.id })
            assertTrue(catalogo.favoritos.first().isEmpty())
            assertTrue(catalogo.obtenerDetalle(original.id).isFailure)
        } finally {
            db.close()
        }
    }
}
