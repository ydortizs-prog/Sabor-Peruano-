package com.tecsup.saborperuano

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.saborperuano.data.local.PreferenciasCliente
import com.tecsup.saborperuano.data.local.TipoAvisoCliente
import com.tecsup.saborperuano.data.repository.CarritoRepository
import com.tecsup.saborperuano.data.repository.PedidoRepository
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.carrito.ConfirmarPedidoViewModel
import com.tecsup.saborperuano.ui.carrito.EnvioPedido
import com.tecsup.saborperuano.ui.carrito.MetodoPago
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PedidoLocalTest {
    @Test
    fun confirmarWhatsAppGuardaElResumenYVaciaElCarritoUnaVez() {
        val instrumentacion = InstrumentationRegistry.getInstrumentation()
        val contexto = instrumentacion.targetContext
        val archivo = "pedido-local-test-${UUID.randomUUID()}"
        val preferencias = contexto.getSharedPreferences(archivo, Context.MODE_PRIVATE)

        try {
            instrumentacion.runOnMainSync {
                val carrito = CarritoRepository()
                val viewModel = ConfirmarPedidoViewModel(
                    carrito, PedidoRepository(habilitado = false), "uid-test", PreferenciasCliente(preferencias)
                )
                assertTrue(carrito.agregar(Item("aji", "Ají", precioCentimos = 1800), 2))
                viewModel.nombre("Cliente de prueba")
                viewModel.telefono("999111222")
                viewModel.metodoPago(MetodoPago.YAPE)
                viewModel.confirmarEnvioWhatsApp()
                viewModel.confirmarEnvioWhatsApp()

                val recibo = viewModel.envio.value as EnvioPedido.GuardadoLocal
                assertEquals(3600L, recibo.resumen.totalCentimos)
                assertTrue(carrito.lineas.value.isEmpty())
                val aviso = PreferenciasCliente(preferencias).actividad.value.avisos.single()
                assertEquals(TipoAvisoCliente.PEDIDO_WHATSAPP, aviso.tipo)
                assertTrue(aviso.plato.orEmpty().contains("2 × Ají"))
            }
        } finally {
            contexto.deleteSharedPreferences(archivo)
        }
    }
}
