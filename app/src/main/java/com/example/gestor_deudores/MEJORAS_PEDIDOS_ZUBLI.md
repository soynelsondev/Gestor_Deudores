# Mejoras de Pedidos en Zubli

Documento para tu agente en Android Studio. Describe cómo mejorar la pantalla **Nuevo Pedido** y el seguimiento de pedidos para negocios de sublimación y personalización: detalles por producto, fotos de los diseños, pago, entrega y resumen para el cliente.

> **Reglas para el agente**
> 1. Antes de editar, lee las entidades, el DAO, el ViewModel y las pantallas de pedidos, y adapta los nombres de este documento a los reales.
> 2. Trabaja por fases (sección 13). Compila y prueba al terminar cada una, y haz commit.
> 3. **Prohibido** borrar datos al migrar: hay pedidos reales guardados. Toda tabla o columna nueva lleva su `Migration` y su valor por defecto declarado igual en la entidad de Room.
> 4. No cambies el cálculo de saldos ni la lógica de abonos ya existente (el saldo sigue siendo la suma de cargos y abonos del cliente).
> 5. Si algo no encaja con el código real, detente y explícalo antes de improvisar.

---

## 1. Resumen

La pantalla actual tiene una buena base: cliente con "+ Nuevo", varios artículos, abono opcional, forma de pago del saldo y fecha de entrega. Pero en este negocio **cada pieza lleva su propio diseño y sus propios detalles**, y hoy solo existe una nota general.

**Lo más importante a agregar:**
1. Detalles propios por artículo (detalle, texto a estampar y nota, siempre opcionales).
2. **Campos configurables por plantilla** (variantes como talla, color, modelo o tamaño; ubicación del estampado u otros), sin listas fijas: cada taller define los suyos o ninguno.
3. **Fotos de los diseños** por artículo (galería, cámara y, después, compartir desde WhatsApp), con aprobación del diseño.
4. Precio automático según la plantilla y la cantidad (escalas del cotizador).
5. Abono con método, moneda y comprobante, y abono sugerido.
6. Mejor manejo de la fecha de entrega y del tipo de entrega.
7. Total fijo abajo, borrador automático y resumen para el cliente por WhatsApp.

---

## 2. Qué está bien (conservar)

| Lo que tienes | Por qué sirve |
|---|---|
| Selector de cliente con "+ Nuevo" | Permite crear al cliente sin salir del pedido |
| Varios artículos por pedido | Un cliente suele pedir distintos productos |
| Plantilla del cotizador por artículo | Trae costos y precio sin escribirlos |
| Abono inicial opcional | Es la forma habitual de apartar un pedido |
| Forma de pago del saldo | Conecta el pedido con cuotas y cobros |
| Fecha de entrega y notas de producción | Base del seguimiento |
| Snapshot de costos al guardar | Los pedidos viejos no cambian cuando suben los insumos |

---

## 3. Problemas detectados en la pantalla actual

| Problema | Por qué importa | Arreglo |
|---|---|---|
| Una sola nota para todo el pedido | Cada artículo puede tener detalles distintos (variantes, texto, diseño, según el producto) | Detalles por artículo (4.2) |
| No hay fotos | El diseño llega por WhatsApp y se pierde entre mensajes | Fotos por artículo (sección 6) |
| Cantidad como número simple | Algunos productos se reparten por talla, color, modelo, tamaño o nombre; otros no | Variantes configurables y opcionales (4.2.1) |
| "Precio Un. ($)" vacío | El usuario debe adivinar el precio | Autocompletar desde la plantilla y la cantidad (sección 5) |
| "Buscar Plantilla Cotizador" | Texto confuso para un usuario nuevo | "Producto", con plantillas recientes como atajo |
| Fecha de entrega con la fecha de hoy por defecto | Muchos pedidos quedan con fecha equivocada | Vacía y obligatoria, o sugerida según días de producción |
| Chips de forma de pago cortados ("M…") | Se ve rota la opción Mensual | Que los chips pasen a otra línea |
| Cuotas sin detalle | No dice cuántas cuotas ni cuándo empiezan | Pedir número de cuotas y primera fecha (4.4) |
| Abono sin método ni moneda | Con doble moneda hay que registrar cómo se pagó | Método, moneda, tasa y referencia (4.4) |
| Total y botón Guardar al final del scroll | Se pierde de vista en pedidos largos | Barra inferior fija (4.7) |
| No muestra la deuda previa del cliente | Se da crédito sin saber si ya debe | Saldo del cliente al elegirlo (4.1) |
| Sin equivalente en Bs ni ganancia | Falta información para decidir | Barra inferior con total en USD y Bs (4.7) |

