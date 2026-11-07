package com.tecsup.saborperuano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.components.Cargando
import com.tecsup.saborperuano.ui.components.ErrorView

@Composable
fun AdminScreen(viewModel: AdminViewModel, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var confirmar by rememberSaveable { mutableStateOf(false) }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text(stringResource(R.string.volver)) }
            Text(stringResource(R.string.admin_panel), style = MaterialTheme.typography.headlineLarge)
            when (val actual = estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(actual.mensaje, viewModel::cargar)
                is UiState.Exito -> {
                    val resumen = actual.datos
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.admin_guardados, resumen.guardados))
                            Text(stringResource(R.string.admin_favoritos, resumen.favoritos))
                        }
                    }
                    Button(onClick = viewModel::sincronizar) {
                        Text(stringResource(R.string.admin_sincronizar))
                    }
                    OutlinedButton(onClick = { confirmar = true }) {
                        Text(stringResource(R.string.admin_limpiar))
                    }
                    resumen.mensaje?.let { Text(it) }
                }
            }
        }
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
