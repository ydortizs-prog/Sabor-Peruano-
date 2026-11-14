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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val reciboLocal = envio as? EnvioPedido.GuardadoLocal
    LaunchedEffect(recibo?.pedido?.pedidoId) {
        recibo?.pedido?.pedidoId?.let(onPedidoGuardado)
    }
    val enviando = envio is EnvioPedido.Guardando
    val errorEnvio = envio as? EnvioPedido.Error
    val datos by viewModel.datos.collectAsStateWithLifecycle()
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }
    var revisando by rememberSaveable { mutableStateOf(false) }
    var errorWhatsApp by rememberSaveable { mutableStateOf(false) }
    var chatAbierto by rememberSaveable { mutableStateOf(false) }
    val numeroWhatsApp = stringResource(R.string.whatsapp_restaurante_numero)
    val sePuedeGuardar = viewModel.envioHabilitado && datos.entrega == Entrega.RECOJO
    val porWhatsApp = !sePuedeGuardar && numeroWhatsApp.isNotBlank()
    val puedeEnviar = sePuedeGuardar || porWhatsApp
    val puedeRevisar = datos.valido && (!porWhatsApp || datos.metodoPago != null)
    val enResumen = revisando && puedeRevisar && resumen.lineas.isNotEmpty() && recibo == null && reciboLocal == null
    val scrollDatos = rememberScrollState()
    val scrollResumen = rememberScrollState()
    val contexto = LocalContext.current
    val foco = LocalFocusManager.current
    val volver = {
        if (enResumen) {
            revisando = false
            chatAbierto = false
            errorWhatsApp = false
        } else {
            onVolver()
        }
    }

    BackHandler(enabled = enviando || enResumen) {
        if (!enviando) volver()
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().imePadding()) {
            TextButton(onClick = volver, enabled = !enviando, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(stringResource(if (enResumen) R.string.pedido_editar else R.string.volver))
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .verticalScroll(if (enResumen) scrollResumen else scrollDatos)
                    .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when {
                    reciboLocal != null -> {
                        Text(stringResource(R.string.pedido_guardado), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.pedido_guardado_local))
                        SeccionPedido(R.string.pedido_tus_platos) {
                            ResumenDePlatos(reciboLocal.resumen, reciboLocal.entrega)
                        }
                        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.pedido_volver_carrito))
                        }
                    }
                    recibo != null -> {
                        Text(stringResource(R.string.pedido_guardado), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.pedido_numero, recibo.pedido.pedidoId))
                        Text(stringResource(R.string.carrito_total, soles(recibo.pedido.totalCentimos)))
                        CompartirPedidoWhatsApp(recibo, onAbrirWhatsApp)
                    }
                    resumen.lineas.isEmpty() -> Text(stringResource(R.string.carrito_vacio))
                    enResumen -> {
                        EncabezadoPedido(
                            R.string.pedido_paso_resumen,
                            if (puedeEnviar) R.string.pedido_confirmar_titulo else R.string.pedido_resumen
                        )
                        ResumenPedido(datos, resumen, porWhatsApp)
                        if (porWhatsApp) {
                            Text(
                                stringResource(
                                    if (chatAbierto) R.string.pedido_chat_regreso else R.string.pedido_whatsapp_directo
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (!sePuedeGuardar) {
                            Text(stringResource(R.string.pedido_envio_pendiente))
                        }
                        if (errorWhatsApp) {
                            Text(stringResource(R.string.pedido_whatsapp_error), color = MaterialTheme.colorScheme.error)
                        }
                        if (errorEnvio != null) {
                            Text(stringResource(errorEnvio.mensaje), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {
                        EncabezadoPedido(R.string.pedido_paso_datos, R.string.pedido_datos)
                        Text(
                            stringResource(R.string.pedido_datos_ayuda),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DatosEntregaPedido(datos, mostrarErrores, viewModel)
                        if (porWhatsApp) {
                            SeccionPedido(R.string.pedido_metodo_pago) {
                                SelectorMetodoPago(
                                    seleccionado = datos.metodoPago,
                                    onSeleccionar = viewModel::metodoPago,
                                    mostrarError = mostrarErrores && datos.metodoPago == null
                                )
                            }
                        } else if (!puedeEnviar) {
                            Text(stringResource(R.string.pedido_envio_pendiente))
                        }
                    }
                }
            }

            if (recibo == null && reciboLocal == null && resumen.lineas.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!enResumen) {
                            if (mostrarErrores && !puedeRevisar) {
                                Text(stringResource(R.string.pedido_datos_pendientes), color = MaterialTheme.colorScheme.error)
                            }
                            Button(
                                onClick = {
                                    foco.clearFocus()
                                    mostrarErrores = true
                                    revisando = puedeRevisar
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text(stringResource(
                                    if (puedeEnviar) R.string.pedido_continuar_resumen else R.string.pedido_previsualizar
                                ))
                            }
                        } else if (sePuedeGuardar) {
                            Button(
                                onClick = viewModel::guardar,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                enabled = !enviando && errorEnvio?.reintentar != false
                            ) {
                                Text(stringResource(if (enviando) R.string.pedido_guardando else R.string.pedido_guardar))
                            }
                        } else if (porWhatsApp) {
                            val abrirChat = {
                                errorWhatsApp = !abrirWhatsApp(
                                    contexto, numeroWhatsApp, mensajeParaWhatsApp(datos, resumen)
                                )
                                if (!errorWhatsApp && !chatAbierto) {
                                    chatAbierto = true
                                    onAbrirWhatsApp()
                                }
                            }
                            if (chatAbierto) {
                                Button(
                                    onClick = viewModel::confirmarEnvioWhatsApp,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) { Text(stringResource(R.string.pedido_whatsapp_ya_enviado)) }
                                TextButton(onClick = abrirChat, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.pedido_whatsapp_reabrir))
                                }
                            } else {
                                Button(
                                    onClick = abrirChat,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) { Text(stringResource(R.string.pedido_whatsapp_abrir)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoPedido(paso: Int, titulo: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(paso), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(titulo), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SeccionPedido(titulo: Int, contenido: @Composable () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(titulo), style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
private fun DatosEntregaPedido(datos: DatosPedido, mostrarErrores: Boolean, viewModel: ConfirmarPedidoViewModel) {
    SeccionPedido(R.string.pedido_contacto) {
        CampoPedido(datos.nombre, viewModel::nombre, R.string.pedido_nombre,
            error = R.string.pedido_nombre_error.takeIf { mostrarErrores && !datos.nombreValido })
        CampoPedido(datos.telefono, viewModel::telefono, R.string.pedido_telefono,
            error = R.string.pedido_telefono_error.takeIf { mostrarErrores && !datos.telefonoValido },
            teclado = KeyboardType.Phone)
    }
    SeccionPedido(R.string.pedido_entrega) {
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
        CampoPedido(datos.observaciones, viewModel::observaciones, R.string.pedido_observaciones, multilinea = true)
    }
}

@Composable
private fun ResumenPedido(datos: DatosPedido, resumen: ResumenCarrito, porWhatsApp: Boolean) {
    SeccionPedido(R.string.pedido_tus_platos) {
        ResumenDePlatos(resumen, datos.entrega)
    }
    SeccionPedido(R.string.pedido_contacto_entrega) {
        Text(datos.nombre.trim())
        Text(datos.telefono.trim())
        Text(stringResource(if (datos.entrega == Entrega.RECOJO) R.string.pedido_recojo else R.string.pedido_delivery))
        if (datos.entrega == Entrega.DELIVERY) Text(datos.direccion.trim())
        if (datos.observaciones.isNotBlank()) Text(datos.observaciones.trim())
    }
    if (porWhatsApp) {
        datos.metodoPago?.let { metodo ->
            SeccionPedido(R.string.pedido_metodo_pago) {
                Text(stringResource(metodo.etiqueta), fontWeight = FontWeight.SemiBold)
                Text(stringResource(metodo.instruccion(datos.entrega)))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorMetodoPago(
    seleccionado: MetodoPago?,
    onSeleccionar: (MetodoPago) -> Unit,
    mostrarError: Boolean
) {
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
