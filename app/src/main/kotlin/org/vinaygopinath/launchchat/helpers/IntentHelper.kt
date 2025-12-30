package org.vinaygopinath.launchchat.helpers

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import org.vinaygopinath.launchchat.Constants
import org.vinaygopinath.launchchat.models.ChatApp
import javax.inject.Inject

class IntentHelper @Inject constructor() {

    fun getGithubRepoIntent(): Intent {
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = Constants.GITHUB_REPO_URL.toUri()
        }
    }

    fun getChatAppIntent(chatApp: ChatApp, phoneNumber: String, message: String?): Intent {
        val url = buildChatAppUrl(chatApp, phoneNumber, message)
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = url.toUri()
            chatApp.intentPackageSelection?.let { setPackage(it) }
        }
    }

    private fun buildChatAppUrl(chatApp: ChatApp, phoneNumber: String, message: String?): String {
        val urlTemplate = chatApp.phoneNumberLaunchUrl
            ?: throw IllegalArgumentException("ChatApp ${chatApp.name} does not have a phoneNumberLaunchUrl")

        return urlTemplate
            .replace(PLACEHOLDER_PHONE_NUMBER, phoneNumber)
            .replace(PLACEHOLDER_MESSAGE, message ?: "")
    }

    fun getOpenWhatsappIntent(phoneNumber: String, message: String?): Intent {
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = generateWhatsappUrl(phoneNumber, message).toUri()
        }
    }

    fun getOpenSignalIntent(phoneNumber: String): Intent {
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = generateSignalUrl(phoneNumber).toUri()
        }
    }

    fun getOpenTelegramIntent(phoneNumber: String): Intent {
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = generateTelegramUrl(phoneNumber).toUri()
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
        private const val PLACEHOLDER_MESSAGE = "[message]"
    }
}
