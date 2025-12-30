package org.vinaygopinath.launchchat.repositories

import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.daos.ChatAppDao
import org.vinaygopinath.launchchat.models.ChatApp
import javax.inject.Inject

class ChatAppRepository @Inject constructor(
    private val chatAppDao: ChatAppDao
) {

    fun getEnabledChatApps(): Flow<List<ChatApp>> {
        return chatAppDao.getEnabledChatApps()
    }
}
