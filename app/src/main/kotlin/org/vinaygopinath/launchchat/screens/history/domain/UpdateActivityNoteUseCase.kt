package org.vinaygopinath.launchchat.screens.history.domain

import org.vinaygopinath.launchchat.repositories.ActivityRepository
import javax.inject.Inject

class UpdateActivityNoteUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    suspend fun execute(activityId: Long, note: String?) {
        val trimmedNote = note?.trim()?.ifEmpty { null }
        activityRepository.updateNote(activityId, trimmedNote)
    }
}
