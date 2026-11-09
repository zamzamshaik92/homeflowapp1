package com.example.data.model

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)

object SupportedCurrencies {
    val list = listOf(
        CurrencyOption("USD", "$", "US Dollar ($)"),
        CurrencyOption("EUR", "€", "Euro (€)"),
        CurrencyOption("GBP", "£", "British Pound (£)"),
        CurrencyOption("INR", "₹", "Indian Rupee (₹)"),
        CurrencyOption("JPY", "¥", "Japanese Yen (¥)"),
        CurrencyOption("CAD", "CA$", "Canadian Dollar (CA$)"),
        CurrencyOption("AUD", "AU$", "Australian Dollar (AU$)"),
        CurrencyOption("BRL", "R$", "Brazilian Real (R$)"),
        CurrencyOption("ZAR", "R", "South African Rand (R)"),
        CurrencyOption("AED", "AED", "UAE Dirham (AED)"),
        CurrencyOption("SAR", "SAR", "Saudi Riyal (SAR)"),
        CurrencyOption("SGD", "SG$", "Singapore Dollar (SG$)"),
        CurrencyOption("CHF", "CHF", "Swiss Franc (CHF)"),
        CurrencyOption("CNY", "¥", "Chinese Yuan (¥)")
    )

    fun getByCode(code: String): CurrencyOption {
        return list.find { it.code.equals(code, ignoreCase = true) } ?: list.first()
    }
}
