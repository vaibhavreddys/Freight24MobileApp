package com.freight24.core.common.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {
    fun formatRelativeTime(timestamp: String): String {
        return try {
            // Parse "2025-12-31T18:20:08.679988"
            // The input format is ISO_LOCAL_DATE_TIME-like
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val createdDateTime = LocalDateTime.parse(timestamp, formatter)
            val now = LocalDateTime.now()

            val seconds = ChronoUnit.SECONDS.between(createdDateTime, now)
            val minutes = ChronoUnit.MINUTES.between(createdDateTime, now)
            val hours = ChronoUnit.HOURS.between(createdDateTime, now)
            val days = ChronoUnit.DAYS.between(createdDateTime, now)

            when {
                seconds < 60 -> "Just Now"
                minutes < 60 -> "$minutes mins ago"
                hours < 24 -> "$hours hours ago"
                days == 1L -> "Yesterday"
                else -> {
                    // Fallback to simple date for older items
                    val datePattern = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
                    createdDateTime.format(datePattern)
                }
            }
        } catch (e: Exception) {
            // If parsing fails, return original or empty
            timestamp
        }
    }
}
