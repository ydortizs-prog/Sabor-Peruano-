package com.tecsup.saborperuano.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.data.auth.AuthRepository
import com.tecsup.saborperuano.data.auth.CuentaYaRegistradaException
import com.tecsup.saborperuano.data.auth.PerfilSinRolException
import com.tecsup.saborperuano.domain.model.Sesion
import com.tecsup.saborperuano.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal sealed interface EstadoRecuperacion {
    data object Inicial : EstadoRecuperacion
    data object Enviando : EstadoRecuperacion
    data object Solicitado : EstadoRecuperacion
    data class Error(val mensaje: Int) : EstadoRecuperacion
}

private val formatoCorreo = Regex("""[^\s@]+@[^\s@]+\.[^\s@]+""")

class LoginViewModel(private val repositorio: AuthRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Cargando)
    val estado: StateFlow<UiState<Sesion?>> = _estado.asStateFlow()
    private var operacion: Job? = null
    private val _recuperacion = MutableStateFlow<EstadoRecuperacion>(EstadoRecuperacion.Inicial)
    internal val recuperacion = _recuperacion.asStateFlow()
    private val _registro = MutableStateFlow<UiState<Unit>>(UiState.Exito(Unit))
    internal val registro = _registro.asStateFlow()

    init {
        restaurarSesion()
    }

    fun restaurarSesion() = actualizarSesion { repositorio.sesionActual() }

    fun iniciarSesion(correo: String, clave: String) {
        if (operacion?.isActive == true) return
        if (correo.isBlank() || clave.isBlank()) {
            _estado.value = UiState.Error("Completa el correo y la contraseña.")
            return
        }
        actualizarSesion { repositorio.iniciarSesion(correo.trim(), clave).getOrThrow() }
    }

    internal fun limpiarRegistro() {
        if (_registro.value != UiState.Cargando) _registro.value = UiState.Exito(Unit)
    }

    internal fun registrar(nombre: String, correo: String, clave: String, confirmacion: String) {
        if (operacion?.isActive == true) return
        val nombreLimpio = nombre.trim()
        val direccion = correo.trim()
        val error = when {
            nombreLimpio.isEmpty() || nombreLimpio.length > 80 -> "Escribe un nombre de hasta 80 caracteres."
            !formatoCorreo.matches(direccion) -> "Escribe un correo electrónico válido."
            clave.length < 6 -> "Usa una contraseña de al menos 6 caracteres."
            clave != confirmacion -> "Las contraseñas deben coincidir."
            else -> null
        }
        if (error != null) {
            _registro.value = UiState.Error(error)
            return
        }

        operacion = viewModelScope.launch {
            _registro.value = UiState.Cargando
            try {
                val sesion = repositorio.registrar(nombreLimpio, direccion, clave)
                _registro.value = UiState.Exito(Unit)
                _estado.value = UiState.Exito(sesion)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _registro.value = UiState.Error(mensajeDeRegistro(error))
            }
        }
    }

    private fun mensajeDeRegistro(error: Exception): String = when (error) {
        is CuentaYaRegistradaException -> "La cuenta ya está registrada. Puedes iniciar sesión."
        is FirebaseAuthWeakPasswordException -> "La contraseña no cumple los requisitos de seguridad."
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
            "No se pudo registrar con estos datos. Si ya tienes una cuenta, inicia sesión."
        is FirebaseNetworkException -> "Revisa tu conexión y vuelve a intentar el registro."
        is FirebaseFirestoreException -> if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            "El registro no está habilitado en el servidor. Inténtalo más tarde."
        } else {
            "No se pudo guardar tu perfil. Vuelve a intentar el registro con el mismo correo y contraseña."
        }
        else -> "No se pudo completar el registro. Inténtalo de nuevo."
    }

    internal fun solicitarRestablecimiento(correo: String) {
        if (_recuperacion.value == EstadoRecuperacion.Enviando) return
        val direccion = correo.trim()
        if (!formatoCorreo.matches(direccion)) {
            _recuperacion.value = EstadoRecuperacion.Error(R.string.login_recuperar_correo_invalido)
            return
        }

        _recuperacion.value = EstadoRecuperacion.Enviando
        viewModelScope.launch {
            _recuperacion.value = try {
                repositorio.enviarRestablecimiento(direccion)
                EstadoRecuperacion.Solicitado
            } catch (error: CancellationException) {
                throw error
            } catch (_: FirebaseAuthInvalidUserException) {
                EstadoRecuperacion.Solicitado
            } catch (_: FirebaseNetworkException) {
                EstadoRecuperacion.Error(R.string.login_recuperar_sin_conexion)
            } catch (_: Exception) {
                EstadoRecuperacion.Error(R.string.login_recuperar_error)
            }
        }
    }

    internal fun limpiarRecuperacion() {
        if (_recuperacion.value != EstadoRecuperacion.Enviando) {
            _recuperacion.value = EstadoRecuperacion.Inicial
        }
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        _estado.value = UiState.Exito(null)
    }

    private fun actualizarSesion(accion: suspend () -> Sesion?) {
        if (operacion?.isActive == true) return
        operacion = viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = try {
                UiState.Exito(accion())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                UiState.Error(mensajeDe(error))
            }
        }
    }

    private fun mensajeDe(error: Exception): String = when (error) {
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
            "El correo o la contraseña no son válidos."
        is PerfilSinRolException -> "Tu cuenta todavía no tiene un rol válido en Sabor Peruano."
        is FirebaseNetworkException -> "Revisa tu conexión e inténtalo de nuevo."
        is FirebaseFirestoreException -> "No se pudo consultar el rol. Verifica la conexión y el acceso al perfil."
        else -> "No se pudo iniciar o recuperar la sesión."
    }

    class Factory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == LoginViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(appContainer.authRepository) as T
        }
    }
}
