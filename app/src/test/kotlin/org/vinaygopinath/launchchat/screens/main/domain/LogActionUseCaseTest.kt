package org.vinaygopinath.launchchat.screens.main.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.vinaygopinath.launchchat.factories.ActivityFactory
import org.vinaygopinath.launchchat.factories.SettingsFactory
import org.vinaygopinath.launchchat.models.Action
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.Activity.Source
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.utils.DateUtils
import java.time.Instant

class LogActionUseCaseTest {
    private val getSettingsUseCase = mock<GetSettingsUseCase>()
    private val activityRepository = mock<ActivityRepository>()
    private val actionRepository = mock<ActionRepository>()
    private val someFixedDate = Instant.now()
    private val dateUtils: DateUtils = mock<DateUtils>().apply {
        whenever(getCurrentInstant()).thenReturn(someFixedDate)
    }
    private val useCase = LogActionUseCase(
        getSettingsUseCase = getSettingsUseCase,
        activityRepository = activityRepository,
        actionRepository = actionRepository,
        dateUtils = dateUtils
    )

    private val somePhoneNumberInputFieldText = "some-text-here-with-398387343"

    @Test
    fun `does not log an action nor activity when the activity history setting is disabled`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        useCase.execute(
            type = Action.Type.WHATSAPP,
            number = "123456789",
            message = null,
            activity = null,
            rawInputText = somePhoneNumberInputFieldText
        )

        verify(activityRepository, never()).create(any())
        verify(actionRepository, never()).create(any())
    }

    @Test
    fun `logs a new activity if one does not exist`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        whenever(activityRepository.create(any()))
            .thenAnswer { answer -> answer.getArgument<Activity>(0) }

        useCase.execute(
            type = Action.Type.WHATSAPP,
            number = "398387343",
            message = null,
            activity = null,
            rawInputText = somePhoneNumberInputFieldText
        )

        val activities = argumentCaptor<Activity> {
            verify(activityRepository).create(capture())
        }.allValues

        assertThat(activities.size).isEqualTo(1)

        val activity = activities.first()
        assertThat(activity.content).isEqualTo(somePhoneNumberInputFieldText)
        assertThat(activity.source).isEqualTo(Source.MANUAL_INPUT)
        assertThat(activity.message).isEqualTo(null)
        assertThat(activity.occurredAt).isEqualTo(someFixedDate)
    }

    @Test
    fun `does not create a new activity if one exists`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        val someActivity = ActivityFactory.build()
        useCase.execute(
            type = Action.Type.WHATSAPP,
            number = "398387343",
            message = null,
            activity = someActivity,
            rawInputText = somePhoneNumberInputFieldText
        )

        verify(activityRepository, never()).create(any())
    }

    @Test
    fun `logs a new action associated with the given activity`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        val someActivity = ActivityFactory.build()
        val type = Action.Type.WHATSAPP
        val selectedPhoneNumber = "398387343"
        val message = "Hi, this is a message"

        useCase.execute(
            type = type,
            number = selectedPhoneNumber,
            message = message,
            activity = someActivity,
            rawInputText = somePhoneNumberInputFieldText
        )

        val actions = argumentCaptor<Action> {
            verify(actionRepository).create(capture())
        }.allValues

        assertThat(actions.size).isEqualTo(1)

        val action = actions.first()

        assertThat(action.activityId).isEqualTo(someActivity.id)
        assertThat(action.phoneNumber).isEqualTo(selectedPhoneNumber)
        assertThat(action.type).isEqualTo(type)
        assertThat(action.occurredAt).isEqualTo(someFixedDate)
    }

    @Test
    fun `logs a new action after logging a new activity`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        val type = Action.Type.WHATSAPP
        val selectedPhoneNumber = "398387343"
        val message = "Hi, this is a message"

        whenever(activityRepository.create(any())).thenAnswer { answer ->
            answer.getArgument<Activity>(0)
        }

        useCase.execute(
            type = type,
            number = selectedPhoneNumber,
            message = message,
            activity = null,
            rawInputText = somePhoneNumberInputFieldText
        )

        val activity = argumentCaptor<Activity> {
            verify(activityRepository).create(capture())
        }.firstValue

        val actions = argumentCaptor<Action> {
            verify(actionRepository).create(capture())
        }.allValues

        assertThat(actions.size).isEqualTo(1)

        val action = actions.first()

        assertThat(action.activityId).isEqualTo(activity.id)
        assertThat(action.phoneNumber).isEqualTo(selectedPhoneNumber)
        assertThat(action.type).isEqualTo(type)
        assertThat(action.occurredAt).isEqualTo(someFixedDate)
    }

    @Test
    fun `returns the existing activity if one is passed`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))

        val someActivity = ActivityFactory.build()
        val returnedActivity = useCase.execute(
            type = Action.Type.WHATSAPP,
            number = "398387343",
            message = null,
            activity = someActivity,
            rawInputText = somePhoneNumberInputFieldText
        )

        assertThat(returnedActivity).isEqualTo(someActivity)
    }

    @Test
    fun `returns a new activity if one is not passed`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        whenever(activityRepository.create(any()))
            .thenAnswer { answer -> answer.getArgument<Activity>(0) }

        val returnedActivity = useCase.execute(
            type = Action.Type.WHATSAPP,
            number = "398387343",
            message = null,
            activity = null,
            rawInputText = somePhoneNumberInputFieldText
        )

        assertThat(returnedActivity).isNotNull()
    }
}
