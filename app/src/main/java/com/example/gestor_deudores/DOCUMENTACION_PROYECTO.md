# 📑 DOCUMENTACIÓN TÉCNICA Y MANUAL DE USO DEL PROYECTO
## **Gestor Financiero y de Producción para Talleres y Comercios**

---

## 📌 1. PROPÓSITO DEL PROYECTO

**Gestor Financiero y de Producción** es una aplicación móvil nativa para Android diseñada específicamente para **talleres de estampado, sublimación, bordado, confección y comercios de producción personalizada**.

### ¿Qué problema resuelve?
1. **Separación de Dinero de Materia Prima vs. Ganancia Real**: En los negocios de producción personalizada, cuando un cliente paga un adelanto o abono, gran parte de ese dinero pertenece a las camisetas, telas, tintas o envíos. Si el emprendedor gasta ese dinero como ganancia, el taller se queda sin capital para comprar insumos. La app separa automáticamente cada cobro en **Sobres de Insumos** y calcula la **Ganancia Neta Real** y el dinero **Disponible para Retirar**.
2. **Control Multimoneda (USD y Bolívares VES)**: Permite registrar cuentas en Dólares ($) y Bolívares (Bs) a la tasa oficial del BCV, realizando conversiones en tiempo real sin mezclar monedas.
3. **Gestión de Cuentas Bancarias y Billeteras (Sistema Zubli)**: Mantiene el saldo corrido en tiempo real de cada banco, pago móvil o caja de efectivo, permitiendo realizar extractos en CSV, transferencias entre cuentas y ajustes contables transparentes.
4. **Desglose de Pedidos de Producción**: Controla el ciclo de vida de los pedidos por ítems individuales con sus variantes (tallas, colores), texto de estampado, notas y fotografías vinculadas por producto.
5. **Cuentas por Cobrar y Cobros en Cascada ("Mis Cobros")**: Elimina la duplicación de cobranzas imputando los abonos globales del cliente a sus compras pendientes en orden cronológico.

---

## 🏗️ 2. ARQUITECTURA DEL SISTEMA Y PATRÓN MVVM

El proyecto está construido siguiendo los principios de la **Android Modern App Architecture**:
* **Patrón de Arquitectura**: **MVVM (Model-View-ViewModel)** estricto.
  * **View (`ui/`)**: Compuesta exclusivamente por composables de **Jetpack Compose**. Es 100% declarativa, reactiva y libre de lógica de negocio o cálculos financieros.
  * **ViewModel (`ui/*ViewModel.kt`)**: Actúa como el cerebro. Administra el estado de la UI (`StateFlow`), maneja corrutinas (`viewModelScope`), aplica reglas financieras y se comunica con la capa de datos.
  * **Model / Data (`data/`)**: Capa de datos administrada mediante **Room Database (v32)** con entidades Kotlin, DAOs y migraciones atómicas.

### Stack Tecnológico
* **Lenguaje**: Kotlin 1.9+
* **UI Toolkit**: Jetpack Compose con Material Design 3
* **Base de Datos**: Room Persistence Library v32
* **Asincronía & Flujos**: Kotlin Coroutines & `StateFlow` / `Flow`
* **Navegación**: Jetpack Compose Navigation
* **Exportación de Datos**: Generador de reportes PDF (`PdfDocument`) y CSV

---

## 🗄️ 3. ESQUEMA DE BASE DE DATOS Y MIGRACIONES (Room v32)

La base de datos Room (`DeudaDataBase.kt`) gestiona las siguientes entidades principales:

### Entidades y Tablas Principales

