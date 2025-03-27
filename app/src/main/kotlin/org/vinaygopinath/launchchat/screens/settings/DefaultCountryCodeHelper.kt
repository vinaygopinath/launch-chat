package org.vinaygopinath.launchchat.screens.settings

import java.util.regex.Pattern

object DefaultCountryCodeHelper {

    fun isValidCountryCode(possibleCountryCode: String?): Boolean {
        return when {
            possibleCountryCode == null -> false
            COUNTRY_CODE_REGEX.matches(possibleCountryCode) -> true
            else -> false
        }
    }

    private val COUNTRY_CODE_REGEX = Regex("\\+\\d{1,3}")
}