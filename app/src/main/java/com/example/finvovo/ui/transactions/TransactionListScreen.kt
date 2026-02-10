package com.example.finvovo.ui.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finvovo.FinvovoApplication
import com.example.finvovo.data.DataRepository
import com.example.finvovo.data.model.Transaction
import com.example.finvovo.data.model.TransactionCategory
import com.example.finvovo.data.model.TransactionType
import com.example.finvovo.ui.theme.GreenCredit
import com.example.finvovo.ui.theme.RedDebit
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionListScreen(
    repository: DataRepository,
    accountId: Int?, // Changed from TransactionType to accountId
    accountName: String?,
    searchQuery: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val rawTransactions by if (accountId != null) {
        repository.getTransactionsByAccount(accountId).collectAsState(initial = emptyList())
    } else {
        repository.allTransactions.collectAsState(initial = emptyList())
    }
    
    val context = LocalContext.current
    val appContainer = (context.applicationContext as FinvovoApplication).container
    val currencySymbol by appContainer.prefs.currencySymbol.collectAsState()

    // Filtering logic
    val transactions = remember(rawTransactions, searchQuery) {
        if (searchQuery.isBlank()) {
            rawTransactions
        } else {
            rawTransactions.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.amount.toString().contains(searchQuery)
            }
        }
    }
    
    val balance by if (accountId != null) {
        repository.getAccountBalance(accountId).collectAsState(initial = 0.0)
    } else {
        repository.getTotalBalance().collectAsState(initial = 0.0)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        topBar = {
            // Optional: If we want a specific top bar here or rely on MainScreen's
            // For now relying on MainScreen but we might want to show Account Name title?
            // MainScreen controls the TopBar title based on route.
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Balance Header (Only show if not searching or if items exist)
            if (searchQuery.isBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(accountName ?: "Total Balance", style = MaterialTheme.typography.labelMedium)
                        Text("$currencySymbol${String.format("%.2f", balance)}", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }

            if (transactions.isEmpty() && searchQuery.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions match '$searchQuery'", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Grouped List
            val groupedTransactions = transactions.groupBy { 
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.date)) 
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedTransactions.forEach { (date, list) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    items(list, key = { it.id }) { transaction ->
                        TransactionItem(transaction, currencySymbol)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, currencySymbol: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                contentAlignment = Alignment.Center
            ) {
                 Text(
                     text = if (transaction.category == TransactionCategory.CREDIT) "↓" else "↑",
                     fontSize = 20.sp,
                     color = if (transaction.category == TransactionCategory.CREDIT) GreenCredit else RedDebit
                 )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    transaction.description, 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(transaction.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = "${if (transaction.category == TransactionCategory.CREDIT) "+" else "-"} $currencySymbol${transaction.amount}",
            color = if (transaction.category == TransactionCategory.CREDIT) GreenCredit else RedDebit,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
