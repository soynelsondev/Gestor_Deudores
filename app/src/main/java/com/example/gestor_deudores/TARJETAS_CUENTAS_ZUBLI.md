# Tarjetas de Bancos y Billeteras de Zubli

Especificación para tu equipo o tu agente en Android Studio: qué información debe mostrar cada tarjeta de cuenta, cómo se calcula, qué muestra el historial de movimientos y qué colores usar. Es una guía de **contenido y reglas**, no un diseño cerrado: el equipo decide los detalles visuales dentro de la paleta.

> **Reglas para el agente**
> 1. Lee las entidades, el DAO, el ViewModel y la pantalla actual de cuentas antes de editar, y adapta los nombres de este documento a los reales.
> 2. No borres datos: toda tabla o columna nueva lleva su `Migration` y su valor por defecto igual en la entidad de Room. El saldo actual de cada cuenta pasa a ser su **saldo inicial**; no se inventan movimientos pasados.
> 3. Redondea a 2 decimales al guardar y sumar. Nunca sumes monedas distintas sin convertir.
> 4. Los movimientos **no se editan ni se borran**: una corrección es un movimiento nuevo (ajuste o reverso).
> 5. Prueba instalando encima de la versión actual con datos de prueba.

---

## 1. Qué debe responder cada tarjeta

1. **¿Qué cuenta es?** Nombre completo, tipo (banco, efectivo, pago móvil, billetera virtual) y moneda.
2. **¿Para qué es ese dinero?** Etiqueta de uso (cobros, reposición, ahorro, gastos del taller, personal).
3. **¿Cuánto hay?** Saldo actual calculado.
4. **¿Qué ha pasado?** Cuánto entró y cuánto salió en el período, y cuál fue el último movimiento.
5. **¿Cuánto vale en dólares?** Si la cuenta es en Bs, su equivalente a la tasa de hoy.

---

## 2. Tipos de cuenta

| Tipo | Moneda habitual | Ejemplos |
|---|---|---|
| Banco nacional | Bs | Cuenta corriente o de ahorro |
| Pago móvil | Bs | Cuenta asociada a pago móvil |
| Efectivo | USD o Bs | Caja del taller (una por moneda) |
| Billetera virtual | USD | Binance, Zinli, PayPal u otras |
| Banco en dólares | USD | Cuenta en dólares o Zelle |
| Otra | Cualquiera | Lo que el usuario necesite |

El tipo define el ícono y el color de la etiqueta (sección 8). La lista de tipos es editable.

**Datos opcionales de la cuenta:**
- Alias o titular (por ejemplo, "Cuenta de Nelson").
- Institución o plataforma.
- **Dato para recibir pagos** (teléfono, correo o alias), con un botón **"Copiar datos de pago"** para enviarlo por WhatsApp a un cliente.
- **No guardes números de cuenta completos, claves ni contraseñas.** Si se muestra un número, que sean solo los últimos 4 dígitos.

---

## 3. La tarjeta (vista de lista)

### 3.1 Contenido

| Elemento | Detalle |
|---|---|
| Ícono y color por tipo | Banco, efectivo, pago móvil o billetera virtual |
| **Nombre completo** | Hasta 2 líneas; al tocar, se ve entero. Nunca recortarlo a una letra |
| Subtítulo | `Tipo · Moneda`, por ejemplo "Billetera virtual · USD" |
| Etiqueta de uso | Chip: Cobros, Reposición, Ahorro, Gastos del taller, Personal (opcional) |
| **Saldo actual** | Grande, en la moneda de la cuenta |
| Equivalente en USD | Solo si la cuenta es en Bs: `≈ $14.28 (tasa 875.65)` |
| **Entró / Salió** | Del período seleccionado en el Resumen: `↓ Entró $20.00` y `↑ Salió $5.23` |
| Último movimiento | `Hoy · Abono de María Pérez · +$20.00` |
| Meta (opcional) | Barra de progreso con el porcentaje: `30 % de $50.00` |
| Acciones | **Ingreso · Gasto · Mover** y menú ⋮ (editar, archivar) |

### 3.2 Estructura de ejemplo (referencia de contenido, no de diseño)

