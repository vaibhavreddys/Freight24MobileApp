package com.freight24.common.util

/**
 * Returns an error message if `negotiatedCost` is a valid number that exceeds `clientCost`.
 * Returns null when both fields are empty, when either side isn't a valid number, or when
 * the negotiated value is within the client cost. The check tolerates formatted input
 * (commas) since these fields are formatted as Indian currency.
 */
fun negotiatedCostExceedsClientError(
    negotiatedCost: String,
    clientCost: String,
): String? = costExceedsClientError(negotiatedCost, clientCost)

/**
 * The same check for the business-proposed cost, which is the other figure that
 * must never sit above what the client pays.
 */
fun proposedCostExceedsClientError(
    proposedCost: String,
    clientCost: String,
): String? = costExceedsClientError(proposedCost, clientCost)

/**
 * Returns an error message if [cost] is a valid number that exceeds [clientCost].
 * Returns null when either side is empty or not a valid number, or when [cost] is
 * within the client cost. The check tolerates formatted input (commas) since
 * these fields are formatted as Indian currency.
 */
private fun costExceedsClientError(
    cost: String,
    clientCost: String,
): String? {
    val value = cost.replace(",", "").toDoubleOrNull() ?: return null
    val client = clientCost.replace(",", "").toDoubleOrNull() ?: return null
    return if (value > client) "This cost can not exceed Client Cost" else null
}
