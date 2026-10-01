package com.example.gestor_deudores.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [Deudor::class, Deuda::class],
    version = 2,
    exportSchema = true
)

abstract class DeudaDataBase: RoomDatabase(){

    abstract fun deudorDao(): DeudorDao
    abstract fun deudaDao(): DeudaDao

    companion object{
        @Volatile
        private var INSTANCE: DeudaDataBase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Agregar columnas a Deudor
                db.execSQL("ALTER TABLE deudores ADD COLUMN archivado INTEGER NOT NULL DEFAULT 0")

                // Agregar columnas a Tabla_Deuda
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN tipo TEXT NOT NULL DEFAULT 'CARGO'")
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN numCuotas INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN frecuencia TEXT NOT NULL DEFAULT 'MENSUAL'")
                db.execSQL("ALTER TABLE Tabla_Deuda ADD COLUMN fechaMillis INTEGER NOT NULL DEFAULT 0")

                // Actualizar tipo de las deudas existentes (abonos vs cargos)
                db.execSQL("UPDATE Tabla_Deuda SET tipo = 'ABONO' WHERE rol = 'PAGO' OR montoRestante < 0")
            }
        }

        fun getDatabase(context: Context): DeudaDataBase{
            return   INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeudaDataBase::class.java,
                   "control_deudas_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}