package com.example.finvovo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Cash", "Bank", "Savings", "Credit Card" etc.
    val balance: Double = 0.0 // Current Balance (Calculated or cached)
)
