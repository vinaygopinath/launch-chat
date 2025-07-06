package org.vinaygopinath.launchchat.unittest.screens.history.domain

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.vinaygopinath.launchchat.fakes.TransactionUtilFake
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.history.domain.DeleteSelectedActivitiesUseCase

class DeleteSelectedActivitiesUseCaseTest {

    private val transactionUtil = TransactionUtilFake(mock())
    private val activityRepository = mock<ActivityRepository>()
    private val actionRepository = mock<ActionRepository>()

    private val useCase = DeleteSelectedActivitiesUseCase(
        transactionUtil = transactionUtil,
        activityRepository = activityRepository,
        actionRepository = actionRepository
    )

    @Test
    fun `deletes the given activities and their associated actions`() = runTest {
        val dummyActivityIds = setOf(1L, 2L, 3L, 4L, 5L)
        useCase.execute(dummyActivityIds)

        verify(actionRepository).deleteByActivityIds(dummyActivityIds)
        verify(activityRepository).deleteByIds(dummyActivityIds)
    }

    @Test
    fun `should do nothing and return early if the given set of activity IDs is empty`() = runTest {
        useCase.execute(emptySet())

        verify(actionRepository, never()).deleteByActivityIds(any())
        verify(activityRepository, never()).deleteByIds(any())
    }
}
