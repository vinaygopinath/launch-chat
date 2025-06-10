package org.vinaygopinath.launchchat.screens.main.domain

import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.utils.DateUtils
import javax.inject.Inject

class LogActivityFromHistoryUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val dateUtils: DateUtils
) {

    suspend fun execute(activity: Activity): ProcessIntentUseCase.ProcessedIntent {
        val newHistoryActivity = activityRepository.create(
            activity.copy(
                id = 0L,
                occurredAt = dateUtils.getCurrentInstant(),
                source = Activity.Source.HISTORY
            )
        )
        val extractedContent = ProcessIntentUseCase.ExtractedContent.PossibleResult(
            source = Activity.Source.HISTORY,
            rawInputText = newHistoryActivity.content,
            rawContent = ""
        )

        return ProcessIntentUseCase.ProcessedIntent(extractedContent, newHistoryActivity)
    }
}
