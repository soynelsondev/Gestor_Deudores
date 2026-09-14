# Documentación del Proyecto: Gestor de Deudores

## 1. ¿De qué trata el proyecto? (Propósito)
**Gestor de Deudores** es una aplicación móvil para Android diseñada para ayudar a comerciantes, prestamistas o emprendedores a llevar un control preciso, digital y organizado de sus cuentas por cobrar. 

La aplicación permite registrar clientes (deudores), asignarles préstamos o créditos por mercancía, y registrar los pagos parciales o totales (abonos) que realizan a lo largo del tiempo. Su principal ventaja es que automatiza toda la matemática financiera: en lugar de tener un número estático, el sistema calcula el saldo restante de cada persona sumando y restando dinámicamente todo su historial de transacciones.

## 2. Estado y Alcance de la Versión Actual
Hasta esta versión, el proyecto se encuentra en un estado funcional, estable y con un flujo completo para la gestión diaria de cobranzas. Las características implementadas y operativas son:

* **Gestión de Perfiles:** Creación, edición y eliminación (con borrado en cascada) de perfiles de clientes, incluyendo nombre, apellido, cédula y teléfono.
* **Sistema de Transacciones:** Registro de deudas iniciales (monto, tipo, cuotas, fecha) con soporte para "abonos iniciales / adelantos" en el mismo paso.
* **Abonos Rápidos:** Capacidad de registrar pagos parciales rápidamente desde un botón en la pantalla principal.
* **Cálculo Matemático Dinámico:** El sistema interpreta los abonos como "deudas con valor negativo". Al sumar todo el historial, se obtiene el monto exacto restante sin riesgo de desajustes.
* **Dashboard Principal:** 
    * Buscador en tiempo real por nombre o apellido.
    * Tarjeta de Resumen Global: Muestra cuántas personas deben dinero y el gran total de capital "en la calle".
    * Pestañas inteligentes: Separa a los clientes en "Pendientes" (aún deben dinero) e "Historial" (ya pagaron todo).
* **Historial Detallado:** Pantalla individual por cliente que lista cronológicamente todos sus movimientos, diferenciando visualmente los cargos (en negro/rojo) de los pagos (en verde).
* **Edición Protegida:** Un flujo de edición de dos fases (Editar Persona -> Editar Deuda) con medidas de seguridad en la UI que bloquean la creación accidental de nuevos abonos al editar, y ajustan matemáticamente el saldo restante si se cambia el monto del préstamo original.

---

## 3. Arquitectura y Estructura del Proyecto

El proyecto sigue el patrón de arquitectura **MVVM (Model-View-ViewModel)** recomendado por Google para Android, usando **Jetpack Compose** para la interfaz de usuario (UI) y **Room** para la base de datos local.

### Estructura de Paquetes
*   `com.example.gestor_deudores` (Raíz): Contiene el `MainActivity` y la configuración de navegación (`navegacion.kt`).
*   `data`: Contiene todo lo relacionado a la base de datos Room (`DeudaDataBase.kt`, `entity.kt`, `dao.kt`).
*   `ui.home`: Contiene la vista principal (lista de deudores), historial y su ViewModel asociado.
*   `ui.Registro`: Contiene la vista y ViewModel para registrar/editar la información personal del **Deudor**.
*   `ui.registroDeuda`: Contiene la vista y ViewModel para registrar/editar la **Deuda** en sí misma, así como los abonos iniciales.
*   `ui.theme`: Contiene paleta de colores, tipografías y componentes UI reutilizables.

## 4. Modelos de Base de Datos (`entity.kt`)

El proyecto usa dos tablas principales relacionadas (1 a Muchos):

*   **`Deudor`:** Almacena la información del cliente (nombre, apellido, cédula, teléfono).
*   **`Deuda`:** Almacena el registro financiero (monto, fecha, descripción, rol). 
    *   *Nota Clave:* Esta tabla se usa tanto para **Préstamos/Mercancía** como para **Abonos/Pagos**.
    *   **Identificador de Abonos:** Si un registro tiene `rol = "PAGO"` o un `montoRestante` negativo/menor a cero, el sistema lo interpreta como un abono.

## 5. Lógica Matemática Crítica

### A. Cálculo del Total Adeudado (`HomeViewModel.kt` y `dao.kt`)
La deuda total de una persona NO se guarda como una variable estática en el `Deudor`. Se calcula de forma dinámica en tiempo real sumando todos los registros de su historial:
*   Si Juan pide \$100, se guarda una `Deuda` con `montoRestante = 100`.
*   Si Juan abona \$20, se guarda un recibo (Deuda) con `montoRestante = -20`.
*   El sistema suma: `100 + (-20) = 80`. El total de la deuda es \$80.

