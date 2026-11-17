# Radiografía de Abonos de Zubli — Plan v3 (basado en las respuestas reales del agente)

Este documento **reemplaza** a `MEJORAS_RADIOGRAFIA_ZUBLI_V2.md` y a la versión anterior. La v2 asumía que la pantalla mostraba lo guardado en `distribucion_abonos`; las respuestas del agente (sección 15 de su documentación) muestran que **la pantalla recalcula** los repartos, y revelaron otros detalles que cambian el diseño.

**Objetivo:** que la radiografía muestre el dinero **exacto de cada material y cada costo** de los pedidos, con nombres que salen de los datos, **sin modificar el cotizador** y **sin cambiar los totales que el Resumen muestra hoy**.

> **Reglas para el agente**
> 1. Lee el código real antes de editar. Si algo no coincide con este documento, detente y explícalo.
> 2. **No modifiques el cotizador:** ni `calcularCostosPlantilla`, ni `PlantillaCotizacion`, ni `InsumoCotizacion`, ni sus pantallas. Solo puedes **leerlos**. Las funciones nuevas van en archivos nuevos.
> 3. En las fases 1 y 2, **los totales que muestra el Resumen no deben cambiar ni un centavo.** Los sub-ítems son una *apertura* de los montos que el `ResumenViewModel` ya calcula.
> 4. Prohibido borrar datos al migrar y prohibido `fallbackToDestructiveMigration`. Toda tabla o columna nueva lleva su `Migration` y el mismo valor por defecto en la entidad de Room. Reutiliza el estilo de `Migration26To27Test.kt` para la prueba de migración.
> 5. No renombres ni refactorices nada existente.
> 6. Los montos nuevos se manejan en **centavos enteros (`Long`)**, sin `Double`, en el reparto.
> 7. Una tarea por vez, con su commit. En migraciones y en el reparto, **muestra primero el plan** (archivos y cambios) y espera confirmación.
> 8. Al terminar cada tarea, ejecuta `./gradlew testDebugUnitTest` (y `connectedDebugAndroidTest` si hay migraciones) y **pega el resultado**.
> 9. **Cualquier cambio que altere números del Resumen** (por ejemplo, corregir un vínculo abono–pedido) va en su propia tarea, con un informe de antes y después, y requiere aprobación del dueño.

---

## 1. Cómo está construido (confirmado por el agente)

| Pieza | Lo que hace |
|---|---|
| `calcularCostosPlantilla` (`CotizacionUtils.kt`) | Calcula costos **unitarios** por pieza. Pieza base = pieza tradicional + insumos dinámicos de categoría `MATERIA_PRIMA`. Pasaje = transporte tradicional + dinámicos `SERVICIO`. Insumos = empaque + papel + DTF + diseño + extra + dinámicos `INSUMO_EXTRA` + **operatividad** (porcentaje sobre el subtotal de materiales). |
| Redondeo | Cada costo **unitario** se redondea a 2 decimales (`redondear2()`) y luego se multiplica por la cantidad. |
| Mano de obra | Se calcula (`minutosPorPieza`, `tarifaPorHoraUsd`) pero **no entra** en los tres bloques. |
| Plantilla | Rubros tradicionales en columnas fijas; extras en JSON (`costosAdicionalesJson`, lista de `InsumoCotizacion`); escalas en JSON; variantes en `plantilla_atributos` y `plantilla_atributo_opciones`. |
| Pedido | Un solo registro. Los artículos (`listaArticulos`, con fotos y opciones) van como JSON dentro de `notas`, entre `[ARTICULOS_JSON]` y `[/ARTICULOS_JSON]`. Los costos del pedido son **totales** (unitario × cantidad). Con varios artículos, `nombrePiezaBase` = "Múltiples Productos". |
| `DistribucionAbono` | `abonoId`, `pedidoId`, `tipoAbono`, `montoUsd`, `material`, `transporte`, `insumos`, `miPago`, `ganancia` (`Double`), `origen`, `fechaMillis`. |
| Primer abono del pedido | Se inserta la distribución dentro de `guardarPedidoCompleto` (`PedidoDao.kt`). |
| Abonos posteriores | El `ResumenViewModel` **deduce** el pedido: busca el cargo activo del cliente y luego el pedido con `totalUsd == cargoOriginal.montoInicial`. |
| Pantalla | Solo lee lo **guardado** para abonos `NO_AFECTA`. Para los demás **recalcula en memoria** con la fotografía **actual** del pedido (o con porcentajes si no hay pedido) y, si no existe fila guardada, la inserta en segundo plano. |
| Consumidores de los totales | Sobres, `AsesorZubliUtils`, `PdfReporteUtils`, `ExportarCsvUtils`. |

