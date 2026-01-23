package org.vinaygopinath.launchchat.unittest.screens.history.domain

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.history.domain.UpdateActivityNoteUseCase

class UpdateActivityNoteUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val useCase = UpdateActivityNoteUseCase(activityRepository)

    @Test
    fun `updates note for given activity id`() = runTest {
        val activityId = 123L
        val note = "This is a test note"

        useCase.execute(activityId, note)

        verify(activityRepository).updateNote(activityId, note)
    }

    @Test
    fun `trims whitespace from note before saving`() = runTest {
        val activityId = 123L
        val noteWithWhitespace = "  note with spaces  "

        useCase.execute(activityId, noteWithWhitespace)

        verify(activityRepository).updateNote(activityId, "note with spaces")
    }

    @Test
    fun `converts empty string to null`() = runTest {
        val activityId = 123L

        useCase.execute(activityId, "")

        verify(activityRepository).updateNote(activityId, null)
    }

    @Test
    fun `converts whitespace-only string to null`() = runTest {
        val activityId = 123L

        useCase.execute(activityId, "   ")

        verify(activityRepository).updateNote(activityId, null)
    }

    @Test
    fun `passes null note as-is`() = runTest {
        val activityId = 123L

        useCase.execute(activityId, null)

        verify(activityRepository).updateNote(activityId, null)
    }
}
