package org.vinaygopinath.launchchat.unittest.screens.main.domain

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.vinaygopinath.launchchat.factories.ActionFactory
import org.vinaygopinath.launchchat.factories.ActivityFactory
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.main.domain.GetRecentDetailedActivityUseCase
import java.time.Instant
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class GetRecentDetailedActivityUseCaseTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var actionRepository: ActionRepository

    @Inject
    lateinit var useCase: GetRecentDetailedActivityUseCase

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `returns an empty list if there are no activities`() = runTest {
        val list = useCase.execute().first()
        assertThat(list).isEmpty()
    }

    @Test
    fun `returns the two most recent activities`() = runTest {
        val activities = (0..5).map {
            val activity = ActivityFactory.build(occurredAt = Instant.ofEpochSecond(it.toLong()))
            activityRepository.create(activity)
        }

        val list = useCase.execute().first()

        assertThat(list.map { it.activity }).containsExactly(activities[5], activities[4]).inOrder()
    }

    @Test
    fun `returns all actions associated with the recent activity`() = runTest {
        val activity = activityRepository.create(ActivityFactory.build())
        val actions = (0..3).map {
            val action = ActionFactory.build(activityId = activity.id)
            actionRepository.create(action)
        }

        val detailedActivity = useCase.execute().first().first()

        assertThat(detailedActivity.activity).isEqualTo(activity)
        assertThat(detailedActivity.actions).isEqualTo(actions)
    }
}