---

## 2. Hallazgos (de más a menos relevantes)

**H1. Los sub-ítems de "Costos Adicionales" son porcentajes fijos inventados** (papel y tintas 40 %, DTF 30 %, diseño 15 %, operatividad 15 %), no costos reales. Es la causa de la rigidez y de la inexactitud.

**H2. La información real sí existe, pero no se guarda con el pedido.** Los costos de cada material salen de la plantilla de forma determinista (empaque, papel, DTF, diseño, extra, cada insumo dinámico y la operatividad). Solo falta **guardar las líneas** al crear el pedido.

**H3. El vínculo entre un abono posterior y su pedido es una heurística frágil.**
- Compara `totalUsd == montoInicial` con **igualdad exacta de `Double`**: una diferencia mínima hace que no coincida y el abono pase a **`ESTIMADO`** aunque exista pedido.
- Usa `find`, que toma el **primer** cargo del cliente: si tiene dos pedidos o cargos con el mismo total, puede asignarse al pedido equivocado.
- El abono posterior no guarda a qué pedido pertenece.

Esto afecta la **exactitud del dinero** más que cualquier otra cosa de la pantalla. Corregirlo cambia números, por eso va aparte (Fase 3).

**H4. La pantalla recalcula en lugar de usar lo guardado.** Si un pedido cambia después, los reportes de meses anteriores cambian. Las filas de `distribucion_abonos` son casi un registro sin uso (salvo `NO_AFECTA`).

**H5. Posibles filas duplicadas** (índice no único, sin `IGNORE`, sin `Mutex`). Como la pantalla no suma las filas guardadas para las ventas, el impacto en los totales **probablemente es menor** de lo que se temía. Hay que verificar si algún código suma `obtenerTodas()` directamente (T0.3 y pregunta 6 de la sección 14).

**H6. La mano de obra no se reparte.** Si una plantilla usa mano de obra, esa parte del precio cae dentro de "Ganancia Neta Libre" y `miPago` queda en 0. No se toca en este plan (cambiaría los totales); queda como decisión opcional (Fase 5).

**H7. Los servicios dinámicos (`SERVICIO`) cuentan en "Pasajes & Transporte".** En la radiografía nueva aparecerán allí con su propio nombre. Si el nombre del grupo les parece engañoso, se puede renombrar a "Pasajes, transporte y servicios".

**H8. Textos fijos** en pantalla y en el modal de configuración ("Material Base (Sublimación/Rígidos)", ejemplos de camisa y de $12).

---

## 3. La solución en una idea

1. **Al guardar el pedido, guardar sus líneas reales de costo** (nombre, sobre y monto) generadas desde la plantilla, **sin tocar el cotizador**.
2. **En la radiografía, abrir cada sobre en esas líneas, al vuelo.** Los montos de material, transporte e insumos que el ViewModel **ya calcula** para cada abono se reparten entre las líneas del pedido, en centavos, de modo que la suma de los sub-ítems es exactamente el monto del sobre. Los totales no cambian por construcción.
3. **Sin líneas o sin pedido:** se muestra **"Sin detalle"** o **"Estimado"**, nunca porcentajes inventados.
4. Lo demás (congelar el historial, corregir vínculos, mano de obra) son **fases posteriores y separadas**.

