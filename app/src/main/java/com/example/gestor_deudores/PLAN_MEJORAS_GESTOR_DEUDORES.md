# Plan de mejoras: Gestor de Deudores

Documento para ejecutar con el agente de IA en Android Studio.
Objetivo: convertir la app actual en un producto vendible para negocios pequeños (sublimación y personalizados), **sin perder nunca los datos ya guardados en el celular**.

---

## 0. Cómo usar este documento

1. Haz primero la **Sección 1 (respaldos)**. No toques código antes.
2. Lee la **Sección 2 (reglas para no perder la base de datos)**. Son obligatorias en cada actualización.
3. Pégale al agente el **Prompt maestro** (Sección 8) al inicio de cada sesión.
4. Trabaja **un bloque a la vez** (Sección 4 en adelante). Al terminar cada bloque: probar, commit, push.
5. Marca cada bloque con su lista de criterios de aceptación antes de pasar al siguiente.

**Regla de oro:** si un cambio toca la base de datos, se hace con una `Migration` de Room y se prueba con datos reales de una versión anterior. Sin excepciones.

---

## 1. Respaldos antes de empezar

### 1.1 Git con GitHub Desktop

1. Verifica que existe `.gitignore` en la raíz (Android Studio lo crea por defecto). Debe ignorar al menos: `build/`, `.gradle/`, `local.properties`, `*.keystore`, `*.jks`.
2. GitHub Desktop → `File → Add local repository…` → elige la carpeta del proyecto. Si dice que no es un repositorio → "create a repository" (rama `main`).
3. Pestaña **Changes**: revisa la lista. No deben aparecer `build/`, `local.properties` ni keystores.
4. Summary: `v1.0 estable antes de mejoras`. Botón **Commit to main**.
5. **Publish repository** → marca *Keep this code private* (repositorio privado).
6. **Crear la rama de trabajo:** `Branch → New Branch…` → `mejoras-negocio` → Create. A partir de aquí, todo se hace en esa rama. `main` queda como la versión actual intacta.
7. (Opcional) Etiqueta: pestaña **History** → clic derecho sobre el commit → *Create Tag…* → `v1.0` → luego *Push origin*. Si tu versión de GitHub Desktop no trae esa opción, la rama `main` ya cumple la función.
8. Haz un commit **después de cada bloque terminado** con mensajes claros (`Bloque 1: textos y archivado`, etc.) y push.

### 1.2 Cómo volver a la versión actual

- **Ver la versión original:** cambia a la rama `main` (selector *Current Branch*). En Android Studio haz `File → Sync Project with Gradle Files`, o cierra y reabre el proyecto.
- **Crear una rama desde ese punto exacto:** pestaña *History* → clic derecho sobre el commit `v1.0 estable...` → *Create Branch from Commit*.
- **Descartar cambios no guardados:** pestaña *Changes* → clic derecho sobre el archivo → *Discard changes*.

### 1.3 Copias adicionales (5 minutos, muy recomendado)

1. **Zip del proyecto:** copia la carpeta completa y comprímela. Guárdala fuera del PC (Drive o USB).
2. **APK de la versión actual:** `Build → Build Bundle(s) / APK(s) → Build APK(s)`. Renómbralo `GestorDeudores-v1.0.apk` y guárdalo. Sirve para volver a instalar la versión vieja y para **probar la migración** (Sección 2.5).
3. **Base de datos del celular** (la más importante, ver 1.4).

### 1.4 Exportar la base de datos del celular

Tus datos reales viven en un archivo SQLite dentro del teléfono. Cópialo antes de instalar nada nuevo.

**Opción A: Device Explorer de Android Studio (la más fácil)**
1. Conecta el celular con depuración USB activada.
2. `View → Tool Windows → Device Explorer`.
3. Navega a `/data/data/<applicationId>/databases/` (el `applicationId` está en `app/build.gradle.kts`; la documentación indica el paquete `com.example.gestor_deudores`).
4. Verás el archivo `.db` y, probablemente, dos más con sufijos `-wal` y `-shm`. **Guarda los tres** (clic derecho → *Save As…*) en una carpeta `respaldo-db-YYYY-MM-DD`.
5. El nombre real de la base está en `DeudaDataBase.kt`, en `Room.databaseBuilder(..., "NOMBRE")`.

