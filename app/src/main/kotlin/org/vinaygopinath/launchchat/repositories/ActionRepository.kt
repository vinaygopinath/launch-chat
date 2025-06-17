package org.vinaygopinath.launchchat.repositories

import org.vinaygopinath.launchchat.daos.ActionDao
import org.vinaygopinath.launchchat.models.Action
import javax.inject.Inject

class ActionRepository @Inject constructor(
    private val actionDao: ActionDao
) {
    suspend fun create(action: Action): Action {
        val newId = actionDao.create(action)
        return action.copy(id = newId)
    }

    suspend fun deleteByActivityIds(activityIds: Set<Long>) {
        actionDao.deleteByActivityIds(activityIds)
    }
}

