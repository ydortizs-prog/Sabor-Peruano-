# Uso de IA en la fase 2

Sabor Peruano, rama `mejora-ia`. Las entradas registran las consultas de implementación y revisión que dieron lugar a cambios o verificaciones. Los prompts son extractos y se omiten datos personales.

## Consulta 1

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «cambiar el proyecto a la rama mejora con IA [...] no juntar los comits de main».
- **Qué respondió** Se separó el trabajo de la fase 2 en `mejora-ia`.
- **Qué se usó o cambió** Se mantuvo `main` como base y se dejó el borrador de H16 en su stash.
- **Qué se aprendió** `git log main..HEAD` muestra los commits propios de la rama.

## Consulta 2

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «ese Reintentar recuperar sesion es parte de olvidaste tu contraseña?».
- **Qué respondió** Se distinguió restaurar una sesión de enviar un correo de restablecimiento.
- **Qué se usó o cambió** Se añadió la acción de restablecimiento mediante Firebase Auth.
- **Qué se aprendió** Restaurar la sesión y cambiar las credenciales tienen estados y errores distintos.

## Consulta 3

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «asi mismo color y diseño el inicio sesion».
- **Qué respondió** Se adaptó la referencia a un login crema y rojo con campos e iconos.
- **Qué se usó o cambió** Se rediseñó `LoginScreen` y se mantuvo el flujo existente de autenticación.
- **Qué se aprendió** Compose permite cambiar la presentación sin mover la autenticación a la UI.

## Consulta 4

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «borra eso de contacte».
- **Qué respondió** Se retiró la leyenda que pedía contactar al administrador.
- **Qué se usó o cambió** Se mantuvo el acceso con cuentas existentes. El registro se añadió después, por solicitud expresa.
- **Qué se aprendió** La interfaz debe mostrar acciones que el usuario pueda completar.

## Consulta 5

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «arregla asi la parte de usuario [...] la pantalla».
- **Qué respondió** Se reorganizó el catálogo con tarjetas, filtros y barra inferior.
- **Qué se usó o cambió** Se reutilizaron las fotos, los precios y la navegación al detalle.
- **Qué se aprendió** Un cambio de diseño puede reutilizar los estados y flujos del catálogo.

## Consulta 6

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «el model se va quedar asi no se va separar?».
- **Qué respondió** Se explicó la vista compacta de paquetes en Android Studio.
- **Qué se usó o cambió** Los modelos siguen en `domain/model`.
- **Qué se aprendió** `domain.model` en el IDE representa paquetes anidados.

## Consulta 7

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «en icono de abajo de persona ahi debe aparecer informacion de mi correo electronico, cerrar sesion y cambiar de modo oscuro y claro [...] calificacion por estrellas».
- **Qué respondió** Se incorporaron el acceso al perfil, el tema, las valoraciones y una campana de actividad.
- **Qué se usó o cambió** Las valoraciones y la preferencia de tema se guardan localmente por cuenta.
- **Qué se aprendió** Identidad y preferencias se pueden guardar en fuentes distintas.

## Consulta 8

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «una pantalla completa [...] en vez de example aparesca el correo real».
- **Qué respondió** Se creó «Mi cuenta» con el correo de la sesión, el tema y el cierre de sesión.
- **Qué se usó o cambió** Se usó el correo autenticado. El perfil muestra actividad de pedidos de este dispositivo.
- **Qué se aprendió** El perfil puede reutilizar la sesión sin duplicar el correo.

## Consulta 9

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «como esta app va ser pedido como tipo de dilevery» y «ya tienes implementada los metodo de pago [...] y el washap?».
- **Qué respondió** Se revisó el flujo de carrito, recojo o delivery, resumen y coordinación por WhatsApp.
- **Qué se usó o cambió** Se precisó que el cliente confirma el envío del pedido en WhatsApp y que el costo del delivery se coordina por chat.
- **Qué se aprendió** Preparar un pedido y confirmar su envío o su pago son pasos distintos.

## Consulta 10

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «Coloca la imagen y el texto “SABOR PERUANO” dentro del mismo Box».
- **Qué respondió** Se propuso una bienvenida con una transición de cinco segundos y una entrada de dos segundos.
- **Qué se usó o cambió** Se mantuvo la restauración de la sesión durante el Welcome. La foto inicial fue sustituida en la siguiente revisión.
- **Qué se aprendió** La pantalla de arranque puede acompañar la restauración de sesión.

