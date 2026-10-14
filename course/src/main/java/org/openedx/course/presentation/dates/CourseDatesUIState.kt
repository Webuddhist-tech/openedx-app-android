package org.openedx.course.presentation.dates

import org.openedx.core.domain.model.CourseDatesResult
import org.openedx.core.domain.model.DatesSection
import org.openedx.core.domain.model.SectionSchedule
import org.openedx.core.presentation.settings.calendarsync.CalendarSyncState

sealed interface CourseDatesUIState {
    data class CourseDates(
        val courseDatesResult: CourseDatesResult,
        val calendarSyncState: CalendarSyncState,
        val scheduleSections: Map<DatesSection, List<SectionSchedule>> = emptyMap(),
    ) : CourseDatesUIState

    data object Error : CourseDatesUIState
    data object Loading : CourseDatesUIState
}
