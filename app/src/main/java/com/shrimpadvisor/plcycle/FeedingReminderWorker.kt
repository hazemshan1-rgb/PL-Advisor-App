package com.shrimpadvisor.plcycle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class FeedingReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val pondName = inputData.getString(KEY_POND_NAME) ?: "your pond"
        showNotification(pondName)
        return Result.success()
    }

    private fun showNotification(pondName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Feeding Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily shrimp feeding schedule reminders"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Feeding Time — $pondName")
            .setContentText("Log today's reading and check FCR after this feed.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "feeding_reminders"
        const val NOTIFICATION_ID = 1001
        const val KEY_POND_NAME = "pond_name"
        const val WORK_TAG = "feeding_reminder"
    }
}
