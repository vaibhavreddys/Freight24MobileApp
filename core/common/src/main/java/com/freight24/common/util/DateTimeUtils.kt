package com.freight24.common.util

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

object DateTimeUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatRelativeTime(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return "--"

        return try {
            val createdDateTime =
                try {
                    // Try parsing with offset (e.g. 2026-01-04T20:57:13.3042+00:00)
                    OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime()
                } catch (e: Exception) {
                    // Fallback to local date time (e.g. 2025-12-31T18:20:08.679988)
                    LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                }

            val now = LocalDateTime.now()
            val seconds = ChronoUnit.SECONDS.between(createdDateTime, now)

            // If event is significantly in the future, show formatted date
            if (seconds < -60) {
                val fullDatePattern = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
                return createdDateTime.format(fullDatePattern)
            }

            val absSeconds = abs(seconds)
            val minutes = ChronoUnit.MINUTES.between(createdDateTime, now)
            val hours = ChronoUnit.HOURS.between(createdDateTime, now)
            val days = ChronoUnit.DAYS.between(createdDateTime, now)

            when {
                absSeconds < 60 -> "Just Now"
                minutes < 60 -> "$minutes mins ago"
                hours < 24 -> "$hours hours ago"
                days == 1L -> "Yesterday"
                else -> {
                    val datePattern = DateTimeFormatter.ofPattern("dd MMM | hh:mm a", Locale.getDefault())
                    createdDateTime.format(datePattern)
                }
            }
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "Error parsing time: $timestamp", e)
            timestamp
        }
    }
}
