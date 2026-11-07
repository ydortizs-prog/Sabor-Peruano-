package com.tecsup.saborperuano

import com.tecsup.saborperuano.data.repository.CarritoRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.carrito.ResumenCarrito
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarritoRepositoryTest {
    private val carrito = CarritoRepository()
    private val plato = Item(id = "prueba", titulo = "Plato de prueba", precioCentimos = 1299)

    @Test
    fun agrupaUnidadesYCalculaTotalEnCentimos() {
        assertTrue(carrito.agregar(plato, 2))
        assertTrue(carrito.agregar(plato, 1))

        val resumen = ResumenCarrito(carrito.lineas.value)
        assertEquals(3, resumen.lineas.single().cantidad)
        assertEquals(3897L, resumen.totalCentimos)
    }

    @Test
    fun rechazaPlatosSinPrecioValido() {
        assertFalse(carrito.agregar(plato.copy(precioCentimos = null), 1))
        assertFalse(carrito.agregar(plato.copy(precioCentimos = 0), 1))
        assertTrue(carrito.lineas.value.isEmpty())
    }

    @Test
    fun permiteEditarYQuitarSinAfectarOtroCarrito() {
        val otroCarrito = CarritoRepository()
        carrito.agregar(plato, 3)
        otroCarrito.agregar(plato, 1)

        carrito.cambiarCantidad(plato.id, 2)
        assertEquals(2598L, ResumenCarrito(carrito.lineas.value).totalCentimos)
        carrito.quitar(plato.id)

        assertTrue(carrito.lineas.value.isEmpty())
        assertEquals(1, otroCarrito.lineas.value.single().cantidad)
    }
}