```
┌─────────────────────────────────────────┐
│ [ícono]  Binance · Fondo de reposición  │
│          Billetera virtual · USD   [Reposición]
│                                         │
│  $14.77                                 │
│  ↓ Entró $20.00     ↑ Salió $5.23       │
│  ▓▓▓░░░░░░░  30 % de tu meta de $50.00  │
│                                         │
│  Hoy · Compra de tintas · −$5.23        │
│  [ Ingreso ]  [ Gasto ]  [ Mover ]   ⋮  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ [ícono]  Pago móvil · Banco principal   │
│          Pago móvil · Bs           [Cobros]
│                                         │
│  Bs 12.500,00        ≈ $14.28           │
│  ↓ Entró Bs 15.000,00  ↑ Salió Bs 2.500,00
│                                         │
│  Ayer · Abono de Carlos Ruiz · +Bs 15.000,00
│  [ Ingreso ]  [ Gasto ]  [ Mover ]   ⋮  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ [ícono]  Caja del taller (efectivo)     │
│          Efectivo · USD            [Cobros]
│                                         │
│  $42.00                                 │
│  ↓ Entró $60.00     ↑ Salió $18.00      │
│                                         │
│  Lun · Retiro del dueño · −$10.00       │
│  [ Ingreso ]  [ Gasto ]  [ Mover ]   ⋮  │
└─────────────────────────────────────────┘
```

(Los nombres y montos son ejemplos inventados; el saldo de cada cuenta cuadra con sus entradas y salidas.)

### 3.3 Cabecera de la sección

Una tarjeta resumen arriba de la lista:
- **Total en cuentas:** `$X` (Bs convertidos a la tasa de hoy).
- Desglose al tocar: por moneda (USD / Bs) y por uso (cobros, reposición, ahorro...).
- Fecha y valor de la tasa usada.
- Botón **"+ Nueva cuenta"**.

---

## 4. Pantalla de detalle (al tocar la tarjeta)

**Encabezado:** nombre completo, tipo, moneda, uso, saldo actual y su equivalente en USD si aplica.

**Conciliación del período** (esta línea es la que demuestra que el dinero "suma bien"):

```
Saldo al inicio del período   $ 0.00
+ Entradas                    $ 20.00
− Salidas                     $  5.23
= Saldo al final del período  $ 14.77
```

**Historial de movimientos:**

| Columna | Contenido |
|---|---|
| Fecha y hora | Del movimiento |
| Concepto | Ver tabla de conceptos abajo |
| Monto | Con signo y color: `+` entradas, `−` salidas |
| **Saldo después** | El saldo de la cuenta tras ese movimiento (saldo corrido) |
| Referencia | Número de referencia del pago, si existe |
| Comprobante | Foto adjunta, si existe |

**Conceptos que se muestran (se arman solos):**

| Origen | Texto del concepto |
|---|---|
| Abono de un pedido | `Abono de {cliente} · Pedido #{n}` |
| Cobro de una cuenta manual | `Cobro de {cliente}` |
| Compra o gasto | `Compra de {descripcion} · {proveedor}` |
| Retiro del dueño | `Retiro del dueño` |
| Transferencia | `Transferencia desde/hacia {otra cuenta}` |
| Ajuste de saldo | `Ajuste: {motivo}` |
| Saldo inicial | `Saldo inicial` |

**Filtros y herramientas:**
- Chips: Todos · Entradas · Salidas · Transferencias · Ajustes.
- Selector de período y búsqueda por cliente, referencia o concepto.
- **Exportar extracto** (PDF o CSV) con el saldo inicial, los movimientos y el saldo final.

**Acciones:** Registrar ingreso · Registrar gasto · Transferir · Ajustar saldo (con motivo) · Editar datos de la cuenta · Archivar.

---

## 5. Reglas de cálculo

```
saldoCuenta(en su moneda)  = saldoInicial + Σ movimientos (entradas con +, salidas con −)
entradasPeriodo            = Σ movimientos positivos del período (sin contar transferencias entre cuentas propias si se desea mostrar aparte)
salidasPeriodo             = Σ movimientos negativos del período
saldoInicialPeriodo        = saldoInicial + Σ movimientos anteriores al período
saldoFinalPeriodo          = saldoInicialPeriodo + entradasPeriodo − salidasPeriodo   // debe igualar al saldo mostrado al final del período
saldoDespuesDeCadaMovimiento = saldo corrido, ordenado por fecha y luego por id

equivalenteUsd (cuenta en Bs) = saldoBs ÷ tasaDeHoy
totalEnCuentasUsd             = Σ saldos en USD + Σ saldos en Bs ÷ tasaDeHoy
```

**Reglas:**
- **Transferencia entre cuentas propias:** no es ni ingreso ni gasto del negocio. Se registra como una salida en una cuenta y una entrada en la otra, enlazadas. Si las monedas son distintas, guardar el monto de cada lado y la tasa usada.
- Un **ajuste de saldo** es un movimiento con signo y motivo; nunca se edita el saldo directamente.
- **Salidas que dejarían la cuenta en negativo:** permitir, pero con aviso (*"Esta cuenta quedaría en negativo"*).
- Cada abono recibido se asigna a su cuenta según el **método de pago** registrado, y genera su movimiento automáticamente.
- Cuentas en Bs: el valor en USD cambia con la tasa; indicar la fecha de la tasa y que es una conversión de hoy.
- **Archivar** una cuenta la oculta de la lista y de los totales, pero conserva su historial. Solo se puede archivar con saldo cero o con una confirmación explícita.
- Si el saldo mostrado y la suma de movimientos difieren por redondeo, siempre manda la suma de movimientos.

