package com.tecsup.saborperuano.ui.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun ListaScreen(
    viewModel: ListaViewModel,
    onAbrirDetalle: (String) -> Unit,
    onAbrirFavoritos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sinConexion by viewModel.sinConexion.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = onAbrirFavoritos) {
                Text(stringResource(R.string.favoritos_titulo))
            }
        }

        if (sinConexion && (estado as? UiState.Exito)?.datos?.isNotEmpty() == true) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(stringResource(R.string.catalogo_sin_conexion))
                    TextButton(onClick = viewModel::refrescar) {
                        Text(stringResource(R.string.catalogo_reintentar))
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (val actual = estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(actual.mensaje, viewModel::refrescar)
                is UiState.Exito -> {
                    if (actual.datos.isEmpty()) {
                        Text(stringResource(R.string.catalogo_vacio))
                    } else {
                        TarjetasRecetas(actual.datos, onAbrirDetalle)
                    }
                }
            }
        }
    }
}

@Composable
internal fun TarjetasRecetas(recetas: List<Item>, onAbrirDetalle: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recetas, key = { it.id }) { receta ->
            Card(
                onClick = { onAbrirDetalle(receta.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = receta.titulo,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = receta.subtitulo,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
