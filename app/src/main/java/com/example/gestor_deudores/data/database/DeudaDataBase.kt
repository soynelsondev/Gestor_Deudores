package com.example.gestor_deudores.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Deudor::class, Deuda::class, Pedido::class, PlantillaCotizacion::class, CuentaBancaria::class, PerfilNegocio::class, InsumoBiblioteca::class, HistorialPrecioInsumo::class],
    version = 22,
    exportSchema = true
)
abstract class DeudaDataBase : RoomDatabase() {

    abstract fun deudorDao(): DeudorDao
    abstract fun deudaDao(): DeudaDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun plantillaDao(): PlantillaDao
    abstract fun cuentaBancariaDao(): CuentaBancariaDao
    abstract fun perfilNegocioDao(): PerfilNegocioDao
    abstract fun insumoBibliotecaDao(): InsumoBibliotecaDao
    abstract fun historialPrecioInsumoDao(): HistorialPrecioInsumoDao

    companion object {
        @Volatile
        private var INSTANCE: DeudaDataBase? = null

        private fun addColumnIfNotExists(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnSql: String) {
            val cursor = db.query("PRAGMA table_info(`$tableName`)")
            var exists = false
            try {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex != -1 && cursor.getString(nameIndex) == columnName) {
                        exists = true
                        break
                    }
                }
            } finally {
                cursor.close()
            }
            if (!exists) {
                db.execSQL("ALTER TABLE `$tableName` ADD COLUMN $columnSql")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "deudores", "archivado", "`archivado` INTEGER NOT NULL DEFAULT 0")
                addColumnIfNotExists(db, "Tabla_Deuda", "tipo", "`tipo` TEXT NOT NULL DEFAULT 'CARGO'")
                addColumnIfNotExists(db, "Tabla_Deuda", "numCuotas", "`numCuotas` INTEGER NOT NULL DEFAULT 1")
                addColumnIfNotExists(db, "Tabla_Deuda", "frecuencia", "`frecuencia` TEXT NOT NULL DEFAULT 'MENSUAL'")
                addColumnIfNotExists(db, "Tabla_Deuda", "fechaMillis", "`fechaMillis` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE Tabla_Deuda SET tipo = 'ABONO' WHERE rol = 'PAGO' OR montoRestante < 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pedidos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `deudorId` INTEGER NOT NULL,
                        `producto` TEXT NOT NULL,
                        `cantidad` INTEGER NOT NULL,
                        `precioUnitarioUsd` REAL NOT NULL,
                        `totalUsd` REAL NOT NULL,
                        `fechaCreacionMillis` INTEGER NOT NULL,
                        `fechaEntregaMillis` INTEGER NOT NULL,
                        `estado` TEXT NOT NULL,
                        `notas` TEXT NOT NULL,
                        FOREIGN KEY(`deudorId`) REFERENCES `deudores`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pedidos_deudorId` ON `pedidos` (`deudorId`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `plantillas_cotizacion` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nombrePlantilla` TEXT NOT NULL,
                        `precioPaquetePieza` REAL NOT NULL,
                        `monedaPaquetePieza` TEXT NOT NULL,
                        `cantidadPaquetePieza` INTEGER NOT NULL,
                        `precioPaqueteEmpaque` REAL NOT NULL,
                        `monedaPaqueteEmpaque` TEXT NOT NULL,
                        `cantidadPaqueteEmpaque` INTEGER NOT NULL,
                        `precioTotalDtf` REAL NOT NULL,
                        `monedaDtf` TEXT NOT NULL,
                        `rendimientoDtf` INTEGER NOT NULL,
                        `costoTransporte` REAL NOT NULL,
                        `monedaTransporte` TEXT NOT NULL,
                        `costoDiseno` REAL NOT NULL,
                        `monedaDiseno` TEXT NOT NULL,
                        `porcentajeOperativo` REAL NOT NULL,
                        `porcentajeGananciaDetal` REAL NOT NULL,
                        `porcentajeGananciaMayor` REAL NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "plantillas_cotizacion", "rendimientoTransporte", "`rendimientoTransporte` INTEGER NOT NULL DEFAULT 1")
                addColumnIfNotExists(db, "plantillas_cotizacion", "rendimientoDiseno", "`rendimientoDiseno` INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "plantillas_cotizacion", "precioPaquetePapel", "`precioPaquetePapel` REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "plantillas_cotizacion", "monedaPaquetePapel", "`monedaPaquetePapel` TEXT NOT NULL DEFAULT 'USD'")
                addColumnIfNotExists(db, "plantillas_cotizacion", "cantidadPaquetePapel", "`cantidadPaquetePapel` INTEGER NOT NULL DEFAULT 100")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "plantillas_cotizacion", "costoExtra", "`costoExtra` REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "plantillas_cotizacion", "monedaExtra", "`monedaExtra` TEXT NOT NULL DEFAULT 'USD'")
                addColumnIfNotExists(db, "plantillas_cotizacion", "rendimientoExtra", "`rendimientoExtra` INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cuentas_bancarias` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `moneda` TEXT NOT NULL,
                        `saldoActual` REAL NOT NULL
                    )
                """)
                db.execSQL("INSERT INTO `cuentas_bancarias` (`nombre`, `tipo`, `moneda`, `saldoActual`) VALUES ('Efectivo Caja Chica', 'EFECTIVO', 'USD', 0.0)")
                db.execSQL("INSERT INTO `cuentas_bancarias` (`nombre`, `tipo`, `moneda`, `saldoActual`) VALUES ('Zelle / Dólares Digitales', 'BILLETERA', 'USD', 0.0)")
                db.execSQL("INSERT INTO `cuentas_bancarias` (`nombre`, `tipo`, `moneda`, `saldoActual`) VALUES ('Pago Móvil / Banco', 'BANCO', 'VES', 0.0)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "pedidos", "costoPiezaBaseUsd", "`costoPiezaBaseUsd` REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "pedidos", "costoPasajeUsd", "`costoPasajeUsd` REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "pedidos", "costoInsumosUsd", "`costoInsumosUsd` REAL NOT NULL DEFAULT 0.0")
                addColumnIfNotExists(db, "pedidos", "nombrePiezaBase", "`nombrePiezaBase` TEXT NOT NULL DEFAULT 'Insumo Base'")
            }
        }

        private fun recrearTablaPlantillasSegura(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `plantillas_cotizacion_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `nombrePlantilla` TEXT NOT NULL,
                    `precioPaquetePieza` REAL NOT NULL DEFAULT 0.0,
                    `monedaPaquetePieza` TEXT NOT NULL DEFAULT 'USD',
                    `cantidadPaquetePieza` INTEGER NOT NULL DEFAULT 1,
                    `precioPaqueteEmpaque` REAL NOT NULL DEFAULT 0.0,
                    `monedaPaqueteEmpaque` TEXT NOT NULL DEFAULT 'USD',
                    `cantidadPaqueteEmpaque` INTEGER NOT NULL DEFAULT 1,
                    `precioPaquetePapel` REAL NOT NULL DEFAULT 0.0,
                    `monedaPaquetePapel` TEXT NOT NULL DEFAULT 'USD',
                    `cantidadPaquetePapel` INTEGER NOT NULL DEFAULT 100,
                    `precioTotalDtf` REAL NOT NULL DEFAULT 0.0,
                    `monedaDtf` TEXT NOT NULL DEFAULT 'USD',
                    `rendimientoDtf` INTEGER NOT NULL DEFAULT 1,
                    `costoTransporte` REAL NOT NULL DEFAULT 0.0,
                    `monedaTransporte` TEXT NOT NULL DEFAULT 'USD',
                    `rendimientoTransporte` INTEGER NOT NULL DEFAULT 1,
                    `costoDiseno` REAL NOT NULL DEFAULT 0.0,
                    `monedaDiseno` TEXT NOT NULL DEFAULT 'USD',
                    `rendimientoDiseno` INTEGER NOT NULL DEFAULT 1,
                    `costoExtra` REAL NOT NULL DEFAULT 0.0,
                    `monedaExtra` TEXT NOT NULL DEFAULT 'USD',
                    `rendimientoExtra` INTEGER NOT NULL DEFAULT 1,
                    `porcentajeOperativo` REAL NOT NULL DEFAULT 10.0,
                    `porcentajeGanancia` REAL NOT NULL DEFAULT 40.0,
                    `esPlantillaMayor` INTEGER NOT NULL DEFAULT 0,
                    `minimoUnidadesMayor` INTEGER NOT NULL DEFAULT 6,
                    `costosAdicionalesJson` TEXT NOT NULL DEFAULT '[]',
                    `minutosPorPieza` REAL NOT NULL DEFAULT 0.0,
                    `tarifaPorHoraUsd` REAL NOT NULL DEFAULT 0.0,
                    `comisionPorcentaje` REAL NOT NULL DEFAULT 0.0,
                    `escalasPrecioJson` TEXT NOT NULL DEFAULT '[]',
                    `modoCosteo` TEXT NOT NULL DEFAULT 'RAPIDO'
                )
            """)

            try {
                val cursor = db.query("PRAGMA table_info(`plantillas_cotizacion`)")
                val columnasExistentes = mutableSetOf<String>()
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIdx != -1) {
                        columnasExistentes.add(cursor.getString(nameIdx))
                    }
                }
                cursor.close()

                if (columnasExistentes.isNotEmpty()) {
                    val colGanancia = if (columnasExistentes.contains("porcentajeGanancia")) "porcentajeGanancia" 
                                     else if (columnasExistentes.contains("porcentajeGananciaDetal")) "porcentajeGananciaDetal" 
                                     else "40.0"

                    val colsToCopy = listOf(
                        "id", "nombrePlantilla", "precioPaquetePieza", "monedaPaquetePieza", "cantidadPaquetePieza",
                        "precioPaqueteEmpaque", "monedaPaqueteEmpaque", "cantidadPaqueteEmpaque",
                        "precioPaquetePapel", "monedaPaquetePapel", "cantidadPaquetePapel",
                        "precioTotalDtf", "monedaDtf", "rendimientoDtf",
                        "costoTransporte", "monedaTransporte", "rendimientoTransporte",
                        "costoDiseno", "monedaDiseno", "rendimientoDiseno",
                        "costoExtra", "monedaExtra", "rendimientoExtra", "porcentajeOperativo",
                        "esPlantillaMayor", "minimoUnidadesMayor", "costosAdicionalesJson",
                        "minutosPorPieza", "tarifaPorHoraUsd", "comisionPorcentaje", "escalasPrecioJson", "modoCosteo"
                    ).filter { columnasExistentes.contains(it) }

                    val selectColsSql = (colsToCopy + "$colGanancia AS porcentajeGanancia").joinToString(", ")
                    val insertColsSql = (colsToCopy + "porcentajeGanancia").joinToString(", ")

                    db.execSQL("INSERT INTO `plantillas_cotizacion_new` ($insertColsSql) SELECT $selectColsSql FROM `plantillas_cotizacion`")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            db.execSQL("DROP TABLE IF EXISTS `plantillas_cotizacion`")
            db.execSQL("ALTER TABLE `plantillas_cotizacion_new` RENAME TO `plantillas_cotizacion`")
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "pedidos", "costosAdicionalesJson", "`costosAdicionalesJson` TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPlantillasSegura(db)
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "pedidos", "costosAdicionalesJson", "`costosAdicionalesJson` TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPlantillasSegura(db)
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPlantillasSegura(db)
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPlantillasSegura(db)
            }
        }

        private fun recrearTablaPerfilSegura(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `perfil_negocio_new` (
                    `id` INTEGER PRIMARY KEY NOT NULL,
                    `nombreNegocio` TEXT NOT NULL DEFAULT 'Mi Taller de Personalización',
                    `rifCedula` TEXT NOT NULL DEFAULT '',
                    `telefonoContacto` TEXT NOT NULL DEFAULT '',
                    `eslogan` TEXT NOT NULL DEFAULT '',
                    `saludoCobro` TEXT NOT NULL DEFAULT '¡Hola! Te escribimos con mucho gusto de parte de nuestro taller.',
                    `cierreCobro` TEXT NOT NULL DEFAULT 'Por favor indícanos cuándo podrías realizar el pago. ¡Muchas gracias!',
                    `saludoListo` TEXT NOT NULL DEFAULT '¡Hola! Te tenemos excelentes noticias de parte de nuestro taller.',
                    `cierreListo` TEXT NOT NULL DEFAULT 'Puedes pasar retirando tu pedido en nuestro horario habitual. ¡Te esperamos!'
                )
            """)

            try {
                val cursor = db.query("PRAGMA table_info(`perfil_negocio`)")
                val columnas = mutableSetOf<String>()
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIdx != -1) {
                        columnas.add(cursor.getString(nameIdx))
                    }
                }
                cursor.close()

                if (columnas.isNotEmpty()) {
                    val colsToCopy = listOf(
                        "id", "nombreNegocio", "rifCedula", "telefonoContacto", "eslogan",
                        "saludoCobro", "cierreCobro", "saludoListo", "cierreListo"
                    ).filter { columnas.contains(it) }

                    if (colsToCopy.isNotEmpty()) {
                        val colsSql = colsToCopy.joinToString(", ")
                        db.execSQL("INSERT INTO `perfil_negocio_new` ($colsSql) SELECT $colsSql FROM `perfil_negocio`")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            db.execSQL("""
                INSERT OR IGNORE INTO `perfil_negocio_new` (`id`, `nombreNegocio`, `rifCedula`, `telefonoContacto`, `eslogan`, `saludoCobro`, `cierreCobro`, `saludoListo`, `cierreListo`)
                VALUES (1, 'Mi Taller de Personalización', '', '', '', '¡Hola! Te escribimos con mucho gusto de parte de nuestro taller.', 'Por favor indícanos cuándo podrías realizar el pago. ¡Muchas gracias!', '¡Hola! Te tenemos excelentes noticias de parte de nuestro taller.', 'Puedes pasar retirando tu pedido en nuestro horario habitual. ¡Te esperamos!')
            """)

            db.execSQL("DROP TABLE IF EXISTS `perfil_negocio`")
            db.execSQL("ALTER TABLE `perfil_negocio_new` RENAME TO `perfil_negocio`")
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPerfilSegura(db)
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPerfilSegura(db)
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPerfilSegura(db)
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPlantillasSegura(db)
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `biblioteca_insumos` (
                        `id` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL DEFAULT '',
                        `categoria` TEXT NOT NULL DEFAULT 'MATERIA_PRIMA',
                        `unidadLote` TEXT NOT NULL DEFAULT 'Piezas',
                        `precioLote` REAL NOT NULL DEFAULT 0.0,
                        `cantidadLote` INTEGER NOT NULL DEFAULT 1,
                        `moneda` TEXT NOT NULL DEFAULT 'USD',
                        `fechaActualizacion` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recrearTablaPlantillasSegura(db)
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `historial_precios_insumo` (
                        `id` TEXT NOT NULL,
                        `insumoId` TEXT NOT NULL DEFAULT '',
                        `precioLote` REAL NOT NULL DEFAULT 0.0,
                        `cantidadLote` INTEGER NOT NULL DEFAULT 1,
                        `fecha` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`id`)
                    )
                """)
                recrearTablaPlantillasSegura(db)
            }
        }

        fun getDatabase(context: Context): DeudaDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeudaDataBase::class.java,
                    "control_deudas_db"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, 
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, 
                        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                        MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                        MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