### B. Actualización en Modo Edición (`RDeudaViewModel.kt`)
Si un usuario se equivoca al registrar una deuda inicial (Ej: puso \$50 pero eran \$100), y luego va a editarla, la matemática es:
1.  Se busca el registro viejo.
2.  Se calcula la diferencia: `Nuevo Monto (100) - Monto Inicial Viejo (50) = Diferencia (50)`.
3.  Se le suma esa diferencia al `montoRestante` actual, para no afectar los abonos que el cliente ya haya hecho.

## 6. Flujos Importantes de la UI

### Flujo de Navegación (`navegacion.kt` & `rutas.kt`)
El sistema usa `NavHost` de Jetpack Compose.
*   **Modo Creación:** `Home` -> `Registro (Deudor)` -> `RegistroDeuda (Deuda)` -> `Home`.
*   **Modo Edición:** `Home` (Botón Lápiz) -> `Registro (Editar Deudor)` -> `RegistroDeuda (Editar Deuda)` -> `Home`.
*   Las rutas de edición pasan los IDs por la URL: `"$REGISTRO/{id}/{idDeuda}"`.

### Interfaz Segura en Edición (`registroDeudaView.kt`)
Para evitar corrupción de datos, si la pantalla de deuda detecta que está en "Modo Edición" (`idDeudaActual > 0`), **oculta automáticamente** los campos de "Abono Inicial" y "Nº de Cuotas". Los abonos posteriores siempre deben hacerse desde el botón `+` en la tarjeta principal (Home).

## 7. Cambios y Correcciones Realizadas (Historial de Parches)

A continuación, el historial de las correcciones implementadas por el asistente AI para reparar el flujo de edición:

1.  **Activación del Flujo de Edición Completo:** 
    *   Se crearon las rutas faltantes en `rutas.kt` y `navegacion.kt` para permitir que, al terminar de editar el Deudor, la app salte a editar la Deuda (antes solo volvía al inicio).
    *   Se ajustó el botón del lápiz en `homeView.kt` para enviar tanto el ID del Deudor como el ID de la Deuda.
2.  **Soporte de Lectura en Base de Datos:**
    *   Se agregó la consulta `obtenerDeudaPorId` en `dao.kt` para que el sistema pudiera buscar la deuda específica a editar.
3.  **Lógica de Edición en `RDeudaViewModel.kt`:**
    *   Se agregó la función `cargarDeuda()` para rellenar los campos de texto con los datos guardados.
    *   Se dividió la función `GuardarDeuda()` en un bloque `if/else` (Creación vs Edición) aplicando la matemática de "Diferencia" explicada en la sección 5B.
4.  **Corrección del Bug del Abono Fantasma (0.0):**
    *   En `HomeViewModel.kt`, se cambió la forma de elegir la "deuda principal" para que ignore los recibos de abono (`lastOrNull { it.rol != "PAGO" }`). Esto aseguró que el botón Editar abriera la deuda real y no un recibo de pago de \$0.0.
    *   Se eliminó la creación de nuevos abonos desde la lógica de edición en el ViewModel.
5.  **Refinamiento de UI en Edición (`registroDeudaView.kt`):**
    *   Se ocultaron condicionalmente los campos de texto de "Abono Inicial" y "Cuotas" si la pantalla se abre en modo edición, bloqueando manipulaciones erróneas del usuario.

## 8. Guía para Futuras Modificaciones

*   **¿Quieres agregar nuevos datos a la persona (Ej: Dirección)?:** 
    1. Agrégalo en `Deudor` (`entity.kt`). 
    2. Actualiza los campos de texto en `registroView.kt`. 
    3. Modifica `RegistroDeudorViewModel.kt` para recibir y guardar la dirección.
*   **¿Quieres modificar el cálculo de la deuda global?:** Revisa la función `obtenerSumaDeudasPorDeudor` en `dao.kt` o el mapeo de `deudorFiltrados` en `HomeViewModel.kt`.
*   **¿Quieres implementar notificaciones de cobro a futuro?:** Tendrás que migrar la propiedad `cuotas` (que es solo texto descriptivo) a un modelo de Base de Datos formal (Tabla `Cuota`), y usar **Android WorkManager** junto con **NotificationCompat** para revisar diariamente qué cuota tiene fecha de vencimiento igual a "Hoy".
