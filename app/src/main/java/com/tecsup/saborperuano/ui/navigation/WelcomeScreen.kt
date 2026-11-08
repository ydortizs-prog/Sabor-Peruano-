package com.tecsup.saborperuano.ui.navigation

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.saborperuano.R
import com.tecsup.saborperuano.ui.theme.TemaCatalogo
import kotlinx.coroutines.delay
import kotlin.math.atan2

private const val DuracionEntradaMs = 2_000L
private const val DuracionWelcomeMs = 5_000L
private const val ProporcionEscena = 1.25f
private const val ProporcionMoto = 120f / 72f
private val InicioRuta = Offset(0.08f, 0.87f)
private val FinRuta = Offset(0.76f, 0.30f)

@Composable
internal fun WelcomeScreen(onFinalizar: () -> Unit, modifier: Modifier = Modifier) {
    var inicio by rememberSaveable { mutableStateOf<Long?>(null) }
    val avance = remember {
        val transcurrido = inicio?.let { SystemClock.elapsedRealtime() - it } ?: 0L
        Animatable((transcurrido / DuracionEntradaMs.toFloat()).coerceIn(0f, 1f))
    }
    var mostrarFrase by remember { mutableStateOf(avance.value == 1f) }
    val opacidadFrase = animateFloatAsState(
        targetValue = if (mostrarFrase) 1f else 0f,
        animationSpec = tween(320),
        label = "Lema del Welcome"
    )

    LaunchedEffect(Unit) {
        if (inicio == null) {
            // Deja que el fondo fijo se dibuje antes de iniciar el recorrido.
            withFrameNanos { }
            withFrameNanos { }
            inicio = SystemClock.elapsedRealtime()
        }
    }
    LaunchedEffect(inicio) {
        val desde = inicio ?: return@LaunchedEffect
        val restante = DuracionEntradaMs - (SystemClock.elapsedRealtime() - desde)
        if (restante > 0) {
            avance.animateTo(1f, tween(restante.toInt(), easing = LinearEasing))
        } else {
            avance.snapTo(1f)
        }
    }
    LaunchedEffect(inicio) {
        val desde = inicio ?: return@LaunchedEffect
        delay((DuracionEntradaMs - (SystemClock.elapsedRealtime() - desde)).coerceAtLeast(0L))
        mostrarFrase = true
        delay((DuracionWelcomeMs - (SystemClock.elapsedRealtime() - desde)).coerceAtLeast(0L))
        onFinalizar()
    }

    TemaCatalogo {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background)
                    )
                ),
                contentAlignment = Alignment.Center
            ) {
                val compacto = maxHeight < 440.dp
                val altoDisponible = (maxHeight - if (compacto) 176.dp else 222.dp)
                    .coerceAtLeast(maxHeight * 0.3f)
                val anchoEscena = minOf(maxWidth * 0.90f, altoDisponible * ProporcionEscena, 470.dp)
                val colores = MaterialTheme.colorScheme

                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (compacto) 8.dp else 14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colores.primaryContainer,
                        contentColor = colores.onPrimaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.catalogo_marca),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        color = colores.primary,
                        style = if (compacto) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    RecorridoWelcome(ancho = anchoEscena, avance = { avance.value })

                    Box(
                        modifier = Modifier.heightIn(min = if (compacto) 60.dp else 76.dp)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mostrarFrase) {
                            Column(
                                modifier = Modifier.graphicsLayer { alpha = opacidadFrase.value },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.welcome_saludo),
                                    color = colores.primary,
                                    style = if (compacto) MaterialTheme.typography.titleMedium
                                        else MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.welcome_frase),
                                    color = colores.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecorridoWelcome(
    ancho: Dp,
    avance: () -> Float,
    modifier: Modifier = Modifier
) {
    val alto = ancho / ProporcionEscena
    val anchoMoto = ancho * 0.37f
    val altoMoto = anchoMoto / ProporcionMoto
    val inicioX = ancho * InicioRuta.x
    val inicioY = alto * InicioRuta.y
    val recorridoX = ancho * (FinRuta.x - InicioRuta.x)
    val recorridoY = alto * (FinRuta.y - InicioRuta.y)
    val angulo = Math.toDegrees(atan2(recorridoY.value, recorridoX.value).toDouble()).toFloat()

    val colores = MaterialTheme.colorScheme
    val marco = RoundedCornerShape(26.dp)

    Box(
        modifier.width(ancho)
            .aspectRatio(ProporcionEscena)
            .clip(marco)
            .background(colores.surface)
            .border(1.dp, colores.onSurfaceVariant.copy(alpha = 0.12f), marco)
    ) {
        CallesWelcome(avance, Modifier.matchParentSize())

        MotoWelcome(
            ancho = anchoMoto,
            modifier = Modifier.graphicsLayer {
                val progreso = avance()
                translationX = (inicioX + recorridoX * progreso - anchoMoto / 2f).toPx()
                translationY = (inicioY + recorridoY * progreso - altoMoto / 2f).toPx()
                rotationZ = angulo
            }
        )
    }
}

