package com.example.finvovo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlanningType {
    INCOME, PAYMENT
}

enum class PlanningStatus {
    PENDING, COMPLETED, DELAYED
}

@Entity(tableName = "upcoming_items")
data class UpcomingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: PlanningType, // INCOME or PAYMENT
    val amount: Double,
    val dueDate: Long,
    val description: String,
    val sourceOrDest: TransactionType, // Which account (Cash/Bank)
    val status: PlanningStatus = PlanningStatus.PENDING
)
