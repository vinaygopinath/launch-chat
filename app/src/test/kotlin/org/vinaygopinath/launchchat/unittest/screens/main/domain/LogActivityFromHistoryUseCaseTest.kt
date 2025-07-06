package org.vinaygopinath.launchchat.unittest.screens.main.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.vinaygopinath.launchchat.extensions.withMillisecondPrecision
import org.vinaygopinath.launchchat.factories.ActivityFactory
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.main.domain.LogActivityFromHistoryUseCase
import org.vinaygopinath.launchchat.screens.main.domain.ProcessIntentUseCase
import org.vinaygopinath.launchchat.utils.DateUtils
import java.time.Instant

class LogActivityFromHistoryUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val dateUtils = mock<DateUtils>()
    private val useCase = LogActivityFromHistoryUseCase(activityRepository, dateUtils)

    @Test
    fun `persists a new Activity with the source HISTORY`() = runTest {
        val instant = Instant.now().withMillisecondPrecision()
        whenever(dateUtils.getCurrentInstant()).thenReturn(instant)
        whenever(activityRepository.create(any())).thenReturn(ActivityFactory.build())
        val activity = ActivityFactory.build(id = 10)

        useCase.execute(activity)

        val historyActivity = argumentCaptor<Activity> {
            verify(activityRepository).create(capture())
        }.firstValue

        assertThat(historyActivity.id).isEqualTo(0)
        assertThat(historyActivity.source).isEqualTo(Activity.Source.HISTORY)
        assertThat(historyActivity.occurredAt).isEqualTo(instant)
        assertThat(historyActivity.message).isEqualTo(activity.message)
        assertThat(historyActivity.content).isEqualTo(activity.content)
    }

    @Test
    fun `returns a new Activity with the source HISTORY`() = runTest {
        val instant = Instant.now().withMillisecondPrecision()
        whenever(dateUtils.getCurrentInstant()).thenReturn(instant)
        val activity = ActivityFactory.build(id = 10)
        val returnedActivity = ActivityFactory.build(id = 0)
        whenever(activityRepository.create(any())).thenReturn(returnedActivity)

        val actualReturnedActivity = useCase.execute(activity).activity

        assertThat(actualReturnedActivity).isEqualTo(returnedActivity)
    }

    @Test
    fun `returns an ExtractedContent with the source HISTORY and previous input`() = runTest {
        val instant = Instant.now().withMillisecondPrecision()
        whenever(dateUtils.getCurrentInstant()).thenReturn(instant)
        val activity = ActivityFactory.build(id = 10)
        whenever(activityRepository.create(any())).thenReturn(activity)

        val extractedContent = useCase.execute(activity).extractedContent
        assertThat(extractedContent is ProcessIntentUseCase.ExtractedContent.PossibleResult).isTrue()

        val possibleResult = extractedContent as ProcessIntentUseCase.ExtractedContent.PossibleResult
        assertThat(possibleResult.source).isEqualTo(Activity.Source.HISTORY)
        assertThat(possibleResult.rawInputText).isEqualTo(activity.content)
        assertThat(possibleResult.rawContent).isEqualTo("")
    }
}
