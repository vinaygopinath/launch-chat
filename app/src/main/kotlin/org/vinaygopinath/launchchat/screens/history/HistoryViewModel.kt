package org.vinaygopinath.launchchat.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.vinaygopinath.launchchat.screens.history.domain.GetDetailedActivitiesUseCase
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository

@HiltViewModel
class HistoryViewModel @Inject constructor(
    useCase: GetDetailedActivitiesUseCase,
    private val actionRepository: ActionRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    val detailedActivities = useCase.execute()
        .distinctUntilChanged()
        .cachedIn(viewModelScope)

    fun deleteActivitiesAndActions(activities: List<DetailedActivity>){
        viewModelScope.launch {
          val acticityIds = activities.map { it.activity.id }
            actionRepository.deleteActionsByActivityIds(acticityIds)
            activityRepository.deleteActivitiesByIds(acticityIds)
        }
    }
}