package com.tecsup.saborperuano.ui.admin

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.components.Cargando
import com.tecsup.saborperuano.ui.components.ErrorView
import com.tecsup.saborperuano.ui.components.FotoReceta
import com.tecsup.saborperuano.ui.components.PrecioPlato
import com.tecsup.saborperuano.ui.lista.ActividadUsuario
import com.tecsup.saborperuano.ui.lista.AvisoCliente

private enum class SeccionAdmin(@param:StringRes val etiqueta: Int, @param:DrawableRes val icono: Int) {
    PANEL(R.string.admin_inicio, R.drawable.ic_menu_inicio),
    MENU(R.string.admin_menu, R.drawable.ic_admin_menu),
    PEDIDOS(R.string.admin_pedidos, R.drawable.ic_perfil_pedido),
    PERFIL(R.string.perfil_pestana, R.drawable.ic_menu_cuenta)
}

@Composable
internal fun AdminScreen(
    viewModel: AdminViewModel,
    onVolver: () -> Unit,
    perfil: @Composable (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val avisos by viewModel.avisos.collectAsStateWithLifecycle()
    var avisosAbiertos by rememberSaveable { mutableStateOf(false) }
    var idSeleccionado by rememberSaveable { mutableStateOf<String?>(null) }
    val edicion by viewModel.edicionMenu.collectAsStateWithLifecycle()
    var revisionEditor by rememberSaveable { mutableIntStateOf(edicion.revision) }
    LaunchedEffect(edicion.revision) {
        if (edicion.revision != revisionEditor) {
            idSeleccionado = null
            revisionEditor = edicion.revision
        }
    }
    var seccion by rememberSaveable { mutableStateOf(SeccionAdmin.PANEL) }
    var confirmar by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = seccion != SeccionAdmin.PANEL) { seccion = SeccionAdmin.PANEL }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (seccion == SeccionAdmin.PERFIL) {
                    perfil { seccion = SeccionAdmin.PEDIDOS }
                } else {
                    when (val actual = estado) {
                        UiState.Cargando -> Cargando()
                        is UiState.Error -> Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ErrorView(actual.mensaje, viewModel::cargar)
                            TextButton(onClick = onVolver) {
                                Text(stringResource(R.string.admin_ver_catalogo))
                            }
                        }
                        is UiState.Exito -> {
                            val platos by viewModel.catalogo.collectAsStateWithLifecycle(initialValue = emptyList())
                            ContenidoAdmin(
                                seccion = seccion,
                                resumen = actual.datos,
                                platos = platos,
                                onVerMenu = { seccion = SeccionAdmin.MENU },
                                onSeleccionarPlato = {
                                    viewModel.iniciarEdicionMenu()
                                    idSeleccionado = it
                                },
                                onCrearPlato = {
                                    viewModel.iniciarEdicionMenu()
                                    idSeleccionado = ""
                                },
                                avisosSinLeer = avisos.sinLeer,
                                onAbrirAvisos = {
                                    viewModel.leerAvisos()
                                    avisosAbiertos = true
                                },
                                onVolver = onVolver,
                                onSincronizar = viewModel::sincronizar,
                                onLimpiar = { confirmar = true }
                            )
                            val plato = platos.firstOrNull { it.id == idSeleccionado }
                            if (idSeleccionado == "" || plato != null) {
                                EditorPlatoLocal(
                                    plato = plato,
                                    edicion = edicion,
                                    onGuardar = { viewModel.guardarPlato(plato?.id, it) },
                                    onRetirar = { if (plato != null) viewModel.retirarPlato(plato.id) },
                                    onCerrar = { idSeleccionado = null }
                                )
                            }
                        }
                    }
                }
            }

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                val colores = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
                SeccionAdmin.entries.forEach { opcion ->
                    NavigationBarItem(
                        selected = seccion == opcion,
                        onClick = { seccion = opcion },
                        icon = { Icon(painterResource(opcion.icono), contentDescription = null) },
                        label = { Text(stringResource(opcion.etiqueta)) },
                        colors = colores
                    )
                }
            }
        }
    }

    if (avisosAbiertos) {
        ActividadUsuario(
            avisos = avisos.eventos.map { AvisoCliente(it) },
            onCerrar = { avisosAbiertos = false },
            textoVacio = R.string.admin_avisos_vacio
        )
    }
    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text(stringResource(R.string.admin_limpiar)) },
            text = { Text(stringResource(R.string.admin_confirmar_limpieza)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.limpiarCache()
                }) { Text(stringResource(R.string.admin_limpiar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) {
                    Text(stringResource(R.string.admin_cancelar))
                }
            }
        )
    }
}

