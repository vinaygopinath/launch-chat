package org.vinaygopinath.launchchat.screens.history.domain

import jakarta.inject.Inject
import org.vinaygopinath.launchchat.repositories.ActivityRepository

class DeleteActivitiesUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend fun deleteActivitiesAndActions(activityIds: Set<Long>) {
        repository.deleteActionsByActivityIds(activityIds)
        repository.deleteByIds(activityIds)
    }

}