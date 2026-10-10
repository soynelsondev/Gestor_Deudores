# Mejoras del Resumen Financiero de Zubli

Documento para tu agente en Android Studio. Describe cómo convertir el módulo **Resumen** (`ui/Resumen/`) en una herramienta que responda las preguntas reales de un dueño de taller: si gana dinero, cuánto puede retirar, cuánto va a cobrar y pagar, qué productos y clientes le dejan más, y si va bien contra su meta.

> **Reglas para el agente**
> 1. Antes de editar, lee `ResumenViewModel.kt`, `resumenView.kt`, las entidades, los DAO de pedidos, abonos y cuentas, y adapta los nombres de este documento a los reales.
> 2. Trabaja por fases (sección 21). Compila y prueba al terminar cada una, y haz commit.
> 3. **Prohibido** borrar datos al migrar. Toda tabla o columna nueva lleva su `Migration` y su valor por defecto declarado igual en la entidad de Room. Prueba siempre instalando encima de la versión anterior con datos de prueba.
> 4. **No cambies los números que el Resumen muestra hoy** para períodos pasados: la migración debe congelar el reparto actual (sección 5.4) y una prueba debe comprobar que los totales no varían.
> 5. Haz las sumas y agregaciones con consultas SQL (`SUM`, `GROUP BY`) y no cargando todo en memoria.
> 6. Todo monto que se muestre debe poder explicarse: de qué período es y con qué datos se calculó.
> 7. Si algo no encaja con el código real, detente y explícalo antes de improvisar.

---

## 1. Resumen

El módulo actual ya tiene una base sólida: reparte cada abono en sobres según la **fotografía de costos** del pedido, muestra KPI, gráfico de flujo, radiografía de abonos, bancos, estado de pedidos y alertas. Los números de las capturas cuadran: $10.85 + $2.77 + $5.01 + $14.75 = $33.38.

**Lo más importante a cambiar:**
1. **Guardar el reparto de cada abono en el momento del pago** (hoy se recalcula, y si se edita un pedido cambian los meses pasados).
2. **Separar lo exacto de lo estimado**, y los cobros que no son ventas.
3. **Renombrar o completar la "Ganancia Neta Libre"**: hoy no descuenta los gastos del taller.
4. **Compras y gastos** que descuenten de los sobres, para que los sobres tengan saldo disponible.
5. **Cuentas ligadas a los abonos**, con movimientos automáticos.
6. **"Disponible para retirar"**, proyección de cobros y pagos, antigüedad de cartera, rentabilidad real, meta y punto de equilibrio.
7. **Estado actual de pedidos** separado del período, y alertas que se puedan accionar.

---

## 2. Qué está bien (conservar)

| Lo que tienes | Por qué es valioso |
|---|---|
| Reparto de cada abono por la fotografía de costos del pedido | Contabilidad real, no estimada |
| Sobres: material, transporte, tintas y papel, ganancia | Separa lo que hay que reponer de lo que es ganancia |
| Radiografía de abonos | Permite auditar cada pago |
| Filtros por período, comparativa con el mes anterior | Da contexto |
| Cambio USD / Bs | Necesario en Venezuela |
| Cuentas y billeteras | Base para saber dónde está el dinero |
| Alertas y top de productos | Ya existe la estructura |
| Cálculo reactivo con Flow | Los datos se actualizan solos |

---

## 3. Problemas detectados

| Problema | Por qué importa | Arreglo (sección) |
|---|---|---|
| El reparto se **recalcula** con el total actual del pedido | Editar un pedido cambia abonos de meses anteriores | Reparto guardado por abono (5) |
| Pagos sueltos usan 50/10/10/30 por defecto | Mezcla datos exactos con estimados; un cobro que no es venta se reparte en materiales | Tipos de abono y etiqueta "estimado" (5.2, 5.3) |
| "Ganancia Neta Libre" solo descuenta material, transporte e insumos | "Neta" promete más: falta luz, equipos, alquiler y el pago del dueño | Renombrar y agregar gastos (10) |
| Los sobres no tienen saldo | Muestran lo que "corresponde", no lo disponible | Compras y saldo de sobres (7) |
| Saldos de cuentas parecen manuales (en $0.00) | No se actualizan con los abonos ni con las compras | Movimientos automáticos (8) |
| "Por Cobrar" es el total de hoy y los demás KPI son del período | Se mezclan y confunden | Etiquetar cada tarjeta (19) |
| "Estado de pedidos" cambia según el período | Producción necesita el estado actual | Dos paneles (18) |
| Alerta larga y sin acción | No ayuda a decidir | Alertas accionables (17) |
| Nombres distintos para lo mismo (Fondo Reposición, Tintas & Extras, Costos adicionales, Pasajes & Transp., Transporte) | Confunde | Glosario único (20) |
| Carrusel de sobres con tarjetas cortadas | No se ven todos | Cuadrícula (19) |
| Bs históricos mostrados sin aclarar tasa | Puede inducir a error | Aclarar "a tasa de hoy" o "recibido en Bs" (14) |
| Sin retiros del dueño | No se sabe cuánto ya se pagó | Retiros (9) |

