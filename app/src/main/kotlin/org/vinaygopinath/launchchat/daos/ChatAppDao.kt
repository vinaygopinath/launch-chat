package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.vinaygopinath.launchchat.models.ChatApp

@Dao
interface ChatAppDao {

    @Query("SELECT COUNT(*) FROM " + ChatApp.TABLE_NAME)
    fun getCount(): Int

    @Insert
    suspend fun create(chatApp: ChatApp): Long

    @Insert
    fun create(chatApps: Iterable<ChatApp>)
}