## Consulta 11

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «la moto no se dezplasa» y «que la moto corra».
- **Qué respondió** Se revisó la escala de animación del emulador y se separó la moto de la carretera.
- **Qué se usó o cambió** Se dibujó un mapa fijo y una moto vectorial con el nombre sobre su caja.
- **Qué se aprendió** Un objeto separado del fondo permite mostrar movimiento relativo.

## Consulta 12

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «aslo más profesional el welcome» y «más amigable mas atractivo».
- **Qué respondió** Se pulieron la marca, el mapa y la aparición del lema.
- **Qué se usó o cambió** Se añadió un saludo y un trazo que avanza junto a la moto.
- **Qué se aprendió** Reservar espacio para el texto evita saltos de posición durante la animación.

## Consulta 13

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «el boto + borra [...] ya hay una opcion de agregar carrito».
- **Qué respondió** Se quitó la acción rápida de compra de la tarjeta.
- **Qué se usó o cambió** La ficha mantiene el selector de cantidad y «Agregar al carrito».
- **Qué se aprendió** La tarjeta puede separar la navegación al detalle de las acciones rápidas.

## Consulta 14

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «le pongas un corazon para agregar a favoritos» y «al momento de agregar a favoritos tambien notifique».
- **Qué respondió** Se incorporó el corazón y se conectó el guardado con la campana.
- **Qué se usó o cambió** El corazón observa Room. Se emite un aviso al guardar el favorito.
- **Qué se aprendió** El estado visual debe seguir al estado confirmado por el repositorio.

## Consulta 15

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «mejores el panel de administrador [...] el icono de persona si o si debe estar como de cliente».
- **Qué respondió** Se rediseñó el panel con resumen, menú, pedidos y perfil.
- **Qué se usó o cambió** Se reutilizó «Mi cuenta» para ambos roles. El panel muestra métricas locales.
- **Qué se aprendió** Compartir el perfil evita duplicar el selector de tema y el cierre de sesión.

## Consulta 16

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «no veo que rediseñaste sigue igual el de administrador».
- **Qué respondió** Se encontró que el administrador seguía entrando primero al catálogo.
- **Qué se usó o cambió** En esta rama el admin abre el panel directamente y puede abrir el catálogo desde ahí.
- **Qué se aprendió** Las pantallas deben verificarse desde el flujo de entrada de cada rol.

## Consulta 17

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «esto no debe mostrar en panel de admin [...] campanita [...] CRUD» y «Demo local en este dispositivo».
- **Qué respondió** Se separó la gestión del flujo de compra y se añadió un CRUD local en Room.
- **Qué se usó o cambió** El admin puede crear, editar y retirar platos. La campana informa de operaciones del panel y el catálogo conserva las ediciones al sincronizar.
- **Qué se aprendió** Guardar el retiro local impide que la descarga vuelva a mostrar un plato retirado.

## Consulta 18

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «solo [...] cuando le doy en el lapiz».
- **Qué respondió** Se hizo explícita la acción de edición con un lápiz.
- **Qué se usó o cambió** Tocar el resto de la tarjeta deja cerrado el editor.
- **Qué se aprendió** Un botón específico ayuda a evitar aperturas por accidente.

## Consulta 19

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «implementa el metodo de pago yape».
- **Qué respondió** Se acordaron Yape manual por WhatsApp y efectivo al recibir o recoger.
- **Qué se usó o cambió** El resumen y el texto de WhatsApp incluyen el método elegido. La app no procesa el cobro.
- **Qué se aprendió** Una preferencia de pago no equivale a un pago confirmado.

## Consulta 20

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «implementa registrse nada» y «que cambio en firebase».
- **Qué respondió** Se incorporó un formulario de registro y una regla de creación de perfiles.
- **Qué se usó o cambió** Se crea el perfil propio con rol `usuario`. Los perfiles existentes se mantienen y el rol admin continúa en Firestore.
- **Qué se aprendió** El control de acceso se aplica en las reglas además de la interfaz.

## Consulta 21

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «que esta fallando con el emulador».
- **Qué respondió** Se distinguió Small_Phone de BlueStacks y del teléfono conectado por USB.
- **Qué se usó o cambió** Se recuperó Small_Phone y se verificó la instalación del APK de depuración.
- **Qué se aprendió** El serial de ADB permite identificar el destino de una instalación.