Nota: esta vía solo funciona si la app instalada es un build de depuración (el que instala Android Studio al darle Run), que es tu caso.

**Opción B: línea de comandos (adb), solo en `cmd`, no en PowerShell**
PowerShell corrompe archivos binarios al redirigir con `>`. Usa `cmd`:
```
adb exec-out "run-as com.example.gestor_deudores cat databases/NOMBRE.db" > NOMBRE.db
adb exec-out "run-as com.example.gestor_deudores cat databases/NOMBRE.db-wal" > NOMBRE.db-wal
adb exec-out "run-as com.example.gestor_deudores cat databases/NOMBRE.db-shm" > NOMBRE.db-shm
```
(Sustituye `NOMBRE` por el nombre real. `adb` está en `<SDK>/platform-tools`.)

**Verificación rápida:** abre el `.db` copiado con "DB Browser for SQLite" (gratis) y confirma que ves a tus clientes. Si los ves, el respaldo es bueno.

---

## 2. Cómo actualizar sin perder los datos del celular

### 2.1 Condiciones para que Android conserve los datos al instalar encima

Android actualiza la app **conservando sus datos** solo si se cumplen las tres:

| Condición | Qué hacer |
|---|---|
| Mismo `applicationId` | **Nunca lo cambies.** Tampoco renombres el paquete en Gradle. |
| Misma firma (keystore) | Instala siempre desde el mismo PC/keystore. Si cambias de PC, la clave debug será distinta y Android rechazará la actualización (ver 2.3). |
| `versionCode` igual o mayor | Súbelo en cada versión que entregues (`versionCode = 2`, `3`, ...). |

### 2.2 Qué NO hacer (cualquiera de estas borra los datos)

- Desinstalar la app antes de instalar la nueva.
- "Borrar almacenamiento" o "Borrar datos" en los ajustes del teléfono.
- Dejar activada en la configuración de Run una opción tipo *Clear app storage before deployment* (revísala en `Run → Edit Configurations`).
- Usar `.fallbackToDestructiveMigration()` en Room (ver 2.4).
- Cambiar el nombre de la base de datos en `Room.databaseBuilder` (Room crearía una base nueva y vacía).

### 2.3 Si Android dice "INSTALL_FAILED_UPDATE_INCOMPATIBLE"

Significa que la firma no coincide. **No desinstales.** Primero exporta la base (1.4), y recién entonces desinstala, instala y vuelve a importar usando el módulo de respaldo (Bloque 5). Por eso conviene tener el respaldo cuanto antes.

### 2.4 Reglas de Room para cambiar la estructura

1. **Buscar y eliminar** en el proyecto cualquier `fallbackToDestructiveMigration`. Si existe, quítalo **antes** de subir la versión: con ese método, al cambiar la versión Room borra todos los datos.
2. Cada cambio de estructura = `version` +1 en `@Database` + un objeto `Migration(vieja, nueva)` registrado con `.addMigrations(...)`.
3. Exportar los esquemas para poder comparar y probar:
   ```kotlin
   @Database(entities = [...], version = 2, exportSchema = true)
   ```
   En `app/build.gradle.kts` (si usas KSP):
   ```kotlin
   ksp { arg("room.schemaLocation", "$projectDir/schemas") }
   ```
   (con kapt: `kapt { arguments { arg("room.schemaLocation", "$projectDir/schemas") } }`).
   La carpeta `schemas/` **sí se sube a Git**: es el historial de tu estructura.
4. Solo se pueden **agregar** columnas con `ALTER TABLE ... ADD COLUMN`. Cambiar el tipo de una columna o borrarla exige recrear la tabla (más delicado), así que se evita.
5. Toda columna nueva `NOT NULL` lleva `DEFAULT` en el SQL de la migración, y la propiedad en la entidad debe tener su valor por defecto en Kotlin y, preferiblemente, `@ColumnInfo(defaultValue = ...)`.
   Si Room lanza *"Migration didn't properly handle..."*, el error muestra el esquema esperado y el encontrado: ajusta el SQL hasta que coincidan. El archivo `schemas/N.json` contiene el `CREATE TABLE` exacto que Room espera.

Ejemplo de estructura (los nombres de tablas y columnas son supuestos: **el agente debe leer `entity.kt` y usar los reales**):

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Deuda ADD COLUMN tipo TEXT NOT NULL DEFAULT 'CARGO'")
        db.execSQL("UPDATE Deuda SET tipo = 'ABONO' WHERE rol = 'PAGO' OR montoRestante < 0")
        // ...resto de columnas del Bloque 1 y 2
    }
}

