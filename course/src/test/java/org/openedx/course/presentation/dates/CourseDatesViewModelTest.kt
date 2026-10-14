package org.openedx.course.presentation.dates

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.openedx.core.CalendarRouter
import org.openedx.core.CoreMocks
import org.openedx.core.R
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.interactor.CalendarInteractor
import kotlinx.coroutines.flow.MutableSharedFlow
import org.openedx.core.domain.model.CourseCalendarState
import org.openedx.core.domain.model.CourseDatesResult
import org.openedx.core.domain.model.DatesSection
import org.openedx.core.domain.model.SectionSchedule
import org.openedx.core.system.notifier.CalendarSyncEvent.CreateCalendarSyncEvent
import org.openedx.core.system.notifier.CourseEvent
import org.openedx.core.system.notifier.CourseLoading
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.RefreshDates
import org.openedx.core.system.notifier.calendar.CalendarEvent
import org.openedx.core.system.notifier.calendar.CalendarNotifier
import org.openedx.core.system.notifier.calendar.CalendarSynced
import org.openedx.course.CourseMocks
import org.openedx.course.domain.interactor.CourseInteractor
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.course.presentation.CourseRouter
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import java.net.UnknownHostException
import org.openedx.foundation.R as foundationR

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDatesViewModelTest {
    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private val resourceManager = mockk<ResourceManager>()
    private val notifier = mockk<CourseNotifier>()
    private val interactor = mockk<CourseInteractor>()
    private val corePreferences = mockk<CorePreferences>()
    private val analytics = mockk<CourseAnalytics>()
    private val config = mockk<Config>()
    private val courseRouter = mockk<CourseRouter>()
    private val calendarRouter = mockk<CalendarRouter>()
    private val calendarNotifier = mockk<CalendarNotifier>()
    private val calendarInteractor = mockk<CalendarInteractor>()
    private val preferencesManager = mockk<CorePreferences>()

    private val openEdx = "OpenEdx"
    private val noInternet = "Slow or no internet connection"
    private val somethingWrong = "Something went wrong"

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { resourceManager.getString(id = R.string.platform_name) } returns openEdx
        every { resourceManager.getString(foundationR.string.foundation_error_no_connection) } returns noInternet
        every { resourceManager.getString(foundationR.string.foundation_error_unknown_error) } returns somethingWrong
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        every { corePreferences.user } returns CoreMocks.mockUser
        every { corePreferences.appConfig } returns CoreMocks.mockAppConfig
        every { notifier.notifier } returns flowOf(CourseLoading(false))
        coEvery { notifier.send(any<CreateCalendarSyncEvent>()) } returns Unit
        coEvery { notifier.send(any<CourseLoading>()) } returns Unit
        every { calendarNotifier.notifier } returns flowOf(CalendarSynced)
        coEvery { calendarNotifier.send(any<CalendarEvent>()) } returns Unit
        every { preferencesManager.isRelativeDatesEnabled } returns true
        coEvery { interactor.getCourseSchedule(any(), any()) } returns emptyList()
        coEvery { calendarInteractor.getCourseCalendarStateByIdFromCache(any()) } returns CourseCalendarState(
            0,
            "",
            true
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCourseDates no internet connection exception`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = CourseDatesViewModel(
            "id",
            "",
            notifier,
            interactor,
            analytics,
            config,
            calendarInteractor,
            calendarNotifier,
            preferencesManager,
            courseRouter,
            calendarRouter,
            resourceManager,
        )
        coEvery { interactor.getCourseDates(any(), any()) } throws UnknownHostException()
        val message = async {
            withTimeoutOrNull(5000) {
                viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
            }
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getCourseDates(any(), any()) }

        Assert.assertEquals(noInternet, message.await()?.message)
        assert(viewModel.uiState.value is CourseDatesUIState.Error)
    }

    @Test
    fun `getCourseDates unknown exception`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = CourseDatesViewModel(
            "id",
            "",
            notifier,
            interactor,
            analytics,
            config,
            calendarInteractor,
            calendarNotifier,
            preferencesManager,
            courseRouter,
            calendarRouter,
            resourceManager,
        )
        coEvery { interactor.getCourseDates(any(), any()) } throws Exception()
        val message = async {
            withTimeoutOrNull(5000) {
                viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
            }
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getCourseDates(any(), any()) }

        assert(message.await()?.message.isNullOrEmpty())
        assert(viewModel.uiState.value is CourseDatesUIState.Error)
    }

    @Test
    fun `getCourseDates success with internet`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = CourseDatesViewModel(
            "id",
            "",
            notifier,
            interactor,
            analytics,
            config,
            calendarInteractor,
            calendarNotifier,
            preferencesManager,
            courseRouter,
            calendarRouter,
            resourceManager,
        )
        coEvery {
            interactor.getCourseDates(
                any(),
                any()
            )
        } returns CourseMocks.courseDatesResultWithData
        val message = async {
            withTimeoutOrNull(5000) {
                viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
            }
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getCourseDates(any(), any()) }

        assert(message.await()?.message.isNullOrEmpty())
        assert(viewModel.uiState.value is CourseDatesUIState.CourseDates)
    }

    @Test
    fun `getCourseDates success with EmptyList`() = runTest(UnconfinedTestDispatcher()) {
        val viewModel = CourseDatesViewModel(
            "id",
            "",
            notifier,
            interactor,
            analytics,
            config,
            calendarInteractor,
            calendarNotifier,
            preferencesManager,
            courseRouter,
            calendarRouter,
            resourceManager,
        )
        coEvery { interactor.getCourseDates(any(), any()) } returns CourseDatesResult(
            datesSection = linkedMapOf(),
            courseBanner = CoreMocks.mockCourseDatesBannerInfo,
        )
        val message = async {
            withTimeoutOrNull(5000) {
                viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
            }
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getCourseDates(any(), any()) }

        assert(message.await()?.message.isNullOrEmpty())
        assert(viewModel.uiState.value is CourseDatesUIState.Error)
    }

    @Test
    fun `schedule failure still shows course dates`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { interactor.getCourseDates(any(), any()) } returns CourseMocks.courseDatesResultWithData
        coEvery { interactor.getCourseSchedule(any(), any()) } throws UnknownHostException()

        val viewModel = newViewModel()
        advanceUntilIdle()

        assert(viewModel.uiState.value is CourseDatesUIState.CourseDates)
    }

    @Test
    fun `sessions are grouped without replacing assignments`() = runTest(UnconfinedTestDispatcher()) {
        val session = sampleSession(startDate = "2026-10-19")
        coEvery { interactor.getCourseDates(any(), any()) } returns CourseMocks.courseDatesResultWithData
        coEvery { interactor.getCourseSchedule(any(), any()) } returns listOf(session)

        val viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as CourseDatesUIState.CourseDates
        assert(state.courseDatesResult.datesSection.isNotEmpty())
        assert(state.scheduleSections.values.flatten().contains(session))
        assert(state.scheduleSections[DatesSection.COMPLETED].isNullOrEmpty())
    }

    @Test
    fun `empty sessions add nothing to the dates screen`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { interactor.getCourseDates(any(), any()) } returns CourseMocks.courseDatesResultWithData
        coEvery { interactor.getCourseSchedule(any(), any()) } returns emptyList()

        val viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as CourseDatesUIState.CourseDates
        assert(state.scheduleSections.isEmpty())
    }

    @Test
    fun `refresh loads dates and the schedule again`() = runTest(UnconfinedTestDispatcher()) {
        val events = MutableSharedFlow<CourseEvent>(extraBufferCapacity = 1)
        every { notifier.notifier } returns events
        coEvery { interactor.getCourseStructure(any(), any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseDates(any(), any()) } returns CourseMocks.courseDatesResultWithData

        newViewModel()
        advanceUntilIdle()
        events.tryEmit(RefreshDates)
        advanceUntilIdle()

        coVerify(exactly = 2) { interactor.getCourseDates(any(), any()) }
        coVerify(exactly = 2) { interactor.getCourseSchedule(any(), any()) }
    }

    private fun newViewModel() = CourseDatesViewModel(
        "id",
        "",
        notifier,
        interactor,
        analytics,
        config,
        calendarInteractor,
        calendarNotifier,
        preferencesManager,
        courseRouter,
        calendarRouter,
        resourceManager,
    )

    private fun sampleSession(startDate: String) = SectionSchedule(
        sectionId = "block-v1:test",
        title = "Morning session",
        startDate = startDate,
        endDate = startDate,
        numDays = 1,
        startTime = "08:30",
        endTime = "11:30",
        timezoneAbbr = "IST",
        instructor = "Ada",
        toolsRequired = "Zoom",
        notes = "Bring a notebook",
        contentAvailableFrom = null,
    )
}
