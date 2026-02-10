package com.example.finvovo.data

import androidx.room.*
import com.example.finvovo.data.model.Account
import com.example.finvovo.data.model.AccountWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>

    // Efficiently fetch accounts with calculated stats
    @Query("""
        SELECT 
            a.id, 
            a.name, 
            a.type,
            (SELECT SUM(amount) FROM transactions t WHERE t.accountId = a.id AND t.category = 'CREDIT') as totalCredit,
            (SELECT SUM(amount) FROM transactions t WHERE t.accountId = a.id AND t.category = 'DEBIT') as totalDebit
        FROM accounts a
    """)
    fun getAccountsWithStats(): Flow<List<AccountWithStats>>


    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Int): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(account: Account)

    @Update
    fun updateAccount(account: Account)

    @Delete
    fun deleteAccount(account: Account)
    
    // For initializing default accounts
    @Query("SELECT COUNT(*) FROM accounts")
    fun getAccountCount(): Int

    // Backup & Restore
    @Query("SELECT * FROM accounts")
    fun getAllAccountsSync(): List<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(accounts: List<Account>)

    @Query("DELETE FROM accounts")
    fun deleteAll()
}
