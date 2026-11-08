package com.tecsup.saborperuano

import com.tecsup.saborperuano.data.repository.LineaSolicitud
import com.tecsup.saborperuano.data.repository.PedidoGuardado
import com.tecsup.saborperuano.data.repository.SolicitudPedido
import com.tecsup.saborperuano.domain.model.LineaCarrito
import com.tecsup.saborperuano.ui.carrito.DatosPedido
import com.tecsup.saborperuano.ui.carrito.Entrega
import com.tecsup.saborperuano.ui.carrito.EnvioPedido
import com.tecsup.saborperuano.ui.carrito.ResumenCarrito
import com.tecsup.saborperuano.ui.carrito.enlaceWhatsApp
import com.tecsup.saborperuano.ui.carrito.mensajeParaWhatsApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class PedidoWhatsAppTest {
    @Test
    fun codificaElResumenDelPedidoSinPerderTildesSaltosNiSignos() {
        val linea = LineaCarrito("aji-de-gallina", "Ají de gallina", 1800, 2)
        val solicitud = SolicitudPedido(
            "intento", "José", "+51999111222", "recojo", null, "Sin ají & con limón",
            listOf(LineaSolicitud(linea.id, linea.cantidad)), 3600
        )
        val recibo = EnvioPedido.Guardado(PedidoGuardado("pedido-prueba", 3600), solicitud, listOf(linea))
        val mensaje = mensajeParaWhatsApp(recibo)
        val url = URI(enlaceWhatsApp("51999111000", mensaje))

        assertEquals("wa.me", url.host)
        assertEquals("/51999111000", url.path)
        assertEquals("text=$mensaje", url.query)
        assertTrue(mensaje.contains("2 Ají de gallina"))
        assertTrue(mensaje.contains("Número de pedido: pedido-prueba"))
        assertFalse(mensaje.contains("Dirección:"))
    }

    @Test
    fun deliverySinRegistroMuestraSubtotalYTarifaPendiente() {
        val contacto = DatosPedido("Cliente", "999111222", Entrega.DELIVERY, "Dirección de prueba")
        val resumen = ResumenCarrito(listOf(LineaCarrito("lomo-saltado", "Lomo saltado", 2200, 1)))

        val mensaje = mensajeParaWhatsApp(contacto, resumen)

        assertTrue(mensaje.contains("Subtotal de platos: S/"))
        assertTrue(mensaje.contains("Costo de delivery por confirmar."))
        assertTrue(mensaje.contains("Dirección: Dirección de prueba"))
        assertFalse(mensaje.contains("Número de pedido:"))
        assertFalse(mensaje.contains("Total:"))
    }
}
