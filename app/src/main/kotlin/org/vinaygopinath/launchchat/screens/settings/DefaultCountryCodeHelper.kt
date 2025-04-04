package org.vinaygopinath.launchchat.screens.settings

object DefaultCountryCodeHelper {

    fun isValidCountryCode(possibleCountryCode: String?): Boolean {
        return try {
            when {
                possibleCountryCode == null -> false
                Integer.parseInt(possibleCountryCode) > 0 -> true
                else -> false
            }
        } catch (_: NumberFormatException) {
            false
        }
    }
}