package org.vinaygopinath.launchchat.screens.history.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.repositories.DetailedActivityRepository
import org.vinaygopinath.launchchat.repositories.DetailedActivityRepository.DetailedActivityPagingSource.Companion.PAGE_SIZE
import javax.inject.Inject

class GetDetailedActivitiesUseCase @Inject constructor(
    private val detailedActivityRepository: DetailedActivityRepository
) {

    fun execute(): Flow<PagingData<DetailedActivity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                detailedActivityRepository.getNewPagingSource()
            }
        ).flow
    }
}
