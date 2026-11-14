package com.tecsup.saborperuano.ui.lista

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.domain.model.Rol
import com.tecsup.saborperuano.domain.model.Sesion

@Composable
internal fun PerfilUsuarioScreen(
    sesion: Sesion,
    temaOscuro: Boolean,
    onCambiarTema: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit,
    pedidos: List<AvisoCliente> = emptyList(),
    onAbrirPedidosAdmin: (() -> Unit)? = null,
    barraInferior: @Composable () -> Unit = {}
) {
    var pedidosAbiertos by rememberSaveable { mutableStateOf(false) }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    val colores = MaterialTheme.colorScheme
    val modoOscuro = stringResource(R.string.perfil_modo_oscuro)

    Surface(modifier = Modifier.fillMaxSize(), color = colores.background) {
        Column {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.perfil_marca),
                        style = MaterialTheme.typography.labelLarge,
                        color = colores.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.catalogo_cuenta),
                        style = MaterialTheme.typography.displaySmall,
                        color = colores.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                TarjetaIdentidad(sesion)

                SeccionPerfil(stringResource(R.string.perfil_actividad)) {
                    Card(
                        onClick = onAbrirPedidosAdmin ?: { pedidosAbiertos = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colores.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        FilaPerfil(
                            icono = R.drawable.ic_perfil_pedido,
                            fondo = colores.primaryContainer,
                            tinta = colores.onPrimaryContainer,
                            titulo = stringResource(
                                if (onAbrirPedidosAdmin == null) R.string.perfil_pedidos else R.string.admin_perfil_pedidos
                            ),
                            subtitulo = stringResource(
                                if (onAbrirPedidosAdmin == null) R.string.perfil_pedidos_subtitulo
                                else R.string.admin_perfil_pedidos_subtitulo
                            )
                        ) {
                            Icon(painterResource(R.drawable.ic_perfil_flecha), contentDescription = null)
                        }
                    }
                }

                SeccionPerfil(stringResource(R.string.perfil_preferencias)) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colores.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        FilaPerfil(
                            icono = R.drawable.ic_perfil_luna,
                            fondo = colores.secondaryContainer,
                            tinta = colores.onSecondaryContainer,
                            titulo = modoOscuro,
                            subtitulo = stringResource(
                                if (temaOscuro) R.string.catalogo_tema_oscuro else R.string.catalogo_tema_claro
                            )
                        ) {
                            Switch(
                                checked = temaOscuro,
                                onCheckedChange = onCambiarTema,
                                modifier = Modifier.semantics { contentDescription = modoOscuro }
                            )
                        }
                    }
                }
            }

            Box(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                OutlinedButton(
                    onClick = { confirmarSalida = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colores.primary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_perfil_salir), contentDescription = null)
                        Text(stringResource(R.string.login_salir), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            barraInferior()
        }
    }

    if (pedidosAbiertos) {
        ActividadUsuario(
            avisos = pedidos,
            onCerrar = { pedidosAbiertos = false },
            titulo = R.string.perfil_pedidos,
            textoVacio = R.string.perfil_sin_pedidos
        )
    }
    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text(stringResource(R.string.perfil_confirmar_salida)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onCerrarSesion()
                }) { Text(stringResource(R.string.login_salir)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text(stringResource(R.string.admin_cancelar))
                }
            }
        )
    }
}

@Composable
private fun TarjetaIdentidad(sesion: Sesion) {
    val colores = MaterialTheme.colorScheme
    val nombre = sesion.nombre ?: stringResource(
        if (sesion.rol == Rol.ADMIN) R.string.admin_perfil_nombre else R.string.perfil_nombre_alternativo
    )
    val iniciales = sesion.nombre?.split(Regex("\\s+"))?.take(2)?.joinToString("") { it.take(1) }
        ?: sesion.correo.substringBefore('@').take(1).ifEmpty { nombre.take(1) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colores.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colores.primary,
                contentColor = colores.onPrimary
            ) {
                Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                    Text(iniciales.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    sesion.correo.ifBlank { stringResource(R.string.catalogo_correo_no_disponible) },
                    color = colores.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SeccionPerfil(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            titulo,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        contenido()
    }
}

@Composable
private fun FilaPerfil(
    @DrawableRes icono: Int,
    fondo: Color,
    tinta: Color,
    titulo: String,
    subtitulo: String,
    accion: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = fondo, contentColor = tinta) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Icon(painterResource(icono), contentDescription = null)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        accion()
    }
}
