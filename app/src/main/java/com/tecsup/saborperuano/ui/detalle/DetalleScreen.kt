package com.tecsup.saborperuano.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.domain.model.Item
import com.tecsup.saborperuano.ui.UiState
import com.tecsup.saborperuano.ui.components.Cargando
import com.tecsup.saborperuano.ui.components.ErrorView
import com.tecsup.saborperuano.ui.components.FotoReceta
import com.tecsup.saborperuano.ui.components.PrecioPlato
import com.tecsup.saborperuano.ui.components.SelectorCantidad
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip

@Composable
fun DetalleScreen(
    viewModel: DetalleViewModel,
    onVolver: () -> Unit,
    onAbrirCarrito: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val cantidad by viewModel.cantidad.collectAsStateWithLifecycle()
    val errorFavorito by viewModel.errorFavorito.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onVolver) {
            Text(stringResource(R.string.volver))
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val actual = estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(actual.mensaje, viewModel::cargar)
                is UiState.Exito -> FichaProducto(
                    plato = actual.datos,
                    cantidad = cantidad,
                    errorFavorito = errorFavorito,
                    onCambiarCantidad = viewModel::cambiarCantidad,
                    onAlternarFavorito = viewModel::alternarFavorito,
                    onAgregarAlCarrito = {
                        if (viewModel.agregarAlCarrito()) onAbrirCarrito()
                    },
                    onAbrirCarrito = onAbrirCarrito
                )
            }
        }
    }
}

@Composable
private fun FichaProducto(
    plato: Item,
    cantidad: Int,
    errorFavorito: String?,
    onCambiarCantidad: (Int) -> Unit,
    onAlternarFavorito: () -> Unit,
    onAgregarAlCarrito: () -> Unit,
    onAbrirCarrito: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FotoReceta(plato, Modifier.fillMaxWidth().aspectRatio(1.8f).clip(MaterialTheme.shapes.large))
        Text(plato.titulo, style = MaterialTheme.typography.headlineMedium)
        Text(plato.subtitulo, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onAlternarFavorito) {
            val etiqueta = if (plato.esFavorito) {
                R.string.favorito_quitar
            } else {
                R.string.favorito_agregar
            }
            Text(stringResource(etiqueta))
        }
        errorFavorito?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        PrecioPlato(plato.precioCentimos)
        val disponible = plato.precioCentimos != null
        SelectorCantidad(cantidad, onCambiarCantidad, habilitado = disponible)
        Button(
            onClick = onAgregarAlCarrito,
            enabled = disponible,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.carrito_agregar))
        }
        TextButton(onClick = onAbrirCarrito) {
            Text(stringResource(R.string.carrito_titulo))
        }
    }
}
