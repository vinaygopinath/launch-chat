package org.vinaygopinath.launchchat.helpers

import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Inject

class PhoneNumberHelper @Inject constructor(private val phoneNumberUtil: PhoneNumberUtil) {

    private val phoneNumberRegex by lazy {
        Regex("(?:tel:)?(\\+?[\\d- ()]+)")
    }

    private val invalidPhoneNumberCharactersRegex by lazy {
        Regex("[-() ]*")
    }

    fun extractPhoneNumbers(rawString: String): List<String> {
        val matches = phoneNumberRegex.findAll(rawString)
        return matches.filter { matchResult -> matchResult.groupValues.size == 2 }
            .map { matchResult -> matchResult.groupValues[1] }
            .filter { match -> match.isNotBlank() }
            .map { match -> match.replace(invalidPhoneNumberCharactersRegex, "") }
            .toList()
    }

    fun extractCountryCodeFromInternationalPhoneNumber(rawPhoneNumberWithCountryCode: String): Int? {
        return try {
            phoneNumberUtil.parse(rawPhoneNumberWithCountryCode, "").countryCode
        } catch (_: NumberParseException) {
            null
        }
    }

    fun buildInternationalPhoneNumberString(
        rawPhoneNumberWithoutCountryCode: String,
        countryCode: Int
    ): String {
        return try {
            val phoneNumber = phoneNumberUtil.parse(
                rawPhoneNumberWithoutCountryCode,
                phoneNumberUtil.getRegionCodeForCountryCode(countryCode)
            )
            phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (_: NumberParseException) {
            rawPhoneNumberWithoutCountryCode
        }
    }
}
