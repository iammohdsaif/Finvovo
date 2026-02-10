package com.example.finvovo.ui.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.finvovo.FinvovoApplication
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    correctPin: String,
    isBiometricEnabled: Boolean,
    onUnlock: () -> Unit,
    onRequestPinReset: () -> Unit,
    enableBiometric: Boolean = true
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val appContainer = (context.applicationContext as FinvovoApplication).container
    val vibrationEnabled = appContainer.prefs.isVibrationEnabled
    
    // Handle PIN verification with a slight delay for better UX and to prevent UI freeze
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (pin == correctPin) {
                delay(150) // Small delay to show the 4th dot and finish vibration
                onUnlock()
            } else {
                delay(200)
                error = "Incorrect PIN"
                pin = ""
            }
        }
    }

    // Biometric Logic
    val biometricPromptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Finvovo")
            .setSubtitle("Use your fingerprint to continue")
            .setNegativeButtonText("Use PIN")
            .build()
    }

    val biometricPrompt = remember {
        val activity = context as? FragmentActivity
        activity?.let {
            val executor = ContextCompat.getMainExecutor(it)
            BiometricPrompt(it, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onUnlock()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })
        }
    }

    // Trigger biometric on start if enabled
    LaunchedEffect(Unit) {
        if (isBiometricEnabled && enableBiometric) {
             val biometricManager = BiometricManager.from(context)
             if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                 biometricPrompt?.authenticate(biometricPromptInfo)
             }
        }
    }

    // Forgot Password Logic
    var showForgotDialog by remember { mutableStateOf(false) }

    if (showForgotDialog) {
        ForgotPasswordDialog(
            prefs = appContainer.prefs,
            onDismiss = { showForgotDialog = false },
            onSuccess = {
                showForgotDialog = false
                onRequestPinReset()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        
        // Curved Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.35f)
                cubicTo(
                    size.width, size.height * 0.35f,
                    size.width * 0.5f, size.height * 0.45f,
                    0f, size.height * 0.35f
                )
                close()
            }
            drawPath(path = path, color = primaryColor)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            // Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    imageVector = Icons.Default.Lock, // Or Wallet/Book if available
                    contentDescription = "App Icon",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Finvovo", // Or "Account Manager" as per screenshot
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
             
             TextButton(onClick = { showForgotDialog = true }) {
                 Text(
                    text = "FORGOT PASSWORD?",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
             }

            Spacer(modifier = Modifier.height(32.dp))

            // Floating Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(), // Removed weight(1f) to prevent stretching
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    
                    // PIN Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(4) { index ->
                            val isFilled = index < pin.length
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isFilled) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // NumberPad inside Card
                    NumberPad(
                        onNumberClick = { num ->
                            if (pin.length < 4) {
                                error = null
                                pin += num
                            }
                        },
                        onDeleteClick = {
                            error = null
                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                        },
                        isVibrationEnabled = vibrationEnabled
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Footer Actions (Biometric / OK)
                    if (isBiometricEnabled && enableBiometric) {
                         TextButton(onClick = { biometricPrompt?.authenticate(biometricPromptInfo) }) {
                             Text("USE BIOMETRIC", color = MaterialTheme.colorScheme.primary)
                         }
                    }
                }
            }
             Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    prefs: com.example.finvovo.data.PreferenceManager,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var answer1 by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    
    // Safety check if questions aren't set
    if (prefs.securityQuestion1 == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Recovery Unavailable") },
            text = { Text("Security questions have not been set up. Please contact support or clear app data.") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("OK") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset PIN") },
        text = {
            Column {
                Text(
                    "Please answer your security questions to reset your PIN.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    prefs.securityQuestion1 ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = answer1,
                    onValueChange = { answer1 = it },
                    label = { Text("Answer 1") },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                )



                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answer1.equals(prefs.securityAnswer1, ignoreCase = true)) {
                        onSuccess()
                    } else {
                        error = "Incorrect answers. Please try again."
                    }
                }
            ) {
                Text("Verify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
