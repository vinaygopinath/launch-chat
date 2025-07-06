package org.vinaygopinath.launchchat.screens.history.domain

import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.utils.TransactionUtil
import javax.inject.Inject

class DeleteSelectedActivitiesUseCase @Inject constructor(
    private val transactionUtil: TransactionUtil,
    private val activityRepository: ActivityRepository,
    private val actionRepository: ActionRepository
) {
    suspend fun execute(activityIds: Set<Long>) {
        if (activityIds.isEmpty()) {
            return
        }

        transactionUtil.run {
            actionRepository.deleteByActivityIds(activityIds)
            activityRepository.deleteByIds(activityIds)
        }
    }
}
