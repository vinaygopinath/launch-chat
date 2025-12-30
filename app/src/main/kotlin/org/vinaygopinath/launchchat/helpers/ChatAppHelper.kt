package org.vinaygopinath.launchchat.helpers

import androidx.annotation.DrawableRes
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.ChatApp
import javax.inject.Inject

class ChatAppHelper @Inject constructor() {

    @DrawableRes
    fun getIconResource(chatApp: ChatApp): Int? {
        return if (chatApp.isPredefined) {
            getPredefinedChatAppIcon(chatApp.name)
        } else {
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
            else -> null
        }
    }
}
