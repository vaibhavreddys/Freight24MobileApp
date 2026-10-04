package com.freight24.common.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    private val IST_TIMEZONE = TimeZone.getTimeZone("Asia/Kolkata")
    private val UTC_TIMEZONE = TimeZone.getTimeZone("UTC")

    // No locale captured here - we'll create formatters with current locale when needed
    private const val SUPABASE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
    private const val DISPLAY_PATTERN = "dd MMM | hh:mm a"
    private const val ABSOLUTE_PATTERN = "MMM d, yyyy 'at' h:mm a"
    private const val PAYMENT_PATTERN = "d MMM, hh:mm a"

    /**
     * Absolute payment timestamp in IST — "23 Sept, 04:40 pm".
     *
     * Deliberately not [formatRelativeTime]: a payment is reconciled against a
     * bank statement, where "2h ago" identifies nothing. The year is dropped
     * because a trip's payments all land inside the current financial year, so
     * it only lengthens the line.
     */
    fun formatPaymentTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return "--"
        return try {
            val utcFormat = SimpleDateFormat(SUPABASE_PATTERN, Locale.getDefault()).apply {
                timeZone = UTC_TIMEZONE
            }
            val utcDate = utcFormat.parse(timestamp.substringBefore(".")) ?: return timestamp
            val displayFormat = SimpleDateFormat(PAYMENT_PATTERN, Locale.getDefault()).apply {
                timeZone = IST_TIMEZONE
            }
            displayFormat.format(utcDate)
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "Invalid UTC timestamp: $timestamp", e)
            timestamp
        }
    }

    /**
     * An absolute timestamp for display, rendered in IST.
     *
     * Supabase returns values like `2026-09-15T19:01:40.057752+00:00` —
     * microseconds *and* an offset. `LocalDateTime.parse` rejects that outright,
     * so a formatter built on it silently fell through to printing the raw ISO
     * string on screen. Truncating at the decimal and reading the remainder as
     * UTC is the same idiom [formatRelativeTime] uses, and handles both shapes.
     */
    fun formatIndentTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return "--"
        return try {
            val utcFormat = SimpleDateFormat(SUPABASE_PATTERN, Locale.getDefault()).apply {
                timeZone = UTC_TIMEZONE
            }
            val utcDate = utcFormat.parse(timestamp.substringBefore(".")) ?: return timestamp
            val displayFormat = SimpleDateFormat(ABSOLUTE_PATTERN, Locale.getDefault()).apply {
                timeZone = IST_TIMEZONE
            }
            displayFormat.format(utcDate)
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "Invalid UTC timestamp: $timestamp", e)
            timestamp
        }
    }

    fun formatRelativeTime(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return "--"

        Log.e("DateTimeUtils", "Timestamp: $timestamp")
        try {
            // Create formatters with current locale each time
            val supabaseFormat =
                SimpleDateFormat(SUPABASE_PATTERN, Locale.getDefault()).apply {
                    timeZone = UTC_TIMEZONE
                }

            // Parse the timestamp (handle optional milliseconds/microseconds)
            val cleanTimestamp = timestamp.substringBefore(".")
            val utcDate = supabaseFormat.parse(cleanTimestamp) ?: return "--"

            // Get current time in UTC to avoid double conversion issues
            val nowUtc = Calendar.getInstance(UTC_TIMEZONE).timeInMillis
            val diffMillis = nowUtc - utcDate.time

            val diffSeconds = diffMillis / 1000
            val diffMinutes = diffSeconds / 60
            val diffHours = diffMinutes / 60
            val diffDays = diffHours / 24

            return when {
                diffMinutes < 1 -> "Just now"
                diffMinutes < 60 -> "${diffMinutes}m ago"
                diffHours < 24 -> "${diffHours}h ago"
                diffDays < 7 -> "${diffDays}d ago"
                else -> {
                    // Create display formatter with current locale
                    val displayFormat =
                        SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault()).apply {
                            timeZone = IST_TIMEZONE
                        }
                    displayFormat.format(utcDate)
                }
            }
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "Invalid UTC timestamp: $timestamp", e)
            return "--"
        }
    }
}
