package com.tecsup.saborperuano.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tecsup.saborperuano.AppContainer
import com.tecsup.saborperuano.ui.detalle.DetalleScreen
import com.tecsup.saborperuano.ui.detalle.DetalleViewModel
import com.tecsup.saborperuano.ui.lista.ListaScreen
import com.tecsup.saborperuano.ui.lista.ListaViewModel
import kotlinx.serialization.Serializable

@Serializable
private data object DestinoLista

@Serializable
private data class DestinoDetalle(val id: String)

@Composable
fun AppNav(appContainer: AppContainer, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = DestinoLista, modifier = modifier) {
        composable<DestinoLista> {
            ListaScreen(
                viewModel = viewModel(factory = ListaViewModel.Factory(appContainer)),
                onAbrirDetalle = { id -> navController.navigate(DestinoDetalle(id)) }
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
