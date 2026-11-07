package com.tecsup.saborperuano.ui.lista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.components.Cargando
import com.tecsup.saborperuano.ui.components.ErrorView
import com.tecsup.saborperuano.ui.components.TarjetaPlato

@Composable
fun ListaScreen(
    viewModel: ListaViewModel,
    onAbrirDetalle: (String) -> Unit,
    onAbrirFavoritos: () -> Unit,
    onAbrirCarrito: () -> Unit,
    unidadesCarrito: Int,
    onCerrarSesion: () -> Unit,
    onAbrirAdmin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.catalogo.collectAsStateWithLifecycle()
    val sinConexion by viewModel.sinConexion.collectAsStateWithLifecycle()
    val consulta by viewModel.consulta.collectAsStateWithLifecycle()
    val categoria by viewModel.categoria.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val actual = estado) {
            UiState.Cargando, is UiState.Error -> Column(Modifier.padding(16.dp)) {
                EncabezadoCatalogo(onAbrirFavoritos, onAbrirCarrito, unidadesCarrito, onCerrarSesion, onAbrirAdmin)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (actual is UiState.Error) {
                        ErrorView(actual.mensaje, viewModel::refrescar)
                    } else {
                        Cargando()
                    }
                }
            }
            is UiState.Exito -> LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item(key = "encabezado") {
                    Column(
                        modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EncabezadoCatalogo(
                            onAbrirFavoritos, onAbrirCarrito, unidadesCarrito, onCerrarSesion, onAbrirAdmin
                        )
                        OutlinedTextField(
                            value = consulta,
                            onValueChange = viewModel::buscar,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.catalogo_buscar)) },
                            shape = MaterialTheme.shapes.large,
                            singleLine = true
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Categoria.entries, key = { it.name }) { opcion ->
                                FilterChip(
                                    selected = categoria == opcion,
                                    onClick = { viewModel.seleccionarCategoria(opcion) },
                                    label = { Text(stringResource(opcion.etiqueta)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                        if (sinConexion) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(stringResource(R.string.catalogo_sin_conexion))
                                    TextButton(onClick = viewModel::refrescar) {
                                        Text(stringResource(R.string.catalogo_reintentar))
                                    }
                                }
                            }
                        }
                    }
                }
                if (actual.datos.platos.isEmpty()) {
                    item(key = "vacio") {
                        Text(stringResource(
                            if (actual.datos.catalogoVacio) R.string.catalogo_vacio
                            else R.string.catalogo_sin_resultados
                        ))
                    }
                }
                items(actual.datos.platos, key = { it.id }) { plato ->
                    TarjetaPlato(
                        plato = plato,
                        onAbrirDetalle = onAbrirDetalle,
                        modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EncabezadoCatalogo(
    onAbrirFavoritos: () -> Unit,
    onAbrirCarrito: () -> Unit,
    unidadesCarrito: Int,
    onCerrarSesion: () -> Unit,
    onAbrirAdmin: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(stringResource(R.string.catalogo_marca), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.catalogo_bajada), style = MaterialTheme.typography.bodyMedium)
            FlowRow {
                val colores = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                TextButton(onClick = onAbrirFavoritos, colors = colores) {
                    Text(stringResource(R.string.favoritos_titulo))
                }
                TextButton(onClick = onAbrirCarrito, colors = colores) {
                    Text(stringResource(R.string.carrito_acceso, unidadesCarrito))
                }
                if (onAbrirAdmin != null) {
                    TextButton(onClick = onAbrirAdmin, colors = colores) {
                        Text(stringResource(R.string.admin_panel))
                    }
                }
                TextButton(onClick = onCerrarSesion, colors = colores) {
                    Text(stringResource(R.string.login_salir))
                }
            }
        }
    }
}
