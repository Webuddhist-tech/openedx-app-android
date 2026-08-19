package org.openedx.course.presentation.unit.container

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.openedx.core.BlockType
import org.openedx.core.CoreMocks
import org.openedx.core.config.Config
import org.openedx.core.domain.helper.VideoPreviewHelper
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseCompletionSet
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.course.domain.interactor.CourseInteractor
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.foundation.system.ResourceManager
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class CourseUnitContainerViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private val config = mockk<Config>()
    private val interactor = mockk<CourseInteractor>()
    private val notifier = mockk<CourseNotifier>()
    private val analytics = mockk<CourseAnalytics>()
    private val networkConnection = mockk<NetworkConnection>()
    private val videoPreviewHelper = mockk<VideoPreviewHelper>()
    private val resourceManager = mockk<ResourceManager>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { videoPreviewHelper.getVideoPreviews(any(), any()) } returns emptyMap()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getBlocks no internet connection exception`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.FULL,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )

        coEvery { interactor.getCourseStructure(any()) } throws UnknownHostException()
        coEvery { interactor.getCourseStructureForVideos(any()) } throws UnknownHostException()

        viewModel.loadBlocks()
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getCourseStructure(any()) }
    }

    @Test
    fun `getBlocks unknown exception`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.FULL,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )

        coEvery { interactor.getCourseStructure(any()) } throws UnknownHostException()
        coEvery { interactor.getCourseStructureForVideos(any()) } throws UnknownHostException()

        viewModel.loadBlocks()
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getCourseStructure(any()) }
    }

    @Test
    fun `getBlocks unknown success`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )

        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks()

        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure(any()) }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos(any()) }
    }

    @Test
    fun setupCurrentIndex() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks("id")
        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure(any()) }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos(any()) }
    }

    @Test
    fun `getCurrentBlock test`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks("id")

        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure("") }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos("") }
        assert(viewModel.getCurrentBlock().id == "id")
    }

    @Test
    fun `moveToPrevBlock null`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks("id3")

        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure("") }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos("") }
        assert(viewModel.moveToPrevBlock() == null)
    }

    @Test
    fun `moveToPrevBlock not null`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "id",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks("id1")

        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure("") }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos("") }
        assert(viewModel.moveToPrevBlock() != null)
    }

    @Test
    fun `moveToNextBlock null`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks("id3")

        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure("") }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos("") }
        assert(viewModel.moveToNextBlock() == null)
    }

    @Test
    fun `moveToNextBlock not null`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "id",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )
        coEvery { interactor.getCourseStructure("") } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos("") } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks("id")

        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure("") }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos("") }
        assert(viewModel.moveToNextBlock() != null)
    }

    @Test
    fun `currentIndex isLastIndex`() = runTest {
        every { notifier.notifier } returns MutableSharedFlow()
        val viewModel = CourseUnitContainerViewModel(
            "",
            "",
            CourseViewMode.VIDEOS,
            config,
            interactor,
            notifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure

        viewModel.loadBlocks("id3")

        advanceUntilIdle()

        coVerify(exactly = 0) { interactor.getCourseStructure(any()) }
        coVerify(exactly = 1) { interactor.getCourseStructureForVideos(any()) }
    }

    @Test
    fun `nested library child is shown and completed locally`() = runTest {
        val courseNotifier = CourseNotifier()
        val sequential = CoreMocks.mockChapterBlock.copy(
            id = "sequential",
            type = BlockType.SEQUENTIAL,
            descendants = listOf("vertical")
        )
        val vertical = CoreMocks.mockChapterBlock.copy(
            id = "vertical",
            type = BlockType.VERTICAL,
            descendants = listOf("library")
        )
        val library = CoreMocks.mockChapterBlock.copy(
            id = "library",
            type = BlockType.LIBRARY_CONTENT,
            descendants = listOf("problem")
        )
        val problem = CoreMocks.mockChapterBlock.copy(
            id = "problem",
            type = BlockType.PROBLEM,
            descendants = emptyList(),
            completion = 0.0
        )
        val structure = CoreMocks.mockCourseStructure.copy(
            id = "course",
            blockData = listOf(sequential, vertical, library, problem)
        )
        coEvery { interactor.getCourseStructure("course", false) } returns structure

        val viewModel = CourseUnitContainerViewModel(
            "course",
            "vertical",
            CourseViewMode.FULL,
            config,
            interactor,
            courseNotifier,
            analytics,
            networkConnection,
            videoPreviewHelper,
            resourceManager
        )

        viewModel.loadBlocks("problem")
        advanceUntilIdle()
        assertEquals(listOf("problem"), viewModel.descendantsBlocks.value.map { it.id })

        courseNotifier.send(CourseCompletionSet("course", "problem"))
        advanceUntilIdle()
        assertEquals(1.0, viewModel.descendantsBlocks.value.single().completion, 0.0)
    }
}