Esto es más pequeño que la v2: no hace falta una tabla de líneas por abono ni cambiar `DistribucionAbono`.

---

## 4. Cómo se verá (ejemplo con datos inventados que suman $135.36)

```
Monto recibido: $135.36 (13 pagos)

▸ Material base                        $44.41   32.8 %
     Franela blanca                     $38.60   28.5 %
     Taza 11 oz                          $5.81    4.3 %
▸ Pasajes & Transporte                 $12.19    9.0 %
     Transporte                         $12.19    9.0 %
▾ Costos adicionales de producción     $15.83   11.7 %
     DTF                                 $6.90    5.1 %
     Papel de sublimación                $1.35    1.0 %
     Empaque (bolsas)                    $1.18    0.9 %
     Diseño                              $2.00    1.5 %
     Cinta térmica                       $0.60    0.4 %
     Operatividad (luz, merma y desgaste) $3.80   2.8 %
▸ Ganancia neta libre                  $62.93   46.5 %
```

- Los tres grupos se expanden, y sus líneas salen de los datos. Un insumo nuevo agregado a una plantilla aparece solo, en el grupo que le corresponde por su categoría.
- La operatividad es **una sola línea**: el cotizador la maneja con un solo deslizador y no se modifica.
- Los pedidos sin detalle muestran una fila "Sin detalle" dentro de su grupo, con una etiqueta que lo explica.

---

## 5. Datos

### 5.1 Lo que NO se toca
`DistribucionAbono`, `Pedido`, `PlantillaCotizacion`, `InsumoCotizacion`, `Deuda`.

### 5.2 Tabla nueva `pedido_costo_lineas`

| Campo | Descripción |
|---|---|
| `id` | Clave primaria |
| `pedidoId` | Clave foránea a `Pedido` con `ON DELETE CASCADE` |
| `indiceArticulo` | Posición del artículo dentro de la lista del pedido |
| `sobre` | `MATERIAL`, `TRANSPORTE` o `INSUMOS` |
| `nombre` | **Texto copiado** al guardar (así renombrar la plantilla después no altera el pasado) |
| `costoCentavos` | Costo de esa línea **para todo el pedido** |
| `tipoDetalle` | `EXACTO`, `RECUPERADO` o `SIN_DETALLE` |

Índice en `pedidoId`. Cada valor por defecto declarado igual en la entidad y en el SQL. La `Migration` va a la versión siguiente a la actual de la base de datos.

### 5.3 Campo nuevo en `ArticuloPedido` (sin migración)
Agregar `lineasCostoUnitarias: List<LineaCostoUnitaria>` con valor por defecto vacío. Como los artículos se serializan como JSON dentro de `notas`, los pedidos antiguos se siguen leyendo sin problema (el campo faltante queda vacío).

---

## 6. Generar las líneas sin tocar el cotizador

### 6.1 Función nueva (archivo nuevo, por ejemplo `LineasCostoPlantilla.kt`)

`desglosarLineasPlantilla(plantilla, tasaBcv, cantidadPedido): List<LineaCostoUnitaria>`

Debe **replicar exactamente la aritmética** de `calcularCostosPlantilla` (mismos parámetros, misma conversión de moneda `aUsd`, mismos divisores `parseCant`), pero devolviendo una línea por concepto, en **dólares sin redondear**:

| Concepto | Sobre | Nombre de la línea |
|---|---|---|
| Pieza tradicional | `MATERIAL` | El nombre de la plantilla o de la pieza |
| Cada insumo dinámico `MATERIA_PRIMA` | `MATERIAL` | Su propio nombre |
| Transporte tradicional | `TRANSPORTE` | "Transporte" |
| Cada insumo dinámico `SERVICIO` | `TRANSPORTE` | Su propio nombre |
| Empaque | `INSUMOS` | "Empaque" |
| Papel | `INSUMOS` | "Papel de sublimación" |
| DTF | `INSUMOS` | "DTF / impresión" |
| Diseño | `INSUMOS` | "Diseño" |
| Extra tradicional | `INSUMOS` | El nombre con que aparece en la plantilla |
| Cada insumo dinámico `INSUMO_EXTRA` | `INSUMOS` | Su propio nombre |
| Operatividad | `INSUMOS` | "Operatividad (luz, merma y desgaste)" = subtotal de materiales × porcentaje |

