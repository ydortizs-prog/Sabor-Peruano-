package com.tecsup.saborperuano.ui.lista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.components.Cargando
import com.tecsup.saborperuano.ui.components.ErrorView
import com.tecsup.saborperuano.ui.components.TarjetaPlato

@Composable
internal fun ListaScreen(
    viewModel: ListaViewModel,
    onAbrirDetalle: (String) -> Unit,
    onAbrirFavoritos: () -> Unit,
    onAbrirCarrito: () -> Unit,
    unidadesCarrito: Int,
    onCerrarSesion: () -> Unit,
    onAbrirAdmin: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    esCliente: Boolean,
    onAbrirCuenta: () -> Unit = {},
    onFavoritoAgregado: (String) -> Unit = {},
    valoraciones: Map<String, Int> = emptyMap(),
    onCalificar: ((String, Int?) -> Unit)? = null,
    avisos: List<AvisoCliente> = emptyList(),
    avisosSinLeer: Boolean = false,
    onLeerAvisos: () -> Unit = {}
) {
    val estado by viewModel.catalogo.collectAsStateWithLifecycle()
    val sinConexion by viewModel.sinConexion.collectAsStateWithLifecycle()
    val consulta by viewModel.consulta.collectAsStateWithLifecycle()
    val categoria by viewModel.categoria.collectAsStateWithLifecycle()
    val errorFavorito by viewModel.errorFavorito.collectAsStateWithLifecycle()

    var avisosAbiertos by rememberSaveable { mutableStateOf(false) }
    var platoParaCalificar by androidx.compose.runtime.remember { mutableStateOf<Item?>(null) }
    val encabezado: @Composable () -> Unit = {
        if (esCliente) {
            EncabezadoUsuario(avisosSinLeer) {
                onLeerAvisos()
                avisosAbiertos = true
            }
        } else {
            EncabezadoCatalogo(onAbrirFavoritos, onAbrirCarrito, unidadesCarrito, onCerrarSesion, onAbrirAdmin)
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            Box(Modifier.weight(1f)) {
                when (val actual = estado) {
                    UiState.Cargando, is UiState.Error -> Column(Modifier.padding(16.dp)) {
                        encabezado()
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
                        verticalArrangement = Arrangement.spacedBy(if (esCliente) 14.dp else 18.dp)
                    ) {
                        item(key = "encabezado") {
                            Column(
                                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                encabezado()
                                OutlinedTextField(
                                    value = consulta,
                                    onValueChange = viewModel::buscar,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = if (esCliente) null else {
                                        { Text(stringResource(R.string.catalogo_buscar)) }
                                    },
                                    placeholder = if (esCliente) {
                                        { Text(stringResource(R.string.catalogo_buscar_usuario)) }
                                    } else null,
                                    leadingIcon = if (esCliente) {
                                        { Icon(painterResource(R.drawable.ic_menu_buscar), contentDescription = null) }
                                    } else null,
                                    trailingIcon = if (esCliente) {
                                        { FiltroUsuario(categoria, viewModel::seleccionarCategoria) }
                                    } else null,
                                    shape = MaterialTheme.shapes.large,
                                    singleLine = true
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(Categoria.entries, key = { it.name }) { opcion ->
                                        FilterChip(
                                            selected = categoria == opcion,
                                            onClick = { viewModel.seleccionarCategoria(opcion) },
                                            label = { Text(stringResource(opcion.etiqueta)) },
                                            shape = if (esCliente) androidx.compose.foundation.shape.CircleShape else FilterChipDefaults.shape,
                                            border = if (esCliente) null else FilterChipDefaults.filterChipBorder(
                                                enabled = true, selected = categoria == opcion
                                            ),
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = if (esCliente) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f) else
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                                selectedContainerColor = if (esCliente) MaterialTheme.colorScheme.primary else
                                                    MaterialTheme.colorScheme.secondaryContainer,
                                                selectedLabelColor = if (esCliente) MaterialTheme.colorScheme.onPrimary else
                                                    MaterialTheme.colorScheme.onSecondaryContainer
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
                                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                                estiloCliente = esCliente,
                                valoracion = valoraciones[plato.id],
                                onAbrirValoracion = if (onCalificar != null) {
                                    { platoParaCalificar = plato }
                                } else null,
                                onAlternarFavorito = if (esCliente) {
                                    {
                                        viewModel.alternarFavorito(plato.id) {
                                            onFavoritoAgregado(plato.titulo)
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
            if (errorFavorito) {
                Text(
                    stringResource(R.string.catalogo_error_favorito),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (esCliente) BarraUsuario(unidadesCarrito, onAbrirFavoritos, onAbrirCarrito, onAbrirCuenta)
        }
    }

    if (avisosAbiertos) ActividadUsuario(avisos, onCerrar = { avisosAbiertos = false })
    val plato = platoParaCalificar
    if (plato != null && onCalificar != null) {
        CalificarUsuario(
            nombre = plato.titulo,
            valorActual = valoraciones[plato.id],
            onCalificar = { estrellas ->
                onCalificar(plato.id, estrellas)
                platoParaCalificar = null
            },
            onCerrar = { platoParaCalificar = null }
        )
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
