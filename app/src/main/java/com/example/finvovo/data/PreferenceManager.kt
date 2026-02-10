package com.example.finvovo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("finvovo_prefs", Context.MODE_PRIVATE)

    var isSetupDone: Boolean
        get() = prefs.getBoolean("is_setup_done", false)
        set(value) = prefs.edit().putBoolean("is_setup_done", value).apply()

    var pinCode: String?
        get() = prefs.getString("pin_code", null)
        set(value) = prefs.edit().putString("pin_code", value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean("is_biometric_enabled", false)
        set(value) = prefs.edit().putBoolean("is_biometric_enabled", value).apply()

    var isTutorialCompleted: Boolean
        get() = prefs.getBoolean("is_tutorial_completed", false)
        set(value) = prefs.edit().putBoolean("is_tutorial_completed", value).apply()

    // Global Currency Symbol
    private val _currencySymbol = MutableStateFlow(prefs.getString("currency_symbol", "₹") ?: "₹")
    val currencySymbol: StateFlow<String> = _currencySymbol

    var selectedCurrencySymbol: String
        get() = prefs.getString("currency_symbol", "₹") ?: "₹"
        set(value) {
            prefs.edit().putString("currency_symbol", value).apply()
            _currencySymbol.value = value
        }

    // Vibration Toggle
    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

    // App Lock Toggle
    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean("is_app_lock_enabled", true)
        set(value) = prefs.edit().putBoolean("is_app_lock_enabled", value).apply()

    // Security Questions
    var securityQuestion1: String?
        get() = prefs.getString("security_question_1", null)
        set(value) = prefs.edit().putString("security_question_1", value).apply()

    var securityAnswer1: String?
        get() = prefs.getString("security_answer_1", null)
        set(value) = prefs.edit().putString("security_answer_1", value).apply()


}
