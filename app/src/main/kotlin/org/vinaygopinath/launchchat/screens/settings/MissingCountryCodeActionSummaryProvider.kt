package org.vinaygopinath.launchchat.screens.settings

import androidx.preference.DropDownPreference
import androidx.preference.Preference
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.Settings
import org.vinaygopinath.launchchat.models.Settings.MissingCountryCodeAction.DefaultCountryCode
import org.vinaygopinath.launchchat.models.Settings.MissingCountryCodeAction.RecentCountryCode
import org.vinaygopinath.launchchat.utils.PreferenceUtil

class MissingCountryCodeActionSummaryProvider(
    private val preferenceUtil: PreferenceUtil
) : Preference.SummaryProvider<DropDownPreference> {

    override fun provideSummary(preference: DropDownPreference): CharSequence? {
        val settings = Settings.build(preferenceUtil)
        val context = preference.context
        return when (settings.missingCountryCodeAction) {
            is DefaultCountryCode -> {
                val defaultCountryCode = settings.missingCountryCodeAction.defaultCountryCode
                if (defaultCountryCode == null) {
                    context.getString(R.string.pref_missing_country_code_action_description_default_country_code_missing)
                } else {
                    context.getString(
                        R.string.pref_missing_country_code_action_description_default_country_code_available,
                        defaultCountryCode
                    )
                }
            }

            is RecentCountryCode -> {
                val recentCountryCode = settings.missingCountryCodeAction.recentCountryCode
                if (recentCountryCode == null) {
                    context.getString(R.string.pref_missing_country_code_action_description_recent_country_code_missing)
                } else {
                    context.getString(
                        R.string.pref_missing_country_code_action_description_recent_country_code_available,
                        recentCountryCode
                    )
                }
            }
        }
    }
}