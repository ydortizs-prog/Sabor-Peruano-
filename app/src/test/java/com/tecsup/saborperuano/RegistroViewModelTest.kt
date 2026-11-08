package com.tecsup.saborperuano

import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.data.auth.AuthRepository
import com.tecsup.saborperuano.domain.model.Rol
import com.tecsup.saborperuano.domain.model.Sesion
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.login.LoginViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {
    private val perfil = Sesion("uid-test", "cliente@example.invalid", Rol.USUARIO, "Cliente de prueba")
    private val repositorio = AuthFalso()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = LoginViewModel(repositorio)
    }

    @After
    fun limpiar() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun clavesDistintasNoIntentanCrearLaCuenta() = runTest {
        runCurrent()
        viewModel.registrar("Cliente", perfil.correo, "prueba123", "otraClave")
        runCurrent()

        assertTrue(viewModel.registro.value is UiState.Error)
        assertEquals(0, repositorio.intentos)
        assertEquals(UiState.Exito(null), viewModel.estado.value)
    }

    @Test
    fun elRegistroIniciaSesionSoloDespuesDeConfirmarElPerfil() = runTest {
        runCurrent()
        viewModel.registrar("Cliente de prueba", perfil.correo, "prueba123", "prueba123")
        runCurrent()

        assertEquals(UiState.Cargando, viewModel.registro.value)
        assertEquals(UiState.Exito(null), viewModel.estado.value)

        repositorio.respuesta.complete(perfil)
        runCurrent()

        assertEquals(UiState.Exito(perfil), viewModel.estado.value)
        assertEquals(1, repositorio.intentos)
    }

    private class AuthFalso : AuthRepository {
        val respuesta = CompletableDeferred<Sesion>()
        var intentos = 0

        override suspend fun registrar(nombre: String, correo: String, clave: String): Sesion {
            intentos++
            return respuesta.await()
        }

        override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> =
            error("Fuera del flujo de registro")

        override suspend fun enviarRestablecimiento(correo: String) = Unit
        override suspend fun sesionActual(): Sesion? = null
        override suspend fun confirmarAdministrador(uid: String): Boolean = false
        override fun cerrarSesion() = Unit
    }
}
