package com.tecsup.saborperuano.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.domain.model.MAX_UNIDADES_POR_PLATO
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

internal fun soles(centimos: Long): String {
    val formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "S/ ${formato.format(BigDecimal.valueOf(centimos, 2))}"
}

@Composable
internal fun PrecioPlato(centimos: Int?) {
    Text(
        centimos?.let { soles(it.toLong()) } ?: stringResource(R.string.precio_pendiente),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun SelectorCantidad(
    cantidad: Int,
    onCambiar: (Int) -> Unit,
    habilitado: Boolean = true
) {
    val menos = stringResource(R.string.cantidad_menos)
    val mas = stringResource(R.string.cantidad_mas)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { onCambiar(cantidad - 1) },
            enabled = habilitado && cantidad > 1,
            modifier = Modifier.semantics { contentDescription = menos }
        ) { Text("−") }
        Text(stringResource(R.string.cantidad_actual, cantidad))
        OutlinedButton(
            onClick = { onCambiar(cantidad + 1) },
            enabled = habilitado && cantidad < MAX_UNIDADES_POR_PLATO,
            modifier = Modifier.semantics { contentDescription = mas }
        ) { Text("+") }
    }
}