| Entidad / Tabla | Propósito | Campos Clave |
| :--- | :--- | :--- |
| `Deudor` (`deudores`) | Perfil del cliente | `id`, `nombre`, `apellido`, `cedula`, `telefono` |
| `Deuda` (`Tabla_Deuda`) | Registro financiero de cargos y abonos | `id`, `idDeudor`, `montoInicial`, `montoRestante`, `tipo`, `descripcion`, `estado`, `fechaMillis` |
| `CuentaBancaria` (`cuentas_bancarias`) | Bancos, billeteras y cajas de efectivo | `id`, `nombre`, `tipo`, `moneda`, `saldoInicial`, `uso`, `metaUsd`, `datosDePago`, `activa` |
| `MovimientoCuenta` (`movimientos_cuenta`) | Historial de transacciones bancarias | `id`, `cuentaId`, `monto`, `montoUsd`, `concepto`, `tipo`, `referenciaPago`, `fechaMillis` |
| `Pedido` (`pedidos`) | Órdenes de trabajo del taller | `id`, `deudorId`, `producto`, `cantidad`, `precioUnitarioUsd`, `totalUsd`, `estado`, `notas`, `fechaCreacionMillis` |
| `Gasto` (`gastos`) | Compras de insumos y gastos operativos | `id`, `categoriaGastoId`, `montoUsd`, `monedaOriginal`, `montoOriginal`, `descripcion`, `fechaMillis` |
| `Retiro` (`retiros`) | Retiros de ganancia del dueño | `id`, `montoUsd`, `nota`, `fechaMillis` |
| `DistribucionAbono` (`distribucion_abonos`) | Desglose de ingresos en sobres de insumos | `id`, `abonoId`, `pedidoId`, `material`, `transporte`, `insumos`, `ganancia` |
| `PlantillaCotizacion` (`plantillas_cotizacion`) | Presupuestos y costos base | `id`, `nombre`, `costoPiezaBaseUsd`, `costoPasajeUsd`, `costoInsumosUsd`, `margenGananciaPct` |

### Regla de Inmutabilidad e Integridad Financiera
1. **Los movimientos no se editan ni se eliminan**: Las correcciones o discrepancias en bancos se registran mediante nuevos movimientos de tipo `AJUSTE` exigiendo un motivo o razón obligatoria.
2. **Migración Atómica `MIGRATION_31_32`**: Al actualizar la BD a la versión 32, el saldo anterior de cada cuenta se convierte de forma segura en `saldoInicial` sin alterar movimientos pasados ni perder información.

---

## 🧩 4. MÓDULOS Y FUNCIONALIDADES PRINCIPALES

### 🟢 Módulo 1: Inicio / "Mis Cobros" (`homeView.kt`, `HomeViewModel.kt`)
* **Propósito**: Administrar clientes y cuentas por cobrar.
* **Cálculo de Cobro en Cascada**: Registra los pagos del cliente de forma global (`Abono / Pago Parcial`). El sistema calcula el saldo acumulado deudor restando todos los abonos a las deudas activas en orden cronológico.
* **Historial por Cliente**: Lista cronológica de todos los cargos y abonos con fechas y referencias.

### 📦 Módulo 2: Pedidos y Producción (`pedidoView.kt`, `PedidoViewModel.kt`)
* **Propósito**: Control de órdenes de trabajo para el taller.
* **Embudo de Estado**: Transición de pedidos entre `RECIBIDO` $\rightarrow$ `EN_PRODUCCION` $\rightarrow$ `LISTO` $\rightarrow$ `ENTREGADO` $\rightarrow$ `CANCELADO`.
* **Desglose Ítem por Ítem**: Descompone cada orden en artículos individuales con cantidad, precio unitario, variantes (tallas S, M, L, XL, colores), texto de estampado y notas por producto.
* **Fotografías Vinculadas**: Adjunta fotos de diseño y muestras asociadas directamente a cada producto dentro de su ficha.

### 🏦 Módulo 3: Bancos y Billeteras — Rediseño Zubli (`resumenView.kt`)
* **Tipos de Cuentas**:
  * `BANCO`: Bancos nacionales en Bs.
  * `PAGO_MOVIL`: Pago Móvil en Bs.
  * `EFECTIVO`: Cajas de efectivo ($ / Bs).
  * `BILLETERA`: Billeteras digitales (Zelle, Zinli, Binance, PayPal).
  * `BANCO_USD`: Bancos internacionales en Dólares.
