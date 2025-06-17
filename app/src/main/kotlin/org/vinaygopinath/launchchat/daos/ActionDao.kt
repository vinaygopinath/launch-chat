package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.vinaygopinath.launchchat.models.Action

@Dao
interface ActionDao {

    @Insert
    suspend fun create(action: Action): Long

    @Query("DELETE FROM actions WHERE activity_id IN (:activityId)")
    suspend fun deleteByActivityIds(activityId: List<Long>)

}
