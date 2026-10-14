package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.core.domain.model.SectionSchedule

data class CourseScheduleResponse(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    @SerializedName("timezone")
    val timezone: String = "",
    @SerializedName("sessions")
    val sessions: List<SectionScheduleModel> = emptyList(),
) {
    fun toSchedules(): List<SectionSchedule> {
        if (!enabled) {
            return emptyList()
        }
        return sessions.map { it.mapToDomain() }
    }
}

data class SectionScheduleModel(
    @SerializedName("section_id")
    val sectionId: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("start_date")
    val startDate: String = "",
    @SerializedName("end_date")
    val endDate: String = "",
    @SerializedName("num_days")
    val numDays: Int = 1,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("timezone_abbr")
    val timezoneAbbr: String = "",
    @SerializedName("instructor")
    val instructor: String = "",
    @SerializedName("tools_required")
    val toolsRequired: String = "",
    @SerializedName("notes")
    val notes: String = "",
    @SerializedName("content_available_from")
    val contentAvailableFrom: String? = null,
) {
    fun mapToDomain() = SectionSchedule(
        sectionId = sectionId,
        title = title,
        startDate = startDate,
        endDate = endDate,
        numDays = numDays,
        startTime = startTime,
        endTime = endTime,
        timezoneAbbr = timezoneAbbr,
        instructor = instructor,
        toolsRequired = toolsRequired,
        notes = notes,
        contentAvailableFrom = contentAvailableFrom,
    )
}