---

## 4. Diseño de la pantalla nueva

La pantalla se organiza en secciones que se pueden plegar, con un resumen de una línea en cada una cuando están cerradas (por ejemplo, "Productos: 2 artículos, 12 piezas, $114.67").

### 4.1 Cliente
- Selector con búsqueda y botón **"+ Nuevo"** (se conserva).
- Al elegir un cliente, mostrar debajo una línea: **saldo pendiente** y si tiene cuotas vencidas. Ejemplo: *"Este cliente debe $24.00 · 1 cuota vencida"*. En rojo si hay vencidas.
- Cliente obligatorio para guardar.

### 4.2 Artículo (se repite por cada producto)

| Campo | Tipo | Notas |
|---|---|---|
| Producto | Búsqueda de plantilla + opción **"Producto libre"** | Etiqueta: "Producto". Muestra las plantillas recientes como atajos |
| Cantidad total | Número | Si el producto usa variantes, se calcula sola; si no, se escribe directamente |
| **Campos configurables** | Según la plantilla | Variantes (talla, color, modelo, tamaño de taza u otra), ubicación del estampado, tamaño del diseño y otros. Cada taller activa solo los que usa (ver 4.2.1). Si la plantilla no define ninguno, no aparecen |
| Precio unitario | Moneda | Se autocompleta (sección 5); editable |
| Subtotal | Calculado | cantidad × precio unitario |
| Detalle del producto | Texto | Color, modelo, observaciones |
| **Texto o nombre a estampar** | Texto libre, opcional | Para nombres, frases, fechas |
| **Fotos del diseño** | Imágenes (hasta 4) | Ver sección 6 |
| **Estado del diseño** | Selección | Pendiente · Enviado al cliente · Aprobado (con fecha) |
| Nota del artículo | Texto | Detalles propios de esa pieza |
| Acciones | Duplicar · Eliminar | Con confirmación al eliminar |

**Producto libre:** permite agregar un artículo sin plantilla, con nombre, cantidad y precio manual, y un costo opcional por pieza. Si no hay costo, la ganancia estimada se muestra como "sin datos" en lugar de inventar un número.

**Regla de cantidad:** si el artículo usa variantes, el total de piezas es la suma de las cantidades de cada variante. Si no las usa, se respeta la cantidad escrita a mano. La distribución de variantes se guarda siempre con el artículo.

#### 4.2.1 Campos configurables por plantilla (nada es fijo)

**Principio:** la app **no impone** tallas, ubicaciones ni tamaños. No todos los talleres trabajan con ropa: puede haber tazas, llaveros, cuadros, almohadas, placas u otros productos que se reparten por color, modelo, forma, tamaño, nombre o por nada. Cada plantilla define qué datos se piden al tomar un pedido. Si la plantilla no define ninguno, el artículo solo pide producto, cantidad, precio, detalle, texto a estampar, fotos y nota.

En el editor de la plantilla, una sección **"¿Qué datos pides en el pedido?"** donde el usuario crea sus propios campos:

