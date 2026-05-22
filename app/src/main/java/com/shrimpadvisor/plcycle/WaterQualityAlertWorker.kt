package com.shrimpadvisor.plcycle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shrimpadvisor.plcycle.data.PondCycleDatabase
import kotlinx.coroutines.flow.first

class WaterQualityAlertWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = PondCycleDatabase.getDatabase(context)
        val cycles = db.pondCycleDao().getAllCycles().first()

        val alerts = mutableListOf<String>()

        for (cycle in cycles) {
            val readings = db.dailyReadingDao()
                .getRecentReadingsForCycle(cycle.id, 1)
                .first()
            val latest = readings.firstOrNull() ?: continue

            val pondAlerts = mutableListOf<String>()
            if (latest.tanLevel >= 0.8) pondAlerts.add("TAN ${String.format("%.2f", latest.tanLevel)} ppm")
            if (latest.doLevel < 5.3)   pondAlerts.add("DO ${String.format("%.1f", latest.doLevel)} ppm")
            if (latest.ph < 7.3)        pondAlerts.add("pH ${String.format("%.1f", latest.ph)} low")
            if (latest.ph > 8.7)        pondAlerts.add("pH ${String.format("%.1f", latest.ph)} high")

            if (pondAlerts.isNotEmpty()) {
                alerts.add("${cycle.pondName}: ${pondAlerts.joinToString(", ")}")
            }
        }

        if (alerts.isNotEmpty()) showNotification(alerts.joinToString("\n"))
        return Result.success()
    }

    private fun showNotification(text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Water Quality Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical water parameter threshold breaches"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Water Quality Alert")
            .setContentText(text.lines().first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "wq_alerts"
        const val NOTIFICATION_ID = 1002
        const val WORK_TAG = "wq_alert"
    }
}
