package org.openedx.core.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CourseStructureModelTest {

    @Test
    fun `normalization excludes only multilingual text children from units`() {
        val blocks = linkedMapOf(
            "vertical" to block(
                "vertical",
                "vertical",
                listOf("multilingual", "library", "regular")
            ),
            "multilingual" to block(
                "multilingual",
                "multilingual_text",
                listOf("english", "tibetan", "hindi")
            ),
            "english" to block("english", "html"),
            "tibetan" to block("tibetan", "html"),
            "hindi" to block("hindi", "html"),
            "library" to block("library", "library_content", listOf("problem")),
            "problem" to block("problem", "problem"),
            "regular" to block("regular", "html"),
        )

        val normalized = blocks.normalizeUnitDescendants()

        assertEquals(
            listOf("multilingual", "library", "problem", "regular"),
            normalized.getValue("vertical").descendants
        )
        assertEquals(
            listOf("english", "tibetan", "hindi"),
            normalized.getValue("multilingual").descendants
        )
        assertEquals(normalized, normalized.normalizeUnitDescendants())
    }

    private fun block(id: String, type: String, descendants: List<String> = emptyList()) = Block(
        id = id,
        blockId = id,
        lmsWebUrl = null,
        legacyWebUrl = null,
        studentViewUrl = null,
        type = type,
        displayName = id,
        graded = false,
        descendants = descendants,
        studentViewData = null,
        studentViewMultiDevice = false,
        blockCounts = BlockCounts(0),
        completion = 0.0,
        containsGatedContent = false,
        gatedContent = null,
        assignmentProgress = null,
        due = null,
        offlineDownload = null,
    )
}
