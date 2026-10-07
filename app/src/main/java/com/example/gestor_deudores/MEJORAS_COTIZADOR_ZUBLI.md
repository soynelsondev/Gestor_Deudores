# Mejoras del Cotizador de Zubli

Documento para tu agente en Android Studio. Revisa tus capturas y el `MANUAL_COTIZADOR_ZUBLI.md`, e indica qué conservar, qué corregir y qué agregar para que el cotizador cubra **sublimación y personalización en general** (franelas, tazas, gorras, llaveros, cuadros, almohadas, mousepads, termos, etc.) y lo entienda cualquier persona.

> **Cómo usar este documento:** trabaja por fases (sección 12). Antes de tocar nada, lee las entidades, el DAO y el ViewModel del cotizador y adapta los nombres de este documento a los reales. No uses migraciones destructivas: hay plantillas y pedidos reales guardados.

---

## 1. Resumen en 30 segundos

**Lo mejor que ya tienes (conservar):** el sistema de lote/paquete, los insumos dinámicos con categorías para los "sobres", el congelamiento de costos al guardar un pedido (snapshot), el resumen con precio y ganancia por pieza, y que todo se conecte con Pedidos y Resumen.

**Lo que más debes cambiar:**
1. La **fórmula**: pasar a `(costo + mano de obra) × (1 + % de ganancia)` y quitar del manual los textos que llaman "error" a ese método.
2. Los **textos y campos**: unificar cómo se pregunta el costo y mostrar el costo por pieza al instante.
3. Separar los **costos por pieza** de los **costos por pedido** (diseño, preparación), para que el precio baje solo cuando suben las cantidades.
4. Reemplazar las **plantillas duplicadas "al mayor"** por una tabla de **precios por cantidad**.
5. Crear una **biblioteca de insumos** reutilizable (papel, tinta, cinta térmica, empaques) para no reescribirlos en cada plantilla.
6. Detallar **tinta, papel, cinta térmica, desgaste de equipos y electricidad** en vez de esconderlos en un solo deslizador.

---

## 2. Qué está bien (no lo rompas)

| Lo que tienes | Por qué es valioso |
|---|---|
| **Lote / Paquete** ("Precio lote" + "Piezas rinde") | Copia la factura sin calculadora. Es la mejor idea del cotizador. |
| **Insumos extra ilimitados**, con categoría Base / Insumo / Servicio | Alimentan los "sobres" del resumen (material, logística, ganancia). |
| **Snapshot de costos** al guardar el pedido | Los pedidos viejos no cambian cuando suben los insumos. Es contabilidad correcta. |
| **Resumen** con costo, precio sugerido y ganancia por pieza | Es el dato que importa: los dólares que quedan por pieza. |
| **Plantillas** y **autocompletado en Pedidos** | Ahorra tiempo y errores al tomar un pedido. |
| **Operatividad (luz/merma)** como opción rápida | Sirve para quien no sabe calcular cada costo (ver sección 6). |

---

## 3. Problemas detectados (con su causa y su arreglo)

### 3.1 La fórmula y el manual (prioridad máxima)
- El manual presenta el margen sobre el precio como la "fórmula oficial" y llama "Error del Principiante" al método **costo + porcentaje**, que es el que usa y entiende la mayoría. Además dice "fórmula contable industrial", una afirmación exagerada.
- El deslizador va de 0 % a 99 %. Con el margen, pedir 100 % da una división entre cero (precios absurdos). Con costo + %, el 100 % es simplemente el doble del costo.
- **Arreglo:** una sola fórmula (sección 4), con el rango del porcentaje de 0 % a 300 % y entrada numérica. El margen se muestra solo como **dato informativo** en el resumen.

### 3.2 Etiquetas inconsistentes
- Sección 1 usa "Precio Lote" + "Piezas". Sección 2 usa "Costo Total" + "Piezas". Es la misma idea con dos nombres, y "Costo Total / Piezas = 1" confunde ("¿total de qué?").
- "Piezas rinde" mezcla dos ideas: cuántas unidades trae el lote y cuántas unidades usa cada pieza.
- El enlace "Agregar insumos extra" aparece al final de la Sección 2, pero la Sección 3 es la de insumos extra.
- Los campos de la Sección 1 usan etiqueta flotante y los de la Sección 2 usan texto grande dentro del campo: se ven distintos.

