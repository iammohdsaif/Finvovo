package com.example.finvovo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finvovo.FinvovoApplication
import com.example.finvovo.data.DataRepository
import com.example.finvovo.ui.components.AppDrawer
import com.example.finvovo.ui.home.HomeScreen
import com.example.finvovo.ui.planning.PlanningScreen
import com.example.finvovo.ui.settings.SettingsScreen
import com.example.finvovo.ui.transactions.AddTransactionScreen
import com.example.finvovo.ui.transactions.TransactionListScreen
import com.example.finvovo.ui.about.AboutScreen
import com.example.finvovo.ui.askai.AskAIScreen
import androidx.compose.ui.res.painterResource
import com.example.finvovo.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.finvovo.utils.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repository: DataRepository) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContainer = (context.applicationContext as FinvovoApplication).container

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    android.widget.Toast.makeText(context, "Exporting PDF...", android.widget.Toast.LENGTH_SHORT).show()
                    val transactions = repository.allTransactions.first()
                    val success = withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                            ExportUtils.exportToPdf(outputStream, transactions)
                        } ?: false
                    }
                    if (success) {
                        android.widget.Toast.makeText(context, "Export successful!", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "Export failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    android.widget.Toast.makeText(context, "Exporting Excel...", android.widget.Toast.LENGTH_SHORT).show()
                    val transactions = repository.allTransactions.first()
                    val success = withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                            ExportUtils.exportToExcel(outputStream, transactions)
                        } ?: false
                    }
                    if (success) {
                        android.widget.Toast.makeText(context, "Export successful!", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "Export failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var showBackupDialog by remember { mutableStateOf(false) }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    android.widget.Toast.makeText(context, "Backing up data...", android.widget.Toast.LENGTH_SHORT).show()
                    val success = com.example.finvovo.data.BackupManager.exportData(context, uri, appContainer.database)
                    if (success) {
                        android.widget.Toast.makeText(context, "Backup successful!", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "Backup failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    android.widget.Toast.makeText(context, "Restoring data...", android.widget.Toast.LENGTH_SHORT).show()
                    val success = com.example.finvovo.data.BackupManager.importData(context, uri, appContainer.database)
                    if (success) {
                        android.widget.Toast.makeText(context, "Restore successful!", android.widget.Toast.LENGTH_LONG).show()
                        // Restart navigation to refresh data if needed, or just rely on Flow observation
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Restore failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Notification Permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    )

    LaunchedEffect(key1 = true) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    if (showBackupDialog) {
        com.example.finvovo.ui.components.BackupDialog(
            onDismiss = { showBackupDialog = false },
            onLocalBackup = {
                showBackupDialog = false
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                backupLauncher.launch("Finvovo_Backup_$timestamp.json")
            },
            onDriveBackup = {
                showBackupDialog = false
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                backupLauncher.launch("Finvovo_Backup_$timestamp.json")
            }
        )
    }

    fun startExport(isPdf: Boolean) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        if (isPdf) {
            pdfLauncher.launch("Transaction_Report_$timestamp.pdf")
        } else {
            excelLauncher.launch("Transaction_Report_$timestamp.xlsx")
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    val totalBalance by repository.getTotalBalance().collectAsState(initial = 0.0)

    // Tutorial State
    var showTutorial by remember { mutableStateOf(!appContainer.prefs.isTutorialCompleted) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    
    // Tutorial Targets
    var balanceRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var addFabRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var accountRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var askAiRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) } // Not capturing yet, but placeholder
    
    val tutorialSteps = remember(balanceRect, addFabRect, accountRect) {
        listOf(
            com.example.finvovo.ui.components.TutorialStep(
                targetRect = null, // Center/General
                title = "Welcome to Finvovo!",
                description = "Let's take a quick tour to help you get started with managing your finances."
            ),
            com.example.finvovo.ui.components.TutorialStep(
                targetRect = balanceRect,
                title = "Total Balance",
                description = "See your total financial health at a glance here. It aggregates all your accounts."
            ),
            com.example.finvovo.ui.components.TutorialStep(
                 targetRect = accountRect,
                 title = "Your Accounts",
                 description = "Tap on an account to view its transactions, or edit/delete it."
            ),
            com.example.finvovo.ui.components.TutorialStep(
                targetRect = addFabRect,
                title = "Add Account",
                description = "Tap this button to create new wallets, bank accounts, or crypto holdings."
            ),
             com.example.finvovo.ui.components.TutorialStep(
                targetRect = null,
                title = "You're Ready!",
                description = "That's it! Explore the app and start tracking your wealth."
            )
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                totalBalance = totalBalance,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onClose = { scope.launch { drawerState.close() } },
                onBackup = {
                    scope.launch { drawerState.close() }
                    showBackupDialog = true
                },
                onRestore = {
                    scope.launch { drawerState.close() }
                    restoreLauncher.launch(arrayOf("application/json"))
                },
                onRate = {
                     val packageName = context.packageName
                     try {
                         val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName"))
                         intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                         context.startActivity(intent)
                     } catch (e: android.content.ActivityNotFoundException) {
                         val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                         intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                         context.startActivity(intent)
                     }
                },
                onPrivacy = {
                     scope.launch { drawerState.close() }
                     navController.navigate("privacy_policy")
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        title = {
                            if (currentRoute == "settings") {
                                Text("Settings")
                            } else if (currentRoute == "about") {
                                Text("About")
                            } else if (currentRoute == "privacy_policy") {
                                Text("Privacy Policy")
                            } else if (isSearching) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search transactions...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                    )
                                )
                            } else {
                                Text("Finvovo")
                            }
                        },
                        navigationIcon = {
                            if (currentRoute == "settings" || currentRoute == "about" || currentRoute == "privacy_policy") {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },
                        actions = {
                            if (currentRoute != "settings" && currentRoute != "about" && currentRoute != "privacy_policy") {
                                if (isSearching) {
                                    IconButton(onClick = {
                                        isSearching = false
                                        searchQuery = ""
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Search")
                                    }
                                } else {
                                    IconButton(onClick = { isSearching = true }) {
                                        Icon(Icons.Default.Search, contentDescription = "Search")
                                    }
                                }

                                var showMenu by remember { mutableStateOf(false) }
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Save as PDF") },
                                        onClick = {
                                            showMenu = false
                                            startExport(true)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Save as Excel") },
                                        onClick = {
                                            showMenu = false
                                            startExport(false)
                                        }
                                    )
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    if (currentRoute == "home" || currentRoute == "planning" || currentRoute == "ask_ai") {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { 
                                    selectedTab = 0 
                                    navController.navigate("home") { launchSingleTop = true }
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { 
                                    selectedTab = 1
                                    navController.navigate("planning") { launchSingleTop = true }
                                },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = "Planning") },
                                label = { Text("Planning") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = {
                                    selectedTab = 2
                                    navController.navigate("ask_ai") { launchSingleTop = true }
                                },
                                icon = { Icon(painterResource(id = R.drawable.ic_ask_ai_v2), contentDescription = "Ask AI", modifier = Modifier.size(24.dp)) },
                                label = { Text("Ask AI") }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("home") {
                        HomeScreen(
                            repository = repository,
                            searchQuery = searchQuery,
                            onNavigateToTransactions = { accountId, accountName -> 
                                 val route = if (accountId != null) "transactions/$accountId?name=$accountName" else "transactions/all"
                                navController.navigate(route) 
                            },
                            onNavigateToAdd = { 
                                navController.navigate("add_transaction/-1") // -1 or null to indicate optional/default
                            },
                            onReportTargets = { balance, fab, account ->
                                balanceRect = balance
                                addFabRect = fab
                                accountRect = account
                            }
                        )
                    }
                    composable("planning") {
                        PlanningScreen(repository = repository)
                    }
                    composable("ask_ai") {
                        AskAIScreen(repository = repository)
                    }
                    composable("settings") {
                        SettingsScreen(
                            prefs = appContainer.prefs,
                            onNavigateToChangePin = { navController.navigate("change_pin") }
                        )
                    }
                    
                    composable("about") {
                        AboutScreen()
                    }

                    composable("privacy_policy") {
                        com.example.finvovo.ui.about.PrivacyPolicyScreen()
                    }
                    
                    composable("change_pin") {
                        var isVerified by remember { mutableStateOf(false) }
                        val currentPin = appContainer.prefs.pinCode

                        if (!isVerified && currentPin != null) {
                            com.example.finvovo.ui.security.LockScreen(
                                correctPin = currentPin,
                                isBiometricEnabled = appContainer.prefs.isBiometricEnabled,
                                onUnlock = { isVerified = true },
                                onRequestPinReset = { isVerified = true },
                                enableBiometric = true
                            )
                        } else {
                            com.example.finvovo.ui.security.PinSetupScreen(
                                onPinSet = { newPin ->
                                    appContainer.prefs.pinCode = newPin
                                    navController.popBackStack()
                                },
                                title = "Set New PIN"
                            )
                        }
                    }
                    
                    composable(
                        "transactions/{accountId}?name={name}",
                        arguments = listOf(
                            navArgument("accountId") { type = NavType.StringType }, // "all" or ID
                            navArgument("name") { type = NavType.StringType; nullable = true }
                        )
                    ) { backStackEntry ->
                        val idStr = backStackEntry.arguments?.getString("accountId")
                        val name = backStackEntry.arguments?.getString("name")
                        val accountId = if (idStr == "all") null else idStr?.toIntOrNull()
                        
                        TransactionListScreen(
                            repository = repository,
                            accountId = accountId,
                            accountName = name,
                            searchQuery = searchQuery,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAdd = { 
                                val addId = accountId ?: -1
                                navController.navigate("add_transaction/$addId") 
                            }
                        )
                    }
                    
                    composable(
                        "add_transaction/{accountId}",
                         arguments = listOf(navArgument("accountId") { type = NavType.IntType })
                    ) { backStackEntry ->
                         val id = backStackEntry.arguments?.getInt("accountId") ?: -1
                         val defaultAccountId = if (id == -1) null else id

                        AddTransactionScreen(
                            repository = repository,
                            defaultAccountId = defaultAccountId,
                            onTransactionSaved = { navController.popBackStack() }
                        )
                    }
                }
            }

            if (showTutorial) {
                com.example.finvovo.ui.components.TutorialOverlay(
                    steps = tutorialSteps,
                    currentStepIndex = currentStepIndex,
                    onStepChange = { currentStepIndex = it },
                    onComplete = {
                        appContainer.prefs.isTutorialCompleted = true
                        showTutorial = false
                    },
                    onSkip = {
                        appContainer.prefs.isTutorialCompleted = true
                        showTutorial = false
                    }
                )
            }
        }
    }
}
