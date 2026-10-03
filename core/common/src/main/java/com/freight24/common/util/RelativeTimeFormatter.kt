package com.freight24.common.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formats a Supabase/Postgres UTC timestamp into a human-friendly
 * "Expires in …" relative string. All calendar arithmetic is performed
 * in the Asia/Kolkata (IST) timezone so the result matches what the
 * user sees on their wall clock.
 */
object RelativeTimeFormatter {

    private val IST = TimeZone.getTimeZone("Asia/Kolkata")
    private val UTC = TimeZone.getTimeZone("UTC")

    private const val SUPABASE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

    /**
     * @param timestamp Supabase UTC timestamp like "2025-11-20T10:30:00"
     *                  (optional fractional seconds / timezone suffix are
     *                  tolerated and ignored).
     * @return "Expires in 1d 12h", "Expires in 45m", "Expires in <1m",
     *         "Expired", or "--" if the input is unparseable.
     */
    fun formatExpiry(timestamp: String?): String {        if (timestamp.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat(SUPABASE_PATTERN, Locale.getDefault()).apply {
                timeZone = UTC
            }
            val clean = stripFractionalAndZone(timestamp)
            val expiresUtc = parser.parse(clean) ?: return "--"

            val nowIst = Calendar.getInstance(IST).timeInMillis
            // Convert the parsed UTC instant to "IST wall clock millis"
            // by re-aligning it to the IST timezone's epoch representation.
            val expiresIst = toIstMillis(expiresUtc)

            val diffMillis = expiresIst - nowIst
            if (diffMillis <= 0) return "Expired"

            val totalMinutes = diffMillis / 60_000
            val totalHours = totalMinutes / 60
            val totalDays = totalHours / 24

            when {
                totalMinutes < 1 -> "Expires in <1m"
                totalMinutes < 60 -> "Expires in ${totalMinutes}m"
                totalHours < 24 -> {
                    val h = totalHours.toInt()
                    val m = (totalMinutes % 60).toInt()
                    if (m > 0) "Expires in ${h}h ${m}m" else "Expires in ${h}h"
                }
                else -> {
                    val d = totalDays.toInt()
                    val h = (totalHours % 24).toInt()
                    if (h > 0) "Expires in ${d}d ${h}h" else "Expires in ${d}d"
                }
            }
        } catch (e: Exception) {
            Log.e("RelativeTimeFormatter", "Invalid UTC timestamp: $timestamp", e)
            "--"
        }
    }

    /**
     * Formats a Supabase/Postgres UTC timestamp into a human-friendly IST
     * "posted on" string: "Today, 10:30 AM IST", "Yesterday, 9:05 PM IST",
     * "4 Sep, 9:08 AM IST", or "4 Sep 2025, 9:08 AM IST" across years.
     *
     * @param timestamp Supabase UTC timestamp like "2025-11-20T10:30:00"
     *                  (optional fractional seconds / timezone suffix are
     *                  tolerated and ignored).
     * @return the IST wall-clock string, or "" when [timestamp] is blank or
     *         unparseable so callers can hide the row instead of showing a
     *         placeholder.
     */
    fun formatCreatedIst(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return try {
            val parser = SimpleDateFormat(SUPABASE_PATTERN, Locale.getDefault()).apply {
                timeZone = UTC
            }
            val clean = stripFractionalAndZone(timestamp)
            val instant = parser.parse(clean) ?: return ""

            val created = Calendar.getInstance(IST).apply { time = instant }
            val now = Calendar.getInstance(IST)
            val yesterday = Calendar.getInstance(IST).apply { add(Calendar.DAY_OF_YEAR, -1) }

            val timePart = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                timeZone = IST
            }.format(instant)

            val sameDay = created.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                created.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            val isYesterday = created.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                created.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
            val sameYear = created.get(Calendar.YEAR) == now.get(Calendar.YEAR)

            when {
                sameDay -> "Today, $timePart IST"
                isYesterday -> "Yesterday, $timePart IST"
                sameYear -> {
                    val datePart = SimpleDateFormat("d MMM", Locale.getDefault()).apply {
                        timeZone = IST
                    }.format(instant)
                    "$datePart, $timePart IST"
                }
                else -> {
                    val datePart = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).apply {
                        timeZone = IST
                    }.format(instant)
                    "$datePart, $timePart IST"
                }
            }
        } catch (e: Exception) {
            Log.e("RelativeTimeFormatter", "Invalid UTC timestamp: $timestamp", e)
            ""
        }
    }

    /**
     * Formats a Supabase/Postgres UTC timestamp into how long ago it
     * happened: "Just now", "12 mins ago", "5 hours ago", "3 days ago",
     * "2 weeks ago".
     *
     * The diff is computed on the absolute instants (both sides parsed as
     * UTC), so the result is timezone-independent — unlike [formatExpiry],
     * which has to re-anchor into IST because it reads a wall clock.
     *
     * @return "" when [timestamp] is blank or unparseable so callers can
     *         hide the row rather than print a placeholder.
     */
    fun formatElapsed(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return try {
            val parser = SimpleDateFormat(SUPABASE_PATTERN, Locale.getDefault()).apply {
                timeZone = UTC
            }
            val instant = parser.parse(stripFractionalAndZone(timestamp)) ?: return ""

            val elapsedMillis = System.currentTimeMillis() - instant.time
            if (elapsedMillis < 0) return ""

            val totalMinutes = elapsedMillis / 60_000
            val totalHours = totalMinutes / 60
            val totalDays = totalHours / 24

            when {
                totalMinutes < 1 -> "Just now"
                totalMinutes == 1L -> "1 min ago"
                totalMinutes < 60 -> "$totalMinutes mins ago"
                totalHours == 1L -> "1 hour ago"
                totalHours < 24 -> "$totalHours hours ago"
                totalDays == 1L -> "Yesterday"
                totalDays < 7 -> "$totalDays days ago"
                totalDays < 14 -> "1 week ago"
                else -> "${totalDays / 7} weeks ago"
            }
        } catch (e: Exception) {
            Log.e("RelativeTimeFormatter", "Invalid UTC timestamp: $timestamp", e)
            ""
        }
    }

    /**
     * Re-anchors a [utcInstant] (the absolute moment in time) into the
     * IST timezone so the diff against `now` in IST stays timezone-safe.
     */
    private fun toIstMillis(utcInstant: Date): Long {
        val calUtc = Calendar.getInstance(UTC).apply { time = utcInstant }
        val calIst = Calendar.getInstance(IST).apply {
            clear()
            set(
                calUtc.get(Calendar.YEAR),
                calUtc.get(Calendar.MONTH),
                calUtc.get(Calendar.DAY_OF_MONTH),
                calUtc.get(Calendar.HOUR_OF_DAY),
                calUtc.get(Calendar.MINUTE),
                calUtc.get(Calendar.SECOND),
            )
        }
        return calIst.timeInMillis
    }

    private fun stripFractionalAndZone(timestamp: String): String {
        // Trim fractional seconds and any trailing timezone suffix ("Z" or "+05:30").
        var s = timestamp
        val dot = s.indexOf('.')
        if (dot >= 0) s = s.substring(0, dot)
        val plus = s.indexOfAny(charArrayOf('+', '-'), startIndex = 11)
        if (plus > 0) s = s.substring(0, plus)
        if (s.endsWith("Z")) s = s.removeSuffix("Z")
        return s
    }
}
