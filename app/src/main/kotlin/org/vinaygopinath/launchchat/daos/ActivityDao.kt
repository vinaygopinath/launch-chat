package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.vinaygopinath.launchchat.models.Activity

@Dao
interface ActivityDao {

    @Insert
    suspend fun create(activity: Activity): Long

    @Query("DELETE FROM activities WHERE id IN (:activityIds)")
    suspend fun deleteByIds(activityIds: Set<Long>)

    @Query("UPDATE activities SET note = :note WHERE id = :activityId")
    suspend fun updateNote(activityId: Long, note: String?)
}
