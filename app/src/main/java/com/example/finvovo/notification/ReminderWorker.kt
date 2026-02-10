package com.example.finvovo.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.finvovo.FinvovoApplication
import com.example.finvovo.data.model.PlanningStatus
import com.example.finvovo.data.model.PlanningType
import kotlinx.coroutines.flow.first
import java.util.*

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appContainer = (applicationContext as FinvovoApplication).container
        val repository = appContainer.repository
        val notificationHelper = NotificationHelper(applicationContext)

        val allItems = repository.allUpcomingItems.first()
        val pendingItems = allItems.filter { it.status == PlanningStatus.PENDING }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tomorrow = today + 24 * 60 * 60 * 1000

        pendingItems.forEach { item ->
            val itemDate = Calendar.getInstance().apply {
                timeInMillis = item.dueDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val typeStr = if (item.type == PlanningType.INCOME) "Upcoming Income" else "Upcoming Payment"

            if (itemDate == today) {
                notificationHelper.showNotification(
                    title = "Reminder: $typeStr Today",
                    message = "${item.description}: ₹${item.amount} is due today.",
                    notificationId = item.id
                )
            } else if (itemDate == tomorrow) {
                notificationHelper.showNotification(
                    title = "Reminder: $typeStr Tomorrow",
                    message = "${item.description}: ₹${item.amount} is due tomorrow.",
                    notificationId = item.id + 100000 // Offset for tomorrow's notification
                )
            }
        }

        return Result.success()
    }
}
