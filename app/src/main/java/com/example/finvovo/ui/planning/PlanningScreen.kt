package com.example.finvovo.ui.planning

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.finvovo.data.DataRepository
import com.example.finvovo.data.model.PlanningStatus
import com.example.finvovo.data.model.PlanningType
import com.example.finvovo.data.model.TransactionType
import com.example.finvovo.data.model.UpcomingItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlanningScreen(repository: DataRepository) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming Income, 1: Upcoming Payments
    var showAddDialog by remember { mutableStateOf(false) } 
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Upcoming Income") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Upcoming Payments") }
                )
            }

            // Slide logic using HorizontalPager
            val pagerState = androidx.compose.foundation.pager.rememberPagerState { 2 }

            LaunchedEffect(selectedTab) {
                pagerState.animateScrollToPage(selectedTab)
            }

            LaunchedEffect(pagerState.currentPage) {
                selectedTab = pagerState.currentPage
            }

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                val currentType = if (page == 0) PlanningType.INCOME else PlanningType.PAYMENT
                val upcomingItems by repository.getUpcomingByType(currentType).collectAsState(initial = emptyList())

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (upcomingItems.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No items found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        items(upcomingItems, key = { it.id }) { item ->
                            UpcomingItemCard(
                                item = item,
                                onMarkProcessed = {
                                    scope.launch { repository.markUpcomingAsProcessed(item) }
                                },
                                onDelete = {
                                     scope.launch { repository.deleteUpcomingItem(item) }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
        }
        
        if (showAddDialog) {
            AddPlanningItemDialog(
                type = if (selectedTab == 0) PlanningType.INCOME else PlanningType.PAYMENT,
                onDismiss = { showAddDialog = false },
                onSave = { item ->
                    scope.launch { repository.addUpcomingItem(item) }
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlanningItemDialog(
    type: PlanningType,
    onDismiss: () -> Unit,
    onSave: (UpcomingItem) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) } 

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${if (type == PlanningType.INCOME) "Income" else "Payment"}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Date Picker
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Due Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate))}")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull()
                
                if (amt != null && description.isNotBlank()) {
                    val newItem = UpcomingItem(
                        type = type,
                        amount = amt,
                        dueDate = selectedDate,
                        description = description,
                        sourceOrDest = TransactionType.CASH,
                        status = PlanningStatus.PENDING
                    )
                    onSave(newItem)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpcomingItemCard(item: UpcomingItem, onMarkProcessed: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.description, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    "₹${item.amount}", 
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.type == PlanningType.INCOME) com.example.finvovo.ui.theme.GreenCredit else com.example.finvovo.ui.theme.RedDebit,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                     imageVector = Icons.Default.DateRange,
                     contentDescription = null,
                     tint = MaterialTheme.colorScheme.outline,
                     modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Due: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.dueDate))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) { 
                    Text("Delete", color = MaterialTheme.colorScheme.error) 
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onMarkProcessed,
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.finvovo.ui.theme.PrimaryViolet)
                ) { 
                    Text("Mark Completed") 
                }
            }
        }
    }
}