Room.databaseBuilder(context, DeudaDataBase::class.java, "NOMBRE_ACTUAL_SIN_CAMBIOS")
    .addMigrations(MIGRATION_1_2 /*, MIGRATION_2_3, ... */)
    .build()
```

### 2.5 Probar la migración con datos reales antes de tocar tu celular

Hazlo en un **emulador** (o un segundo celular):
1. Instala el `GestorDeudores-v1.0.apk` guardado en 1.3.
2. Crea 3 o 4 clientes de prueba, con deudas y abonos.
3. Con Android Studio, ejecuta la versión nueva **encima** (sin desinstalar).
4. Verifica: los clientes siguen ahí, los saldos son iguales, el historial está completo.
5. Solo si pasa, instala en tu celular real.

Anota los saldos de 3 clientes y el "total por cobrar" **antes** de actualizar; deben ser idénticos después.q

### 2.6 Firma para entregar la app a clientes

Cuando empieces a entregar APK a otras personas:
- Crea **una sola keystore de release** (`Build → Generate Signed Bundle / APK`), guárdala en 2 lugares seguros (Drive privado + USB) y **nunca la subas a Git**.
- Firma todas las versiones con esa misma keystore. Si la pierdes o cambias, tus clientes tendrán que desinstalar y perder sus datos.
- Sube `versionCode` y `versionName` en cada entrega.

---

## 3. Mapa de cambios en la base de datos

Todos son **agregados**: las columnas existentes no se tocan ni se renombran.

### Versión 2 (Bloques 1 y 2)

| Tabla | Columna nueva | Tipo | Default | Para qué |
|---|---|---|---|---|
| Deuda | `tipo` | TEXT NOT NULL | `'CARGO'` | Criterio único para distinguir cargos de abonos |
| Deuda | `numCuotas` | INTEGER NOT NULL | `1` | Número de cuotas como número (hoy es texto) |
| Deuda | `frecuencia` | TEXT NOT NULL | `'MENSUAL'` | SEMANAL, QUINCENAL o MENSUAL (solo si hoy no se guarda) |
| Deuda | `fechaMillis` | INTEGER NOT NULL | `0` | Fecha en milisegundos para ordenar y filtrar por mes |
| Deudor | `archivado` | INTEGER NOT NULL | `0` | Archivar en vez de borrar |

Datos a rellenar dentro de la migración:
- `tipo`: `ABONO` donde `rol = 'PAGO'` o `montoRestante < 0`; el resto `CARGO`.
- `numCuotas`: convertir el texto actual a número cuando sea posible:
  ```sql
  UPDATE Deuda SET numCuotas = CASE WHEN CAST(cuotas AS INTEGER) > 0 THEN CAST(cuotas AS INTEGER) ELSE 1 END WHERE tipo = 'CARGO'
  ```
  (verifica el nombre real de la columna de cuotas).
- `fechaMillis`: dentro de `migrate`, recorrer las filas con `db.query("SELECT id, fecha FROM Deuda")`, convertir cada fecha de texto (por ejemplo `dd/MM/yyyy`) con `LocalDate`/`SimpleDateFormat` y hacer `UPDATE Deuda SET fechaMillis = ? WHERE id = ?`. Si una fecha no se puede leer, dejar `0`.
- **No borrar** las columnas viejas (`rol`, `cuotas`): se siguen escribiendo para mantener compatibilidad.

### Versión 3 (Bloque 4, doble moneda)

| Tabla | Columna nueva | Tipo | Default | Para qué |
|---|---|---|---|---|
| Deuda | `monedaOriginal` | TEXT NOT NULL | `'USD'` | Moneda en que se registró (USD o BS) |
| Deuda | `montoOriginal` | REAL NOT NULL | `0` | Monto tal como lo escribió el usuario |
| Deuda | `tasaUsada` | REAL NOT NULL | `0` | Tasa Bs/USD del momento (0 = desconocida) |

Los montos principales (`montoRestante`, etc.) **siguen en USD**: así toda la suma actual sigue funcionando sin cambios. Los registros viejos quedan con `USD` y tasa `0`.

### Versión 4 (Bloques 6 y 7, pedidos y cotizador)

Tabla nueva `Pedido`:

| Campo | Tipo | Nota |
|---|---|---|
| id | INTEGER PK autoGenerate | |
| deudorId | INTEGER | clave foránea a Deudor |
| producto | TEXT | |
| cantidad | INTEGER | |
| precioUnitarioUsd | REAL | |
| totalUsd | REAL | cantidad × precio |
| fechaCreacionMillis | INTEGER | |
| fechaEntregaMillis | INTEGER | 0 = sin fecha |
| estado | TEXT | `RECIBIDO`, `EN_PRODUCCION`, `LISTO`, `ENTREGADO` |
| notas | TEXT | |

Más una columna `pedidoId INTEGER` (nullable) en `Deuda`, para enlazar el cargo con su pedido.
Para el cotizador, opcionalmente una tabla `PlantillaCosto` (nombre del producto, costo del producto en blanco, tinta, papel, otros, minutos de prensa, margen).

### Ajustes de la app (sin tabla)

Usa **DataStore Preferences** (o SharedPreferences) para: `tasaBs`, `fechaTasa`, `nombreNegocio`, `plantillaMensajeWhatsApp`, `ultimoRespaldoMillis`, `costoMinutoPrensaUsd`.

---

## 4. Bloque 1: Textos, pantalla de inicio y seguridad de datos

**Objetivo:** que la app deje de parecer de prestamista y se vea como una herramienta de negocio, sin riesgo de perder información por un toque.

**Cambios en la interfaz**
1. Título de la pantalla de inicio: de "NUEVO DEUDOR" a **"Mis cobros"** (o el nombre del negocio, cuando exista el ajuste). "Nuevo deudor" queda solo en la pantalla de registro.
2. Cambiar términos: "Deudor" → **"Cliente"**, "Tipo de préstamo" → **"Producto o pedido"**, "Detalles del préstamo" → **"Detalles de la venta"**, "Monto inicial" → **"Total de la venta"**.
3. Quitar de la UI el selector **Rol** (Cliente / Familiar / Otro). La columna `rol` se conserva en la base, y al guardar se escribe un valor fijo (`CLIENTE` para cargos, `PAGO` para abonos hasta que `tipo` sea el criterio único).
4. **Producto o pedido** como campo con sugerencias: al escribir, mostrar como sugerencias los valores usados antes (consulta `SELECT DISTINCT tipo FROM Deuda`), para que cada negocio arme su propia lista (franelas, tazas, gorras...).
5. **Teléfono obligatorio** (necesario para WhatsApp): validar que tenga entre 10 y 11 dígitos y mostrar error debajo del campo.
6. **Cédula:** selector V / E / J junto al número, guardado en el mismo campo de texto actual (por ejemplo `V-12345678`).
7. Botones de la tarjeta: separar el botón rojo de eliminar del resto o moverlo al menú de la tarjeta, para evitar toques accidentales.

**Cambios de seguridad**
8. Eliminar pide **confirmación** con texto explícito: "Se archivará a Carolina Heredia. Podrás recuperarla desde Historial".
9. **Archivar en vez de borrar:** `Deudor.archivado = 1`. Las consultas de Pendientes, Historial y buscador filtran `archivado = 0`. En Historial, una opción "Ver archivados" con botón *Restaurar*.
10. Mantener el borrado definitivo solo dentro de "Archivados", con doble confirmación.

**Base de datos:** requiere Migration v1→v2 (columna `archivado`, y el resto de la versión 2; ver Sección 3). Conviene hacer una sola migración v2 para los Bloques 1 y 2.

**Criterios de aceptación**
- [ ] La app abre y muestra todos los clientes y saldos exactamente como antes.
- [ ] No queda ningún texto "deudor/préstamo/rol" visible al usuario.
- [ ] Eliminar un cliente lo mueve a Archivados y se puede restaurar con todo su historial.
- [ ] No se puede guardar un cliente sin teléfono válido.
- [ ] Datos viejos intactos tras actualizar encima de la versión 1 (prueba 2.5).

---

## 5. Bloque 2: Cuotas, vencimientos y orden por urgencia

**Objetivo:** que al abrir la app se vea **a quién cobrar hoy**.

**Datos necesarios por cada cargo:** monto original, `numCuotas` (número), `frecuencia`, fecha de la venta (`fechaMillis`). Verifica primero en `entity.kt` y en el formulario si la frecuencia ya se guarda; si no, agregarla (Sección 3).

**Regla de cálculo (sin tabla de cuotas)**
1. `montoCuota = montoOriginal / numCuotas`.
2. La cuota número `k` vence en: `fecha de la venta + k × periodo` (periodo: 7, 15 o 30 días; para mensual, mejor sumar `k` meses con `plusMonths(k)`).
3. **Asignación de abonos del cliente (FIFO):** juntar todos los abonos del cliente (incluyendo el adelanto inicial) y asignarlos a sus cargos del más antiguo al más nuevo.
4. Para cada cargo: `cuotasCubiertas = floor(abonoAsignado / montoCuota)`.
5. **Próxima cuota** del cliente = la primera cuota no cubierta de su cargo más antiguo con saldo. Si su fecha de vencimiento es anterior a hoy → **Vencida** (mostrar días de atraso).
6. Función sugerida (en un archivo de utilidades, con pruebas):
   ```kotlin
   data class EstadoCobro(
       val saldo: Double,
       val proximaCuotaMonto: Double,
       val proximoVencimiento: LocalDate?,
       val vencida: Boolean,
       val diasAtraso: Long
   )
   fun calcularEstadoCobro(cargos: List<Deuda>, abonos: List<Deuda>, hoy: LocalDate): EstadoCobro
   ```
7. Cuidado con `LocalDate`: requiere API 26+. Si `minSdk` es menor, activar *core library desugaring*.

**Cambios en la interfaz**
- Tarjeta del cliente: línea nueva **"Próxima cuota: 8.00 USD · vence 03/10"**; si está vencida, etiqueta roja **"Vencida hace N días"**.
- Orden de Pendientes: primero vencidos (más atrasados arriba), luego por próximo vencimiento.
- Filtros rápidos (chips) bajo el buscador: **Todos · Vencidos · Al día**.

**Redondeo:** toda suma o división de dinero se redondea a 2 decimales con una función única:
```kotlin
fun Double.redondear2(): Double = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
```
Aplicarla al guardar y al calcular. (Pasar todo a centavos enteros sería lo ideal, pero implica recrear tablas; se pospone.)

**Criterios de aceptación**
- [ ] Un cliente con cuota vencida aparece arriba y con la etiqueta roja.
- [ ] Un cliente al día muestra la fecha correcta de su próxima cuota.
- [ ] Cliente con adelanto inicial: la primera cuota se calcula descontando el adelanto.
- [ ] Un cliente con un solo pago (1 cuota) muestra el vencimiento a un periodo de la fecha de venta; si esa fecha no es lo que se quiere, se aclara en pantalla.
- [ ] Los saldos totales son iguales que en la versión anterior.

---

## 6. Bloque 3: Recordatorio por WhatsApp (sin API ni hosting)

**Objetivo:** cobrar con un toque. No usa la API oficial, solo abre WhatsApp con el mensaje ya escrito.

**Implementación**
1. Normalizar el teléfono venezolano:
   ```kotlin
   fun normalizarTelefonoVe(raw: String): String? {
       val d = raw.filter { it.isDigit() }
       return when {
           d.startsWith("58") && d.length == 12 -> d
           d.startsWith("0") && d.length == 11 -> "58" + d.drop(1)
           d.length == 10 -> "58$d"
           else -> null
       }
   }
   ```
2. Abrir WhatsApp:
   ```kotlin
   fun abrirWhatsApp(context: Context, telefono: String, mensaje: String) {
       val url = "https://wa.me/$telefono?text=${Uri.encode(mensaje)}"
       try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
       catch (e: ActivityNotFoundException) { /* avisar: WhatsApp no instalado */ }
   }
   ```
3. Botón de WhatsApp en la tarjeta del cliente (icono verde) y en la pantalla de detalle.
4. **Plantilla editable** en Ajustes, con variables: `{cliente}`, `{saldo}`, `{cuota}`, `{vence}`, `{negocio}`. Mensaje por defecto:
   > Hola {cliente}, te escribo de {negocio}. Tu saldo pendiente es de {saldo} USD. Tu próxima cuota de {cuota} USD vence el {vence}. ¡Gracias!
5. Si el cliente está vencido, usar una variante más directa ("Tu cuota de {cuota} USD venció el {vence}...").
6. Si no hay teléfono válido, el botón queda deshabilitado y avisa por qué.
7. Cuando exista la doble moneda (Bloque 4), el mensaje incluye el equivalente en Bs con la tasa del día.

**Criterios de aceptación**
- [ ] Al pulsar el botón se abre WhatsApp con el chat del cliente y el texto armado.
- [ ] Funciona con teléfonos escritos como `0414-1234567`, `04141234567` y `584141234567`.
- [ ] Sin teléfono válido no se rompe la app.

---

## 7. Bloques 4 al 8

### Bloque 4: Doble moneda (USD y Bs)
Requiere Migration v2→v3 (Sección 3).

1. Pantalla de **Ajustes**: campo "Tasa del día (Bs por 1 USD)" y fecha de última actualización. Se actualiza **manualmente** (sin depender de internet).
2. Si la tasa tiene más de 24 horas sin actualizar, mostrar un aviso discreto en Inicio.
3. Todos los montos principales siguen **guardados en USD**.
4. En el total por cobrar y en cada tarjeta: debajo del monto en USD, el equivalente en Bs (`USD × tasa`, formato `Bs. 1.234,56`).
5. Al registrar un **abono**, selector USD / Bs:
   - Si es Bs: `montoUsd = montoBs / tasaActual`; guardar `monedaOriginal = 'BS'`, `montoOriginal = montoBs`, `tasaUsada = tasaActual`; el abono baja el saldo en USD por ese `montoUsd`.
   - Mostrar antes de confirmar: "Bs. 500 a tasa 36,50 = 13,70 USD".
6. **Importante:** el historial muestra la tasa usada en cada abono; cambiar la tasa de hoy **no modifica** abonos anteriores.
7. Registros anteriores a la actualización: `monedaOriginal = 'USD'`, `tasaUsada = 0` (se muestran solo en USD).
8. Todas las conversiones se redondean a 2 decimales con `redondear2()`.

**Criterios de aceptación**
- [ ] Abonar en Bs reduce el saldo en USD por el monto convertido correcto.
- [ ] Cambiar la tasa no altera el saldo en USD de nadie.
- [ ] Los registros viejos se ven igual que antes.

### Bloque 5: Respaldo (exportar e importar)
Sin cambios de base de datos.

**Exportar**
1. Botón "Crear respaldo" en Ajustes.
2. Antes de copiar, volcar el WAL al archivo principal para que el respaldo no pierda los últimos datos:
   ```kotlin
   db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
   ```
3. Copiar el archivo de `context.getDatabasePath(NOMBRE_DB)` a la ubicación que el usuario elija con el selector de archivos del sistema (`ActivityResultContracts.CreateDocument`), nombre sugerido `respaldo_cobros_YYYY-MM-DD.db`.
4. Botón "Compartir respaldo" (hoja de compartir hacia WhatsApp o Drive) usando `FileProvider` con una copia en `cacheDir`.
5. Guardar `ultimoRespaldoMillis`. Si pasan **7 días** sin respaldo, mostrar un banner en Inicio: "Hace 7 días que no haces respaldo".
6. Opcional: exportar a CSV (clientes, cargos y abonos) para quien quiera ver sus datos en Excel.

**Importar (restaurar)**
1. `ActivityResultContracts.OpenDocument` para elegir el archivo.
2. **Validar** antes de reemplazar nada:
   - Los primeros bytes deben ser `SQLite format 3`.
   - La versión de esquema (`PRAGMA user_version`) no puede ser **mayor** a la de la app instalada (si es menor, Room la migrará sola al abrir).
3. Confirmación fuerte: "Esto reemplazará TODOS los datos actuales".
4. Hacer **primero un respaldo automático** de los datos actuales en `cacheDir`/almacenamiento interno, por si algo falla.
5. Cerrar la base (`db.close()`), reemplazar el archivo, **borrar** los archivos `-wal` y `-shm` viejos, y **reiniciar la app** (reabrir la actividad principal con flags de limpieza o cerrar el proceso).
6. Si la restauración falla, devolver el respaldo automático del paso 4.

**Criterios de aceptación**
- [ ] Crear respaldo, borrar un cliente, restaurar: el cliente vuelve.
- [ ] Un archivo que no es una base válida es rechazado con un mensaje claro.
- [ ] El respaldo abre correctamente en "DB Browser for SQLite".

### Bloque 6: Pedidos conectados a la cuenta del cliente
Requiere Migration v3→v4 (tabla `Pedido` y `pedidoId` en `Deuda`).

**Diseño:** el pedido **genera un cargo** en la cuenta del cliente. Los abonos siguen aplicándose al total del cliente (como ahora). Así el cálculo de saldos no cambia.

1. Pantalla **Pedidos**: lista con filtro por estado (Recibido · En producción · Listo · Entregado) y buscador.
2. Formulario de pedido: cliente (seleccionar o crear), producto, cantidad, precio unitario (el total se calcula solo), abono inicial opcional, número de cuotas y frecuencia (si queda saldo), fecha de entrega, notas.
3. Al guardar, **en una sola transacción de Room** (`@Transaction`): insertar `Pedido`, insertar el cargo en `Deuda` con `pedidoId`, e insertar el abono inicial si existe. Si algo falla, no se guarda nada.
4. Cambiar el estado desde la tarjeta del pedido. Al pasar a **LISTO**, ofrecer el botón "Avisar por WhatsApp" con mensaje "Tu pedido está listo, saldo pendiente: X".
5. Editar un pedido ajusta el cargo vinculado con la misma lógica de diferencia que ya existe para editar deudas.
6. Navegación: barra inferior **Inicio · Pedidos · Cotizar · Resumen**, y el "+" de agregar pasa a ser un botón flotante.

**Criterios de aceptación**
- [ ] Crear un pedido deja al cliente con el saldo correcto en Inicio.
- [ ] Un cliente puede tener varios pedidos y su saldo total es la suma.
- [ ] Un fallo a mitad de guardado no deja datos a medias.

### Bloque 7: Cotizador
1. Campos: producto, costo de la pieza en blanco, costo de tinta, costo de papel, otros costos, minutos de prensa, cantidad, margen deseado (%).
2. En Ajustes: `costoMinutoPrensaUsd` (energía y desgaste).
3. Fórmulas:
   - `costoUnitario = blanco + tinta + papel + otros + (minutosPrensa × costoMinutoPrensa)`
   - `precioUnitario = costoUnitario / (1 − margen)` (margen sobre el precio de venta; con `margen = 0,40` ganas el 40 % de lo que cobras)
   - `total = precioUnitario × cantidad`; `ganancia = total − costoUnitario × cantidad`
4. Mostrar la tabla de precios para 1, 12 y 50 piezas, con **descuento por volumen editable** (por ejemplo 0 %, 5 %, 10 %).
5. Mostrar el equivalente en Bs con la tasa del día.
6. Botones: **"Compartir cotización"** (texto armado para WhatsApp) y **"Convertir en pedido"** (abre el formulario de pedido con los datos cargados).
7. Opcional: guardar plantillas de costos por producto (tabla `PlantillaCosto`) para no reescribir todo cada vez.

**Criterios de aceptación**
- [ ] Con costos conocidos, el resultado coincide con el cálculo hecho a mano.
- [ ] Margen igual o mayor a 100 % se rechaza con un mensaje (división por cero).
- [ ] La cotización se convierte en pedido sin reescribir datos.

### Bloque 8: Resumen del negocio
1. Tarjetas: total por cobrar (USD y Bs), **cobrado este mes** (suma de abonos del mes, usando `fechaMillis`), clientes con cuotas vencidas, pedidos pendientes de entrega.
2. Lista "Top 5 clientes que más deben".
3. Selector de mes para ver meses anteriores.

**Criterios de aceptación**
- [ ] Las cifras coinciden con la suma manual de los datos de prueba.

---

## 8. Prompts para el agente

### Prompt maestro (pegar al inicio de cada sesión)

```
Estás trabajando en la app Android "Gestor de Deudores" (Kotlin, Jetpack Compose,
MVVM, Room). La app YA está instalada en celulares con datos reales de usuarios.

