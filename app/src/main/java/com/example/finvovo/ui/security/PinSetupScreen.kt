package com.example.finvovo.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.finvovo.FinvovoApplication

@Composable
fun PinSetupScreen(
    onPinSet: (String) -> Unit,
    title: String? = null
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: Enter, 2: Confirm
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val appContainer = (context.applicationContext as FinvovoApplication).container
    val vibrationEnabled = appContainer.prefs.isVibrationEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title ?: (if (step == 1) "Set a 4-digit PIN" else "Confirm your PIN"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // PIN Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val currentPin = if (step == 1) pin else confirmPin
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < currentPin.length) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Keypad
        NumberPad(
            onNumberClick = { num ->
                error = null
                if (step == 1) {
                    if (pin.length < 4) pin += num
                    if (pin.length == 4) {
                        step = 2
                    }
                } else {
                    if (confirmPin.length < 4) confirmPin += num
                    if (confirmPin.length == 4) {
                        if (confirmPin == pin) {
                            onPinSet(pin)
                        } else {
                            error = "PINs do not match. Try again."
                            confirmPin = ""
                            pin = ""
                            step = 1
                        }
                    }
                }
            },
            onDeleteClick = {
                error = null
                if (step == 1) {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                } else {
                    if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                }
            },
            isVibrationEnabled = vibrationEnabled
        )
    }
}
