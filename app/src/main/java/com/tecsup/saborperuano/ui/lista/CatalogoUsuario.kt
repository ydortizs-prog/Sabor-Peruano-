package com.tecsup.saborperuano.ui.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.data.local.TipoAvisoCliente

@Composable
internal fun EncabezadoUsuario(
    sinLeer: Boolean,
    cantidad: Int = 0,
    onAbrirAvisos: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.catalogo_marca),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onAbrirAvisos) {
            BadgedBox(badge = {
                when {
                    cantidad > 0 -> Badge { Text(if (cantidad > 9) "9+" else cantidad.toString()) }
                    sinLeer -> Badge()
                }
            }) {
                IconoMenu(R.drawable.ic_menu_campana, R.string.catalogo_actividad)
            }
        }
    }
}

@Composable
internal fun FiltroUsuario(categoria: Categoria, onElegir: (Categoria) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { abierto = true }) {
            IconoMenu(R.drawable.ic_menu_filtros, R.string.catalogo_filtros)
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            Categoria.entries.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(opcion.etiqueta),
                            color = if (opcion == categoria) MaterialTheme.colorScheme.primary else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onElegir(opcion)
                        abierto = false
                    }
                )
            }
        }
    }
}

@Composable
internal fun BarraUsuario(
    unidades: Int,
    onAbrirFavoritos: () -> Unit,
    onAbrirCarrito: () -> Unit,
    onAbrirCuenta: () -> Unit,
    perfilSeleccionado: Boolean = false,
    onAbrirInicio: () -> Unit = {}
) {
    NavigationBar(
        modifier = if (perfilSeleccionado) Modifier.heightIn(min = 72.dp) else Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val colores = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        NavigationBarItem(
            selected = !perfilSeleccionado,
            onClick = onAbrirInicio,
            icon = { IconoPestana(R.drawable.ic_menu_inicio, R.string.catalogo_inicio, perfilSeleccionado) },
            label = etiquetaPestana(perfilSeleccionado, R.string.catalogo_inicio),
            colors = colores
        )
        NavigationBarItem(
            selected = false,
            onClick = onAbrirFavoritos,
            icon = { IconoPestana(R.drawable.ic_menu_favorito, R.string.favoritos_titulo, perfilSeleccionado) },
            label = etiquetaPestana(perfilSeleccionado, R.string.favoritos_titulo),
            colors = colores
        )
        NavigationBarItem(
            selected = false,
            onClick = onAbrirCarrito,
            label = etiquetaPestana(perfilSeleccionado, R.string.carrito_titulo),
            icon = {
                BadgedBox(badge = {
                    if (unidades > 0) Badge { Text(if (unidades > 99) "99+" else unidades.toString()) }
                }) {
                    Icon(
                        painterResource(R.drawable.ic_menu_carrito),
                        stringResource(R.string.carrito_acceso, unidades)
                    )
                }
            },
            colors = colores
        )
        NavigationBarItem(
            selected = perfilSeleccionado,
            onClick = onAbrirCuenta,
            icon = { IconoPestana(R.drawable.ic_menu_cuenta, R.string.catalogo_cuenta, perfilSeleccionado) },
            label = etiquetaPestana(perfilSeleccionado, R.string.perfil_pestana),
            colors = colores
        )
    }
}

@Composable
private fun IconoPestana(icono: Int, etiqueta: Int, mostrarEtiqueta: Boolean) {
    Icon(painterResource(icono), if (mostrarEtiqueta) null else stringResource(etiqueta))
}

private fun etiquetaPestana(mostrar: Boolean, etiqueta: Int): (@Composable () -> Unit)? =
    if (mostrar) ({ Text(stringResource(etiqueta)) }) else null

internal data class AvisoCliente(
    val mensaje: Int,
    val plato: String? = null,
    val tipo: TipoAvisoCliente? = null,
    val fecha: Long = 0L
)

@Composable
internal fun ActividadUsuario(
    avisos: List<AvisoCliente>,
    onCerrar: () -> Unit,
    titulo: Int = R.string.catalogo_actividad,
    textoVacio: Int = R.string.catalogo_sin_avisos
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(titulo)) },
        text = {
            if (avisos.isEmpty()) {
                Text(stringResource(textoVacio))
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(avisos) { aviso ->
                        Text(
                            if (aviso.plato == null) stringResource(aviso.mensaje)
                            else stringResource(aviso.mensaje, aviso.plato)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.catalogo_cerrar)) } }
    )
}

@Composable
internal fun CalificarUsuario(
    nombre: String,
    valorActual: Int?,
    onCalificar: (Int?) -> Unit,
    onCerrar: () -> Unit
) {
    var estrellas by remember { mutableStateOf(valorActual ?: 0) }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.catalogo_calificar_titulo, nombre)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.catalogo_calificar_privado))
                if (valorActual != null) {
                    TextButton(onClick = { onCalificar(null) }) {
                        Text(stringResource(R.string.catalogo_quitar_valoracion))
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    (1..5).forEach { valor ->
                        val descripcion = stringResource(R.string.catalogo_estrellas_opcion, valor)
                        IconButton(
                            onClick = { estrellas = valor },
                            modifier = Modifier.weight(1f).semantics { contentDescription = descripcion }
                        ) {
                            Text(
                                if (valor <= estrellas) "★" else "☆",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = estrellas > 0, onClick = { onCalificar(estrellas) }) {
                Text(stringResource(R.string.catalogo_guardar_valoracion))
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text(stringResource(R.string.admin_cancelar)) } }
    )
}

@Composable
private fun IconoMenu(icono: Int, descripcion: Int) {
    Icon(
        painterResource(icono),
        contentDescription = stringResource(descripcion),
        tint = MaterialTheme.colorScheme.primary
    )
}