La **mano de obra no genera línea** (no forma parte de los tres bloques).

### 6.2 Dónde y cuándo llamarla
En el mismo punto donde cada artículo recibe `costoPiezaBaseUnitario`, `costoPasajeUnitario` y `costoInsumosUnitario` a partir de `calcularCostosPlantilla`, y **con los mismos argumentos** (tasa, cantidad, escalas). Guardar el resultado en `ArticuloPedido.lineasCostoUnitarias`. Como los insumos dinámicos dependen de `cantidadPedido`, si el artículo recalcula sus costos al cambiar la cantidad, también debe recalcular las líneas.

### 6.3 Compuerta por artículo
Para cada sobre, la suma de las líneas unitarias debe coincidir con el costo unitario del artículo (que está redondeado a 2 decimales) con una diferencia máxima de **0.006**. Si no coincide, esas líneas **no se usan** para ese artículo (se tratará como "Sin detalle") y se escribe el motivo en Logcat.

### 6.4 Al crear el pedido (`crearNuevoPedido` / `guardarPedidoCompleto`)
1. Convertir las líneas de cada artículo a pesos: `costo unitario × cantidad total del artículo`, en micro-dólares (`Long`).
2. Para cada sobre, repartir el **total del pedido que ya se calcula hoy** (`costoPiezaBaseTotal`, `costoPasajeTotal`, `costoInsumosTotal`, convertidos a centavos) entre todas las líneas de todos los artículos, con `repartirProporcional` (sección 7). La suma de las líneas es **exactamente** el total del pedido.
3. Los artículos sin líneas válidas aportan una línea **"Sin detalle"** con su parte del total.
4. Insertar las líneas en `pedido_costo_lineas` **dentro de la misma `@Transaction`** de `guardarPedidoCompleto`.
5. Los productos libres (sin plantilla) no generan líneas.

---

## 7. Cálculo exacto (en centavos)

```kotlin
// Reparte totalCentavos entre las posiciones según sus pesos, sin perder ni inventar centavos
fun repartirProporcional(totalCentavos: Long, pesos: List<Long>): List<Long> {
    require(totalCentavos >= 0) { "El total no puede ser negativo" }
    require(pesos.all { it >= 0 }) { "Los pesos no pueden ser negativos" }
    if (pesos.isEmpty()) return emptyList()
    val sumaPesos = pesos.sum()
    if (sumaPesos == 0L) return List(pesos.size) { 0L }

    val base = pesos.map { (it * totalCentavos) / sumaPesos }.toMutableList()
    var faltan = totalCentavos - base.sum()

    val porResiduo = pesos
        .mapIndexed { i, p -> i to ((p * totalCentavos) % sumaPesos) }
        .sortedByDescending { it.second }
    var k = 0
    while (faltan > 0) {
        base[porResiduo[k % porResiduo.size].first] += 1
        faltan--
        k++
    }
    return base
}
```

**Apertura de un sobre de un abono** (con los montos que el ViewModel ya calcula):

```kotlin
data class SubItemRadiografia(
    val sobre: String,
    val nombre: String,
    val centavos: Long,
    val sinDetalle: Boolean = false
)

fun abrirSobre(
    sobre: String,
    sobreUsd: Double,                     // el monto que ya calcula el ViewModel
    lineasDelPedido: List<LineaPedido>    // solo las de ese sobre
): List<SubItemRadiografia> {
    val total = Math.round(sobreUsd * 100)
    if (total <= 0L) return emptyList()
    val pesos = lineasDelPedido.map { it.costoCentavos }
    if (pesos.sum() <= 0L) {
        return listOf(SubItemRadiografia(sobre, "Sin detalle", total, sinDetalle = true))
    }
    return lineasDelPedido.zip(repartirProporcional(total, pesos)) { l, c ->
        SubItemRadiografia(sobre, l.nombre, c)
    }.filter { it.centavos > 0 }
}
```