@Composable
private fun ContenidoAdmin(
    seccion: SeccionAdmin,
    resumen: ResumenAdmin,
    platos: List<Item>,
    onVerMenu: () -> Unit,
    onSeleccionarPlato: (String) -> Unit,
    onCrearPlato: () -> Unit,
    avisosSinLeer: Boolean,
    onAbrirAvisos: () -> Unit,
    onVolver: () -> Unit,
    onSincronizar: () -> Unit,
    onLimpiar: () -> Unit
) {
    val panel = seccion == SeccionAdmin.PANEL
    val colores = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "encabezado") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.catalogo_marca),
                        color = colores.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        stringResource(R.string.app_name),
                        color = colores.primary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(if (panel) R.string.admin_panel else seccion.etiqueta),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                IconButton(onClick = onAbrirAvisos) {
                    BadgedBox(badge = { if (avisosSinLeer) Badge() }) {
                        Icon(
                            painterResource(R.drawable.ic_menu_campana),
                            contentDescription = stringResource(R.string.catalogo_actividad),
                            tint = colores.primary
                        )
                    }
                }
            }
        }

        if (panel) {
            item(key = "resumen") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IndicadorAdmin(resumen.guardados, R.string.admin_indicador_catalogo, Modifier.weight(1f))
                    IndicadorAdmin(resumen.favoritos, R.string.admin_indicador_favoritos, Modifier.weight(1f))
                }
            }
        }

        if (panel || seccion == SeccionAdmin.MENU) {
            item(key = "menu") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(if (panel) R.string.admin_menu else R.string.admin_catalogo_local),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (panel) {
                        TextButton(onClick = onVerMenu) { Text(stringResource(R.string.admin_ver_menu)) }
                    } else {
                        FilledTonalButton(onClick = onCrearPlato) { Text(stringResource(R.string.admin_nuevo_plato)) }
                    }
                }
            }
            if (seccion == SeccionAdmin.MENU) {
                item(key = "demo") { NotaAdmin(stringResource(R.string.admin_menu_local)) }
            }
            if (platos.isEmpty()) {
                item(key = "menu_vacio") { NotaAdmin(stringResource(R.string.admin_catalogo_vacio)) }
            }
            items(if (panel) platos.take(3) else platos, key = { it.id }) { plato ->
                FilaPlatoAdmin(plato, onEditar = { onSeleccionarPlato(plato.id) })
            }
            item(key = "acciones") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.admin_gestion_local), style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onSincronizar, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.admin_sincronizar))
                    }
                    OutlinedButton(onClick = onLimpiar, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.admin_limpiar))
                    }
                    resumen.mensaje?.let { Text(it, color = colores.onSurfaceVariant) }
                    TextButton(onClick = onVolver) { Text(stringResource(R.string.admin_ver_catalogo)) }
                }
            }
        }

        if (seccion == SeccionAdmin.PEDIDOS) {
            item(key = "pedidos") {
                NotaAdmin(stringResource(R.string.admin_pedidos_por_chat))
            }
        }
    }
}

@Composable
private fun IndicadorAdmin(valor: Int, @StringRes etiqueta: Int, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(etiqueta), style = MaterialTheme.typography.labelMedium)
            Text(valor.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilaPlatoAdmin(plato: Item, onEditar: () -> Unit) {
    OutlinedCard(shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FotoReceta(plato, Modifier.size(66.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(plato.titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    plato.subtitulo,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                PrecioPlato(plato.precioCentimos)
            }
            IconButton(
                onClick = onEditar,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    painterResource(R.drawable.ic_admin_editar),
                    contentDescription = stringResource(R.string.admin_editar_plato_acceso, plato.titulo)
                )
            }
        }
    }
}

@Composable
private fun NotaAdmin(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(texto, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}
