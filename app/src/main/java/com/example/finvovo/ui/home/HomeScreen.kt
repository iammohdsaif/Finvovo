package com.example.finvovo.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finvovo.data.DataRepository
import com.example.finvovo.data.model.Account
import com.example.finvovo.ui.theme.GreenCredit
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.finvovo.FinvovoApplication
import com.example.finvovo.ui.components.shimmerEffect
import com.example.finvovo.ui.theme.RedDebit
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(
    repository: DataRepository,
    searchQuery: String = "",
    onNavigateToTransactions: (Int?, String?) -> Unit,
    onNavigateToAdd: () -> Unit,
    onReportTargets: (Rect?, Rect?, Rect?) -> Unit = { _, _, _ -> }
) {
    val accounts by repository.accountsWithStats.collectAsState(initial = emptyList())
    val totalBalance by repository.getTotalBalance().collectAsState(initial = null)
    val rawRecentTransactions by repository.recentTransactions.collectAsState(initial = null)
    val currencySymbol by (LocalContext.current.applicationContext as FinvovoApplication).container.prefs.currencySymbol.collectAsState()

    // Filter transactions based on search query
    val recentTransactions = remember(rawRecentTransactions, searchQuery) {
        if (searchQuery.isBlank()) {
            rawRecentTransactions
        } else {
            rawRecentTransactions?.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.amount.toString().contains(searchQuery)
            }
        }
    }
    
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    val scope = rememberCoroutineScope()

    // Tutorial Targets
    var balanceCardRect by remember { mutableStateOf<Rect?>(null) }
    var addAccountFabRect by remember { mutableStateOf<Rect?>(null) }
    var firstAccountCardRect by remember { mutableStateOf<Rect?>(null) }

    // Pass these up if needed, or expose via a side effect / callback to MainScreen
    // For simplicity, let's execute a callback if provided. 
    // Ideally MainScreen passes a "onTutorialTargetsReady" callback.
    // For now, let's just assume we need to expose them. 
    
    // Actually, to make this clean, HomeScreen should accept a `onReportTutorialTargets` callback.
    // But since I can't easily change the signature without breaking MainScreen extensively in one go,
    // I will add the callback to the signature and update MainScreen in the next step.

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Total Balance Header
            if (searchQuery.isBlank()) {
                item {
                    if (totalBalance == null) {
                         Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect()) {}
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            com.example.finvovo.ui.theme.PrimaryViolet,
                                            com.example.finvovo.ui.theme.DarkViolet
                                        )
                                    )
                                )
                                .onGloballyPositioned { coordinates ->
                                    balanceCardRect = coordinates.boundsInRoot()
                                    onReportTargets(balanceCardRect, addAccountFabRect, firstAccountCardRect)
                                }
                        ) {
                            // Decorative Circles
                            Box(
                                modifier = Modifier
                                    .offset(x = (-20).dp, y = (-20).dp)
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 20.dp, y = 20.dp)
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Total Balance",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "$currencySymbol${String.format("%.2f", totalBalance)}",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Account List
                itemsIndexed(accounts) { index, account ->
                     AccountCard(
                        account = Account(id = account.id, name = account.name, type = account.type),
                        currencySymbol = currencySymbol,
                        totalCredit = account.totalCredit ?: 0.0,
                        totalDebit = account.totalDebit ?: 0.0,
                        balance = account.balance,
                        onEdit = { 
                            accountToEdit = Account(id = account.id, name = account.name, type = account.type)
                        },
                        onDelete = { 
                            accountToDelete = Account(id = account.id, name = account.name, type = account.type)
                            showDeleteConfirmDialog = true
                        },
                        modifier = Modifier
                            .clickable { onNavigateToTransactions(account.id, account.name) }
                            .onGloballyPositioned { coordinates ->
                                if (index == 0) {
                                    firstAccountCardRect = coordinates.boundsInRoot()
                                    onReportTargets(balanceCardRect, addAccountFabRect, firstAccountCardRect)
                                }
                            }
                     )
                }
            }

            // Transactions Section
            item {
                Text(
                    if (searchQuery.isNotBlank()) "Search Results" else "Recent Transactions", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (recentTransactions != null && recentTransactions.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions match '$searchQuery'", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            if (recentTransactions == null) {
                items(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerEffect()
                    ) {}
                }
            } else {
                items(items = recentTransactions!!) { transaction ->
                     TransactionItem(transaction, currencySymbol)
                     HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
            
            item {
                OutlinedCard(
                    onClick = { onNavigateToTransactions(null, null) }, 
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                     Box(
                         modifier = Modifier.padding(16.dp).fillMaxWidth(),
                         contentAlignment = Alignment.Center
                     ) {
                         Text("View All Transactions ->", color = MaterialTheme.colorScheme.primary)
                     }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddAccountDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .onGloballyPositioned { coordinates ->
                    addAccountFabRect = coordinates.boundsInRoot()
                    onReportTargets(balanceCardRect, addAccountFabRect, firstAccountCardRect)
                },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Account")
        }
    }
    
    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, type ->
                scope.launch {
                    repository.addAccount(Account(name = name, type = type, balance = 0.0))
                    showAddAccountDialog = false
                }
            }
        )
    }

    accountToEdit?.let { account ->
        EditAccountDialog(
            account = account,
            onDismiss = { accountToEdit = null },
            onConfirm = { updatedAccount ->
                scope.launch {
                    repository.updateAccount(updatedAccount)
                    accountToEdit = null
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete the account '${accountToDelete?.name}'? This will also delete all associated transactions.") },
            confirmButton = {
                Button(
                    onClick = {
                        accountToDelete?.let {
                            scope.launch {
                                repository.deleteAccount(it)
                            }
                        }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionItem(transaction: com.example.finvovo.data.model.Transaction, currencySymbol: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
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
                     text = if (transaction.category == com.example.finvovo.data.model.TransactionCategory.CREDIT) "↓" else "↑",
                     fontSize = 20.sp,
                     color = if (transaction.category == com.example.finvovo.data.model.TransactionCategory.CREDIT) GreenCredit else RedDebit
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
                    java.text.SimpleDateFormat("dd MMM • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(transaction.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = "${if (transaction.category == com.example.finvovo.data.model.TransactionCategory.CREDIT) "+" else "-"} $currencySymbol${transaction.amount}",
            color = if (transaction.category == com.example.finvovo.data.model.TransactionCategory.CREDIT) GreenCredit else RedDebit,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
