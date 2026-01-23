package org.vinaygopinath.launchchat.screens.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.Settings
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_DEFAULT_COUNTRY_CODE
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_MANAGE_CHAT_APPS
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_MISSING_COUNTRY_CODE_ACTION
import org.vinaygopinath.launchchat.models.Settings.MissingCountryCodeAction.DefaultCountryCode
import org.vinaygopinath.launchchat.screens.chatapps.ChatAppListActivity
import org.vinaygopinath.launchchat.screens.settings.DefaultCountryCodeHelper.isValidCountryCode
import org.vinaygopinath.launchchat.utils.PreferenceUtil
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(KEY_MANAGE_CHAT_APPS)?.setOnPreferenceClickListener {
            startActivity(ChatAppListActivity.getIntent(requireContext()))
            true
        }

        val settings = Settings.build(preferenceUtil)
        val missingCountryCodePref =
            findPreference<DropDownPreference>(KEY_MISSING_COUNTRY_CODE_ACTION)
        val defaultCountryCodePref = findPreference<IntPreference>(KEY_DEFAULT_COUNTRY_CODE)
        missingCountryCodePref?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val isNewValueDefaultCountryCode =
                    newValue == Settings.VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_DEFAULT
                defaultCountryCodePref?.isVisible = isNewValueDefaultCountryCode
                if (!isNewValueDefaultCountryCode) {
                    preferenceUtil.clear(KEY_DEFAULT_COUNTRY_CODE)
                }

                true
            }
        missingCountryCodePref?.summaryProvider =
            MissingCountryCodeActionSummaryProvider(preferenceUtil)
        defaultCountryCodePref?.isVisible = settings.missingCountryCodeAction is DefaultCountryCode
        defaultCountryCodePref?.summaryProvider = DefaultCountryCodeSummaryProvider(preferenceUtil)
        defaultCountryCodePref?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val isNewValueValid = isValidCountryCode(newValue as String?)
                if (isNewValueValid) {
                    /*
                                   Preference does not provide a way to refresh the summary,
                                   so we're toggling its visibility as a workaround.
                     */
                    missingCountryCodePref?.isVisible = false
                    missingCountryCodePref?.isVisible = true
                } else {
                    Toast.makeText(
                        context,
                        R.string.pref_default_country_code_invalid_format,
                        LENGTH_LONG
                    ).show()
                }

                isNewValueValid
            }
    }
}
