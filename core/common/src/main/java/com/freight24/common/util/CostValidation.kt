package com.freight24.common.util

/**
 * Returns an error message if `negotiatedCost` is a valid number that exceeds `clientCost`.
 * Returns null when both fields are empty, when either side isn't a valid number, or when
 * the negotiated value is within the client cost. The check tolerates formatted input
 * (commas) since these fields are formatted as Indian currency.
 */
fun negotiatedCostExceedsClientError(negotiatedCost: String, clientCost: String): String? {
    val negotiated = negotiatedCost.replace(",", "").toDoubleOrNull() ?: return null
    val client = clientCost.replace(",", "").toDoubleOrNull() ?: return null
    return if (negotiated > client) "This cost can not exceed Client Cost" else null
}
