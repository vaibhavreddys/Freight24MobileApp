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
