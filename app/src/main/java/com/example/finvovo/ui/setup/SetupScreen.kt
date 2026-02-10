package com.example.finvovo.ui.setup

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.finvovo.FinvovoApplication
import com.example.finvovo.R
import com.example.finvovo.ui.components.CurrencySelector
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(onComplete: (Double, Double) -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) } // 1: Currency, 2: Balance, 3: Security
    val context = LocalContext.current
    val appContainer = (context.applicationContext as FinvovoApplication).container
    val prefs = appContainer.prefs
    val scope = rememberCoroutineScope()

    // Step 1: Currency State
    var selectedCurrencySymbol by remember { mutableStateOf("₹") } // Default

    // Step 2: Balance State
    var cashBalance by remember { mutableStateOf("") }
    var bankBalance by remember { mutableStateOf("") }

    // Step 3: Security State
    var pin by remember { mutableStateOf("") }
    var securityQuestion1 by remember { mutableStateOf("") }
    var securityAnswer1 by remember { mutableStateOf("") }

    
    // UI Helpers
    val snackbarHostState = remember { SnackbarHostState() }
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Content matches step
            Column(modifier = Modifier.fillMaxSize()) {
                 Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentStep,
                        label = "SetupWizard"
                    ) { step ->
                        when (step) {
                            1 -> CurrencyStep(
                                currentSymbol = selectedCurrencySymbol,
                                onCurrencySelected = { symbol ->
                                    selectedCurrencySymbol = symbol
                                    prefs.selectedCurrencySymbol = symbol
                                    currentStep = 2
                                }
                            )
                            2 -> BalanceStep(
                                cashBalance = cashBalance,
                                onCashChange = { cashBalance = it },
                                bankBalance = bankBalance,
                                onBankChange = { bankBalance = it },
                                currencySymbol = selectedCurrencySymbol,
                                onNext = { currentStep = 3 }
                            )
                            3 -> SecurityStep(
                                pin = pin,
                                onPinChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                                q1 = securityQuestion1,
                                onQ1Change = { securityQuestion1 = it },
                                a1 = securityAnswer1,
                                onA1Change = { securityAnswer1 = it },

                                onFinish = {
                                    if (pin.length != 4) {
                                        scope.launch { snackbarHostState.showSnackbar("PIN must be 4 digits") }
                                        return@SecurityStep
                                    }
                                    if (securityQuestion1.isBlank() || securityAnswer1.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Please fill all security fields") }
                                        return@SecurityStep
                                    }

                                    // Save Security Data
                                    prefs.pinCode = pin
                                    prefs.securityQuestion1 = securityQuestion1
                                    prefs.securityAnswer1 = securityAnswer1


                                    // Finish
                                    val cash = cashBalance.toDoubleOrNull() ?: 0.0
                                    val bank = bankBalance.toDoubleOrNull() ?: 0.0
                                    onComplete(cash, bank)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyStep(
    currentSymbol: String,
    onCurrencySelected: (String) -> Unit
) {
    CurrencySelector(
        currentCurrencySymbol = currentSymbol,
        onCurrencySelected = onCurrencySelected
    )
}

@Composable
fun BalanceStep(
    cashBalance: String,
    onCashChange: (String) -> Unit,
    bankBalance: String,
    onBankChange: (String) -> Unit,
    currencySymbol: String,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "Welcome to Finvovo",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Let's set up your initial balances",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = cashBalance,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onCashChange(it) },
                    label = { Text("Initial Cash Balance") },
                    prefix = { Text(currencySymbol) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = bankBalance,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onBankChange(it) },
                    label = { Text("Initial Bank Balance") },
                    prefix = { Text(currencySymbol) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Next", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityStep(
    pin: String,
    onPinChange: (String) -> Unit,
    q1: String,
    onQ1Change: (String) -> Unit,
    a1: String,
    onA1Change: (String) -> Unit,
    onFinish: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Scroll state for smaller screens
    val scrollState = rememberScrollState()
    
    // Sample Security Questions
    val questions = listOf(
        "What was the first mobile that you purchased?",
        "What was the name of your best friend at childhood?",
        "What was the name of your first pet?",
        "What is your favourite children's book?",
        "What was the first film you saw in the cinema?",
        "What was the name of your favourite teacher at primary school?",
        "What is the name of your favourite sports team?",
        "Who was your favourite singer or band?",
        "What is your first job?",
        "What was the first dish you learned to cook?",
        "What was the model of your first motorised vehicle?",
        "What was your childhood nickname?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Setup Security",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        )

        // PIN Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Set Password (PIN)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = onPinChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("****") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    }
                )
                Text(
                    "Password must be 4 characters long.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Set security questions to recover your password when you forget.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question 1
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
             border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
             Column(modifier = Modifier.padding(24.dp)) {
                SecurityQuestionDropdown(
                    label = "Security Question 1",
                    selectedQuestion = q1,
                    onQuestionSelected = onQ1Change,
                    questions = questions
                )
                OutlinedTextField(
                    value = a1,
                    onValueChange = onA1Change,
                    label = { Text("Answer 1") },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onFinish() })
                )
             }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Finish Setup", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityQuestionDropdown(
    label: String,
    selectedQuestion: String,
    onQuestionSelected: (String) -> Unit,
    questions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedQuestion,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            questions.forEach { question ->
                DropdownMenuItem(
                    text = { Text(question) },
                    onClick = {
                        onQuestionSelected(question)
                        expanded = false
                    }
                )
            }
        }
    }
}