* **Tarjetas Visuales Zubli**: Muestran nombre completo, tipo con ícono, etiqueta de uso (`[Cobros]`, `[Reposición]`, `[Ahorro]`), saldo calculado en su moneda, equivalente en USD a tasa BCV e indicadores de ingresos/egresos (`↓ Entró +$20.00`, `↑ Salió −$5.23`).
* **Detalle y Conciliación**:
  ```
  Saldo al inicio del período:   $  0.00
  (+) Entradas del período:      +$ 20.00
  (-) Salidas del período:       -$  5.23
  (=) Saldo al final del período: $ 14.77
  ```
* **Historial con Saldo Corrido**: Muestra el saldo acumulado en la cuenta después de cada transacción.
* **Herramientas**:
  * `[ + Ingreso ]` y `[ − Gasto ]`: Registro rápido de movimientos.
  * `[ ⇄ Mover ]`: Transferencias entre cuentas propias con conversión de moneda automática.
  * `[ ⚙️ Ajustar Saldo ]`: Ajuste contable con motivo obligatorio.
  * `[ 📄 Extracto CSV ]`: Generación y exportación de reporte contable en archivo CSV.

### 📊 Módulo 4: Resumen Financiero y Sobres (`ResumenViewModel.kt`)
* **Disponible para Retirar (`💎 DISPONIBLE PARA RETIRAR`)**:
  Cálculo de la ganancia neta real disponible para transferirse al bolsillo personal del dueño, protegiendo las reservas de caja para compra de materia prima y pago de gastos del taller.
* **Sobres de Insumos**: Apartado automático para *Prenda/Materia Prima*, *Logística/Pasajes* e *Insumos/Tintas*.
* **Proyección de Cobros**: Anticipa los ingresos previstos a 7 y 30 días.
* **Antigüedad de Cartera**: Mide la puntualidad de pago de los clientes y niveles de morosidad.

### 🧮 Módulo 5: Cotizador y Plantillas (`cotizadorView.kt`, `CotizadorViewModel.kt`)
* Presupuestador automático para clientes calculando costos base de tela/camisa, logística/pasaje, consumo de insumos/tintas y margen de ganancia deseado.

---

## 📖 5. MANUAL DE USUARIO Y GUÍA DE USO RÁPIDO

### A. ¿Cómo registrar un cliente y cobrarle?
1. Ve a la pestaña **Mis Cobros**.
2. Pulsa el botón **`+ Nuevo Cliente`** e ingresa sus datos (Nombre, Apellido, Teléfono).
3. Para registrar un pago recibido, toca la tarjeta del cliente y pulsa **`+ Registrar Abono`**.

### B. ¿Cómo crear un Pedido de Producción?
1. Ve a la pestaña **Pedidos** y pulsa **`+ Nuevo Pedido`**.
2. Selecciona el cliente y agrega los productos con sus cantidades, precios, variantes y texto de estampado.
3. El pedido aparecerá en la columna **Recibido**. Puedes avanzar su estado a **En Prod.**, **Listo** y **Entregado**.

### C. ¿Cómo administrar Cuentas Bancarias y Efectivo?
1. Ve a la sección **Bancos y Billeteras** en la pestaña **Resumen**.
2. Pulsa **`+ Nueva cuenta`** para registrar tu banco, pago móvil o caja de efectivo.
3. Toca cualquier tarjeta de cuenta para ver su **Conciliación de Período**, **Historial con Saldo Corrido** o exportar su **Extracto en CSV**.
4. Para mover dinero entre cuentas (ej. cambiar dólares a bolívares), usa el botón **`⇄ Mover`**.

---

## 🛠️ 6. MANTENIMIENTO Y BUENAS PRÁCTICAS PARA DESARROLLADORES

1. **Cumplimiento MVVM**:
   * Nunca invoques estados o corrutinas directamente desde la vista sin pasar por el `ViewModel`.
   * Los `ViewModels` emiten un único `StateFlow` inmutable (`uiState`) consumido por Compose con `collectAsState()`.
2. **Precisiones Financieras**:
   * Utiliza la extensión `.redondear2()` para todas las operaciones monetarias para evitar errores de punto flotante en IEEE 754.
3. **Compilación y Pruebas**:
   * Ejecuta `./gradlew :app:testDebugUnitTest` para verificar la suite de unit tests antes de cada despliegue.