| Parte del campo | Descripción |
|---|---|
| Nombre | Libre: Talla, Color, Modelo, Tamaño de la taza, Ubicación del estampado, Material, etc. |
| Tipo | **Reparte la cantidad** (contadores − 0 +) · **Elegir una opción** (una sola) · **Texto libre** |
| Opciones | Solo para los dos primeros tipos. Se pueden agregar, quitar y reordenar |
| Ajuste de precio por opción | Opcional (por ejemplo, +$1 en una talla grande o en un tamaño mayor). Se suma al precio unitario de esa variante |
| Obligatorio | Sí o no |

**Reglas:**
- Solo **un** campo de tipo "Reparte la cantidad" por plantilla, para evitar combinaciones complejas. Quien necesite combinar (talla y color) puede usar variantes libres con etiquetas como "M · roja".
- **Puntos de partida opcionales** al crear la plantilla ("Empezar desde un ejemplo"): ropa (Talla y Ubicación), tazas (Tamaño y Color), llaveros o placas (Modelo o forma), o **"Sin campos"**. Son solo sugerencias: el usuario las edita, las cambia o las ignora, y no aparecen si no las elige.
- **Variantes libres dentro del artículo:** aunque la plantilla no tenga ningún campo, el artículo permite **"Dividir por variantes"**: agrega líneas con una etiqueta y una cantidad ("Para Ana ×1", "Roja ×3", "Modelo 2 ×4"). Cada línea puede tener su propio **texto a estampar**. Sirve, por ejemplo, para 12 tazas con nombres distintos.
- **Tamaño del diseño:** si el taller crea un campo de tamaño, cada opción puede vincularse de forma opcional con el consumo de DTF, papel y tinta del cotizador. Si no se vincula, solo es información para producción.
- **Se guarda el texto, no solo el identificador:** al guardar el pedido se copian el nombre del campo y la opción elegida. Así, si el taller edita o borra opciones después, los pedidos ya guardados no cambian.

### 4.3 Líneas extra del pedido (opcionales)
- **Descuento** (en dólares o en porcentaje).
- **Diseño personalizado** (cargo por pedido, sugerido desde la plantilla si existe).
- **Envío o delivery** (monto).
- Todas se suman al total y se muestran por separado en el resumen para el cliente.

### 4.4 Pago
- **Abono inicial (opcional):**
  - Monto y **moneda** (USD o Bs). Si es Bs, convertir con la tasa del día y guardar `montoOriginal`, `moneda` y `tasa` en el movimiento (igual que el módulo de doble moneda).
  - **Método:** efectivo, transferencia, pago móvil, Zinli, Binance, otro. Lista editable en Ajustes.
  - **Referencia** (opcional) y **comprobante** (foto, opcional).
  - Validación: el abono no puede ser mayor que el total.
- **Abono sugerido:**
  - Botón "Pedir 50 %" (el porcentaje es configurable en Ajustes; por defecto el taller decide).
  - Línea informativa: *"Con $4.20 de abono cubres el material de este pedido"*, calculada con los costos del snapshot (materiales, insumos y servicios; sin mano de obra ni ganancia).
- **Forma de pago del saldo:** Al retirar / entregar · Semanal · Quincenal · Mensual. Los chips deben acomodarse en varias líneas.
  - Si se elige semanal, quincenal o mensual: pedir **número de cuotas** y **fecha de la primera cuota**. Calcular cada cuota y mostrarlas antes de guardar.
  - Si se elige "Al retirar", no hay cuotas: el saldo vence en la fecha de entrega.
- Mostrar siempre: **Total · Abono · Saldo.**

### 4.5 Entrega
- **Fecha de entrega:** campo vacío y obligatorio, o sugerida según los **días de producción** que el usuario configura en Ajustes. No debe quedar la fecha de hoy por defecto.
- **Tipo de entrega:** retiro en taller o delivery. Si es delivery: dirección o referencia y costo (que alimenta la línea de envío de 4.3).
- **Urgente:** marca que destaca el pedido en la lista.
- **Aviso de carga:** *"Ese día ya tienes N pedidos"* (opcional; la capacidad diaria se configura en Ajustes).
- Validación: la fecha no puede ser anterior a hoy.

