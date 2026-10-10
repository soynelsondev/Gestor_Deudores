package com.example.gestor_deudores.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

data class ResultadoProcesamientoImagen(
    val rutaOriginalComprimida: String,
    val rutaMiniatura: String
)

object ImagenUtils {

    suspend fun procesarYGuardarImagenPedido(
        context: Context,
        pedidoId: Int,
        uriRawString: String
    ): ResultadoProcesamientoImagen? = withContext(Dispatchers.IO) {
        try {
            if (uriRawString.isBlank()) return@withContext null
            val uri = Uri.parse(uriRawString)

            // 1. Cargar Bitmap corrigiendo orientación EXIF
            val bitmapOriginal = cargarBitmapConExif(context, uri) ?: return@withContext null

            // 2. Crear carpeta interna del pedido: filesDir/pedidos/{pedidoId}/
            val carpetaPedido = File(context.filesDir, "pedidos/$pedidoId").apply {
                if (!exists()) mkdirs()
            }

            val uuidName = UUID.randomUUID().toString()

            // 3. Comprimir Imagen Principal (lado mayor 1280 px, JPEG 80%)
            val bitmap1280 = redimensionarBitmap(bitmapOriginal, 1280)
            val archivoPrincipal = File(carpetaPedido, "${uuidName}_full.jpg")
            FileOutputStream(archivoPrincipal).use { out ->
                bitmap1280.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            // 4. Comprimir Miniatura (lado mayor 256 px, JPEG 80%)
            val bitmap256 = redimensionarBitmap(bitmapOriginal, 256)
            val archivoMiniatura = File(carpetaPedido, "${uuidName}_thumb.jpg")
            FileOutputStream(archivoMiniatura).use { out ->
                bitmap256.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            // Liberar memoria
            if (bitmapOriginal != bitmap1280) bitmapOriginal.recycle()
            if (bitmap1280 != bitmap256) bitmap1280.recycle()
            bitmap256.recycle()

            ResultadoProcesamientoImagen(
                rutaOriginalComprimida = archivoPrincipal.absolutePath,
                rutaMiniatura = archivoMiniatura.absolutePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun cargarBitmapConExif(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream.close()

            // Leer orientación EXIF
            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exif = ExifInterface(exifStream)
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                }
            } catch (e: Exception) {
                // Si la imagen no soporta EXIF, se mantiene NORMAL
            }

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            if (!matrix.isIdentity) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try { inputStream?.close() } catch (e: Exception) { }
        }
    }

    private fun redimensionarBitmap(bitmap: Bitmap, maxLadoPx: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxLadoPx && height <= maxLadoPx) return bitmap

        val aspectRatio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width >= height) {
            targetWidth = maxLadoPx
            targetHeight = (maxLadoPx / aspectRatio).toInt()
        } else {
            targetHeight = maxLadoPx
            targetWidth = (maxLadoPx * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