---

## 6. Datos mínimos (adaptar a los nombres reales)

| Tabla | Campos principales |
|---|---|
| `CuentaBancaria` (existente) | Agregar: `tipo`, `moneda`, `institucion` (nullable), `alias` (nullable), `uso` (nullable), `metaUsd` (nullable), `datosDePago` (nullable), `color`, `icono`, `saldoInicial`, `archivada`, `orden` |
| `MovimientoCuenta` | id, cuentaId, tipo (ABONO / GASTO / RETIRO / TRANSFERENCIA_ENTRADA / TRANSFERENCIA_SALIDA / AJUSTE / SALDO_INICIAL), monto (con signo), moneda, tasa (nullable), montoUsd, concepto, referenciaTipo y referenciaId (abono, gasto, retiro, etc.), referenciaPago (nullable), comprobanteRuta (nullable), fecha |

**Migración:** el saldo actual de cada cuenta pasa a `saldoInicial`; los campos nuevos tienen valores por defecto; los abonos antiguos no se asignan a ninguna cuenta (no se inventa historia). Declarar en la entidad cada valor por defecto igual que en el SQL. Índices en `cuentaId` y `fecha`.

---

## 7. Textos de la interfaz

| Elemento | Texto |
|---|---|
| Título de la sección | Bancos y billeteras |
| Cabecera | Total en cuentas |
| Botón | + Nueva cuenta |
| Entradas / salidas | Entró · Salió |
| Meta | {x} % de tu meta de ${y} |
| Último movimiento | Último movimiento |
| Acciones | Ingreso · Gasto · Mover |
| Ajuste | Ajustar saldo (te pedirá el motivo) |
| Archivar | Archivar cuenta |
| Aviso de negativo | Esta cuenta quedaría en negativo |
| Vacío (sin movimientos) | Aún no hay movimientos en esta cuenta |
| Vacío (sin cuentas) | Agrega tu primera cuenta para saber dónde está tu dinero |
| Tasa | Tasa usada: Bs {tasa} · {fecha} |
| Ayuda del saldo | El saldo es el saldo inicial más todo lo que ha entrado y salido |

---

## 8. Paleta de colores

Los tonos principales salen de tus capturas, así que son **aproximados**. Si tienes los códigos exactos en el tema de Compose, úsalos en su lugar. Verifica los contrastes con una herramienta antes de fijarlos.

### 8.1 Base de Zubli

| Uso | Color | Código |
|---|---|---|
| Primario (encabezados, botones) | Verde azulado oscuro | `#00707A` |
| Primario claro (chips, fondos suaves) | Verde azulado claro | `#88C5BD` |
| Fondo de pantalla | Crema | `#FCF1EE` |
| Acento decorativo | Coral | `#EC9F85` |
| Superficie de tarjeta | Blanco | `#FFFFFF` |
| Texto principal | Casi negro | `#1C1B1F` |
| Texto secundario | Gris verdoso (sugerido) | `#5F6B6D` |

### 8.2 Colores con significado

| Significado | Texto o ícono | Fondo suave | Nota |
|---|---|---|---|
| **Entró (positivo)** | `#2E7D32` | `#E4F2E5` | Siempre con `↓` y signo `+` |
| **Salió (negativo)** | `#C62828` | `#FBE4E4` | Siempre con `↑` y signo `−` |
| Reposición | `#1E5BC6` | `#E3EDFB` | Igual que en el Resumen |
| Ganancia | `#C2185B` | `#FBE3EE` | Igual que en el Resumen |
| Por cobrar / aviso | `#B23C00` (texto) | `#FDEBDD` | El naranja `#E65100` se usa solo en íconos y textos grandes |
| Acción secundaria | `#6B4FA3` | `#ECE6F5` | El morado del botón "Nueva" |

### 8.3 Colores por tipo de cuenta (ícono y etiqueta)

| Tipo | Texto o ícono | Fondo suave |
|---|---|---|
| Banco nacional | `#1E5BC6` | `#E3EDFB` |
| Pago móvil | `#00707A` | `#DDF0EE` |
| Efectivo | `#2E7D32` | `#E4F2E5` |
| Billetera virtual | `#6B4FA3` | `#ECE6F5` |
| Banco en dólares | `#0B5C73` | `#DDEEF3` |
| Otra | `#5F6B6D` | `#ECEFEF` |

