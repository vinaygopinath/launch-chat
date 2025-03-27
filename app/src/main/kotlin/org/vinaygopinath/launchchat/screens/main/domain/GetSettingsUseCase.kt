package org.vinaygopinath.launchchat.screens.main.domain

import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.Settings
import org.vinaygopinath.launchchat.utils.PreferenceUtil
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val preferenceUtil: PreferenceUtil
) {

    fun execute(): Settings = Settings(
        isActivityHistoryEnabled = preferenceUtil.getBoolean(R.string.pref_activity_history_key, true),
        missingCountryCodeAction = Settings.MissingCountryCodeAction.build(preferenceUtil)
    )
}