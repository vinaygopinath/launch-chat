package org.vinaygopinath.launchchat.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import org.vinaygopinath.launchchat.screens.history.domain.DeleteSelectedActivitiesUseCase
import org.vinaygopinath.launchchat.screens.history.domain.GetDetailedActivitiesUseCase
import org.vinaygopinath.launchchat.screens.history.domain.UpdateActivityNoteUseCase
import org.vinaygopinath.launchchat.utils.CoroutineUtil
import org.vinaygopinath.launchchat.utils.DispatcherUtil
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dispatcherUtil: DispatcherUtil,
    getDetailedActivitiesUseCase: GetDetailedActivitiesUseCase,
    private val deleteSelectedActivitiesUseCase: DeleteSelectedActivitiesUseCase,
    private val updateActivityNoteUseCase: UpdateActivityNoteUseCase
) : ViewModel() {

    private val internalUiState = MutableStateFlow<UiState>(UiState.None)
    val uiState: StateFlow<UiState> = internalUiState.asStateFlow()

    val detailedActivities = getDetailedActivitiesUseCase.execute()
        .distinctUntilChanged()
        .cachedIn(viewModelScope)

    fun deleteSelectedActivitiesAndActions(activityIds: Set<Long>) {
        CoroutineUtil.doWorkInBackground(
            viewModelScope = viewModelScope,
            dispatcherUtil = dispatcherUtil,
            doWork = { deleteSelectedActivitiesUseCase.execute(activityIds) },
            onComplete = {
                internalUiState.update { UiState.DeleteSuccessful }
            }
        )
    }

    fun updateNote(activityId: Long, note: String?) {
        CoroutineUtil.doWorkInBackground(
            viewModelScope = viewModelScope,
            dispatcherUtil = dispatcherUtil,
            doWork = { updateActivityNoteUseCase.execute(activityId, note) },
            onComplete = {
                internalUiState.update { UiState.NoteUpdated }
            }
        )
    }

    sealed class UiState {
        data object None : UiState()
        data object DeleteSuccessful : UiState()
        data object NoteUpdated : UiState()
    }
}
