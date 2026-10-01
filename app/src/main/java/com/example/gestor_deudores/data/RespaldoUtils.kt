package com.example.gestor_deudores.data

import android.content.Context
import android.net.Uri
import java.io.FileInputStream
import java.io.FileOutputStream

object RespaldoUtils {

    // 1. Exportar la base de datos actual a un archivo seleccionado por el usuario
    fun exportarBaseDatos(context: Context, uriDestino: Uri): Boolean {
        return try {
            val db = DeudaDataBase.getDatabase(context)
            
            // Forzamos a Room a volcar la memoria temporal (archivos WAL) al archivo principal
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

    // 2. Validar si un archivo seleccionado es realmente una base de datos SQLite válida
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

    // 3. Reemplazar la base de datos actual con la del archivo seleccionado
    fun importarBaseDatos(context: Context, uriOrigen: Uri): Boolean {
        if (!esBaseDatosValida(context, uriOrigen)) return false

        return try {
            val dbFile = context.getDatabasePath("control_deudas_db")
            val walFile = context.getDatabasePath("control_deudas_db-wal")
            val shmFile = context.getDatabasePath("control_deudas_db-shm")

            // Cerramos la conexión activa de Room para liberar el archivo antes de reemplazarlo
            DeudaDataBase.getDatabase(context).close()

            // Borramos los archivos temporales viejos para que no interfieran con los nuevos datos
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // Copiamos el archivo seleccionado encima del archivo principal de la app
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
