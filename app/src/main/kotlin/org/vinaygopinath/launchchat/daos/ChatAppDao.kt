package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.models.ChatApp

@Dao
interface ChatAppDao {

    @Query("SELECT COUNT(*) FROM " + ChatApp.TABLE_NAME)
    fun getCount(): Int

    @Query(
        "SELECT * FROM " + ChatApp.TABLE_NAME +
            " WHERE is_enabled = 1 AND deleted_at IS NULL" +
            " ORDER BY name ASC"
    )
    fun getEnabledChatApps(): Flow<List<ChatApp>>

    @Insert
    suspend fun create(chatApp: ChatApp): Long

    @Insert
    fun create(chatApps: Iterable<ChatApp>)
}