package com.tecsup.saborperuano.ui.navigation

import android.os.SystemClock
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val DuracionWelcomeMs = 6_000L

// Colores de marca (los mismos del inicio de sesión).
private val Vino = Color(0xFF691E25)
private val VinoOscuro = Color(0xFF3E0D14)
private val VinoClaro = Color(0xFF9B2C35)
private val Crema = Color(0xFFF5F1E2)
private val Dorado = Color(0xFFF2C14E)
private val RojoPeru = Color(0xFFD91023)
private val TextoOscuro = Color(0xFF261D19)
private val TextoSuave = Color(0xFF625650)

private data class Ventaja(@DrawableRes val icono: Int, @StringRes val texto: Int)

private val ventajas = listOf(
    Ventaja(R.drawable.ic_menu_buscar, R.string.welcome_ventaja_menu),
    Ventaja(R.drawable.ic_menu_favorito_activo, R.string.welcome_ventaja_favoritos),
    Ventaja(R.drawable.ic_menu_carrito, R.string.welcome_ventaja_pedido)
)

@Composable
internal fun WelcomeScreen(onFinalizar: () -> Unit, modifier: Modifier = Modifier) {
    // La hora de inicio sobrevive a la rotación: el Welcome no vuelve a empezar al girar.
    var inicio by rememberSaveable { mutableStateOf<Long?>(null) }
    val yaIniciado = inicio != null
    val progreso = remember {
        Animatable(
            inicio?.let { ((SystemClock.elapsedRealtime() - it) / DuracionWelcomeMs.toFloat()).coerceIn(0f, 1f) } ?: 0f
        )
    }
    val entrada = remember { Animatable(if (yaIniciado) 1f else 0f) }
    var terminado by remember { mutableStateOf(false) }
    val finalizar: () -> Unit = {
        if (!terminado) {
            terminado = true
            onFinalizar()
        }
    }

    LaunchedEffect(Unit) {
        entrada.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        val desde = inicio ?: SystemClock.elapsedRealtime().also { inicio = it }
        val restante = DuracionWelcomeMs - (SystemClock.elapsedRealtime() - desde)
        if (restante > 0) progreso.animateTo(1f, tween(restante.toInt(), easing = LinearEasing))
        finalizar()
    }

    val aparecer: (Float) -> Float = { retraso -> ((entrada.value - retraso) / (1f - retraso)).coerceIn(0f, 1f) }
    val desplazamiento = with(LocalDensity.current) { 28.dp.toPx() }

    Surface(modifier = modifier.fillMaxSize(), color = Crema) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta principal con los platos en órbita.
            HeroPlatos(
                modifier = Modifier.fillMaxWidth().weight(1f).widthIn(max = 520.dp).graphicsLayer {
                    val valor = aparecer(0f)
                    alpha = valor
                    scaleX = 0.92f + 0.08f * valor
                    scaleY = 0.92f + 0.08f * valor
                }
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.widthIn(max = 520.dp).graphicsLayer {
                    val valor = aparecer(0.25f)
                    alpha = valor
                    translationY = (1f - valor) * desplazamiento
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    stringResource(R.string.welcome_saludo),
                    color = VinoClaro,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    stringResource(R.string.app_name),
                    color = Vino,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.welcome_frase),
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ventajas.forEachIndexed { indice, ventaja ->
                    VentajaWelcome(
                        ventaja,
                        Modifier.weight(1f).graphicsLayer {
                            val valor = aparecer(0.4f + indice * 0.15f)
                            alpha = valor
                            translationY = (1f - valor) * desplazamiento
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Pie animado: puntos que saltan, barra de avance y sello "Hecho en Perú".
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).graphicsLayer { alpha = aparecer(0.6f) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PuntosCargando()
                    Text(
                        stringResource(R.string.welcome_preparando),
                        color = TextoSuave,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                BarraProgreso(progreso = { progreso.value })
                SelloPeru()
            }
        }
    }
}

@Composable
private fun HeroPlatos(modifier: Modifier = Modifier) {
    val movimiento = rememberInfiniteTransition(label = "Platos del Welcome")
    val giro by movimiento.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing)),
        label = "Órbita"
    )
    val flotar by movimiento.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Flotar"
    )
    val brillo by movimiento.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_400, easing = LinearEasing), RepeatMode.Reverse),
        label = "Destellos"
    )
    val forma = RoundedCornerShape(32.dp)

    BoxWithConstraints(
        modifier = modifier
            .shadow(18.dp, forma, ambientColor = Vino, spotColor = Vino)
            .clip(forma)
            .background(Brush.linearGradient(listOf(VinoClaro, Vino, VinoOscuro))),
        contentAlignment = Alignment.Center
    ) {
        val lado = minOf(maxWidth, maxHeight)
        val platoGrande = lado * 0.46f
        val platoChico = lado * 0.21f
        val radioOrbita = lado * 0.36f

        // Fondo decorativo: círculos suaves, anillo punteado que gira y destellos.
        Canvas(Modifier.fillMaxSize()) {
            val centro = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Color.White.copy(alpha = 0.05f), size.minDimension * 0.62f, Offset(size.width * 0.05f, size.height * 0.05f))
            drawCircle(Color.White.copy(alpha = 0.04f), size.minDimension * 0.45f, Offset(size.width * 1.0f, size.height * 0.95f))
            drawCircle(
                Brush.radialGradient(
                    listOf(Dorado.copy(alpha = 0.35f), Color.Transparent),
                    center = centro, radius = platoGrande.toPx() * 1.1f
                ),
                platoGrande.toPx() * 1.1f, centro
            )
            drawCircle(
                Crema.copy(alpha = 0.35f), radioOrbita.toPx(), centro,
                style = Stroke(
                    width = 1.5.dp.toPx(), cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 10.dp.toPx()), giro)
                )
            )
            val destellos = listOf(Offset(0.14f, 0.20f), Offset(0.86f, 0.16f), Offset(0.10f, 0.78f), Offset(0.90f, 0.70f), Offset(0.52f, 0.08f))
            destellos.forEachIndexed { i, p ->
                val alfa = if (i % 2 == 0) brillo else 1.25f - brillo
                drawCircle(Dorado.copy(alpha = (alfa * 0.9f).coerceIn(0.1f, 0.9f)), (2.5f + i % 3).dp.toPx(), Offset(size.width * p.x, size.height * p.y))
            }
        }

        // Etiqueta de marca arriba a la izquierda.
        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
        ) {
            Text(
                stringResource(R.string.catalogo_marca),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Crema,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp
            )
        }

        // Platos pequeños girando en órbita.
        val chicos = listOf(R.drawable.platillo_aji, R.drawable.platillo_causa, R.drawable.platillo_arroz)
        val radioPx = with(LocalDensity.current) { radioOrbita.toPx() }
        chicos.forEachIndexed { i, foto ->
            val angulo = Math.toRadians((giro + i * 120f - 90f).toDouble())
            FotoCircular(
                foto, platoChico, borde = 3.dp,
                modifier = Modifier.graphicsLayer {
                    translationX = (cos(angulo) * radioPx).toFloat()
                    translationY = (sin(angulo) * radioPx).toFloat()
                }
            )
        }

        // Plato principal que flota suavemente.
        val flotarPx = with(LocalDensity.current) { 6.dp.toPx() }
        FotoCircular(
            R.drawable.platillo_lomo, platoGrande, borde = 6.dp,
            modifier = Modifier.graphicsLayer { translationY = flotar * flotarPx }
        )
    }
}