**Etiquetas de moneda:** `USD` en `#00707A` sobre `#DDF0EE`; `Bs` en `#8A4B1E` sobre `#FCE9DD`. El color de la moneda no debe confundirse con el de entradas y salidas.

### 8.4 Reglas de uso del color
- El **verde y el rojo se reservan** para entradas y salidas. No los uses como color de tipo de cuenta salvo el verde de efectivo en su etiqueta.
- **El color nunca es el único indicador:** acompañarlo siempre con ícono (`↓` `↑`), signo (`+` `−`) o texto.
- El coral `#EC9F85` y el verde claro `#88C5BD` son decorativos: **no los uses como color de texto** sobre fondos claros (contraste insuficiente).
- Texto pequeño: contraste mínimo de 4.5:1. Texto grande y elementos gráficos: mínimo de 3:1.
- Modo oscuro (si lo habilitas): usar tonos más claros de los mismos colores y fondos de superficie oscuros; revisar contrastes de nuevo.

### 8.5 Medidas de referencia
- Esquinas redondeadas de 16 dp en tarjetas y de 12 dp en chips.
- Saldo en 24 a 28 sp en negrita; nombre en 16 sp seminegrita; subtítulos y etiquetas en 12 sp.
- Mínimo 48 dp de alto en botones y zonas táctiles.
- Montos con separador de miles y decimales según el formato local (`Bs 12.500,00`) y alineados a la derecha en listas.

---

## 9. Accesibilidad

- Descripción hablada de cada tarjeta: *"Cuenta Binance, fondo de reposición, dólares, saldo 14 dólares con 77 centavos, entraron 20 dólares y salieron 5 con 23"*.
- Botones de ícono con descripción (`contentDescription`).
- Tamaño de fuente del sistema respetado: la tarjeta crece en altura y no recorta el texto.
- Los gráficos y barras de meta también se expresan en texto.

---

## 10. Pruebas

**Cálculo:**
- Saldo inicial $10, ingreso $5, gasto $2 → saldo $13.
- Entró $20.00 y salió $5.23 con saldo inicial $0 → saldo $14.77 (como en el ejemplo).
- Saldo inicial del período + entradas − salidas = saldo final del período.
- Saldo corrido después de cada movimiento correcto en el orden por fecha.
- Transferencia de $3 entre dos cuentas USD → una baja $3, la otra sube $3, el total no cambia.
- Transferencia USD → Bs con tasa 875.65 → ambos montos guardados y coherentes.
- Total en cuentas: $14.77 + $4.23 + Bs 1.000 a tasa 875.65 → $19.00 + $1.14 = **$20.14**.
- Gasto mayor que el saldo → aviso de negativo, y se permite si el usuario confirma.
- Ajuste de saldo → queda como movimiento con motivo y fecha.

**Interfaz:**
- Nombre largo → se ve completo en dos líneas.
- Cuenta sin movimientos → mensaje de vacío, sin errores.
- Cuenta en Bs sin tasa configurada → se muestra el saldo en Bs y se oculta el equivalente en USD, sin cierres.
- Archivar una cuenta con movimientos → desaparece de la lista y de los totales, conserva su historial y se puede restaurar.
- Fuente grande del sistema → nada se recorta.

**Datos y migración:**
- Actualizar encima de la versión actual: cada cuenta conserva su saldo (como saldo inicial) y su nombre.
- Los abonos nuevos se asignan a su cuenta según el método de pago y aparecen en el historial.

---

## 11. Prompt para tu agente

> Rediseña las tarjetas de Bancos y Billeteras del Resumen siguiendo `TARJETAS_CUENTAS_ZUBLI.md`. Cada cuenta debe mostrar nombre completo (hasta 2 líneas), tipo con ícono, moneda, etiqueta de uso, saldo calculado como saldo inicial más la suma de movimientos, equivalente en USD si es en Bs, entradas y salidas del período seleccionado, último movimiento y meta opcional. Crea `MovimientoCuenta` con migración sin borrar datos: el saldo actual de cada cuenta pasa a ser su saldo inicial. Agrega la pantalla de detalle con la conciliación del período, historial con saldo corrido, filtros, acciones (ingreso, gasto, transferir, ajustar saldo), archivar en vez de borrar y exportar extracto. Usa la paleta de la sección 8 y no uses solo color para indicar entradas y salidas. Redondea a 2 decimales al guardar y sumar, y no cambies el desglose guardado de los abonos. Incluye pruebas unitarias con los casos de la sección 10.
