package com.tecsup.saborperuano.ui.carrito

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.components.soles

@Composable
internal fun ConfirmarPedidoScreen(
    viewModel: ConfirmarPedidoViewModel,
    onVolver: () -> Unit,
    onAbrirWhatsApp: () -> Unit = {},
    onPedidoGuardado: (String) -> Unit = {}
) {
    val envio by viewModel.envio.collectAsStateWithLifecycle()
    val recibo = envio as? EnvioPedido.Guardado
    LaunchedEffect(recibo?.pedido?.pedidoId) {
        recibo?.pedido?.pedidoId?.let(onPedidoGuardado)
    }
    val enviando = envio is EnvioPedido.Guardando
    val errorEnvio = envio as? EnvioPedido.Error
    BackHandler(enviando) {}
    val datos by viewModel.datos.collectAsStateWithLifecycle()
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }
    var verResumen by rememberSaveable { mutableStateOf(false) }
    var errorWhatsApp by rememberSaveable { mutableStateOf(false) }
    val numeroWhatsApp = stringResource(R.string.whatsapp_restaurante_numero)
    val sePuedeGuardar = viewModel.envioHabilitado && datos.entrega == Entrega.RECOJO
    val porWhatsApp = !sePuedeGuardar && numeroWhatsApp.isNotBlank()
    val metodoPago = datos.metodoPago
    val puedeVerResumen = datos.valido && (!porWhatsApp || metodoPago != null)
    val context = LocalContext.current

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text(stringResource(R.string.volver)) }
            if (recibo != null) {
                Text(stringResource(R.string.pedido_guardado), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.pedido_numero, recibo.pedido.pedidoId))
                Text(stringResource(R.string.carrito_total, soles(recibo.pedido.totalCentimos)))
                CompartirPedidoWhatsApp(recibo, onAbrirWhatsApp)
                return@Column
            }
            Text(stringResource(R.string.pedido_datos), style = MaterialTheme.typography.headlineMedium)
            if (resumen.lineas.isEmpty()) {
                Text(stringResource(R.string.carrito_vacio))
                return@Column
            }

            CampoPedido(datos.nombre, viewModel::nombre, R.string.pedido_nombre,
                error = R.string.pedido_nombre_error.takeIf { mostrarErrores && !datos.nombreValido })
            CampoPedido(datos.telefono, viewModel::telefono, R.string.pedido_telefono,
                error = R.string.pedido_telefono_error.takeIf { mostrarErrores && !datos.telefonoValido },
                teclado = KeyboardType.Phone)
            Text(stringResource(R.string.pedido_entrega), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = datos.entrega == Entrega.RECOJO,
                    onClick = { viewModel.entrega(Entrega.RECOJO) },
                    label = { Text(stringResource(R.string.pedido_recojo)) }
                )
                FilterChip(
                    selected = datos.entrega == Entrega.DELIVERY,
                    onClick = { viewModel.entrega(Entrega.DELIVERY) },
                    label = { Text(stringResource(R.string.pedido_delivery)) }
                )
            }
            if (datos.entrega == Entrega.DELIVERY) {
                CampoPedido(datos.direccion, viewModel::direccion, R.string.pedido_direccion,
                    error = R.string.pedido_direccion_error.takeIf { mostrarErrores && !datos.direccionValida })
            }
            CampoPedido(datos.observaciones, viewModel::observaciones, R.string.pedido_observaciones,
                multilinea = true)
            if (porWhatsApp) {
                SelectorMetodoPago(
                    seleccionado = metodoPago,
                    onSeleccionar = viewModel::metodoPago,
                    mostrarError = mostrarErrores && metodoPago == null
                )
            }
            Text(stringResource(R.string.pedido_resumen), style = MaterialTheme.typography.titleLarge)
            ResumenDePlatos(resumen, datos.entrega)
            Button(
                onClick = {
                    mostrarErrores = true
                    errorWhatsApp = false
                    verResumen = puedeVerResumen
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.pedido_previsualizar)) }
        }
    }

    if (verResumen && puedeVerResumen && resumen.lineas.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { if (!enviando) verResumen = false },
            title = { Text(stringResource(R.string.pedido_resumen)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(datos.nombre.trim())
                    Text(datos.telefono.trim())
                    Text(stringResource(
                        if (datos.entrega == Entrega.RECOJO) R.string.pedido_recojo else R.string.pedido_delivery
                    ))
                    if (datos.entrega == Entrega.DELIVERY) Text(datos.direccion.trim())
                    if (datos.observaciones.isNotBlank()) Text(datos.observaciones.trim())
                    ResumenDePlatos(resumen, datos.entrega)
                    if (porWhatsApp && metodoPago != null) {
                        Text(
                            stringResource(R.string.pedido_pago_elegido, stringResource(metodoPago.etiqueta)),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(stringResource(metodoPago.instruccion(datos.entrega)))
                        Text(stringResource(R.string.pedido_whatsapp_directo))
                        if (errorWhatsApp) {
                            Text(
                                stringResource(R.string.pedido_whatsapp_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else if (!sePuedeGuardar) {
                        Text(stringResource(R.string.pedido_envio_pendiente))
                    }
                    if (enviando) Text(stringResource(R.string.pedido_guardando))
                    if (errorEnvio != null) {
                        Text(stringResource(errorEnvio.mensaje), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                if (sePuedeGuardar) {
                    Button(
                        onClick = viewModel::guardar,
                        enabled = !enviando && errorEnvio?.reintentar != false
                    ) { Text(stringResource(R.string.pedido_guardar)) }
                } else if (porWhatsApp) {
                    Button(onClick = {
                        errorWhatsApp = !abrirWhatsApp(
                            context, numeroWhatsApp, mensajeParaWhatsApp(datos, resumen)
                        )
                        if (!errorWhatsApp) {
                            verResumen = false
                            onAbrirWhatsApp()
                        }
                    }) { Text(stringResource(R.string.pedido_whatsapp_abrir)) }
                }
            },
            dismissButton = {
                TextButton(enabled = !enviando, onClick = { verResumen = false }) {
                    Text(stringResource(R.string.pedido_editar))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorMetodoPago(
    seleccionado: MetodoPago?,
    onSeleccionar: (MetodoPago) -> Unit,
    mostrarError: Boolean
) {
    Text(stringResource(R.string.pedido_metodo_pago), style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetodoPago.entries.forEach { opcion ->
            FilterChip(
                selected = seleccionado == opcion,
                onClick = { onSeleccionar(opcion) },
                label = { Text(stringResource(opcion.etiqueta)) }
            )
        }
    }
    if (mostrarError) {
        Text(stringResource(R.string.pedido_pago_requerido), color = MaterialTheme.colorScheme.error)
    }
}

private fun MetodoPago.instruccion(entrega: Entrega): Int = when (this) {
    MetodoPago.YAPE -> R.string.pedido_pago_yape_manual
    MetodoPago.EFECTIVO -> if (entrega == Entrega.DELIVERY) R.string.pedido_pago_efectivo_delivery
        else R.string.pedido_pago_efectivo_recojo
}

@Composable
private fun CompartirPedidoWhatsApp(recibo: EnvioPedido.Guardado, onAbrirWhatsApp: () -> Unit) {
    val numero = stringResource(R.string.whatsapp_restaurante_numero)
    if (numero.isBlank()) return

    val context = LocalContext.current
    var mostrar by rememberSaveable { mutableStateOf(false) }
    var errorAlAbrir by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = {
        errorAlAbrir = false
        mostrar = true
    }) { Text(stringResource(R.string.pedido_whatsapp_revisar)) }

    if (mostrar) {
        val mensaje = mensajeParaWhatsApp(recibo)
        AlertDialog(
            onDismissRequest = { mostrar = false },
            title = { Text(stringResource(R.string.pedido_whatsapp_revisar)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(mensaje)
                    if (errorAlAbrir) {
                        Text(
                            stringResource(R.string.pedido_whatsapp_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    errorAlAbrir = !abrirWhatsApp(context, numero, mensaje)
                    if (!errorAlAbrir) {
                        mostrar = false
                        onAbrirWhatsApp()
                    }
                }) { Text(stringResource(R.string.pedido_whatsapp_abrir)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrar = false }) {
                    Text(stringResource(R.string.admin_cancelar))
                }
            }
        )
    }
}

private fun abrirWhatsApp(context: Context, numero: String, mensaje: String): Boolean =
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(enlaceWhatsApp(numero, mensaje))))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    }

@Composable
private fun CampoPedido(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: Int,
    error: Int? = null,
    teclado: KeyboardType = KeyboardType.Text,
    multilinea: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(stringResource(etiqueta)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multilinea,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        isError = error != null,
        supportingText = if (error != null) { { Text(stringResource(error)) } } else null
    )
}

@Composable
private fun ResumenDePlatos(resumen: ResumenCarrito, entrega: Entrega) {
    resumen.lineas.forEach { linea ->
        Text(stringResource(R.string.pedido_linea, linea.cantidad, linea.nombre, soles(linea.subtotalCentimos)))
    }
    val importe = if (entrega == Entrega.RECOJO) R.string.carrito_total else R.string.pedido_subtotal
    Text(
        stringResource(importe, soles(resumen.totalCentimos)),
        style = MaterialTheme.typography.titleMedium
    )
    if (entrega == Entrega.DELIVERY) Text(stringResource(R.string.pedido_tarifa_pendiente))
}