### 3.3 "Papel (Resma) 100 piezas": ¿cien piezas de qué?
Una resma trae hojas, no piezas. Cada producto usa una cantidad distinta de papel (una taza puede usar una hoja o parte de ella; un llavero, una fracción). Hoy el cotizador no puede expresar "esta pieza usa media hoja". Pasa igual con tinta, cinta térmica y DTF.

### 3.4 Operatividad como "cajón de sastre"
Un solo deslizador de 10 % intenta cubrir luz, merma, desgaste de plancha, impresora, **tinta** y **cinta térmica**. Es cómodo, pero:
- El usuario nunca sabe cuánto gasta en tinta o cinta.
- No se puede ver qué costo subió.
- Dos negocios muy distintos usan el mismo 10 %.

**Veredicto:** el deslizador está bien como **"Modo rápido"** para empezar, pero un cotizador premium debe tener también un **"Modo detallado"** (sección 6).

### 3.5 Falta la mano de obra
No hay dónde poner el tiempo de trabajo. Para quien dice "mi mano de obra es mi ganancia", puede quedar en 0, pero otros usuarios sí necesitan separarla.

### 3.6 Costos por pedido mezclados con costos por pieza
El diseño y el transporte aparecen como "costo total ÷ piezas". Si el pedido es de 2 piezas o de 24, el costo por pieza cambia, y hoy el usuario tiene que recalcular a mano.

### 3.7 Tarifa al mayor como casilla
Marcar "Es tarifa de venta al mayor" obliga a **duplicar toda la plantilla** (una para detal, otra para mayor) y a repetir todos los costos. Si sube un insumo, hay que editar las dos.

### 3.8 Resumen
- Con todo en cero, muestra "$0.00" sin ninguna guía.
- No muestra el **desglose** (cuánto va a materiales, servicios, operativo, mano de obra, ganancia).
- No muestra el equivalente en **bolívares** ni el **precio por docena**.
- No avisa si el precio queda por debajo del costo.

### 3.9 Detalles de uso
- En Venezuela muchas personas escriben decimales con **coma** (`23,4`). La app debe aceptar coma y punto.
- Los deslizadores son imprecisos: hay que poder escribir el número.
- Faltan avisos cuando sube un insumo.

---

## 4. Fórmula única estándar

### 4.1 Definiciones
- **Costos por pieza:** lo que cada pieza consume (prenda, empaque, papel, tinta, cinta, DTF, desgaste...).
- **Costos por pedido:** lo que se paga una vez por pedido, sin importar cuántas piezas (diseño, preparación del arte, envío único).
- **q:** cantidad de piezas del pedido.

### 4.2 Cálculo (por cada pieza)

```
costoPorPieza      = suma de líneas "por pieza"      (precioLote ÷ rinde × consumoPorPieza)
costoPorPedido     = suma de líneas "por pedido"
base(q)            = costoPorPieza + costoPorPedido ÷ q
operativo(q)       = base(q) × operatividad%                      // merma e imprevistos
manoDeObra(q)      = (minutosPorPieza ÷ 60) × tarifaPorHora       // opcional, 0 por defecto
costoTotal(q)      = base(q) + operativo(q) + manoDeObra(q)

precioSinComision  = costoTotal(q) × (1 + gananciaPorcentaje)     // o costoTotal + gananciaFijaEnDolares
precioFinal        = precioSinComision ÷ (1 − comision%)           // solo si el usuario ingresó comisión; por defecto 0
precioRedondeado   = redondeo comercial (a $0.50 o $1, configurable)
gananciaReal       = precioRedondeado − costoTotal − comisiónEnDólares
```

**Reglas:**
- Si el usuario escribe la ganancia en **dólares fijos** en vez de porcentaje, la app calcula el porcentaje equivalente y lo muestra.
- La comisión (cobro con tarjeta, transferencias, etc.) es **la única división**, porque se cobra sobre el precio que paga el cliente. Por defecto está en 0 y se esconde en una sección "Avanzado".
- **Después de redondear**, siempre se recalcula la ganancia real y se muestra.
- Si el modo detallado ya incluye tinta, cinta, electricidad y equipos, el porcentaje de operatividad queda solo para **merma e imprevistos**, y la app avisa para no contar dos veces lo mismo.