## Consulta 22

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «puse en modo claro y entre a los platos se vei en modo socuro».
- **Qué respondió** Se encontró que el detalle transparente dejaba ver el fondo del sistema.
- **Qué se usó o cambió** La navegación de la cuenta aplica un fondo del tema elegido.
- **Qué se aprendió** Cambiar el esquema de colores no pinta por sí solo el fondo de una pantalla transparente.

## Consulta 23

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «cuando cierro la app desaparece mi favorito y la notificaciones».
- **Qué respondió** Se comprobó que los avisos se guardaban en memoria y que los favoritos usaban Room.
- **Qué se usó o cambió** Se persistieron los avisos nuevos y su estado de lectura. Una prueba reabre la base de favoritos y vuelve a sincronizar.
- **Qué se aprendió** Hay que probar la reapertura además de comprobar el estado en pantalla.

## Consulta 24

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «revisa ahora que todo funcione segun el pdf [...] prepara promts [...] interfaz [...] un comit aparte».
- **Qué respondió** Se cotejaron la guía, la rama y las verificaciones disponibles.
- **Qué se usó o cambió** Se agregaron tres pruebas de ListaViewModel con repositorio falso y se separó el rediseño de los demás cambios en Git.
- **Qué se aprendió** Las pruebas de un ViewModel permiten verificar carga, caché y reintento sin depender de servicios externos.

## Consulta 25

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «esa logica de revisar pedido no la entiendo [...] mejora esa logica de acrrito».
- **Qué respondió** Se reorganizó el checkout en carrito, entrega y pago, y confirmación.
- **Qué se usó o cambió** Se reemplazó el cuadro de revisión por una pantalla completa. Se distinguió el precio por unidad, el subtotal y la tarifa pendiente de delivery.
- **Qué se aprendió** Indicar el paso y la acción siguiente ayuda a entender qué falta antes de enviar.

## Consulta 26

- **Fecha** 8 de noviembre de 2026.
- **Herramienta** Asistente de IA de OpenAI.
- **Prompt** «cuando se envie el mensaje a washap [...] se ponga guardado [...] en agregar plato en administrado pon la opcion de subir foto».
- **Qué respondió** Se añadió una confirmación manual al volver de WhatsApp y un selector de fotos para el menú local.
- **Qué se usó o cambió** «Ya envié el pedido» guarda su resumen en el dispositivo y vacía el carrito. El editor permite elegir una imagen de la galería y conservar su acceso al reabrir la app.
- **Qué se aprendió** Abrir un chat con texto preparado no permite detectar el envío. El selector de Android permite acceder solo a la foto elegida.

## Consulta 27

- **Fecha** 10 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «entres a mi android studio [...] y me digas si cumple estos requisitos» (tabla de pruebas PA-01 a PA-14).
- **Qué respondió** Revisó el código contra las 14 pruebas de aceptación. Encontró que el admin entraba directo al panel y no al catálogo (PA-02), y que Limpiar caché no borra los platos editados localmente (PA-11).
- **Qué se usó o cambió** Se cambió el destino inicial de `AppNav` a `DestinoLista` para ambos roles. Volver desde el panel Admin ahora usa `navigateUp()` en lugar de crear otra lista.
- **Qué se aprendió** Las pruebas de aceptación sirven como lista de verificación del código, no solo del emulador.

## Consulta 28

- **Fecha** 10 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «arreglar el interfaz y la lógica de notificaciones del cliente algo así [...] una ventana propia de notificación».
- **Qué respondió** Propuso una ventana propia con tarjetas por tipo de aviso, contador de no leídos y avisos nuevos resaltados.
- **Qué se usó o cambió** Se creó `PanelNotificaciones.kt`. `PreferenciasCliente` guarda la fecha de cada aviso y cuántos no se han leído, y agrupa avisos repetidos. La campana muestra el número de avisos sin leer.
- **Qué se aprendió** La lógica de los avisos queda en `PreferenciasCliente` y la pantalla solo dibuja la lista que recibe.

## Consulta 29

- **Fecha** 10 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «puedes colocar las horas de las notificaciones y fechas».
- **Qué respondió** Sugirió mostrar "Hoy", "Ayer" o la fecha, junto con la hora en formato de Perú.
- **Qué se usó o cambió** Cada tarjeta muestra, por ejemplo, "Hace 5 min · Hoy, 8:15 p. m.". Los avisos guardados antes del cambio no tienen hora.
- **Qué se aprendió** Guardar la fecha como número (milisegundos) permite mostrarla en cualquier formato después.

