# Uso de IA en la fase 2

Sabor Peruano, rama `mejora-ia`. Las entradas registran las consultas de implementación y revisión que dieron lugar a cambios o verificaciones. Los prompts son extractos y se omiten datos personales.

## Consulta 1

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «cambiar el proyecto a la rama mejora con IA [...] no juntar los comits de main».
- **Qué respondió** Se separó el trabajo de la fase 2 en `mejora-ia`.
- **Qué se usó o cambió** Se mantuvo `main` como base y se dejó el borrador de H16 en su stash.
- **Qué se aprendió** `git log main..HEAD` muestra los commits propios de la rama.

## Consulta 2

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «ese Reintentar recuperar sesion es parte de olvidaste tu contraseña?».
- **Qué respondió** Se distinguió restaurar una sesión de enviar un correo de restablecimiento.
- **Qué se usó o cambió** Se añadió la acción de restablecimiento mediante Firebase Auth.
- **Qué se aprendió** Restaurar la sesión y cambiar las credenciales tienen estados y errores distintos.

## Consulta 3

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «asi mismo color y diseño el inicio sesion».
- **Qué respondió** Se adaptó la referencia a un login crema y rojo con campos e iconos.
- **Qué se usó o cambió** Se rediseñó `LoginScreen` y se mantuvo el flujo existente de autenticación.
- **Qué se aprendió** Compose permite cambiar la presentación sin mover la autenticación a la UI.

## Consulta 4

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «borra eso de contacte».
- **Qué respondió** Se retiró la leyenda que pedía contactar al administrador.
- **Qué se usó o cambió** Se mantuvo el acceso con cuentas existentes. El registro se añadió después, por solicitud expresa.
- **Qué se aprendió** La interfaz debe mostrar acciones que el usuario pueda completar.

## Consulta 5

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «arregla asi la parte de usuario [...] la pantalla».
- **Qué respondió** Se reorganizó el catálogo con tarjetas, filtros y barra inferior.
- **Qué se usó o cambió** Se reutilizaron las fotos, los precios y la navegación al detalle.
- **Qué se aprendió** Un cambio de diseño puede reutilizar los estados y flujos del catálogo.

## Consulta 6

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «el model se va quedar asi no se va separar?».
- **Qué respondió** Se explicó la vista compacta de paquetes en Android Studio.
- **Qué se usó o cambió** Los modelos siguen en `domain/model`.
- **Qué se aprendió** `domain.model` en el IDE representa paquetes anidados.

## Consulta 7

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «en icono de abajo de persona ahi debe aparecer informacion de mi correo electronico, cerrar sesion y cambiar de modo oscuro y claro [...] calificacion por estrellas».
- **Qué respondió** Se incorporaron el acceso al perfil, el tema, las valoraciones y una campana de actividad.
- **Qué se usó o cambió** Las valoraciones y la preferencia de tema se guardan localmente por cuenta.
- **Qué se aprendió** Identidad y preferencias se pueden guardar en fuentes distintas.

## Consulta 8

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «una pantalla completa [...] en vez de example aparesca el correo real».
- **Qué respondió** Se creó «Mi cuenta» con el correo de la sesión, el tema y el cierre de sesión.
- **Qué se usó o cambió** Se usó el correo autenticado. El perfil muestra actividad de pedidos de este dispositivo.
- **Qué se aprendió** El perfil puede reutilizar la sesión sin duplicar el correo.

## Consulta 9

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «como esta app va ser pedido como tipo de dilevery» y «ya tienes implementada los metodo de pago [...] y el washap?».
- **Qué respondió** Se revisó el flujo de carrito, recojo o delivery, resumen y coordinación por WhatsApp.
- **Qué se usó o cambió** Se precisó que el cliente confirma el envío del pedido en WhatsApp y que el costo del delivery se coordina por chat.
- **Qué se aprendió** Preparar un pedido y confirmar su envío o su pago son pasos distintos.

## Consulta 10

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «Coloca la imagen y el texto “SABOR PERUANO” dentro del mismo Box».
- **Qué respondió** Se propuso una bienvenida con una transición de cinco segundos y una entrada de dos segundos.
- **Qué se usó o cambió** Se mantuvo la restauración de la sesión durante el Welcome. La foto inicial fue sustituida en la siguiente revisión.
- **Qué se aprendió** La pantalla de arranque puede acompañar la restauración de sesión.

## Consulta 11

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «la moto no se dezplasa» y «que la moto corra».
- **Qué respondió** Se revisó la escala de animación del emulador y se separó la moto de la carretera.
- **Qué se usó o cambió** Se dibujó un mapa fijo y una moto vectorial con el nombre sobre su caja.
- **Qué se aprendió** Un objeto separado del fondo permite mostrar movimiento relativo.

## Consulta 12

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «aslo más profesional el welcome» y «más amigable mas atractivo».
- **Qué respondió** Se pulieron la marca, el mapa y la aparición del lema.
- **Qué se usó o cambió** Se añadió un saludo y un trazo que avanza junto a la moto.
- **Qué se aprendió** Reservar espacio para el texto evita saltos de posición durante la animación.

