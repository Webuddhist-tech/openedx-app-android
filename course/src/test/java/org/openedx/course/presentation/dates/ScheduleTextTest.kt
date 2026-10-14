package org.openedx.course.presentation.dates

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openedx.core.domain.model.SectionSchedule

class ScheduleTextTest {

    @Test
    fun `single day session keeps the course clock`() {
        val text = formatSessionWhen(
            session(startDate = "2026-10-19", endDate = "2026-10-19", numDays = 1)
        )

        assertEquals("19 Oct 2026 · 8:30 am–11:30 am IST", text)
    }

    @Test
    fun `multi day session shows the date range once`() {
        val text = formatSessionDateRange(
            session(startDate = "2026-10-13", endDate = "2026-10-17", numDays = 5)
        )

        assertEquals("13 Oct – 17 Oct 2026", text)
    }

    @Test
    fun `evening times stay on a twelve hour clock`() {
        assertEquals("7:00 pm", formatWallTime("19:00"))
        assertEquals("12:00 am", formatWallTime("00:00"))
        assertEquals("12:15 pm", formatWallTime("12:15"))
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
        instructor = "Ada",
        toolsRequired = "Zoom",
        notes = "",
        contentAvailableFrom = null,
    )
}
