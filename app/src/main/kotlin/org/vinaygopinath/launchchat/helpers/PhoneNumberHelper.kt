package org.vinaygopinath.launchchat.helpers

import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Inject

class PhoneNumberHelper @Inject constructor(private val phoneNumberUtil: PhoneNumberUtil) {

    companion object {
        private const val MINIMUM_PHONE_NUMBER_LENGTH = 7
    }

    private val phoneNumberRegex by lazy {
        Regex("(?:tel:)?(\\+?[\\d- ()]{$MINIMUM_PHONE_NUMBER_LENGTH,})")
    }

    private val invalidPhoneNumberCharactersRegex by lazy {
        Regex("[-() ]*")
    }

    private val phoneNumberInputRegex by lazy {
        Regex("^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$")
    }

    fun doesTextMatchPhoneNumberRegex(text: String): Boolean {
        return phoneNumberInputRegex.matches(text)
    }

    fun containsPhoneNumbers(text: String): Boolean {
        return extractPhoneNumbers(text).isNotEmpty()
    }

    fun extractPhoneNumbers(rawString: String): List<String> {
        val matches = phoneNumberRegex.findAll(rawString)
        return matches.filter { matchResult -> matchResult.groupValues.size == 2 }
            .map { matchResult -> matchResult.groupValues[1] }
            .filter { match -> match.isNotBlank() }
            .map { match -> match.replace(invalidPhoneNumberCharactersRegex, "") }
            .filter { phoneNumber -> phoneNumber.length >= MINIMUM_PHONE_NUMBER_LENGTH }
            .toList()
    }

    fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.filter { it.isDigit() || it == '+' }
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
