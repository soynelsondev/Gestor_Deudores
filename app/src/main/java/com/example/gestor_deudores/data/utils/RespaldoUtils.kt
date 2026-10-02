package com.example.gestor_deudores.data.utils

import android.content.Context
import android.net.Uri
import com.example.gestor_deudores.data.database.DeudaDataBase
import java.io.FileInputStream
import java.io.FileOutputStream

object RespaldoUtils {

    fun exportarBaseDatos(context: Context, uriDestino: Uri): Boolean {
        return try {
            val db = DeudaDataBase.getDatabase(context)
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath("control_deudas_db")
            if (!dbFile.exists()) return false

            context.contentResolver.openOutputStream(uriDestino)?.use { outputStream ->
                FileInputStream(dbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun esBaseDatosValida(context: Context, uriOrigen: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uriOrigen)?.use { inputStream ->
                val header = ByteArray(16)
                val bytesRead = inputStream.read(header)
                if (bytesRead < 16) return false
                val headerString = String(header, Charsets.US_ASCII)
                headerString.startsWith("SQLite format 3")
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun importarBaseDatos(context: Context, uriOrigen: Uri): Boolean {
        if (!esBaseDatosValida(context, uriOrigen)) return false

        return try {
            val dbFile = context.getDatabasePath("control_deudas_db")
            val walFile = context.getDatabasePath("control_deudas_db-wal")
            val shmFile = context.getDatabasePath("control_deudas_db-shm")

            DeudaDataBase.getDatabase(context).close()

            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            context.contentResolver.openInputStream(uriOrigen)?.use { inputStream ->
                FileOutputStream(dbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
