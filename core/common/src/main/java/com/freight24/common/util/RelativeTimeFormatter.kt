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
    fun formatExpiry(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return "--"
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
