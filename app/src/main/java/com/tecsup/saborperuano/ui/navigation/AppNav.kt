package com.tecsup.saborperuano.ui.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.domain.model.Rol
import com.tecsup.saborperuano.domain.model.Sesion
import com.tecsup.saborperuano.ui.carrito.CarritoScreen
import com.tecsup.saborperuano.ui.carrito.CarritoViewModel
import com.tecsup.saborperuano.ui.admin.AdminScreen
import com.tecsup.saborperuano.ui.admin.AdminViewModel
import com.tecsup.saborperuano.ui.detalle.DetalleScreen
import com.tecsup.saborperuano.ui.detalle.DetalleViewModel
import com.tecsup.saborperuano.ui.favoritos.FavoritosScreen
import com.tecsup.saborperuano.ui.favoritos.FavoritosViewModel
import com.tecsup.saborperuano.ui.lista.ListaScreen
import com.tecsup.saborperuano.ui.lista.ListaViewModel
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
private data object DestinoAdmin

@Serializable
private data class DestinoDetalle(val id: String)

@Composable
fun AppNav(appContainer: AppContainer, modifier: Modifier = Modifier) {
    val login: LoginViewModel = viewModel(factory = LoginViewModel.Factory(appContainer))
    val estado by login.estado.collectAsStateWithLifecycle()
    val sesion = (estado as? UiState.Exito)?.datos

    if (sesion == null) {
        LoginScreen(login, modifier)
    } else if (PermisoNotificacionesResuelto()) {
        TemaCatalogo {
            key(sesion.uid) {
                CatalogoNav(appContainer, sesion, login::cerrarSesion, modifier)
            }
        }
    }
}

@Composable
private fun CatalogoNav(
    appContainer: AppContainer,
    sesion: Sesion,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val repositorioCarrito = remember { appContainer.crearCarritoDeSesion() }
    val lineas by repositorioCarrito.lineas.collectAsStateWithLifecycle()
    val abrirCarrito: () -> Unit = {
        navController.navigate(DestinoCarrito) { launchSingleTop = true }
    }
    val abrirDetalle: (String) -> Unit = { id -> navController.navigate(DestinoDetalle(id)) }

    NavHost(navController, startDestination = DestinoLista, modifier = modifier) {
        composable<DestinoLista> {
            ListaScreen(
                viewModel = viewModel(factory = ListaViewModel.Factory(appContainer)),
                onAbrirDetalle = abrirDetalle,
                onAbrirFavoritos = { navController.navigate(DestinoFavoritos) },
                onAbrirCarrito = abrirCarrito,
                unidadesCarrito = lineas.sumOf { it.cantidad },
                onCerrarSesion = onCerrarSesion,
                onAbrirAdmin = if (sesion.rol == Rol.ADMIN) {
                    { navController.navigate(DestinoAdmin) }
                } else null
            )
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
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable<DestinoCarrito> {
            CarritoScreen(
                viewModel = viewModel(factory = CarritoViewModel.Factory(repositorioCarrito)),
                onVolver = { navController.navigateUp() }
            )
        }

        composable<DestinoDetalle> { entrada ->
            val id = entrada.toRoute<DestinoDetalle>().id
            DetalleScreen(
                viewModel = viewModel(
                    factory = DetalleViewModel.Factory(appContainer, id, repositorioCarrito)
                ),
                onAbrirCarrito = abrirCarrito,
                onVolver = { navController.navigateUp() }
            )
        }
    }
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
