package com.example.gestor_deudores.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Deudor::class, Deuda::class, Pedido::class, PlantillaCotizacion::class],
    version = 4,
    exportSchema = true
)
abstract class DeudaDataBase : RoomDatabase() {

    abstract fun deudorDao(): DeudorDao
    abstract fun deudaDao(): DeudaDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun plantillaDao(): PlantillaDao

    companion object {
        @Volatile
        private var INSTANCE: DeudaDataBase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE deudores ADD COLUMN archivado INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN tipo TEXT NOT NULL DEFAULT 'CARGO'")
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN numCuotas INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN frecuencia TEXT NOT NULL DEFAULT 'MENSUAL'")
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN fechaMillis INTEGER NOT NULL DEFAULT 0")
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

        fun getDatabase(context: Context): DeudaDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeudaDataBase::class.java,
                    "control_deudas_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
