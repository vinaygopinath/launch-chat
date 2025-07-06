package org.vinaygopinath.launchchat.integrationtest.screens.history.domain

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.vinaygopinath.launchchat.factories.ActionFactory
import org.vinaygopinath.launchchat.factories.ActivityFactory
import org.vinaygopinath.launchchat.helpers.QueryHelper
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.history.domain.DeleteSelectedActivitiesUseCase
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
class DeleteSelectedActivitiesUseCaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var actionRepository: ActionRepository

    @Inject
    lateinit var useCase: DeleteSelectedActivitiesUseCase

    @Inject
    lateinit var queryHelper: QueryHelper

    @Test
    fun `deletes the given activities and their associated actions`() = runTest {
        val activity = ActivityFactory.build(id = 10).also { activityRepository.create(it) }
        (1..3).forEach {
            ActionFactory.build(id = it.toLong(), activityId = activity.id).also {
                actionRepository.create(it)
            }
        }

        useCase.execute(setOf(activity.id))

        assertThat(queryHelper.queryTableRowCount("activities")).isEqualTo(0)
        assertThat(queryHelper.queryTableRowCount("actions")).isEqualTo(0)
    }

    @Test
    fun `does not delete other activities and their associated actions`() = runTest {
        val activityToBeDeleted =
            ActivityFactory.build(id = 10).also { activityRepository.create(it) }
        (1..3).forEach { actionId ->
            ActionFactory.build(id = actionId.toLong(), activityId = activityToBeDeleted.id).also {
                actionRepository.create(it)
            }
        }
        val unrelatedActicity =
            ActivityFactory.build(id = 11).also { activityRepository.create(it) }
        val unrelatedActions = (4..6).map { actionId ->
            ActionFactory.build(id = actionId.toLong(), activityId = unrelatedActicity.id).also {
                actionRepository.create(it)
            }
        }

        useCase.execute(setOf(activityToBeDeleted.id))

        assertThat(
            queryHelper.queryRecordCountById("activities", setOf(unrelatedActicity.id))
        ).isEqualTo(1)
        assertThat(
            queryHelper.queryRecordCountById("actions", unrelatedActions.map { it.id }.toSet())
        ).isEqualTo(unrelatedActions.size)
    }
}
