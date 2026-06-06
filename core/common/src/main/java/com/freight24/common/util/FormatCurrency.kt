package com.freight24.common.util

fun formatCurrency(amount: String?): String {
    if (amount.isNullOrBlank() || amount == "null") return "₹ 0"
    return try {
        val numericValue = amount.replace("[₹,\\s]".toRegex(), "").toDoubleOrNull()
        if (numericValue != null) {
            "₹ ${"%,.0f".format(numericValue)}"
        } else {
            "₹ 0"
        }
    } catch (_: Exception) {
        "₹ 0"
    }
}
