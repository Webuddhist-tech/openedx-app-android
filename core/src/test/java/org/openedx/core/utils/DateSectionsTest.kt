package org.openedx.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openedx.core.domain.model.DatesSection
import org.openedx.core.domain.model.SectionSchedule
import java.util.Calendar
import java.util.Date

class DateSectionsTest {

    @Test
    fun `wednesday splits the week at sunday and the next monday`() {
        val today = day(2026, 10, 14)

        assertEquals(DatesSection.PAST_DUE, classifyCalendarDay(day(2026, 10, 12), today))
        assertEquals(DatesSection.TODAY, classifyCalendarDay(today, today))
        assertEquals(DatesSection.THIS_WEEK, classifyCalendarDay(day(2026, 10, 15), today))
        assertEquals(DatesSection.THIS_WEEK, classifyCalendarDay(day(2026, 10, 18), today))
        assertEquals(DatesSection.NEXT_WEEK, classifyCalendarDay(day(2026, 10, 19), today))
        assertEquals(DatesSection.NEXT_WEEK, classifyCalendarDay(day(2026, 10, 25), today))
        assertEquals(DatesSection.UPCOMING, classifyCalendarDay(day(2026, 10, 26), today))
    }

    @Test
    fun `sunday has no remaining this week`() {
        val today = day(2026, 10, 18)

        assertEquals(DatesSection.TODAY, classifyCalendarDay(today, today))
        assertEquals(DatesSection.NEXT_WEEK, classifyCalendarDay(day(2026, 10, 19), today))
        assertEquals(DatesSection.NEXT_WEEK, classifyCalendarDay(day(2026, 10, 25), today))
        assertEquals(DatesSection.UPCOMING, classifyCalendarDay(day(2026, 10, 26), today))
    }

    @Test
    fun `monday keeps the rest of the week as this week`() {
        val today = day(2026, 10, 12)

        assertEquals(DatesSection.TODAY, classifyCalendarDay(today, today))
        assertEquals(DatesSection.THIS_WEEK, classifyCalendarDay(day(2026, 10, 13), today))
        assertEquals(DatesSection.THIS_WEEK, classifyCalendarDay(day(2026, 10, 18), today))
        assertEquals(DatesSection.NEXT_WEEK, classifyCalendarDay(day(2026, 10, 19), today))
    }

    @Test
    fun `week buckets cross a month boundary`() {
        val today = day(2026, 1, 30)

        assertEquals(DatesSection.THIS_WEEK, classifyCalendarDay(day(2026, 1, 31), today))
        assertEquals(DatesSection.THIS_WEEK, classifyCalendarDay(day(2026, 2, 1), today))
        assertEquals(DatesSection.NEXT_WEEK, classifyCalendarDay(day(2026, 2, 2), today))
        assertEquals(DatesSection.NEXT_WEEK, classifyCalendarDay(day(2026, 2, 8), today))
        assertEquals(DatesSection.UPCOMING, classifyCalendarDay(day(2026, 2, 9), today))
    }

    @Test
    fun `a multi day session is grouped by its start date`() {
        val today = day(2026, 10, 14)
        val session = session(startDate = "2026-10-19", endDate = "2026-10-23", numDays = 5)

        val grouped = groupSectionSchedules(listOf(session), today)

        assertEquals(listOf(session), grouped[DatesSection.NEXT_WEEK])
        assertEquals(null, grouped[DatesSection.UPCOMING])
    }

    private fun session(startDate: String, endDate: String, numDays: Int) = SectionSchedule(
        sectionId = "block-v1:test",
        title = "Morning session",
        startDate = startDate,
        endDate = endDate,
        numDays = numDays,
        startTime = "08:30",
        endTime = "11:30",
        timezoneAbbr = "IST",
        instructor = "",
        toolsRequired = "",
        notes = "",
        contentAvailableFrom = null,
    )

    private fun day(year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month - 1, day, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
