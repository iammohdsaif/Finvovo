package com.example.finvovo

import android.app.Application
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finvovo.data.AppDatabase
import com.example.finvovo.data.DataRepository
import com.example.finvovo.data.PreferenceManager
import com.example.finvovo.notification.ReminderWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

class FinvovoApplication : Application() {
    lateinit var container: AppContainer

    val applicationScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        setupReminders()
        
        // Warm up the database in the background to prevent freeze on first screen load
        applicationScope.launch {
            try {
                // Force database initialization
                container.database.openHelper.writableDatabase
                container.repository.initDefaultAccounts()
                android.util.Log.d("Finvovo", "Database warmed up & Defaults checked")
            } catch (e: Exception) {
                android.util.Log.e("Finvovo", "Database warmup failed", e)
            }
        }
    }

    private fun setupReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(8, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("planning_reminders")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PlanningReminders",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}

class AppContainer(private val context: android.content.Context) {
    val database by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "finvovo-db")
            .build()
    }
    
    val repository by lazy {
        DataRepository(database.transactionDao(), database.upcomingDao(), database.accountDao())
    }
    
    val prefs by lazy {
        PreferenceManager(context)
    }
}
