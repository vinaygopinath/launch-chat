package org.vinaygopinath.launchchat.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.vinaygopinath.launchchat.models.ChatApp.Companion.TABLE_NAME
import org.vinaygopinath.launchchat.utils.DateUtils
import java.time.Instant

@Entity(
    tableName = TABLE_NAME,
    indices = [

    ]
)
data class ChatApp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val name: String,
    @ColumnInfo("identifier_type") val identifierType: IdentifierType,
    @ColumnInfo("launch_type") val launchType: LaunchType,
    @ColumnInfo("intent_package_selection") val intentPackageSelection: String?,
    @ColumnInfo("phone_number_launch_intent") val phoneNumberLaunchIntent: String?,
    @ColumnInfo("phone_number_launch_url") val phoneNumberLaunchUrl: String?,
    @ColumnInfo("username_launch_intent") val usernameLaunchIntent: String?,
    @ColumnInfo("username_launch_url") val usernameLaunchUrl: String?,
    @ColumnInfo("created_at") val createdAt: Instant,
    @ColumnInfo("deleted_at") val deletedAt: Instant?,
    @ColumnInfo("is_predefined") val isPredefined: Boolean,
    @ColumnInfo("is_enabled") val isEnabled: Boolean,
    @ColumnInfo("icon_uri") val iconUri: String? = null,
    @ColumnInfo("phone_number_format", defaultValue = "with_plus") val phoneNumberFormat: PhoneNumberFormat? = null,
    @ColumnInfo("position") val position: Int = 0
) {

    enum class IdentifierType(val internalName: String) {
        PHONE_NUMBER_ONLY("phone_number_only"),
        USERNAME_ONLY("username_only"),
        BOTH_PHONE_NUMBER_AND_USERNAME("both_phone_number_and_username");

        fun supportsPhoneNumbers(): Boolean {
            return this == PHONE_NUMBER_ONLY || this == BOTH_PHONE_NUMBER_AND_USERNAME
        }

        fun supportsUsernames(): Boolean {
            return this == USERNAME_ONLY || this == BOTH_PHONE_NUMBER_AND_USERNAME
        }
    }

    enum class LaunchType(val internalName: String) {
        URL_ONLY("url_only"),
        INTENT_ONLY("intent_only"),
        BOTH_URL_AND_INTENT("both_url_and_intent")
    }

    enum class PhoneNumberFormat(val internalName: String) {
        WITH_PLUS_PREFIX("with_plus"),
        WITHOUT_PLUS_PREFIX("without_plus"),
        RAW("raw")
    }

    enum class InputType {
        PHONE_NUMBER,
        USERNAME,
        EMPTY
    }

    companion object {
        const val TABLE_NAME = "chat_apps"
        const val PREDEFINED_CHAT_APP_WHATSAPP_NAME = "WhatsApp"
        const val PREDEFINED_CHAT_APP_WHATSAPP_BUSINESS_NAME = "WhatsApp Business"
        const val PREDEFINED_CHAT_APP_SIGNAL_NAME = "Signal"
        const val PREDEFINED_CHAT_APP_TELEGRAM_NAME = "Telegram"
        const val PREDEFINED_CHAT_APP_SMS_NAME = "SMS"
        const val PREDEFINED_CHAT_APP_PHONE_CALL_NAME = "Phone Call"

        fun getPredefinedChatApps(dateUtils: DateUtils): List<ChatApp> {
            val currentTime = dateUtils.getCurrentInstant()
            return listOf(
                ChatApp(
                    name = PREDEFINED_CHAT_APP_WHATSAPP_NAME,
                    identifierType = IdentifierType.PHONE_NUMBER_ONLY,
                    launchType = LaunchType.BOTH_URL_AND_INTENT,
                    intentPackageSelection = "com.whatsapp",
                    phoneNumberLaunchIntent = "whatsapp://send/?phone=[phone-number]&text=[message]",
                    phoneNumberLaunchUrl = "https://wa.me/[phone-number]?text=[message]",
                    usernameLaunchIntent = null,
                    usernameLaunchUrl = null,
                    isPredefined = true,
                    isEnabled = true,
                    createdAt = currentTime,
                    deletedAt = null,
                    phoneNumberFormat = PhoneNumberFormat.WITH_PLUS_PREFIX,
                    position = 0
                ),
                ChatApp(
                    name = PREDEFINED_CHAT_APP_WHATSAPP_BUSINESS_NAME,
                    identifierType = IdentifierType.PHONE_NUMBER_ONLY,
                    launchType = LaunchType.BOTH_URL_AND_INTENT,
                    intentPackageSelection = "com.whatsapp.w4b",
                    phoneNumberLaunchIntent = "whatsapp://send/?phone=[phone-number]&text=[message]",
                    phoneNumberLaunchUrl = "https://wa.me/[phone-number]?text=[message]",
                    usernameLaunchIntent = null,
                    usernameLaunchUrl = null,
                    isPredefined = true,
                    isEnabled = false,
                    createdAt = currentTime,
                    deletedAt = null,
                    phoneNumberFormat = PhoneNumberFormat.WITH_PLUS_PREFIX,
                    position = 1
                ),
                ChatApp(
                    name = PREDEFINED_CHAT_APP_SIGNAL_NAME,
                    identifierType = IdentifierType.BOTH_PHONE_NUMBER_AND_USERNAME,
                    launchType = LaunchType.URL_ONLY,
                    intentPackageSelection = "org.thoughtcrime.securesms",
                    phoneNumberLaunchIntent = null,
                    phoneNumberLaunchUrl = "https://signal.me/#p/[phone-number]",
                    usernameLaunchIntent = null,
                    usernameLaunchUrl = "https://signal.me/#eu/[username]",
                    isPredefined = true,
                    isEnabled = true,
                    createdAt = currentTime,
                    deletedAt = null,
                    phoneNumberFormat = PhoneNumberFormat.WITH_PLUS_PREFIX,
                    position = 2
                ),
                ChatApp(
                    name = PREDEFINED_CHAT_APP_TELEGRAM_NAME,
                    identifierType = IdentifierType.BOTH_PHONE_NUMBER_AND_USERNAME,
                    launchType = LaunchType.URL_ONLY,
                    intentPackageSelection = "org.telegram.messenger",
                    phoneNumberLaunchIntent = null,
                    phoneNumberLaunchUrl = "https://t.me/[phone-number]",
                    usernameLaunchIntent = null,
                    usernameLaunchUrl = "https://t.me/[username]",
                    isPredefined = true,
                    isEnabled = true,
                    createdAt = currentTime,
                    deletedAt = null,
                    phoneNumberFormat = PhoneNumberFormat.WITH_PLUS_PREFIX,
                    position = 3
                ),
                ChatApp(
                    name = PREDEFINED_CHAT_APP_SMS_NAME,
                    identifierType = IdentifierType.PHONE_NUMBER_ONLY,
                    launchType = LaunchType.INTENT_ONLY,
                    intentPackageSelection = null,
                    phoneNumberLaunchIntent = "sms:[phone-number]?body=[message]",
                    phoneNumberLaunchUrl = null,
                    usernameLaunchIntent = null,
                    usernameLaunchUrl = null,
                    isPredefined = true,
                    isEnabled = false,
                    createdAt = currentTime,
                    deletedAt = null,
                    phoneNumberFormat = PhoneNumberFormat.RAW,
                    position = 4
                ),
                ChatApp(
                    name = PREDEFINED_CHAT_APP_PHONE_CALL_NAME,
                    identifierType = IdentifierType.PHONE_NUMBER_ONLY,
                    launchType = LaunchType.INTENT_ONLY,
                    intentPackageSelection = null,
                    phoneNumberLaunchIntent = "tel:[phone-number]",
                    phoneNumberLaunchUrl = null,
                    usernameLaunchIntent = null,
                    usernameLaunchUrl = null,
                    isPredefined = true,
                    isEnabled = false,
                    createdAt = currentTime,
                    deletedAt = null,
                    phoneNumberFormat = PhoneNumberFormat.RAW,
                    position = 5
                )
            )
        }
    }
}
