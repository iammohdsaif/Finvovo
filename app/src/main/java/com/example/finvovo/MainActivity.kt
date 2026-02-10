package com.example.finvovo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.finvovo.ui.MainScreen
import com.example.finvovo.ui.security.LockScreen
import com.example.finvovo.ui.security.PinSetupScreen
import com.example.finvovo.ui.setup.SetupScreen
import com.example.finvovo.ui.splash.SplashScreen
import com.example.finvovo.ui.theme.FinvovoTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    
    private var isAppInBackground = false
    private var shouldLockApp = false
    private var lastBackgroundTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as FinvovoApplication).container

        // Lifecycle observer for App Lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                isAppInBackground = true
                lastBackgroundTimestamp = System.currentTimeMillis()
            }

            override fun onStart(owner: LifecycleOwner) {
                isAppInBackground = false
                val currentTime = System.currentTimeMillis()
                if (appContainer.prefs.pinCode != null && 
                    appContainer.prefs.isAppLockEnabled &&
                    (currentTime - lastBackgroundTimestamp > 6000)) { // 6 Seconds Cooldown
                    shouldLockApp = true
                } else {
                    shouldLockApp = false
                }
            }
        })

        setContent {
            FinvovoTheme {
                val appState = remember { mutableStateOf<AppState>(AppState.Splash) }
                val prefs = appContainer.prefs
                val scope = rememberCoroutineScope()
                
                // Handle App Resume Lock
                DisposableEffect(Unit) {
                    val observer = object : DefaultLifecycleObserver {
                        override fun onStart(owner: LifecycleOwner) {
                            if (shouldLockApp && prefs.pinCode != null && prefs.isAppLockEnabled) {
                                appState.value = AppState.Locked
                            }
                            shouldLockApp = false
                        }
                    }
                    ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
                    onDispose { ProcessLifecycleOwner.get().lifecycle.removeObserver(observer) }
                }

                // AnimatedContent for custom transitions
                AnimatedContent(
                    targetState = appState.value,
                    transitionSpec = {
                        if (initialState == AppState.Locked && targetState == AppState.Home) {
                            // Slide up lock screen to reveal home
                            (fadeIn(animationSpec = tween(1300))).togetherWith(
                                slideOutVertically(
                                    animationSpec = tween(1300, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                    targetOffsetY = { -it }
                                ) + fadeOut(animationSpec = tween(1300))
                            ).apply {
                                targetContentZIndex = -1f // Home is behind
                            }
                        } else {
                            // Default crossfade for other transitions
                            fadeIn(animationSpec = tween(400)).togetherWith(
                                fadeOut(animationSpec = tween(400))
                            )
                        }
                    },
                    label = "AppTransition"
                ) { state ->
                    when (state) {
                        AppState.Splash -> {
                            SplashScreen {
                                if (prefs.pinCode != null && prefs.isAppLockEnabled) {
                                    appState.value = AppState.Locked
                                } else if (!prefs.isSetupDone) {
                                    appState.value = AppState.Setup
                                } else {
                                    appState.value = AppState.Home
                                }
                            }
                        }
                        AppState.Locked -> {
                            LockScreen(
                                correctPin = prefs.pinCode ?: "",
                                isBiometricEnabled = prefs.isBiometricEnabled,
                                onUnlock = { appState.value = AppState.Home },
                                onRequestPinReset = { appState.value = AppState.PinSetup }
                            )
                        }
                        AppState.Setup -> {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                 SetupScreen { cash, bank ->
                                    scope.launch {
                                        appContainer.repository.initDefaultAccounts()
                                        if (cash > 0) saveInitialTransaction(cash, 1) // ID 1 is Cash
                                        if (bank > 0) saveInitialTransaction(bank, 2) // ID 2 is Bank Account
                                        prefs.isSetupDone = true
                                        appState.value = AppState.Home // Setup Wizard handles PIN now
                                    }
                                }
                            }
                        }
                        AppState.PinSetup -> {
                             PinSetupScreen(onPinSet = { newPin ->
                                 prefs.pinCode = newPin
                                 appState.value = AppState.Home
                             })
                        }
                        AppState.Home -> {
                            MainScreen(repository = appContainer.repository)
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun saveInitialTransaction(amount: Double, accountId: Int) {
         val container = (application as FinvovoApplication).container
         container.repository.addTransaction(
            com.example.finvovo.data.model.Transaction(
                type = if (accountId == 1) com.example.finvovo.data.model.TransactionType.CASH else com.example.finvovo.data.model.TransactionType.BANK,
                category = com.example.finvovo.data.model.TransactionCategory.CREDIT,
                amount = amount,
                date = System.currentTimeMillis(),
                description = "Opening Balance",
                accountId = accountId
            )
        )
    }

    enum class AppState {
        Splash, Setup, PinSetup, Locked, Home
    }
}
