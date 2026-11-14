package com.tecsup.saborperuano.ui.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.data.local.AvisoGuardado
import com.tecsup.saborperuano.data.local.TipoAvisoCliente
import com.tecsup.saborperuano.ui.lista.AvisoCliente
import com.tecsup.saborperuano.ui.lista.BarraUsuario
import com.tecsup.saborperuano.domain.model.Rol
import com.tecsup.saborperuano.domain.model.Sesion
import com.tecsup.saborperuano.ui.carrito.CarritoScreen
import com.tecsup.saborperuano.ui.carrito.CarritoViewModel
import com.tecsup.saborperuano.ui.carrito.ConfirmarPedidoScreen
import com.tecsup.saborperuano.ui.carrito.ConfirmarPedidoViewModel
import com.tecsup.saborperuano.ui.admin.AdminScreen
import com.tecsup.saborperuano.ui.admin.AdminViewModel
import com.tecsup.saborperuano.ui.components.Cargando
import com.tecsup.saborperuano.ui.detalle.DetalleScreen
import com.tecsup.saborperuano.ui.detalle.DetalleViewModel
import com.tecsup.saborperuano.ui.favoritos.FavoritosScreen
import com.tecsup.saborperuano.ui.favoritos.FavoritosViewModel
import com.tecsup.saborperuano.ui.lista.ListaScreen
import com.tecsup.saborperuano.ui.lista.ListaViewModel
import com.tecsup.saborperuano.ui.lista.PerfilUsuarioScreen
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.login.LoginScreen
import com.tecsup.saborperuano.ui.login.LoginViewModel
import com.tecsup.saborperuano.ui.theme.TemaCatalogo
import kotlinx.serialization.Serializable

@Serializable
private data object DestinoLista

@Serializable
private data object DestinoFavoritos

@Serializable
private data object DestinoCarrito

@Serializable
private data object DestinoPerfil

@Serializable
private data object DestinoConfirmarPedido

@Serializable
private data object DestinoAdmin

@Serializable
private data class DestinoDetalle(val id: String)