### 4.3 Ejemplo con los números de tus capturas (ilustrativo)

Franela: $23.40 el lote de 6. Empaque: $5.54 el lote de 100. DTF: $8.50 el metro, alcanza para 6. Transporte: $5 para 6. Operatividad 10 %. Ganancia 40 %.

| Concepto | Cuenta | Por pieza |
|---|---|---|
| Franela | 23.40 ÷ 6 | $3.9000 |
| Empaque | 5.54 ÷ 100 | $0.0554 |
| DTF | 8.50 ÷ 6 | $1.4167 |
| Transporte | 5 ÷ 6 | $0.8333 |
| Subtotal | | $6.2054 |
| Operatividad 10 % | | $0.6205 |
| **Costo total** | | **$6.83** |
| Ganancia 40 % sobre el costo | 6.8259 × 0.40 | $2.73 |
| **Precio sugerido** | 6.8259 × 1.40 | **$9.56** |

Resumen que debe verse: *"Ganas $2.73 por pieza (40 % sobre tu costo, 28.6 % del precio). Una docena: $114.67 de venta y $32.77 de ganancia."*

### 4.4 Ejemplo de precios por cantidad (con un diseño personalizado de $3 por pedido)

El diseño se paga una vez por pedido y se reparte entre las piezas. Con el mismo costo de $6.83 por pieza y operatividad 10 % sobre el diseño ($3.30):

| Cantidad | Costo por pieza | Precio (40 %) |
|---|---|---|
| 1 | $10.13 | **$14.18** |
| 6 | $7.38 | **$10.33** |
| 12 | $7.10 | **$9.94** |
| 24 | $6.96 | **$9.75** |

Así el precio baja solo con el volumen, sin crear plantillas duplicadas.

---

## 5. Modelo de insumos mejorado

### 5.1 Una línea de costo = tres datos claros

| Dato | Pregunta en pantalla | Ejemplo |
|---|---|---|
| Precio del lote | "¿Cuánto pagaste?" | $8.50 |
| Qué trae el lote | "¿Cuánto trae?" (con unidad) | 1 metro |
| Consumo por pieza | "¿Cuánto usa cada pieza?" | 1/6 de metro |

`costo por pieza = precio del lote ÷ cantidad que trae × consumo por pieza`

### 5.2 Unidades
Pieza, hoja, metro / centímetro, litro / mililitro, kilo / gramo, m². La app convierte dentro de la misma familia (m ↔ cm, l ↔ ml, kg ↔ g).

### 5.3 Atajo para quien no sabe el consumo
Cada línea permite elegir entre:
- **Calcular** (lote + consumo), o
- **"Ya sé cuánto me cuesta por pieza"** (escribir el costo directo).

### 5.4 Vista previa inmediata
Al llenar los campos, mostrar debajo en tiempo real: **"= $3.90 por pieza"**. Es lo que quita el miedo a no entender.

### 5.5 Ayuda por campo
Un icono (?) con un ejemplo corto: *"Compraste una caja de 36 tazas a $45. Escribe 45 y 36."*

---

## 6. Qué hacer con tinta, papel, cinta térmica, DTF, equipos y electricidad

### 6.1 Modo rápido y modo detallado
- **Modo rápido** (por defecto para principiantes): se mantiene el deslizador de Luz/Merma, con la explicación "Cubre luz, merma y desgaste. Si prefieres calcularlos uno por uno, usa el Modo detallado".
- **Modo detallado:** líneas separadas para cada consumible, equipo y servicio. El deslizador queda solo para merma e imprevistos.

La app debe **avisar** si en modo detallado el usuario deja el 10 % de operatividad *y además* detalló tinta y cinta (posible doble conteo).

### 6.2 Cómo capturar cada costo