---

## 4. Definiciones y fórmulas (glosario financiero)

Dos lecturas distintas, siempre etiquetadas:

- **Caja (lo cobrado):** dinero que entró en el período (abonos).
- **Ventas (lo vendido):** pedidos tomados en el período, aunque no estén cobrados.

```
cobradoPeriodo        = Σ abonos del período de tipo VENTA_PEDIDO o COBRO_MANUAL (tipo NO_AFECTA se excluye)
vendidoPeriodo        = Σ total de pedidos creados en el período y no cancelados
porCobrarHoy          = Σ saldos pendientes de todos los clientes a la fecha de hoy (no depende del período)

reposicionPeriodo     = Σ asignado a material + transporte + insumos de los abonos del período
gananciaLibrePeriodo  = Σ asignado a ganancia de los abonos del período     // antes de gastos del taller y de "Mi pago"
gastosTallerPeriodo   = Σ gastos del taller del período (sección 10)
gananciaNetaPeriodo   = gananciaLibrePeriodo − gastosTallerPeriodo
```

Si el cotizador usa **mano de obra**, el reparto agrega un sobre **"Mi pago"** y la ganancia libre es lo que queda después de él.

---

## 5. Reparto de abonos guardado (cambio más importante)

### 5.1 Qué hacer
Al registrar cada abono, **calcular y guardar su desglose** en una tabla propia. Los reportes suman esos registros; ya no recalculan desde el pedido.

### 5.2 Tipos de abono
Cada abono tiene un tipo (seleccionable al registrarlo; por defecto, el que corresponda según tenga pedido o no):

| Tipo | Qué es | ¿Entra en el reparto? |
|---|---|---|
| `VENTA_PEDIDO` | Abono de un pedido con fotografía de costos | Sí, reparto exacto |
| `COBRO_MANUAL` | Pago de una cuenta manual sin pedido (por ejemplo, una deuda anterior) | Sí, con porcentajes base, marcado como **estimado** |
| `NO_AFECTA` | Un pago que no es una venta del taller (por ejemplo, un préstamo personal) | No: suma a la deuda y a la cuenta, pero no a ingresos ni a sobres |

### 5.3 Algoritmo de reparto

**Con fotografía de costos (exacto):**
```
proporcion  = montoAbonoUsd ÷ totalPedidoUsd           // total del pedido vigente al momento del abono
material    = round2(costoPiezaBase  × proporcion)
transporte  = round2(costoTransporte × proporcion)
insumos     = round2(costoInsumos    × proporcion)
miPago      = round2(costoManoDeObra × proporcion)     // 0 si no se usa mano de obra
ganancia    = montoAbonoUsd − (material + transporte + insumos + miPago)
```
**Sin fotografía (estimado):** usar los porcentajes base que configure el dueño (por defecto, 50 % material, 10 % transporte, 10 % insumos, 30 % ganancia), con la ganancia como resto. Guardar `origen = ESTIMADO`.

**Reglas:**
- Redondear a 2 decimales cada sobre y asignar el resto a la ganancia, para que la suma siempre sea exactamente el abono.
- Si los costos asignados superan al abono (venta con pérdida), la ganancia sale **negativa** y debe mostrarse así, con alerta.
- Si el abono se registró en Bs, usar su equivalente en USD con la tasa guardada en ese movimiento.
- **Devoluciones y cancelaciones:** crean un movimiento negativo con su propio desglose (el inverso del original). Nunca borrar el desglose original.
- **Si el pedido se edita después de haber abonos:** los desgloses guardados **no cambian**. Los abonos futuros usan el total nuevo. Ofrecer, de forma opcional y con confirmación, un **ajuste** (un movimiento aparte con fecha) que corrija la diferencia.

