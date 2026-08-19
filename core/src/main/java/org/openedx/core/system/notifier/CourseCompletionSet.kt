package org.openedx.core.system.notifier

data class CourseCompletionSet(
    val courseId: String = "",
    val blockId: String = ""
) : CourseEvent
