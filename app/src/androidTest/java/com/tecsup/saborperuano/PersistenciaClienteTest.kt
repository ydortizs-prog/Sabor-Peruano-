package com.tecsup.saborperuano

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.saborperuano.data.local.AppDatabase
import com.tecsup.saborperuano.data.local.PreferenciasCliente
import com.tecsup.saborperuano.data.local.TipoAvisoCliente
import com.tecsup.saborperuano.data.remote.ApiService
import com.tecsup.saborperuano.data.remote.dto.ItemDto
import com.tecsup.saborperuano.data.remote.dto.RespuestaMeals
import com.tecsup.saborperuano.data.repository.ItemRepositoryImpl
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenciaClienteTest {
    @Test
    fun favoritosYAvisosSeRestauranAlReabrirLosDatos() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val nombre = "persistencia-test-${UUID.randomUUID()}"
        val preferencias = contexto.getSharedPreferences(nombre, Context.MODE_PRIVATE)
        val plato = ItemDto("52959", "Baked salmon with fennel & tomatoes")
        val api = object : ApiService {
            override suspend fun listar(categoria: String) = RespuestaMeals(listOf(plato))
            override suspend fun obtenerDetalle(id: String) = RespuestaMeals(listOf(plato))
        }
        fun abrirBase() = Room.databaseBuilder(contexto, AppDatabase::class.java, nombre).build()

        try {
            val primera = abrirBase()
            try {
                val items = ItemRepositoryImpl(api, primera)
                items.refrescar().getOrThrow()
                assertTrue(items.alternarFavorito(plato.idMeal))
                PreferenciasCliente(preferencias).agregarAviso(TipoAvisoCliente.FAVORITO, plato.strMeal)
            } finally {
                primera.close()
            }

            val segunda = abrirBase()
            try {
                val items = ItemRepositoryImpl(api, segunda)
                items.refrescar().getOrThrow()
                assertEquals(plato.idMeal, items.favoritos.first().single().id)
            } finally {
                segunda.close()
            }

            val restaurada = PreferenciasCliente(preferencias)
            assertTrue(restaurada.actividad.value.sinLeer)
            assertEquals(plato.strMeal, restaurada.actividad.value.avisos.single().plato)

            restaurada.marcarAvisosLeidos()
            val leida = PreferenciasCliente(preferencias).actividad.value
            assertFalse(leida.sinLeer)
            assertEquals(TipoAvisoCliente.FAVORITO, leida.avisos.single().tipo)
        } finally {
            contexto.deleteDatabase(nombre)
            contexto.deleteSharedPreferences(nombre)
        }
    }
}
