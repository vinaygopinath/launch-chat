package org.vinaygopinath.launchchat.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.vinaygopinath.launchchat.screens.history.domain.DeleteActivitiesUseCase
import org.vinaygopinath.launchchat.screens.history.domain.GetDetailedActivitiesUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository

@HiltViewModel
class HistoryViewModel @Inject constructor(
    useCase: GetDetailedActivitiesUseCase,
    private val deleteActivitiesUseCase: DeleteActivitiesUseCase,
    private val actionRepository: ActionRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    val detailedActivities = useCase.execute()
        .distinctUntilChanged()
        .cachedIn(viewModelScope)

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    fun toggleSelection(id: Long) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelectedActivities() {
        val ids = _selectedIds.value
        if (ids.isNotEmpty()) {
            viewModelScope.launch {
                deleteActivitiesUseCase.deleteActivitiesAndActions(ids)
                clearSelection()
            }
        }
    }

    fun deleteActivitiesAndActions(activities: List<DetailedActivity>){
        viewModelScope.launch {
            activities.forEach{ activity ->
                actionRepository.deleteActionsByActivityIds(activity.activity.id)
                activityRepository.delete(activity.activity)
            }
        }
    }
}