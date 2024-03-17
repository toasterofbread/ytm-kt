package dev.toastbits.ytmkt.uistrings

import dev.toastbits.ytmkt.uistrings.localised.HMSData
import dev.toastbits.ytmkt.uistrings.localised.getHoursMinutesSecondsSuffixes

private const val HOUR_MS: Long = 3600000L

fun durationToString(duration_ms: Long, hl: String, short: Boolean = false): String {
    val string: StringBuilder = StringBuilder()

    val hours: Long = duration_ms / HOUR_MS
    val minutes: Long = (duration_ms % HOUR_MS) / 60000L
    val seconds: Long = (duration_ms % 60000L) / 1000L

    if (short) {
        if (hours > 0L) {
            string.append("$hours:$minutes:$seconds")
        }
        else {
            string.append("$minutes:$seconds")
        }
    }
    else {
        var hms = getHoursMinutesSecondsSuffixes(hl)
        if (hms == null) {
            hms = getHoursMinutesSecondsSuffixes("en")
        }
        checkNotNull(hms)

        if (hours != 0L) {
            string.append("$hours${hms.splitter}${hms.hours}")
        }

        if (minutes != 0L) {
            string.append("${hms.splitter}$minutes${hms.splitter}${hms.minutes}")
        }

        if (seconds != 0L) {
            string.append("${hms.splitter}$seconds${hms.splitter}${hms.seconds}")
        }
    }

    return string.toString()
}

fun parseYoutubeDurationString(string: String, hl: String): Long? {
    if (string.contains(':')) {
        val parts = string.split(':')

        if (parts.size !in 2..3) {
            return null
        }

        val seconds = parts.last().toLong()
        val minutes = parts[parts.size - 2].toLong()
        val hours = if (parts.size == 3) parts.first().toLong() else 0L

        return ((hours * 60 + minutes) * 60 + seconds) * 1000
    }

    var hms = getHoursMinutesSecondsSuffixes(hl)
    if (hms == null) {
        hms = getHoursMinutesSecondsSuffixes(null)
    }

    return parseHhMmSsDurationString(string, hms!!)
}

private fun parseHhMmSsDurationString(string: String, hms: HMSData): Long? {
    try {
        val parts = string.split(' ').map { it.removeSuffix("+") }

        val h = parts.indexOf(hms.hours)
        val hours =
            if (h != -1) parts[h - 1].toLong()
            else null

        val m = parts.indexOf(hms.minutes)
        val minutes =
            if (m != -1) parts[m - 1].toLong()
            else null

        val s = parts.indexOf(hms.seconds)
        val seconds =
            if (s != -1) parts[s - 1].toLong()
            else null

        if (hours == null && minutes == null && seconds == null) {
            return null
        }

        return (((hours ?: 0) * 60 + (minutes ?: 0)) * 60 + (seconds ?: 0)) * 1000
    }
    catch (e: Throwable) {
        return null
    }
}