@Composable
private fun FotoCircular(@DrawableRes foto: Int, tamano: Dp, borde: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(foto),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .border(borde, Crema, CircleShape)
    )
}

@Composable
private fun VentajaWelcome(ventaja: Ventaja, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Vino.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(ventaja.icono), contentDescription = null, tint = Vino, modifier = Modifier.size(24.dp))
        }
        Text(
            stringResource(ventaja.texto),
            color = TextoOscuro,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BarraProgreso(progreso: () -> Float) {
    Canvas(Modifier.fillMaxWidth().height(4.dp)) {
        val y = size.height / 2f
        drawLine(Vino.copy(alpha = 0.15f), Offset(0f, y), Offset(size.width, y), size.height, StrokeCap.Round)
        drawLine(Vino, Offset(0f, y), Offset(size.width * progreso(), y), size.height, StrokeCap.Round)
    }
}

@Composable
private fun PuntosCargando() {
    val movimiento = rememberInfiniteTransition(label = "Puntos del Welcome")
    val fase by movimiento.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing)),
        label = "Salto"
    )
    val salto = with(LocalDensity.current) { 6.dp.toPx() }
    Row(
        modifier = Modifier.height(20.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) { i ->
            val local = (fase - i * 0.15f + 1f) % 1f
            val altura = if (local < 0.4f) sin(local / 0.4f * PI).toFloat() else 0f
            Box(
                Modifier.size(9.dp)
                    .graphicsLayer { translationY = -altura * salto }
                    .clip(CircleShape)
                    .background(if (i == 1) Dorado else Vino)
            )
        }
    }
}

@Composable
private fun SelloPeru() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.clip(RoundedCornerShape(3.dp))
                .border(1.dp, TextoSuave.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
        ) {
            Box(Modifier.width(7.dp).height(14.dp).background(RojoPeru))
            Box(Modifier.width(7.dp).height(14.dp).background(Color.White))
            Box(Modifier.width(7.dp).height(14.dp).background(RojoPeru))
        }
        Text(
            stringResource(R.string.welcome_hecho_peru),
            color = TextoSuave,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.sp
        )
    }
}