| Costo | Cómo se ingresa | Fórmula |
|---|---|---|
| **Papel de sublimación** | Lote: resma (cantidad de hojas). Consumo: hojas por pieza (por ejemplo 1, 0.5, 0.25) | precio ÷ hojas × hojas por pieza |
| **Tinta de sublimación** | Lote: botella (ml). Rinde: hojas que imprime en promedio. Consumo: hojas por pieza | precio ÷ hojas que rinde × hojas por pieza |
| **Cinta térmica** | Lote: rollo (metros). Consumo: centímetros por pieza | precio ÷ longitud × consumo |
| **DTF** | Lote: precio por metro. Consumo: fracción del metro que ocupa el diseño | ver 6.3 |
| **Empaque** | Lote: paquete. Consumo: 1 por pieza | precio ÷ rinde |
| **Equipos** (plancha, prensa de tazas, impresora, cabezal) | Precio del equipo y **vida útil estimada en piezas** | precio ÷ vida útil = desgaste por pieza |
| **Electricidad** | Costo estimado por pieza, o potencia × tiempo × tarifa | directo o calculado |
| **Merma** | Porcentaje de piezas dañadas | % sobre el costo |

Los valores de consumo (cuántas hojas usa una taza, cuántos cm de cinta) **los decide el usuario con su experiencia**. La app puede sugerir una casilla vacía con ayuda, pero no debe imponer números como si fueran verdad universal.

### 6.3 Ayudante de DTF (opcional pero muy útil)
Entradas: ancho útil del rollo, ancho y alto del diseño, separación entre diseños.
Salida: piezas por metro (aproximadas) y costo del DTF por pieza.
Ejemplo de lógica: `piezasPorMetro = floor(anchoRollo ÷ (anchoDiseño + sep)) × floor(100 ÷ (altoDiseño + sep))`.
El usuario puede ajustar el resultado a mano.

### 6.4 Calibrador de tinta (fase posterior)
Pregunta: *"Con una botella, ¿cuántas hojas imprimiste aproximadamente?"* Guarda el dato y lo usa para todos los productos. Se puede afinar con el tiempo, comparando con lo que realmente se gasta.

---

## 7. Precios por cantidad (reemplaza la casilla "al mayor")

Dentro de **una sola plantilla**, una tabla editable:

| Escala | Desde (piezas) | Ganancia |
|---|---|---|
| Detal | 1 | 40 % |
| Mayor | 6 | 30 % |
| Mayor grande | 24 | 25 % |

- El usuario puede agregar o quitar escalas.
- En **Pedidos**, al cambiar la cantidad, la app toma la escala que corresponde y calcula el precio con los costos por pedido repartidos (sección 4). Muestra la etiqueta "Tarifa al mayor aplicada (≥ 6 piezas)".
- En el cotizador, un botón **"Ver tabla de precios"** genera una tabla automática: 1, 6, 12, 24, 50 piezas, con precio por pieza, precio total y ganancia.
- Si el usuario cambia el precio a mano en el pedido, la app **muestra de inmediato cuánto gana** y avisa si baja de su mínimo.
- **Migración:** las plantillas actuales marcadas "al mayor" se convierten en escalas de su plantilla de detal si coinciden en nombre; si no, se conservan tal cual hasta que el usuario las una.

---

## 8. Biblioteca de insumos

**Problema:** hoy el papel, el empaque o la tinta se escriben en cada plantilla. Si cambia el precio del papel, hay que editar todas.

**Solución:** pantalla **"Mis insumos"** (en el menú):
- Cada insumo: nombre, categoría, unidad, precio del lote, cantidad que trae, fecha de actualización.
- Las plantillas **usan** los insumos y solo guardan el consumo por pieza.
- Cambiar el precio una vez actualiza todas las plantillas.
- **Historial de precios** por insumo (fecha y valor).
- **Alerta de aumento:** al subir un insumo, la app lista las plantillas afectadas con *"Ya no ganas lo planeado. Precio nuevo sugerido: $X"* y un botón para actualizar.
- Los pedidos anteriores **no cambian** (snapshot).
- Opción de **moneda del insumo**: si se compra en bolívares, guardar el monto en Bs y la tasa usada, y mostrar el costo en USD con la tasa actual (aviso de que cambia con la tasa).