### 5.4 Migración de los abonos existentes
Al migrar, calcular el desglose de **cada abono antiguo con la lógica actual** y guardarlo (`origen = EXACTO` si tenía fotografía y `ESTIMADO` si no). **Antes de migrar, anotar los totales que muestra hoy el Resumen para cada período, y verificar que, después, sean idénticos.** Esa comparación es una prueba obligatoria.

---

## 6. Modelo de datos sugerido (adaptar a los nombres reales)

| Tabla | Campos principales |
|---|---|
| `DistribucionAbono` | id, abonoId, pedidoId (nullable), tipoAbono, montoUsd, material, transporte, insumos, miPago, ganancia, origen (EXACTO / ESTIMADO), fecha |
| `CategoriaGasto` | id, nombre, tipo (COMPRA_INSUMO / GASTO_TALLER), sobreAsociado (nullable: MATERIAL / TRANSPORTE / INSUMOS), recurrente, activa |
| `Gasto` | id, categoriaId, descripcion, montoUsd, moneda, montoOriginal, tasa, cuentaId, proveedor (nullable), fecha, insumoId (nullable) |
| `Retiro` | id, montoUsd, moneda, montoOriginal, tasa, cuentaId, nota, fecha |
| `CuentaBancaria` (existente) | Agregar: moneda, saldoInicial, activa. El saldo pasa a ser calculado |
| `MovimientoCuenta` | id, cuentaId, tipo (ABONO / GASTO / RETIRO / TRANSFERENCIA_ENTRADA / TRANSFERENCIA_SALIDA / AJUSTE), monto (con signo), moneda, tasa, referenciaId, nota, fecha |
| `MetaMensual` | id, mes (año-mes), metaGananciaUsd, retiroObjetivoUsd |
| `CierreCaja` | id, fecha, cuentaId, saldoEsperado, saldoReal, diferencia, nota |
| `ConfiguracionResumen` | porcentajes base para pagos estimados, reserva mínima de caja, días de gracia de cuota, umbrales de alertas |

**Notas técnicas:**
- Guardar las fechas en un formato **ordenable** (milisegundos o `yyyy-MM-dd`), nunca solo `dd/MM/yyyy`, para poder filtrar por período con SQL. Si las fechas actuales están en texto, agregar una columna ordenable en la migración.
- Índices en las columnas de fecha, `cuentaId`, `pedidoId` y `abonoId`.
- Si los montos están en `Double`, redondear a 2 decimales al guardar y sumar.
- Los períodos (semana de lunes a domingo, mes, 3 meses) deben calcularse con la zona horaria del teléfono; probar el borde del cambio de mes.

---

## 7. Sobres con saldo disponible

**Problema:** hoy el sobre dice lo que "corresponde" reponer en el período, pero no hay forma de registrar cuando se compra material.

**Solución:** módulo **Compras y gastos**.
- Registrar una compra: categoría (material, transporte, tintas y papel, otros), descripción, monto y moneda, cuenta de pago, proveedor (opcional) y fecha. Si se paga en Bs, guardar tasa y monto original.
- Cada compra de insumo **descuenta del sobre correspondiente**.
- Cada sobre muestra: **asignado acumulado − comprado = disponible para reponer.** El acumulado se calcula desde la fecha de inicio que el usuario elija (por defecto, el inicio del uso de la app).
- Saldo de sobre negativo = se gastó más de lo reservado: alerta suave ("Compraste $X más de lo que habías reservado para tintas").
- Opcional: una compra de insumo puede **actualizar el precio del insumo** en el cotizador (biblioteca de insumos), con confirmación.

---

## 8. Cuentas y movimientos

