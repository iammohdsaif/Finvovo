package com.example.finvovo.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.finvovo.data.DataRepository
import com.example.finvovo.data.model.Transaction
import com.example.finvovo.data.model.TransactionCategory
import com.example.finvovo.data.model.TransactionType
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    repository: DataRepository,
    defaultAccountId: Int?,
    onTransactionSaved: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TransactionCategory.DEBIT) }
    
    // Account Selection
    val accounts by repository.allAccounts.collectAsState(initial = emptyList())
    var selectedAccount by remember { mutableStateOf<com.example.finvovo.data.model.Account?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Set default account when list loads
    LaunchedEffect(accounts) {
        if (selectedAccount == null && accounts.isNotEmpty()) {
            selectedAccount = if (defaultAccountId != null) {
                accounts.find { it.id == defaultAccountId } ?: accounts.first()
            } else {
                accounts.first()
            }
        }
    }

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
    
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("New Transaction", style = MaterialTheme.typography.headlineSmall)

        // Type Selector (Credit/Debit)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = category == TransactionCategory.DEBIT,
                onClick = { category = TransactionCategory.DEBIT },
                label = { Text("Debit (Expense)") },
                leadingIcon = { if (category == TransactionCategory.DEBIT) Text("❤️") }
            )
            FilterChip(
                selected = category == TransactionCategory.CREDIT,
                onClick = { category = TransactionCategory.CREDIT },
                label = { Text("Credit (Income)") },
                leadingIcon = { if (category == TransactionCategory.CREDIT) Text("💚") }
            )
        }
        
        // Account Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedAccount?.name ?: "Select Account",
                onValueChange = {},
                readOnly = true,
                label = { Text("Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.name) },
                        onClick = {
                            selectedAccount = account
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate))}")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val amt = amount.toDoubleOrNull()
                val finalAccount = selectedAccount
                if (amt != null && description.isNotBlank() && finalAccount != null) {
                    scope.launch {
                        val transaction = Transaction(
                            type = TransactionType.CASH, // Deprecated/Legacy, sticking to CASH default as we use accountId now
                            category = category,
                            amount = amt,
                            date = selectedDate,
                            description = description,
                            accountId = finalAccount.id
                        )
                        repository.addTransaction(transaction)
                        onTransactionSaved()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Transaction")
        }
    }
}
