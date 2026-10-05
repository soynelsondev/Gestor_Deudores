package com.example.gestor_deudores.data.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificacionUtils {
    fun programarNotificacionMensual(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NotificacionCierreMesWorker>(
            28, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "cierre_mes_zubli_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}