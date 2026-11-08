package com.tecsup.saborperuano.ui.carrito

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.components.Cargando
import com.tecsup.saborperuano.ui.components.SelectorCantidad
import com.tecsup.saborperuano.ui.components.soles

@Composable
fun CarritoScreen(
    viewModel: CarritoViewModel,
    onVolver: () -> Unit,
    onRevisarPedido: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text(stringResource(R.string.volver)) }
            Text(stringResource(R.string.carrito_titulo), style = MaterialTheme.typography.headlineLarge)
            when (val actual = estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> Text(actual.mensaje, color = MaterialTheme.colorScheme.error)
                is UiState.Exito -> {
                    val resumen = actual.datos
                    if (resumen.lineas.isEmpty()) {
                        Text(stringResource(R.string.carrito_vacio))
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(resumen.lineas, key = { it.id }) { linea ->
                                ElevatedCard(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(linea.nombre, style = MaterialTheme.typography.titleLarge)
                                        Text(soles(linea.precioCentimos.toLong()))
                                        SelectorCantidad(linea.cantidad, { viewModel.cambiarCantidad(linea.id, it) })
                                        Text(soles(linea.subtotalCentimos), style = MaterialTheme.typography.titleMedium)
                                        TextButton(onClick = { viewModel.quitar(linea.id) }) {
                                            Text(stringResource(R.string.carrito_quitar))
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            stringResource(R.string.carrito_total, soles(resumen.totalCentimos)),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Button(onClick = onRevisarPedido, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.carrito_revisar))
                        }
                    }
                }
            }
        }
    }
}
