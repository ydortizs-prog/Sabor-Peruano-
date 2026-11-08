package com.tecsup.saborperuano.ui.admin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.data.auth.AuthRepository
import com.tecsup.saborperuano.data.repository.DatosPlatilloLocal
import com.tecsup.saborperuano.data.repository.ItemRepository
import com.tecsup.saborperuano.data.repository.MenuLocalRepository
import com.tecsup.saborperuano.notification.Notificador
import com.tecsup.saborperuano.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResumenAdmin(val guardados: Int, val favoritos: Int, val mensaje: String? = null)

internal data class AvisosAdmin(val eventos: List<Int> = emptyList(), val sinLeer: Boolean = false)

internal data class EdicionMenu(
    val guardando: Boolean = false,
    val error: String? = null,
    val revision: Int = 0
)

class AdminViewModel internal constructor(
    private val uid: String,
    private val auth: AuthRepository,
    private val items: ItemRepository,
    private val notificador: Notificador,
    private val menuLocal: MenuLocalRepository
) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<ResumenAdmin>>(UiState.Cargando)
    val estado: StateFlow<UiState<ResumenAdmin>> = _estado.asStateFlow()
    internal val catalogo = items.items
    private val _avisos = MutableStateFlow(AvisosAdmin())
    internal val avisos = _avisos.asStateFlow()

    internal fun leerAvisos() {
        _avisos.update { it.copy(sinLeer = false) }
    }
    private var operacion: Job? = null
    private val _edicionMenu = MutableStateFlow(EdicionMenu())
    internal val edicionMenu = _edicionMenu.asStateFlow()

    internal fun iniciarEdicionMenu() {
        _edicionMenu.update { it.copy(error = null) }
    }

    internal fun guardarPlato(id: String?, datos: DatosPlatilloLocal) =
        ejecutar(R.string.admin_aviso_plato_guardado, edicion = true) {
            menuLocal.guardar(id, datos)
            null
        }

    internal fun retirarPlato(id: String) =
        ejecutar(R.string.admin_aviso_plato_retirado, edicion = true) {
            menuLocal.retirar(id)
            null
        }

    init {
        cargar()
    }

    fun cargar() = ejecutar {
        if (items.cantidadGuardada() == 0 && items.refrescar().isFailure) {
            "No se pudo cargar el menú. Puedes volver a sincronizar el catálogo."
        } else {
            null
        }
    }

    fun sincronizar() = ejecutar(R.string.admin_aviso_sincronizado) {
        val cantidad = items.refrescar().getOrThrow()
        val mensaje = "Se guardaron $cantidad elementos."
        notificador.mostrar("Catálogo actualizado", mensaje)
        mensaje
    }

    fun limpiarCache() = ejecutar(R.string.admin_aviso_cache) {
        items.limpiarCache()
        "Se limpió el catálogo guardado."
    }

    private fun ejecutar(
        @StringRes aviso: Int? = null,
        edicion: Boolean = false,
        accion: suspend () -> String?
    ) {
        if (operacion?.isActive == true) return

        operacion = viewModelScope.launch {
            if (edicion) _edicionMenu.update { it.copy(guardando = true, error = null) }
            else _estado.value = UiState.Cargando
            val resultado = try {
                if (auth.confirmarAdministrador(uid)) {
                    val mensaje = accion()
                    val resumen = ResumenAdmin(items.cantidadGuardada(), items.cantidadFavoritos(), mensaje)
                    if (aviso != null) {
                        _avisos.update { AvisosAdmin((listOf(aviso) + it.eventos).take(12), sinLeer = true) }
                    }
                    UiState.Exito(resumen)
                } else {
                    UiState.Error("Esta sección requiere una cuenta administradora.")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                UiState.Error("No se pudo completar la operación. Revisa la conexión y el acceso.")
            }
            if (edicion) {
                _edicionMenu.update {
                    if (resultado is UiState.Error) it.copy(guardando = false, error = resultado.mensaje)
                    else EdicionMenu(revision = it.revision + 1)
                }
            }
            if (!edicion || resultado is UiState.Exito) _estado.value = resultado
        }
    }

    class Factory(private val container: AppContainer, private val uid: String) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == AdminViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(
                uid, container.authRepository, container.itemRepository, container.notificador, container.menuLocal
            ) as T
        }
    }
}
