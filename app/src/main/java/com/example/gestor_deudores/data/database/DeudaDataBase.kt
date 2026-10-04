package com.example.gestor_deudores.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Deudor::class, Deuda::class, Pedido::class, PlantillaCotizacion::class, CuentaBancaria::class],
    version = 9,
    exportSchema = true
)
abstract class DeudaDataBase : RoomDatabase() {

    abstract fun deudorDao(): DeudorDao
    abstract fun deudaDao(): DeudaDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun plantillaDao(): PlantillaDao
    abstract fun cuentaBancariaDao(): CuentaBancariaDao

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
                // Room espera la creación limpia de la columna nombrePiezaBase en un orden o string específico, 
                // pero si la DB ya estaba instalada, usaremos ADD COLUMN simple con literales simples
                db.execSQL("ALTER TABLE pedidos ADD COLUMN costoPiezaBaseUsd REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE pedidos ADD COLUMN costoPasajeUsd REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE pedidos ADD COLUMN costoInsumosUsd REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE pedidos ADD COLUMN nombrePiezaBase TEXT NOT NULL DEFAULT 'Insumo Base'")
            }
        }

        fun getDatabase(context: Context): DeudaDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeudaDataBase::class.java,
                    "control_deudas_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