### 4.6 Notas generales
Una nota para todo el pedido (condiciones, acuerdos, forma de entrega). Las notas por artículo quedan dentro de cada artículo.

### 4.7 Barra inferior fija
Siempre visible mientras se llena el pedido:
- **Total en USD** y su equivalente en **Bs** (con la tasa del día).
- **Abono** y **Saldo**.
- **Ganancia estimada** del pedido, visible solo para el dueño, con un icono de ojo para ocultarla.
- Botones **Cancelar** y **Guardar pedido**.
- Si el precio de algún artículo queda por debajo del mínimo configurado o del costo, aviso en rojo: *"Con este precio pierdes dinero"*.

### 4.8 Borrador automático
- Mientras se llena el pedido, guardar un borrador (por ejemplo, en una tabla `PedidoBorrador` con los datos serializados, o en el estado persistente del ViewModel) para no perder lo escrito si entra una llamada o se cierra la app.
- Al salir con datos sin guardar: *"¿Guardar como borrador?"* con las opciones Guardar borrador · Descartar · Seguir editando.
- En la lista de pedidos, mostrar los borradores con una etiqueta.

### 4.9 Resumen para el cliente por WhatsApp
Al guardar, ofrecer **"Enviar resumen al cliente"**: arma el texto y abre WhatsApp (sin API, igual que los recordatorios de cobro). Sin mostrar costos ni ganancia. Ejemplo:

```
Hola {nombre}, tu pedido quedó registrado:

• 12 franelas (azules ×5, rojas ×7) — $9.94 c/u
• 2 tazas — $5.00 c/u

Total: $129.28 (Bs {total_bs})
Abono recibido: $50.00
Saldo: $79.28 — se paga al retirar
Fecha de entrega: 15/10/2026

Si necesitas cambiar algo, escríbeme antes de {fecha_limite}. ¡Gracias!
```

Las variantes que aparecen en el mensaje salen de lo que configure cada taller; si un producto no usa variantes, solo aparece la cantidad.

Opcional: un botón **"Compartir como imagen"** con el mismo contenido en formato tarjeta.

---

## 5. Precio automático

- Al elegir la plantilla y la cantidad, rellenar el **precio unitario** con el de la **escala por cantidad** que corresponde (detal, mayor, etc.), según el documento de mejoras del cotizador.
- Mostrar bajo el campo: *"Precio sugerido $9.56 · Tarifa al mayor (≥ 6 piezas)"*.
- Al cambiar la cantidad, recalcular el precio sugerido. Si el usuario ya lo editó a mano, **no pisarlo** y mostrar un botón "Usar precio sugerido".
- Si el usuario cambia el precio a mano, mostrar al instante **cuánto gana** por pieza y avisar si queda por debajo de su mínimo o del costo.
- Si el cotizador todavía no tiene escalas (Fase 2 del cotizador), usar el precio sugerido de la plantilla y dejar el campo editable.
- **Snapshot por artículo al guardar:** cantidad, variantes, escala aplicada, costo por pieza, costo por pedido repartido, mano de obra (si existe), precio, ganancia calculada y tasa de cambio del momento. Los pedidos antiguos no cambian si luego se edita la plantilla.

---

## 6. Fotos de los diseños (especificación técnica)

