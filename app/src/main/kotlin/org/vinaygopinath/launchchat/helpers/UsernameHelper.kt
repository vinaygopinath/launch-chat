package org.vinaygopinath.launchchat.helpers

import javax.inject.Inject

class UsernameHelper @Inject constructor() {

    private val usernameRegex by lazy {
        Regex("[a-zA-Z0-9][a-zA-Z0-9_.]{2,15}")
    }

    fun containsUsernames(text: String): Boolean {
        return usernameRegex.containsMatchIn(text)
    }

    fun extractUsernames(rawString: String): List<String> {
        val matches = usernameRegex.findAll(rawString)
        return matches.toList().map { it.groupValues[0] }
    }
}