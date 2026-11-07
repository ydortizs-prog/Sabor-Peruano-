package com.tecsup.saborperuano.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun DetalleScreen(
    viewModel: DetalleViewModel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val errorFavorito by viewModel.errorFavorito.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TextButton(onClick = onVolver) {
            Text(stringResource(R.string.volver))
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val actual = estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(actual.mensaje, viewModel::cargar)
                is UiState.Exito -> RecetaDetalle(
                    receta = actual.datos,
                    errorFavorito = errorFavorito,
                    onAlternarFavorito = viewModel::alternarFavorito
                )
            }
        }
    }
}

@Composable
private fun RecetaDetalle(
    receta: Item,
    errorFavorito: String?,
    onAlternarFavorito: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(receta.titulo, style = MaterialTheme.typography.headlineMedium)
        Text(receta.subtitulo, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onAlternarFavorito) {
            val etiqueta = if (receta.esFavorito) {
                R.string.favorito_quitar
            } else {
                R.string.favorito_agregar
            }
            Text(stringResource(etiqueta))
        }
        errorFavorito?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        receta.porciones?.let {
            Text(stringResource(R.string.detalle_porciones, it))
        }
        Text(
            stringResource(R.string.detalle_ingredientes),
            style = MaterialTheme.typography.titleLarge
        )
        receta.ingredientes.forEach { ingrediente ->
            Text(text = "• $ingrediente", modifier = Modifier.fillMaxWidth())
        }

        Text(
            stringResource(R.string.detalle_preparacion),
            style = MaterialTheme.typography.titleLarge
        )
        receta.preparacion.forEachIndexed { indice, paso ->
            Text(text = "${indice + 1}. $paso", modifier = Modifier.fillMaxWidth())
        }
    }
}
