package com.example.gestor_deudores.data.utils

import android.content.Context
import android.net.Uri
import com.example.gestor_deudores.data.database.DeudaDataBase
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object RespaldoUtils {

    fun exportarBaseDatos(context: Context, uriDestino: Uri): Boolean {
        return exportarRespaldoZip(context, uriDestino, incluirFotos = true)
    }

    fun exportarRespaldoZip(context: Context, uriDestino: Uri, incluirFotos: Boolean = true): Boolean {
        return try {
            val db = DeudaDataBase.getDatabase(context)
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath("control_deudas_db")
            if (!dbFile.exists()) return false

            context.contentResolver.openOutputStream(uriDestino)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // 1. Agregar Base de Datos al .zip
                    val dbEntry = ZipEntry("database/control_deudas_db")
                    zipOut.putNextEntry(dbEntry)
                    FileInputStream(dbFile).use { it.copyTo(zipOut) }
                    zipOut.closeEntry()

                    // 2. Agregar carpeta de fotos (pedidos) al .zip si incluirFotos == true
                    if (incluirFotos) {
                        val carpetaPedidos = File(context.filesDir, "pedidos")
                        if (carpetaPedidos.exists()) {
                            agregarCarpetaAZip(carpetaPedidos, "pedidos", zipOut)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun agregarCarpetaAZip(carpeta: File, rutaRelativa: String, zipOut: ZipOutputStream) {
        val archivos = carpeta.listFiles() ?: return
        for (archivo in archivos) {
            val entradaRuta = "$rutaRelativa/${archivo.name}"
            if (archivo.isDirectory) {
                agregarCarpetaAZip(archivo, entradaRuta, zipOut)
            } else {
                val entry = ZipEntry(entradaRuta)
                zipOut.putNextEntry(entry)
                FileInputStream(archivo).use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
    }

    fun esBaseDatosValida(context: Context, uriOrigen: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uriOrigen)?.use { inputStream ->
                val header = ByteArray(16)
                val bytesRead = inputStream.read(header)
                if (bytesRead < 16) return false
                val headerString = String(header, Charsets.US_ASCII)
                headerString.startsWith("SQLite format 3") || headerString.startsWith("PK")
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun importarBaseDatos(context: Context, uriOrigen: Uri): Boolean {
        return importarRespaldoZip(context, uriOrigen)
    }

    fun importarRespaldoZip(context: Context, uriOrigen: Uri): Boolean {
        if (!esBaseDatosValida(context, uriOrigen)) return false

        return try {
            val dbFile = context.getDatabasePath("control_deudas_db")
            val walFile = context.getDatabasePath("control_deudas_db-wal")
            val shmFile = context.getDatabasePath("control_deudas_db-shm")

            DeudaDataBase.getDatabase(context).close()

            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            context.contentResolver.openInputStream(uriOrigen)?.use { inputStream ->
                val bufferedIn = BufferedInputStream(inputStream)
                bufferedIn.mark(16)
                val header = ByteArray(16)
                bufferedIn.read(header)
                bufferedIn.reset()

                val headerStr = String(header, Charsets.US_ASCII)

                if (headerStr.startsWith("PK")) {
                    // Es un archivo .ZIP (Base de Datos + Fotos)
                    ZipInputStream(bufferedIn).use { zipIn ->
                        var entry: ZipEntry? = zipIn.nextEntry
                        while (entry != null) {
                            val name = entry.name
                            if (name == "database/control_deudas_db" || name == "control_deudas_db") {
                                FileOutputStream(dbFile).use { zipIn.copyTo(it) }
                            } else if (name.startsWith("pedidos/")) {
                                val destinoFoto = File(context.filesDir, name)
                                destinoFoto.parentFile?.mkdirs()
                                if (!entry.isDirectory) {
                                    FileOutputStream(destinoFoto).use { zipIn.copyTo(it) }
                                }
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                } else if (headerStr.startsWith("SQLite format 3")) {
                    // Es un archivo .db directo de SQLite
                    FileOutputStream(dbFile).use { outputStream ->
                        bufferedIn.copyTo(outputStream)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun obtenerEspacioFotosBytes(context: Context): Long {
        val carpeta = File(context.filesDir, "pedidos")
        if (!carpeta.exists()) return 0L
        return calcularTamanoCarpeta(carpeta)
    }

    private fun calcularTamanoCarpeta(dir: File): Long {
        var size = 0L
        val archivos = dir.listFiles() ?: return 0L
        for (archivo in archivos) {
            size += if (archivo.isDirectory) calcularTamanoCarpeta(archivo) else archivo.length()
        }
        return size
    }

    suspend fun limpiarFotosHuerfanas(context: Context, db: DeudaDataBase): Int {
        var eliminados = 0
        try {
            val imagenesEnDb = db.pedidoDao().obtenerTodasLasImagenesSync()
            val rutasValidas = imagenesEnDb.flatMap { listOf(it.ruta, it.rutaMiniatura) }.filter { it.isNotBlank() }.toSet()

            val carpetaPedidos = File(context.filesDir, "pedidos")
            if (carpetaPedidos.exists()) {
                eliminados += borrarArchivosHuerfanosEnCarpeta(carpetaPedidos, rutasValidas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return eliminados
    }

    private fun borrarArchivosHuerfanosEnCarpeta(dir: File, rutasValidas: Set<String>): Int {
        var count = 0
        val archivos = dir.listFiles() ?: return 0
        for (archivo in archivos) {
            if (archivo.isDirectory) {
                count += borrarArchivosHuerfanosEnCarpeta(archivo, rutasValidas)
                if (archivo.listFiles()?.isEmpty() == true) {
                    archivo.delete()
                }
            } else {
                if (!rutasValidas.contains(archivo.absolutePath)) {
                    if (archivo.delete()) count++
                }
            }
        }
        return count
    }
}
