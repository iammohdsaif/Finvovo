package com.example.finvovo.data

import androidx.room.TypeConverter
import com.example.finvovo.data.model.*

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromTransactionCategory(value: TransactionCategory): String = value.name

    @TypeConverter
    fun toTransactionCategory(value: String): TransactionCategory = TransactionCategory.valueOf(value)

    @TypeConverter
    fun fromPlanningType(value: PlanningType): String = value.name

    @TypeConverter
    fun toPlanningType(value: String): PlanningType = PlanningType.valueOf(value)

    @TypeConverter
    fun fromPlanningStatus(value: PlanningStatus): String = value.name

    @TypeConverter
    fun toPlanningStatus(value: String): PlanningStatus = PlanningStatus.valueOf(value)
}
