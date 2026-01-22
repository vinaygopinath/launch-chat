package org.vinaygopinath.launchchat.screens.main.domain

import org.vinaygopinath.launchchat.helpers.PhoneNumberHelper
import org.vinaygopinath.launchchat.helpers.UsernameHelper
import org.vinaygopinath.launchchat.models.ChatApp
import javax.inject.Inject

class LaunchChatUseCase @Inject constructor(
    private val phoneNumberHelper: PhoneNumberHelper,
    private val usernameHelper: UsernameHelper,
    private val prefixCountryCodeUseCase: PrefixCountryCodeUseCase
) {

    fun execute(
        inputString: String,
        chatApp: ChatApp,
        messageInputString: String?
    ): LaunchChatResult {
        if (phoneNumberHelper.containsPhoneNumbers(inputString)) {
            return handlePhoneNumbers(inputString, chatApp, messageInputString)
        } else if (usernameHelper.containsUsernames(inputString)) {
            return handleUsernames(inputString, chatApp, messageInputString)
        }

        return LaunchChatResult.NoIdentifierFoundError
    }

    fun executeAfterSinglePhoneNumberSelection(
        selectedPhoneNumber: String,
        chatApp: ChatApp,
        messageInputString: String?
    ): LaunchChatResult {
        return LaunchChatResult.PhoneNumberLaunch(
            phoneNumber = prefixCountryCodeUseCase.execute(selectedPhoneNumber),
            message = if (messageInputString.isNullOrBlank()) {
                null
            } else {
                messageInputString
            },
            chatApp = chatApp
        )
    }

    private fun handlePhoneNumbers(
        inputString: String,
        chatApp: ChatApp,
        messageInputString: String?
    ): LaunchChatResult {
        val phoneNumbers = phoneNumberHelper.extractPhoneNumbers(inputString)
        return if (phoneNumbers.size > 1) {
            LaunchChatResult.MultiplePhoneNumbersDetected(phoneNumbers)
        } else if (phoneNumbers.size == 1) {
            if (chatApp.identifierType.supportsPhoneNumbers()) {
                executeAfterSinglePhoneNumberSelection(
                    phoneNumbers.first(),
                    chatApp,
                    messageInputString
                )
            } else {
                LaunchChatResult.ChatAppDoesNotSupportPhoneNumbersError
            }
        } else {
            error(
                """
                    handlePhoneNumbers was called when the input string
                    did not contain any phone numbers. Did you forget a check?
                """.trimIndent()
            )
        }
    }

    private fun handleUsernames(
        inputString: String,
        chatApp: ChatApp,
        messageInputString: String?
    ): LaunchChatResult {
        val usernames = usernameHelper.extractUsernames(inputString)
        return if (usernames.size == 1) {
            if (chatApp.identifierType.supportsUsernames()) {
                LaunchChatResult.UsernameLaunch(
                    username = usernames.first(),
                    message = if (messageInputString.isNullOrBlank()) {
                        null
                    } else {
                        messageInputString
                    },
                    chatApp = chatApp
                )
            } else {
                LaunchChatResult.ChatAppDoesNotSupportUsernamesError
            }
        } else if (usernames.size > 1) {
            LaunchChatResult.MultipleUsernamesDetected(usernames)
        } else {
            error(
                """
                    handleUsernames was called when the input string
                    did not contain any usernames. Did you forget a check?
                """.trimIndent()
            )
        }
    }

    companion object {
        sealed class LaunchChatResult {
            data class PhoneNumberLaunch(
                val phoneNumber: String,
                val message: String?,
                val chatApp: ChatApp
            ) : LaunchChatResult()

            data class UsernameLaunch(
                val username: String,
                val message: String?,
                val chatApp: ChatApp
            ) : LaunchChatResult()

            data class MultiplePhoneNumbersDetected(val phoneNumbers: List<String>) :
                LaunchChatResult()

            data class MultipleUsernamesDetected(val usernames: List<String>) :
                LaunchChatResult()

            data object NoIdentifierFoundError : LaunchChatResult()
            data object ChatAppDoesNotSupportPhoneNumbersError : LaunchChatResult()
            data object ChatAppDoesNotSupportUsernamesError : LaunchChatResult()
        }
    }
}
