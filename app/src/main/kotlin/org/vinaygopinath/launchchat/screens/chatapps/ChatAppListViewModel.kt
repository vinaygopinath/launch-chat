package org.vinaygopinath.launchchat.screens.chatapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.repositories.ChatAppRepository
import javax.inject.Inject

@HiltViewModel
class ChatAppListViewModel @Inject constructor(
    private val chatAppRepository: ChatAppRepository
) : ViewModel() {

    val chatApps: StateFlow<List<ChatApp>> = chatAppRepository.getAllChatApps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
    }

    fun toggleEnabled(chatApp: ChatApp, isEnabled: Boolean) {
        viewModelScope.launch {
            chatAppRepository.toggleEnabled(chatApp.id, isEnabled)
        }
    }

    fun onItemMoved(chatApps: List<ChatApp>) {
        viewModelScope.launch {
            chatAppRepository.updatePositions(chatApps)
        }
    }
}