## Consulta 13

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «el boto + borra [...] ya hay una opcion de agregar carrito».
- **Qué respondió** Se quitó la acción rápida de compra de la tarjeta.
- **Qué se usó o cambió** La ficha mantiene el selector de cantidad y «Agregar al carrito».
- **Qué se aprendió** La tarjeta puede separar la navegación al detalle de las acciones rápidas.

## Consulta 14

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «le pongas un corazon para agregar a favoritos» y «al momento de agregar a favoritos tambien notifique».
- **Qué respondió** Se incorporó el corazón y se conectó el guardado con la campana.
- **Qué se usó o cambió** El corazón observa Room. Se emite un aviso al guardar el favorito.
- **Qué se aprendió** El estado visual debe seguir al estado confirmado por el repositorio.

## Consulta 15

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «mejores el panel de administrador [...] el icono de persona si o si debe estar como de cliente».
- **Qué respondió** Se rediseñó el panel con resumen, menú, pedidos y perfil.
- **Qué se usó o cambió** Se reutilizó «Mi cuenta» para ambos roles. El panel muestra métricas locales.
- **Qué se aprendió** Compartir el perfil evita duplicar el selector de tema y el cierre de sesión.

## Consulta 16

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «no veo que rediseñaste sigue igual el de administrador».
- **Qué respondió** Se encontró que el administrador seguía entrando primero al catálogo.
- **Qué se usó o cambió** En esta rama el admin abre el panel directamente y puede abrir el catálogo desde ahí.
- **Qué se aprendió** Las pantallas deben verificarse desde el flujo de entrada de cada rol.

## Consulta 17

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «esto no debe mostrar en panel de admin [...] campanita [...] CRUD» y «Demo local en este dispositivo».
- **Qué respondió** Se separó la gestión del flujo de compra y se añadió un CRUD local en Room.
- **Qué se usó o cambió** El admin puede crear, editar y retirar platos. La campana informa de operaciones del panel y el catálogo conserva las ediciones al sincronizar.
- **Qué se aprendió** Guardar el retiro local impide que la descarga vuelva a mostrar un plato retirado.

## Consulta 18

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «solo [...] cuando le doy en el lapiz».
- **Qué respondió** Se hizo explícita la acción de edición con un lápiz.
- **Qué se usó o cambió** Tocar el resto de la tarjeta deja cerrado el editor.
- **Qué se aprendió** Un botón específico ayuda a evitar aperturas por accidente.

## Consulta 19

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «implementa el metodo de pago yape».
- **Qué respondió** Se acordaron Yape manual por WhatsApp y efectivo al recibir o recoger.
- **Qué se usó o cambió** El resumen y el texto de WhatsApp incluyen el método elegido. La app no procesa el cobro.
- **Qué se aprendió** Una preferencia de pago no equivale a un pago confirmado.

## Consulta 20

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «implementa registrse nada» y «que cambio en firebase».
- **Qué respondió** Se incorporó un formulario de registro y una regla de creación de perfiles.
- **Qué se usó o cambió** Se crea el perfil propio con rol `usuario`. Los perfiles existentes se mantienen y el rol admin continúa en Firestore.
- **Qué se aprendió** El control de acceso se aplica en las reglas además de la interfaz.

## Consulta 21

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «que esta fallando con el emulador».
- **Qué respondió** Se distinguió Small_Phone de BlueStacks y del teléfono conectado por USB.
- **Qué se usó o cambió** Se recuperó Small_Phone y se verificó la instalación del APK de depuración.
- **Qué se aprendió** El serial de ADB permite identificar el destino de una instalación.

## Consulta 22

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «puse en modo claro y entre a los platos se vei en modo socuro».
- **Qué respondió** Se encontró que el detalle transparente dejaba ver el fondo del sistema.
- **Qué se usó o cambió** La navegación de la cuenta aplica un fondo del tema elegido.
- **Qué se aprendió** Cambiar el esquema de colores no pinta por sí solo el fondo de una pantalla transparente.

## Consulta 23

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «cuando cierro la app desaparece mi favorito y la notificaciones».
- **Qué respondió** Se comprobó que los avisos se guardaban en memoria y que los favoritos usaban Room.
- **Qué se usó o cambió** Se persistieron los avisos nuevos y su estado de lectura. Una prueba reabre la base de favoritos y vuelve a sincronizar.
- **Qué se aprendió** Hay que probar la reapertura además de comprobar el estado en pantalla.

## Consulta 24

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Codex.
- **Prompt** «revisa ahora que todo funcione segun el pdf [...] prepara promts [...] interfaz [...] un comit aparte».
- **Qué respondió** Se cotejaron la guía, la rama y las verificaciones disponibles.
- **Qué se usó o cambió** Se agregaron tres pruebas de ListaViewModel con repositorio falso y se separó el rediseño de los demás cambios en Git.
- **Qué se aprendió** Las pruebas de un ViewModel permiten verificar carga, caché y reintento sin depender de servicios externos.
