package com.example.finvovo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TransactionType {
    CASH, BANK
}

enum class TransactionCategory {
    CREDIT, DEBIT
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: TransactionType, // CASH or BANK
    val category: TransactionCategory, // CREDIT or DEBIT
    val amount: Double,
    val date: Long, // Timestamp
    val description: String,
    @androidx.room.ColumnInfo(defaultValue = "1") val accountId: Int = 1
)
