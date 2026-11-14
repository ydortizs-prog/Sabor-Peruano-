package com.tecsup.saborperuano.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.UiState

// Mismos colores que el inicio de sesión.
private val Vino = Color(0xFF691E25)
private val Crema = Color(0xFFF5F1E2)
private val Contorno = Color(0xFF8B7C74)
private val TextoSuave = Color(0xFF625650)

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
    var claveVisible by remember { mutableStateOf(false) }
    val enviando = estado == UiState.Cargando
    val foco = LocalFocusManager.current
    val registrar = {
        foco.clearFocus()
        claveVisible = false
        onRegistrar(nombre, correo, clave, confirmacion)
    }
    val ojo: @Composable () -> Unit = {
        IconButton(onClick = { claveVisible = !claveVisible }, enabled = !enviando) {
            Icon(
                painterResource(if (claveVisible) R.drawable.ic_acceso_visible else R.drawable.ic_acceso_oculto),
                contentDescription = stringResource(
                    if (claveVisible) R.string.login_ocultar_clave else R.string.login_mostrar_clave
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = { if (!enviando) onCerrar() },
        containerColor = Crema,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Vino.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_menu_cuenta), contentDescription = null, tint = Vino)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.registro_titulo),
                    color = Vino,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.registro_subtitulo),
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampoRegistro(
                    nombre, { if (it.length <= 80) nombre = it }, R.string.registro_nombre,
                    R.drawable.ic_menu_cuenta, !enviando
                )
                CampoRegistro(
                    correo, { correo = it }, R.string.login_correo,
                    R.drawable.ic_acceso_correo, !enviando, KeyboardType.Email
                )
                CampoRegistro(
                    clave, { clave = it }, R.string.login_clave,
                    R.drawable.ic_acceso_candado, !enviando, KeyboardType.Password,
                    claveVisible = claveVisible, accion = ojo
                )
                CampoRegistro(
                    confirmacion, { confirmacion = it }, R.string.registro_confirmar_clave,
                    R.drawable.ic_acceso_candado, !enviando, KeyboardType.Password, ImeAction.Done,
                    claveVisible = claveVisible
                )
                if (enviando) Text(stringResource(R.string.registro_enviando), color = TextoSuave)
                if (estado is UiState.Error) Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = registrar,
                enabled = !enviando,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(stringResource(R.string.registro_titulo), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !enviando) {
                Text(stringResource(R.string.admin_cancelar), color = TextoSuave)
            }
        }
    )
}

@Composable
private fun CampoRegistro(
    valor: String,
    onCambiar: (String) -> Unit,
    etiqueta: Int,
    icono: Int,
    habilitado: Boolean,
    teclado: KeyboardType = KeyboardType.Text,
    accionIme: ImeAction = ImeAction.Next,
    claveVisible: Boolean = false,
    accion: (@Composable () -> Unit)? = null
) {
    val esClave = teclado == KeyboardType.Password
    OutlinedTextField(
        value = valor,
        onValueChange = onCambiar,
        label = { Text(stringResource(etiqueta)) },
        leadingIcon = { Icon(painterResource(icono), contentDescription = null) },
        trailingIcon = accion,
        modifier = Modifier.fillMaxWidth(),
        enabled = habilitado,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Vino,
            focusedLabelColor = Vino,
            focusedLeadingIconColor = Vino,
            unfocusedLeadingIconColor = Contorno,
            unfocusedTrailingIconColor = Contorno,
            cursorColor = Vino
        ),
        keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = accionIme),
        visualTransformation = if (esClave && !claveVisible) PasswordVisualTransformation()
            else VisualTransformation.None
    )
}
