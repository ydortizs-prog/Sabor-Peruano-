const { test } = require("node:test");
const assert = require("node:assert/strict");
const { guardarPedido } = require("./pedidos");

function base() {
  const documentos = new Map([
    ["usuarios/cliente", { rol: "usuario" }],
    ["usuarios/otra", { rol: "usuario" }],
    ["platillos/aji-de-gallina", { nombre: "Ají", precioCentimos: 1800, activo: true }],
    ["platillos/lomo-saltado", { nombre: "Lomo", precioCentimos: 2200, activo: true }],
  ]);
  const snapshot = (ruta) => ({ exists: documentos.has(ruta), data: () => documentos.get(ruta) });
  const transaccion = {
    get: async (ruta) => snapshot(ruta),
    getAll: async (...rutas) => rutas.map(snapshot),
    create(ruta, valor) {
      assert.ok(!documentos.has(ruta));
      documentos.set(ruta, valor);
    },
  };
  return {
    documentos,
    db: {
      collection: (coleccion) => ({ doc: (id) => coleccion + "/" + id }),
      runTransaction: async (operacion) => operacion(transaccion),
    },
  };
}

const solicitud = {
  intento: "915f7dd8-6ac1-440a-aa42-53101e40fcde",
  nombre: "Cliente de prueba",
  telefono: "999111222",
  entrega: "recojo",
  subtotalCentimos: 5800,
  lineas: [{ id: "aji-de-gallina", cantidad: 2 }, { id: "lomo-saltado", cantidad: 1 }],
};

test("recalcula y guarda un único pedido por cuenta e intento", async () => {
  const { db, documentos } = base();
  const enviar = (uid, datos) => guardarPedido(db, uid, datos, () => "fecha-servidor");
  const primero = await enviar("cliente", { ...solicitud, uid: "otra", estado: "entregado" });
  const repetido = await enviar("cliente", { ...solicitud, lineas: [...solicitud.lineas].reverse() });
  assert.deepEqual(repetido, primero);
  assert.equal(primero.totalCentimos, 5800);
  assert.equal(documentos.get("pedidos/" + primero.pedidoId).uid, "cliente");
  assert.equal(documentos.get("pedidos/" + primero.pedidoId).estado, "pendiente");
  assert.equal(documentos.get("pedidos/" + primero.pedidoId).creadoEn, "fecha-servidor");
  assert.equal([...documentos.keys()].filter((ruta) => ruta.startsWith("pedidos/")).length, 1);
  await assert.rejects(enviar("cliente", { ...solicitud, telefono: "999111223" }),
    { codigo: "already-exists" });
  assert.notEqual((await enviar("otra", solicitud)).pedidoId, primero.pedidoId);
});

test("rechaza un subtotal distinto, delivery sin tarifa y cuentas sin perfil", async () => {
  const { db, documentos } = base();
  const enviar = (uid, datos) => guardarPedido(db, uid, datos, () => "fecha-servidor");
  await assert.rejects(enviar("cliente", { ...solicitud, subtotalCentimos: 1 }),
    { codigo: "failed-precondition" });
  await assert.rejects(enviar("cliente", { ...solicitud, entrega: "delivery", direccion: "Dirección de prueba" }),
    { codigo: "failed-precondition" });
  await assert.rejects(enviar("desconocido", solicitud), { codigo: "permission-denied" });
  assert.ok([...documentos.keys()].every((ruta) => !ruta.startsWith("pedidos/")));
});