### 6.1 Cómo se agregan
1. **Galería:** selector de fotos del sistema (`ActivityResultContracts.PickMultipleVisualMedia`). No requiere permisos amplios de almacenamiento.
2. **Cámara:** `ActivityResultContracts.TakePicture` con un `FileProvider`. Solo se usa el permiso de cámara si hace falta.
3. **Compartir desde WhatsApp (Fase 4):** Zubli aparece en el menú "Compartir" de Android para imágenes. Al recibir una imagen, muestra una pantalla "¿A qué pedido la adjuntas?" con los pedidos recientes y la opción de elegir artículo.
   ```xml
   <!-- AndroidManifest.xml, en la actividad que recibe imágenes -->
   <intent-filter>
       <action android:name="android.intent.action.SEND" />
       <action android:name="android.intent.action.SEND_MULTIPLE" />
       <category android:name="android.intent.category.DEFAULT" />
       <data android:mimeType="image/*" />
   </intent-filter>
   ```

### 6.2 Procesamiento y almacenamiento
- **Comprimir al guardar:** lado mayor de 1280 px y JPEG con calidad cercana a 80 %. Corregir la orientación EXIF antes de guardar.
- Guardar el archivo en el almacenamiento **interno** de la app, por ejemplo `filesDir/pedidos/{pedidoId}/{uuid}.jpg`. Mantener también una miniatura pequeña (por ejemplo, 256 px) para las listas.
- **En la base de datos solo va la ruta**, nunca la imagen.
- Límites: máximo 4 fotos por artículo (configurable) y mostrar un mensaje claro si se supera.
- Cargar las miniaturas de forma asíncrona (por ejemplo con Coil) y no en el hilo principal.

### 6.3 Tipos de imagen
`DISENO` (enviado por el cliente), `COMPROBANTE` (de un pago) y `ENTREGA` (foto del producto terminado). La foto de entrega puede enviarse al cliente por WhatsApp como confirmación.

### 6.4 Visor
- Miniaturas en el artículo, en la tarjeta del pedido y en la pantalla de detalle.
- Al tocar una miniatura: pantalla completa con zoom, y botones **Compartir** (por WhatsApp), **Cambiar tipo** y **Eliminar** (con confirmación).

### 6.5 Respaldo, restauración y limpieza (obligatorio)
- **El respaldo debe incluir las fotos.** Si solo se respalda la base de datos, al restaurar los pedidos quedan sin imágenes.
- Formato sugerido: un archivo comprimido (`.zip`) con la base de datos y la carpeta de imágenes. Ofrecer la opción **"Incluir fotos"**, porque el archivo pesa mucho más.
- Importar: validar el contenido antes de reemplazar, hacer una copia automática de lo actual y restaurar base e imágenes juntas.
- Si se elimina o archiva un pedido, borrar sus archivos de imagen. Agregar una limpieza de archivos huérfanos (imágenes sin registro en la base de datos).
- Mostrar en Ajustes el espacio que ocupan las fotos y la opción de liberar espacio (por ejemplo, borrar las fotos de pedidos entregados hace más de X meses, con confirmación).

### 6.6 Privacidad
Las fotos de los clientes y los comprobantes de pago son datos personales. Se guardan solo en el teléfono, no se suben a ningún servicio sin permiso expreso y no se incluyen en capturas o exportaciones sin avisar.

---

## 7. Modelo de datos sugerido (adaptar a los nombres reales)

Todas las tablas y columnas nuevas se agregan con una `Migration` que **no borra nada** y con valores por defecto.

