package com.tecsup.saborperuano

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.carrito.ConfirmarPedidoViewModel
import com.tecsup.saborperuano.ui.navigation.SesionNavegacionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RotacionSesionTest {
    @Test
    fun conservaCarritoYFormularioAlGirarLaActividad() {
        val uid = "rotacion-prueba"
        lateinit var carritoAntes: SesionNavegacionViewModel
        lateinit var formularioAntes: ConfirmarPedidoViewModel

        ActivityScenario.launch(MainActivity::class.java).use { escenario ->
            escenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            esperarOrientacion(escenario, Configuration.ORIENTATION_PORTRAIT)
            escenario.onActivity { actividad ->
                val container = AppContainer.obtener(actividad.applicationContext)
                carritoAntes = ViewModelProvider(actividad, SesionNavegacionViewModel.Factory(container, uid))
                    .get("sesion-navegacion-$uid", SesionNavegacionViewModel::class.java)
                formularioAntes = formulario(actividad, container, carritoAntes, uid)
                assertTrue(carritoAntes.carrito.agregar(Item("aji", "Ají", precioCentimos = 1800), 2))
                formularioAntes.nombre("Cliente de prueba")
                formularioAntes.telefono("999111222")
                actividad.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            esperarOrientacion(escenario, Configuration.ORIENTATION_LANDSCAPE)
            escenario.onActivity { actividad ->
                val container = AppContainer.obtener(actividad.applicationContext)
                val carrito = ViewModelProvider(actividad, SesionNavegacionViewModel.Factory(container, uid))
                    .get("sesion-navegacion-$uid", SesionNavegacionViewModel::class.java)
                val formulario = formulario(actividad, container, carrito, uid)
                assertSame(carritoAntes, carrito)
                assertSame(formularioAntes, formulario)
                assertEquals(2, carrito.carrito.lineas.value.single().cantidad)
                assertEquals("Cliente de prueba", formulario.datos.value.nombre)
                assertEquals("999111222", formulario.datos.value.telefono)
                actividad.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun formulario(
        actividad: MainActivity,
        container: AppContainer,
        estado: SesionNavegacionViewModel,
        uid: String
    ) = ViewModelProvider(
        actividad,
        ConfirmarPedidoViewModel.Factory(estado.carrito, container.pedidoRepository, uid, estado.preferencias)
    )[ConfirmarPedidoViewModel::class.java]

    private fun esperarOrientacion(escenario: ActivityScenario<MainActivity>, esperada: Int) {
        val limite = SystemClock.uptimeMillis() + 8_000
        do {
            var actual = Configuration.ORIENTATION_UNDEFINED
            escenario.onActivity { actual = it.resources.configuration.orientation }
            if (actual == esperada) return
            SystemClock.sleep(100)
        } while (SystemClock.uptimeMillis() < limite)
        throw AssertionError("Android no aplicó la orientación $esperada")
    }
}
