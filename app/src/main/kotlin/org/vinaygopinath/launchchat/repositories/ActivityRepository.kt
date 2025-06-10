package org.vinaygopinath.launchchat.repositories

import org.vinaygopinath.launchchat.daos.ActivityDao
import org.vinaygopinath.launchchat.models.Activity
import javax.inject.Inject

class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao
) {

    suspend fun create(activity: Activity): Activity {
        val newId = activityDao.create(activity)
        return activity.copy(id = newId)
    }
}
