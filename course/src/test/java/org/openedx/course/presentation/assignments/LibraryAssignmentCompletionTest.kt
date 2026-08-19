package org.openedx.course.presentation.assignments

import org.junit.Assert.assertEquals
import org.junit.Test
import org.openedx.core.BlockType
import org.openedx.core.CoreMocks

class LibraryAssignmentCompletionTest {

    @Test
    fun `calculates completion for nested and flattened library questions`() {
        val assignment = CoreMocks.mockChapterBlock.copy(
            id = "assignment",
            type = BlockType.SEQUENTIAL,
            descendants = listOf("vertical"),
            completion = 0.0
        )
        val library = CoreMocks.mockChapterBlock.copy(
            id = "library",
            type = BlockType.LIBRARY_CONTENT,
            descendants = listOf("question1", "question2")
        )
        val question1 = CoreMocks.mockChapterBlock.copy(
            id = "question1",
            type = BlockType.PROBLEM,
            descendants = emptyList(),
            completion = 1.0
        )
        val question2 = question1.copy(id = "question2", completion = 0.0)

        fun completion(verticalDescendants: List<String>, nested: Boolean): Double? {
            val vertical = CoreMocks.mockChapterBlock.copy(
                id = "vertical",
                type = BlockType.VERTICAL,
                descendants = verticalDescendants
            )
            val blocks = listOf(
                assignment,
                vertical,
                if (nested) library else library.copy(descendants = emptyList()),
                question1,
                question2
            ).associateBy { it.id }
            return calculateLibraryAssignmentCompletion(assignment, blocks)
        }

        assertEquals(0.5, completion(listOf("library"), nested = true) ?: -1.0, 0.0)
        assertEquals(
            0.5,
            completion(listOf("library", "question1", "question2"), nested = false) ?: -1.0,
            0.0
        )
    }
}
