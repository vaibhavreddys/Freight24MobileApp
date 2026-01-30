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

            // Convert UTC time to IST time
            val istCalendar =
                Calendar.getInstance(IST_TIMEZONE).apply {
                    time = utcDate
                }

            // Get current time in IST
            val nowIstCalendar = Calendar.getInstance(IST_TIMEZONE)

            // Calculate difference in seconds
            val diffMillis = nowIstCalendar.timeInMillis - istCalendar.timeInMillis
            val diffSeconds = diffMillis / 1000

            return when {
                diffSeconds < 60 -> "Just now"
                diffSeconds < 3600 -> "${diffSeconds / 60} mins ago"
                diffSeconds < 86400 -> "${diffSeconds / 3600} hours ago"
                diffSeconds < 172800 -> "Yesterday"
                else -> {
                    // Create display formatter with current locale
                    val displayFormat =
                        SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault()).apply {
                            timeZone = IST_TIMEZONE
                        }
                    displayFormat.format(istCalendar.time)
                }
            }
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "Invalid UTC timestamp: $timestamp", e)
            return "--"
        }
    }
}
