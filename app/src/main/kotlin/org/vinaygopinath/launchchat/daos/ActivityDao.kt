package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import org.vinaygopinath.launchchat.models.Activity

@Dao
interface ActivityDao {

    @Insert
    suspend fun create(activity: Activity): Long

    @Query("DELETE FROM activities WHERE id IN (:activityIds)")
    suspend fun deleteByIds(activityIds: List<Long>)

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun delete(activityId: Long)

    @Transaction
    suspend fun deleteActivityAndActions(activityId: Long) {
        deleteActionsByActivityIds(listOf(activityId))
        delete(activityId)
    }

    @Query("DELETE FROM actions WHERE activity_id IN (:activityIds)")
    suspend fun deleteActionsByActivityIds(activityIds: List<Long>)
}