REGLAS OBLIGATORIAS:
1. Lee primero entity.kt, dao.kt, DeudaDataBase.kt y los ViewModels relevantes antes
   de proponer cambios. Usa los nombres reales de tablas, columnas y clases.
2. NUNCA uses fallbackToDestructiveMigration. Si existe, elimínalo y avísame.
3. Todo cambio de estructura de base de datos requiere: subir la versión de @Database,
   una Migration(vieja, nueva) con ALTER TABLE ... ADD COLUMN (con DEFAULT si es NOT NULL),
   registrada en addMigrations, y exportSchema = true.
4. No renombres ni borres columnas ni tablas existentes. No cambies el nombre de la base
   de datos ni el applicationId.
5. Mantén la matemática actual: los abonos reducen el saldo y el saldo es la suma del
   historial. No cambies esa lógica sin avisarme.
6. Todo cálculo con dinero se redondea a 2 decimales con una función única.
7. Hazlo por bloques pequeños. Al terminar cada bloque muéstrame: archivos modificados,
   la migración y cómo probar. No avances al siguiente bloque sin que yo confirme.
8. Si algo es ambiguo, pregúntame antes de asumir.
```

### Prompt por bloque (ejemplo para el Bloque 1; repite la estructura con los demás)

```
Bloque 1 del documento PLAN_MEJORAS_GESTOR_DEUDORES.md: textos, archivado y validaciones.
Haz solo esto:
- Cambia títulos y etiquetas visibles según la sección 4 (sin tocar nombres internos).
- Quita el selector de Rol de la UI (conserva la columna; escribe un valor fijo al guardar).
- Campo "Producto o pedido" con sugerencias a partir de los valores existentes.
- Teléfono obligatorio con validación, y selector V/E/J para la cédula.
- Reemplaza el borrado por archivado (Deudor.archivado) con diálogo de confirmación,
  y una opción "Ver archivados" con Restaurar.
