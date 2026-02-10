package com.example.finvovo.data.model

data class AccountWithStats(
    val id: Int,
    val name: String,
    val type: String,
    val totalCredit: Double?,
    val totalDebit: Double?
) {
    val balance: Double
        get() = (totalCredit ?: 0.0) - (totalDebit ?: 0.0)
}