**Consolidado:** sumar los sub-ítems por `(sobre, nombre)` de todos los abonos del período. Después, para que los totales visibles **no cambien**, repartir el **total del sobre que se muestra hoy** (convertido a centavos) entre esos sub-ítems agregados, con `repartirProporcional`. Así la suma de los sub-ítems es siempre exactamente el sobre mostrado.

**Reglas:**
- Abono `ESTIMADO` (sin pedido): no hay sub-ítems; se muestra el sobre completo con la etiqueta "Estimado". **Nunca** se repiten los porcentajes 40/30/15/15.
- Abono `NO_AFECTA`: se mantiene como hoy.
- Pedido sin líneas guardadas (anteriores a este cambio): "Sin detalle" dentro de cada sobre.
- La ganancia no se abre.
- Los porcentajes en pantalla se redondean con el método del mayor resto para que sumen 100.0 %.

---

## 8. Recuperar el detalle de pedidos existentes (opcional, con compuerta)

Los pedidos anteriores no tienen líneas guardadas. Solo se puede recuperar el detalle si **cada artículo** guarda a qué plantilla corresponde (pregunta 1 de la sección 14) y esa plantilla todavía existe:

1. Generar las líneas con `desglosarLineasPlantilla` usando la plantilla **actual**.
2. **Solo si** pasan la compuerta de 6.3 (suma por sobre ≈ costo unitario guardado del artículo), guardar `tipoDetalle = RECUPERADO`.
3. Si no pasan (la plantilla cambió de precios o ya no existe), guardar "Sin detalle".
4. Informar: *"Se recuperó el detalle de X de Y pedidos"*.

Si los artículos no guardan la plantilla, esta parte no se hace y los pedidos anteriores quedan como "Sin detalle". No se infiere nada por el nombre.

---

## 9. Cambios en el `ResumenViewModel` y la pantalla

**ViewModel:**
1. Obtener las líneas con una consulta agregada por los `pedidoId` de los abonos del período, en lugar de cargar todo.
2. Para cada abono, construir sus sub-ítems con `abrirSobre` usando los montos que ya calcula (`mat`, `trans`, `ins`).
3. Reemplazar el reparto fijo 40/30/15/15 del consolidado por la agregación real de la sección 7.
4. Ampliar la estructura actual (`DesglosePagoIndividual` y sus sub-ítems) en lugar de rehacerla.
5. Interruptor en el código (`USAR_LINEAS_EN_RADIOGRAFIA`) para volver al comportamiento anterior mientras se valida.
6. Opcional: "Ver todos los abonos" más allá de los 15 recientes.

**Pantalla (`resumenView.kt`):**
- Los **tres grupos** usan `ItemDesgloseAcordeon` y muestran sus líneas con monto y porcentaje.
- Mover los textos fijos a `strings.xml`. Cambiar "Material Base (Sublimación/Rígidos)" por "Material base". Los nombres de las líneas **nunca** van en el código.
- Etiquetas "Estimado" y "Sin detalle", con un icono (?) que explique su significado.
- Modal ⚙️ "Configurar costos base": quitar ejemplos concretos ("camisa $3.90", "$12") y aclarar que **solo se usa cuando un pago no tiene pedido**.
- No usar solo el color para distinguir información; descripciones habladas por grupo.
- Mantener los colores actuales de los sobres.

---

## 10. Vínculo abono–pedido: exactitud (Fase 3, aparte)

**Diagnóstico primero (T0.4, sin cambiar nada):** listar cada abono con el pedido que se le asigna hoy y señalar:
- abonos de clientes con **más de un pedido o cargo activo**;
- abonos cuyo `totalUsd` y `montoInicial` difieren por **menos de un centavo** pero no son iguales (la comparación `==` falla);
- abonos que hoy salen como `ESTIMADO` aunque el cliente **tiene** pedidos;
- pedidos con el mismo total para un mismo cliente.