@Composable
fun AppNav(appContainer: AppContainer, modifier: Modifier = Modifier) {
    val login: LoginViewModel = viewModel(factory = LoginViewModel.Factory(appContainer))
    val estado by login.estado.collectAsStateWithLifecycle()
    val sesion = (estado as? UiState.Exito)?.datos
    var mostrarWelcome by rememberSaveable { mutableStateOf(true) }
    var esperandoSesionInicial by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(estado) {
        if (estado != UiState.Cargando) esperandoSesionInicial = false
    }

    if (mostrarWelcome) {
        WelcomeScreen(onFinalizar = { mostrarWelcome = false }, modifier = modifier)
    } else if (esperandoSesionInicial && estado == UiState.Cargando) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Cargando()
        }
    } else if (sesion == null) {
        LoginScreen(login, modifier)
    } else if (PermisoNotificacionesResuelto()) {
        key(sesion.uid) {
            val estadoSesion: SesionNavegacionViewModel = viewModel(
                key = "sesion-navegacion-${sesion.uid}",
                factory = SesionNavegacionViewModel.Factory(appContainer, sesion.uid)
            )
            val preferencias = estadoSesion.preferencias
            val tema = preferencias.temaOscuro.collectAsStateWithLifecycle().value ?: isSystemInDarkTheme()
            TemaCatalogo(oscuro = tema) {
                Surface(
                    modifier = modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CatalogoNav(
                        appContainer = appContainer,
                        sesion = sesion,
                        onCerrarSesion = {
                            estadoSesion.limpiarSesion()
                            login.cerrarSesion()
                        },
                        modifier = Modifier.fillMaxSize(),
                        estadoSesion = estadoSesion,
                        temaOscuro = tema
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogoNav(
    appContainer: AppContainer,
    sesion: Sesion,
    onCerrarSesion: () -> Unit,
    modifier: Modifier,
    estadoSesion: SesionNavegacionViewModel,
    temaOscuro: Boolean
) {
    val navController = rememberNavController()
    val repositorioCarrito = estadoSesion.carrito
    val preferencias = estadoSesion.preferencias
    val lineas by repositorioCarrito.lineas.collectAsStateWithLifecycle()
    val abrirCarrito: () -> Unit = {
        navController.navigate(DestinoCarrito) { launchSingleTop = true }
    }
    val abrirDetalle: (String) -> Unit = { id -> navController.navigate(DestinoDetalle(id)) }
    val esCliente = sesion.rol == Rol.USUARIO
    val valoraciones = if (esCliente) preferencias.valoraciones.collectAsStateWithLifecycle().value
        else emptyMap()
    val actividad = preferencias.actividad.collectAsStateWithLifecycle()
    val avisarFavorito: (String) -> Unit = { nombre ->
        if (esCliente) preferencias.agregarAviso(TipoAvisoCliente.FAVORITO, nombre)
    }

    val destinoInicial: Any = DestinoLista

    NavHost(navController, startDestination = destinoInicial, modifier = modifier) {
        composable<DestinoLista> {
            ListaScreen(
                viewModel = viewModel(factory = ListaViewModel.Factory(appContainer)),
                onAbrirDetalle = abrirDetalle,
                onAbrirFavoritos = { navController.navigate(DestinoFavoritos) },
                onAbrirCarrito = abrirCarrito,
                unidadesCarrito = lineas.sumOf { it.cantidad },
                onCerrarSesion = onCerrarSesion,
                onAbrirAdmin = if (sesion.rol == Rol.ADMIN) {
                    {
                        navController.navigate(DestinoAdmin) {
                            popUpTo<DestinoAdmin>()
                            launchSingleTop = true
                        }
                    }
                } else null,
                esCliente = esCliente,
                onAbrirCuenta = { navController.navigate(DestinoPerfil) { launchSingleTop = true } },
                onFavoritoAgregado = avisarFavorito,
                valoraciones = valoraciones,
                onCalificar = if (esCliente) preferencias::calificar else null,
                avisos = actividad.value.avisos.mapIndexedNotNull { indice, aviso ->
                    if (aviso.oculto) null else aviso.aAvisoVisible().copy(indice = indice)
                },
                avisosSinLeer = actividad.value.sinLeer,
                avisosNoLeidos = actividad.value.noLeidos,
                onLeerAvisos = preferencias::marcarAvisosLeidos,
                onBorrarAviso = preferencias::ocultarAviso,
                onBorrarAvisos = preferencias::ocultarTodosLosAvisos
            )
        }

        if (esCliente) {
            composable<DestinoPerfil> {
                PerfilUsuarioScreen(
                    sesion = sesion,
                    temaOscuro = temaOscuro,
                    onCambiarTema = preferencias::cambiarTema,
                    pedidos = actividad.value.avisos.filter {
                        it.tipo == TipoAvisoCliente.WHATSAPP || it.tipo == TipoAvisoCliente.PEDIDO_GUARDADO ||
                            it.tipo == TipoAvisoCliente.PEDIDO_WHATSAPP
                    }.map(AvisoGuardado::aAvisoVisible),
                    onCerrarSesion = onCerrarSesion
                ) {
                    BarraUsuario(
                        unidades = lineas.sumOf { it.cantidad },
                        onAbrirFavoritos = { navController.navigate(DestinoFavoritos) },
                        onAbrirCarrito = abrirCarrito,
                        onAbrirCuenta = {},
                        perfilSeleccionado = true,
                        onAbrirInicio = { navController.navigateUp() }
                    )
                }
            }
        }

        composable<DestinoFavoritos> {
            FavoritosScreen(
                viewModel = viewModel(factory = FavoritosViewModel.Factory(appContainer)),
                onAbrirDetalle = abrirDetalle,
                onVolver = { navController.navigateUp() }
            )
        }

        if (sesion.rol == Rol.ADMIN) {
            composable<DestinoAdmin> {
                AdminScreen(
                    viewModel = viewModel(factory = AdminViewModel.Factory(appContainer, sesion.uid)),
                    onVolver = { navController.navigateUp() },
                    perfil = { onAbrirPedidos ->
                        PerfilUsuarioScreen(
                            sesion = sesion,
                            temaOscuro = temaOscuro,
                            onCambiarTema = preferencias::cambiarTema,
                            onCerrarSesion = onCerrarSesion,
                            onAbrirPedidosAdmin = onAbrirPedidos
                        )
                    }
                )
            }
        }

        composable<DestinoCarrito> {
            CarritoScreen(
                viewModel = viewModel(factory = CarritoViewModel.Factory(repositorioCarrito)),
                onVolver = { navController.navigateUp() },
                onRevisarPedido = {
                    navController.navigate(DestinoConfirmarPedido) { launchSingleTop = true }
                }
            )
        }

        composable<DestinoConfirmarPedido> {
            ConfirmarPedidoScreen(
                viewModel = viewModel(factory = ConfirmarPedidoViewModel.Factory(
                    repositorioCarrito, appContainer.pedidoRepository, sesion.uid, preferencias
                )),
                onVolver = { navController.navigateUp() },
                onAbrirWhatsApp = {
                    if (esCliente) preferencias.agregarAviso(TipoAvisoCliente.WHATSAPP)
                },
                onPedidoGuardado = { id ->
                    if (esCliente && estadoSesion.registrarPedido(id)) {
                        preferencias.agregarAviso(TipoAvisoCliente.PEDIDO_GUARDADO)
                    }
                }
            )
        }

        composable<DestinoDetalle> { entrada ->
            val id = entrada.toRoute<DestinoDetalle>().id
            DetalleScreen(
                viewModel = viewModel(
                    factory = DetalleViewModel.Factory(appContainer, id, repositorioCarrito)
                ),
                onAbrirCarrito = abrirCarrito,
                onVolver = { navController.navigateUp() },
                onFavoritoAgregado = avisarFavorito,
                onPlatoAgregado = { nombre ->
                    if (esCliente) preferencias.agregarAviso(TipoAvisoCliente.CARRITO, nombre)
                }
            )
        }
    }
}


private fun AvisoGuardado.aAvisoVisible(): AvisoCliente {
    val mensaje = when (tipo) {
        TipoAvisoCliente.FAVORITO -> R.string.catalogo_aviso_favorito
        TipoAvisoCliente.CARRITO -> R.string.catalogo_aviso_agregado
        TipoAvisoCliente.WHATSAPP -> R.string.catalogo_aviso_whatsapp
        TipoAvisoCliente.PEDIDO_GUARDADO -> R.string.catalogo_aviso_pedido
        TipoAvisoCliente.PEDIDO_WHATSAPP -> R.string.pedido_aviso_whatsapp_guardado
    }
    return AvisoCliente(mensaje, plato, tipo, fecha)
}

@Composable
private fun PermisoNotificacionesResuelto(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

    val context = LocalContext.current
    val preferencias = context.getSharedPreferences("catalogo", Context.MODE_PRIVATE)
    val clave = "permiso_notificaciones_solicitado"
    var resuelto by rememberSaveable {
        mutableStateOf(
            preferencias.getBoolean(clave, false) ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var solicitado by rememberSaveable { mutableStateOf(false) }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        preferencias.edit().putBoolean(clave, true).apply()
        resuelto = true
    }

    LaunchedEffect(resuelto) {
        if (!resuelto && !solicitado) {
            solicitado = true
            permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    return resuelto
}
