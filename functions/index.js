const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { ErrorPedido, guardarPedido } = require("./pedidos");

initializeApp();

exports.crearPedido = onCall({ region: "us-central1", maxInstances: 10 }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Inicia sesión para registrar el pedido.");
  }

  try {
    return await guardarPedido(
      getFirestore(), request.auth.uid, request.data, FieldValue.serverTimestamp
    );
  } catch (error) {
    if (error instanceof ErrorPedido) {
      throw new HttpsError(error.codigo, error.message);
    }
    throw new HttpsError("internal", "No se pudo guardar el pedido.");
  }
});
