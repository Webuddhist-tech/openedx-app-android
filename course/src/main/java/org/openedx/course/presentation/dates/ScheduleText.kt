package org.openedx.course.presentation.dates

import org.openedx.core.domain.model.SectionSchedule
import org.openedx.core.utils.parseWallDate
import org.openedx.core.utils.toCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** "19 Oct 2026 · 8:30 am–11:30 am IST" */
fun formatSessionWhen(session: SectionSchedule): String {
    val dateText = formatSessionDateRange(session)
    val timeText = formatSessionTimeRange(session)
    return listOf(dateText, timeText).filter { it.isNotEmpty() }.joinToString(" · ")
}

fun formatSessionDateRange(session: SectionSchedule): String {
    val start = parseWallDate(session.startDate) ?: return session.startDate
    val end = parseWallDate(session.endDate)
    val sameDay = session.numDays <= 1 || session.startDate == session.endDate || end == null
    if (sameDay) {
        return formatDay(start, withYear = true)
    }
    val sameYear = start.toCalendar().get(Calendar.YEAR) == end.toCalendar().get(Calendar.YEAR)
    return "${formatDay(start, withYear = !sameYear)} – ${formatDay(end, withYear = true)}"
}

fun formatSessionTimeRange(session: SectionSchedule): String {
    val start = session.startTime?.let(::formatWallTime).orEmpty()
    val end = session.endTime?.let(::formatWallTime).orEmpty()
    val range = when {
        start.isNotEmpty() && end.isNotEmpty() -> "$start–$end"
        else -> start.ifEmpty { end }
    }
    if (range.isEmpty()) {
        return ""
    }
    return if (session.timezoneAbbr.isNotBlank()) "$range ${session.timezoneAbbr}" else range
}

/** "08:30" stays a wall-clock time and becomes "8:30 am". */
fun formatWallTime(time: String): String {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return time
    val minute = parts.getOrNull(1)?.padStart(2, '0') ?: "00"
    val suffix = if (hour < 12) "am" else "pm"
    val hour12 = when (hour) {
        0, 12 -> 12
        in 13..23 -> hour - 12
        else -> hour
    }
    return "$hour12:$minute $suffix"
}

fun formatWeekday(isoDate: String): String {
    val date = parseWallDate(isoDate) ?: return isoDate
    return SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
}

fun formatContentDate(isoDate: String): String {
    val date = parseWallDate(isoDate) ?: return isoDate
    return formatDay(date, withYear = true)
}

private fun formatDay(date: Date, withYear: Boolean): String {
    val pattern = if (withYear) "d MMM yyyy" else "d MMM"
    return SimpleDateFormat(pattern, Locale.ENGLISH).format(date)
}
