package com.freight24.common.util

// ---------------------------------------------------------------------------
// Indian mobile-number + countdown formatting.
//
// Pure Kotlin — no Compose dependency — so feature modules (app-trucker,
// app-connect) and core:ui Compose helpers share one implementation instead
// of each screen carrying its own 5+5 grouping or m:ss clock.
// ---------------------------------------------------------------------------

/** Groups digit input as the user types: "9876543210" -> "98765 43210". */
fun groupIndianMobileDigits(digits: String): String {
    val raw = digits.filter(Char::isDigit).take(10)
    return if (raw.length <= 5) raw else "${raw.take(5)} ${raw.drop(5)}"
}

/**
 * Formats a stored number for display. Tolerates both the E.164 form the
 * backend returns ("+919880300300") and a bare local 10-digit number.
 *
 * @param withCountryCode Prefix the result with "+91 ".
 */
fun formatIndianMobileNumber(phoneNumber: String, withCountryCode: Boolean = false): String {
    val digits = phoneNumber.filter(Char::isDigit)
    val local = when {
        digits.length > 10 && digits.startsWith("91") -> digits.drop(2)
        else -> digits
    }.take(10)
    val grouped = groupIndianMobileDigits(local)
    return if (withCountryCode) "+91 $grouped" else grouped
}

/** "284" -> "4:44". Used for OTP resend and expiry timers. */
fun formatCountdownClock(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return "${safe / 60}:${(safe % 60).toString().padStart(2, '0')}"
}
