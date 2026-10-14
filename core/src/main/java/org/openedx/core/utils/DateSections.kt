package org.openedx.core.utils

import org.openedx.core.domain.model.DatesSection
import org.openedx.core.domain.model.SectionSchedule
import java.util.Calendar
import java.util.Date

/**
 * Places a calendar day into Today, This week, Next week, or later.
 *
 * A week runs Monday through Sunday. "This week" is the rest of the current
 * week after today, and "next week" is the following Monday through Sunday.
 */
fun classifyCalendarDay(date: Date, today: Date = Date()): DatesSection {
    val day = date.clearTime()
    val todayStart = today.clearTime()
    if (day.before(todayStart)) {
        return DatesSection.PAST_DUE
    }
    if (day == todayStart) {
        return DatesSection.TODAY
    }
    val thisSunday = sundayOfWeek(todayStart)
    if (!day.after(thisSunday)) {
        return DatesSection.THIS_WEEK
    }
    val nextSunday = thisSunday.addDays(days = 7)
    if (!day.after(nextSunday)) {
        return DatesSection.NEXT_WEEK
    }
    return DatesSection.UPCOMING
}

/** Sunday of the week that contains [day]. */
fun sundayOfWeek(day: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = day.clearTime()
    val daysUntilSunday = (Calendar.SUNDAY - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7
    calendar.add(Calendar.DATE, daysUntilSunday)
    return calendar.time
}

/**
 * A schedule date ("2026-10-19") as a local calendar day.
 *
 * These values are wall-clock dates, so they are not parsed as UTC instants.
 */
fun parseWallDate(isoDate: String): Date? {
    val parts = isoDate.split("-")
    if (parts.size != 3) {
        return null
    }
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(year, month - 1, day, 12, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun groupSectionSchedules(
    sessions: List<SectionSchedule>,
    today: Date = Date(),
): LinkedHashMap<DatesSection, List<SectionSchedule>> {
    val grouped = linkedMapOf<DatesSection, MutableList<SectionSchedule>>()
    sessions.sortedWith(compareBy({ it.startDate }, { it.startTime ?: "" }))
        .forEach { session ->
            val date = parseWallDate(session.startDate) ?: return@forEach
            val section = classifyCalendarDay(date, today)
            grouped.getOrPut(section) { mutableListOf() }.add(session)
        }
    return LinkedHashMap(grouped.mapValues { it.value.toList() })
}
