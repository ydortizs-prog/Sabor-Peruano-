package com.tecsup.saborperuano.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.ui.detalle.DetalleScreen
import com.tecsup.saborperuano.ui.detalle.DetalleViewModel
import com.tecsup.saborperuano.ui.favoritos.FavoritosScreen
import com.tecsup.saborperuano.ui.favoritos.FavoritosViewModel
import com.tecsup.saborperuano.ui.lista.ListaScreen
import com.tecsup.saborperuano.ui.lista.ListaViewModel
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.login.LoginScreen
import com.tecsup.saborperuano.ui.login.LoginViewModel
import kotlinx.serialization.Serializable

@Serializable
private data object DestinoLista

@Serializable
private data object DestinoFavoritos

@Serializable
private data class DestinoDetalle(val id: String)

@Composable
fun AppNav(appContainer: AppContainer, modifier: Modifier = Modifier) {
    val login: LoginViewModel = viewModel(factory = LoginViewModel.Factory(appContainer))
    val estado by login.estado.collectAsStateWithLifecycle()
    val sesion = (estado as? UiState.Exito)?.datos

    if (sesion == null) {
        LoginScreen(login, modifier)
    } else {
        CatalogoNav(appContainer, login::cerrarSesion, modifier)
    }
}

@Composable
private fun CatalogoNav(
    appContainer: AppContainer,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val abrirDetalle: (String) -> Unit = { id -> navController.navigate(DestinoDetalle(id)) }

    NavHost(navController, startDestination = DestinoLista, modifier = modifier) {
        composable<DestinoLista> {
            ListaScreen(
                viewModel = viewModel(factory = ListaViewModel.Factory(appContainer)),
                onAbrirDetalle = abrirDetalle,
                onAbrirFavoritos = { navController.navigate(DestinoFavoritos) },
                onCerrarSesion = onCerrarSesion
            )
        }

        composable<DestinoFavoritos> {
            FavoritosScreen(
                viewModel = viewModel(factory = FavoritosViewModel.Factory(appContainer)),
                onAbrirDetalle = abrirDetalle,
                onVolver = { navController.navigateUp() }
            )
        }

        composable<DestinoDetalle> { entrada ->
            val id = entrada.toRoute<DestinoDetalle>().id
            DetalleScreen(
                viewModel = viewModel(factory = DetalleViewModel.Factory(appContainer, id)),
                onVolver = { navController.navigateUp() }
            )
        }
    }
}