@Composable
private fun CallesWelcome(avance: () -> Float, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val calles = colores.onSurfaceVariant.copy(alpha = 0.12f)
    val carretera = colores.onSurfaceVariant.copy(alpha = 0.21f)
    val linea = colores.background.copy(alpha = 0.90f)

    Canvas(modifier) {
        fun punto(x: Float, y: Float) = Offset(size.width * x, size.height * y)

        val parque = colores.secondaryContainer.copy(alpha = 0.62f)
        val manzana = colores.onSurfaceVariant.copy(alpha = 0.06f)
        val radio = CornerRadius(5.dp.toPx())
        drawRoundRect(parque, punto(0.055f, 0.08f), Size(size.width * 0.17f, size.height * 0.15f), radio)
        drawRoundRect(manzana, punto(0.39f, 0.10f), Size(size.width * 0.20f, size.height * 0.13f), radio)
        drawRoundRect(manzana, punto(0.41f, 0.74f), Size(size.width * 0.15f, size.height * 0.14f), radio)
        drawRoundRect(parque, punto(0.69f, 0.70f), Size(size.width * 0.15f, size.height * 0.18f), radio)

        val callesLaterales = Path().apply {
            moveTo(-0.02f * size.width, 0.50f * size.height)
            lineTo(0.30f * size.width, 0.21f * size.height)
            lineTo(0.62f * size.width, 0.49f * size.height)
            lineTo(0.62f * size.width, 0.97f * size.height)
            moveTo(0.30f * size.width, 0.21f * size.height)
            lineTo(0.30f * size.width, 0.06f * size.height)
            moveTo(0.32f * size.width, 0.67f * size.height)
            lineTo(0.32f * size.width, 0.97f * size.height)
            lineTo(0.89f * size.width, 0.97f * size.height)
            moveTo(0.91f * size.width, 0.20f * size.height)
            lineTo(0.91f * size.width, 0.69f * size.height)
        }
        drawPath(callesLaterales, calles, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

        val origen = punto(-0.07f, 0.996f)
        val destino = punto(0.99f, 0.107f)
        drawLine(carretera, origen, destino, strokeWidth = size.width * 0.17f, cap = StrokeCap.Round)
        drawLine(
            linea, origen, destino, strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 9.dp.toPx()))
        )
        val origenRuta = punto(InicioRuta.x, InicioRuta.y)
        val puntoMoto = lerp(origenRuta, punto(FinRuta.x, FinRuta.y), avance())
        drawLine(colores.primary.copy(alpha = 0.70f), origenRuta, puntoMoto, 3.dp.toPx(), StrokeCap.Round)
        drawCircle(colores.primaryContainer, 6.dp.toPx(), origenRuta)
        drawCircle(colores.primary, 3.dp.toPx(), origenRuta)

        val destinoRuta = punto(0.82f, 0.12f)
        val casa = Path().apply {
            val unidad = 1.dp.toPx()
            moveTo(destinoRuta.x - 6 * unidad, destinoRuta.y - unidad)
            lineTo(destinoRuta.x, destinoRuta.y - 6 * unidad)
            lineTo(destinoRuta.x + 6 * unidad, destinoRuta.y - unidad)
            lineTo(destinoRuta.x + 4 * unidad, destinoRuta.y - unidad)
            lineTo(destinoRuta.x + 4 * unidad, destinoRuta.y + 5 * unidad)
            lineTo(destinoRuta.x - 4 * unidad, destinoRuta.y + 5 * unidad)
            lineTo(destinoRuta.x - 4 * unidad, destinoRuta.y - unidad)
            close()
        }
        drawCircle(colores.primaryContainer, 13.dp.toPx(), destinoRuta)
        drawPath(casa, colores.onPrimaryContainer)
    }
}

@Composable
private fun MotoWelcome(ancho: Dp, modifier: Modifier = Modifier) {
    val alto = ancho / ProporcionMoto
    val tamanoLetra = with(LocalDensity.current) { (ancho * 0.055f).toSp() }

    Box(modifier.width(ancho).aspectRatio(ProporcionMoto)) {
        Image(
            painter = painterResource(R.drawable.welcome_moto),
            contentDescription = stringResource(R.string.welcome_moto_descripcion),
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = stringResource(R.string.welcome_marca),
            modifier = Modifier
                .offset(x = ancho * 0.047f, y = alto * 0.386f)
                .width(ancho * 0.28f),
            color = Color(0xFFFFF1D7),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = tamanoLetra,
                lineHeight = tamanoLetra,
                letterSpacing = 0.sp
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = false
        )
    }
}
