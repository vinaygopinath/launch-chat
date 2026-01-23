package org.vinaygopinath.launchchat.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.ChatApp
import java.io.File
import javax.inject.Inject

class ChatAppHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getIconDrawable(chatApp: ChatApp): Drawable {
        return when {
            chatApp.isPredefined -> {
                getPredefinedChatAppIcon(chatApp.name)?.let {
                    ContextCompat.getDrawable(context, it)
                } ?: getDefaultIcon()
            }
            chatApp.iconUri != null -> {
                loadIconFromPath(chatApp.iconUri) ?: getDefaultIcon()
            }
            else -> getDefaultIcon()
        }
    }

    @DrawableRes
    fun getIconResource(chatApp: ChatApp): Int? {
        return if (chatApp.isPredefined) {
            getPredefinedChatAppIcon(chatApp.name)
        } else {
            null
        }
    }

    private fun getDefaultIcon(): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.ic_chat_app_default)!!
    }

    private fun loadIconFromPath(path: String): Drawable? {
        return try {
            val file = File(path)
            if (file.exists()) {
                Drawable.createFromPath(path)
            } else {
                null
            }
        } catch (expected: Exception) {
            null
        }
    }

    @DrawableRes
    private fun getPredefinedChatAppIcon(name: String): Int? {
        return when (name) {
            ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME -> R.drawable.ic_whatsapp
            ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_BUSINESS_NAME -> R.drawable.ic_whatsapp_business
            ChatApp.PREDEFINED_CHAT_APP_SIGNAL_NAME -> R.drawable.ic_signal
            ChatApp.PREDEFINED_CHAT_APP_TELEGRAM_NAME -> R.drawable.ic_telegram
            ChatApp.PREDEFINED_CHAT_APP_SMS_NAME -> R.drawable.ic_sms
            ChatApp.PREDEFINED_CHAT_APP_PHONE_CALL_NAME -> R.drawable.ic_phone_call
            else -> null
        }
    }
}
