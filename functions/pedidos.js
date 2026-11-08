const { createHash } = require("node:crypto");

class ErrorPedido extends Error {
  constructor(codigo, mensaje) {
    super(mensaje);
    this.codigo = codigo;
  }
}

function invalido() {
  throw new ErrorPedido("invalid-argument", "Revisa los datos del pedido.");
}

function texto(valor, maximo, obligatorio = true) {
  if (!obligatorio && valor == null) return "";
  if (typeof valor !== "string" || valor.length > maximo) invalido();
  const limpio = valor.trim();
  if (obligatorio && !limpio) invalido();
  return limpio;
}

function validarPedido(datos) {
  if (!datos || typeof datos !== "object") invalido();
  const { intento, entrega, lineas, subtotalCentimos } = datos;
  if (typeof intento !== "string" || !/^[a-f0-9-]{36}$/.test(intento)) invalido();
  if (entrega !== "recojo" && entrega !== "delivery") invalido();
  if (!Number.isSafeInteger(subtotalCentimos) || subtotalCentimos <= 0) invalido();
  if (!Array.isArray(lineas) || lineas.length < 1 || lineas.length > 30) invalido();

  const nombre = texto(datos.nombre, 100);
  const telefono = texto(datos.telefono, 40);
  const digitos = telefono.replace(/[^0-9]/g, "").length;
  if (!/^\+?[0-9 ()-]+$/.test(telefono) || digitos < 7 || digitos > 15) invalido();

  const ids = new Set();
  const productos = lineas.map((linea) => {
    const { id, cantidad } = linea ?? {};
    if (typeof id !== "string" || !/^[a-z0-9-]{1,80}$/.test(id) || ids.has(id)) invalido();
    if (!Number.isInteger(cantidad) || cantidad < 1 || cantidad > 99) invalido();
    ids.add(id);
    return { id, cantidad };
  }).sort((a, b) => a.id.localeCompare(b.id, "en"));

  const direccion = entrega === "delivery" ? texto(datos.direccion, 250) : null;
  const observaciones = texto(datos.observaciones, 500, false);
  return { intento, nombre, telefono, entrega, direccion, observaciones, productos, subtotalCentimos };
}

function hash(valor) {
  return createHash("sha256").update(valor).digest("hex");
}

async function guardarPedido(db, uid, datos, fechaServidor) {
  const { intento, productos, subtotalCentimos: cotizacion, ...contacto } = validarPedido(datos);
  const pedidoId = hash(JSON.stringify([uid, intento]));
  const huella = hash(JSON.stringify([contacto, productos, cotizacion]));
  const referencia = db.collection("pedidos").doc(pedidoId);

  return db.runTransaction(async (transaccion) => {
    const perfil = await transaccion.get(db.collection("usuarios").doc(uid));
    if (!["usuario", "admin"].includes(perfil.data()?.rol)) {
      throw new ErrorPedido("permission-denied", "Tu cuenta requiere un perfil válido.");
    }

    const previo = await transaccion.get(referencia);
    if (previo.exists) {
      const pedido = previo.data();
      if (pedido.huella !== huella) {
        throw new ErrorPedido("already-exists", "Ese intento ya está asociado a un pedido.");
      }
      return { pedidoId, totalCentimos: pedido.totalCentimos };
    }

    if (contacto.entrega === "delivery") {
      throw new ErrorPedido("failed-precondition", "El costo de delivery sigue pendiente.");
    }

    const referencias = productos.map(({ id }) => db.collection("platillos").doc(id));
    const fichas = await transaccion.getAll(...referencias);
    const lineas = productos.map(({ id, cantidad }, indice) => {
      const ficha = fichas[indice].data();
      const precio = ficha?.precioCentimos;
      if (ficha?.activo !== true || typeof ficha.nombre !== "string" ||
          !Number.isSafeInteger(precio) || precio <= 0 || precio > 1_000_000) {
        throw new ErrorPedido("failed-precondition", "Algún platillo ya no está disponible.");
      }
      return { id, nombre: ficha.nombre, cantidad, precioCentimos: precio };
    });
    const totalCentimos = lineas.reduce((total, linea) => total + linea.precioCentimos * linea.cantidad, 0);
    if (totalCentimos !== cotizacion) {
      throw new ErrorPedido("failed-precondition", "Los precios cambiaron. Actualiza el catálogo.");
    }

    transaccion.create(referencia, {
      id: pedidoId,
      uid,
      ...contacto,
      huella,
      lineas,
      subtotalCentimos: totalCentimos,
      totalCentimos,
      creadoEn: fechaServidor(),
      estado: "pendiente",
    });
    return { pedidoId, totalCentimos };
  });
}

module.exports = { ErrorPedido, guardarPedido };
