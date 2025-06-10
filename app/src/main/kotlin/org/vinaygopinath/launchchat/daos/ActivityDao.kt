package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import org.vinaygopinath.launchchat.models.Activity

@Dao
interface ActivityDao {

    @Insert
    suspend fun create(activity: Activity): Long
}
