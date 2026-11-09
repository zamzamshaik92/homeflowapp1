package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("homeflow_preferences", Context.MODE_PRIVATE)

    private val _currencyCode = MutableStateFlow(prefs.getString(KEY_CURRENCY, "USD") ?: "USD")
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _isAmountsHidden = MutableStateFlow(prefs.getBoolean(KEY_HIDE_AMOUNTS, false))
    val isAmountsHidden: StateFlow<Boolean> = _isAmountsHidden.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    fun setCurrency(code: String) {
        prefs.edit().putString(KEY_CURRENCY, code).apply()
        _currencyCode.value = code
    }

    fun setAmountsHidden(hidden: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_AMOUNTS, hidden).apply()
        _isAmountsHidden.value = hidden
    }

    fun toggleAmountsHidden() {
        val newVal = !_isAmountsHidden.value
        setAmountsHidden(newVal)
    }

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, amount.toFloat()).apply()
        _monthlyBudget.value = amount
    }

    fun resetPreferences() {
        prefs.edit().clear().apply()
        _currencyCode.value = "USD"
        _isAmountsHidden.value = false
        _monthlyBudget.value = 0.0
    }

    companion object {
        private const val KEY_CURRENCY = "key_currency_code"
        private const val KEY_HIDE_AMOUNTS = "key_hide_amounts"
        private const val KEY_MONTHLY_BUDGET = "key_monthly_budget"
    }
}
