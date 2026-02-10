package com.example.finvovo.data

import android.content.Context
import android.net.Uri
import com.example.finvovo.data.model.Account
import com.example.finvovo.data.model.PlanningStatus
import com.example.finvovo.data.model.PlanningType
import com.example.finvovo.data.model.Transaction
import com.example.finvovo.data.model.TransactionCategory
import com.example.finvovo.data.model.TransactionType
import com.example.finvovo.data.model.UpcomingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val accounts: List<Account>,
    val transactions: List<Transaction>,
    val upcomingItems: List<UpcomingItem>
)

object BackupManager {

    suspend fun exportData(context: Context, uri: Uri, database: AppDatabase): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val accounts = database.accountDao().getAllAccountsSync()
                val transactions = database.transactionDao().getAllTransactionsSync()
                val upcomingItems = database.upcomingDao().getAllItemsSync()

                val root = JSONObject()
                root.put("version", 1)
                root.put("timestamp", System.currentTimeMillis())

                // Accounts
                val accountsArray = JSONArray()
                accounts.forEach { account ->
                    val accObj = JSONObject()
                    accObj.put("id", account.id)
                    accObj.put("name", account.name)
                    accObj.put("balance", account.balance)
                    accObj.put("type", account.type)
                    // No color property in Account entity
                    accountsArray.put(accObj)
                }
                root.put("accounts", accountsArray)

                // Transactions
                val transactionsArray = JSONArray()
                transactions.forEach { txn ->
                    val txnObj = JSONObject()
                    txnObj.put("id", txn.id)
                    txnObj.put("amount", txn.amount)
                    txnObj.put("category", txn.category.name) // Enum to String
                    txnObj.put("description", txn.description)
                    txnObj.put("date", txn.date) // Long
                    txnObj.put("type", txn.type.name) // Enum to String
                    txnObj.put("accountId", txn.accountId)
                    transactionsArray.put(txnObj)
                }
                root.put("transactions", transactionsArray)

                // Upcoming
                val upcomingArray = JSONArray()
                upcomingItems.forEach { item ->
                    val itemObj = JSONObject()
                    itemObj.put("id", item.id)
                    itemObj.put("type", item.type.name) // Enum to String
                    itemObj.put("amount", item.amount)
                    itemObj.put("dueDate", item.dueDate)
                    itemObj.put("description", item.description)
                    itemObj.put("sourceOrDest", item.sourceOrDest.name) // Enum to String
                    itemObj.put("status", item.status.name) // Enum to String
                    upcomingArray.put(itemObj)
                }
                root.put("upcomingItems", upcomingArray)

                val jsonString = root.toString(2) // Pretty print

                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(jsonString.toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importData(context: Context, uri: Uri, database: AppDatabase): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sb = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            sb.append(line)
                            line = reader.readLine()
                        }
                    }
                }

                val root = JSONObject(sb.toString())
                
                // Parse Data
                val accountsJson = root.optJSONArray("accounts")
                val transactionsJson = root.optJSONArray("transactions")
                val upcomingJson = root.optJSONArray("upcomingItems")

                val accounts = mutableListOf<Account>()
                if (accountsJson != null) {
                    for (i in 0 until accountsJson.length()) {
                        val obj = accountsJson.getJSONObject(i)
                        accounts.add(
                            Account(
                                id = obj.optInt("id"),
                                name = obj.getString("name"),
                                balance = obj.getDouble("balance"),
                                type = obj.getString("type")
                            )
                        )
                    }
                }

                val transactions = mutableListOf<Transaction>()
                if (transactionsJson != null) {
                    for (i in 0 until transactionsJson.length()) {
                        val obj = transactionsJson.getJSONObject(i)
                        transactions.add(
                            Transaction(
                                id = obj.optInt("id"),
                                amount = obj.getDouble("amount"),
                                category = TransactionCategory.valueOf(obj.getString("category")),
                                description = obj.getString("description"),
                                date = obj.getLong("date"),
                                type = TransactionType.valueOf(obj.getString("type")),
                                accountId = obj.optInt("accountId")
                            )
                        )
                    }
                }

                val upcomingItems = mutableListOf<UpcomingItem>()
                if (upcomingJson != null) {
                    for (i in 0 until upcomingJson.length()) {
                        val obj = upcomingJson.getJSONObject(i)
                        // Handle legacy "isPaid" if restoring from old backup (not applicable here since new feature, but good practice)
                        // For now we assume new format
                        
                        upcomingItems.add(
                            UpcomingItem(
                                id = obj.optInt("id"),
                                type = PlanningType.valueOf(obj.getString("type")),
                                amount = obj.getDouble("amount"),
                                dueDate = obj.getLong("dueDate"),
                                description = obj.getString("description"),
                                sourceOrDest = TransactionType.valueOf(obj.getString("sourceOrDest")),
                                status = PlanningStatus.valueOf(obj.getString("status"))
                            )
                        )
                    }
                }

                // Perform Database Operations (Replace)
                database.runInTransaction {
                    database.accountDao().deleteAll()
                    database.transactionDao().deleteAll()
                    database.upcomingDao().deleteAll()

                    database.accountDao().insertAll(accounts)
                    database.transactionDao().insertAll(transactions)
                    database.upcomingDao().insertAll(upcomingItems)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