- Crea MIGRATION_1_2 con las columnas de la versión 2 de la sección 3 (tipo, numCuotas,
  frecuencia si falta, fechaMillis con relleno, archivado), sube la versión a 2 y verifica
  que no exista fallbackToDestructiveMigration.
Al final: lista de archivos cambiados y los pasos para probar la migración sobre datos reales.
```

---

## 9. Lista de pruebas manuales por cada versión

Antes de instalar en el celular real:

- [ ] Instalé la versión anterior en el emulador y cargué datos de prueba.
- [ ] Actualicé encima (sin desinstalar) con la versión nueva.
- [ ] Los clientes, saldos y el total por cobrar coinciden con lo anotado antes.
- [ ] Registrar un cliente nuevo con adelanto y cuotas funciona.
- [ ] Registrar un abono desde el botón "+" funciona y baja el saldo.
- [ ] Editar una deuda ajusta el saldo sin afectar los abonos.
- [ ] Buscador y pestañas Pendientes / Historial funcionan.
- [ ] Cierro y reabro la app: nada se pierde.
- [ ] Hice un respaldo y pude abrirlo en DB Browser.
- [ ] Hice commit y push de la rama `mejoras-negocio`.

---

## 10. Orden recomendado y metas

| Orden | Bloque | Versión de BD | Resultado |
|---|---|---|---|
| 1 | Bloques 1 y 2 | v2 | App con lenguaje de negocio y vencimientos. **Ya se puede grabar el video y ofrecerla.** |
| 2 | Bloque 3 | sin cambio | Recordatorio por WhatsApp. El gancho de venta. |
| 3 | Bloque 5 | sin cambio | Respaldo: permite entregar la app con tranquilidad. |
| 4 | Bloque 4 | v3 | Doble moneda. |
| 5 | Bloques 6 y 8 | v4 | Pedidos y resumen. |
| 6 | Bloque 7 | v4 | Cotizador (el diferencial para sublimación). |

**Mínimo para empezar a vender:** Bloques 1, 2, 3 y 5. Las demás funciones se pueden ofrecer como ampliación para quien las pida.

**Para el video de demostración (45 a 60 segundos):** registrar un cliente con adelanto, mostrar la tarjeta con "próxima cuota" y etiqueta de vencida, pulsar WhatsApp y mostrar el mensaje armado, mostrar el respaldo.

---

## 11. Qué NO hacer (resumen)

- No desinstalar la app del celular antes de actualizar.
- No cambiar `applicationId`, el nombre de la base de datos ni la keystore.
- No usar `fallbackToDestructiveMigration`.
- No cambiar columnas existentes; solo agregar.
- No hacer varios bloques a la vez sin probar y sin commit entre ellos.
- No subir keystores ni `local.properties` a GitHub.
- No instalar la versión nueva en el celular real sin haberla probado antes sobre datos de prueba y sin tener el respaldo de la base.
