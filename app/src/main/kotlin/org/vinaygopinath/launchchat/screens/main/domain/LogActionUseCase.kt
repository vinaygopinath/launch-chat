package org.vinaygopinath.launchchat.screens.main.domain

import org.vinaygopinath.launchchat.models.Action
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.Activity.Source
import org.vinaygopinath.launchchat.repositories.ActionRepository
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.utils.DateUtils
import java.time.Instant
import javax.inject.Inject

class LogActionUseCase @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val activityRepository: ActivityRepository,
    private val actionRepository: ActionRepository,
    private val dateUtils: DateUtils
) {

    suspend fun execute(
        type: Action.Type,
        number: String,
        message: String?,
        activity: Activity?,
        rawInputText: String,
        source: Source = Source.MANUAL_INPUT
    ): Activity? {
        if (!getSettingsUseCase.execute().isActivityHistoryEnabled) {
            return null
        }

        val currentTime = dateUtils.getCurrentInstant()
        val associatedActivity = activity ?: createActivity(message, rawInputText, currentTime, source)

        actionRepository.create(
            Action(
                activityId = associatedActivity.id,
                phoneNumber = number,
                type = type,
                occurredAt = currentTime
            )
        )

        return associatedActivity
    }

    private suspend fun createActivity(
        message: String?,
        rawInputText: String,
        currentTime: Instant,
        source: Source
    ): Activity {
        val activity = Activity(
            content = rawInputText,
            source = source,
            message = message,
            occurredAt = currentTime
        )

        return activityRepository.create(activity)
    }
}
