package com.tecsup.saborperuano.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.domain.model.Item
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    ampliada: Boolean = true,
    estiloCliente: Boolean = false,
    valoracion: Int? = null,
    onAbrirValoracion: (() -> Unit)? = null,
    onAlternarFavorito: (() -> Unit)? = null
) {
    ElevatedCard(
        onClick = { onAbrirDetalle(plato.id) },
        modifier = modifier,
        shape = when {
            estiloCliente -> MaterialTheme.shapes.large
            ampliada -> MaterialTheme.shapes.extraLarge
            else -> CardDefaults.elevatedShape
        },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // Foto más baja para que la tarjeta muestre nombre, precio y botones sin tanto desplazamiento.
        val proporcion = if (ampliada && !estiloCliente) 1.8f else 2.2f
        FotoReceta(plato, Modifier.fillMaxWidth().aspectRatio(proporcion).heightIn(max = 180.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(
                if (ampliada && !estiloCliente) 20.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (ampliada) 8.dp else 5.dp)
        ) {
            Text(plato.titulo, style = MaterialTheme.typography.titleLarge)
            Text(
                plato.subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!estiloCliente) {
                PrecioPlato(plato.precioCentimos)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        plato.precioCentimos?.let { soles(it.toLong()) } ?: stringResource(R.string.precio_pendiente),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (onAbrirValoracion != null) {
                        TextButton(onClick = onAbrirValoracion) {
                            Text(
                                if (valoracion == null) stringResource(R.string.catalogo_calificar)
                                else stringResource(R.string.catalogo_valoracion, valoracion)
                            )
                        }
                    }
                    if (onAlternarFavorito != null) {
                        IconToggleButton(
                            checked = plato.esFavorito,
                            onCheckedChange = { onAlternarFavorito() },
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                contentColor = MaterialTheme.colorScheme.primary,
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            val icono = if (plato.esFavorito) R.drawable.ic_menu_favorito_activo
                                else R.drawable.ic_menu_favorito
                            val etiqueta = if (plato.esFavorito) R.string.favorito_quitar
                                else R.string.favorito_agregar
                            Icon(painterResource(icono), contentDescription = stringResource(etiqueta))
                        }
                    }
                }
            }
        }
    }
}

internal const val PrefijoFotoLocal = "foto-local:"

@Composable
internal fun FotoReceta(receta: Item, modifier: Modifier = Modifier) {
    val referencia = receta.imagenUrl?.takeIf { it.startsWith(PrefijoFotoLocal) }
    val contexto = LocalContext.current
    val foto by produceState<ImageBitmap?>(null, referencia) {
        value = null
        if (referencia == null) return@produceState
        val uri = Uri.parse(referencia.removePrefix(PrefijoFotoLocal))
        if (uri.scheme != "content") return@produceState
        value = withContext(Dispatchers.IO) {
            try {
                val resolver = contexto.contentResolver
                if (resolver.persistedUriPermissions.none { it.uri == uri && it.isReadPermission }) {
                    return@withContext null
                }
                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                if (limites.outWidth <= 0 || limites.outHeight <= 0) return@withContext null
                var muestra = 1
                val lado = maxOf(limites.outWidth, limites.outHeight)
                while (lado / muestra > 1600) muestra *= 2
                val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
                resolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opciones)?.asImageBitmap()
                }
            } catch (_: IOException) {
                null
            } catch (_: SecurityException) {
                null
            }
        }
    }
    foto?.let {
        Image(it, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
        return
    }
    val remota = receta.imagenUrl?.takeIf { it.startsWith("https://") || it.startsWith("http://") }
    val fotoRemota by produceState(FotosRemotas.enCache(remota), remota) {
        if (remota == null || value != null) return@produceState
        value = withContext(Dispatchers.IO) { FotosRemotas.descargar(remota) }
    }
    fotoRemota?.let {
        Image(it, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
        return
    }
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

/** Descarga las fotos de TheMealDB y guarda en memoria las últimas para no repetir descargas. */
private object FotosRemotas {
    private val cache = LruCache<String, ImageBitmap>(40)

    fun enCache(url: String?): ImageBitmap? = url?.let { cache.get(it) }

    fun descargar(url: String): ImageBitmap? {
        return try {
            val conexion = URL(url).openConnection() as HttpURLConnection
            conexion.connectTimeout = 10_000
            conexion.readTimeout = 15_000
            try {
                if (conexion.responseCode !in 200..299) return null
                val bytes = conexion.inputStream.use { it.readBytes() }
                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
                if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
                var muestra = 1
                while (maxOf(limites.outWidth, limites.outHeight) / muestra > 800) muestra *= 2
                val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)?.asImageBitmap()
                    ?.also { cache.put(url, it) }
            } finally {
                conexion.disconnect()
            }
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }
}
