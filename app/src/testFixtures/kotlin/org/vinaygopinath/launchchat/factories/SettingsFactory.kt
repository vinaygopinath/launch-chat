package org.vinaygopinath.launchchat.factories

import org.vinaygopinath.launchchat.models.Settings
import org.vinaygopinath.launchchat.models.Settings.MissingCountryCodeAction

object SettingsFactory {

    fun build(
        isActivityHistoryEnabled: Boolean = true,
        missingCountryCodeAction: MissingCountryCodeAction = MissingCountryCodeAction.DefaultCountryCode("+238")
    ): Settings = Settings(
        isActivityHistoryEnabled = isActivityHistoryEnabled,
        missingCountryCodeAction = missingCountryCodeAction
    )
}