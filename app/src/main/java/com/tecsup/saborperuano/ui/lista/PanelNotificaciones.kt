package com.tecsup.saborperuano.ui.lista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.data.local.TipoAvisoCliente
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Ventana propia de notificaciones del cliente.
 * Muestra cada aviso como una tarjeta con icono de color, título, descripción y hace cuánto pasó.
 * Los primeros [nuevos] avisos (los que no estaban leídos al abrir) se resaltan.
 */
@Composable
internal fun PanelNotificaciones(
    avisos: List<AvisoCliente>,
    nuevos: Int,
    onCerrar: () -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp).widthIn(max = 480.dp).fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        stringResource(R.string.catalogo_actividad),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        if (nuevos > 0) stringResource(R.string.notif_resumen_nuevas, nuevos)
                        else stringResource(R.string.notif_resumen_al_dia),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (avisos.isEmpty()) {
                    SinNotificaciones()
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(avisos) { indice, aviso ->
                            TarjetaNotificacion(aviso, nueva = indice < nuevos)
                        }
                    }
                }

                Button(
                    onClick = onCerrar,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.notif_cerrar), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun TarjetaNotificacion(aviso: AvisoCliente, nueva: Boolean) {
    val estilo = estiloDe(aviso.tipo)
    val esquema = MaterialTheme.colorScheme
    val descripcion = if (aviso.plato == null) stringResource(aviso.mensaje)
        else stringResource(aviso.mensaje, aviso.plato)
    val ahora = remember { System.currentTimeMillis() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (nueva) esquema.primary.copy(alpha = 0.06f).compositeOver(esquema.surface) else esquema.surface,
        border = BorderStroke(1.dp, if (nueva) esquema.primary.copy(alpha = 0.35f) else esquema.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(estilo.color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(estilo.icono),
                    contentDescription = null,
                    tint = estilo.color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(estilo.titulo),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = esquema.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (nueva) {
                        Text(
                            stringResource(R.string.notif_nueva),
                            style = MaterialTheme.typography.labelSmall,
                            color = esquema.primary,
                            modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                        )
                        Box(Modifier.size(8.dp).clip(CircleShape).background(esquema.primary))
                    }
                }
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = esquema.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (aviso.fecha > 0L) {
                    Text(
                        haceCuanto(aviso.fecha, ahora),
                        style = MaterialTheme.typography.labelSmall,
                        color = esquema.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun SinNotificaciones() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_menu_campana),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            stringResource(R.string.catalogo_sin_avisos),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.notif_vacio_detalle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun haceCuanto(fecha: Long, ahora: Long): String {
    val minutos = ((ahora - fecha).coerceAtLeast(0L) / 60_000L).toInt()
    val relativo = when {
        minutos < 1 -> stringResource(R.string.notif_ahora)
        minutos < 60 -> stringResource(R.string.notif_hace_minutos, minutos)
        else -> null
    }
    val momento = fechaYHora(fecha, ahora)
    return if (relativo != null) "$relativo · $momento" else momento
}

/** Ejemplos: "Hoy, 8:15 p. m.", "Ayer, 3:20 p. m.", "lun 9 nov, 10:05 a. m.", "15 oct 2025, 9:00 a. m." */
@Composable
private fun fechaYHora(fecha: Long, ahora: Long): String {
    val local = Locale.forLanguageTag("es-PE")
    val aviso = Calendar.getInstance().apply { timeInMillis = fecha }
    val hoy = Calendar.getInstance().apply { timeInMillis = ahora }
    val ayer = (hoy.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    fun mismoDia(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    val hora = SimpleDateFormat("h:mm a", local).format(Date(fecha))
    val dia = when {
        mismoDia(aviso, hoy) -> stringResource(R.string.notif_hoy)
        mismoDia(aviso, ayer) -> stringResource(R.string.notif_ayer)
        aviso.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) ->
            SimpleDateFormat("EEE d MMM", local).format(Date(fecha))
        else -> SimpleDateFormat("d MMM yyyy", local).format(Date(fecha))
    }
    return "$dia, $hora"
}

private data class EstiloAviso(
    val icono: Int,
    val titulo: Int,
    val color: Color
)

private fun estiloDe(tipo: TipoAvisoCliente?): EstiloAviso = when (tipo) {
    TipoAvisoCliente.FAVORITO ->
        EstiloAviso(R.drawable.ic_menu_favorito_activo, R.string.notif_titulo_favorito, Color(0xFFE0457B))
    TipoAvisoCliente.CARRITO ->
        EstiloAviso(R.drawable.ic_menu_carrito, R.string.notif_titulo_carrito, Color(0xFF5B7DB1))
    TipoAvisoCliente.WHATSAPP ->
        EstiloAviso(R.drawable.ic_perfil_pedido, R.string.notif_titulo_whatsapp, Color(0xFF25A35A))
    TipoAvisoCliente.PEDIDO_GUARDADO ->
        EstiloAviso(R.drawable.ic_perfil_pedido, R.string.notif_titulo_pedido, Color(0xFF2E9D57))
    TipoAvisoCliente.PEDIDO_WHATSAPP ->
        EstiloAviso(R.drawable.ic_perfil_pedido, R.string.notif_titulo_pedido_whatsapp, Color(0xFF25A35A))
    null ->
        EstiloAviso(R.drawable.ic_menu_campana, R.string.notif_titulo_general, Color(0xFF8A5A44))
}
