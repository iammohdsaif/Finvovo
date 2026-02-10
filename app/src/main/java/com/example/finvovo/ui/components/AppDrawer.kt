package com.example.finvovo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.finvovo.ui.theme.PrimaryViolet
import com.example.finvovo.ui.theme.WhiteSurface

@Composable
fun AppDrawer(
    currentRoute: String?,
    totalBalance: Double?,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onRate: () -> Unit,
    onPrivacy: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.width(300.dp),
        windowInsets = WindowInsets(0, 0, 0, 0) // Draw behind status bar
    ) {
        // 1. Header Section
        DrawerHeader(totalBalance)

        // 2. Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            // Section: Main
            DrawerSectionHeader("Main")
            DrawerItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentRoute == "home",
                onClick = {
                    onNavigate("home")
                    onClose()
                }
            )
            DrawerItem(
                icon = Icons.Default.DateRange,
                label = "Planning",
                isSelected = currentRoute == "planning",
                onClick = {
                    onNavigate("planning")
                    onClose()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section: Data & Backup
            DrawerSectionHeader("Data & Backup")
            DrawerItem(
                icon = Icons.Default.CloudUpload,
                label = "Backup Data",
                subtitle = "Save local backup",
                onClick = { 
                    onBackup()
                    onClose() 
                }
            )
            DrawerItem(
                icon = Icons.Default.CloudDownload,
                label = "Restore Data",
                subtitle = "Restore from file",
                onClick = { 
                    onRestore()
                    onClose() 
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section: Security
            DrawerSectionHeader("Security")
            DrawerItem(
                icon = Icons.Default.Lock,
                label = "Change PIN",
                isSelected = currentRoute == "change_pin",
                onClick = {
                    onNavigate("change_pin")
                    onClose()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Section: App & Info
            DrawerSectionHeader("App & Info")
            DrawerItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentRoute == "settings",
                onClick = {
                    onNavigate("settings")
                    onClose()
                }
            )
            DrawerItem(
                icon = Icons.Default.Info,
                label = "About",
                isSelected = currentRoute == "about",
                onClick = {
                    onNavigate("about")
                    onClose()
                }
            )
            DrawerItem(
                icon = Icons.Default.Star,
                label = "Rate Us",
                onClick = {
                    onRate()
                    onClose()
                }
            )
             DrawerItem(
                icon = Icons.Default.PrivacyTip,
                label = "Privacy Policy",
                onClick = {
                    onPrivacy()
                    onClose()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerHeader(totalBalance: Double?) {
    val formattedBalance = remember(totalBalance) {
        totalBalance?.let { "Balance: ₹${String.format("%.2f", it)}" }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryViolet)
            .statusBarsPadding() // Add padding for status bar
            .padding(24.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                     imageVector = Icons.Default.AccountBalanceWallet,
                     contentDescription = "Logo",
                     tint = Color.White,
                     modifier = Modifier.size(24.dp)
                 )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Finvovo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            formattedBalance?.let { balanceText ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryViolet.copy(alpha = 0.1f) else Color.Transparent
    val contentColor = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.onSurface
    val iconColor = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                subtitle?.let {
                     Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
