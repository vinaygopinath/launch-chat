package org.vinaygopinath.launchchat.screens.settings

import androidx.preference.EditTextPreference
import androidx.preference.Preference
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_DEFAULT_COUNTRY_CODE
import org.vinaygopinath.launchchat.utils.PreferenceUtil

class DefaultCountryCodeSummaryProvider(private val preferenceUtil: PreferenceUtil) : Preference.SummaryProvider<EditTextPreference> {
    override fun provideSummary(preference: EditTextPreference): CharSequence? {
        val defaultCountryCode = preferenceUtil.getString(KEY_DEFAULT_COUNTRY_CODE, null)
        val context = preference.context
        return if (defaultCountryCode == null) {
            context.getString(R.string.pref_default_country_code_description_missing)
        } else {
            context.getString(R.string.pref_default_country_code_description_set, defaultCountryCode)
        }
    }
}