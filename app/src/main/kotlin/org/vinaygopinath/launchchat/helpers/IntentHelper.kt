package org.vinaygopinath.launchchat.helpers

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import org.vinaygopinath.launchchat.Constants
import org.vinaygopinath.launchchat.models.ChatApp
import javax.inject.Inject

class IntentHelper @Inject constructor() {

    /**
     * Contains the intents available for launching a chat app.
     * Caller should try appIntent first, then fall back to urlIntent if appIntent fails.
     */
    data class ChatAppLaunchIntents(
        val appIntent: Intent?,
        val urlIntent: Intent?
    )

    fun getGithubRepoIntent(): Intent {
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = Constants.GITHUB_REPO_URL.toUri()
        }
    }

    fun getChatAppIntentsForPhoneNumber(chatApp: ChatApp, phoneNumber: String, message: String?): ChatAppLaunchIntents {
        require(chatApp.identifierType.supportsPhoneNumbers()) {
            "ChatApp ${chatApp.name} does not support phone numbers"
        }

        val format = chatApp.phoneNumberFormat ?: ChatApp.PhoneNumberFormat.WITH_PLUS_PREFIX
        val formattedNumber = formatPhoneNumber(phoneNumber, format)

        val appIntent = chatApp.phoneNumberLaunchIntent?.let { template ->
            val uri = buildFromTemplate(template, formattedNumber, null, message)
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = uri.toUri()
                chatApp.intentPackageSelection?.let { setPackage(it) }
            }
        }

        val urlIntent = chatApp.phoneNumberLaunchUrl?.let { template ->
            val uri = buildFromTemplate(template, formattedNumber, null, message)
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = uri.toUri()
            }
        }

        return ChatAppLaunchIntents(appIntent, urlIntent)
    }

    fun getChatAppIntentsForUsername(chatApp: ChatApp, username: String, message: String?): ChatAppLaunchIntents {
        require(chatApp.identifierType.supportsUsernames()) {
            "ChatApp ${chatApp.name} does not support usernames"
        }

        val appIntent = chatApp.usernameLaunchIntent?.let { template ->
            val uri = buildFromTemplate(template, null, username, message)
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = uri.toUri()
                chatApp.intentPackageSelection?.let { setPackage(it) }
            }
        }

        val urlIntent = chatApp.usernameLaunchUrl?.let { template ->
            val uri = buildFromTemplate(template, null, username, message)
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = uri.toUri()
            }
        }

        return ChatAppLaunchIntents(appIntent, urlIntent)
    }

    private fun buildFromTemplate(
        template: String,
        phoneNumber: String?,
        username: String?,
        message: String?
    ): String {
        return template
            .replace(PLACEHOLDER_PHONE_NUMBER, phoneNumber ?: "")
            .replace(PLACEHOLDER_USERNAME, username ?: "")
            .replace(PLACEHOLDER_MESSAGE, message ?: "")
    }

    private fun formatPhoneNumber(phoneNumber: String, format: ChatApp.PhoneNumberFormat): String {
        return when (format) {
            ChatApp.PhoneNumberFormat.WITH_PLUS_PREFIX -> {
                if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"
            }
            ChatApp.PhoneNumberFormat.WITHOUT_PLUS_PREFIX -> {
                phoneNumber.removePrefix("+")
            }
            ChatApp.PhoneNumberFormat.RAW -> phoneNumber
        }
    }

    @VisibleForTesting
    fun generateWhatsappUrl(phoneNumber: String, message: String?): String {
        val builder = StringBuilder()
        builder.append("https://wa.me/$phoneNumber/")
        if (message != null) {
            builder.append("?text=$message")
        }

        return builder.toString()
    }

    @VisibleForTesting
    fun generateSignalUrl(phoneNumber: String): String {
        return "https://signal.me/#p/$phoneNumber"
    }

    @VisibleForTesting
    fun generateTelegramUrl(phoneNumber: String): String {
        return "https://t.me/$phoneNumber"
    }

    companion object {
        private const val PLACEHOLDER_PHONE_NUMBER = "[phone-number]"
        private const val PLACEHOLDER_USERNAME = "[username]"
        private const val PLACEHOLDER_MESSAGE = "[message]"
    }
}