**Correcciones (solo después de la línea base y con aprobación):**
1. Comparar en **centavos** (`abs(a − b) < 0.005`) en lugar de `==`.
2. **Guardar el `pedidoId` en el abono al registrarlo.** Si el cliente tiene un solo pedido pendiente, se asigna solo; si tiene varios, preguntar *"¿A qué pedido corresponde este abono?"*, con el más antiguo con saldo como opción por defecto.
3. Entregar un **informe de cambios** (abonos afectados, valores antes y después). Estos cambios modifican totales, por eso necesitan aprobación expresa del dueño.

---

## 11. Persistencia y estabilidad del historial (Fase 4, opcional)

- **Duplicados:** si T0.3 los encuentra, migración que conserva la fila más antigua de cada abono, índice **único** sobre `abonoId` e inserción con `OnConflictStrategy.IGNORE`, más un `Mutex` en el ViewModel. Avisar al dueño antes: si algún código suma las filas directamente, los números cambiarán (para bien).
- **Congelar el historial:** hoy la pantalla recalcula con la fotografía actual. Para que los meses pasados no cambien nunca, hay que leer lo guardado. Se hace en **modo sombra**: se compara el valor recalculado contra el guardado de cada abono en `debug`; si hay diferencias, se revisan con el dueño antes de cambiar la fuente. Recién entonces se guardan también las líneas por abono (`DistribucionAbonoLinea`) y se crea la distribución **al registrar el abono**, en la misma transacción.

---

## 12. Mano de obra "Mi pago" (Fase 5, opcional, decisión del dueño)

El cotizador calcula la mano de obra pero no se reparte: hoy forma parte de "Ganancia Neta Libre" y `miPago` es 0. Cambiarlo haría bajar la ganancia mostrada y subir "Mi pago" en los abonos de plantillas con mano de obra. **No se hace sin aprobación**, y se haría con su propio informe de antes y después. Si ninguna plantilla usa mano de obra (`minutosPorPieza` en 0), no hay nada que hacer.

---

## 13. Fases, tareas y qué cambia

| Fase | Tareas | Qué cambia | Qué NO debe cambiar | Riesgo |
|---|---|---|---|---|
| **0** | T0.1 (hecha), T0.2 línea base, T0.3 duplicados, T0.4 diagnóstico de vínculos, T0.5 segunda ronda de preguntas | Nada en la app | Todo | Ninguno |
| **1** | T1.1 `repartirProporcional` y pruebas · T1.2 `desglosarLineasPlantilla` y prueba de equivalencia con `calcularCostosPlantilla` · T1.3 tabla `pedido_costo_lineas` y `Migration` con su prueba · T1.4 `ArticuloPedido.lineasCostoUnitarias` y su llenado · T1.5 guardar líneas en `guardarPedidoCompleto` con compuerta | Se guardan líneas al crear pedidos nuevos | Pantallas, totales, saldos, cotizador | Bajo-medio |
| **2** | T2.1 `abrirSobre` y consolidación con pruebas · T2.2 ViewModel con líneas reales detrás del interruptor · T2.3 pantalla y `strings.xml` · T2.4 etiquetas "Estimado" y "Sin detalle" · T2.5 recuperación de pedidos existentes (si aplica) | La radiografía muestra sub-ítems reales | **Los totales** (idénticos), saldos, cuentas, cotizador | Medio |
| **3** | Vínculo abono–pedido (sección 10) | **Cambia números** de abonos mal vinculados | Resto de la app | Medio-alto (con aprobación) |
| **4** | Duplicados, historial estable, modo sombra (sección 11) | Lectura de lo guardado | Totales actuales hasta aprobar | Alto |
| **5** | Mano de obra "Mi pago", detalle en PDF y CSV | Totales de plantillas con mano de obra | — | Alto (con aprobación) |

