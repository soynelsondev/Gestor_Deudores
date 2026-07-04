package com.example.gestor_deudores.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [Deudor::class, Deuda::class],
    version = 1)

abstract class DeudaDataBase: RoomDatabase(){

    abstract fun deudorDao(): DeudorDao
    abstract fun deudaDao(): DeudaDao

    companion object{
        @Volatile
        private var INSTANCE: DeudaDataBase? = null

        fun getDatabase(context: Context): DeudaDataBase{
            return   INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeudaDataBase::class.java,
                   "control_deudas_db"
                )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}