- Las cuentas dejan de tener un saldo editable a mano. **Saldo = saldo inicial + suma de movimientos.**
- Cada abono recibe una **cuenta destino** (usando el método de pago registrado en el pedido) y genera un movimiento `ABONO`. Cada gasto y cada retiro generan su movimiento negativo.
- **Transferencia entre cuentas** (por ejemplo, de Pago Móvil a efectivo), con conversión si cambia la moneda.
- **Ajuste de saldo:** en lugar de editar el número, registrar un ajuste con motivo y fecha. Así queda historial.
- Cuentas en Bs: mostrar el saldo en Bs y su **equivalente en USD con la tasa de hoy**.
- Total de caja = suma de saldos en USD (los Bs convertidos a la tasa de hoy).
- Migración: las cuentas actuales se conservan con su saldo como `saldoInicial` (a la fecha de la migración); no se inventan movimientos pasados.

---

## 9. Disponible para retirar y retiros

Pantalla de retiros: registrar cuánto sacó el dueño para sí, de qué cuenta y cuándo. Con eso se calcula el número más útil del Resumen:

```
gananciaPendiente    = Σ ganancia libre acumulada − Σ gastos del taller acumulados − Σ retiros
cajaUsd              = Σ saldos de cuentas (en USD, Bs a tasa de hoy)
reservas             = Σ saldos positivos de sobres de reposición + cuentas por pagar a proveedores + reservaMinimaCaja
disponibleParaRetirar = max(0, min(gananciaPendiente, cajaUsd − reservas))
```

**Ejemplo ilustrativo:** ganancia pendiente $120, caja $200, reservas $110 → disponible para retirar **$90**, no $120.

Reglas:
- Mostrar las tres piezas al tocar la tarjeta, para que se entienda por qué el número es el que es.
- Si `disponibleParaRetirar` es 0 pero hay ganancia pendiente, mostrar el motivo: *"Tienes ganancia, pero tu caja no alcanza para cubrir las reposiciones."*
- La reserva mínima de caja la configura el dueño (por defecto, 0).
- Un retiro mayor al disponible no se bloquea, pero pide confirmación con aviso.

---

## 10. Gastos del taller y ganancia neta

- Categorías sugeridas (editables): luz, internet, alquiler, mantenimiento de equipos, desgaste o reposición de equipos, publicidad, comisiones, otros.
- Gastos **recurrentes:** monto mensual que se reparte en los meses (por ejemplo, el alquiler).
- **Nombres en pantalla:**
  - *Ganancia libre* = antes de gastos del taller.
  - *Ganancia neta* = después de gastos del taller.
  - Si el usuario no registra gastos, mostrar solo "Ganancia libre (antes de gastos del taller)" y un aviso: *"Registra tus gastos del taller para ver tu ganancia neta real."*

---

## 11. Proyección de cobros y pagos (7 y 30 días)

```
cobrosEsperados(7 y 30 días)  = Σ cuotas programadas con vencimiento en la ventana (según el cálculo de cuotas)
                                + saldos de pedidos "al retirar" cuya fecha de entrega cae en la ventana
pagosEsperados(7 y 30 días)   = gastos recurrentes que vencen en la ventana + cuentas por pagar a proveedores
```

- Mostrar siempre como **"esperado"** (es una proyección, no dinero cobrado), con un detalle desplegable de quién y cuánto.
- Línea final: *"Esta semana esperas cobrar unos $X y pagar unos $Y."*
- No contar como esperado lo que ya está vencido: va a la cartera vencida (sección 12).

---

## 12. Antigüedad de cartera y puntualidad

**Antigüedad:** por cada cliente con monto vencido, calcular los días de atraso desde la cuota vencida más antigua sin cubrir.

| Grupo | Criterio |
|---|---|
| Al día | Sin monto vencido |
| 1 a 7 días | Atraso entre 1 y 7 días |
| 8 a 30 días | Atraso entre 8 y 30 días |
| Más de 30 días | Atraso mayor a 30 días |

Mostrar monto y número de clientes por grupo, los **mayores deudores** y un botón de **recordatorio por WhatsApp** en cada uno.

**Cuotas cobradas a tiempo (%):**
```
cuotasConVencimientoEnElPeriodo = N
aTiempo = cuotas cuyo acumulado de abonos, a la fecha de vencimiento + días de gracia, ya cubría esa cuota
porcentaje = aTiempo ÷ N
```
(el acumulado se evalúa por orden cronológico de cuotas, del más antiguo al más nuevo, usando las fechas de los abonos). Mostrar con semáforo y la tendencia contra el período anterior.

---

## 13. Rentabilidad real