**Verificar en cada fase:** saldos de clientes, cuotas, cuentas y precios del cotizador iguales que antes; que el respaldo incluya la tabla nueva; que una copia de seguridad de la versión anterior se restaure bien.

---

## 14. Preguntas pendientes para el agente (T0.5, sin cambiar nada)

1. Pega la clase `ArticuloPedido` completa. ¿Guarda el id o el nombre de la plantilla de la que salió el artículo? ¿Cómo se calcula `obtenerCantidadTotal()`?
2. ¿En qué lugar de `pedidoView.kt` o `pedidoViewModel.kt` se asignan `costoPiezaBaseUnitario`, `costoPasajeUnitario` y `costoInsumosUnitario` a partir de `calcularCostosPlantilla`? Pega ese código. ¿Se recalculan si cambian la cantidad o las variantes?
3. Pega las clases `InsumoCotizacion` (con `calcularCostoUnitarioUsd`) y `PlantillaCotizacion`. ¿Qué nombre muestra la interfaz para `costoExtra`?
4. Pega la firma y el cuerpo de `guardarPedidoCompleto` y la entidad `Pedido`.
5. En `ResumenViewModel`, pega el código que calcula `mat`, `trans` e `ins` de cada abono (todo lo anterior a la auto-persistencia). ¿Hay algún lugar que **sume** filas de `obtenerTodas()` directamente, sin pasar por el recálculo?
6. ¿Se puede **editar** un pedido después de creado? Si sí, ¿se vuelve a tomar la fotografía de costos?
7. ¿Alguna plantilla o artículo usa mano de obra (`minutosPorPieza` mayor que 0)?
8. ¿Cuál es el número de versión actual de la base de datos y está activado `exportSchema`?
9. ¿Un abono posterior guarda alguna referencia al pedido? ¿La `Deuda` de un cargo tiene `pedidoId`?

---

## 15. Pruebas obligatorias

**Equivalencia con el cotizador (la más importante):**
- Para varias plantillas de prueba (solo rubros tradicionales; con insumos dinámicos de las tres categorías; con montos en `VES`; con porcentaje operativo 0 y distinto de 0; con cantidades 1, 6 y 12), la suma de las líneas por sobre coincide con los costos unitarios de `calcularCostosPlantilla` (antes de `redondear2()`, o con diferencia máxima de 0.006 después).

**Reparto:**
- `repartirProporcional`: la suma es exactamente el total en muchos casos aleatorios; pesos con ceros; lista de un solo elemento; total 0; sin pesos (todos 0) → ceros sin dividir.
- Las líneas guardadas de un pedido suman **exactamente** sus totales de material, transporte e insumos (en centavos).
- Los sub-ítems de cada sobre de un abono suman exactamente el monto de ese sobre; el consolidado suma exactamente el sobre mostrado.
- Pedido con **varios artículos** (con y sin plantilla) y con variantes: el reparto es correcto y no inventa detalle.
- Artículo que no pasa la compuerta → "Sin detalle", no cifras dudosas.

**Datos y migración:**
- **Los totales por período del Resumen son idénticos a los del archivo de referencia de la Fase 0, sin tolerancia.**
- Pedidos antiguos (sin líneas) siguen mostrándose bien, con "Sin detalle".
- Un pedido nuevo con un insumo nuevo en la plantilla muestra ese insumo con su nombre.
- Renombrar una plantilla no cambia pedidos ya guardados.
- La `Migration` conserva todos los datos (extender la prueba de migración existente).
- Restaurar un respaldo de la versión anterior migra sin perder nada; un respaldo nuevo incluye las líneas.
- Saldos de clientes, cuotas y cuentas iguales antes y después.

**Pantalla:**
- No queda en el código ninguna constante 0.40, 0.30 o 0.15 usada para repartir costos adicionales (búsqueda de texto).
- Los porcentajes visibles suman 100.0 %.
- Sin abonos en el período: mensaje de vacío, sin cierres.
- Fuente grande del sistema: nada se recorta.

