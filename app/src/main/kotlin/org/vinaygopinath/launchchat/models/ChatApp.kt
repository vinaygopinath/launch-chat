package org.vinaygopinath.launchchat.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "chat_apps",
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
    @ColumnInfo("is_enabled") val isEnabled: Boolean
) {

    enum class IdentifierType(val internalName: String) {
        PHONE_NUMBER_ONLY("phone_number_only"),
        USERNAME_ONLY("username_only"),
        BOTH_PHONE_NUMBER_AND_USERNAME("both_phone_number_and_username")
    }

    enum class LaunchType(val internalName: String) {
        URL_ONLY("url_only"),
        INTENT_ONLY("intent_only"),
        BOTH_USER_AND_INTENT("both_url_and_intent")
    }
}