Basada en las fotografías de costos de los pedidos (no en los abonos):

| Indicador | Cálculo |
|---|---|
| Ganancia por pedido | total del pedido − costo total congelado |
| **Ganancia por producto** | Σ ganancia de sus artículos, unidades vendidas, ganancia por unidad y margen |
| **Descuentos dados** | Σ (precio sugerido congelado − precio cobrado) × cantidad, solo cuando es positivo. Mostrar: *"Este mes regalaste $X en descuentos"* |
| Ticket promedio | vendido ÷ número de pedidos |
| Mejores clientes | Por total comprado y por puntualidad de pago |
| Ganancia por hora | (ganancia + mano de obra del pedido) ÷ horas estimadas (tiempo por pieza × cantidad), si se usa tiempo |

El ranking de productos debe ordenarse por **ganancia**, con la opción de ver por **unidades**. Aclarar en pantalla la base (pedidos creados en el período).

---

## 14. Bolívares y dólares

- Cuentas en Bs: **valor en USD hoy** y valor hace 30 días (usando el historial de tasas).
- Variación de los bolívares: `saldoBs ÷ tasaHoy − saldoBs ÷ tasaHace30Dias`. Mostrar como **efecto cambiario** (normalmente negativo), **separado de la ganancia del negocio**: *"Tus Bs 5.000 valían $6.20 hace un mes y hoy valen $5.70."*
- En los reportes en Bs, aclarar con una etiqueta si el monto es **"recibido en Bs"** (dato real) o **"equivalente a tasa de hoy"** (conversión).
- Mostrar la fecha y antigüedad de la tasa; si pasaron más de 24 horas, un aviso discreto, y permitir corregirla a mano.

---

## 15. Meta, punto de equilibrio y sueldo objetivo

- **Meta mensual de ganancia** con barra de progreso y semáforo (verde: va al ritmo; amarillo: por debajo del ritmo; rojo: lejos).
- **Retiro objetivo:** cuánto necesita el dueño sacar al mes y si el negocio lo está permitiendo.
- **Punto de equilibrio:**
  ```
  gananciaPromedioPorPieza = gananciaLibrePeriodo ÷ piezasVendidasPeriodo
  piezasEquilibrio         = gastosFijosDelMes ÷ gananciaPromedioPorPieza
  ```
  Mostrar *"Necesitas vender unas N piezas este mes para cubrir tus gastos."* Solo si hay datos suficientes; si no, mostrar "aún sin datos".

---

## 16. Cierre de caja (diario o semanal)

Rutina de 2 minutos: por cada cuenta, la app muestra el **saldo esperado** (según los movimientos); el usuario escribe el **saldo real** (lo que cuenta en efectivo o ve en el banco). Si hay diferencia, se guarda y se pide una nota. Opcionalmente, ofrecer crear un ajuste. Mostrar el historial de cierres y la última fecha de cierre en el Resumen.

---

## 17. Alertas accionables

- Máximo **3 visibles**, ordenadas por urgencia, con un botón de acción cada una.
- Todos los umbrales los configura el dueño.

| Alerta | Texto de ejemplo | Acción |
|---|---|---|
| Cuotas vencidas | Tienes 3 clientes con cuotas vencidas ($X) | Ver y enviar recordatorios |
| Concentración de cartera | Juan Carlos debe $84 (40 % de lo que te deben) | Cobrar |
| Caja insuficiente | Tu caja no alcanza para cubrir las reposiciones | Ver sobres |
| Sobre en negativo | Compraste $X más de lo reservado para tintas | Ver compras |
| Ventas bajas | Vendiste 20 % menos que el período anterior | Ver pedidos |
| Margen bajo | Tu ganancia está por debajo de tu meta | Ver productos |
| Descuentos altos | Este mes regalaste $X en descuentos | Ver detalle |
| Reposición más cara | Reponer lo vendido hoy cuesta $X más que cuando lo vendiste | Actualizar precios |
| Datos estimados | 12 % de tu ganancia es estimada | Corregir |

---

## 18. Estado de pedidos

- **Estado actual de pedidos** (sin filtro de período): Recibidos, En proceso, Listos, **Por entregar hoy**, **Atrasados**. Al tocar cada uno, abre la lista de pedidos filtrada.
- **Pedidos del período:** creados, entregados, cancelados.

