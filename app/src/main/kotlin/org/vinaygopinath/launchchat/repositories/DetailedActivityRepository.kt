package org.vinaygopinath.launchchat.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.daos.DetailedActivityDao
import org.vinaygopinath.launchchat.models.DetailedActivity
import javax.inject.Inject

class DetailedActivityRepository @Inject constructor(
    private val detailedActivityDao: DetailedActivityDao
) {

    fun getRecentDetailedActivities(): Flow<List<DetailedActivity>> {
        return detailedActivityDao.getRecentDetailedActivities()
    }

    fun getNewPagingSource(): DetailedActivityPagingSource {
        return DetailedActivityPagingSource(detailedActivityDao)
    }

    class DetailedActivityPagingSource(
        private val detailedActivityDao: DetailedActivityDao
    ) : PagingSource<Int, DetailedActivity>() {
        override fun getRefreshKey(state: PagingState<Int, DetailedActivity>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DetailedActivity> {
            val currentPage = params.key ?: 0
            return try {
                val detailedActivities = detailedActivityDao.getDetailedActivities(
                    pageSize = params.loadSize,
                    pageNumber = currentPage
                )
                val nextKey = if (detailedActivities.isEmpty()) {
                    null
                } else {
                    currentPage + (params.loadSize) / PAGE_SIZE
                }

                LoadResult.Page(
                    data = detailedActivities,
                    nextKey = nextKey,
                    prevKey = if (currentPage == 0) {
                        null
                    } else {
                        currentPage - 1
                    }
                )
            } catch (genericException: Exception) {
                LoadResult.Error(genericException)
            }
        }

        companion object {
            const val PAGE_SIZE = 10
        }
    }
}
