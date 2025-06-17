package org.vinaygopinath.launchchat.screens.history.domain

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository


class DeleteActivitiesUseCaseTest {

  private val activityRepository = mock<ActivityRepository>()
  private val actionRepository = mock<ActionRepository>()
  private val useCase = DeleteActivitiesUseCase(activityRepository, actionRepository)

  @Test
  fun `deletes the given activities and their associated actions`() = runTest {
    val dummyActivityIds = setOf(1L,2L,3L,4L,5L)
    useCase.execute(dummyActivityIds)

    verify(actionRepository).deleteByActivityIds(dummyActivityIds)
    verify(activityRepository).deleteByIds(dummyActivityIds)
  }

  @Test
  fun `rolls back all changes if deleting actions fails`() {
  }

  @Test
  fun `rolls back all changes if deleting activities fails`() {
  }

  @Test
  fun `does not delete other activities and their associated actions`() {
    
  }

  @Test
  fun `should do nothing and return early if the given set of activity IDs is empty`() = runTest {
    // Setup (Define variables and the conditions of the test)

    // Action (Run something)
    useCase.execute(emptySet())

    // Assert (what is the expected behaviour?)
    verify(actionRepository, never()).deleteByActivityIds(any())
    verify(activityRepository, never()).deleteByIds(any())
  }
}