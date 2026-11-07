package com.tecsup.saborperuano.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.domain.model.Item

@Composable
internal fun TarjetasRecetas(
    recetas: List<Item>,
    onAbrirDetalle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(256.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recetas, key = Item::id) { receta ->
            TarjetaPlato(receta, onAbrirDetalle, Modifier.fillMaxWidth(), ampliada = false)
        }
    }
}

@Composable
internal fun TarjetaPlato(
    plato: Item,
    onAbrirDetalle: (String) -> Unit,
    modifier: Modifier = Modifier,
    ampliada: Boolean = true
) {
    ElevatedCard(
        onClick = { onAbrirDetalle(plato.id) },
        modifier = modifier,
        shape = if (ampliada) MaterialTheme.shapes.extraLarge else CardDefaults.elevatedShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        FotoReceta(plato, Modifier.fillMaxWidth().aspectRatio(if (ampliada) 1.5f else 1.8f))
        Column(
            modifier = Modifier.padding(if (ampliada) 20.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (ampliada) 8.dp else 5.dp)
        ) {
            Text(plato.titulo, style = MaterialTheme.typography.titleLarge)
            Text(
                plato.subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PrecioPlato(plato.precioCentimos)
        }
    }
}

@Composable
internal fun FotoReceta(receta: Item, modifier: Modifier = Modifier) {
    val recurso = when (receta.id) {
        "aji-de-gallina" -> R.drawable.platillo_aji
        "lomo-saltado" -> R.drawable.platillo_lomo
        "causa-limena" -> R.drawable.platillo_causa
        "arroz-con-pollo" -> R.drawable.platillo_arroz
        else -> null
    }
    if (recurso == null) {
        Surface(modifier, color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Text(receta.titulo, modifier = Modifier.padding(12.dp))
            }
        }
    } else {
        Image(
            painter = painterResource(recurso),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
