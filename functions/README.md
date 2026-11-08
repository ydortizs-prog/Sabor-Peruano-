# Registro de pedidos

`crearPedido` es una función invocable en `us-central1`. Usa el UID de Firebase Auth y valida el perfil en `usuarios/{uid}`. Android transmite identificadores, cantidades y el subtotal que el cliente revisó. El servidor lee los precios vigentes en Firestore, calcula el total y rechaza diferencias antes de registrar el pedido.

Para preparar un entorno de prueba:

1. Instala las dependencias con `npm ci` en esta carpeta y ejecuta `npm test`.
2. Usa Firebase Emulator Suite con un proyecto `demo-...` y crea perfiles `usuario` y `admin` en Auth y `usuarios/{uid}`.
3. Carga `platillos/{id}` con los mismos IDs del catálogo. Cada documento requiere `nombre`, `precioCentimos` entero positivo y `activo` booleano. Solo un operador con acceso administrativo debe gestionar estos precios.
4. Comprueba que el propietario y Admin puedan leer el pedido, que otro cliente reciba un rechazo y que ningún cliente pueda escribir directamente en `pedidos` ni cambiar su rol.

`firestore.rules` contiene las reglas propuestas. Antes de publicar, compáralas con las reglas activas y conserva los accesos de las demás colecciones que use el proyecto. El envío desde Android se activa con `pedido_backend_habilitado` cuando el entorno esté preparado. La compilación actual usa la URL del proyecto Firebase configurado en Android y deja el envío desactivado.

El servidor acepta pedidos de recojo. Delivery requiere acordar una tarifa y mostrarla para aprobación antes de aceptar el pedido. Un intento repetido conserva el mismo ID durante la sesión del carrito. Reiniciar la app descarta el borrador y su clave de intento.

Con el registro de Firebase desactivado, Android puede abrir el resumen en WhatsApp
si tiene un número configurado. El número de prueba se define solo en
app/src/debug/res/values/whatsapp.xml, que se excluye de Git. El cliente revisa y
envía el mensaje desde WhatsApp. Este flujo no registra documentos en Firestore.
