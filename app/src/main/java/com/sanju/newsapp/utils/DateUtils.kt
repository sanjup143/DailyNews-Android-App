package com.sanju.newsapp.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private const val FALLBACK_PATTERN = "dd MMM yyyy"

    @Suppress("SpellCheckingInspection")
    private val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    )

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        val date = parseDate(dateString) ?: return ""

        val now = System.currentTimeMillis()
        val diff = now - date.time

        return try {
            if (diff < DateUtils.WEEK_IN_MILLIS) {
                DateUtils.getRelativeTimeSpanString(
                    date.time,
                    now,
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } else {
                formatFallback(date)
            }
        } catch (_: Exception) { // ✅ fixed
            formatFallback(date)
        }
    }

    private fun parseDate(dateString: String): Date? {
        for (pattern in inputPatterns) {
            try {
                val format = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                return format.parse(dateString)
            } catch (_: Exception) {
                // ignore and try next
            }
        }
        return null
    }

    private fun formatFallback(date: Date): String {
        return SimpleDateFormat(
            FALLBACK_PATTERN,
            Locale.getDefault()
        ).format(date)
    }
}