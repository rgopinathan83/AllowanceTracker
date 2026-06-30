package com.yourname.allowancetracker.utils

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val decimalPlaces: Int = 2
)

val SUPPORTED_CURRENCIES = listOf(
    CurrencyInfo("USD", "$", "US Dollar"),
    CurrencyInfo("EUR", "€", "Euro"),
    CurrencyInfo("GBP", "£", "British Pound"),
    CurrencyInfo("CAD", "CA$", "Canadian Dollar"),
    CurrencyInfo("AUD", "A$", "Australian Dollar"),
    CurrencyInfo("JPY", "¥", "Japanese Yen", decimalPlaces = 0),
    CurrencyInfo("INR", "₹", "Indian Rupee"),
    CurrencyInfo("CNY", "¥", "Chinese Yuan"),
    CurrencyInfo("MXN", "MX$", "Mexican Peso"),
    CurrencyInfo("BRL", "R$", "Brazilian Real")
)

fun getCurrencyInfo(code: String): CurrencyInfo =
    SUPPORTED_CURRENCIES.find { it.code == code } ?: SUPPORTED_CURRENCIES.first()

fun formatCurrency(amount: Double, currencyCode: String): String {
    val currency = getCurrencyInfo(currencyCode)
    return "${currency.symbol}${String.format("%.${currency.decimalPlaces}f", amount)}"
}

fun formatCurrencyWithSign(amount: Double, currencyCode: String): String {
    val currency = getCurrencyInfo(currencyCode)
    val formatted = String.format("%.${currency.decimalPlaces}f", Math.abs(amount))
    return if (amount >= 0) "+${currency.symbol}$formatted" else "-${currency.symbol}$formatted"
}