| Tabla | Campos principales |
|---|---|
| `Pedido` (existente) | Agregar: `tipoEntrega` (RETIRO / DELIVERY, default RETIRO), `direccionEntrega` (nullable), `costoEnvio` (default 0), `descuento` (default 0), `urgente` (default 0), `notaGeneral` (default ''), `estadoDiseno` (default 'PENDIENTE'), `esBorrador` (default 0) |
| `PedidoItem` | id, pedidoId, plantillaId (nullable), nombreProducto, cantidad, precioUnitario, detalle, textoEstampar, ubicacion, tamanoDiseno, estadoDiseno, fechaAprobacionDiseno (nullable), nota, **snapshot de costos** |
| `PlantillaAtributo` | id, plantillaId, nombre, tipo (REPARTE_CANTIDAD / OPCION / TEXTO), obligatorio, orden |
| `PlantillaAtributoOpcion` | id, atributoId, etiqueta, ajustePrecio (default 0), orden |
| `PedidoVariante` | id, itemId, etiqueta (texto libre, por ejemplo "M", "Roja" o "Para Ana"), cantidad, textoEstampar (nullable) |
| `PedidoItemAtributo` | id, itemId, nombreCampo, valor (texto copiado al guardar, sin depender de que la opción siga existiendo) |
| `PedidoImagen` | id, pedidoId, itemId (nullable), ruta, rutaMiniatura, tipo (DISENO / COMPROBANTE / ENTREGA), fecha |
| `MetodoPago` | id, nombre, activo (lista editable) |
| `PedidoBorrador` (opcional) | id, datosSerializados, fecha |

**Notas:**
- Revisar cómo guarda hoy los artículos del pedido (probablemente texto o tabla propia). Si ya existe una tabla de artículos, **extenderla** en vez de crear otra, y migrar los pedidos existentes a `PedidoItem` conservando cantidad, precio y snapshot.
- Mantener la relación con `Deuda` (`pedidoId`): al guardar el pedido se crea el cargo con el total y, si hay abono inicial, el movimiento de abono con su método, moneda y tasa.
- Claves foráneas con `ON DELETE CASCADE` hacia `Pedido`, e índices en las columnas de relación (Room los exige y los valida).
- **Dinero:** si los montos están en `Double`, redondear a 2 decimales al sumar y al mostrar.

---

## 8. Estados y seguimiento del pedido

### 8.1 Estados
`RECIBIDO → EN_PRODUCCION → LISTO → ENTREGADO`, además de `CANCELADO`. Un pedido nuevo nace en `RECIBIDO`. El estado del diseño (pendiente, enviado, aprobado) es independiente del estado del pedido.

**Recomendación:** no permitir pasar a `EN_PRODUCCION` si el diseño no está aprobado, salvo que el usuario lo confirme ("Producir sin diseño aprobado").

### 8.2 Lista de pedidos
- Tarjeta con: cliente, miniatura del primer diseño, resumen ("12 franelas + 2 tazas"), fecha de entrega, estado (chip de color), saldo pendiente y marca de urgente.
- Filtros: Todos · Hoy · Esta semana · Por estado · Con saldo pendiente.
- Orden por fecha de entrega, con los vencidos primero.
- Contador en el inicio: **"Por entregar hoy (N)"**.

### 8.3 Acciones por estado (WhatsApp, sin API)
- `RECIBIDO`: enviar resumen del pedido y, si aplica, el diseño para aprobación.
- `LISTO`: *"Hola {nombre}, tu pedido está listo. Saldo: ${saldo}."* con la foto de entrega adjunta como opción.
- `ENTREGADO`: ofrecer registrar el cobro del saldo.

### 8.4 Cancelación
No borrar el cargo. Crear un movimiento de ajuste y preguntar qué hacer con el abono recibido: **devolverlo** (registrar la devolución) o **conservarlo** (como compensación, con nota). Dejar todo en el historial.

---

## 9. Conexión con Cobros y Resumen

- Al guardar: se crea el **cargo** por el total del pedido (con `pedidoId`) y el **abono inicial** si lo hay. El saldo del cliente se sigue calculando como suma de movimientos.
- Si hay cuotas, calcular los vencimientos según la regla del módulo de cuotas (fecha de la primera cuota y frecuencia).
- El resumen financiero debe separar el abono en los "sobres" (material, logística, ganancia y, si existe, "Mi pago"), usando el snapshot del pedido.
- Pedidos cancelados y devoluciones se reflejan en los totales sin borrar historial.
- Con varios pedidos por cliente, los abonos se aplican **al saldo total del cliente**; para vencimientos por pedido, aplicar del más antiguo al más nuevo.

