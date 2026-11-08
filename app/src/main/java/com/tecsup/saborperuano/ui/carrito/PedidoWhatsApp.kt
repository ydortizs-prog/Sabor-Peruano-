package com.tecsup.saborperuano.ui.carrito

import com.tecsup.saborperuano.ui.components.soles
import java.net.URLEncoder

internal fun mensajeParaWhatsApp(recibo: EnvioPedido.Guardado): String {
    val solicitud = recibo.solicitud
    return mensajeParaWhatsApp(
        contacto = DatosPedido(
            nombre = solicitud.nombre,
            telefono = solicitud.telefono,
            entrega = if (solicitud.entrega == "delivery") Entrega.DELIVERY else Entrega.RECOJO,
            direccion = solicitud.direccion.orEmpty(),
            observaciones = solicitud.observaciones
        ),
        resumen = ResumenCarrito(recibo.lineas),
        numeroPedido = recibo.pedido.pedidoId,
        totalCentimos = recibo.pedido.totalCentimos
    )
}

internal fun mensajeParaWhatsApp(
    contacto: DatosPedido,
    resumen: ResumenCarrito,
    numeroPedido: String? = null,
    totalCentimos: Long = resumen.totalCentimos
): String = buildString {
    val delivery = contacto.entrega == Entrega.DELIVERY
    appendLine("SABOR PERUANO — NUEVO PEDIDO")
    appendLine()
    appendLine("Cliente: ${contacto.nombre.trim()}")
    appendLine("Teléfono: ${contacto.telefono.trim()}")
    appendLine()
    appendLine("Productos:")
    resumen.lineas.forEach { linea ->
        appendLine("- ${linea.cantidad} ${linea.nombre} — ${soles(linea.subtotalCentimos)}")
    }
    appendLine()
    if (delivery && numeroPedido == null) {
        appendLine("Subtotal de platos: ${soles(resumen.totalCentimos)}")
        appendLine("Costo de delivery por confirmar.")
    } else {
        appendLine("Total: ${soles(totalCentimos)}")
    }
    appendLine("Entrega: ${if (delivery) "Delivery" else "Recojo"}")
    if (delivery) appendLine("Dirección: ${contacto.direccion.trim()}")
    if (contacto.observaciones.isNotBlank()) {
        appendLine("Observaciones: ${contacto.observaciones.trim()}")
    }
    if (numeroPedido != null) appendLine("Número de pedido: $numeroPedido")
}

internal fun enlaceWhatsApp(numero: String, mensaje: String): String {
    require(numero.matches(Regex("[1-9][0-9]{6,14}")))
    val textoCodificado = URLEncoder.encode(mensaje, "UTF-8").replace("+", "%20")
    return "https://wa.me/$numero?text=$textoCodificado"
}
