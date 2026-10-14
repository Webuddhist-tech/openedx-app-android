package org.openedx.core.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseScheduleResponseTest {

    @Test
    fun `disabled schedule returns no sessions`() {
        val response = CourseScheduleResponse(
            enabled = false,
            sessions = listOf(session()),
        )

        assertTrue(response.toSchedules().isEmpty())
    }

    @Test
    fun `enabled schedule keeps wall clock fields`() {
        val schedules = CourseScheduleResponse(
            enabled = true,
            timezone = "Asia/Kolkata",
            sessions = listOf(session()),
        ).toSchedules()

        assertEquals(1, schedules.size)
        assertEquals("2026-10-19", schedules.first().startDate)
        assertEquals("08:30", schedules.first().startTime)
        assertEquals("IST", schedules.first().timezoneAbbr)
    }

    private fun session() = SectionScheduleModel(
        sectionId = "block-v1:test",
        title = "Morning session",
        startDate = "2026-10-19",
        endDate = "2026-10-19",
        numDays = 1,
        startTime = "08:30",
        endTime = "11:30",
        timezoneAbbr = "IST",
    )
}
