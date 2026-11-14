package com.tecsup.saborperuano.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.components.Cargando

private val coloresAcceso = lightColorScheme(
    primary = Color(0xFF691E25),
    onPrimary = Color.White,
    background = Color(0xFFF5F1E2),
    onBackground = Color(0xFF261D19),
    surface = Color(0xFFF5F1E2),
    onSurface = Color(0xFF261D19),
    onSurfaceVariant = Color(0xFF625650),
    outline = Color(0xFF8B7C74)
)

@Composable
fun LoginScreen(viewModel: LoginViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val recuperacion by viewModel.recuperacion.collectAsStateWithLifecycle()
    val registro by viewModel.registro.collectAsStateWithLifecycle()
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    var dialogoAbierto by rememberSaveable { mutableStateOf(false) }
    var registroAbierto by rememberSaveable { mutableStateOf(false) }
    val cargando = estado == UiState.Cargando
    val foco = LocalFocusManager.current
    val iniciarSesion = {
        foco.clearFocus()
        claveVisible = false
        viewModel.iniciarSesion(correo, clave)
    }

    MaterialTheme(colorScheme = coloresAcceso, typography = MaterialTheme.typography) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .then(modifier).imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.catalogo_marca),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.login_bienvenida),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                CampoAcceso(
                    valor = correo,
                    onCambio = { correo = it },
                    etiqueta = R.string.login_correo,
                    pista = R.string.login_correo_pista,
                    icono = R.drawable.ic_acceso_correo,
                    habilitado = !cargando,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
                CampoAcceso(
                    valor = clave,
                    onCambio = { clave = it },
                    etiqueta = R.string.login_clave,
                    pista = R.string.login_clave_pista,
                    icono = R.drawable.ic_acceso_candado,
                    habilitado = !cargando,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    acciones = KeyboardActions(onDone = { if (!cargando) iniciarSesion() }),
                    transformacion = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    accion = {
                        IconButton(onClick = { claveVisible = !claveVisible }, enabled = !cargando) {
                            Icon(
                                painterResource(
                                    if (claveVisible) R.drawable.ic_acceso_visible else R.drawable.ic_acceso_oculto
                                ),
                                contentDescription = stringResource(
                                    if (claveVisible) R.string.login_ocultar_clave else R.string.login_mostrar_clave
                                )
                            )
                        }
                    }
                )
                // "¿Olvidaste tu contraseña?" alineado a la derecha, debajo de la contraseña.
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    enabled = !cargando,
                    onClick = {
                        foco.clearFocus()
                        claveVisible = false
                        viewModel.limpiarRecuperacion()
                        dialogoAbierto = true
                    }
                ) {
                    Text(
                        stringResource(R.string.login_olvidaste_clave),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = iniciarSesion,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(50),
                    enabled = !cargando
                ) {
                    Text(
                        stringResource(R.string.login_entrar),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.login_primera_vez),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    enabled = !cargando,
                    onClick = {
                        foco.clearFocus()
                        clave = ""
                        claveVisible = false
                        viewModel.limpiarRegistro()
                        registroAbierto = true
                    }
                ) {
                    Text(
                        stringResource(R.string.login_registrate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                when (val actual = estado) {
                    UiState.Cargando -> Cargando()
                    is UiState.Error -> {
                        Text(actual.mensaje, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::restaurarSesion) {
                            Text(stringResource(R.string.login_reintentar_sesion))
                        }
                    }
                    is UiState.Exito -> Unit
                }

            }
        }
        if (registroAbierto) {
            RegistroDialogo(
                correoInicial = correo,
                estado = registro,
                onRegistrar = viewModel::registrar,
                onCerrar = {
                    registroAbierto = false
                    viewModel.limpiarRegistro()
                }
            )
        }
        if (dialogoAbierto) {
            RecuperarClaveDialogo(
                correoInicial = correo,
                estado = recuperacion,
                onEnviar = viewModel::solicitarRestablecimiento,
                onCerrar = {
                    dialogoAbierto = false
                    viewModel.limpiarRecuperacion()
                }
            )
        }
    }
}

@Composable
private fun CampoAcceso(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: Int,
    pista: Int,
    icono: Int,
    habilitado: Boolean,
    teclado: KeyboardOptions,
    acciones: KeyboardActions = KeyboardActions.Default,
    transformacion: VisualTransformation = VisualTransformation.None,
    accion: (@Composable () -> Unit)? = null
) {
    val descripcion = stringResource(etiqueta)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(descripcion, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = descripcion },
            placeholder = { Text(stringResource(pista)) },
            leadingIcon = { Icon(painterResource(icono), contentDescription = null) },
            trailingIcon = accion,
            singleLine = true,
            enabled = habilitado,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline
            ),
            keyboardOptions = teclado,
            keyboardActions = acciones,
            visualTransformation = transformacion
        )
    }
}

@Composable
private fun RecuperarClaveDialogo(
    correoInicial: String,
    estado: EstadoRecuperacion,
    onEnviar: (String) -> Unit,
    onCerrar: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf(correoInicial) }
    val enviando = estado == EstadoRecuperacion.Enviando
    val solicitado = estado == EstadoRecuperacion.Solicitado
    val error = estado as? EstadoRecuperacion.Error
    AlertDialog(
        onDismissRequest = { if (!enviando) onCerrar() },
        title = { Text(stringResource(R.string.login_recuperar_titulo)) },
        text = {
            if (solicitado) {
                Text(stringResource(R.string.login_recuperar_solicitado))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.login_recuperar_indicacion))
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.login_correo)) },
                        singleLine = true,
                        enabled = !enviando,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (!enviando) onEnviar(correo) }),
                        isError = error != null,
                        supportingText = error?.let { aviso -> { Text(stringResource(aviso.mensaje)) } }
                    )
                    if (enviando) Text(stringResource(R.string.login_recuperar_enviando))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !enviando,
                onClick = { if (solicitado) onCerrar() else onEnviar(correo) }
            ) {
                Text(stringResource(if (solicitado) R.string.login_recuperar_listo else R.string.login_recuperar_enviar))
            }
        },
        dismissButton = {
            if (!solicitado) {
                TextButton(onClick = onCerrar, enabled = !enviando) {
                    Text(stringResource(R.string.admin_cancelar))
                }
            }
        }
    )
}
