package com.example.util

import com.example.data.model.CurrencyOption
import com.example.data.model.SupportedCurrencies
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)

    fun formatCurrency(
        amount: Double,
        currency: CurrencyOption = SupportedCurrencies.getByCode("USD"),
        isHidden: Boolean = false
    ): String {
        if (isHidden) {
            return "${currency.symbol}••••"
        }
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,##0.00", symbols)
        return "${currency.symbol}${formatter.format(amount)}"
    }

    fun formatDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun formatDaysRemaining(dueDateMillis: Long): String {
        val now = System.currentTimeMillis()
        val diffMillis = dueDateMillis - now
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000L)).toInt()

        return when {
            diffDays < 0 -> "Overdue by ${-diffDays} ${if (-diffDays == 1) "day" else "days"}"
            diffDays == 0 -> "Due today!"
            diffDays == 1 -> "Due tomorrow"
            else -> "Due in $diffDays days"
        }
    }
}