**Para no romper lo existente:** al migrar, cada plantilla actual conserva sus costos como líneas con precio local. Después, un asistente opcional ofrece "convertir en insumo de la biblioteca" cuando detecta el mismo nombre en varias plantillas.

---

## 9. Plantillas prearmadas por tipo de producto

Al crear una plantilla, el usuario elige un tipo y la app **precarga las líneas típicas vacías** (solo hay que escribir precios y consumos):

| Producto | Líneas sugeridas |
|---|---|
| **Taza** | Taza, caja o empaque, papel, tinta, cinta térmica, desgaste de prensa, electricidad |
| **Franela (DTF)** | Franela, DTF, bolsa o empaque, etiqueta (opcional), desgaste de plancha |
| **Franela (sublimación)** | Franela sublimable, papel, tinta, cinta térmica, bolsa, desgaste de plancha |
| **Gorra** | Gorra, papel, tinta, cinta térmica, empaque, desgaste de prensa |
| **Llavero** | Base (MDF u otro), argolla o accesorio, papel, tinta, cinta térmica, bolsita |
| **Cuadro / placa** | Base o marco, papel, tinta, cinta térmica, embalaje |
| **Almohada / cojín** | Funda sublimable, relleno (opcional), papel, tinta, cinta térmica, empaque |
| **Mousepad** | Base, papel, tinta, cinta térmica, empaque |
| **Termo / botella / otros** | Producto base, papel, tinta, cinta térmica, empaque |
| **Personalizado** | Vacío, para armar a gusto |

Cada plantilla también incluye: **diseño personalizado (por pedido)**, **tiempo de producción por pieza** (opcional) y la **tabla de precios por cantidad**.

Además: botón **"Duplicar plantilla"** (para variantes como tazas de 11 oz y 15 oz o talla infantil y adulto).

---

## 10. Interfaz y textos (claros para cualquiera)

### 10.1 Reglas generales
- Un solo estilo para todos los campos (etiqueta flotante), en todas las secciones.
- Textos de ayuda cortos, con ejemplos reales de la vida del taller.
- Aceptar **coma y punto** en los decimales.
- Teclado numérico en campos de dinero y cantidad.
- Botón **"Guardar"** siempre visible; confirmar si se sale con cambios sin guardar.

### 10.2 Cambios de etiquetas

| Hoy | Mejor |
|---|---|
| Precio Lote | ¿Cuánto pagaste? |
| Piezas / Piezas rinde | ¿Para cuántas piezas alcanza? (o ¿Cuánto trae?) |
| Costo Total (Sección 2) | ¿Cuánto cuesta? |
| Costos Operativos (Luz/Merma) | Luz, merma y desgaste |
| Margen de Ganancia Libre | **Tu ganancia (%)** |
| Es Tarifa de Venta al Mayor | Precios por cantidad (tabla) |
| Resumen de Cotización | Resumen de precio |

### 10.3 Estructura de pantalla sugerida
1. **Producto** (nombre, tipo).
2. **Materiales** (prenda o base, empaque).
3. **Impresión** (papel, tinta, cinta, DTF).
4. **Servicios** (transporte, diseño por pedido).
5. **Tiempo y mano de obra** (opcional).
6. **Gastos del taller** (equipos, electricidad, merma).
7. **Ganancia y precios por cantidad.**

Cada sección con un resumen de una línea ("Materiales: $3.96 por pieza") para ver cómo va sin abrir todo.

### 10.4 Resumen mejorado (tarjeta fija abajo)
- **Estado vacío:** *"Completa la prenda o base para ver tu precio."* en vez de $0.00.
- **Costo por pieza** y **precio sugerido**, grandes.
- **Ganancia por pieza** y **por docena**.
- **Equivalente en Bs** (con la tasa del día).
- **Desglose** desplegable: materiales, servicios, operativo, mano de obra, ganancia.
- **Indicadores:** "40 % sobre tu costo · 28.6 % del precio de venta" (el margen aparece aquí, como información).
- **Entrada numérica** junto a cada deslizador (con pasos de 1 %).
- Selector: **ganancia en % o en dólares fijos**.

---

## 11. Protecciones y herramientas extra

