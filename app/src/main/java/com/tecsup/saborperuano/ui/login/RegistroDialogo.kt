package com.tecsup.saborperuano.ui.login

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.UiState

@Composable
internal fun RegistroDialogo(
    correoInicial: String,
    estado: UiState<Unit>,
    onRegistrar: (String, String, String, String) -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf(correoInicial) }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    val enviando = estado == UiState.Cargando
    val foco = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { if (!enviando) onCerrar() },
        title = { Text(stringResource(R.string.registro_titulo)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoRegistro(nombre, { if (it.length <= 80) nombre = it }, R.string.registro_nombre, !enviando)
                CampoRegistro(correo, { correo = it }, R.string.login_correo, !enviando, KeyboardType.Email)
                CampoRegistro(clave, { clave = it }, R.string.login_clave, !enviando, KeyboardType.Password)
                CampoRegistro(
                    confirmacion, { confirmacion = it }, R.string.registro_confirmar_clave,
                    !enviando, KeyboardType.Password, ImeAction.Done
                )
                if (enviando) Text(stringResource(R.string.registro_enviando))
                if (estado is UiState.Error) Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    foco.clearFocus()
                    onRegistrar(nombre, correo, clave, confirmacion)
                },
                enabled = !enviando
            ) {
                Text(stringResource(R.string.registro_titulo))
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !enviando) {
                Text(stringResource(R.string.admin_cancelar))
            }
        }
    )
}

@Composable
private fun CampoRegistro(
    valor: String,
    onCambiar: (String) -> Unit,
    etiqueta: Int,
    habilitado: Boolean,
    teclado: KeyboardType = KeyboardType.Text,
    accionIme: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambiar,
        label = { Text(stringResource(etiqueta)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = habilitado,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = accionIme),
        visualTransformation = if (teclado == KeyboardType.Password) PasswordVisualTransformation()
            else VisualTransformation.None
    )
}