---

## 19. Pantalla y experiencia de uso

**Modo simple (por defecto):** cuatro números grandes con semáforo: **Cobrado · Ganancia libre · Por cobrar · Disponible para retirar.** El **modo detallado** está debajo, para quien quiera profundizar.

**Orden sugerido:** tarjetas principales → alertas (máximo 3) → proyección de cobros y pagos → gráfico de flujo → sobres (en cuadrícula, no en carrusel) → radiografía de abonos → cuentas → estado de pedidos → rentabilidad → meta.

**Otros ajustes:**
- Cada tarjeta indica su base: *"Este mes"* o *"Al día de hoy"*.
- Icono **(?)** en cada término con una explicación corta (ver glosario).
- **Indicador de calidad de datos:** *"El 12 % de tu ganancia es estimada porque 2 pagos no tienen costos"*, con acceso para corregirlos.
- **Ocultar montos** con un toque, y **PIN** opcional para abrir el Resumen.
- **Exportar** reporte a PDF y a CSV (para Excel) con la opción de ocultar nombres de clientes. Los temas de impuestos conviene consultarlos con un contador local.
- Rango de fechas personalizado, además de los cinco períodos actuales.
- Sin colores como único indicador (añadir icono o texto) y buen contraste.

---

## 20. Glosario único de nombres (usar siempre estos)

| Concepto | Nombre en pantalla | Ayuda (?) |
|---|---|---|
| Dinero que entró | Cobrado | Abonos y pagos recibidos en el período |
| Pedidos tomados | Vendido | Total de pedidos creados en el período, cobrados o no |
| Lo que te deben | Por cobrar | Saldo pendiente de todos tus clientes al día de hoy |
| Parte para volver a comprar | Reposición | Dinero que debes guardar para volver a comprar materiales, transporte e insumos |
| Material y prendas | Material base | Costo de la pieza o base que se vendió |
| Envíos y pasajes | Transporte | Costo de traer y llevar mercancía |
| Tintas, papel, cintas, DTF | Insumos de impresión | Consumibles para estampar |
| Lo que te quedó antes de gastos | Ganancia libre | Lo que queda después de reponer materiales (antes de gastos del taller) |
| Lo que te quedó de verdad | Ganancia neta | Ganancia libre menos los gastos del taller |
| Tu pago por tu trabajo | Mi pago | Si usas mano de obra en el cotizador |
| Lo que puedes sacar | Disponible para retirar | Cuánto puedes sacar sin dejar el taller sin capital |

---

## 21. Orden de implementación

| Fase | Qué | Resultado |
|---|---|---|
| **1** | Reparto guardado por abono (`DistribucionAbono`) con migración que congela lo actual y prueba de totales idénticos · tipos de abono · etiqueta "estimado" · glosario único y etiquetas de período · estado actual de pedidos vs. período · alertas accionables · sobres en cuadrícula | El Resumen es estable y honesto |
| **2** | Compras y gastos · saldo disponible por sobre · gastos del taller · ganancia libre vs. neta · retiros | Los sobres y la ganancia reflejan la realidad |
| **3** | Cuentas con movimientos automáticos, transferencias y ajustes · cuentas ligadas a los abonos | Se sabe dónde está el dinero |
| **4** | Disponible para retirar · proyección de cobros y pagos · antigüedad de cartera y puntualidad | Las decisiones de cada semana |
| **5** | Rentabilidad por producto, descuentos dados, ticket promedio, mejores clientes · efecto cambiario · meta y punto de equilibrio | Entender qué deja dinero |
| **6** | Cierre de caja · resumen semanal en palabras · temporadas (con al menos un año de datos) · exportar PDF y CSV · PIN y ocultar montos · modo simple y detallado | Pulido y hábitos |

---

## 22. Pruebas obligatorias

**Reparto de abonos:**
- Abono de $20 sobre un pedido de $100 con costos congelados de 40 + 10 + 10 → material 8.00, transporte 2.00, insumos 2.00, ganancia 8.00; la suma da exactamente 20.00.
- Reparto con redondeo: los centavos sobrantes van a la ganancia y la suma siempre cuadra.
- Abono sin pedido → porcentajes base y `origen = ESTIMADO`.
- Abono de tipo `NO_AFECTA` → no suma a ingresos ni a sobres, pero sí al saldo del cliente y a la cuenta.
- Venta con pérdida (costos > abono) → ganancia negativa mostrada, con alerta.
- Devolución → movimiento negativo con el desglose inverso; el original no se borra.
- **Editar el precio de un pedido con abonos previos → los totales de meses anteriores no cambian.**
- Abono en Bs → desglose en USD con la tasa guardada; cambiar la tasa después no altera el pasado.

