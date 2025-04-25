package org.vinaygopinath.launchchat.helpers

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import org.vinaygopinath.launchchat.Constants
import javax.inject.Inject

class IntentHelper @Inject constructor() {

    fun getGithubRepoIntent(): Intent {
        return Intent().apply {
            action = Intent.ACTION_VIEW
            data = Constants.GITHUB_REPO_URL.toUri()
        }
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
        return "https://signal.me/#p/${phoneNumber}"
    }

    @VisibleForTesting
    fun generateTelegramUrl(phoneNumber: String): String {
        return "https://t.me/${phoneNumber}"
    }


}