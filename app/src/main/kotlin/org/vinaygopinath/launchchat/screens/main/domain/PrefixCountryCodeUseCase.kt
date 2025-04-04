package org.vinaygopinath.launchchat.screens.main.domain

import org.vinaygopinath.launchchat.helpers.PhoneNumberHelper
import org.vinaygopinath.launchchat.models.Settings
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_RECENT_COUNTRY_CODE
import org.vinaygopinath.launchchat.models.Settings.MissingCountryCodeAction.DefaultCountryCode
import org.vinaygopinath.launchchat.models.Settings.MissingCountryCodeAction.RecentCountryCode
import org.vinaygopinath.launchchat.utils.PreferenceUtil
import javax.inject.Inject

class PrefixCountryCodeUseCase @Inject constructor(
    private val preferenceUtil: PreferenceUtil,
    private val phoneNumberHelper: PhoneNumberHelper
) {

    fun execute(rawPhoneNumber: String): String {
        return if (rawPhoneNumber.startsWith("+")) {
            phoneNumberHelper.extractCountryCodeFromInternationalPhoneNumber(rawPhoneNumber)?.let {
                preferenceUtil.setInt(KEY_RECENT_COUNTRY_CODE, it)
            }
            return rawPhoneNumber
        } else {
            val missingCountryCodeAction = Settings.build(preferenceUtil).missingCountryCodeAction
            when {
                missingCountryCodeAction is RecentCountryCode && missingCountryCodeAction.recentCountryCode != null -> {
                    phoneNumberHelper.buildInternationalPhoneNumberString(rawPhoneNumber, missingCountryCodeAction.recentCountryCode)
                }
                missingCountryCodeAction is DefaultCountryCode && missingCountryCodeAction.defaultCountryCode != null -> {
                    phoneNumberHelper.buildInternationalPhoneNumberString(rawPhoneNumber, missingCountryCodeAction.defaultCountryCode)
                }
                else -> {
                    rawPhoneNumber
                }
            }
        }
    }
}