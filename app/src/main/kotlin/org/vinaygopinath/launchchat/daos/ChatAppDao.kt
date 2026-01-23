package org.vinaygopinath.launchchat.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.models.ChatApp

@Dao
interface ChatAppDao {

    @Query("SELECT COUNT(*) FROM " + ChatApp.TABLE_NAME)
    fun getCount(): Int

    @Query(
        "SELECT * FROM " + ChatApp.TABLE_NAME +
            " WHERE is_enabled = 1 AND deleted_at IS NULL" +
            " ORDER BY position ASC"
    )
    fun getEnabledChatApps(): Flow<List<ChatApp>>

    @Query(
        "SELECT * FROM " + ChatApp.TABLE_NAME +
            " WHERE deleted_at IS NULL" +
            " ORDER BY position ASC"
    )
    fun getAllChatApps(): Flow<List<ChatApp>>

    @Query("SELECT * FROM " + ChatApp.TABLE_NAME + " WHERE id = :id")
    suspend fun getById(id: Long): ChatApp?

    @Insert
    suspend fun create(chatApp: ChatApp): Long

    @Insert
    fun create(chatApps: Iterable<ChatApp>)

    @Update
    suspend fun update(chatApp: ChatApp)

    @Query("UPDATE " + ChatApp.TABLE_NAME + " SET deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long)

    @Query("UPDATE " + ChatApp.TABLE_NAME + " SET is_enabled = :isEnabled WHERE id = :id")
    suspend fun updateEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE " + ChatApp.TABLE_NAME + " SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    @Query("SELECT COALESCE(MAX(position), -1) FROM " + ChatApp.TABLE_NAME + " WHERE deleted_at IS NULL")
    suspend fun getMaxPosition(): Int
}
