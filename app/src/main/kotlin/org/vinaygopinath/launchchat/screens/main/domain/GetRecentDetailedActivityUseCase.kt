package org.vinaygopinath.launchchat.screens.main.domain

import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.repositories.DetailedActivityRepository
import javax.inject.Inject

class GetRecentDetailedActivityUseCase @Inject constructor(
    private val detailedActivityRepository: DetailedActivityRepository
) {

    fun execute(): Flow<List<DetailedActivity>> {
        return detailedActivityRepository.getRecentDetailedActivities()
    }
}
