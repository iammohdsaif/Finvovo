package com.example.finvovo.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finvovo.data.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: PreferenceManager,
    onNavigateToChangePin: () -> Unit
) {
    val context = LocalContext.current
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val currentCurrency by prefs.currencySymbol.collectAsState()
    var vibrationEnabled by remember { mutableStateOf(prefs.isVibrationEnabled) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            // Padding is now handled by the parent NavHost/Scaffold
    ) {
        item {
            SettingsGroup(title = "App Preferences") {
                SettingsClickableItem(
                    title = "Currency",
                    subtitle = "Select preferred currency symbol ($currentCurrency)",
                    onClick = { showCurrencyDialog = true }
                )
                SettingsToggleItem(
                    title = "Vibration on Login Screen",
                    checked = vibrationEnabled,
                    onCheckedChange = {
                        vibrationEnabled = it
                        prefs.isVibrationEnabled = it
                    }
                )
            }
        }

        item {
            SettingsGroup(title = "Security") {
                var isLockEnabled by remember { mutableStateOf(prefs.isAppLockEnabled) }
                
                SettingsToggleItem(
                    title = "Enable App Lock",
                    checked = isLockEnabled,
                    onCheckedChange = {
                        isLockEnabled = it
                        prefs.isAppLockEnabled = it
                    }
                )
                
                SettingsClickableItem(
                    title = "Change PIN",
                    subtitle = "Update your 4-digit security PIN",
                    onClick = onNavigateToChangePin,
                    trailingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    enabled = isLockEnabled
                )
            }
        }

        item {
            SettingsGroup(title = "Support") {
                SettingsClickableItem(
                    title = "Suggestion / Feedback",
                    subtitle = "Send us your thoughts",
                    onClick = { sendFeedback(context) },
                    trailingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectorDialog(
            currentCurrency = currentCurrency,
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = {
                prefs.selectedCurrencySymbol = it
                showCurrencyDialog = false
            }
        )
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { 
            Text(
                title, 
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ) 
        },
        supportingContent = subtitle?.let { { 
            Text(
                it, 
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            ) 
        } },
        trailingContent = trailingIcon,
        modifier = Modifier.clickable(enabled = enabled) { onClick() }
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        trailingContent = {
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scale(0.8f) // Slightly smaller switch to look cleaner
            )
        }
    )
}



@Composable
fun CurrencySelectorDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                // Custom Close Button or Header if needed, but CurrencySelector has a Header.
                // We might want to add a "Close" button if CurrencySelector doesn't have one explicitly preventing exit
                // but CurrencySelector has a "DONE" button.
                // Let's wrap CurrencySelector.
                
                com.example.finvovo.ui.components.CurrencySelector(
                    currentCurrencySymbol = currentCurrency,
                    onCurrencySelected = onCurrencySelected
                )
            }
        }
    }
}

private fun sendFeedback(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:i.am.mohd.saif2006@gmail.com")
        putExtra(Intent.EXTRA_SUBJECT, "Finvovo App Feedback")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    } catch (e: Exception) {
        // Handle error
    }
}