---

## 10. Validaciones y casos borde

- Cliente obligatorio. Al menos un artículo con cantidad ≥ 1.
- Cantidad entera y positiva. Con variantes, el total debe coincidir con la suma.
- Precio unitario ≥ 0; aviso si es menor que el costo o que el mínimo.
- Abono ≤ total; si se paga en Bs, tasa vigente obligatoria.
- Fecha de entrega obligatoria y no anterior a hoy.
- Con cuotas: número ≥ 1 y fecha de la primera cuota válida.
- Aceptar coma y punto en los decimales (`23,4` y `23.4`).
- Sin cámara o sin permiso: mensaje claro, sin cierre de la app.
- Foto corrupta o muy grande: rechazar con mensaje.
- Sin espacio de almacenamiento: avisar antes de guardar.
- Rotar la pantalla o pasar a segundo plano no debe borrar lo escrito (borrador).
- Eliminar un artículo con fotos: confirmar y borrar los archivos asociados.
- Producto libre sin costo: no mostrar ganancia inventada.

---

## 11. Textos de la interfaz

| Elemento | Texto |
|---|---|
| Buscador de plantilla | Producto |
| Campo de cantidad con variantes | Piezas (se calcula con las variantes) |
| Botón de variantes libres | Dividir por variantes |
| Editor de plantilla | ¿Qué datos pides en el pedido? |
| Precio | Precio por pieza ($) |
| Texto a estampar | Nombre o texto que lleva |
| Fotos | Agregar diseño del cliente |
| Estado del diseño | Diseño: pendiente / enviado / aprobado |
| Abono | Abono inicial |
| Abono sugerido | Pedir 50 % de abono |
| Ayuda del abono | Con $X cubres el material de este pedido |
| Saldo | Saldo pendiente |
| Entrega | Fecha de entrega (obligatoria) |
| Aviso de pérdida | Con este precio pierdes dinero |
| Aviso de cliente con deuda | Este cliente debe $X |
| Borrador | ¿Guardar como borrador? |

---

## 12. Pruebas

**Pantalla y cálculo:**
- Pedido con 1 artículo: total = cantidad × precio.
- Plantilla sin campos configurados: el artículo solo pide cantidad, precio, detalle, texto y fotos, y guarda sin errores.
- Plantilla con un campo que reparte la cantidad (con cualquier nombre: talla, color, modelo): el total es la suma y el subtotal es correcto.
- Editar o borrar una opción de la plantilla no cambia los pedidos ya guardados.
- Variantes libres en un artículo sin configuración: 3 líneas (etiqueta y cantidad) suman bien, y cada una puede llevar su texto a estampar.
- Opción con ajuste de precio: se refleja en el subtotal de esa variante.
- Cambiar la cantidad cambia el precio sugerido según la escala; un precio editado a mano no se pisa.
- Descuento, envío y diseño extra se suman correctamente al total.
- Abono mayor al total → bloqueado con mensaje.
- Abono en Bs → se guarda en USD con la tasa usada y los datos originales.
- Con cuotas semanales, las fechas y montos de las cuotas son correctos (fin de mes en mensuales).

**Datos y migración:**
- Instalar la versión actual con pedidos reales de prueba, actualizar **encima** y verificar que los pedidos antiguos, sus artículos, totales y abonos **no cambian**.
- Al guardar un pedido nuevo se crea el cargo y el abono y el saldo del cliente es correcto.
- Cancelar un pedido deja historial y totales coherentes.

**Fotos:**
- Agregar desde galería y desde cámara; verificar compresión y orientación correcta.
- Eliminar una foto y un pedido borra los archivos.
- Respaldo con fotos → restaurar en otro teléfono: pedidos e imágenes completos.
- Respaldo sin fotos → los pedidos se restauran sin imágenes y sin errores.
- Sin permiso de cámara o con foto corrupta: mensaje claro, sin cierre.

