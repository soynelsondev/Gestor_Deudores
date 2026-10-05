package com.example.gestor_deudores.data.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gestor_deudores.MainActivity
import com.example.gestor_deudores.R

class NotificacionCierreMesWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): androidx.work.ListenableWorker.Result {
        mostrarNotificacionCierreMes(context)
        return androidx.work.ListenableWorker.Result.success()
    }

    companion object {
        fun mostrarNotificacionCierreMes(context: Context) {
            val channelId = "zubli_cierre_mes_channel"
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Reporte Financiero Mensual Zubli",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones automáticas de cierre de mes y auditoría"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("📄 Zubli - ¡Cierre Financiero del Mes!")
                .setContentText("Tu reporte mensual está listo. Toca aquí para ver tu rentabilidad y comparativa.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notification)
        }
    }
}