## Consulta 30

- **Fecha** 10 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «no puedo eliminar mensajes no?».
- **Qué respondió** Propuso borrar deslizando, con una papelera por tarjeta y un botón "Borrar todo" con confirmación.
- **Qué se usó o cambió** Borrar solo oculta el aviso de la campana (campo `oculto`), para no perder el historial de pedidos que muestra Mi cuenta.
- **Qué se aprendió** Antes de borrar datos hay que revisar qué otras pantallas los usan.

## Consulta 31

- **Fecha** 11 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «estos requerimientos en qué archivo están [...] cómo se conectó mi Firebase con el ViewModel».
- **Qué respondió** Ubicó cada requisito R01 a R13 en su archivo y línea, y explicó la cadena Firebase → `FirebaseAuthRepository` → `AuthRepository` → `AppContainer` → `LoginViewModel` → `LoginScreen`.
- **Qué se usó o cambió** No hubo cambios de código. Se usó como guía para la sustentación.
- **Qué se aprendió** El ViewModel no conoce Firebase: recibe la interface desde la Factory, y por eso se puede probar con un repositorio falso.

## Consulta 32

- **Fecha** 11 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «según el documento era obligatorio hacer con esas API [...] puedes implementar esa API o se rompe toda mi app?».
- **Qué respondió** Explicó que el tema Comidas usa TheMealDB y que el cambio se limita casi por completo a la capa `data/remote`.
- **Qué se usó o cambió** Se cambió la API a TheMealDB, categoría Seafood: lista con `filter.php?c=Seafood` y detalle con `lookup.php?i={id}`. Se agregó `RespuestaMeals` porque la respuesta viene dentro de `meals`, que puede ser null. Como la API no trae precios, cada plato recibe un precio fijo de S/ 15 a S/ 25. Los filtros pasaron a Pescados, Mariscos y Otros, las tarjetas cargan fotos remotas y al sincronizar se quitan los platos que ya no vienen en la API. Se agregó `MapeoTheMealDbTest`.
- **Qué se aprendió** Gracias a la separación en capas, Room, favoritos, roles y notificaciones siguieron funcionando sin cambios.

## Consulta 33

- **Fecha** 11 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «configura el tamaño de las imágenes y cámbialo a español, está en inglés».
- **Qué respondió** Propuso traducir en el celular con ML Kit (inglés → español) detrás de una interface `Traductor` en la capa `data`.
- **Qué se usó o cambió** Se agregó `TraductorMlKit`. Los nombres se guardan traducidos en Room y la preparación se traduce en el detalle; origen y categoría usan una tabla fija en español. Las fotos son más bajas en las tarjetas y en el detalle.
- **Qué se aprendió** El modelo de idioma se descarga una sola vez y luego traduce sin internet. La traducción automática puede fallar con nombres propios de platos ("Pad Thai" salió como "Almohadilla").

## Consulta 34

- **Fecha** 11 de noviembre de 2026.
- **Herramienta** Claude (Anthropic), en Cowork.
- **Prompt** «por qué aparece tu marca» y «ponlo en mejora con IA nomás y súbelo con un commit».
- **Qué respondió** Explicó que GitHub muestra como coautor la línea `Co-Authored-By` del mensaje del commit, y cómo juntar varios commits en uno.
- **Qué se usó o cambió** Se reescribieron los mensajes sin esa línea y los cambios de TheMealDB se unieron a `mejora-ia` en un solo commit con `git merge --squash`.
- **Qué se aprendió** Cambiar commits que ya están en GitHub obliga a hacer force push, y conviene hacerlo antes de que otros usen la rama.

## Reflexión final

- **Qué hizo bien la IA:** encontrar en el código las causas de fallas concretas, como el admin que entraba al panel (PA-02) o los avisos que no se guardaban, y proponer cambios que respetan MVVM: lógica en `data` y ViewModel, pantallas que solo observan.
- **Qué revisé yo:** que cada cambio compilara y funcionara en el emulador, que las pruebas unitarias siguieran pasando y que la API fuera la que pide la guía (TheMealDB para Comidas).
- **Qué no acepté tal cual:** la marca de coautor en los commits y los nombres mal traducidos; estos últimos se revisan a mano.
- **Qué aprendí:** separar la app en capas permitió cambiar la API completa tocando casi solo `data/remote`. Además, una interface (`ItemRepository`, `Traductor`, `Notificador`) permite probar el ViewModel sin internet ni Firebase.