---

## 16. Prompts

**T0.2 (línea base) — corrección al plan propuesto por el agente:**
> Aprobado el plan de T0.2 con estos ajustes: (1) la herramienta de depuración (solo `debug`) debe exportar, además de las filas de `distribucion_abonos` y los totales de los períodos que existan en `PeriodoFiltro`, **los valores que el `ResumenViewModel` calcula para cada abono** (cliente, fecha, monto en USD, pedido deducido, material, transporte, insumos, ganancia y origen), porque la pantalla recalcula y esos son los números que deben permanecer idénticos. (2) No uses una prueba unitaria de JVM que necesite `Context` o Room: si quieres una prueba, que sea instrumentada (`androidTest`); si no, basta con la herramienta de depuración. (3) No modifiques ninguna lógica de producción en esta tarea. Muestra el plan final y espera mi confirmación.

**T0.3:** el plan propuesto por el agente (consulta de duplicados en el DAO y reporte en Logcat) está bien. Aplica con una sola condición: que el resultado se informe sin corregir nada.

**T0.4 (diagnóstico de vínculos):**
> Sin cambiar ningún archivo de producción, agrega a la herramienta de depuración un reporte que liste, para cada abono: cliente, fecha, monto, el pedido que el `ResumenViewModel` le asigna hoy, el `origen` resultante y estas marcas: (a) cliente con más de un pedido o cargo activo, (b) abono de un cliente con pedidos que quedó como `ESTIMADO`, (c) `totalUsd` y `montoInicial` que difieren por menos de 0.005 pero no son iguales, (d) varios pedidos del mismo cliente con el mismo total. Informa los conteos.

**T0.5:** pasarle las 9 preguntas de la sección 14.

**Plantilla de prompt para cada tarea:**
> Contexto: proyecto Android en Kotlin con Room, MVVM y Jetpack Compose. Sigue `MEJORAS_RADIOGRAFIA_ZUBLI_V3.md` y las reglas del recuadro inicial.
> Tarea: **[código y descripción]**.
> Archivos que puedes crear o modificar: **[lista]**. Archivos que NO puedes tocar: **todo el cotizador (`CotizacionUtils.kt`, `PlantillaCotizacion`, `InsumoCotizacion` y sus pantallas)** y **[otros]**. No renombres ni refactorices nada existente.
> Criterio de aceptación: **[lo que debe cumplirse]**.
> Pruebas a ejecutar y pegar: **[comandos]**.
> Antes de escribir código, muestra el plan (archivos y cambios) y espera mi confirmación. Al terminar, lista los archivos modificados y cualquier suposición.

**Revisión final de las fases 1 y 2:**
> Revisa lo implementado contra `MEJORAS_RADIOGRAFIA_ZUBLI_V3.md`. Confirma que el cotizador no tiene ningún cambio, que los totales por período son idénticos al archivo de referencia, que las líneas de cada pedido suman exactamente sus tres totales, que los sub-ítems de cada sobre suman exactamente el sobre mostrado, que no queda ningún porcentaje fijo 40/30/15/15 ni nombre de material escrito en pantallas, que el reparto no usa `Double`, y que la prueba de equivalencia con `calcularCostosPlantilla` existe y pasa. Lista cualquier diferencia.

---

## 17. Cómo trabajar con un agente de menor capacidad

1. **Una tarea por conversación y un commit por tarea.** Pega solo el contexto necesario.
2. **Tú ejecutas las pruebas** y revisas en GitHub Desktop qué archivos tocó el agente antes de hacer commit. Si tocó el cotizador, deshaz esos cambios.
3. No aceptes una migración sin su prueba de migración ejecutada.
4. En las piezas delicadas (`repartirProporcional`, `desglosarLineasPlantilla` con su prueba de equivalencia, la compuerta y la migración), usa el modelo más potente que tengas o pide una segunda revisión del código.
5. Antes de cada fase, respaldo de tus datos desde la app y etiqueta en Git.
