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

    // Inputs
    // 1. Raw string
    // 2. Selected chat app

    // Algorithm
    // 1. Process phone number or username input field
    // 2. If phone number, check if it contains one phone number or multiple
    //    a. If single phone number, check if the chat app supports phone numbers
    //         a1. If supported, delegate to country code use case
    //             a1a. Launch phone number intent
    //         a2. If not supported, reject with unsupported_phone_number error
    //    b. If multiple, return result with extracted phone numbers back to the activity
    // 3. If username, check if the chat app supports usernames
    //    3a. If supported, launch chat with username intent and/or URL
    // 4. Before launching action (a1a and 3a), log an action!


    // Actual launch intent process
    // If intent is available,
    //   and package selection is available, launch with intent with package selection
    //     If it fails, launch without package selection
    //   and package selection is not available, launch with intent and no package selection
    //     If it fails, launch with URL + package selection
    //     If it fails, launch with URL and no package selection.
    suspend fun execute(
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

    suspend fun executeAfterSinglePhoneNumberSelection(
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

    private suspend fun handlePhoneNumbers(
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