**Borrador:**
- Salir con datos escritos → ofrece guardar; recuperar el borrador completo al volver.

---

## 13. Orden de implementación

| Fase | Qué | Resultado |
|---|---|---|
| **1** | Detalles por artículo (detalle, texto a estampar y nota, siempre opcionales) · fecha de entrega vacía y obligatoria · precio automático desde la plantilla y la cantidad · barra inferior fija con total, abono, saldo y Bs · saldo del cliente al elegirlo · producto libre · chips acomodados · validaciones | Pedido claro y menos errores |
| **2** | Fotos por artículo (galería y cámara) · miniaturas y visor · respaldo y restauración con fotos · método de pago, moneda y referencia del abono | Los diseños quedan guardados con el pedido |
| **3** | Campos configurables por plantilla (variantes, ubicación, tamaño u otros) y variantes libres · estado del diseño y aprobación · tipo de entrega · líneas extra (descuento, diseño, envío) · abono sugerido · cuotas con fecha de inicio · resumen del pedido por WhatsApp | Pedido completo para ropa, tazas y más |
| **4** | Compartir desde WhatsApp hacia Zubli · borrador automático · aviso de carga por día · ganancia estimada del pedido · filtros y "Por entregar hoy" · flujo de cancelación | Comodidad y control diario |

Cada fase que agregue tablas necesita su migración y la prueba de instalar encima de la versión anterior con datos de prueba.

---

## 14. Prompts para tu agente

**Fase 1:**
> Lee la pantalla de Nuevo Pedido (vista, ViewModel, entidades y DAO) y este documento. Implementa solo la Fase 1: agrega por artículo los campos de texto opcionales detalle, texto a estampar y nota (sin listas fijas de tallas, ubicaciones ni tamaños); deja la fecha de entrega vacía y obligatoria; autocompleta el precio unitario desde la plantilla y la cantidad sin pisar un precio editado a mano; agrega la barra inferior fija con total en USD y Bs, abono, saldo y botón Guardar; muestra el saldo pendiente del cliente al elegirlo; permite productos libres; haz que los chips de forma de pago se acomoden en varias líneas; y agrega las validaciones de la sección 10. Si hay columnas nuevas, escribe la Migration sin borrar datos con valores por defecto iguales en la entidad, y no cambies el cálculo de saldos.

**Fase 2:**
> Implementa la Fase 2 según las secciones 6 y 7: fotos por artículo con selector de fotos del sistema y cámara con FileProvider, compresión (lado mayor 1280 px, JPEG ~80 %), miniaturas, visor a pantalla completa, tabla `PedidoImagen` con su migración, borrado de archivos al eliminar, y respaldo y restauración que incluyan las imágenes en un archivo comprimido con opción "Incluir fotos". Agrega método de pago, moneda y referencia al abono inicial. No guardes imágenes en la base de datos.

**Fase 3:**
> Implementa la Fase 3: campos configurables por plantilla según la sección 4.2.1 (variantes, ubicación, tamaño u otros, todos opcionales y editables por el usuario, sin listas fijas) y variantes libres dentro del artículo, con las tablas de la sección 7, estado del diseño con fecha de aprobación, tipo de entrega, líneas de descuento, diseño y envío, abono sugerido con el costo de materiales del snapshot, cuotas con número y fecha de la primera cuota, y el resumen del pedido por WhatsApp sin mostrar costos. Incluye migración sin borrar datos y pruebas unitarias con los casos de la sección 12.

**Fase 4:**
> Implementa la Fase 4: recibir imágenes compartidas desde WhatsApp y adjuntarlas a un pedido o artículo, borrador automático, aviso de carga por día, ganancia estimada del pedido con opción de ocultarla, filtros de la lista de pedidos, contador "Por entregar hoy" y el flujo de cancelación con ajuste y decisión sobre el abono.
