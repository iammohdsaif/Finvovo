package com.example.finvovo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.finvovo.data.model.Transaction
import com.example.finvovo.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Get transactions for a specific account
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    // For Ask AI Export
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): List<Transaction>

    @Insert
    fun insertTransaction(transaction: Transaction)

    @Delete
    fun deleteTransaction(transaction: Transaction)
    
    // Legacy support (to be removed or refactored)
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND category = 'CREDIT'")
    fun getTotalCredit(type: TransactionType): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND category = 'DEBIT'")
    fun getTotalDebit(type: TransactionType): Flow<Double?>
    
    // New Account-based Balance Queries
    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND category = 'CREDIT'")
    fun getAccountTotalCredit(accountId: Int): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE accountId = :accountId AND category = 'DEBIT'")
    fun getAccountTotalDebit(accountId: Int): Flow<Double?>

    // Backup & Restore
    @Query("SELECT * FROM transactions")
    fun getAllTransactionsSync(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(transactions: List<Transaction>)

    @Query("DELETE FROM transactions")
    fun deleteAll()
}