| Función | Qué hace |
|---|---|
| **Aviso de pérdida** | Si el precio es menor que el costo (más comisión): rojo, *"Con este precio pierdes dinero."* |
| **Precio mínimo** | El usuario fija cuánto quiere ganar como mínimo por pieza; avisa si el precio queda por debajo (útil en ventas al mayor). |
| **Precio del mercado (referencia)** | Campo opcional para comparar: *"Tu precio está $1.20 por encima de lo que cobra otro taller."* |
| **Redondeo comercial** | Redondear a $0.50 o $1 y recalcular la ganancia real. |
| **Cotización para el cliente** | Genera un mensaje, imagen o PDF **sin mostrar costos**: producto, cantidad, precio por pieza, total, vigencia y abono sugerido. Se envía por WhatsApp. |
| **Mano de obra → sobre "Mi pago"** | Si se usa mano de obra, el resumen de sobres agrega "Mi pago" además de material, logística y ganancia. |
| **Combos o kits** (fase posterior) | Unir plantillas (taza + franela + caja de regalo) con la suma de costos. |
| **Variantes** (fase posterior) | Tallas, colores o tamaños del arte con costos distintos dentro de una misma plantilla. |

---

## 12. Modelo de datos sugerido (adaptar a los nombres reales)

Tablas nuevas (con `Migration` que **no borra nada**):

| Tabla | Campos principales |
|---|---|
| `Insumo` | id, nombre, categoria (BASE / INSUMO / SERVICIO / EQUIPO), unidadLote, precioLote, cantidadLote, moneda, tasaUsada, fechaActualizacion |
| `HistorialPrecioInsumo` | id, insumoId, precioLote, cantidadLote, fecha |
| `PlantillaLinea` | id, plantillaId, insumoId (nullable), nombreLocal, categoria, tipoCosto (POR_PIEZA / POR_PEDIDO), precioLoteLocal, cantidadLoteLocal, unidadLote, consumoPorPieza, unidadConsumo, costoDirectoPorPieza (nullable) |
| `EscalaPrecio` | id, plantillaId, nombre, desdeCantidad, gananciaTipo (PORCENTAJE / FIJO), gananciaValor |

Columnas nuevas en la tabla de plantillas existente (con valores por defecto): `tipoProducto`, `modoCosteo` (RAPIDO / DETALLADO), `minutosPorPieza`, `tarifaPorHora`, `comisionPorcentaje`, `redondeo`, `precioMinimo`.

**Notas importantes:**
- Para equipos, reutilizar `Insumo` con `cantidadLote` = vida útil en piezas y `consumoPorPieza` = 1.
- Cada columna nueva en la base debe declararse **con el mismo valor por defecto** en la entidad de Room.
- **Dinero:** si los montos están en `Double`, redondear a 2 decimales al mostrar y al sumar; evitar acumular errores.
- **Snapshot del pedido:** agregar al registro congelado la cantidad, la escala aplicada, la tasa usada, el costo por pieza, el costo por pedido, la mano de obra y la ganancia calculada.
- **Migración de las plantillas actuales:** convertir cada campo existente (pieza, empaque, papel, DTF, transporte, diseño, extras) en una `PlantillaLinea`, conservando sus precios y su "piezas rinde". Verificar que el precio sugerido de cada plantilla migrada **sea idéntico** al anterior (salvo cambio de fórmula, que debe quedar claro al usuario).

---

## 13. Cambios para el manual del usuario (`MANUAL_COTIZADOR_ZUBLI.md`)

1. Reescribir la sección 1: la fórmula es **costo + mano de obra + porcentaje de ganancia**. Quitar "Error del Principiante", "fórmula oficial" y "contable industrial".
2. Explicar con un ejemplo de franela y otro de taza, **con la comprobación en dólares por pieza**.
3. Agregar una sección sobre **costos por pieza vs. por pedido**.
4. Agregar una sección de **precios por cantidad** (reemplaza "Cajero de tarifas al mayor").
5. Agregar una sección de **tinta, papel, cinta, equipos y electricidad**.
6. Aclarar que el **margen** (porcentaje sobre el precio) se muestra como dato informativo.
7. Mantener la sección del flujo hacia Pedidos y Resumen, añadiendo la mano de obra y el sobre "Mi pago".

