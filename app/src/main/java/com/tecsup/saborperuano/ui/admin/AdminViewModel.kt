package com.tecsup.saborperuano.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.data.auth.AuthRepository
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.notification.Notificador
import com.tecsup.saborperuano.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResumenAdmin(val guardados: Int, val favoritos: Int, val mensaje: String? = null)

class AdminViewModel(
    private val uid: String,
    private val auth: AuthRepository,
    private val items: ItemRepository,
    private val notificador: Notificador
) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<ResumenAdmin>>(UiState.Cargando)
    val estado: StateFlow<UiState<ResumenAdmin>> = _estado.asStateFlow()
    private var operacion: Job? = null

    init {
        cargar()
    }

    fun cargar() = ejecutar { null }

    fun sincronizar() = ejecutar {
        val cantidad = items.refrescar().getOrThrow()
        val mensaje = "Se guardaron $cantidad elementos."
        notificador.mostrar("Catálogo actualizado", mensaje)
        mensaje
    }

    fun limpiarCache() = ejecutar {
        items.limpiarCache()
        "Se limpió el catálogo guardado."
    }

    private fun ejecutar(accion: suspend () -> String?) {
        if (operacion?.isActive == true) return

        operacion = viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = try {
                if (auth.confirmarAdministrador(uid)) {
                    val mensaje = accion()
                    UiState.Exito(
                        ResumenAdmin(items.cantidadGuardada(), items.cantidadFavoritos(), mensaje)
                    )
                } else {
                    UiState.Error("Esta sección requiere una cuenta administradora.")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                UiState.Error("No se pudo completar la operación. Revisa la conexión y el acceso.")
            }
        }
    }

    class Factory(private val container: AppContainer, private val uid: String) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == AdminViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(uid, container.authRepository, container.itemRepository, container.notificador) as T
        }
    }
}
