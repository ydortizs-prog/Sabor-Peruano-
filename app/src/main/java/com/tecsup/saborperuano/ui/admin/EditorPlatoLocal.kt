package com.tecsup.saborperuano.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.data.repository.DatosPlatilloLocal
import com.tecsup.saborperuano.data.repository.MaxPrecioLocalCentimos
import com.tecsup.saborperuano.domain.model.Item
import java.math.BigDecimal

private val formatoPrecio = Regex("[0-9]{1,5}([.,][0-9]{1,2})?")

@Composable
internal fun EditorPlatoLocal(
    plato: Item?,
    edicion: EdicionMenu,
    onGuardar: (DatosPlatilloLocal) -> Unit,
    onRetirar: () -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by rememberSaveable(plato?.id) { mutableStateOf(plato?.titulo.orEmpty()) }
    var descripcion by rememberSaveable(plato?.id) { mutableStateOf(plato?.subtitulo.orEmpty()) }
    var precio by rememberSaveable(plato?.id) {
        mutableStateOf(plato?.precioCentimos?.let { BigDecimal.valueOf(it.toLong(), 2).toPlainString() }.orEmpty())
    }
    var confirmarRetiro by rememberSaveable { mutableStateOf(false) }
    val precioLimpio = precio.trim()
    val centimos = precioLimpio.takeIf(formatoPrecio::matches)
        ?.replace(',', '.')?.toBigDecimalOrNull()?.movePointRight(2)?.toInt()
        ?.takeIf { it in 1..MaxPrecioLocalCentimos }
    val activo = !edicion.guardando

    if (confirmarRetiro && plato != null) {
        AlertDialog(
            onDismissRequest = { if (activo) confirmarRetiro = false },
            title = { Text(stringResource(R.string.admin_retirar_plato)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.admin_confirmar_retiro, plato.titulo))
                    MensajeEdicion(edicion)
                }
            },
            confirmButton = {
                TextButton(onClick = onRetirar, enabled = activo) {
                    Text(stringResource(R.string.admin_retirar_plato), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRetiro = false }, enabled = activo) {
                    Text(stringResource(R.string.admin_cancelar))
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = { if (activo) onCerrar() },
        title = { Text(stringResource(if (plato == null) R.string.admin_nuevo_plato else R.string.admin_editar_plato)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (it.length <= 80) nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_nombre_plato)) },
                    enabled = activo,
                    singleLine = true
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 240) descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_descripcion_plato)) },
                    enabled = activo,
                    maxLines = 3
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { if (it.length <= 8) precio = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_precio_plato)) },
                    enabled = activo,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = precio.isNotBlank() && centimos == null
                )
                MensajeEdicion(edicion)
                if (plato != null) {
                    TextButton(onClick = { confirmarRetiro = true }, enabled = activo) {
                        Text(stringResource(R.string.admin_retirar_plato), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (centimos != null) {
                        onGuardar(DatosPlatilloLocal(nombre.trim(), descripcion.trim(), centimos))
                    }
                },
                enabled = activo && nombre.isNotBlank() && centimos != null
            ) { Text(stringResource(R.string.admin_guardar_plato)) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = activo) {
                Text(stringResource(R.string.admin_cancelar))
            }
        }
    )
}

@Composable
private fun MensajeEdicion(edicion: EdicionMenu) {
    if (edicion.guardando) Text(stringResource(R.string.admin_guardando_menu))
    edicion.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