---

## 14. Pruebas obligatorias

**Cálculo (con los números de la sección 4):**
- Costo por pieza $6.8259 (redondeo a 2 decimales al mostrar $6.83).
- Ganancia 40 % → $9.56 (ganancia $2.73).
- Ganancia 0 % → precio igual al costo.
- Ganancia 100 % → doble del costo (sin errores ni valores enormes).
- Diseño por pedido $3 con q = 1, 6, 12, 24 → $14.18, $10.33, $9.94, $9.75.
- Mano de obra $1.50 por pieza con costo base $7.18 → base ajustada y precio coherente.
- Ganancia fija de $2.00 → porcentaje equivalente mostrado.
- Comisión 3 % → `precio = (costo + ganancia) ÷ 0.97`.
- Comisión 100 % o más → error con mensaje, no cálculo.
- Precio menor al costo → aviso de pérdida.
- Redondeo a $0.50 → ganancia real recalculada.

**Entradas:**
- `23,4` y `23.4` dan el mismo resultado.
- `piezas = 0` o vacío → mensaje claro, sin cierre de la app.
- Texto en campos numéricos → rechazado.
- Todo vacío → estado vacío, sin $0.00 confuso.

**Datos y migración:**
- Instalar la versión actual con plantillas y pedidos reales de prueba; actualizar encima; verificar que **plantillas, precios y pedidos antiguos no cambian**.
- Cambiar el precio de un insumo en la biblioteca: las plantillas se actualizan; los pedidos antiguos no.
- Plantilla con escalas: cambiar cantidad en Pedidos y ver el precio correcto.

---

## 15. Orden de implementación

| Fase | Qué | Resultado |
|---|---|---|
| **1 (antes de vender el cotizador)** | Fórmula única + textos y etiquetas unificadas + vista previa "= $X por pieza" + coma decimal + estado vacío + resumen con desglose, Bs y docena + aviso de pérdida + manual corregido | El cotizador se entiende y no engaña |
| **2** | Costos por pieza vs. por pedido + tabla de precios por cantidad (reemplaza "al mayor") + mano de obra opcional + comisión opcional | Precios correctos para detal y mayor |
| **3** | Biblioteca de insumos + unidades y consumo por pieza + historial + alertas de aumento | Cotizador que se mantiene al día |
| **4** | Modo detallado: tinta, papel, cinta, equipos, electricidad + ayudante de DTF + plantillas prearmadas por producto | Cubre toda la sublimación y personalización |
| **5** | Cotización para el cliente (PDF, imagen o WhatsApp) + precio de mercado + precio mínimo + redondeo | Herramienta premium completa |
| **6 (después)** | Variantes, combos, calibrador de tinta | Pulido y casos avanzados |

---

## 16. Prompts para tu agente

**Fase 1:**
> Lee el cotizador (entidades, DAO, ViewModel y pantallas) y este documento. Implementa solo la Fase 1: cambia la fórmula a `(costo + mano de obra) × (1 + ganancia%)` con la ganancia de 0 % a 300 % y entrada numérica además del deslizador; unifica las etiquetas según la sección 10; muestra "= $X por pieza" al llenar cada línea; acepta coma y punto en los decimales; agrega el estado vacío y el resumen con desglose, equivalente en Bs, precio por docena y aviso de pérdida; muestra el margen solo como indicador informativo. No cambies el esquema de la base de datos en esta fase y no alteres los pedidos ya guardados.

**Fase 2:**
> Implementa la Fase 2 según las secciones 4, 7 y 12: costos por pieza y por pedido, tabla de precios por cantidad que reemplace la casilla "al mayor", mano de obra y comisión opcionales. Escribe la `Migration` sin borrar datos, convierte las plantillas existentes sin cambiar su precio sugerido y agrega pruebas unitarias con los casos de la sección 14.

**Fases 3 y 4:**
> Implementa la biblioteca de insumos, las unidades y el consumo por pieza, el historial de precios y las alertas de aumento (sección 8). Luego el modo detallado con las líneas de la sección 6 y las plantillas prearmadas de la sección 9. Mantén la compatibilidad con plantillas y pedidos existentes.
