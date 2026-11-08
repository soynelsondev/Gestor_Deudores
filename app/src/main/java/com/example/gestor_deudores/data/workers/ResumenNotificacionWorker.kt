package com.example.gestor_deudores.data.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gestor_deudores.MainActivity
import com.example.gestor_deudores.R

class ResumenNotificacionWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        mostrarNotificacionResumenSemanal()
        return Result.success()
    }

    private fun mostrarNotificacionResumenSemanal() {
        val channelId = "resumen_semanal_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Resumen Financiero",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones semanales para realizar el resumen financiero y revisar cuentas"
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
            .setSmallIcon(R.drawable.zubli_logo)
            .setContentTitle("📊 ¡Es hora de tu Resumen Semanal!")
            .setContentText("Revisa tus finanzas de esta semana, verifica tus cuentas y genera tu reporte PDF.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Revisa tus ingresos de esta semana, verifica tus cuentas bancarias y genera tu reporte financiero ejecutivo en PDF para mantener el control de tu negocio.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
