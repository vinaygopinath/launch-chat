package org.vinaygopinath.launchchat.screens.main.domain

import kotlinx.coroutines.flow.Flow
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.repositories.ChatAppRepository
import javax.inject.Inject

class GetEnabledChatAppsUseCase @Inject constructor(
    private val chatAppRepository: ChatAppRepository
) {

    fun execute(): Flow<List<ChatApp>> {
        return chatAppRepository.getEnabledChatApps()
    }
}
