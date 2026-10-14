package org.openedx.course.presentation.dates

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.openedx.core.domain.model.SectionSchedule
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R

private val scheduleDetailColor = Color(0xFF8E9BAE)

@Composable
fun SectionScheduleList(
    sessions: List<SectionSchedule>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        sessions.groupBy { it.startDate }.forEach { (date, daySessions) ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                text = formatWeekday(date),
                style = MaterialTheme.appTypography.labelMedium,
                color = MaterialTheme.appColors.textDark,
            )
            daySessions.forEach { session ->
                SectionScheduleCard(session)
            }
        }
    }
}

@Composable
fun SectionScheduleCard(session: SectionSchedule) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .border(0.75.dp, MaterialTheme.appColors.cardViewBorder, MaterialTheme.shapes.medium)
            .padding(12.dp),
    ) {
        Text(
            text = session.title,
            style = MaterialTheme.appTypography.titleMedium,
            color = MaterialTheme.appColors.textDark,
        )
        val whenText = formatSessionWhen(session)
        if (whenText.isNotBlank()) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = whenText,
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textPrimaryVariant,
            )
        }
        ScheduleDetail(R.string.course_schedule_instructor, session.instructor)
        ScheduleDetail(R.string.course_schedule_tools_required, session.toolsRequired)
        if (session.notes.isNotBlank()) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                thickness = 0.5.dp,
                color = scheduleDetailColor.copy(alpha = 0.7f),
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = session.notes,
                style = MaterialTheme.appTypography.bodySmall,
                color = scheduleDetailColor,
            )
        }
        session.contentAvailableFrom?.takeIf { it.isNotBlank() }?.let { availableFrom ->
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(
                    R.string.course_schedule_content_available,
                    formatContentDate(availableFrom),
                ),
                style = MaterialTheme.appTypography.labelMedium,
                color = MaterialTheme.appColors.textWarning,
            )
        }
    }
}

@Composable
private fun ScheduleDetail(labelRes: Int, value: String) {
    if (value.isBlank()) {
        return
    }
    Text(
        modifier = Modifier.padding(top = 4.dp),
        text = stringResource(labelRes, value),
        style = MaterialTheme.appTypography.bodySmall,
        color = scheduleDetailColor,
    )
}
