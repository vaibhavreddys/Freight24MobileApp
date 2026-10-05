package com.freight24.common.util

/**
 * Load weight is `DECIMAL(10,2)` on the backend, so it arrives as `2`, `2.5`, `12.25`, ...
 * Kotlin's `Double.toString()` would render a whole weight as `2.0`; these helpers keep the
 * trailing zeros off so the UI shows the value the way it was entered.
 */

/** Renders [weight] without trailing zeros — `2.0` -> `2`, `2.50` -> `2.5`. */
fun formatWeightValue(weight: Double?): String {
    if (weight == null || weight.isNaN() || weight.isInfinite()) return ""
    if (weight == weight.toLong().toDouble()) return weight.toLong().toString()
    return "%.2f".format(java.util.Locale.US, weight).trimEnd('0').trimEnd('.')
}

/** Renders [weight] with [unit], e.g. `formatLoadWeight(2.5, "MT")` -> `2.5 MT`. */
fun formatLoadWeight(
    weight: Double?,
    unit: String,
): String = formatWeightValue(weight).let { value -> if (value.isEmpty()) "" else "$value $unit" }
