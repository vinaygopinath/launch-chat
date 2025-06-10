package org.vinaygopinath.launchchat.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.vinaygopinath.launchchat.screens.history.domain.GetDetailedActivitiesUseCase
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    useCase: GetDetailedActivitiesUseCase
) : ViewModel() {

    val detailedActivities = useCase.execute()
        .distinctUntilChanged()
        .cachedIn(viewModelScope)
}
