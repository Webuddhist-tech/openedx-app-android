package org.openedx.core.domain.model

/**
 * One taught session from the course schedule API.
 *
 * Dates and times are wall-clock values in the course timezone. They are kept
 * as strings so the device timezone cannot shift them.
 */
data class SectionSchedule(
    val sectionId: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val numDays: Int,
    val startTime: String?,
    val endTime: String?,
    val timezoneAbbr: String,
    val instructor: String,
    val toolsRequired: String,
    val notes: String,
    val contentAvailableFrom: String?,
)
