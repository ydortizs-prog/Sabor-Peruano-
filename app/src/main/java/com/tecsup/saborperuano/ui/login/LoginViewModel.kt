package com.tecsup.saborperuano.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.auth.AuthRepository
import com.tecsup.saborperuano.data.auth.PerfilSinRolException
import com.tecsup.saborperuano.domain.model.Sesion
import com.tecsup.saborperuano.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repositorio: AuthRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Cargando)
    val estado: StateFlow<UiState<Sesion?>> = _estado.asStateFlow()
    private var operacion: Job? = null

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
