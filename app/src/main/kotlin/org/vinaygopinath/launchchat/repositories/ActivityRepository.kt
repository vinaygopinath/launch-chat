package org.vinaygopinath.launchchat.repositories

import org.vinaygopinath.launchchat.daos.ActionDao
import org.vinaygopinath.launchchat.daos.ActivityDao
import org.vinaygopinath.launchchat.models.Activity
import javax.inject.Inject

class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val actionDao: ActionDao
) {

    suspend fun deleteByIds(activityIds: Set<Long>) {
        actionDao.deleteByActivityIds(activityIds.toList())
    }

    suspend fun deleteActionsByActivityIds(activityIds: Set<Long>) {
        activityDao.deleteByIds(activityIds.toList())
    }

    suspend fun delete(activity: Activity) {
        deleteActionsByActivityIds(setOf(activity.id))
        activityDao.delete(activity.id)
    }

    suspend fun create(activity: Activity): Activity {
        val newId = activityDao.create(activity)
        return activity.copy(id = newId)
    }

    suspend fun deleteActivitiesByIds(activityIds: List<Long>) {
        activityDao.deleteByIds(activityIds.toList())
    }
}