package org.vinaygopinath.launchchat.helpers

object TextHelper {

    private val phoneNumberRegex = Regex("^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$")

    fun doesTextMatchPhoneNumberRegex(text: String): Boolean {
        return phoneNumberRegex.matches(text)
    }
}
