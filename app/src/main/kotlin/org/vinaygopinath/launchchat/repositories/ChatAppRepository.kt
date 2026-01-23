package org.vinaygopinath.launchchat.repositories

import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.daos.ChatAppDao
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.utils.DateUtils
import javax.inject.Inject

class ChatAppRepository @Inject constructor(
    private val chatAppDao: ChatAppDao,
    private val dateUtils: DateUtils
) {

    fun getEnabledChatApps(): Flow<List<ChatApp>> {
        return chatAppDao.getEnabledChatApps()
    }

    fun getAllChatApps(): Flow<List<ChatApp>> {
        return chatAppDao.getAllChatApps()
    }

    suspend fun getChatAppById(id: Long): ChatApp? {
        return chatAppDao.getById(id)
    }

    suspend fun saveChatApp(chatApp: ChatApp): Long {
        return if (chatApp.id == 0L) {
            chatAppDao.create(chatApp)
        } else {
            chatAppDao.update(chatApp)
            chatApp.id
        }
    }

    suspend fun deleteChatApp(id: Long) {
        chatAppDao.softDelete(id, dateUtils.getCurrentInstant().toEpochMilli())
    }

    suspend fun toggleEnabled(id: Long, isEnabled: Boolean) {
        chatAppDao.updateEnabled(id, isEnabled)
    }

    suspend fun updatePositions(chatApps: List<ChatApp>) {
        chatApps.forEachIndexed { index, chatApp ->
            chatAppDao.updatePosition(chatApp.id, index)
        }
    }

    suspend fun getNextPosition(): Int {
        return chatAppDao.getMaxPosition() + 1
    }
}
