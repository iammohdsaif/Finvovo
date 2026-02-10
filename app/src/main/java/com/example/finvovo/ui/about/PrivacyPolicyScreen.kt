package com.example.finvovo.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Last Updated: February 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            PolicySection(
                title = "1. Introduction",
                content = "Welcome to Finvovo. We respect your privacy and are committed to protecting your personal data. This privacy policy explains how we handle your information."
            )

            PolicySection(
                title = "2. Data Collection & Storage",
                content = "Finvovo is an offline-first application. All your financial data, including transactions, accounts, and budget plans, is stored locally on your device. We (the developers) do not collect, transmit, or store any of your personal financial data on our servers."
            )

            PolicySection(
                title = "3. Backup & Restore",
                content = "The app provides a Backup & Restore feature that allows you to export your data. You can choose to save these backups to your local device storage or your personal Cloud Storage (e.g., Google Drive). This process is entirely initiated and controlled by you. We do not have access to your backups."
            )

            PolicySection(
                title = "4. Data Security",
                content = "Since your data is stored locally, it is protected by your device's security protocols. Finvovo also offers an optional App Lock feature (PIN/Biometric) to prevent unauthorized access to the app on your device."
            )

            PolicySection(
                title = "5. Third-Party Services",
                content = "The app may interact with third-party services like Google Drive only when you explicitly use the Backup feature. These services are governed by their own privacy policies."
            )
            
            PolicySection(
                title = "6. Contact Us",
                content = "If you have any questions about this Privacy Policy, please contact the developer at:\n\nEmail: i.am.mohd.saif2006@gmail.com\nInstagram: @i.am_.saif"
            )
            
            Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
