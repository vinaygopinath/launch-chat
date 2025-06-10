package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import org.vinaygopinath.launchchat.models.Action

@Dao
interface ActionDao {

    @Insert
    suspend fun create(action: Action): Long
}