**Migración:**
- Instalar la versión actual con datos de prueba, anotar los totales de cada período, actualizar encima y verificar que son idénticos.
- Los abonos antiguos con fotografía quedan como `EXACTO` y los demás como `ESTIMADO`.

**Sobres, cuentas y retiros:**
- Una compra de $10 de tintas descuenta del sobre de insumos de impresión; el saldo disponible baja en $10.
- Saldo de cuenta = inicial + movimientos; un ajuste de saldo deja historial.
- Una transferencia entre cuentas con distinta moneda convierte con la tasa indicada.
- Disponible para retirar: ganancia pendiente 120, caja 200, reservas 110 → 90. Con caja 100 y reservas 110 → 0 con mensaje explicativo.

**Cartera y proyección:**
- Cliente con cuota vencida hace 5 días → grupo "1 a 7 días"; con 40 días → "más de 30".
- Puntualidad: una cuota pagada un día después del vencimiento con 0 días de gracia → no cuenta a tiempo; con 2 días de gracia → sí.
- La proyección de 7 días suma solo cuotas y entregas dentro de la ventana, sin contar lo ya vencido.

**Períodos y datos:**
- Semana de lunes a domingo; último día del mes a las 23:59 y primer día del mes a las 00:00; zona horaria del teléfono.
- Sin datos (taller nuevo): todas las tarjetas muestran "aún sin datos", sin $0.00 engañosos ni cierres de la app.
- Rendimiento: con miles de abonos, el Resumen carga con consultas agregadas y sin trabarse.

---

## 23. Prompts para tu agente

**Fase 1:**
> Lee el módulo Resumen (`ResumenViewModel.kt`, `resumenView.kt`, entidades y DAO de pedidos y abonos) y este documento. Implementa solo la Fase 1: crea la tabla `DistribucionAbono` y guarda el desglose de cada abono en el momento de registrarlo (sección 5); agrega tipos de abono (`VENTA_PEDIDO`, `COBRO_MANUAL`, `NO_AFECTA`) y el origen EXACTO/ESTIMADO; escribe la `Migration` sin borrar datos que calcule el desglose de los abonos existentes con la lógica actual; haz que los reportes sumen `DistribucionAbono` en lugar de recalcular. Antes de migrar, anota los totales actuales por período y agrega una prueba que verifique que, después de migrar, son idénticos. Aplica el glosario de la sección 20, etiqueta cada tarjeta con su período, separa "estado actual de pedidos" del período, acorta las alertas y agrega un botón de acción, y muestra los sobres en cuadrícula.

**Fase 2:**
> Implementa la Fase 2 según las secciones 7, 9 y 10: tablas `CategoriaGasto`, `Gasto` y `Retiro` con su migración; pantalla de compras y gastos; saldo disponible por sobre (asignado acumulado menos compras); gastos del taller con recurrentes; y la diferencia entre "ganancia libre" y "ganancia neta". No modifiques el desglose guardado de los abonos.

**Fases 3 y 4:**
> Implementa las secciones 8, 9, 11 y 12: cuentas con saldo calculado a partir de `MovimientoCuenta` (abonos, gastos, retiros, transferencias y ajustes), con migración que conserve el saldo actual como saldo inicial; luego la tarjeta "Disponible para retirar" con el detalle de su cálculo, la proyección de cobros y pagos a 7 y 30 días, la antigüedad de cartera y el porcentaje de cuotas cobradas a tiempo. Incluye pruebas unitarias con los casos de la sección 22.

**Fases 5 y 6:**
> Implementa las secciones 13 a 16 y 19: rentabilidad por producto, descuentos dados, ticket promedio y mejores clientes; efecto cambiario de los bolívares; meta mensual y punto de equilibrio; cierre de caja; modo simple y detallado, ocultar montos, PIN opcional y exportación a PDF y CSV con opción de ocultar nombres de clientes.
