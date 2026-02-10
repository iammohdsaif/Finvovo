package com.example.finvovo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finvovo.data.model.Transaction
import com.example.finvovo.data.model.UpcomingItem
import com.example.finvovo.data.model.Account

@Database(
    entities = [Transaction::class, UpcomingItem::class, Account::class],
    version = 2,
    autoMigrations = [
        androidx.room.AutoMigration(from = 1, to = 2)
    ]
)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun upcomingDao(): UpcomingDao
    abstract fun accountDao(): AccountDao
}
