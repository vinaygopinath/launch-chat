package org.vinaygopinath.launchchat.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            childColumns = ["activity_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("activity_id")
    ]
)
data class Action(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo("activity_id") val activityId: Long,
    @ColumnInfo("phone_number") val phoneNumber: String,
    @ColumnInfo val type: Type,
    @ColumnInfo("occurred_at") val occurredAt: Instant
) {
    enum class Type {
        WHATSAPP,
        SIGNAL,
        TELEGRAM,
        WHATSAPP_BUSINESS,
        SMS,
        PHONE_CALL,
        OTHER;

        companion object {
            fun fromChatAppName(name: String): Type {
                return when (name) {
                    ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME -> WHATSAPP
                    ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_BUSINESS_NAME -> WHATSAPP_BUSINESS
                    ChatApp.PREDEFINED_CHAT_APP_SIGNAL_NAME -> SIGNAL
                    ChatApp.PREDEFINED_CHAT_APP_TELEGRAM_NAME -> TELEGRAM
                    ChatApp.PREDEFINED_CHAT_APP_SMS_NAME -> SMS
                    ChatApp.PREDEFINED_CHAT_APP_PHONE_CALL_NAME -> PHONE_CALL
                    else -> OTHER
                }
            }
        }
    }
}
