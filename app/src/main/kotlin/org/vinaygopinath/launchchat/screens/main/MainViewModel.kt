package org.vinaygopinath.launchchat.screens.main

import android.content.ContentResolver
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.screens.main.domain.ProcessIntentUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val processIntentUseCase: ProcessIntentUseCase
) : ViewModel() {

    data class MainUiState(
        val extractedContent: ProcessIntentUseCase.ExtractedContent? = null
    )

    private val internalUiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = internalUiState.asStateFlow()

    fun processIntent(intent: Intent?, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val extractedContent = processIntentUseCase.execute(intent, contentResolver)
            internalUiState.update { currentState ->
                currentState.copy(
                    extractedContent = extractedContent
                )
            }
        }
    }
}