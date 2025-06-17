package org.vinaygopinath.launchchat.screens.history.domain

import jakarta.inject.Inject
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository

class DeleteActivitiesUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val actionRepository: ActionRepository
) {
     suspend fun execute(activityIds: Set<Long>) {
        if (activityIds.isEmpty()) {
          return
        }
        actionRepository.deleteByActivityIds(activityIds)
        activityRepository.deleteByIds(activityIds)
     }
}