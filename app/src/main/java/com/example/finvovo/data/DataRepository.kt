package com.example.finvovo.data

import com.example.finvovo.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class DataRepository(
    private val transactionDao: TransactionDao,
    private val upcomingDao: UpcomingDao,
    private val accountDao: AccountDao
) {
    // Accounts
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val accountsWithStats: Flow<List<AccountWithStats>> = accountDao.getAccountsWithStats()


    suspend fun initDefaultAccounts() {
        withContext(Dispatchers.IO) {
            if (accountDao.getAccountCount() == 0) {
                android.util.Log.d("Finvovo", "Repo: Initializing default accounts")
                // ID 1: Cash
                accountDao.insertAccount(Account(name = "Cash", type = "Cash", balance = 0.0)) 
                // ID 2: Bank
                accountDao.insertAccount(Account(name = "Bank Account", type = "Bank", balance = 0.0))
            }
        }
    }
    
    suspend fun addAccount(account: Account) {
        withContext(Dispatchers.IO) {
            accountDao.insertAccount(account)
        }
    }

    suspend fun updateAccount(account: Account) {
        withContext(Dispatchers.IO) {
            accountDao.updateAccount(account)
        }
    }
    
    suspend fun deleteAccount(account: Account) {
         withContext(Dispatchers.IO) {
             // Optional: specific logic to handle transactions of deleted account?
             // For now just delete the account.
             accountDao.deleteAccount(account)
         }
    }

    // Transactions
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<Transaction>> = transactionDao.getRecentTransactions(5)
    
    // Updated to filter by Account instead of just Type
    fun getTransactionsByAccount(accountId: Int): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByAccount(accountId)
        
    // Legacy mapping (migrating Type to Accounts for UI if needed, or purely deprecating)
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByType(type)

    suspend fun getTransactionsBetweenDates(startDate: Long, endDate: Long): List<Transaction> {
        return withContext(Dispatchers.IO) {
            transactionDao.getTransactionsBetweenDates(startDate, endDate)
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.insertTransaction(transaction)
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.deleteTransaction(transaction)
        }
    }

    // Balances (Calculated per Account)
    fun getAccountBalance(accountId: Int): Flow<Double> {
        val creditFlow = transactionDao.getAccountTotalCredit(accountId)
        val debitFlow = transactionDao.getAccountTotalDebit(accountId)
        
        return kotlinx.coroutines.flow.combine(creditFlow, debitFlow) { credit, debit ->
            (credit ?: 0.0) - (debit ?: 0.0)
        }
    }
    
    fun getAccountCredit(accountId: Int): Flow<Double> = 
        transactionDao.getAccountTotalCredit(accountId).map { it ?: 0.0 }

    fun getAccountDebit(accountId: Int): Flow<Double> = 
        transactionDao.getAccountTotalDebit(accountId).map { it ?: 0.0 }
    
    // Total Balance (Sum of all accounts)
    // Note: ideally we sum all accounts' calculated balances
    fun getTotalBalance(): Flow<Double> {
         // This is a bit complex with Flows. 
         // Simplified approach: Sum of all transactions (ignore specific accounts) or sum known accounts.
         // Let's stick to global transaction sum for now as a quick fix, or combine flows.
         // Better: 
         val allCredits = transactionDao.getAllTransactions().map { list -> list.filter { it.category == TransactionCategory.CREDIT }.sumOf { it.amount } }
         val allDebits = transactionDao.getAllTransactions().map { list -> list.filter { it.category == TransactionCategory.DEBIT }.sumOf { it.amount } }
         
         return kotlinx.coroutines.flow.combine(allCredits, allDebits) { credit, debit -> credit - debit }
    }
    
    // Deprecated Balance (Legacy Type based) - Keep for compatibility if any UI uses it before refactor is complete
    fun getBalance(type: TransactionType): Flow<Double> {
        return combine(transactionDao.getTotalCredit(type), transactionDao.getTotalDebit(type)) { c, d -> 
            (c ?: 0.0) - (d ?: 0.0) 
        }
    }

    // Upcoming
    val allUpcomingItems: Flow<List<UpcomingItem>> = upcomingDao.getAllUpcomingItems()

    fun getUpcomingByType(type: PlanningType): Flow<List<UpcomingItem>> =
        upcomingDao.getUpcomingItemsByType(type).map { list ->
            android.util.Log.d("Finvovo", "Repo: DAO returned ${list.size} items for type $type")
            val filtered = list.filter { it.status == PlanningStatus.PENDING }
            android.util.Log.d("Finvovo", "Repo: After filtering PENDING: ${filtered.size}")
            filtered
        }

    fun getDuePlanningItems(): Flow<List<UpcomingItem>> {
        val todayEnd = System.currentTimeMillis() + 24 * 3600 * 1000 // Include all of today roughly
        return upcomingDao.getDuePendingItems(todayEnd)
    }
        
    suspend fun addUpcomingItem(item: UpcomingItem) {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("Finvovo", "Repo: Adding item: $item")
                val id = upcomingDao.insertUpcomingItem(item)
                android.util.Log.d("Finvovo", "Repo: Item added with ID: $id")
            } catch (e: Exception) {
                android.util.Log.e("Finvovo", "Repo: Error adding item", e)
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteUpcomingItem(item: UpcomingItem) {
        withContext(Dispatchers.IO) {
            upcomingDao.deleteUpcomingItem(item)
        }
    }
    
    // Process Upcoming
    suspend fun markUpcomingAsProcessed(upcomingItem: UpcomingItem) {
        // Only update status to COMPLETED
        // We DO NOT create a transaction anymore (Pure Reminder System)
        
        val updatedItem = upcomingItem.copy(status = PlanningStatus.COMPLETED)
        withContext(Dispatchers.IO) {
            upcomingDao.updateUpcomingItem(updatedItem)
        }
    }
}
